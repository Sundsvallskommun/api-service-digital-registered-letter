package se.sundsvall;

import java.util.UUID;

public class TestUtil {
	private TestUtil() {}

	public static boolean isValidUUID(final String value) {
		try {
			UUID.fromString(String.valueOf(value));
		} catch (final Exception e) {
			return false;
		}

		return true;
	}
}
