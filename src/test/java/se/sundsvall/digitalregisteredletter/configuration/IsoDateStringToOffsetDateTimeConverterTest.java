package se.sundsvall.digitalregisteredletter.configuration;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsoDateStringToOffsetDateTimeConverterTest {

	private final IsoDateStringToOffsetDateTimeConverter converter = new IsoDateStringToOffsetDateTimeConverter();

	@ParameterizedTest
	@CsvSource({
		"2023-01-15, 2023-01-15T00:00+01:00",
		"2023-07-15, 2023-07-15T00:00+02:00",
		"2023-03-26, 2023-03-26T00:00+01:00",
		"2023-10-29, 2023-10-29T00:00+02:00"
	})
	void convertUsesOffsetInEffectOnDate(String value, OffsetDateTime expected) {
		final var defaultTimeZone = TimeZone.getDefault();
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("Europe/Stockholm"));

			assertThat(converter.convert(value)).isEqualTo(expected);
		} finally {
			TimeZone.setDefault(defaultTimeZone);
		}
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
