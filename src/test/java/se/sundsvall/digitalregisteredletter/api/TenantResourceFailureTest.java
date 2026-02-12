package se.sundsvall.digitalregisteredletter.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.api.model.TenantBuilder;
import se.sundsvall.digitalregisteredletter.service.TenantService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class TenantResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String INVALID_MUNICIPALITY_ID = "bad-municipality-id";
	private static final String INVALID_UUID = "not-a-valid-uuid";

	@MockitoBean
	private TenantService tenantServiceMock;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void noMoreInteractions() {
		verifyNoInteractions(tenantServiceMock);
	}

	@Test
	void getTenantsBadMunicipalityId() {
		final var response = webTestClient.get()
			.uri("/%s/tenants".formatted(INVALID_MUNICIPALITY_ID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getTenants.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void getTenantBadMunicipalityId() {
		final var id = "11111111-1111-1111-1111-111111111111";

		final var response = webTestClient.get()
			.uri("/%s/tenants/%s".formatted(INVALID_MUNICIPALITY_ID, id))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getTenant.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void getTenantBadId() {
		final var response = webTestClient.get()
			.uri("/%s/tenants/%s".formatted(MUNICIPALITY_ID, INVALID_UUID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getTenant.id", "not a valid UUID"));
	}

	@Test
	void createTenantBadMunicipalityId() {
		final var tenant = TenantBuilder.create()
			.withOrgNumber("5591628136")
			.withTenantKey("some-tenant-key")
			.build();

		final var response = webTestClient.post()
			.uri("/%s/tenants".formatted(INVALID_MUNICIPALITY_ID))
			.contentType(APPLICATION_JSON)
			.bodyValue(tenant)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("createTenant.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void createTenantEmptyBody() {
		final var tenant = TenantBuilder.create().build();

		final var response = webTestClient.post()
			.uri("/%s/tenants".formatted(MUNICIPALITY_ID))
			.contentType(APPLICATION_JSON)
			.bodyValue(tenant)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(
				tuple("orgNumber", "must not be blank"),
				tuple("tenantKey", "must not be blank"));
	}

	@Test
	void updateTenantBadMunicipalityId() {
		final var id = "11111111-1111-1111-1111-111111111111";
		final var tenant = TenantBuilder.create()
			.withOrgNumber("5591628136")
			.withTenantKey("some-tenant-key")
			.build();

		final var response = webTestClient.put()
			.uri("/%s/tenants/%s".formatted(INVALID_MUNICIPALITY_ID, id))
			.contentType(APPLICATION_JSON)
			.bodyValue(tenant)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("updateTenant.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void updateTenantBadId() {
		final var tenant = TenantBuilder.create()
			.withOrgNumber("5591628136")
			.withTenantKey("some-tenant-key")
			.build();

		final var response = webTestClient.put()
			.uri("/%s/tenants/%s".formatted(MUNICIPALITY_ID, INVALID_UUID))
			.contentType(APPLICATION_JSON)
			.bodyValue(tenant)
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("updateTenant.id", "not a valid UUID"));
	}

	@Test
	void deleteTenantBadMunicipalityId() {
		final var id = "11111111-1111-1111-1111-111111111111";

		final var response = webTestClient.delete()
			.uri("/%s/tenants/%s".formatted(INVALID_MUNICIPALITY_ID, id))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("deleteTenant.municipalityId", "not a valid municipality ID"));
	}

	@Test
	void deleteTenantBadId() {
		final var response = webTestClient.delete()
			.uri("/%s/tenants/%s".formatted(MUNICIPALITY_ID, INVALID_UUID))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("deleteTenant.id", "not a valid UUID"));
	}
}
