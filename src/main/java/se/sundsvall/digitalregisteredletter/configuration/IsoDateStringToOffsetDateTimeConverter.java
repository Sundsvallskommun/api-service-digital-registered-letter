package se.sundsvall.digitalregisteredletter.configuration;

import static java.time.OffsetTime.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Component converts string with ISO local date format (YYYY-MM-DD, for example 2025-09-18) to a
 * OffsetDateTime object representing the local date at start of day (using system offset)
 */
@Component
public class IsoDateStringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {

	private static final ZoneOffset SYSTEM_ZONE_OFFSET = now(systemDefault()).getOffset();

	@Override
	public OffsetDateTime convert(String source) {
		return ofNullable(source)
			.map(LocalDate::parse)
			.map(LocalDate::atStartOfDay)
			.map(localDateTime -> OffsetDateTime.of(localDateTime, SYSTEM_ZONE_OFFSET))
			.orElse(null);
	}
}
