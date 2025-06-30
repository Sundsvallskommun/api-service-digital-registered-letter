package se.sundsvall.digitalregisteredletter.integration.kivra;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.digitalregisteredletter.integration.kivra.configuration.KivraConfiguration.CLIENT_ID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.digitalregisteredletter.integration.kivra.configuration.KivraConfiguration;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUser;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.kivra.url}",
	configuration = KivraConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface KivraClient {

	/**
	 * This resource is used to send content to Kivra. The request body contains the content to be sent, and the response
	 * will contain the content that was sent.
	 *
	 * @param  content the content to be sent to Kivra, encapsulated in a ContentUserV2 object
	 * @return         a ResponseEntity containing a ContentUser object, which includes the subject, type and generated_at
	 *                 of the content sent by Kivra to the end user.
	 */
	@PostMapping(value = "/content", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<ContentUser> sendContent(@RequestBody final ContentUserV2 content);

	/**
	 * This resource is used to match a list of users to check that they are eligible for receiving Content from the
	 * specific Tenant. The request contains a list of recipient SSNs to be matched, and the response is a filtered list
	 * containing only the SSNs
	 * that are eligible to receive content from the tenant. If none of the provided SSNs are eligible to receive content
	 * from this tenant, an empty list will be returned.
	 *
	 * @param  userMatchV2SSN the request body containing a list of SSNs to be matched
	 * @return                a ResponseEntity containing a UserMatchV2SSN object with a list of matched SSNs
	 */
	@PostMapping(value = "/usermatch/ssn", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ResponseEntity<UserMatchV2SSN> checkEligibility(@RequestBody final UserMatchV2SSN userMatchV2SSN);

}
