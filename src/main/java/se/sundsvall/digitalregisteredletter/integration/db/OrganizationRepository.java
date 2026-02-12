package se.sundsvall.digitalregisteredletter.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;

@CircuitBreaker(name = "organizationRepository")
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, String> {

	Optional<OrganizationEntity> findByNumber(final Integer number);
}
