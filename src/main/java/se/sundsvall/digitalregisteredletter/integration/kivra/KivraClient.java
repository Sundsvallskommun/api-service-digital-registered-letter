package se.sundsvall.digitalregisteredletter.integration.kivra;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.digitalregisteredletter.integration.kivra.configuration.KivraConfiguration.CLIENT_ID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.digitalregisteredletter.integration.kivra.configuration.KivraConfiguration;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUser;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.KeyValue;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.RegisteredLetterResponse;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.UserMatchV2SSN;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.kivra.base-url}",
	dismiss404 = true,
	configuration = KivraConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface KivraClient {

	/**
	 * This resource is used to send content to Kivra. The request body contains the content to be sent, and the response
	 * will contain the content that was sent.
	 *
	 * @param  tenantKey the tenant key identifying the Kivra tenant
	 * @param  content   the content to be sent to Kivra, encapsulated in a ContentUserV2 object
	 * @return           a ContentUser object, which includes the subject, type, and generated_at of the content sent by
	 *                   Kivra to the end user.
	 */
	@PostMapping(value = "/v2/tenant/{tenantKey}/content", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	ContentUser sendContent(@PathVariable final String tenantKey, @RequestBody final ContentUserV2 content);

	/**
	 * This resource is used to match a list of users to check that they are eligible for receiving Content from the
	 * specific Tenant. The request contains a list of recipient SSNs to be matched, and the response is a filtered list
	 * containing only the SSNs
	 * that are eligible to receive content from the tenant. If none of the provided SSNs are eligible to receive content
	 * from this tenant, an empty list will be returned.
	 *
	 * @param  tenantKey      the tenant key identifying the Kivra tenant
	 * @param  userMatchV2SSN the request body containing a list of SSNs to be matched
	 * @return                a UserMatchV2SSN object with a list of matched SSNs
	 */
	@PostMapping(value = "/v2/tenant/{tenantKey}/usermatch/ssn", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	UserMatchV2SSN checkEligibility(@PathVariable final String tenantKey, @RequestBody final UserMatchV2SSN userMatchV2SSN);

	/**
	 * This resource is used to retrieve the responses of all registered letters sent by the tenant that have been signed or
	 * expired. The response key can then be used to fetch the response details of each registered letter. The resource
	 * returns a list of
	 * key-value pairs where the key is the 'responseKey' and the value is the status (either 'signed' or 'expired').
	 *
	 * @param  tenantKey the tenant key identifying the Kivra tenant
	 * @return           a list of KeyValue objects containing the response keys and their corresponding statuses
	 */
	@GetMapping(value = "/v2/tenant/{tenantKey}/registered", produces = APPLICATION_JSON_VALUE)
	List<KeyValue> getAllResponses(@PathVariable final String tenantKey);

	/**
	 * This resource is used to retrieve details of a specific registered letter response using the response key.
	 *
	 * @param  tenantKey   the tenant key identifying the Kivra tenant
	 * @param  responseKey the key of the response to be fetched
	 * @return             a RegisteredLetterResponse object containing the details of the registered letter response
	 */
	@GetMapping(value = "/v2/tenant/{tenantKey}/registered/{responseKey}", produces = APPLICATION_JSON_VALUE)
	RegisteredLetterResponse getResponseDetails(@PathVariable final String tenantKey, @PathVariable final String responseKey);

	/**
	 * This resource is used to delete a specific registered letter response after we have updated the status of the letter
	 * in our system.
	 *
	 * @param tenantKey   the tenant key identifying the Kivra tenant
	 * @param responseKey the key of the response to be deleted
	 */
	@DeleteMapping(value = "/v2/tenant/{tenantKey}/registered/{responseKey}", produces = ALL_VALUE)
	void deleteResponse(@PathVariable final String tenantKey, @PathVariable final String responseKey);

	/**
	 * Method is used to verify that the certificate to Kivra is valid
	 */
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	void getTenantInformation();

}
