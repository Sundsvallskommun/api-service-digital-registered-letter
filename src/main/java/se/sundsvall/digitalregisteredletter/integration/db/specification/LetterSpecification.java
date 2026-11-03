package se.sundsvall.digitalregisteredletter.integration.db.specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity_;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity_;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity_;

import static java.util.Optional.ofNullable;

public class LetterSpecification {

	private static final SpecificationBuilder<LetterEntity> BUILDER = new SpecificationBuilder<>();
	private static final ZoneId ZONE = ZoneId.of("Europe/Stockholm");

	private LetterSpecification() {
		// To prevent instantiation
	}

	/**
	 * Creates filter for matching municipality id if provided, else match all
	 *
	 * @param  value the value to compare the municipality id to
	 * @return       a specification that compares the municipality id to the provided value (or match all if value is not
	 *               provided)
	 */
	public static Specification<LetterEntity> withMunicipalityId(final String value) {
		return BUILDER.buildEqualFilter(LetterEntity_.MUNICIPALITY_ID, value);
	}

	/**
	 * Creates filter for matching soft delete setting
	 *
	 * @param  value the value to compare against the soft delete flag
	 * @return       a specification that compares the soft delete flag against the provided value
	 */
	public static Specification<LetterEntity> withDeleted(final Boolean value) {
		return BUILDER.buildEqualFilter(LetterEntity_.DELETED, value);
	}

	/**
	 * Creates filter for matching org id of connected department if provided, else match all
	 *
	 * @param  value the value to compare against the connected departments org id
	 * @return       a specification that compares the connected departments org id to the provided value (or match all if
	 *               value is not provided)
	 */
	public static Specification<LetterEntity> withDepartmentOrgId(final Long value) {
		return BUILDER.buildEqualFilter(LetterEntity_.ORGANIZATION, OrganizationEntity_.NUMBER, value);
	}

	/**
	 * Creates filter for matching username of connected user if provided, else match all
	 *
	 * @param  value the value to compare against the username of connected user
	 * @return       a specification that compares the username of connected user to the provided value (or match all if
	 *               value is not provided)
	 */
	public static Specification<LetterEntity> withUsername(final String value) {
		return BUILDER.buildEqualsIgnoreCaseFilter(LetterEntity_.USER, UserEntity_.USERNAME, value);
	}

	/**
	 * Creates filter for matching creation date to be equal to or later than if provided, else match all
	 *
	 * @param  value the value to compare the creation date to
	 * @return       a specification that compares the creation date to the provided value (or match all if value is not
	 *               provided)
	 */
	public static Specification<LetterEntity> withCreatedEqualOrAfter(final LocalDate value) {
		return BUILDER.buildDateIsEqualOrAfterFilter(LetterEntity_.CREATED, startOfDay(value));
	}

	/**
	 * Creates filter for matching creation date to be equal to or earlier than if provided, else match all
	 *
	 * @param  value the value to compare the creation date to
	 * @return       a specification that compares the creation date to the provided value (or match all if value is not
	 *               provided)
	 */
	public static Specification<LetterEntity> withCreatedEqualOrBefore(final LocalDate value) {
		return BUILDER.buildDateIsBeforeFilter(LetterEntity_.CREATED, startOfDay(ofNullable(value)
			.map(date -> date.plusDays(1))
			.orElse(null)));
	}

	private static OffsetDateTime startOfDay(final LocalDate date) {
		return ofNullable(date)
			.map(day -> day.atStartOfDay(ZONE).toOffsetDateTime())
			.orElse(null);
	}
}
