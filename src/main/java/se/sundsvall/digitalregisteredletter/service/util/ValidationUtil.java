package se.sundsvall.digitalregisteredletter.service.util;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;

public final class ValidationUtil {

	private ValidationUtil() {
		// Utility class, prevent instantiation
	}

	public static <T> void validate(final T t) {
		final var validator = buildDefaultValidatorFactory().getValidator();
		final Set<ConstraintViolation<T>> violations = validator.validate(t);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}
	}
}
