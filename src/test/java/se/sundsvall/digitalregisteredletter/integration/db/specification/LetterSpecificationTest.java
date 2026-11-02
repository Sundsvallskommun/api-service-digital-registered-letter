package se.sundsvall.digitalregisteredletter.integration.db.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity_;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class LetterSpecificationTest {

	@Mock
	private LetterRepository letterRepositoryMock;

	@Mock
	private Root<LetterEntity> rootMock;

	@Mock
	private Path<OffsetDateTime> pathMock;

	@Mock
	private CriteriaBuilder criteriaBuilderMock;

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
		final var createdEarliest = LocalDate.now().minusDays(7);
		final var createdLatest = LocalDate.now();
		final var orgId = 123L;
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

	@ParameterizedTest
	@CsvSource({
		"2026-01-15, 2026-01-15T00:00+01:00", // vintertid
		"2026-07-15, 2026-07-15T00:00+02:00", // sommartid
		"2026-03-29, 2026-03-29T00:00+01:00", // sommartid börjar
		"2026-10-25, 2026-10-25T00:00+02:00"  // sommartid slutar
	})
	void withCreatedEqualOrAfterUsesStartOfDay(final LocalDate date, final OffsetDateTime expectedLowerBound) {
		doReturn(pathMock).when(rootMock).get(LetterEntity_.CREATED);

		LetterSpecification.withCreatedEqualOrAfter(date).toPredicate(rootMock, null, criteriaBuilderMock);

		verify(criteriaBuilderMock).greaterThanOrEqualTo(pathMock, expectedLowerBound);
		verifyNoMoreInteractions(criteriaBuilderMock);
	};

	@ParameterizedTest
	@CsvSource({
		"2026-01-15, 2026-01-16T00:00+01:00", // vintertid
		"2026-07-15, 2026-07-16T00:00+02:00", // sommartid
		"2026-03-29, 2026-03-30T00:00+02:00", // sommartid börjar
		"2026-10-25, 2026-10-26T00:00+01:00"  // sommartid slutar
	})
	void withCreatedEqualOrBeforeUsesStartOfNextDayExclusive(LocalDate date, OffsetDateTime expectedUpperBound) {
		doReturn(pathMock).when(rootMock).get(LetterEntity_.CREATED);

		LetterSpecification.withCreatedEqualOrBefore(date).toPredicate(rootMock, null, criteriaBuilderMock);

		verify(criteriaBuilderMock).lessThan(pathMock, expectedUpperBound);
		verifyNoMoreInteractions(criteriaBuilderMock);
	};

	@Test
	void withCreatedFiltersApplyNoFilteringWhenDateIsNull() {
		LetterSpecification.withCreatedEqualOrAfter(null).toPredicate(rootMock, null, criteriaBuilderMock);
		LetterSpecification.withCreatedEqualOrBefore(null).toPredicate(rootMock, null, criteriaBuilderMock);

		verify(criteriaBuilderMock, times(2)).and();
		verifyNoMoreInteractions(criteriaBuilderMock);
		verifyNoInteractions(rootMock);
	};

}
