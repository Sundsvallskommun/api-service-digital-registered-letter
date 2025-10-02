package se.sundsvall.digitalregisteredletter.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.api.model.Attachments;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.Letter.Attachment;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.integration.db.RepositoryIntegration;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;
import se.sundsvall.digitalregisteredletter.service.util.IdentifierUtil;

@Service
public class LetterService {

	private final KivraIntegration kivraIntegration;
	private final PartyIntegration partyIntegration;
	private final RepositoryIntegration repositoryIntegration;
	private final LetterMapper letterMapper;

	public LetterService(
		final KivraIntegration kivraIntegration,
		final PartyIntegration partyIntegration,
		final RepositoryIntegration repositoryIntegration,
		final LetterMapper letterMapper) {

		this.kivraIntegration = kivraIntegration;
		this.partyIntegration = partyIntegration;
		this.repositoryIntegration = repositoryIntegration;
		this.letterMapper = letterMapper;
	}

	public Letter sendLetter(final String municipalityId, final LetterRequest letterRequest, final Attachments attachments) {
		final var username = IdentifierUtil.getAdUser();
		final var legalId = partyIntegration.getLegalIdByPartyId(municipalityId, letterRequest.partyId()); // Verify that match for party in request exists as there is no point in persisting entity otherwise

		final var letterEntity = repositoryIntegration.persistLetter(municipalityId, username, letterRequest, attachments); // Create a new entity in database for the letter
		final var status = kivraIntegration.sendContent(letterEntity, legalId); // Send letter to Kivra
		repositoryIntegration.updateStatus(letterEntity, status); // Update entity with status from Kivra response

		return letterMapper.toLetter(letterEntity);
	}

	public Letter getLetter(final String municipalityId, final String letterId) {
		final var letterEntity = getLetterEntity(municipalityId, letterId);

		return letterMapper.toLetter(letterEntity);
	}

	public Letters getLetters(final String municipalityId, final LetterFilter filter, final Pageable pageable) {
		final var page = repositoryIntegration.getPagedLetterEntities(municipalityId, filter, pageable);

		return letterMapper.toLetters(page);
	}

	public SigningInfo getSigningInformation(final String municipalityId, final String letterId) {
		final var letterEntity = repositoryIntegration.getLetterEntity(municipalityId, letterId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));

		return ofNullable(letterMapper.toSigningInfo(letterEntity.getSigningInformation()))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Signing information belonging to letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));
	}

	public Attachment getLetterAttachment(final String municipalityId, final String letterId, final String attachmentId) {
		final var letter = getLetter(municipalityId, letterId);

		return ofNullable(letter.attachments())
			.orElse(emptyList())
			.stream()
			.filter(attachment -> attachment.id().equals(attachmentId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Attachment with id '%s' not found in letter with id '%s'".formatted(attachmentId, letterId)));
	}

	@Transactional(readOnly = true)
	public void writeAttachmentContent(final String municipalityId, final String letterId, final String attachmentId, final OutputStream output) {
		final var attachmentEntity = getAttachmentEntity(municipalityId, letterId, attachmentId);
		final var content = ofNullable(attachmentEntity.getContent())
			.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "No content for attachment with id '%s'".formatted(attachmentId)));

		try (var input = content.getBinaryStream()) {
			StreamUtils.copy(input, output);
		} catch (SQLException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to open content stream for attachment with id '%s'".formatted(attachmentId));
		} catch (IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to stream content for attachment with id '%s'".formatted(attachmentId));
		}
	}

	private AttachmentEntity getAttachmentEntity(final String municipalityId, final String letterId, final String attachmentId) {
		final var letter = getLetterEntity(municipalityId, letterId);

		return ofNullable(letter.getAttachments())
			.orElse(emptyList())
			.stream()
			.filter(attachmentEntity -> attachmentEntity.getId().equals(attachmentId))
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Attachment with id '%s' not found in letter '%s'".formatted(attachmentId, letterId)));
	}

	private LetterEntity getLetterEntity(final String municipalityId, final String letterId) {
		return repositoryIntegration.getLetterEntity(municipalityId, letterId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));
	}
}
