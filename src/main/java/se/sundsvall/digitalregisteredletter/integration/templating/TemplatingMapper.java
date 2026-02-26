package se.sundsvall.digitalregisteredletter.integration.templating;

import generated.se.sundsvall.templating.RenderRequest;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Component
public class TemplatingMapper {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Value("${integration.templating.receipt-template-identifier}")
	private String receiptIdentifier;

	public RenderRequest toRenderRequest(final LetterEntity letterEntity) {
		final var signingInformation = Optional.ofNullable(letterEntity.getSigningInformation())
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No signing information found for letter with id '%s'".formatted(letterEntity.getId())));

		final var parameters = new HashMap<String, Object>();
		parameters.put("subject", letterEntity.getSubject());
		parameters.put("personalNumber", signingInformation.getPersonalNumber());
		parameters.put("name", signingInformation.getName());
		parameters.put("signed", Optional.ofNullable(signingInformation.getSigned())
			.map(DATE_FORMATTER::format)
			.orElse(null));
		return new RenderRequest()
			.identifier(receiptIdentifier)
			.parameters(parameters);
	}
}
