package se.sundsvall.digitalregisteredletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createLetterEntity;
import static se.sundsvall.TestDataFactory.createLetterRequest;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentsBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilterBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.integration.db.RepositoryIntegration;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.service.util.IdentifierUtil;

@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private RepositoryIntegration repositoryIntegrationMock;

	@InjectMocks
	private LetterService letterService;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(repositoryIntegrationMock, partyIntegrationMock, kivraIntegrationMock);
	}

	@Test
	void sendLetter() {
		final var username = "username";
		final var multipartFile = mock(MultipartFile.class);
		final var municipalityId = "2281";
		final var letterRequest = createLetterRequest();
		final var legalId = "legalId";
		final var attachments = AttachmentsBuilder.create()
			.withFiles(List.of(multipartFile))
			.build();
		final var letterEntity = createLetterEntity();
		final var status = "status";

		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, letterRequest.partyId())).thenReturn(legalId);
		when(repositoryIntegrationMock.persistLetter(any(), any(), any(), any())).thenReturn(letterEntity);
		when(kivraIntegrationMock.sendContent(letterEntity, legalId)).thenReturn(status);

		try (final MockedStatic<IdentifierUtil> identifierUtilMock = mockStatic(IdentifierUtil.class)) {
			identifierUtilMock.when(IdentifierUtil::getAdUser).thenReturn(username);

			final var response = letterService.sendLetter(municipalityId, letterRequest, attachments);

			assertThat(response).isEqualTo(letterEntity.getId());

			identifierUtilMock.verify(IdentifierUtil::getAdUser);
			verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, letterRequest.partyId());
			verify(repositoryIntegrationMock).persistLetter(municipalityId, username, letterRequest, attachments);
			verify(kivraIntegrationMock).sendContent(letterEntity, legalId);
			verify(repositoryIntegrationMock).updateStatus(letterEntity, status);
			identifierUtilMock.verifyNoMoreInteractions();
		}
	}

	@Test
	void sendLetterForNonExistingPartyId() {
		final var username = "username";
		final var municipalityId = "2281";
		final var letterRequest = createLetterRequest();
		final var attachments = AttachmentsBuilder.create()
			.build();
		AttachmentEntity.create()
			.withContentType("application/pdf")
			.withFileName("attachment.pdf");
		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, letterRequest.partyId())).thenThrow(Problem.valueOf(Status.BAD_REQUEST, "The given partyId [%s] does not exist in the Party API or is not of type PRIVATE".formatted(letterRequest
			.partyId())));

		try (final MockedStatic<IdentifierUtil> identifierUtilMock = mockStatic(IdentifierUtil.class)) {
			identifierUtilMock.when(IdentifierUtil::getAdUser).thenReturn(username);

			assertThrows(ThrowableProblem.class, () -> letterService.sendLetter(municipalityId, letterRequest, attachments));

			identifierUtilMock.verify(IdentifierUtil::getAdUser);
			verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, letterRequest.partyId());
			identifierUtilMock.verifyNoMoreInteractions();
		}
	}

	@Test
	void getLetter() {
		final var municipalityId = "2281";
		final var letterId = "12345";
		final var letterEntity = createLetterEntity();

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntity));

		final var result = letterService.getLetter(municipalityId, letterId);

		assertThat(result).isNotNull().isInstanceOf(Letter.class);
		assertThat(result.id()).isEqualTo(letterEntity.getId());
		assertThat(result.municipalityId()).isEqualTo(letterEntity.getMunicipalityId());
		assertThat(result.body()).isEqualTo(letterEntity.getBody());
		assertThat(result.contentType()).isEqualTo(letterEntity.getContentType());
		assertThat(result.status()).isEqualTo(letterEntity.getStatus());
		assertThat(result.attachments()).isNotNull().hasSize(letterEntity.getAttachments().size());
		assertThat(result.supportInfo()).isNotNull();
		assertThat(result.supportInfo().supportText()).isEqualTo(letterEntity.getSupportInformation().getSupportText());
		assertThat(result.supportInfo().contactInformationUrl()).isEqualTo(letterEntity.getSupportInformation().getContactInformationUrl());
		assertThat(result.supportInfo().contactInformationEmail()).isEqualTo(letterEntity.getSupportInformation().getContactInformationEmail());
		assertThat(result.supportInfo().contactInformationPhoneNumber()).isEqualTo(letterEntity.getSupportInformation().getContactInformationPhoneNumber());

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
	}

	@Test
	void getLetterNotFound() {
		final var municipalityId = "2281";
		final var letterId = "12345";

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> letterService.getLetter(municipalityId, letterId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Letter with id '%s' and municipalityId '%s' not found", letterId, municipalityId);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
	}

	@Test
	void getLetters() {
		final var municipalityId = "2281";
		final var pageable = mock(Pageable.class);
		final var letterEntity = createLetterEntity();
		final var letterFilter = LetterFilterBuilder.create().build();

		when(repositoryIntegrationMock.getPagedLetterEntities(municipalityId, letterFilter, pageable)).thenReturn(new PageImpl<>(List.of(letterEntity)));

		final var result = letterService.getLetters(municipalityId, letterFilter, pageable);

		assertThat(result).isNotNull().isInstanceOf(Letters.class);
		assertThat(result.metaData()).satisfies(metaData -> {
			assertThat(metaData.getPage()).isEqualTo(1);
			assertThat(metaData.getSortDirection()).isNull();
		});
		assertThat(result.letters()).hasSize(1).allSatisfy(assertedLetter -> {
			assertThat(assertedLetter.id()).isEqualTo(letterEntity.getId());
			assertThat(assertedLetter.municipalityId()).isEqualTo(letterEntity.getMunicipalityId());
			assertThat(assertedLetter.body()).isEqualTo(letterEntity.getBody());
			assertThat(assertedLetter.contentType()).isEqualTo(letterEntity.getContentType());
			assertThat(assertedLetter.status()).isEqualTo(letterEntity.getStatus());
			assertThat(assertedLetter.attachments()).isNotNull().hasSize(letterEntity.getAttachments().size());
			assertThat(assertedLetter.supportInfo()).isNotNull();
			assertThat(assertedLetter.supportInfo().supportText()).isEqualTo(letterEntity.getSupportInformation().getSupportText());
			assertThat(assertedLetter.supportInfo().contactInformationUrl()).isEqualTo(letterEntity.getSupportInformation().getContactInformationUrl());
			assertThat(assertedLetter.supportInfo().contactInformationEmail()).isEqualTo(letterEntity.getSupportInformation().getContactInformationEmail());
			assertThat(assertedLetter.supportInfo().contactInformationPhoneNumber()).isEqualTo(letterEntity.getSupportInformation().getContactInformationPhoneNumber());
		});

		verify(repositoryIntegrationMock).getPagedLetterEntities(municipalityId, letterFilter, pageable);
	}

}
