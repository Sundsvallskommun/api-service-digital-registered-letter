package se.sundsvall.digitalregisteredletter.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.digitalregisteredletter.api.model.KivraTenantRequestBuilder;
import se.sundsvall.digitalregisteredletter.api.model.TenantBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CONFLICT;

@ExtendWith(MockitoExtension.class)
class KivraTenantServiceTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String ORG_NUMBER = "5591628136";
	private static final String NAME = "Department 44";
	private static final String LEGAL_NAME = "Department 44 AB";
	private static final String TENANT_KEY = "some-tenant-key";
	private static final String ID = "cb20c51f-fcf3-42c0-b613-de563634a8ec";

	@Mock
	private KivraIntegration kivraIntegrationMock;

	@Mock
	private TenantService tenantServiceMock;

	@Mock
	private TenantRepository tenantRepositoryMock;

	@InjectMocks
	private KivraTenantService kivraTenantService;

	@AfterEach
	void noMoreInteractions() {
		verifyNoMoreInteractions(kivraIntegrationMock, tenantServiceMock, tenantRepositoryMock);
	}

	@Test
	void createKivraTenant() {
		final var request = KivraTenantRequestBuilder.create()
			.withName(NAME)
			.withLegalName(LEGAL_NAME)
			.withOrgNumber(ORG_NUMBER)
			.build();

		final var kivraTenant = TenantBuilder.create()
			.withOrgNumber(ORG_NUMBER)
			.withTenantKey(TENANT_KEY)
			.build();

		when(tenantRepositoryMock.existsByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(false);
		when(kivraIntegrationMock.createTenant(NAME, LEGAL_NAME, ORG_NUMBER)).thenReturn(TENANT_KEY);
		when(tenantServiceMock.createTenant(MUNICIPALITY_ID, kivraTenant)).thenReturn(ID);

		final var result = kivraTenantService.createKivraTenant(MUNICIPALITY_ID, request);

		assertThat(result).isEqualTo(ID);
		verify(tenantRepositoryMock).existsByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).createTenant(NAME, LEGAL_NAME, ORG_NUMBER);
		verify(tenantServiceMock).createTenant(MUNICIPALITY_ID, kivraTenant);
	}

	@Test
	void createKivraTenantAlreadyExists() {
		final var request = KivraTenantRequestBuilder.create()
			.withName(NAME)
			.withOrgNumber(ORG_NUMBER)
			.build();

		when(tenantRepositoryMock.existsByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(true);

		assertThatThrownBy(() -> kivraTenantService.createKivraTenant(MUNICIPALITY_ID, request))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", CONFLICT)
			.hasMessage("Conflict: Tenant with orgNumber '%s' already exists for municipalityId '%s'", ORG_NUMBER, MUNICIPALITY_ID);
		verify(tenantRepositoryMock).existsByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER);
	}

	@Test
	void createKivraTenantWhenKivraFails() {
		final var request = KivraTenantRequestBuilder.create()
			.withName(NAME)
			.withOrgNumber(ORG_NUMBER)
			.build();

		when(tenantRepositoryMock.existsByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(false);
		when(kivraIntegrationMock.createTenant(NAME, null, ORG_NUMBER)).thenThrow(Problem.valueOf(BAD_GATEWAY, "Could not create Kivra tenant"));

		assertThatThrownBy(() -> kivraTenantService.createKivraTenant(MUNICIPALITY_ID, request))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", BAD_GATEWAY);
		verify(tenantRepositoryMock).existsByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).createTenant(NAME, null, ORG_NUMBER);
	}

}
