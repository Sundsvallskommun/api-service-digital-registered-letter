package se.sundsvall.digitalregisteredletter.integration.templating;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.digitalregisteredletter.integration.templating.configuration.TemplatingConfiguration.CLIENT_ID;

import generated.se.sundsvall.templating.RenderRequest;
import generated.se.sundsvall.templating.RenderResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.digitalregisteredletter.integration.templating.configuration.TemplatingConfiguration;

@FeignClient(
	name = CLIENT_ID,
	configuration = TemplatingConfiguration.class,
	url = "${integration.templating.url}")
@CircuitBreaker(name = CLIENT_ID)
public interface TemplatingClient {

	/**
	 * Renders a template as PDF.
	 *
	 * @param  municipalityId the municipality id to use for template rendering
	 * @param  renderRequest  the {@link RenderRequest} containing template identifiers and data to render
	 * @return                the rendered PDF as a byte array wrapped in a {@link RenderResponse}
	 */
	@PostMapping(path = "/{municipalityId}/render/pdf", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	RenderResponse render(@PathVariable(name = "municipalityId") String municipalityId, @RequestBody RenderRequest renderRequest);
}
