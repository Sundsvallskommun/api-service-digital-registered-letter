package se.sundsvall.digitalregisteredletter.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createAttachments;
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
import org.springframework.data.domain.PageRequest;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilterBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;

@ExtendWith(MockitoExtension.class)
class RepositoryIntegrationTest {

	@Mock
	private AttachmentMapper attachmentMapperMock;

	@Mock
	private LetterMapper letterMapperMock;

	@Mock
	private LetterRepository letterRepositoryMock;

	@Mock
	private OrganizationRepository organizationRepositoryMock;

	@Mock
	private UserRepository userRepositoryMock;

	@Mock
	private AttachmentEntity attachmentEntityMock;

	@Mock
	private LetterEntity letterEntityMock;

	@Mock
	private OrganizationEntity organizationEntityMock;

	@Mock
	private UserEntity userEntityMock;

	@InjectMocks
	private RepositoryIntegration repositoryIntegration;

	@Captor
	private ArgumentCaptor<LetterEntity> letterEntityArgumentCaptor;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(
			attachmentMapperMock,
			letterEntityMock,
			letterMapperMock,
			letterRepositoryMock,
			organizationRepositoryMock,
			userRepositoryMock,
			attachmentEntityMock,
			letterEntityMock,
			organizationEntityMock,
			userEntityMock);
	}

	@Test
	void persistLetterWhenOrganizationAndUserExist() {
		final var municipalityId = "2281";
		final var username = "username";
		final var letterRequest = createLetterRequest();
		final var attachments = createAttachments();

		when(letterMapperMock.toLetterEntity(letterRequest)).thenReturn(letterEntityMock);
		when(letterMapperMock.addLetter(organizationEntityMock, letterEntityMock)).thenReturn(organizationEntityMock);
		when(letterMapperMock.addLetter(userEntityMock, letterEntityMock)).thenReturn(userEntityMock);
		when(attachmentMapperMock.toAttachmentEntities(attachments)).thenReturn(List.of(attachmentEntityMock));
		when(organizationRepositoryMock.findByNumber(anyInt())).thenReturn(Optional.of(organizationEntityMock));
		when(userRepositoryMock.findByUsernameIgnoreCase(any())).thenReturn(Optional.of(userEntityMock));
		when(letterRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(letterEntityMock.withAttachments(List.of(attachmentEntityMock))).thenReturn(letterEntityMock);
		when(letterEntityMock.withMunicipalityId(municipalityId)).thenReturn(letterEntityMock);
		when(letterEntityMock.withStatus(any())).thenReturn(letterEntityMock);

		final var response = repositoryIntegration.persistLetter(municipalityId, username, letterRequest, attachments);

		verify(letterMapperMock).toLetterEntity(letterRequest);
		verify(letterMapperMock).addLetter(organizationEntityMock, letterEntityMock);
		verify(letterMapperMock).addLetter(userEntityMock, letterEntityMock);
		verify(letterMapperMock).toOrganizationEntity(letterRequest.organization(), letterEntityMock);
		verify(letterMapperMock).toUserEntity(username, letterEntityMock);
		verify(attachmentMapperMock).toAttachmentEntities(attachments);
		verify(organizationRepositoryMock).findByNumber(234);
		verify(userRepositoryMock).findByUsernameIgnoreCase(username);
		verify(letterRepositoryMock).save(letterEntityMock);
		verify(letterEntityMock).withAttachments(List.of(attachmentEntityMock));
		verify(letterEntityMock).withMunicipalityId(municipalityId);
		verify(letterEntityMock).withStatus("NEW");
		verify(letterEntityMock).setOrganization(organizationEntityMock);
		verify(letterEntityMock).setUser(userEntityMock);

		assertThat(response).isEqualTo(letterEntityMock);
	}

	@Test
	void persistLetterWhenOrganizationAndUserDoesNotExist() {
		final var municipalityId = "2281";
		final var username = "username";
		final var letterRequest = createLetterRequest();
		final var attachments = createAttachments();

		when(letterMapperMock.toLetterEntity(letterRequest)).thenReturn(letterEntityMock);
		when(letterMapperMock.toOrganizationEntity(any(), any())).thenCallRealMethod();
		when(letterMapperMock.toUserEntity(any(), any())).thenCallRealMethod();
		when(attachmentMapperMock.toAttachmentEntities(attachments)).thenReturn(List.of(attachmentEntityMock));

		when(letterRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0, LetterEntity.class));
		when(letterEntityMock.withAttachments(List.of(attachmentEntityMock))).thenReturn(letterEntityMock);
		when(letterEntityMock.withMunicipalityId(municipalityId)).thenReturn(letterEntityMock);
		when(letterEntityMock.withStatus(any())).thenReturn(letterEntityMock);

		final var response = repositoryIntegration.persistLetter(municipalityId, username, letterRequest, attachments);

		verify(letterMapperMock).toLetterEntity(letterRequest);
		verify(letterMapperMock).toOrganizationEntity(letterRequest.organization(), letterEntityMock);
		verify(letterMapperMock).toUserEntity(username, letterEntityMock);
		verify(letterMapperMock).toOrganizationEntity(letterRequest.organization(), letterEntityMock);
		verify(letterMapperMock).toUserEntity(username, letterEntityMock);
		verify(letterMapperMock, never()).addLetter(any(OrganizationEntity.class), any());
		verify(letterMapperMock, never()).addLetter(any(UserEntity.class), any());
		verify(attachmentMapperMock).toAttachmentEntities(attachments);
		verify(organizationRepositoryMock).findByNumber(234);
		verify(userRepositoryMock).findByUsernameIgnoreCase(username);
		verify(letterRepositoryMock).save(letterEntityMock);
		verify(letterEntityMock).withAttachments(List.of(attachmentEntityMock));
		verify(letterEntityMock).withMunicipalityId(municipalityId);
		verify(letterEntityMock).withStatus("NEW");
		verify(letterEntityMock, never()).setOrganization(organizationEntityMock);
		verify(letterEntityMock, never()).setUser(userEntityMock);
		verify(letterEntityMock).setOrganization(any());
		verify(letterEntityMock).setUser(any());

		assertThat(response).isEqualTo(letterEntityMock);
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
