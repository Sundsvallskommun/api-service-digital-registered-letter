package se.sundsvall.digitalregisteredletter.service.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

public record AttachmentData(String filename, String contentType, InputStream inputStream) {

	public static AttachmentData from(final MultipartFile file) {
		try {
			return new AttachmentData(
				file.getOriginalFilename(),
				file.getContentType(),
				new ByteArrayInputStream(file.getBytes()));
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Could not read multipart file '%s'".formatted(file.getOriginalFilename()));
		}
	}

	public static AttachmentData from(final ResponseEntity<Resource> response) throws IOException {
		var contentType = Optional.ofNullable(response.getHeaders().getContentType())
			.map(MediaType::toString)
			.orElse(APPLICATION_OCTET_STREAM_VALUE);

		var filename = Optional.ofNullable(response.getHeaders().getContentDisposition().getFilename())
			.orElse("attachment");

		return new AttachmentData(
			filename,
			contentType,
			response.getBody().getInputStream());
	}

}
