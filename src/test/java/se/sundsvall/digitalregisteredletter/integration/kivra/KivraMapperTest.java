package se.sundsvall.digitalregisteredletter.integration.kivra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

import java.sql.Blob;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.digitalregisteredletter.integration.db.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.model.ContentUserV2;
import se.sundsvall.digitalregisteredletter.service.util.BlobUtil;

@ExtendWith(MockitoExtension.class)
class KivraMapperTest {

	@Mock
	private BlobUtil blobUtil;

	@InjectMocks
	private KivraMapper kivraMapper;

	@Test
	void toCheckEligibilityRequestTest() {
		var legalId = "1234567890";
		var legalIds = List.of(legalId);

		var result = kivraMapper.toCheckEligibilityRequest(legalIds);

		assertThat(result).isNotNull().satisfies(userMatchV2SSN -> assertThat(userMatchV2SSN.legalIds()).hasSize(1).containsExactly(legalId));
	}

	@Test
	void toSendContentRequestTest() throws SQLException {

		var subject = "Test Subject";
		var letterId = "letterId";
		var legalId = "1234567890";
		var blob = Mockito.mock(Blob.class);
		when(blob.getBytes(1, (int) blob.length())).thenReturn("test".getBytes());
		var attachment = new AttachmentEntity()
			.withContentType("text/plain")
			.withFileName("test.pdf")
			.withContent(blob);
		var letterEntity = new LetterEntity()
			.withSubject(subject)
			.withId(letterId)
			.withAttachments(List.of(attachment));
		when(kivraMapper.toSendContentRequest(letterEntity, legalId)).thenCallRealMethod();

		var result = kivraMapper.toSendContentRequest(letterEntity, legalId);

		assertThat(result).isNotNull().satisfies(contentUserV2 -> {
			assertThat(contentUserV2.subject()).isEqualTo(subject);
			assertThat(contentUserV2.legalId()).isEqualTo(legalId);
			assertThat(contentUserV2.type()).isEqualTo("registered.letter");
			assertThat(contentUserV2.parts()).isNotNull().allSatisfy(content -> {
				assertThat(content.contentType()).isEqualTo(attachment.getContentType());
				assertThat(content.name()).isEqualTo(attachment.getFileName());
				assertThat(content.data()).isEqualTo("dGVzdA=="); // Base64 encoded "test"
			});
			assertThat(contentUserV2.registered()).isNotNull().satisfies(registeredLetter -> {
				assertThat(registeredLetter.expiresAt()).isCloseTo(OffsetDateTime.now().plusDays(30).format(DateTimeFormatter.ISO_DATE_TIME), within(1, ChronoUnit.SECONDS));
				assertThat(registeredLetter.senderReference()).isEqualTo(new ContentUserV2.RegisteredLetter.SenderReference(letterId));
				assertThat(registeredLetter.hidden()).isNotNull().satisfies(hidden -> {
					assertThat(hidden.sender()).isFalse();
					assertThat(hidden.subject()).isFalse();
				});
			});
		});

	}

	@Test
	void toRegisteredLetterTest() {
		var reference = "letterId";

		var result = kivraMapper.toRegisteredLetter(reference);

		assertThat(result).isNotNull().satisfies(registeredLetter -> {
			assertThat(registeredLetter.expiresAt()).isCloseTo(OffsetDateTime.now().plusDays(30), within(1, ChronoUnit.SECONDS));
			assertThat(registeredLetter.senderReference()).isEqualTo(new ContentUserV2.RegisteredLetter.SenderReference(reference));
			assertThat(registeredLetter.hidden()).isNotNull().satisfies(hidden -> {
				assertThat(hidden.sender()).isFalse();
				assertThat(hidden.subject()).isFalse();
			});
		});
	}

	@Test
	void createRegisteredLetterHiddenTest() {

		var result = kivraMapper.createRegisteredLetterHidden();

		assertThat(result).isNotNull().satisfies(hidden -> {
			assertThat(hidden.sender()).isFalse();
			assertThat(hidden.subject()).isFalse();
		});
	}

	@Test
	void toPartsResponsivesTest() throws SQLException {
		var blob = Mockito.mock(Blob.class);
		when(blob.getBytes(1, (int) blob.length())).thenReturn("test".getBytes());
		var attachment1 = new AttachmentEntity()
			.withContentType("text/plain")
			.withFileName("test1.txt")
			.withContent(blob);
		var attachment2 = new AttachmentEntity()
			.withContentType("text/plain")
			.withFileName("test2.txt")
			.withContent(blob);

		when(kivraMapper.toPartsResponsives(List.of(attachment1, attachment2))).thenCallRealMethod();

		var result = kivraMapper.toPartsResponsives(List.of(attachment1, attachment2));

		assertThat(result).isNotNull().hasSize(2).satisfies(parts -> {
			assertThat(parts.getFirst().contentType()).isEqualTo("text/plain");
			assertThat(parts.getFirst().name()).isEqualTo("test1.txt");
			assertThat(parts.getFirst().data()).isEqualTo("dGVzdA=="); // Base64 encoded "test"
			assertThat(parts.getLast().contentType()).isEqualTo("text/plain");
			assertThat(parts.getLast().name()).isEqualTo("test2.txt");
			assertThat(parts.getLast().data()).isEqualTo("dGVzdA=="); // Base64 encoded "test"
		});

	}

	@Test
	void toPartsResponsiveTest() throws SQLException {
		var blob = Mockito.mock(Blob.class);
		when(blob.getBytes(1, (int) blob.length())).thenReturn("test".getBytes());
		var attachment = new AttachmentEntity()
			.withContentType("text/plain")
			.withFileName("test.pdf")
			.withContent(blob);

		when(kivraMapper.toPartsResponsive(attachment)).thenCallRealMethod();

		var result = kivraMapper.toPartsResponsive(attachment);

		assertThat(result).isNotNull().satisfies(part -> {
			assertThat(part.contentType()).isEqualTo("text/plain");
			assertThat(part.name()).isEqualTo("test.pdf");
			assertThat(part.data()).isEqualTo("dGVzdA=="); // Base64 encoded "test"
		});

	}
}
