package se.sundsvall.digitalregisteredletter.integration.messaging;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.digitalregisteredletter.integration.messaging.configuration.MessagingConfiguration.CLIENT_ID;

import generated.se.sundsvall.messaging.EmailRequest;
import generated.se.sundsvall.messaging.MessageResult;
import generated.se.sundsvall.messaging.SlackRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.digitalregisteredletter.integration.messaging.configuration.MessagingConfiguration;

@FeignClient(name = CLIENT_ID,
	url = "${integration.messaging.url}",
	configuration = MessagingConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface MessagingClient {

	/**
	 * Sends a single e-mail
	 *
	 * @param  request containing email information
	 * @return         response containing id and delivery results for the message that was sent
	 */
	@PostMapping(path = "/{municipalityId}/email", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendEmail(@PathVariable("municipalityId") final String municipalityId, @RequestBody final EmailRequest request);

	/**
	 * Sends a Slack message
	 *
	 * @param  slackRequest containing message information
	 * @return              response containing id and delivery results for the message that was sent
	 */
	@PostMapping(path = "/{municipalityId}/slack", produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
	MessageResult sendSlackMessage(@PathVariable("municipalityId") final String municipalityId, @RequestBody final SlackRequest slackRequest);
}
