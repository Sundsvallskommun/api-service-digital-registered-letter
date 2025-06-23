package se.sundsvall.digitalregisteredletter.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class ValidPdfConstraintValidator implements ConstraintValidator<ValidPdf, List<MultipartFile>> {

	@Override
	public boolean isValid(final List<MultipartFile> files, final ConstraintValidatorContext context) {
		return files.stream()
			.map(MultipartFile::getContentType)
			.allMatch("application/pdf"::equals);
	}
}
