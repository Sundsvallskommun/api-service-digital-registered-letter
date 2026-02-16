package se.sundsvall.digitalregisteredletter.api;

import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.digitalregisteredletter.Application;
import se.sundsvall.digitalregisteredletter.service.scheduler.CertificateHealthScheduler;
import se.sundsvall.digitalregisteredletter.service.scheduler.SchedulerWorker;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SchedulerResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private SchedulerWorker schedulerWorker;

	@MockitoBean
	private CertificateHealthScheduler certificateHealthScheduler;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void updateLetterStatuses_OK() {
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/%s/scheduler".formatted(MUNICIPALITY_ID)).build())
			.exchange()
			.expectStatus().isOk();

		verify(schedulerWorker).updateLetterInformation();
	}

	@Test
	void checkCertificateHealth_OK() {
		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.path("/%s/scheduler/certificate-health".formatted(MUNICIPALITY_ID)).build())
			.exchange()
			.expectStatus().isOk();

		verify(certificateHealthScheduler).execute();
	}

}
