package se.sundsvall.digitalregisteredletter.integration.postportalservice;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.digitalregisteredletter.integration.postportalservice.configuration.PostportalserviceConfiguration;

import static org.springframework.http.MediaType.ALL_VALUE;
import static se.sundsvall.digitalregisteredletter.integration.postportalservice.configuration.PostportalserviceConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID,
	url = "${integration.postportalservice.url}",
	configuration = PostportalserviceConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface PostportalserviceClient {

	@GetMapping(value = "/{municipalityId}/attachments/{attachmentId}", produces = ALL_VALUE)
	ResponseEntity<Resource> downloadAttachment(@PathVariable final String municipalityId, @PathVariable final String attachmentId);

}
