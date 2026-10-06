package se.sundsvall.digitalregisteredletter.service;

import org.springframework.stereotype.Service;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.digitalregisteredletter.api.model.KivraTenantRequest;
import se.sundsvall.digitalregisteredletter.api.model.TenantBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
public class KivraTenantService {
	private final KivraIntegration kivraIntegration;
	private final TenantService tenantService;
	private final TenantRepository tenantRepository;

	public KivraTenantService(final KivraIntegration kivraIntegration, final TenantService tenantService, final TenantRepository tenantRepository) {
		this.kivraIntegration = kivraIntegration;
		this.tenantService = tenantService;
		this.tenantRepository = tenantRepository;
	}

	public String createKivraTenant(final String municipalityId, final KivraTenantRequest kivraTenantRequest) {
		final var orgNumber = kivraTenantRequest.orgNumber();
		if (tenantRepository.existsByMunicipalityIdAndOrgNumber(municipalityId, orgNumber)) {
			throw Problem.valueOf(CONFLICT, "Tenant with orgNumber '%s' already exists for municipalityId '%s'".formatted(orgNumber, municipalityId));
		}
		final var tenantKey = kivraIntegration.createTenant(kivraTenantRequest.name(), kivraTenantRequest.legalName(), orgNumber);

		final var tenant = TenantBuilder.create()
			.withOrgNumber(orgNumber)
			.withTenantKey(tenantKey)
			.build();

		return tenantService.createTenant(municipalityId, tenant);
	}
}
