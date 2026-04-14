package se.sundsvall.digitalregisteredletter.service.util;

import jakarta.persistence.EntityManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlobUtilTest {

	@Mock
	private EntityManager entityManagerMock;

	@InjectMocks
	private BlobUtil blobUtil;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(entityManagerMock);
	}

	@Test
	void getSessionTest() {

		final var session = Mockito.mock(Session.class);
		when(entityManagerMock.unwrap(Session.class)).thenReturn(session);

		final var result = blobUtil.getSession();

		assertThat(result).isEqualTo(session);
	}

	@Test
	void createBlob_OK() {
		final var inputStream = new ByteArrayInputStream(new byte[123]);

		final var result = blobUtil.createBlob(inputStream);

		assertThat(result).isNotNull();
	}

	@Test
	void createBlob_IOException() throws IOException {
		final var spy = Mockito.spy(blobUtil);
		final var inputStream = Mockito.mock(InputStream.class);

		when(inputStream.readAllBytes()).thenThrow(new IOException("Test exception"));

		assertThatThrownBy(() -> spy.createBlob(inputStream))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Could not convert input stream to database object");
	}

	@Test
	void convertToBlobTest() {
		final var inputStream = new ByteArrayInputStream(new byte[123]);

		final var result = blobUtil.convertToBlob(inputStream);

		assertThat(result).isNotNull();
	}

	@Test
	void convertBlobToBase64StringTest() throws SQLException {
		final var blob = Mockito.mock(Blob.class);

		when(blob.getBytes(1, (int) blob.length())).thenReturn("test".getBytes());

		final var result = blobUtil.convertBlobToBase64String(blob);

		assertThat(result).isEqualTo("dGVzdA==");
	}

	@Test
	void convertToBlob_SQLException() throws SQLException {
		final var blob = Mockito.mock(Blob.class);
		when(blob.length()).thenThrow(new SQLException("Test exception"));

		assertThatThrownBy(() -> blobUtil.convertBlobToBase64String(blob))
			.isInstanceOf(Problem.class)
			.hasMessage("Internal Server Error: Could not convert Blob to Base64 string");

	}

}
