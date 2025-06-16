package se.sundsvall.digitalregisteredletter.api;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.digitalregisteredletter.service.SchedulerService;

@RestController
@Validated
@RequestMapping("/{municipalityId}/scheduler")
@Tag(name = "Scheduler Resource", description = "Manually trigger scheduled tasks")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class SchedulerResource {

	private final SchedulerService schedulerService;

	SchedulerResource(final SchedulerService schedulerService) {
		this.schedulerService = schedulerService;
	}

	// TODO: Implement endpoints for manually triggering scheduled tasks

}
