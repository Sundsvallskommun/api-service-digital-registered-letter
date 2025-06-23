package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.digitalregisteredletter.api.validation.NoDuplicateFileNames;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
public record Attachments(

	@NoDuplicateFileNames @Schema(description = "List of files") List<MultipartFile> files) {
}
