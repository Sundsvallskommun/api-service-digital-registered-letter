package se.sundsvall.digitalregisteredletter.configuration;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.digitalregisteredletter.Application;

import static java.time.OffsetTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class IsoDateStringToOffsetDateTimeConverterTest {

	@Autowired
	private IsoDateStringToOffsetDateTimeConverter converter;

	@Test
	void convertCorrectValue() {
		final var localDate = LocalDate.now();

		final var offsetDateTime = converter.convert(localDate.format(ISO_LOCAL_DATE));

		assertThat(offsetDateTime.toInstant()).isEqualTo(localDate.atStartOfDay().toInstant(now(systemDefault()).getOffset()));
	}

	@Test
	void convertNullValue() {
		assertThat(converter.convert(null)).isNull();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"2025-01-011", "20250101", "", " "
	})
	void convertIncorrectValue(String value) {
		assertThrows(DateTimeParseException.class, () -> converter.convert(value));
	}
}
