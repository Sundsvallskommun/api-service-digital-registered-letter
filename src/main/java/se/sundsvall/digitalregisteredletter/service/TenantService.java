package se.sundsvall.digitalregisteredletter.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.api.model.Tenant;
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;
import se.sundsvall.digitalregisteredletter.service.util.EncryptionUtility;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.digitalregisteredletter.service.mapper.TenantMapper.toTenant;
import static se.sundsvall.digitalregisteredletter.service.mapper.TenantMapper.toTenantEntity;
import static se.sundsvall.digitalregisteredletter.service.mapper.TenantMapper.toTenantList;
import static se.sundsvall.digitalregisteredletter.service.mapper.TenantMapper.updateEntity;

@Service
public class TenantService {

	private static final String TENANT_NOT_FOUND = "Tenant with id '%s' and municipalityId '%s' not found";
	private static final String TENANT_NOT_FOUND_BY_ORG = "Tenant with municipalityId '%s' and organizationNumber '%s' not found";

	private final TenantRepository tenantRepository;
	private final EncryptionUtility encryptionUtility;

	public TenantService(final TenantRepository tenantRepository, final EncryptionUtility encryptionUtility) {
		this.tenantRepository = tenantRepository;
		this.encryptionUtility = encryptionUtility;
	}

	public Tenant getTenant(final String municipalityId, final String id) {
		return toTenant(getTenantEntity(municipalityId, id));
	}

	public List<Tenant> getTenants(final String municipalityId) {
		return toTenantList(tenantRepository.findAllByMunicipalityId(municipalityId));
	}

	public String createTenant(final String municipalityId, final Tenant tenant) {
		final var entity = toTenantEntity(municipalityId, tenant);
		entity.setTenantKey(encryptionUtility.encrypt(tenant.tenantKey().getBytes()));
		final var savedEntity = tenantRepository.save(entity);
		return savedEntity.getId();
	}

	public void updateTenant(final String municipalityId, final String id, final Tenant tenant) {
		final var entity = getTenantEntity(municipalityId, id);
		updateEntity(entity, tenant);
		entity.setTenantKey(encryptionUtility.encrypt(tenant.tenantKey().getBytes()));
		tenantRepository.save(entity);
	}

	public void deleteTenant(final String municipalityId, final String id) {
		final var entity = getTenantEntity(municipalityId, id);
		tenantRepository.delete(entity);
	}

	public String getDecryptedTenantKey(final String municipalityId, final String organizationNumber) {
		final var entity = tenantRepository.findByMunicipalityIdAndOrgNumber(municipalityId, organizationNumber)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, TENANT_NOT_FOUND_BY_ORG.formatted(municipalityId, organizationNumber)));
		return encryptionUtility.decrypt(entity.getTenantKey());
	}

	private TenantEntity getTenantEntity(final String municipalityId, final String id) {
		return tenantRepository.findByIdAndMunicipalityId(id, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, TENANT_NOT_FOUND.formatted(id, municipalityId)));
	}
}
