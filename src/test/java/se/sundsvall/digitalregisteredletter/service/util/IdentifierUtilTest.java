package se.sundsvall.digitalregisteredletter.service.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.dept44.support.Identifier.Type;

@ExtendWith(MockitoExtension.class)
class IdentifierUtilTest {

	@Mock
	private Identifier identifierMock;

	@AfterEach
	void teardown() {
		verifyNoMoreInteractions(identifierMock);
	}

	@Test
	void testExistingIdentifier() {
		final var username = "username";
		try (final MockedStatic<Identifier> staticMock = mockStatic(Identifier.class)) {
			staticMock.when(Identifier::get).thenReturn(identifierMock);

			when(identifierMock.getType()).thenReturn(Type.AD_ACCOUNT);
			when(identifierMock.getValue()).thenReturn(username);

			assertThat(IdentifierUtil.getAdUser()).isEqualTo(username);
			verify(identifierMock).getValue();
			verify(identifierMock).getType();
		}
	}

	@ParameterizedTest
	@EnumSource(value = Type.class, mode = EXCLUDE, names = {
		"AD_ACCOUNT"
	})
	void testNotCorrectIdentifierType() {
		try (final MockedStatic<Identifier> staticMock = mockStatic(Identifier.class)) {
			staticMock.when(Identifier::get).thenReturn(identifierMock);

			when(identifierMock.getType()).thenReturn(Type.PARTY_ID);

			final var e = assertThrows(ThrowableProblem.class, IdentifierUtil::getAdUser);

			assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
			assertThat(e.getMessage()).isEqualTo("Bad Request: Identifier for ad account must be present as a header value.");
			verify(identifierMock).getType();
		}
	}

	@Test
	void testMissingIdentifier() {
		final var e = assertThrows(ThrowableProblem.class, IdentifierUtil::getAdUser);

		assertThat(e.getStatus()).isEqualTo(Status.BAD_REQUEST);
		assertThat(e.getMessage()).isEqualTo("Bad Request: Identifier for ad account must be present as a header value.");
	}
}
