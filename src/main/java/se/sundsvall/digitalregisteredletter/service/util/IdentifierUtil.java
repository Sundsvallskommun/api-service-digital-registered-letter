package se.sundsvall.digitalregisteredletter.service.util;

import static org.zalando.problem.Status.BAD_REQUEST;
import static se.sundsvall.dept44.support.Identifier.Type.AD_ACCOUNT;

import java.util.Optional;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.support.Identifier;

public class IdentifierUtil {
	private IdentifierUtil() {}

	static final String ERROR_AD_ACCOUNT_IDENTIFIER_NOT_FOUND = "Identifier for ad account must be present as a header value.";

	public static String getAdUser() {
		return Optional.ofNullable(Identifier.get())
			.filter(identifier -> AD_ACCOUNT.equals(identifier.getType()))
			.map(Identifier::getValue)
			.orElseThrow(() -> Problem.valueOf(BAD_REQUEST, ERROR_AD_ACCOUNT_IDENTIFIER_NOT_FOUND));
	}
}
