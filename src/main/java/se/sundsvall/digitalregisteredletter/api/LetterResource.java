package se.sundsvall.digitalregisteredletter.api;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.digitalregisteredletter.service.util.ParseUtil.parseLetterRequest;
import static se.sundsvall.digitalregisteredletter.service.util.ValidationUtil.validate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.digitalregisteredletter.api.model.AttachmentsBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.validation.ValidPdf;
import se.sundsvall.digitalregisteredletter.service.LetterService;

@RestController
@Validated
@RequestMapping("/{municipalityId}/letters")
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

	@GetMapping(produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get all letters",
		description = "Retrieves all letters for a municipality. Response is possible to filter by any combination of department id, username, earliest and latest created date.",
		responses = @ApiResponse(responseCode = "200", description = "Successful Operation - OK", useReturnTypeSchema = true))
	ResponseEntity<Letters> getLetters(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@ParameterObject final LetterFilter filter,
		@ParameterObject final Pageable pageable) {
		return ok(letterService.getLetters(municipalityId, pageable));
	}

	@GetMapping(value = "/{letterId}", produces = APPLICATION_JSON_VALUE)
	@Operation(summary = "Get letter", description = "Retrieves a letter by id", responses = {
		@ApiResponse(responseCode = "200", description = "Successful Operation - OK", useReturnTypeSchema = true),
		@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
	})
	ResponseEntity<Letter> getLetter(@PathVariable @ValidMunicipalityId final String municipalityId, @PathVariable final String letterId) {
		return ok(letterService.getLetter(municipalityId, letterId));
	}

	@PostMapping(produces = ALL_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Send letter",
		description = "Send a digital registered letter using Kivra",
		responses = @ApiResponse(responseCode = "201", headers = @Header(name = LOCATION, schema = @Schema(type = "string")), description = "Successful operation - Created", useReturnTypeSchema = true))
	ResponseEntity<Void> sendLetter(
		@PathVariable @ValidMunicipalityId final String municipalityId,
		@RequestPart(name = "letter") @Schema(description = "LetterRequest as a JSON string", implementation = LetterRequest.class) final String letterString,
		@RequestPart(name = "letterAttachments") @ValidPdf final List<MultipartFile> files) {

		// Parses the letter string to an actual LetterRequest object and validates it.
		final var letterRequest = parseLetterRequest(letterString);
		validate(letterRequest);

		// Places the multipart files into an Attachments object and validates it.
		final var attachments = AttachmentsBuilder.create()
			.withFiles(files)
			.build();
		validate(attachments);

		final var id = letterService.sendLetter(municipalityId, letterRequest, attachments);

		return created(fromPath("/{municipalityId}/letters/{letterId}")
			.buildAndExpand(municipalityId, id).toUri())
			.header(CONTENT_TYPE, ALL_VALUE)
			.build();
	}
}
