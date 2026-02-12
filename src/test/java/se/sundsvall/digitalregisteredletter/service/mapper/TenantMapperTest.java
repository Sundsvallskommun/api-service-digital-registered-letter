package se.sundsvall.digitalregisteredletter.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.digitalregisteredletter.service.mapper.TenantMapper.toTenant;
import static se.sundsvall.digitalregisteredletter.service.mapper.TenantMapper.toTenantEntity;
import static se.sundsvall.digitalregisteredletter.service.mapper.TenantMapper.toTenantList;
import static se.sundsvall.digitalregisteredletter.service.mapper.TenantMapper.updateEntity;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.digitalregisteredletter.api.model.Tenant;
import se.sundsvall.digitalregisteredletter.api.model.TenantBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;

class TenantMapperTest {

	private static final String ID = "cb20c51f-fcf3-42c0-b613-de563634a8ec";
	private static final String ORG_NUMBER = "5591628136";
	private static final String TENANT_KEY = "some-tenant-key";
	private static final String MUNICIPALITY_ID = "2281";

	@Test
	void toTenantFromEntity() {
		final var entity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(TENANT_KEY)
			.withMunicipalityId(MUNICIPALITY_ID);

		final var result = toTenant(entity);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(ID);
		assertThat(result.orgNumber()).isEqualTo(ORG_NUMBER);
		assertThat(result.tenantKey()).isNull();
	}

	@Test
	void toTenantFromNull() {
		assertThat(toTenant(null)).isNull();
	}

	@Test
	void toTenantListFromEntities() {
		final var entities = List.of(
			TenantEntity.create().withId(ID).withOrgNumber(ORG_NUMBER).withTenantKey(TENANT_KEY).withMunicipalityId(MUNICIPALITY_ID));

		final var result = toTenantList(entities);

		assertThat(result).hasSize(1);
		assertThat(result.getFirst().id()).isEqualTo(ID);
		assertThat(result.getFirst().orgNumber()).isEqualTo(ORG_NUMBER);
		assertThat(result.getFirst().tenantKey()).isNull();
	}

	@Test
	void toTenantListFromNull() {
		assertThat(toTenantList(null)).isEmpty();
	}

	@Test
	void toTenantEntityFromModel() {
		final var tenant = TenantBuilder.create()
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(TENANT_KEY)
			.build();

		final var result = toTenantEntity(MUNICIPALITY_ID, tenant);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isNull();
		assertThat(result.getOrgNumber()).isEqualTo(ORG_NUMBER);
		assertThat(result.getTenantKey()).isEqualTo(TENANT_KEY);
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	@Test
	void toTenantEntityFromNull() {
		assertThat(toTenantEntity(MUNICIPALITY_ID, null)).isNull();
	}

	@Test
	void updateEntityFromModel() {
		final var entity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber("old-org")
			.withTenantKey("old-key")
			.withMunicipalityId(MUNICIPALITY_ID);

		final var tenant = TenantBuilder.create()
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(TENANT_KEY)
			.build();

		final var result = updateEntity(entity, tenant);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(ID);
		assertThat(result.getOrgNumber()).isEqualTo(ORG_NUMBER);
		assertThat(result.getTenantKey()).isEqualTo("old-key");
		assertThat(result.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	@Test
	void updateEntityWithNullEntity() {
		final var tenant = TenantBuilder.create()
			.withOrgNumber(ORG_NUMBER)
			.build();

		assertThat(updateEntity(null, tenant)).isNull();
	}

	@Test
	void updateEntityWithNullModel() {
		final var entity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER);

		final var result = updateEntity(entity, null);

		assertThat(result).isNotNull();
		assertThat(result.getOrgNumber()).isEqualTo(ORG_NUMBER);
	}

	@Test
	void updateEntityWithPartialModel() {
		final var entity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber("old-org")
			.withTenantKey("old-key")
			.withMunicipalityId(MUNICIPALITY_ID);

		final var tenant = new Tenant(null, ORG_NUMBER, null);

		final var result = updateEntity(entity, tenant);

		assertThat(result).isNotNull();
		assertThat(result.getOrgNumber()).isEqualTo(ORG_NUMBER);
		assertThat(result.getTenantKey()).isEqualTo("old-key");
	}
}
