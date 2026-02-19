package se.sundsvall.digitalregisteredletter.integration.db.specification;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilterBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class LetterSpecificationTest {

	@Mock
	private LetterRepository letterRepositoryMock;

	@Mock
	private PageRequest pageRequestMock;

	@Captor
	private ArgumentCaptor<Specification<LetterEntity>> specificationCaptor;

	@Test
	void verifySpecificationsWithAllValuesEmpty() {
		when(letterRepositoryMock.findAllByFilter(any(), any(), any(), any())).thenCallRealMethod();

		try (MockedStatic<LetterSpecification> specificationMock = mockStatic(LetterSpecification.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {
			// Act
			letterRepositoryMock.findAllByFilter(null, LetterFilterBuilder.create().build(), null, pageRequestMock);

			// Verify and assert
			verify(letterRepositoryMock).findAll(specificationCaptor.capture(), eq(pageRequestMock));
			specificationMock.verify(() -> LetterSpecification.withMunicipalityId(null));
			specificationMock.verify(() -> LetterSpecification.withUsername(null));
			specificationMock.verify(() -> LetterSpecification.withDeleted(null));
			specificationMock.verify(() -> LetterSpecification.withDepartmentOrgId(null));
			specificationMock.verify(() -> LetterSpecification.withCreatedEqualOrAfter(null));
			specificationMock.verify(() -> LetterSpecification.withCreatedEqualOrBefore(null));
			assertThat(specificationCaptor.getValue()).isNotNull();

			specificationMock.verifyNoMoreInteractions();
			verifyNoMoreInteractions(letterRepositoryMock, pageRequestMock);
		}
	}

	@Test
	void verifySpecificationsWithAllValuesSet() {
		final var municipalityId = "2281";
		final var deleted = true;
		final var createdEarliest = OffsetDateTime.now().minusDays(7);
		final var createdLatest = OffsetDateTime.now();
		final var orgId = 123;
		final var username = "username";

		final var letterFilter = LetterFilterBuilder.create()
			.withCreatedEarliest(createdEarliest)
			.withCreatedLatest(createdLatest)
			.withOrgId(orgId)
			.withUsername(username)
			.build();

		when(letterRepositoryMock.findAllByFilter(any(), any(), any(), any())).thenCallRealMethod();

		try (MockedStatic<LetterSpecification> specificationMock = mockStatic(LetterSpecification.class, withSettings().defaultAnswer(CALLS_REAL_METHODS))) {
			// Act
			letterRepositoryMock.findAllByFilter(municipalityId, letterFilter, deleted, pageRequestMock);

			// Verify and assert
			verify(letterRepositoryMock).findAll(specificationCaptor.capture(), eq(pageRequestMock));
			specificationMock.verify(() -> LetterSpecification.withMunicipalityId(municipalityId));
			specificationMock.verify(() -> LetterSpecification.withUsername(username));
			specificationMock.verify(() -> LetterSpecification.withDeleted(deleted));
			specificationMock.verify(() -> LetterSpecification.withDepartmentOrgId(orgId));
			specificationMock.verify(() -> LetterSpecification.withCreatedEqualOrAfter(createdEarliest));
			specificationMock.verify(() -> LetterSpecification.withCreatedEqualOrBefore(createdLatest));
			assertThat(specificationCaptor.getValue()).isNotNull();

			specificationMock.verifyNoMoreInteractions();
			verifyNoMoreInteractions(letterRepositoryMock, pageRequestMock);
		}

	}

}
