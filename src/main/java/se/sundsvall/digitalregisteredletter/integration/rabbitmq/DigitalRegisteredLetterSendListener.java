package se.sundsvall.digitalregisteredletter.integration.rabbitmq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Organization;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfo;
import se.sundsvall.digitalregisteredletter.integration.postportalservice.PostportalserviceIntegration;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Constants;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.configuration.Queue;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterSendEvent;
import se.sundsvall.digitalregisteredletter.integration.rabbitmq.model.DigitalRegisteredLetterStatusEvent;
import se.sundsvall.digitalregisteredletter.service.LetterService;
import se.sundsvall.digitalregisteredletter.service.model.AttachmentData;

import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Component
public class DigitalRegisteredLetterSendListener {

	private static final Logger LOG = LoggerFactory.getLogger(DigitalRegisteredLetterSendListener.class);

	private final PostportalserviceIntegration postportalserviceIntegration;
	private final LetterService letterService;
	private final Publisher publisher;
	private final Dept44HealthUtility dept44HealthUtility;

	public DigitalRegisteredLetterSendListener(final PostportalserviceIntegration postportalserviceIntegration,
		final LetterService letterService,
		final Publisher publisher,
		final Dept44HealthUtility dept44HealthUtility) {
		this.postportalserviceIntegration = postportalserviceIntegration;
		this.letterService = letterService;
		this.publisher = publisher;
		this.dept44HealthUtility = dept44HealthUtility;
	}

	@RabbitListener(queues = Constants.SEND_DIGITAL_REGISTERED_LETTER_QUEUE)
	@Transactional
	public void handleEvent(final DigitalRegisteredLetterSendEvent event) throws IOException {
		RequestId.init(event.requestId());
		Identifier.set(Identifier.parse("%s; type=adAccount".formatted(event.sender().identifier())));
		LOG.info("Consumed event to send digital registered letter with recipientId: {} and municipalityId: {}", sanitizeForLogging(event.recipientId()), sanitizeForLogging(event.municipalityId()));

		try {
			final var municipalityId = event.municipalityId();

			List<AttachmentData> attachments = new ArrayList<>();
			for (var attachmentId : event.message().attachmentIds()) {
				attachments.add(postportalserviceIntegration.getAttachment(municipalityId, attachmentId));
			}

			final var letterRequest = toLetterRequest(event);
			final var letter = letterService.sendLetter(municipalityId, event.sender().organizationNumber(), letterRequest, attachments);

			try {
				publisher.publishEvent(Queue.STATUS_DIGITAL_REGISTERED_LETTER, new DigitalRegisteredLetterStatusEvent(
					event.recipientId(),
					letter.id(),
					"SENT",
					null));
			} catch (final Exception e) {
				var recipientId = sanitizeForLogging(event.recipientId());
				LOG.error("Letter with id: {} was sent successfully but failed to publish status event for recipientId: {}", letter.id(), recipientId, e);
				dept44HealthUtility.setHealthIndicatorUnhealthy("handleEvent", "Failed to publish status event for recipientId: %s".formatted(sanitizeForLogging(event.recipientId())));
			}
		} finally {
			RequestId.reset();
			Identifier.remove();
		}
	}

	public LetterRequest toLetterRequest(final DigitalRegisteredLetterSendEvent event) {
		final var sender = event.sender();
		final var message = event.message();
		final var recipient = event.recipient();

		return new LetterRequest(
			recipient.partyId(),
			message.subject(),
			new SupportInfo(
				sender.supportText(),
				sender.contactInformationUrl(),
				sender.contactInformationPhoneNumber(),
				sender.contactInformationEmail()),
			new Organization(
				Optional.ofNullable(sender.organizationNumber())
					.map(Long::valueOf)
					.orElse(null),
				sender.organizationName()),
			message.contentType(),
			message.body());
	}
}
