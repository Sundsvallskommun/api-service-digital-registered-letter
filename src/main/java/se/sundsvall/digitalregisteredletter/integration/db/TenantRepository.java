package se.sundsvall.digitalregisteredletter.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;

@CircuitBreaker(name = "tenantRepository")
public interface TenantRepository extends JpaRepository<TenantEntity, String> {

	Optional<TenantEntity> findByIdAndMunicipalityId(final String id, final String municipalityId);
}
