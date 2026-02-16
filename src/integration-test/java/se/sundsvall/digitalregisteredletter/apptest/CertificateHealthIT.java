package se.sundsvall.digitalregisteredletter.apptest;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.digitalregisteredletter.Application;

@WireMockAppTestSuite(files = "classpath:/CertificateHealthIT/", classes = Application.class)
class CertificateHealthIT extends AbstractAppTest {

	@Test
	void test01_healthCheckSuccessful() {
		setupCall()
			.withServicePath("/2281/scheduler/certificate-health")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}
}
