package se.sundsvall.digitalregisteredletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createLetterRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentsBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.dao.LetterRepository;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;

@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

	@Mock
	private LetterRepository letterRepository;

	@Mock
	private AttachmentMapper attachmentMapper;

	@Captor
	private ArgumentCaptor<LetterEntity> letterEntityArgumentCaptor;

	@InjectMocks
	private LetterService letterService;

	@BeforeEach
	void setUp() {
		var objectMapper = new ObjectMapper();
		letterService = new LetterService(letterRepository, objectMapper, attachmentMapper);
	}

	@Test
	void sendLetterTest() {
		var multipartFile = mock(MultipartFile.class);
		var municipalityId = "2281";
		var letterRequest = createLetterRequest();
		var attachments = AttachmentsBuilder.create()
			.withFiles(List.of(multipartFile))
			.build();
		var attachmentEntity = AttachmentEntity.create()
			.withContentType("application/pdf")
			.withFileName("attachment.pdf");
		when(attachmentMapper.toAttachmentEntities(attachments)).thenReturn(List.of(attachmentEntity));

		var response = letterService.sendLetter(municipalityId, letterRequest, attachments);

		assertThat(response).isNull();

		verify(letterRepository).save(letterEntityArgumentCaptor.capture());
		var capturedLetterEntity = letterEntityArgumentCaptor.getValue();
		assertThat(capturedLetterEntity).isNotNull();
		assertThat(capturedLetterEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(capturedLetterEntity.getAttachments()).containsExactly(attachmentEntity);
		// TODO: This tes

	}

	@Test
	void getLetterTest() {
		// TODO: Will be implemented in a future task
		letterService.getLetters(null, null);
	}

	@Test
	void getLettersTest() {
		// TODO: Will be implemented in a future task
		letterService.getLetter(null, null);
	}

	@Test
	void parseLetterRequest_OK() {
		var letterString = """
			{
			  "partyId": "123e4567-e89b-12d3-a456-426614174000",
			  "subject": "Important Notification",
			  "supportInfo": {
			    "supportText": "For support, please contact us at the information below.",
			    "contactInformationUrl": "https://example.com/support",
			    "contactInformationPhoneNumber": "+46123456789",
			    "contactInformationEmail": "support@email.com"
			  },
			  "contentType": "text/plain",
			  "body": "This is the content of the letter. Plain-text body"
			}
			""";

		var object = letterService.parseLetterRequest(letterString);

		assertThat(object).isNotNull();
		assertThat(object.partyId()).isEqualTo("123e4567-e89b-12d3-a456-426614174000");
		assertThat(object.subject()).isEqualTo("Important Notification");
		assertThat(object.supportInfo()).isNotNull();
		assertThat(object.supportInfo().supportText()).isEqualTo("For support, please contact us at the information below.");
		assertThat(object.supportInfo().contactInformationUrl()).isEqualTo("https://example.com/support");
		assertThat(object.supportInfo().contactInformationPhoneNumber()).isEqualTo("+46123456789");
		assertThat(object.supportInfo().contactInformationEmail()).isEqualTo("support@email.com");
		assertThat(object.contentType()).isEqualTo("text/plain");
		assertThat(object.body()).isEqualTo("This is the content of the letter. Plain-text body");
	}

}
