package se.sundsvall.digitalregisteredletter.service.util;

import static org.zalando.fauxpas.FauxPas.throwingFunction;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import jakarta.persistence.EntityManager;
import java.sql.Blob;
import java.util.Optional;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;

@Component
public class BlobUtil {

	private static final Logger LOG = LoggerFactory.getLogger(BlobUtil.class);
	private final EntityManager entityManager;

	public BlobUtil(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Blob convertToBlob(final MultipartFile multipartFile) {
		return Optional.ofNullable(multipartFile)
			.map(throwingFunction(this::createBlob))
			.orElse(null);
	}

	Session getSession() {
		return entityManager.unwrap(Session.class);
	}

	Blob createBlob(final MultipartFile multipartFile) {
		try {
			return getSession().getLobHelper().createBlob(multipartFile.getInputStream(), multipartFile.getSize());
		} catch (Exception e) {
			LOG.warn("Failed to create Blob from MultipartFile: {}", e.getMessage(), e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Could not convert file with name [ %s ] to database object".formatted(multipartFile.getOriginalFilename()));
		}
	}
}
