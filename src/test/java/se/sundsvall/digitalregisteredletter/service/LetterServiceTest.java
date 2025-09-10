package se.sundsvall.digitalregisteredletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentsBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;

@ExtendWith(MockitoExtension.class)
class LetterServiceTest {

	@Mock
	private LetterRepository letterRepositoryMock;

	@Mock
	private AttachmentMapper attachmentMapperMock;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Captor
	private ArgumentCaptor<LetterEntity> letterEntityArgumentCaptor;

	@InjectMocks
	private LetterService letterService;

	@AfterEach
	void ensureNoInteractionsWereMissed() {
		verifyNoMoreInteractions(attachmentMapperMock, letterRepositoryMock, partyIntegrationMock, kivraIntegrationMock);
	}

	@Test
	void sendLetterTest() {
		var multipartFile = mock(MultipartFile.class);
		var municipalityId = "2281";
		var letterRequest = createLetterRequest();
		var legalId = "legalId";
		var attachments = AttachmentsBuilder.create()
			.withFiles(List.of(multipartFile))
			.build();
		var attachmentEntity = AttachmentEntity.create()
			.withContentType("application/pdf")
			.withFileName("attachment.pdf");
		when(letterRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(attachmentMapperMock.toAttachmentEntities(attachments)).thenReturn(List.of(attachmentEntity));
		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, letterRequest.partyId())).thenReturn(legalId);

		var response = letterService.sendLetter(municipalityId, letterRequest, attachments);

		assertThat(response).isNull();

		verify(letterRepositoryMock, times(2)).save(letterEntityArgumentCaptor.capture());
		var capturedLetterEntity = letterEntityArgumentCaptor.getValue();
		assertThat(capturedLetterEntity).isNotNull();
		assertThat(capturedLetterEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(capturedLetterEntity.getAttachments()).containsExactly(attachmentEntity);

		verify(attachmentMapperMock).toAttachmentEntities(attachments);
		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, letterRequest.partyId());
		verify(kivraIntegrationMock).sendContent(capturedLetterEntity, legalId);
	}

	@Test
	void getLetterTest() {
		var municipalityId = "2281";
		var letterId = "12345";
		var letterEntity = createLetterEntity();

		when(letterRepositoryMock.findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false))
			.thenReturn(Optional.of(letterEntity));

		var result = letterService.getLetter(municipalityId, letterId);

		assertThat(result).isNotNull().isInstanceOf(Letter.class);
		assertThat(result.id()).isEqualTo(letterEntity.getId());
		assertThat(result.municipalityId()).isEqualTo(letterEntity.getMunicipalityId());
		assertThat(result.body()).isEqualTo(letterEntity.getBody());
		assertThat(result.contentType()).isEqualTo(letterEntity.getContentType());
		assertThat(result.status()).isEqualTo(letterEntity.getStatus());
		assertThat(result.attachments()).isNotNull().hasSize(letterEntity.getAttachments().size());
		assertThat(result.supportInfo()).isNotNull();
		assertThat(result.supportInfo().supportText()).isEqualTo(letterEntity.getSupportInfo().getSupportText());
		assertThat(result.supportInfo().contactInformationUrl()).isEqualTo(letterEntity.getSupportInfo().getContactInformationUrl());
		assertThat(result.supportInfo().contactInformationEmail()).isEqualTo(letterEntity.getSupportInfo().getContactInformationEmail());
		assertThat(result.supportInfo().contactInformationPhoneNumber()).isEqualTo(letterEntity.getSupportInfo().getContactInformationPhoneNumber());

		verify(letterRepositoryMock).findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false);
	}

	@Test
	void getLetterNotFoundTest() {
		var municipalityId = "2281";
		var letterId = "12345";

		when(letterRepositoryMock.findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> letterService.getLetter(municipalityId, letterId))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Letter with id '%s' and municipalityId '%s' not found", letterId, municipalityId);

		verify(letterRepositoryMock).findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false);
	}

	@Test
	void getLettersTest() {
		var municipalityId = "2281";
		var pageable = mock(Pageable.class);
		var letterEntity = createLetterEntity();

		when(letterRepositoryMock.findAllByMunicipalityIdAndDeleted(municipalityId, false, pageable))
			.thenReturn(new PageImpl<>(List.of(letterEntity)));

		var result = letterService.getLetters(municipalityId, pageable);

		assertThat(result).isNotNull().isInstanceOf(Letters.class);
		assertThat(result.metaData()).satisfies(metaData -> {
			assertThat(metaData.getPage()).isEqualTo(1);
			assertThat(metaData.getSortDirection()).isNull();
		});
		assertThat(result.letters()).hasSize(1).allSatisfy(letter -> {
			assertThat(letter.id()).isEqualTo(letterEntity.getId());
			assertThat(letter.municipalityId()).isEqualTo(letterEntity.getMunicipalityId());
			assertThat(letter.body()).isEqualTo(letterEntity.getBody());
			assertThat(letter.contentType()).isEqualTo(letterEntity.getContentType());
			assertThat(letter.status()).isEqualTo(letterEntity.getStatus());
			assertThat(letter.attachments()).isNotNull().hasSize(letterEntity.getAttachments().size());
			assertThat(letter.supportInfo()).isNotNull();
			assertThat(letter.supportInfo().supportText()).isEqualTo(letterEntity.getSupportInfo().getSupportText());
			assertThat(letter.supportInfo().contactInformationUrl()).isEqualTo(letterEntity.getSupportInfo().getContactInformationUrl());
			assertThat(letter.supportInfo().contactInformationEmail()).isEqualTo(letterEntity.getSupportInfo().getContactInformationEmail());
			assertThat(letter.supportInfo().contactInformationPhoneNumber()).isEqualTo(letterEntity.getSupportInfo().getContactInformationPhoneNumber());
		});

		verify(letterRepositoryMock).findAllByMunicipalityIdAndDeleted(municipalityId, false, pageable);
	}

}
