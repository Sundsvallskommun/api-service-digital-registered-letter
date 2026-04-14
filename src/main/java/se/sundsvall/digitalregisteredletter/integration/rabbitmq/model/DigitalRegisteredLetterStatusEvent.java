package se.sundsvall.digitalregisteredletter.integration.rabbitmq.model;

public record DigitalRegisteredLetterStatusEvent(
	String recipientId,
	String externalId,
	String status,
	String statusDetail) {
}
