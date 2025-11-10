package se.sundsvall.digitalregisteredletter.integration.templating;

import generated.se.sundsvall.templating.RenderResponse;
import org.springframework.stereotype.Component;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

@Component
public class TemplatingIntegration {

	private final TemplatingClient templatingClient;
	private final TemplatingMapper templatingMapper;

	public TemplatingIntegration(final TemplatingClient templatingClient, final TemplatingMapper templatingMapper) {
		this.templatingClient = templatingClient;
		this.templatingMapper = templatingMapper;
	}

	public RenderResponse renderPdf(final String municipalityId, final LetterEntity letterEntity) {

		return templatingClient.render(municipalityId, templatingMapper.toRenderRequest(letterEntity));
	}
}
