package se.sundsvall.digitalregisteredletter.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createAttachments;
import static se.sundsvall.TestDataFactory.createLetterRequest;

import java.util.ArrayList;
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
import org.springframework.data.domain.PageRequest;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilterBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;

@ExtendWith(MockitoExtension.class)
class RepositoryIntegrationTest {

	@Mock
	private AttachmentMapper attachmentMapperMock;

	@Mock
	private LetterRepository letterRepositoryMock;

	@Mock
	private LetterEntity letterEntityMock;

	@Mock
	private OrganizationRepository organizationRepositoryMock;

	@Mock
	private UserRepository userRepositoryMock;

	@InjectMocks
	private RepositoryIntegration repositoryIntegration;

	@Captor
	private ArgumentCaptor<LetterEntity> letterEntityArgumentCaptor;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(
			letterEntityMock,
			letterRepositoryMock,
			organizationRepositoryMock,
			userRepositoryMock);
	}

	@Test
	void persistLetterWhenOrganizationAndUserExist() {
		final var municipalityId = "2281";
		final var username = "username";
		final var letterId = "letterId";
		final var letterRequest = createLetterRequest();
		final var attachments = createAttachments();

		final var attachmentEntity = AttachmentEntity.create();
		final var existingLetterEntity = LetterEntity.create()
			.withId("existingLetterId");
		final var organizationEntity = OrganizationEntity.create()
			.withId("organizationId")
			.withLetters(new ArrayList<>(List.of(existingLetterEntity)));
		final var userEntity = UserEntity.create()
			.withId("userId")
			.withLetters(new ArrayList<>(List.of(existingLetterEntity)));

		when(attachmentMapperMock.toAttachmentEntities(attachments)).thenReturn(List.of(attachmentEntity));
		when(organizationRepositoryMock.findByNumber(234)).thenReturn(Optional.of(organizationEntity));
		when(userRepositoryMock.findByUsernameIgnoreCase(username)).thenReturn(Optional.of(userEntity));
		when(letterRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0, LetterEntity.class).withId(letterId));

		final var response = repositoryIntegration.persistLetter(municipalityId, username, letterRequest, attachments);

		verify(attachmentMapperMock).toAttachmentEntities(attachments);
		verify(letterRepositoryMock).save(letterEntityArgumentCaptor.capture());
		verify(organizationRepositoryMock).findByNumber(234);
		verify(userRepositoryMock).findByUsernameIgnoreCase(username);

		final var capturedLetterEntity = letterEntityArgumentCaptor.getValue();
		assertThat(response).isNotNull().extracting(LetterEntity::getId).isEqualTo(letterId);
		assertThat(capturedLetterEntity).isNotNull();
		assertThat(capturedLetterEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(capturedLetterEntity.getAttachments()).containsExactly(attachmentEntity);
		assertThat(capturedLetterEntity.getUser()).isEqualTo(userEntity);
		assertThat(capturedLetterEntity.getUser()).satisfies(assertedUserEntity -> {
			assertThat(assertedUserEntity.getId()).isEqualTo("userId");
			assertThat(assertedUserEntity.getLetters()).hasSize(2)
				.containsExactlyInAnyOrder(existingLetterEntity, capturedLetterEntity);
		});
		assertThat(capturedLetterEntity.getOrganization()).isEqualTo(organizationEntity);
		assertThat(capturedLetterEntity.getOrganization()).satisfies(assertedOrganizationEntity -> {
			assertThat(assertedOrganizationEntity.getId()).isEqualTo("organizationId");
			assertThat(assertedOrganizationEntity.getLetters()).hasSize(2)
				.containsExactlyInAnyOrder(existingLetterEntity, capturedLetterEntity);
		});
	}

	@Test
	void persistLetterWhenOrganizationAndUserDoesNotExist() {
		final var letterId = "letterId";
		final var municipalityId = "2281";
		final var username = "username";
		final var letterRequest = createLetterRequest();
		final var attachments = createAttachments();

		final var attachmentEntity = AttachmentEntity.create()
			.withContentType("application/pdf")
			.withFileName("attachment.pdf");

		when(attachmentMapperMock.toAttachmentEntities(attachments)).thenReturn(List.of(attachmentEntity));
		when(letterRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0, LetterEntity.class).withId(letterId));

		final var response = repositoryIntegration.persistLetter(municipalityId, username, letterRequest, attachments);

		verify(attachmentMapperMock).toAttachmentEntities(attachments);
		verify(letterRepositoryMock).save(letterEntityArgumentCaptor.capture());
		verify(organizationRepositoryMock).findByNumber(234);
		verify(userRepositoryMock).findByUsernameIgnoreCase(username);

		final var capturedLetterEntity = letterEntityArgumentCaptor.getValue();
		assertThat(response).isNotNull().extracting(LetterEntity::getId).isEqualTo(letterId);
		assertThat(capturedLetterEntity).isNotNull();
		assertThat(capturedLetterEntity.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(capturedLetterEntity.getAttachments()).containsExactly(attachmentEntity);
		assertThat(capturedLetterEntity.getUser()).isNotNull();
		assertThat(capturedLetterEntity.getUser()).satisfies(assertedUserEntity -> {
			assertThat(assertedUserEntity.getId()).isNull();
			assertThat(assertedUserEntity.getUsername()).isEqualTo(username);
			assertThat(assertedUserEntity.getLetters()).hasSize(1)
				.contains(capturedLetterEntity);
		});
		assertThat(capturedLetterEntity.getOrganization()).isNotNull();
		assertThat(capturedLetterEntity.getOrganization()).satisfies(assertedOrganizationEntity -> {
			assertThat(assertedOrganizationEntity.getId()).isNull();
			assertThat(assertedOrganizationEntity.getNumber()).isEqualTo(234);
			assertThat(assertedOrganizationEntity.getLetters()).hasSize(1)
				.contains(capturedLetterEntity);
		});
	}

	@Test
	void updateStatus() {
		final var status = "status";

		repositoryIntegration.updateStatus(letterEntityMock, status);

		verify(letterEntityMock).setStatus(status);
		verify(letterRepositoryMock).save(letterEntityMock);
	}

	@Test
	void getLetterEntity() {
		final var letterId = "letterId";
		final var municipalityId = "municipalityId";

		when(letterRepositoryMock.findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false)).thenReturn(Optional.of(letterEntityMock));

		assertThat(repositoryIntegration.getLetterEntity(municipalityId, letterId)).isPresent().contains(letterEntityMock);

		verify(letterRepositoryMock).findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false);
	}

	@Test
	void getPagedLetterEntities() {
		final var municipalityId = "municipalityId";
		final var letterFilter = LetterFilterBuilder.create().build();
		final var pageable = PageRequest.of(1, 100);

		when(letterRepositoryMock.findAllByFilter(municipalityId, letterFilter, false, pageable)).thenReturn(new PageImpl<>(List.of(letterEntityMock)));

		assertThat(repositoryIntegration.getPagedLetterEntities(municipalityId, letterFilter, pageable)).hasSize(1).contains(letterEntityMock);

		verify(letterRepositoryMock).findAllByFilter(municipalityId, letterFilter, false, pageable);
	}

	@Test
	void softDeleteLetterEntity() {
		final var letterId = "letterId";

		when(letterRepositoryMock.findById(letterId)).thenReturn(Optional.of(letterEntityMock));

		repositoryIntegration.softDeleteLetterEntity(letterId);

		verify(letterRepositoryMock).findById(letterId);
		verify(letterEntityMock).setDeleted(true);
		verify(letterRepositoryMock).save(letterEntityMock);

	}

	@Test
	void softDeleteNonPresentLetterEntity() {
		final var letterId = "letterId";

		repositoryIntegration.softDeleteLetterEntity(letterId);

		verify(letterRepositoryMock).findById(letterId);
		verify(letterEntityMock, never()).setDeleted(true);
		verify(letterRepositoryMock, never()).save(letterEntityMock);
	}
}
