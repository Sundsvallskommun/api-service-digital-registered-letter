package se.sundsvall.digitalregisteredletter.service.util;

import static org.zalando.problem.Status.BAD_REQUEST;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;

public final class ParseUtil {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private ParseUtil() {}

	public static LetterRequest parseLetterRequest(final String letterString) {
		try {
			return OBJECT_MAPPER.readValue(letterString, LetterRequest.class);
		} catch (JsonProcessingException e) {
			throw Problem.valueOf(BAD_REQUEST, "Couldn't parse letter request");
		}
	}
}
