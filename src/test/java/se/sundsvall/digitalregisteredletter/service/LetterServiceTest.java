package se.sundsvall.digitalregisteredletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentsBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.integration.db.RepositoryIntegration;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.SigningInformationEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;
import se.sundsvall.digitalregisteredletter.service.util.IdentifierUtil;

@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private RepositoryIntegration repositoryIntegrationMock;

	@Mock
	private LetterMapper letterMapperMock;

	@InjectMocks
	private LetterService letterService;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(repositoryIntegrationMock, partyIntegrationMock, kivraIntegrationMock, letterMapperMock);
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
		final var letterMock = mock(Letter.class);
		final var status = "status";

		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, letterRequest.partyId())).thenReturn(legalId);
		when(repositoryIntegrationMock.persistLetter(any(), any(), any(), any())).thenReturn(letterEntity);
		when(kivraIntegrationMock.sendContent(letterEntity, legalId)).thenReturn(status);
		when(letterMapperMock.toLetter(letterEntity)).thenReturn(letterMock);

		try (final MockedStatic<IdentifierUtil> identifierUtilMock = mockStatic(IdentifierUtil.class)) {
			identifierUtilMock.when(IdentifierUtil::getAdUser).thenReturn(username);

			final var response = letterService.sendLetter(municipalityId, letterRequest, attachments);

			assertThat(response).isEqualTo(letterMock);

			identifierUtilMock.verify(IdentifierUtil::getAdUser);
			verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, letterRequest.partyId());
			verify(repositoryIntegrationMock).persistLetter(municipalityId, username, letterRequest, attachments);
			verify(kivraIntegrationMock).sendContent(letterEntity, legalId);
			verify(repositoryIntegrationMock).updateStatus(letterEntity, status);
			verifyNoInteractions(letterMock);
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
		final var letterEntityMock = mock(LetterEntity.class);
		final var letterMock = mock(Letter.class);

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));
		when(letterMapperMock.toLetter(letterEntityMock)).thenReturn(letterMock);

		final var result = letterService.getLetter(municipalityId, letterId);

		assertThat(result).isSameAs(letterMock);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
		verifyNoInteractions(letterEntityMock, letterMock);
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
	void getSigningInformation() {
		final var municipalityId = "2281";
		final var letterId = "12345";
		final var letterEntityMock = mock(LetterEntity.class);
		final var signingInformationEntityMock = mock(SigningInformationEntity.class);
		final var signingInfoMock = mock(SigningInfo.class);

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));
		when(letterEntityMock.getSigningInformation()).thenReturn(signingInformationEntityMock);
		when(letterMapperMock.toSigningInfo(signingInformationEntityMock)).thenReturn(signingInfoMock);

		final var result = letterService.getSigningInformation(municipalityId, letterId);

		assertThat(result).isSameAs(signingInfoMock);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
		verify(letterMapperMock).toSigningInfo(signingInformationEntityMock);
		verify(letterEntityMock).getSigningInformation();
		verifyNoMoreInteractions(letterEntityMock, signingInformationEntityMock, signingInfoMock);
	}

	@Test
	void getSigningInformationForMissingLetter() {
		final var municipalityId = "2281";
		final var letterId = "12345";

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> letterService.getSigningInformation(municipalityId, letterId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Letter with id '%s' and municipalityId '%s' not found", letterId, municipalityId);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
	}

	@Test
	void getSigningInformationNotFound() {
		final var municipalityId = "2281";
		final var letterId = "12345";

		final var letterEntityMock = mock(LetterEntity.class);

		when(repositoryIntegrationMock.getLetterEntity(municipalityId, letterId)).thenReturn(Optional.of(letterEntityMock));

		assertThatThrownBy(() -> letterService.getSigningInformation(municipalityId, letterId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Signing information belonging to letter with id '%s' and municipalityId '%s' not found", letterId, municipalityId);

		verify(repositoryIntegrationMock).getLetterEntity(municipalityId, letterId);
		verify(letterMapperMock).toSigningInfo(null);
	}

	@SuppressWarnings("unchecked")
	@Test
	void getLetters() {
		final var municipalityId = "2281";
		final var letterFilterMock = mock(LetterFilter.class);
		final var pageableMock = mock(Pageable.class);
		final var pageMock = mock(Page.class);
		final var lettersMock = mock(Letters.class);

		when(repositoryIntegrationMock.getPagedLetterEntities(municipalityId, letterFilterMock, pageableMock)).thenReturn(pageMock);
		when(letterMapperMock.toLetters(pageMock)).thenReturn(lettersMock);

		final var result = letterService.getLetters(municipalityId, letterFilterMock, pageableMock);

		assertThat(result).isSameAs(lettersMock);

		verify(repositoryIntegrationMock).getPagedLetterEntities(municipalityId, letterFilterMock, pageableMock);
		verifyNoInteractions(letterFilterMock, pageableMock, pageMock, lettersMock);
	}

}
