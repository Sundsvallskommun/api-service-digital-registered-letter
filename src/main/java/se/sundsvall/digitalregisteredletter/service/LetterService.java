package se.sundsvall.digitalregisteredletter.service;

import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toLetterEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.api.model.Attachments;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.integration.db.dao.LetterRepository;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;

@Service
public class LetterService {

	private final LetterRepository letterRepository;
	private final ObjectMapper objectMapper;
	private final AttachmentMapper attachmentMapper;

	public LetterService(final LetterRepository letterRepository, final ObjectMapper objectMapper, final AttachmentMapper attachmentMapper) {
		this.letterRepository = letterRepository;
		this.objectMapper = objectMapper;
		this.attachmentMapper = attachmentMapper;
	}

	public String sendLetter(final String municipalityId, final LetterRequest letterRequest, final Attachments attachments) {
		var attachmentEntities = attachmentMapper.toAttachmentEntities(attachments);

		var letterEntity = toLetterEntity(letterRequest);
		letterEntity.setAttachments(attachmentEntities);
		letterEntity.setMunicipalityId(municipalityId);
		letterEntity.setStatus("NEW");
		letterRepository.save(letterEntity);

		// TODO: Use the newly created letter entity to send a request to Kivra. Do this synchronously and update the letter
		// status accordingly.
		return null;
	}

	public Letter getLetter(final String municipalityId, final String letterId) {
		// TODO: Will be implemented in a future task
		return null;
	}

	public Letters getLetters(final String municipalityId, final Pageable pageable) {
		// TODO: Will be implemented in a future task
		return null;
	}

	public LetterRequest parseLetterRequest(final String letterString) {
		try {
			return objectMapper.readValue(letterString, LetterRequest.class);
		} catch (JsonProcessingException e) {
			throw Problem.valueOf(BAD_REQUEST, "Couldn't parse letter request");
		}
	}

}
