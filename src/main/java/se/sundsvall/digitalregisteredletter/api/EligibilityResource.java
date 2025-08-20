package se.sundsvall.digitalregisteredletter.api;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.digitalregisteredletter.api.model.EligibilityRequest;
import se.sundsvall.digitalregisteredletter.service.EligibilityService;

@RestController
@Validated
@RequestMapping("/{municipalityId}/eligibility")
@Tag(name = "Eligibility Resource", description = "Check eligibility for sending digital registered letters")
@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class EligibilityResource {

	private final EligibilityService eligibilityService;

	EligibilityResource(final EligibilityService eligibilityService) {
		this.eligibilityService = eligibilityService;
	}

	@GetMapping("/kivra")
	@Operation(summary = "Check if the given partyIds are eligible for receiving digital registered letters with Kivra", description = "Returns a list of party IDs that are eligible for Kivra based on the provided municipality ID and party IDs")
	ResponseEntity<List<String>> checkKivraEligibility(@ValidMunicipalityId @PathVariable final String municipalityId, final EligibilityRequest request) {
		return ok(eligibilityService.checkEligibility(municipalityId, request));
	}

}
