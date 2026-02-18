package se.sundsvall.digitalregisteredletter.integration.templating;

import generated.se.sundsvall.templating.RenderRequest;
import generated.se.sundsvall.templating.RenderResponse;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemplatingIntegrationTest {

	@Mock
	private TemplatingClient templatingClient;

	@Mock
	private TemplatingMapper templatingMapper;

	@InjectMocks
	private TemplatingIntegration templatingIntegration;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(templatingClient, templatingMapper);
	}

	@Test
	void renderPdf() {
		// Arrange
		final var municipalityId = "2281";
		final var letterId = "letter-id-123";
		final var personalNumber = "191212121212";
		final var name = "Test Testsson";
		final var signed = OffsetDateTime.now();

		final var signingInformation = SigningInformationEntity.create()
			.withPersonalNumber(personalNumber)
			.withName(name)
			.withSigned(signed);

		final var letterEntity = new LetterEntity()
			.withId(letterId)
			.withSigningInformation(signingInformation);

		final var renderRequest = new RenderRequest();
		final var renderResponse = new RenderResponse();

		when(templatingMapper.toRenderRequest(letterEntity)).thenReturn(renderRequest);
		when(templatingClient.render(municipalityId, renderRequest)).thenReturn(renderResponse);

		// Act
		final var result = templatingIntegration.renderPdf(municipalityId, letterEntity);

		// Assert
		assertThat(result).isNotNull().isSameAs(renderResponse);
		verify(templatingMapper).toRenderRequest(letterEntity);
		verify(templatingClient).render(municipalityId, renderRequest);
	}

	@Test
	void renderPdfWithNullMunicipalityId() {
		// Arrange
		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSigningInformation(SigningInformationEntity.create()
				.withPersonalNumber("191212121212")
				.withName("Test Testsson")
				.withSigned(OffsetDateTime.now()));

		final var renderRequest = new RenderRequest();
		final var renderResponse = new RenderResponse();

		when(templatingMapper.toRenderRequest(letterEntity)).thenReturn(renderRequest);
		when(templatingClient.render(null, renderRequest)).thenReturn(renderResponse);

		// Act
		final var result = templatingIntegration.renderPdf(null, letterEntity);

		// Assert
		assertThat(result).isNotNull().isSameAs(renderResponse);
		verify(templatingMapper).toRenderRequest(letterEntity);
		verify(templatingClient).render(null, renderRequest);
	}

	@Test
	void renderPdfWhenLetterEntityIsNull() {
		// Arrange
		final var municipalityId = "2281";

		when(templatingMapper.toRenderRequest(null)).thenThrow(new NullPointerException("Letter entity cannot be null"));

		// Act & Assert
		assertThatThrownBy(() -> templatingIntegration.renderPdf(municipalityId, null))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("Letter entity cannot be null");

		verify(templatingMapper).toRenderRequest(null);
	}

	@Test
	void renderPdfWhenMapperReturnsNull() {
		// Arrange
		final var municipalityId = "2281";
		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSigningInformation(SigningInformationEntity.create()
				.withPersonalNumber("191212121212")
				.withName("Test Testsson")
				.withSigned(OffsetDateTime.now()));

		final var renderResponse = new RenderResponse();

		when(templatingMapper.toRenderRequest(letterEntity)).thenReturn(null);
		when(templatingClient.render(municipalityId, null)).thenReturn(renderResponse);

		// Act
		final var result = templatingIntegration.renderPdf(municipalityId, letterEntity);

		// Assert
		assertThat(result).isNotNull().isSameAs(renderResponse);
		verify(templatingMapper).toRenderRequest(letterEntity);
		verify(templatingClient).render(municipalityId, null);
	}

	@Test
	void renderPdfWhenClientReturnsNull() {
		// Arrange
		final var municipalityId = "2281";
		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSigningInformation(SigningInformationEntity.create()
				.withPersonalNumber("191212121212")
				.withName("Test Testsson")
				.withSigned(OffsetDateTime.now()));

		final var renderRequest = new RenderRequest();

		when(templatingMapper.toRenderRequest(letterEntity)).thenReturn(renderRequest);
		when(templatingClient.render(municipalityId, renderRequest)).thenReturn(null);

		// Act
		final var result = templatingIntegration.renderPdf(municipalityId, letterEntity);

		// Assert
		assertThat(result).isNull();
		verify(templatingMapper).toRenderRequest(letterEntity);
		verify(templatingClient).render(municipalityId, renderRequest);
	}

	@Test
	void renderPdfWhenMapperThrowsException() {
		// Arrange
		final var municipalityId = "2281";
		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSigningInformation(null);

		final var expectedException = new IllegalStateException("Signing information is required");
		when(templatingMapper.toRenderRequest(letterEntity)).thenThrow(expectedException);

		// Act & Assert
		assertThatThrownBy(() -> templatingIntegration.renderPdf(municipalityId, letterEntity))
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("Signing information is required")
			.isSameAs(expectedException);

		verify(templatingMapper).toRenderRequest(letterEntity);
	}

	@Test
	void renderPdfWhenClientThrowsException() {
		// Arrange
		final var municipalityId = "2281";
		final var letterEntity = new LetterEntity()
			.withId("letter-id")
			.withSigningInformation(SigningInformationEntity.create()
				.withPersonalNumber("191212121212")
				.withName("Test Testsson")
				.withSigned(OffsetDateTime.now()));

		final var renderRequest = new RenderRequest();
		final var expectedException = new RuntimeException("Templating service unavailable");

		when(templatingMapper.toRenderRequest(letterEntity)).thenReturn(renderRequest);
		when(templatingClient.render(municipalityId, renderRequest)).thenThrow(expectedException);

		// Act & Assert
		assertThatThrownBy(() -> templatingIntegration.renderPdf(municipalityId, letterEntity))
			.isInstanceOf(RuntimeException.class)
			.hasMessage("Templating service unavailable")
			.isSameAs(expectedException);

		verify(templatingMapper).toRenderRequest(letterEntity);
		verify(templatingClient).render(municipalityId, renderRequest);
	}

	@Test
	void renderPdfMultipleCallsWithDifferentData() {
		// Arrange
		final var municipalityId1 = "2281";
		final var municipalityId2 = "1234";

		final var letterEntity1 = new LetterEntity()
			.withId("letter-1")
			.withSigningInformation(SigningInformationEntity.create()
				.withPersonalNumber("191212121212")
				.withName("First Person")
				.withSigned(OffsetDateTime.now()));

		final var letterEntity2 = new LetterEntity()
			.withId("letter-2")
			.withSigningInformation(SigningInformationEntity.create()
				.withPersonalNumber("199901011234")
				.withName("Second Person")
				.withSigned(OffsetDateTime.now()));

		final var renderRequest1 = new RenderRequest();
		final var renderRequest2 = new RenderRequest();
		final var renderResponse1 = new RenderResponse();
		final var renderResponse2 = new RenderResponse();

		when(templatingMapper.toRenderRequest(letterEntity1)).thenReturn(renderRequest1);
		when(templatingMapper.toRenderRequest(letterEntity2)).thenReturn(renderRequest2);
		when(templatingClient.render(municipalityId1, renderRequest1)).thenReturn(renderResponse1);
		when(templatingClient.render(municipalityId2, renderRequest2)).thenReturn(renderResponse2);

		// Act
		final var result1 = templatingIntegration.renderPdf(municipalityId1, letterEntity1);
		final var result2 = templatingIntegration.renderPdf(municipalityId2, letterEntity2);

		// Assert
		assertThat(result1).isNotNull().isSameAs(renderResponse1);
		assertThat(result2).isNotNull().isSameAs(renderResponse2);
		assertThat(result1).isNotSameAs(result2);

		verify(templatingMapper).toRenderRequest(letterEntity1);
		verify(templatingMapper).toRenderRequest(letterEntity2);
		verify(templatingClient).render(municipalityId1, renderRequest1);
		verify(templatingClient).render(municipalityId2, renderRequest2);
	}
}
