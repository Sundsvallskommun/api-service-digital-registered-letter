package se.sundsvall.digitalregisteredletter.integration.db.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;

@Repository
public interface LetterRepository extends JpaRepository<LetterEntity, String> {

	Optional<LetterEntity> findByIdAndMunicipalityIdAndDeleted(final String id, final String municipalityId, boolean deleted);

	List<LetterEntity> findAllByDeleted(final boolean deleted);

	@Modifying
	@Query("UPDATE LetterEntity l SET l.deleted = true WHERE l.id = :id")
	void deleteLetterById(final String id);

	Page<LetterEntity> findAllByMunicipalityIdAndDeleted(final String municipalityId, final boolean deleted, final Pageable pageable);

	Optional<LetterEntity> findByIdAndDeleted(final String id, final boolean deleted);
}
