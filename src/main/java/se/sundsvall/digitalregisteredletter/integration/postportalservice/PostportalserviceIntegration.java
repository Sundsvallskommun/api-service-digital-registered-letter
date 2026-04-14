package se.sundsvall.digitalregisteredletter.integration.postportalservice;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.digitalregisteredletter.service.model.AttachmentData;

@Component
public class PostportalserviceIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(PostportalserviceIntegration.class);
	private final PostportalserviceClient postportalserviceClient;

	public PostportalserviceIntegration(final PostportalserviceClient postportalserviceClient) {
		this.postportalserviceClient = postportalserviceClient;
	}

	public AttachmentData getAttachment(final String municipalityId, final String attachmentId) throws IOException {
		var response = postportalserviceClient.downloadAttachment(municipalityId, attachmentId);

		return AttachmentData.from(response);
	}

}
