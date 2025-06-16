package se.sundsvall;

import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequestBuilder;
import se.sundsvall.digitalregisteredletter.api.model.LetterResponse;
import se.sundsvall.digitalregisteredletter.api.model.LetterResponseBuilder;
import se.sundsvall.digitalregisteredletter.api.model.LetterResponses;
import se.sundsvall.digitalregisteredletter.api.model.LetterResponsesBuilder;

public class TestDataFactory {

	public static LetterRequest createLetterRequest() {
		return LetterRequestBuilder.create()
			.build();
	}

	public static LetterResponse createLetterResponse() {
		return LetterResponseBuilder.create()
			.build();
	}

	public static LetterResponses createLetterResponses() {
		return LetterResponsesBuilder.create()
			.build();
	}

}
