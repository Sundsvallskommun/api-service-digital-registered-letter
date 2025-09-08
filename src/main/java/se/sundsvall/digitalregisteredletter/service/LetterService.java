package se.sundsvall.digitalregisteredletter.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toLetter;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toLetterEntity;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toLetters;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.digitalregisteredletter.api.model.Attachments;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.LettersBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;

@Service
public class LetterService {

	private final LetterRepository letterRepository;
	private final AttachmentMapper attachmentMapper;

	private final PartyIntegration partyIntegration;
	private final KivraIntegration kivraIntegration;

	public LetterService(final LetterRepository letterRepository,
		final AttachmentMapper attachmentMapper,
		final PartyIntegration partyIntegration,
		final KivraIntegration kivraIntegration) {
		this.letterRepository = letterRepository;
		this.attachmentMapper = attachmentMapper;
		this.partyIntegration = partyIntegration;
		this.kivraIntegration = kivraIntegration;
	}

	public String sendLetter(final String municipalityId, final LetterRequest letterRequest, final Attachments attachments) {
		var letter = persistLetter(municipalityId, letterRequest, attachments);
		var legalId = partyIntegration.getLegalIdByPartyId(municipalityId, letterRequest.partyId());

		var status = kivraIntegration.sendContent(letter, legalId);

		letter.setStatus(status);
		letterRepository.save(letter);
		return letter.getId();
	}

	LetterEntity persistLetter(final String municipalityId, final LetterRequest letterRequest, final Attachments attachments) {
		var attachmentEntities = attachmentMapper.toAttachmentEntities(attachments);

		var letterEntity = toLetterEntity(letterRequest);
		letterEntity.setAttachments(attachmentEntities);
		letterEntity.setMunicipalityId(municipalityId);
		letterEntity.setStatus("NEW");

		return letterRepository.save(letterEntity);
	}

	public Letter getLetter(final String municipalityId, final String letterId) {
		var letter = letterRepository.findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));
		return toLetter(letter);
	}

	public Letters getLetters(final String municipalityId, final Pageable pageable) {
		var page = letterRepository.findAllByMunicipalityIdAndDeleted(municipalityId, false, pageable);

		return LettersBuilder.create()
			.withMetaData(PagingAndSortingMetaData.create().withPageData(page))
			.withLetters(toLetters(page.getContent()))
			.build();
	}

}
