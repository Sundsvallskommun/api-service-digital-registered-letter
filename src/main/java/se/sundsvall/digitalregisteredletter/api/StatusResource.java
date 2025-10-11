package se.sundsvall.digitalregisteredletter.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.digitalregisteredletter.api.model.LetterStatus;
import se.sundsvall.digitalregisteredletter.api.model.LetterStatusRequest;
import se.sundsvall.digitalregisteredletter.service.LetterService;

@RestController
@Validated
@RequestMapping("/{municipalityId}/status")
@Tag(name = "Status Resource")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class StatusResource {

	private final LetterService letterService;

	StatusResource(final LetterService letterService) {
		this.letterService = letterService;
	}

	@PostMapping(path = "/letters", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get status of given letters", description = "Returns the current status for each provided letterId", responses = {
		@ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true)
	})
	ResponseEntity<List<LetterStatus>> getLetterStatuses(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@RequestBody @Valid final LetterStatusRequest request) {
		return ok(letterService.getLetterStatuses(municipalityId, request.letterIds()));
	}

}
