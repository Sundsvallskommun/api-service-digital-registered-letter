package se.sundsvall.digitalregisteredletter.configuration;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static java.time.ZoneId.systemDefault;
import static java.util.Optional.ofNullable;

/**
 * Component converts string with ISO local date format (YYYY-MM-DD, for example 2025-09-18) to a
 * OffsetDateTime object representing the local date at start of day (using the system offset in effect on that date)
 */
@Component
public class IsoDateStringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

	@Override
	public OffsetDateTime convert(String source) {
		return ofNullable(source)
			.map(LocalDate::parse)
			.map(localDate -> localDate.atStartOfDay(systemDefault()))
			.map(ZonedDateTime::toOffsetDateTime)
			.orElse(null);
	}
}
