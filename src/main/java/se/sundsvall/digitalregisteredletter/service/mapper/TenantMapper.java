package se.sundsvall.digitalregisteredletter.service.mapper;

import java.util.List;
import se.sundsvall.digitalregisteredletter.api.model.Tenant;
import se.sundsvall.digitalregisteredletter.api.model.TenantBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public final class TenantMapper {

	private TenantMapper() {
		// Prevent instantiation
	}

	public static Tenant toTenant(final TenantEntity entity) {
		return ofNullable(entity)
			.map(tenantEntity -> TenantBuilder.create()
				.withId(tenantEntity.getId())
				.withOrgNumber(tenantEntity.getOrgNumber())
				.build())
			.orElse(null);
	}

	public static List<Tenant> toTenantList(final List<TenantEntity> entities) {
		return ofNullable(entities)
			.map(list -> list.stream().map(TenantMapper::toTenant).toList())
			.orElse(emptyList());
	}

	public static TenantEntity toTenantEntity(final String municipalityId, final Tenant tenant) {
		return ofNullable(tenant)
			.map(mappedTenant -> TenantEntity.create()
				.withOrgNumber(mappedTenant.orgNumber())
				.withTenantKey(mappedTenant.tenantKey())
				.withMunicipalityId(municipalityId))
			.orElse(null);
	}

	public static TenantEntity updateEntity(final TenantEntity entity, final Tenant tenant) {
		if (entity == null || tenant == null) {
			return entity;
		}

		ofNullable(tenant.orgNumber()).ifPresent(entity::setOrgNumber);

		return entity;
	}
}
