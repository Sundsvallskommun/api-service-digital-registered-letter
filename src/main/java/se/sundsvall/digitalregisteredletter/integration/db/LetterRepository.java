package se.sundsvall.digitalregisteredletter.integration.db;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;

@Repository
public interface LetterRepository extends JpaRepository<LetterEntity, String> {

	Optional<LetterEntity> findByIdAndMunicipalityIdAndDeleted(final String id, final String municipalityId, boolean deleted);

	List<LetterEntity> findAllByDeleted(final boolean deleted);

	Page<LetterEntity> findAllByMunicipalityIdAndDeleted(final String municipalityId, final boolean deleted, final Pageable pageable);

	Optional<LetterEntity> findByIdAndDeleted(final String id, final boolean deleted);
}
