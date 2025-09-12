package se.sundsvall.digitalregisteredletter.integration.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {

	Optional<OrganizationEntity> findByNumber(final Integer number);
}
