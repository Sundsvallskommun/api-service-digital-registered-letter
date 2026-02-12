package se.sundsvall.digitalregisteredletter.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;

@CircuitBreaker(name = "tenantRepository")
public interface TenantRepository extends JpaRepository<TenantEntity, String> {

	Optional<TenantEntity> findByIdAndMunicipalityId(final String id, final String municipalityId);

	Optional<TenantEntity> findByMunicipalityIdAndOrgNumber(final String municipalityId, final String orgNumber);

	List<TenantEntity> findAllByMunicipalityId(final String municipalityId);
}
