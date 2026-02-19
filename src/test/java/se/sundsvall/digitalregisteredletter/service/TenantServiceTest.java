package se.sundsvall.digitalregisteredletter.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.digitalregisteredletter.api.model.TenantBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;
import se.sundsvall.digitalregisteredletter.service.util.EncryptionUtility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ID = UUID.randomUUID().toString();
	private static final String ORG_NUMBER = "5591628136";
	private static final String TENANT_KEY = "some-tenant-key";
	private static final String ENCRYPTED_TENANT_KEY = "encrypted-value";

	@Mock
	private TenantRepository tenantRepositoryMock;

	@Mock
	private EncryptionUtility encryptionUtilityMock;

	@InjectMocks
	private TenantService tenantService;

	@AfterEach
	void noMoreInteractions() {
		verifyNoMoreInteractions(tenantRepositoryMock, encryptionUtilityMock);
	}

	@Test
	void getTenant() {
		// Setup
		final var entity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(ENCRYPTED_TENANT_KEY)
			.withMunicipalityId(MUNICIPALITY_ID);

		// Mock
		when(tenantRepositoryMock.findByIdAndMunicipalityId(ID, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		// Act
		final var result = tenantService.getTenant(MUNICIPALITY_ID, ID);

		// Verify
		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(ID);
		assertThat(result.orgNumber()).isEqualTo(ORG_NUMBER);
		assertThat(result.tenantKey()).isNull();
		verify(tenantRepositoryMock).findByIdAndMunicipalityId(ID, MUNICIPALITY_ID);
	}

	@Test
	void getTenantNotFound() {
		// Mock
		when(tenantRepositoryMock.findByIdAndMunicipalityId(ID, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		// Act & Verify
		assertThatThrownBy(() -> tenantService.getTenant(MUNICIPALITY_ID, ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND);
		verify(tenantRepositoryMock).findByIdAndMunicipalityId(ID, MUNICIPALITY_ID);
	}

	@Test
	void getTenants() {
		// Setup
		final var entity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(ENCRYPTED_TENANT_KEY)
			.withMunicipalityId(MUNICIPALITY_ID);

		// Mock
		when(tenantRepositoryMock.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of(entity));

		// Act
		final var result = tenantService.getTenants(MUNICIPALITY_ID);

		// Verify
		assertThat(result).hasSize(1);
		assertThat(result.getFirst().id()).isEqualTo(ID);
		assertThat(result.getFirst().orgNumber()).isEqualTo(ORG_NUMBER);
		verify(tenantRepositoryMock).findAllByMunicipalityId(MUNICIPALITY_ID);
	}

	@Test
	void createTenant() {
		// Setup
		final var tenant = TenantBuilder.create()
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(TENANT_KEY)
			.build();
		final var savedEntity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(ENCRYPTED_TENANT_KEY)
			.withMunicipalityId(MUNICIPALITY_ID);

		// Mock
		when(encryptionUtilityMock.encrypt(TENANT_KEY.getBytes())).thenReturn(ENCRYPTED_TENANT_KEY);
		when(tenantRepositoryMock.save(any(TenantEntity.class))).thenReturn(savedEntity);

		// Act
		final var result = tenantService.createTenant(MUNICIPALITY_ID, tenant);

		// Verify
		assertThat(result).isEqualTo(ID);
		verify(encryptionUtilityMock).encrypt(TENANT_KEY.getBytes());
		verify(tenantRepositoryMock).save(any(TenantEntity.class));
	}

	@Test
	void updateTenant() {
		// Setup
		final var existingEntity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber("old-org")
			.withTenantKey("old-encrypted-key")
			.withMunicipalityId(MUNICIPALITY_ID);
		final var tenant = TenantBuilder.create()
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(TENANT_KEY)
			.build();

		// Mock
		when(tenantRepositoryMock.findByIdAndMunicipalityId(ID, MUNICIPALITY_ID)).thenReturn(Optional.of(existingEntity));
		when(encryptionUtilityMock.encrypt(TENANT_KEY.getBytes())).thenReturn(ENCRYPTED_TENANT_KEY);
		when(tenantRepositoryMock.save(any(TenantEntity.class))).thenReturn(existingEntity);

		// Act
		tenantService.updateTenant(MUNICIPALITY_ID, ID, tenant);

		// Verify
		verify(tenantRepositoryMock).findByIdAndMunicipalityId(ID, MUNICIPALITY_ID);
		verify(encryptionUtilityMock).encrypt(TENANT_KEY.getBytes());
		verify(tenantRepositoryMock).save(any(TenantEntity.class));
	}

	@Test
	void updateTenantNotFound() {
		// Setup
		final var tenant = TenantBuilder.create()
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(TENANT_KEY)
			.build();

		// Mock
		when(tenantRepositoryMock.findByIdAndMunicipalityId(ID, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		// Act & Verify
		assertThatThrownBy(() -> tenantService.updateTenant(MUNICIPALITY_ID, ID, tenant))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND);
		verify(tenantRepositoryMock).findByIdAndMunicipalityId(ID, MUNICIPALITY_ID);
	}

	@Test
	void deleteTenant() {
		// Setup
		final var entity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(ENCRYPTED_TENANT_KEY)
			.withMunicipalityId(MUNICIPALITY_ID);

		// Mock
		when(tenantRepositoryMock.findByIdAndMunicipalityId(ID, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		// Act
		tenantService.deleteTenant(MUNICIPALITY_ID, ID);

		// Verify
		verify(tenantRepositoryMock).findByIdAndMunicipalityId(ID, MUNICIPALITY_ID);
		verify(tenantRepositoryMock).delete(entity);
	}

	@Test
	void deleteTenantNotFound() {
		// Mock
		when(tenantRepositoryMock.findByIdAndMunicipalityId(ID, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		// Act & Verify
		assertThatThrownBy(() -> tenantService.deleteTenant(MUNICIPALITY_ID, ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND);
		verify(tenantRepositoryMock).findByIdAndMunicipalityId(ID, MUNICIPALITY_ID);
	}

	@Test
	void getDecryptedTenantKey() {
		// Setup
		final var entity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(ENCRYPTED_TENANT_KEY)
			.withMunicipalityId(MUNICIPALITY_ID);

		// Mock
		when(tenantRepositoryMock.findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(Optional.of(entity));
		when(encryptionUtilityMock.decrypt(ENCRYPTED_TENANT_KEY)).thenReturn(TENANT_KEY);

		// Act
		final var result = tenantService.getDecryptedTenantKey(MUNICIPALITY_ID, ORG_NUMBER);

		// Verify
		assertThat(result).isEqualTo(TENANT_KEY);
		verify(tenantRepositoryMock).findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER);
		verify(encryptionUtilityMock).decrypt(ENCRYPTED_TENANT_KEY);
	}

	@Test
	void getDecryptedTenantKeyNotFound() {
		// Mock
		when(tenantRepositoryMock.findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(Optional.empty());

		// Act & Verify
		assertThatThrownBy(() -> tenantService.getDecryptedTenantKey(MUNICIPALITY_ID, ORG_NUMBER))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND);
		verify(tenantRepositoryMock).findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER);
	}
}
