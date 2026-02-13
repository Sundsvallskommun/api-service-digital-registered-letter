package se.sundsvall.digitalregisteredletter.apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.integration.db.TenantRepository;

@WireMockAppTestSuite(files = "classpath:/TenantIT/", classes = Application.class)
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/testdata-it.sql"
})
class TenantIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String TENANT_ID = "b7c8d9e0-f1a2-3b4c-5d6e-7f8a9b0c1d2e";
	private static final String TENANT_ID_2 = "e5a41e35-2b1c-4e6d-8f3a-7c9d0e1f2a3b";
	private static final String RESPONSE = "response.json";
	private static final String REQUEST = "request.json";

	@Autowired
	private TenantRepository tenantRepository;

	@Test
	void test01_getTenants() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/tenants")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test02_getTenant() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/tenants/" + TENANT_ID)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test03_createTenant() {
		final var initialCount = tenantRepository.count();

		final var headers = setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/tenants")
			.withHttpMethod(POST)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("/" + MUNICIPALITY_ID + "/tenants/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse()
			.getResponseHeaders();

		assertThat(headers.get(LOCATION)).isNotNull();
		assertThat(tenantRepository.count()).isEqualTo(initialCount + 1);
	}

	@Test
	void test04_updateTenant() {
		assertThat(tenantRepository.findByIdAndMunicipalityId(TENANT_ID, MUNICIPALITY_ID))
			.isPresent()
			.hasValueSatisfying(entity -> assertThat(entity.getOrgNumber()).isEqualTo("1234567890"));

		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/tenants/" + TENANT_ID)
			.withHttpMethod(PUT)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(tenantRepository.findByIdAndMunicipalityId(TENANT_ID, MUNICIPALITY_ID))
			.isPresent()
			.hasValueSatisfying(entity -> assertThat(entity.getOrgNumber()).isEqualTo("9876543210"));
	}

	@Test
	void test05_deleteTenant() {
		assertThat(tenantRepository.findByIdAndMunicipalityId(TENANT_ID_2, MUNICIPALITY_ID)).isPresent();

		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/tenants/" + TENANT_ID_2)
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NO_CONTENT)
			.withExpectedResponseBodyIsNull()
			.sendRequestAndVerifyResponse();

		assertThat(tenantRepository.findByIdAndMunicipalityId(TENANT_ID_2, MUNICIPALITY_ID)).isEmpty();
	}

	@Test
	void test06_getTenantNotFound() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/tenants/00000000-0000-0000-0000-000000000000")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test07_updateTenantNotFound() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/tenants/00000000-0000-0000-0000-000000000000")
			.withHttpMethod(PUT)
			.withRequest(REQUEST)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test08_deleteTenantNotFound() {
		setupCall()
			.withServicePath("/" + MUNICIPALITY_ID + "/tenants/00000000-0000-0000-0000-000000000000")
			.withHttpMethod(DELETE)
			.withExpectedResponseStatus(NOT_FOUND)
			.withExpectedResponse(RESPONSE)
			.sendRequestAndVerifyResponse();
	}
}
