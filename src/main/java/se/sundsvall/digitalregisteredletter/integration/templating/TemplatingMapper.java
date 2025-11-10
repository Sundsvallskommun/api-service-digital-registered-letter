package se.sundsvall.digitalregisteredletter.integration.templating;

import static org.zalando.problem.Status.NOT_FOUND;

import generated.se.sundsvall.templating.RenderRequest;
import java.util.HashMap;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

@Component
public class TemplatingMapper {

	@Value("${integration.templating.receipt-template-identifier}")
	private String receiptIdentifier;

	public RenderRequest toRenderRequest(final LetterEntity letterEntity) {
		final var signingInformation = Optional.ofNullable(letterEntity.getSigningInformation())
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No signing information found for letter with id '%s'".formatted(letterEntity.getId())));

		final var parameters = new HashMap<String, Object>();
		parameters.put("personalNumber", signingInformation.getPersonalNumber());
		parameters.put("name", signingInformation.getName());
		parameters.put("signed", signingInformation.getSigned());
		return new RenderRequest()
			.identifier(receiptIdentifier)
			.parameters(parameters);
	}
}
