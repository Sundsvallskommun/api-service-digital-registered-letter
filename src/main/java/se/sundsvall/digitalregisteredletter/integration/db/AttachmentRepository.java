package se.sundsvall.digitalregisteredletter.integration.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;

@Repository
public interface AttachmentRepository extends JpaRepository<AttachmentEntity, String> {

	Optional<AttachmentEntity> findByIdAndLetterIdAndLetter_MunicipalityId(final String id, final String letterId, final String municipalityId);
}
