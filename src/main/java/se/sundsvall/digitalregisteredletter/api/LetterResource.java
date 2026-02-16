package se.sundsvall.digitalregisteredletter.api;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.api.validation.NoDuplicateFileNames;
import se.sundsvall.digitalregisteredletter.api.validation.ValidIdentifier;
import se.sundsvall.digitalregisteredletter.api.validation.ValidPdf;
import se.sundsvall.digitalregisteredletter.service.LetterService;

@RestController
@Validated
@RequestMapping("/{municipalityId}")
@Tag(name = "Letter Resource", description = "Send and manage digital registered letters")
@ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class LetterResource {

	private final LetterService letterService;

	LetterResource(final LetterService letterService) {
		this.letterService = letterService;
	}

	@GetMapping(value = "/letters", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get all letters",
		description = "Retrieves all letters for a municipality. Response is possible to filter by any combination of department id, username, earliest and latest created date.",
		responses = @ApiResponse(responseCode = "200", description = "Successful Operation - OK", useReturnTypeSchema = true))
	ResponseEntity<Letters> getLetters(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@ParameterObject final LetterFilter filter,
		@ParameterObject final Pageable pageable) {
		return ok(letterService.getLetters(municipalityId, filter, pageable));
	}

	@GetMapping(value = "/letters/{letterId}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get letter", description = "Retrieves a letter by id", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation - OK", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Letter> getLetter(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String letterId) {

		return ok(letterService.getLetter(municipalityId, letterId));
	}

	@GetMapping(value = "/letters/{letterId}/signinginfo", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get signing information", description = "Retrieves signing information connected to letter matching provided id", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation - OK", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<SigningInfo> getSigningInformation(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String letterId) {

		return ok(letterService.getSigningInformation(municipalityId, letterId));
	}

	@GetMapping(value = "/letters/{letterId}/attachments/{attachmentId}", produces = ALL_VALUE)
	@Operation(summary = "Downloads letter attachment content", description = "Retrieves attachment content by id", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	void downloadLetterAttachment(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String letterId,
		@PathVariable @ValidUuid final String attachmentId,
		final HttpServletResponse response) {

		letterService.readLetterAttachment(municipalityId, letterId, attachmentId, response);
	}

	@PostMapping(value = "/{organizationNumber}/letters", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Send letter",
		description = "Send a digital registered letter using Kivra",
		responses = {
			@ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful operation - Created", useReturnTypeSchema = true),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	ResponseEntity<Letter> sendLetter(
		@RequestHeader(value = Identifier.HEADER_NAME) @ValidIdentifier final String xSentBy,
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@NotBlank @PathVariable final String organizationNumber,
		@RequestPart(name = "letter") @Valid final LetterRequest request,
		@RequestPart(name = "letterAttachments") @NoDuplicateFileNames @ValidPdf final List<MultipartFile> attachments) {
		Identifier.set(Identifier.parse(xSentBy));

		final var letter = letterService.sendLetter(municipalityId, organizationNumber, request, attachments);

		return created(fromPath("/{municipalityId}/{organizationNumber}/letters/{letterId}")
			.buildAndExpand(municipalityId, organizationNumber, letter.id()).toUri())
			.body(letter);
	}

	@GetMapping(value = "/letters/{letterId}/receipt", produces = ALL_VALUE)
	@Operation(summary = "Read letter receipt with the complete letter", description = "Retrieves letter receipt combined with the letter", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	void readLetterReceipt(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@PathVariable @ValidUuid final String letterId,
		final HttpServletResponse response) {

		letterService.readLetterReceipt(municipalityId, letterId, response);
	}

}
