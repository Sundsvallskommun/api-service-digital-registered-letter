package se.sundsvall.digitalregisteredletter.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.LetterResponse;
import se.sundsvall.digitalregisteredletter.api.model.LetterResponses;

@Service
public class LetterService {

	public String sendLetter(final String municipalityId, final LetterRequest request) {
		// TODO: Will be implemented in a future task
		return null;
	}

	public LetterResponse getLetter(final String municipalityId, final String letterId) {
		// TODO: Will be implemented in a future task
		return null;
	}

	public LetterResponses getLetters(final String municipalityId, final Pageable pageable) {
		// TODO: Will be implemented in a future task
		return null;
	}
}
