package se.sundsvall.digitalregisteredletter.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.digitalregisteredletter.support.Builder;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@Builder
public record SigningInfo(

	@Schema(description = "Status of the signing order", accessMode = READ_ONLY) String status,

	@Schema(description = "Timestamp when the letter was signed by receiving party", accessMode = READ_ONLY) @DateTimeFormat(iso = DATE_TIME) OffsetDateTime signed,

	@Schema(description = "The unique Kivra id for the signing order", accessMode = READ_ONLY) String contentKey,

	@Schema(description = "Order reference in Kivra for the signing order", accessMode = READ_ONLY) String orderRef,

	@Schema(description = "The signature made by the receiving party", accessMode = READ_ONLY) String signature,

	@Schema(description = "Online certificate status protocol for the signing order", accessMode = READ_ONLY) String ocspResponse,

	@Schema(description = "Information regarding the signing party", accessMode = READ_ONLY) User user,

	@Schema(description = "Information regarding the device used for the signing order", accessMode = READ_ONLY) Device device,

	@Schema(description = "Information about possible additional verifications that were part of the signing order", accessMode = READ_ONLY) StepUp stepUp) {

	@Builder
	public record User(
		@Schema(description = "Personal identity number for the signing party", accessMode = READ_ONLY) String personalIdentityNumber,

		@Schema(description = "Full name of the signing party", accessMode = READ_ONLY) String name,

		@Schema(description = "First name of the signing party", accessMode = READ_ONLY) String givenName,

		@Schema(description = "Last name of the signing party", accessMode = READ_ONLY) String surname) {
	}

	@Builder
	public record StepUp(
		@Schema(description = "Whether an MRTD check was performed before the order was completed", accessMode = READ_ONLY) Boolean mrtd) {
	}

	@Builder
	public record Device(
		@Schema(description = "Ip address used when the letter was signed", accessMode = READ_ONLY) String ipAddress) {
	}
}
