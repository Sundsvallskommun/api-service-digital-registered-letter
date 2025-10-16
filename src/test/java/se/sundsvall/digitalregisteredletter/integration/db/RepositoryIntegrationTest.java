package se.sundsvall.digitalregisteredletter.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createLetterRequest;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilterBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;

@ExtendWith(MockitoExtension.class)
class RepositoryIntegrationTest {

	private static final String USERNAME = "test01user";

	@Mock
	private AttachmentMapper attachmentMapperMock;

	@Mock
	private AttachmentRepository attachmentRepositoryMock;

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

	@BeforeAll
	static void setup() {
		var xSentBy = Identifier.parse("type=adAccount; %s".formatted(USERNAME));
		Identifier.set(xSentBy);
	}

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(
			attachmentMapperMock,
			attachmentRepositoryMock,
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
		final var letterRequest = createLetterRequest();
		final var multipartFile = Mockito.mock(MultipartFile.class);
		final var multipartFileList = List.of(multipartFile);

		when(letterMapperMock.toLetterEntity(letterRequest)).thenReturn(letterEntityMock);
		when(letterMapperMock.addLetter(organizationEntityMock, letterEntityMock)).thenReturn(organizationEntityMock);
		when(letterMapperMock.addLetter(userEntityMock, letterEntityMock)).thenReturn(userEntityMock);
		when(attachmentMapperMock.toAttachmentEntities(multipartFileList)).thenReturn(List.of(attachmentEntityMock));
		when(organizationRepositoryMock.findByNumber(anyInt())).thenReturn(Optional.of(organizationEntityMock));
		when(userRepositoryMock.findByUsernameIgnoreCase(any())).thenReturn(Optional.of(userEntityMock));
		when(letterRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
		when(letterEntityMock.withAttachments(List.of(attachmentEntityMock))).thenReturn(letterEntityMock);
		when(letterEntityMock.withMunicipalityId(municipalityId)).thenReturn(letterEntityMock);
		when(letterEntityMock.withStatus(any())).thenReturn(letterEntityMock);

		final var response = repositoryIntegration.persistLetter(municipalityId, letterRequest, multipartFileList);

		verify(letterMapperMock).toLetterEntity(letterRequest);
		verify(letterMapperMock).addLetter(organizationEntityMock, letterEntityMock);
		verify(letterMapperMock).addLetter(userEntityMock, letterEntityMock);
		verify(letterMapperMock).toOrganizationEntity(letterRequest.organization(), letterEntityMock);
		verify(letterMapperMock).toUserEntity(USERNAME, letterEntityMock);
		verify(attachmentMapperMock).toAttachmentEntities(multipartFileList);
		verify(organizationRepositoryMock).findByNumber(234);
		verify(userRepositoryMock).findByUsernameIgnoreCase(USERNAME);
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
		final var letterRequest = createLetterRequest();
		final var multipartFile = Mockito.mock(MultipartFile.class);
		final var multipartFileList = List.of(multipartFile);

		when(letterMapperMock.toLetterEntity(letterRequest)).thenReturn(letterEntityMock);
		when(letterMapperMock.toOrganizationEntity(any(), any())).thenCallRealMethod();
		when(letterMapperMock.toUserEntity(any(), any())).thenCallRealMethod();
		when(attachmentMapperMock.toAttachmentEntities(multipartFileList)).thenReturn(List.of(attachmentEntityMock));

		when(letterRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0, LetterEntity.class));
		when(letterEntityMock.withAttachments(List.of(attachmentEntityMock))).thenReturn(letterEntityMock);
		when(letterEntityMock.withMunicipalityId(municipalityId)).thenReturn(letterEntityMock);
		when(letterEntityMock.withStatus(any())).thenReturn(letterEntityMock);

		final var response = repositoryIntegration.persistLetter(municipalityId, letterRequest, multipartFileList);

		verify(letterMapperMock).toLetterEntity(letterRequest);
		verify(letterMapperMock).toOrganizationEntity(letterRequest.organization(), letterEntityMock);
		verify(letterMapperMock).toUserEntity(USERNAME, letterEntityMock);
		verify(letterMapperMock).toOrganizationEntity(letterRequest.organization(), letterEntityMock);
		verify(letterMapperMock).toUserEntity(USERNAME, letterEntityMock);
		verify(letterMapperMock, never()).addLetter(any(OrganizationEntity.class), any());
		verify(letterMapperMock, never()).addLetter(any(UserEntity.class), any());
		verify(attachmentMapperMock).toAttachmentEntities(multipartFileList);
		verify(organizationRepositoryMock).findByNumber(234);
		verify(userRepositoryMock).findByUsernameIgnoreCase(USERNAME);
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
	void getLetterEntities() {
		final var municipalityId = "municipalityId";
		final var letterIds = List.of("letterId1", "letterId2");
		final var letterEntities = List.of(
			mock(LetterEntity.class),
			mock(LetterEntity.class));

		when(letterRepositoryMock.findAllByMunicipalityIdAndIdInAndDeletedFalse(municipalityId, letterIds)).thenReturn(letterEntities);

		assertThat(repositoryIntegration.getLetterEntities(municipalityId, letterIds)).containsAll(letterEntities);

		verify(letterRepositoryMock).findAllByMunicipalityIdAndIdInAndDeletedFalse(municipalityId, letterIds);
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

	@Test
	void getAttachmentEntity() {
		final var attachmentId = "attachmentId";
		final var letterId = "letterId";
		final var municipalityId = "municipalityId";

		when(attachmentRepositoryMock.findByIdAndLetterIdAndLetter_MunicipalityId(attachmentId, letterId, municipalityId))
			.thenReturn(Optional.of(attachmentEntityMock));

		assertThat(repositoryIntegration.getAttachmentEntity(municipalityId, letterId, attachmentId))
			.isPresent()
			.contains(attachmentEntityMock);

		verify(attachmentRepositoryMock).findByIdAndLetterIdAndLetter_MunicipalityId(attachmentId, letterId, municipalityId);
	}
}
