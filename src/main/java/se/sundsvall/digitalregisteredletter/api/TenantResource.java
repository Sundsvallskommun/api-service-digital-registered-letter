package se.sundsvall.digitalregisteredletter.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.digitalregisteredletter.api.model.Tenant;
import se.sundsvall.digitalregisteredletter.service.TenantService;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@RestController
@Validated
@RequestMapping("/{municipalityId}/tenants")
@Tag(name = "Tenant Resource", description = "Manage tenants")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class TenantResource {

	private final TenantService tenantService;

	TenantResource(final TenantService tenantService) {
		this.tenantService = tenantService;
	}

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get all tenants", description = "Returns all tenants for a municipality", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true)
	})
	ResponseEntity<List<Tenant>> getTenants(
		@PathVariable @ValidMunicipalityId final String municipalityId) {

		return ok(tenantService.getTenants(municipalityId));
	}

	@GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get tenant", description = "Returns a specific tenant", responses = {
		@ApiResponse(responseCode = "200", description = "Successful operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Tenant> getTenant(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String id) {

		return ok(tenantService.getTenant(municipalityId, id));
	}

	@PostMapping(produces = ALL_VALUE, consumes = APPLICATION_JSON_VALUE)
	@Operation(summary = "Create tenant",
		description = "Creates a new tenant",
		responses = @ApiResponse(responseCode = "201", headers = @Header(name = "Location", schema = @Schema(type = "string")), description = "Successful operation - Created", useReturnTypeSchema = true))
	ResponseEntity<Void> createTenant(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@RequestBody @Valid final Tenant tenant) {

		final var id = tenantService.createTenant(municipalityId, tenant);

		return created(fromPath("/{municipalityId}/tenants/{id}")
			.buildAndExpand(municipalityId, id).toUri())
			.build();
	}

	@PutMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE)
	@Operation(summary = "Update tenant", description = "Updates an existing tenant", responses = {
		@ApiResponse(responseCode = "204", description = "Successful Operation - No Content"),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> updateTenant(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String id,
		@RequestBody @Valid final Tenant tenant) {

		tenantService.updateTenant(municipalityId, id, tenant);
		return noContent().build();
	}

	@DeleteMapping(value = "/{id}")
	@Operation(summary = "Delete tenant", description = "Deletes an existing tenant", responses = {
		@ApiResponse(responseCode = "204", description = "Successful Operation - No Content"),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Void> deleteTenant(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String id) {

		tenantService.deleteTenant(municipalityId, id);
		return noContent().build();
	}
}
