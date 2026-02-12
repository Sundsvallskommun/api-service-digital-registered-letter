package se.sundsvall.digitalregisteredletter.integration.db;

import static org.springframework.data.jpa.domain.Specification.allOf;
import static se.sundsvall.digitalregisteredletter.integration.db.specification.LetterSpecification.withCreatedEqualOrAfter;
import static se.sundsvall.digitalregisteredletter.integration.db.specification.LetterSpecification.withCreatedEqualOrBefore;
import static se.sundsvall.digitalregisteredletter.integration.db.specification.LetterSpecification.withDeleted;
import static se.sundsvall.digitalregisteredletter.integration.db.specification.LetterSpecification.withDepartmentOrgId;
import static se.sundsvall.digitalregisteredletter.integration.db.specification.LetterSpecification.withMunicipalityId;
import static se.sundsvall.digitalregisteredletter.integration.db.specification.LetterSpecification.withUsername;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

@CircuitBreaker(name = "letterRepository")
public interface LetterRepository extends JpaRepository<LetterEntity, String>, JpaSpecificationExecutor<LetterEntity> {

	Optional<LetterEntity> findByIdAndMunicipalityIdAndDeleted(final String id, final String municipalityId, boolean deleted);

	List<LetterEntity> findAllByMunicipalityIdAndIdInAndDeletedFalse(final String municipalityId, final List<String> ids);

	List<LetterEntity> findAllByDeleted(final boolean deleted);

	Optional<LetterEntity> findByIdAndDeleted(final String id, final boolean deleted);

	/**
	 * Performs a search in LetterEntities.
	 *
	 * @param  municipalityId muncipality id for the letter entity
	 * @param  filter         optional filters to match when retriving result
	 * @param  deleted        filter that decides if only deleted, only non deleted or all entities shall be part of the
	 *                        result
	 * @param  pageable       the pageable object.
	 * @return                a Page of LetterEntity objects that matches the search parameters
	 */
	default Page<LetterEntity> findAllByFilter(String municipalityId, LetterFilter filter, Boolean deleted, Pageable pageable) {
		return this.findAll(
			allOf(withMunicipalityId(municipalityId)
				.and(withDepartmentOrgId(filter.orgId()))
				.and(withUsername(filter.username()))
				.and(withCreatedEqualOrAfter(filter.createdEarliest()))
				.and(withCreatedEqualOrBefore(filter.createdLatest()))
				.and(withDeleted(deleted))),
			pageable);
	}
}
