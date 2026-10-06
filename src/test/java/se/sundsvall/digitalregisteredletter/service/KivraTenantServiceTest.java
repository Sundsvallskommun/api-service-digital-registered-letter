package se.sundsvall.digitalregisteredletter.service;

import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;
import se.sundsvall.digitalregisteredletter.api.model.KivraTenantRequestBuilder;
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.TenantEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;

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
	void createKivraTenantAlreadyExists() {
		final var request = KivraTenantRequestBuilder.create()
			.withName(NAME)
			.withOrgNumber(ORG_NUMBER)
			.build();
		final var existingEntity = TenantEntity.create()
			.withId(ID)
			.withOrgNumber(ORG_NUMBER)
			.withMunicipalityId(MUNICIPALITY_ID);

		when(tenantRepositoryMock.findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(Optional.of(existingEntity));

		assertThatThrownBy(() -> kivraTenantService.createKivraTenant(MUNICIPALITY_ID, request))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", CONFLICT)
			.hasMessage("Conflict: Tenant with orgNumber '%s' already exists for municipalityId '%s'", ORG_NUMBER, MUNICIPALITY_ID);
		verify(tenantRepositoryMock).findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER);
	}

	@Test
	void createKivraTenantWhenKivraFails() {
		final var request = KivraTenantRequestBuilder.create()
			.withName(NAME)
			.withOrgNumber(ORG_NUMBER)
			.build();

		when(tenantRepositoryMock.findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER)).thenReturn(Optional.empty());
		when(kivraIntegrationMock.createTenant(NAME, null, ORG_NUMBER)).thenThrow(Problem.valueOf(BAD_GATEWAY, "Could not create Kivra tenant"));

		assertThatThrownBy(() -> kivraTenantService.createKivraTenant(MUNICIPALITY_ID, request))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", BAD_GATEWAY);
		verify(tenantRepositoryMock).findByMunicipalityIdAndOrgNumber(MUNICIPALITY_ID, ORG_NUMBER);
		verify(kivraIntegrationMock).createTenant(NAME, null, ORG_NUMBER);
	}

}
