package se.sundsvall.digitalregisteredletter.integration.templating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;

@ExtendWith(MockitoExtension.class)
class TemplatingMapperTest {

	private static final String RECEIPT_IDENTIFIER = "receipt-template-id";

	@InjectMocks
	private TemplatingMapper templatingMapper;

	private static Stream<Arguments> fieldVariationsProvider() {
		final var validPersonalNumber = "191212121212";
		final var validName = "Test Testsson";
		final var validSigned = OffsetDateTime.now();

		return Stream.of(
			// Null field variations
			Arguments.of(null, validName, validSigned),
			Arguments.of(validPersonalNumber, null, validSigned),
			Arguments.of(validPersonalNumber, validName, null),
			// String variations
			Arguments.of("", "", validSigned),
			Arguments.of("   ", "\t\n", validSigned),
			Arguments.of("19121212-1212", "Test Testsson-Andersson (Jr.)", validSigned));
	}

	private static Stream<Arguments> dateVariationsProvider() {
		final var personalNumber = "191212121212";
		final var name = "Test Testsson";

		return Stream.of(
			Arguments.of(personalNumber, name, OffsetDateTime.now().minusYears(5)),
			Arguments.of(personalNumber, name, OffsetDateTime.now().plusYears(5)));
	}

	@Test
	void toRenderRequest() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var personalNumber = "191212121212";
		final var name = "Test Testsson";
		final var signed = OffsetDateTime.parse("2025-11-12T14:30:00+01:00");
		final var subject = "Test Subject";

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber(personalNumber)
			.withName(name)
			.withSigned(signed);

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject(subject)
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getIdentifier()).isEqualTo(RECEIPT_IDENTIFIER);
		assertThat(result.getParameters()).isNotNull().hasSize(4);
		assertThat(result.getParameters()).containsEntry("subject", subject);
		assertThat(result.getParameters()).containsEntry("personalNumber", personalNumber);
		assertThat(result.getParameters()).containsEntry("name", name);
		assertThat(result.getParameters()).containsEntry("signed", "2025-11-12 14:30");
	}

	@Test
	void toRenderRequestWithAllSigningInformationFields() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var personalNumber = "191212121212";
		final var name = "Test Testsson";
		final var signed = OffsetDateTime.parse("2025-11-12T14:30:00+01:00");
		final var givenName = "Test";
		final var surname = "Testsson";
		final var ipAddress = "192.168.1.1";
		final var subject = "Test Subject";

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber(personalNumber)
			.withName(name)
			.withSigned(signed)
			.withGivenName(givenName)
			.withSurname(surname)
			.withIpAddress(ipAddress)
			.withStatus("COMPLETED")
			.withOrderRef("order-ref-123")
			.withContentKey("content-key-456");

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject(subject)
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getIdentifier()).isEqualTo(RECEIPT_IDENTIFIER);
		assertThat(result.getParameters()).isNotNull().hasSize(4);
		assertThat(result.getParameters()).containsEntry("subject", subject);
		assertThat(result.getParameters()).containsEntry("personalNumber", personalNumber);
		assertThat(result.getParameters()).containsEntry("name", name);
		assertThat(result.getParameters()).containsEntry("signed", "2025-11-12 14:30");
	}

	@ParameterizedTest
	@MethodSource("fieldVariationsProvider")
	void toRenderRequestWithFieldVariations(final String personalNumber, final String name, final OffsetDateTime signed) {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var subject = "Test Subject";

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber(personalNumber)
			.withName(name)
			.withSigned(signed);

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject(subject)
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getIdentifier()).isEqualTo(RECEIPT_IDENTIFIER);
		assertThat(result.getParameters()).isNotNull().hasSize(4);
		assertThat(result.getParameters()).containsEntry("subject", subject);
		assertThat(result.getParameters()).containsEntry("personalNumber", personalNumber);
		assertThat(result.getParameters()).containsEntry("name", name);
		if (signed != null) {
			assertThat(result.getParameters()).containsKey("signed");
			assertThat(result.getParameters().get("signed")).isInstanceOf(String.class);
		} else {
			assertThat(result.getParameters()).containsEntry("signed", null);
		}
	}

	@Test
	void toRenderRequestWithAllNullSigningInformationFields() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber(null)
			.withName(null)
			.withSigned(null);

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject(null)
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getIdentifier()).isEqualTo(RECEIPT_IDENTIFIER);
		assertThat(result.getParameters()).isNotNull().hasSize(4);
		assertThat(result.getParameters()).containsEntry("subject", null);
		assertThat(result.getParameters()).containsEntry("personalNumber", null);
		assertThat(result.getParameters()).containsEntry("name", null);
		assertThat(result.getParameters()).containsEntry("signed", null);
	}

	@Test
	void toRenderRequestWithVeryLongStrings() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var personalNumber = "A".repeat(1000);
		final var name = "B".repeat(2000);
		final var signed = OffsetDateTime.parse("2025-11-12T14:30:00+01:00");
		final var subject = "C".repeat(500);

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber(personalNumber)
			.withName(name)
			.withSigned(signed);

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject(subject)
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getIdentifier()).isEqualTo(RECEIPT_IDENTIFIER);
		assertThat(result.getParameters()).isNotNull().hasSize(4);
		assertThat(result.getParameters()).containsEntry("subject", subject);
		assertThat(result.getParameters()).containsEntry("personalNumber", personalNumber);
		assertThat(result.getParameters()).containsEntry("name", name);
		assertThat(result.getParameters()).containsEntry("signed", "2025-11-12 14:30");
	}

	@Test
	void toRenderRequestWithUnicodeCharacters() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var personalNumber = "191212121212";
		final var name = "Tëst Tëstssön 测试 🎉";
		final var signed = OffsetDateTime.parse("2025-11-12T14:30:00+01:00");
		final var subject = "Ämne med ÅÄÖ";

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber(personalNumber)
			.withName(name)
			.withSigned(signed);

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject(subject)
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getIdentifier()).isEqualTo(RECEIPT_IDENTIFIER);
		assertThat(result.getParameters()).isNotNull().hasSize(4);
		assertThat(result.getParameters()).containsEntry("subject", subject);
		assertThat(result.getParameters()).containsEntry("personalNumber", personalNumber);
		assertThat(result.getParameters()).containsEntry("name", name);
		assertThat(result.getParameters()).containsEntry("signed", "2025-11-12 14:30");
	}

	@Test
	void toRenderRequestWhenSigningInformationIsNull() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var letterId = "letter-id-123";
		final var letterEntity = new LetterEntity()
			.withId(letterId)
			.withSigningInformation(null);

		// Act & Assert
		assertThatThrownBy(() -> templatingMapper.toRenderRequest(letterEntity))
			.isInstanceOf(Problem.class)
			.satisfies(thrown -> {
				final var problem = (Problem) thrown;
				assertThat(problem.getStatus()).isEqualTo(Status.NOT_FOUND);
				assertThat(problem.getDetail()).isEqualTo("No signing information found for letter with id '%s'".formatted(letterId));
			});
	}

	@Test
	void toRenderRequestWhenLetterEntityIsNullThrowsNullPointerException() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		// Act & Assert
		assertThatThrownBy(() -> templatingMapper.toRenderRequest(null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void toRenderRequestWithDifferentReceiptIdentifiers() {
		// Arrange
		final var customIdentifier = "custom-receipt-template-identifier-xyz-123";
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", customIdentifier);

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber("191212121212")
			.withName("Test Testsson")
			.withSigned(OffsetDateTime.parse("2025-11-12T14:30:00+01:00"));

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject("Test Subject")
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getIdentifier()).isEqualTo(customIdentifier);
	}

	@Test
	void toRenderRequestVerifyParametersAreMutable() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber("191212121212")
			.withName("Test Testsson")
			.withSigned(OffsetDateTime.parse("2025-11-12T14:30:00+01:00"));

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject("Test Subject")
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert - verify we can modify the parameter map
		assertThat(result).isNotNull();
		final var parameters = result.getParameters();
		assertThat(parameters).isNotNull();

		// Should be able to add new entries
		parameters.put("newKey", "newValue");
		assertThat(parameters).containsKey("newKey");

		// Should be able to modify existing entries
		parameters.put("name", "Modified Name");
		assertThat(parameters).containsEntry("name", "Modified Name");
	}

	@ParameterizedTest
	@MethodSource("dateVariationsProvider")
	void toRenderRequestWithDateVariations(final String personalNumber, final String name, final OffsetDateTime signed) {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber(personalNumber)
			.withName(name)
			.withSigned(signed);

		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSubject("Test Subject")
			.withSigningInformation(signingInformation);

		// Act
		final var result = templatingMapper.toRenderRequest(letterEntity);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getParameters()).containsKey("signed");
		assertThat(result.getParameters().get("signed")).isInstanceOf(String.class);
		assertThat(result.getParameters().get("signed").toString()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}");
	}

	@Test
	void toRenderRequestMultipleCallsProduceIndependentResults() {
		// Arrange
		ReflectionTestUtils.setField(templatingMapper, "receiptIdentifier", RECEIPT_IDENTIFIER);

		final var signingInformation1 = SigningInformationEntity.create()
			.withPersonalNumber("191212121212")
			.withName("First Person")
			.withSigned(OffsetDateTime.parse("2025-11-12T14:30:00+01:00"));

		final var letterEntity1 = new LetterEntity()
			.withId("letter-id-1")
			.withSubject("First Subject")
			.withSigningInformation(signingInformation1);

		final var signingInformation2 = SigningInformationEntity.create()
			.withPersonalNumber("199901011234")
			.withName("Second Person")
			.withSigned(OffsetDateTime.parse("2025-11-13T15:45:00+01:00"));

		final var letterEntity2 = new LetterEntity()
			.withId("letter-id-2")
			.withSubject("Second Subject")
			.withSigningInformation(signingInformation2);

		// Act
		final var result1 = templatingMapper.toRenderRequest(letterEntity1);
		final var result2 = templatingMapper.toRenderRequest(letterEntity2);

		// Assert - results are independent
		assertThat(result1.getParameters()).isNotEqualTo(result2.getParameters());
		assertThat(result1.getParameters()).containsEntry("name", "First Person");
		assertThat(result2.getParameters()).containsEntry("name", "Second Person");

		// Modifying one should not affect the other
		final var parameters1 = result1.getParameters();
		assertThat(parameters1).isNotNull();
		parameters1.put("test", "value");
		assertThat(result2.getParameters()).doesNotContainKey("test");
	}
}
