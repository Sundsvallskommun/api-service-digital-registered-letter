package se.sundsvall.digitalregisteredletter.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.digitalregisteredletter.service.scheduler.CertificateHealthScheduler;
import se.sundsvall.digitalregisteredletter.service.scheduler.SchedulerWorker;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@Validated
@RequestMapping("/{municipalityId}/scheduler")
@Tag(name = "Scheduler Resource", description = "Manually trigger scheduled tasks")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class SchedulerResource {

	private final SchedulerWorker schedulerWorker;
	private final CertificateHealthScheduler certificateHealthScheduler;

	SchedulerResource(final SchedulerWorker schedulerWorker, final CertificateHealthScheduler certificateHealthScheduler) {
		this.schedulerWorker = schedulerWorker;
		this.certificateHealthScheduler = certificateHealthScheduler;
	}

	@PostMapping(produces = ALL_VALUE)
	@Operation(summary = "Trigger update letter information task", description = "Triggers the task that updates letter information", responses = @ApiResponse(responseCode = "200", description = "Successful Operation - OK", useReturnTypeSchema = true))
	ResponseEntity<Void> updateLetterInformation(
		@PathVariable @ValidMunicipalityId final String municipalityId) {

		schedulerWorker.updateLetterInformation();
		return ok().build();
	}

	@PostMapping(value = "/certificate-health", produces = ALL_VALUE)
	@Operation(summary = "Trigger certificate health check", description = "Triggers the task that checks the Kivra certificate health", responses = @ApiResponse(responseCode = "200", description = "Successful Operation - OK", useReturnTypeSchema = true))
	ResponseEntity<Void> checkCertificateHealth(
		@PathVariable @ValidMunicipalityId final String municipalityId) {

		certificateHealthScheduler.execute();
		return ok().build();
	}

}
