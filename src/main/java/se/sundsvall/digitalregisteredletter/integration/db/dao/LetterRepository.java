package se.sundsvall.digitalregisteredletter.integration.db.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;

@Repository
public interface LetterRepository extends JpaRepository<LetterEntity, String> {

	Optional<LetterEntity> findLetterByIdAndDeleted(final String id, boolean deleted);

	Optional<LetterEntity> findByIdAndDeleted(String id, boolean deleted);

	List<LetterEntity> findAllByDeleted(boolean deleted);

	@Modifying
	@Query("UPDATE LetterEntity l SET l.deleted = true WHERE l.id = :id")
	void deleteLetterById(final String id);

}
