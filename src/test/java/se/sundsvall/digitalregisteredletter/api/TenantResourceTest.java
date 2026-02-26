package se.sundsvall.digitalregisteredletter.api;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.Tenant;
import se.sundsvall.digitalregisteredletter.api.model.TenantBuilder;
import se.sundsvall.digitalregisteredletter.service.TenantService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class TenantResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private TenantService tenantServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void verifyNoMoreInteractions() {
		Mockito.verifyNoMoreInteractions(tenantServiceMock);
	}

	@Test
	void getTenants() {
		final var tenant = TenantBuilder.create()
			.withId(UUID.randomUUID().toString())
			.withOrgNumber("5591628136")
			.build();

		when(tenantServiceMock.getTenants(MUNICIPALITY_ID)).thenReturn(List.of(tenant));

		webTestClient.get()
			.uri("/%s/tenants".formatted(MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Tenant.class).hasSize(1);

		verify(tenantServiceMock).getTenants(MUNICIPALITY_ID);
	}

	@Test
	void getTenant() {
		final var id = UUID.randomUUID().toString();
		final var tenant = TenantBuilder.create()
			.withId(id)
			.withOrgNumber("5591628136")
			.build();

		when(tenantServiceMock.getTenant(MUNICIPALITY_ID, id)).thenReturn(tenant);

		webTestClient.get()
			.uri("/%s/tenants/%s".formatted(MUNICIPALITY_ID, id))
			.exchange()
			.expectStatus().isOk()
			.expectBody(Tenant.class);

		verify(tenantServiceMock).getTenant(MUNICIPALITY_ID, id);
	}

	@Test
	void createTenant() {
		final var id = UUID.randomUUID().toString();
		final var tenant = TenantBuilder.create()
			.withOrgNumber("5591628136")
			.withTenantKey("some-tenant-key")
			.build();

		when(tenantServiceMock.createTenant(eq(MUNICIPALITY_ID), any(Tenant.class))).thenReturn(id);

		webTestClient.post()
			.uri("/%s/tenants".formatted(MUNICIPALITY_ID))
			.contentType(APPLICATION_JSON)
			.bodyValue(tenant)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueEquals("Location", "/%s/tenants/%s".formatted(MUNICIPALITY_ID, id))
			.expectBody().isEmpty();

		verify(tenantServiceMock).createTenant(eq(MUNICIPALITY_ID), any(Tenant.class));
	}

	@Test
	void updateTenant() {
		final var id = UUID.randomUUID().toString();
		final var tenant = TenantBuilder.create()
			.withOrgNumber("5591628136")
			.withTenantKey("updated-key")
			.build();

		webTestClient.put()
			.uri("/%s/tenants/%s".formatted(MUNICIPALITY_ID, id))
			.contentType(APPLICATION_JSON)
			.bodyValue(tenant)
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();

		verify(tenantServiceMock).updateTenant(eq(MUNICIPALITY_ID), eq(id), any(Tenant.class));
	}

	@Test
	void deleteTenant() {
		final var id = UUID.randomUUID().toString();

		webTestClient.delete()
			.uri("/%s/tenants/%s".formatted(MUNICIPALITY_ID, id))
			.exchange()
			.expectStatus().isNoContent();

		verify(tenantServiceMock).deleteTenant(MUNICIPALITY_ID, id);
	}
}
