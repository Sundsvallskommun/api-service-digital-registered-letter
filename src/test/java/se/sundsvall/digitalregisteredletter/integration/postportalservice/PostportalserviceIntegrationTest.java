package se.sundsvall.digitalregisteredletter.integration.postportalservice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostportalserviceIntegrationTest {

	@Mock
	private PostportalserviceClient postportalserviceClientMock;

	@InjectMocks
	private PostportalserviceIntegration postportalserviceIntegration;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(postportalserviceClientMock);
	}

	@Test
	void getAttachment() throws IOException {
		var municipalityId = "municipalityId";
		var attachmentId = "attachmentId";
		var inputStream = new ByteArrayInputStream("content".getBytes());

		var resource = mock(Resource.class);
		when(resource.getInputStream()).thenReturn(inputStream);

		var headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.setContentDisposition(ContentDisposition.attachment().filename("document.pdf").build());

		var response = ResponseEntity.ok().headers(headers).body(resource);
		when(postportalserviceClientMock.downloadAttachment(municipalityId, attachmentId)).thenReturn(response);

		var result = postportalserviceIntegration.getAttachment(municipalityId, attachmentId);

		assertThat(result).isNotNull();
		assertThat(result.filename()).isEqualTo("document.pdf");
		assertThat(result.contentType()).isEqualTo("application/pdf");
		assertThat(result.inputStream()).isNotNull();

		verify(postportalserviceClientMock).downloadAttachment(municipalityId, attachmentId);
	}
}
