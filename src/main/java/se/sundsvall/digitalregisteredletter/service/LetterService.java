package se.sundsvall.digitalregisteredletter.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.digitalregisteredletter.service.util.CustomPredicate.distinctById;
import static se.sundsvall.digitalregisteredletter.service.util.InvoicePdfMerger.mergePdfs;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.Letter.Attachment;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.LetterStatus;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.integration.db.RepositoryIntegration;
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.integration.templating.TemplatingIntegration;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;

@Service
public class LetterService {

	private final KivraIntegration kivraIntegration;
	private final PartyIntegration partyIntegration;
	private final RepositoryIntegration repositoryIntegration;
	private final TenantRepository tenantRepository;
	private final LetterMapper letterMapper;
	private final TemplatingIntegration templatingIntegration;

	public LetterService(
		final KivraIntegration kivraIntegration,
		final PartyIntegration partyIntegration,
		final RepositoryIntegration repositoryIntegration,
		final TenantRepository tenantRepository,
		final LetterMapper letterMapper,
		final TemplatingIntegration templatingIntegration) {

		this.kivraIntegration = kivraIntegration;
		this.partyIntegration = partyIntegration;
		this.repositoryIntegration = repositoryIntegration;
		this.tenantRepository = tenantRepository;
		this.letterMapper = letterMapper;
		this.templatingIntegration = templatingIntegration;
	}

	public Letter sendLetter(final String municipalityId, final String organizationNumber, final LetterRequest letterRequest, final List<MultipartFile> attachments) {
		final var legalId = resolveLegalId(municipalityId, letterRequest);
		final var letterEntity = repositoryIntegration.persistLetter(municipalityId, letterRequest, attachments);
		final var tenant = tenantRepository.findByMunicipalityIdAndOrgNumber(municipalityId, organizationNumber)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No tenant found for municipalityId '%s' and organizationNumber '%s'".formatted(municipalityId, organizationNumber)));
		letterEntity.setTenant(tenant);
		final var status = kivraIntegration.sendContent(letterEntity, legalId, municipalityId, organizationNumber);
		repositoryIntegration.updateStatus(letterEntity, status);

		return letterMapper.toLetter(letterEntity);
	}

	private String resolveLegalId(final String municipalityId, final LetterRequest letterRequest) {
		return partyIntegration.getLegalIdByPartyId(municipalityId, letterRequest.partyId())
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND,
				"No legalId found for partyId '%s' and municipalityId '%s'".formatted(letterRequest.partyId(), municipalityId)));
	}

	public Letter getLetter(final String municipalityId, final String letterId) {
		final var letterEntity = getLetterEntity(municipalityId, letterId);

		return letterMapper.toLetter(letterEntity);
	}

	public Letters getLetters(final String municipalityId, final LetterFilter filter, final Pageable pageable) {
		final var page = repositoryIntegration.getPagedLetterEntities(municipalityId, filter, pageable);

		return letterMapper.toLetters(page);
	}

	@Transactional(readOnly = true)
	public List<LetterStatus> getLetterStatuses(final String municipalityId, final List<String> letterIds) {
		final var lettersById = repositoryIntegration.getLetterEntities(municipalityId, letterIds)
			.stream()
			.collect(toMap(LetterEntity::getId, identity()));

		return letterIds.stream()
			.map(id -> ofNullable(lettersById.get(id))
				.map(letterMapper::toLetterStatus)
				.orElseGet(() -> letterMapper.toLetterStatus(id, null, null)))
			.filter(distinctById(LetterStatus::letterId))
			.toList();
	}

	public SigningInfo getSigningInformation(final String municipalityId, final String letterId) {
		final var letterEntity = repositoryIntegration.getLetterEntity(municipalityId, letterId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));

		return ofNullable(letterMapper.toSigningInfo(letterEntity.getSigningInformation()))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Signing information belonging to letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));
	}

	private Attachment getLetterAttachment(final String municipalityId, final String letterId, final String attachmentId) {
		final var letter = getLetter(municipalityId, letterId);

		return ofNullable(letter.attachments())
			.orElse(emptyList())
			.stream()
			.filter(attachment -> attachment.id().equals(attachmentId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Attachment with id '%s' not found in letter with id '%s'".formatted(attachmentId, letterId)));
	}

	@Transactional(readOnly = true)
	public void readLetterAttachment(final String municipalityId, final String letterId, final String attachmentId, final HttpServletResponse response) {
		final var attachmentEntity = getAttachmentEntity(municipalityId, letterId, attachmentId);
		final var attachment = getLetterAttachment(municipalityId, letterId, attachmentId);

		try {
			final var content = ofNullable(attachmentEntity.getContent())
				.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "No content for attachment with id '%s'".formatted(attachmentId)));

			final var input = content.getBinaryStream();
			writeToResponse(response, attachment.contentType(), "attachment; filename=\"" + attachment.fileName() + "\"", (int) content.length(), input);
			input.close();
		} catch (final SQLException | IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to read attachment with id '%s': %s".formatted(attachmentId, e.getMessage()));
		}
	}

	@Transactional(readOnly = true)
	public void readLetterReceipt(final String municipalityId, final String letterId, final HttpServletResponse response) {
		final var letterEntity = getLetterEntity(municipalityId, letterId);
		final var receipt = templatingIntegration.renderPdf(municipalityId, letterEntity);

		try (final var outputStream = (ByteArrayOutputStream) mergePdfs(letterEntity.getAttachments(), receipt)) {
			final var pdfBytes = outputStream.toByteArray();
			writeToResponse(response, "application/pdf", "attachment; filename=\"kvittens_rekutskick_" + letterId + ".pdf\"", pdfBytes.length, pdfBytes);
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to write receipt content: %s".formatted(e.getMessage()));
		}
	}

	private AttachmentEntity getAttachmentEntity(final String municipalityId, final String letterId, final String attachmentId) {
		return repositoryIntegration.getAttachmentEntity(municipalityId, letterId, attachmentId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Attachment with id '%s' not found in letter '%s'".formatted(attachmentId, letterId)));
	}

	private LetterEntity getLetterEntity(final String municipalityId, final String letterId) {
		return repositoryIntegration.getLetterEntity(municipalityId, letterId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));
	}

	private void writeToResponse(final HttpServletResponse response, final String contentType, final String contentDisposition, final int contentLength, final Object content) throws IOException {
		response.addHeader(CONTENT_TYPE, contentType);
		response.addHeader(CONTENT_DISPOSITION, contentDisposition);
		response.setContentLength(contentLength);

		if (content instanceof final byte[] bytes) {
			response.getOutputStream().write(bytes);
		} else if (content instanceof final InputStream inputStream) {
			StreamUtils.copy(inputStream, response.getOutputStream());
		}
	}

}
