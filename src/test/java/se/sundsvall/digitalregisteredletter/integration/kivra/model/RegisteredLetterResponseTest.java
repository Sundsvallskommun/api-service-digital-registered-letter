package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class RegisteredLetterResponseTest {

	private static final String STATUS = "status";
	private static final String INTERNAL_ID = "internalId";
	private static final OffsetDateTime SIGNED_AT = OffsetDateTime.now();
	private static final String CONTENT_KEY = "contentKey";
	private static final String ORDER_REF = "orderRef";
	private static final String SIGN_STATUS = "signStatus";
	private static final String SIGNATURE = "signature";
	private static final String OCSP_RESPONSE = "ocspResponse";
	private static final String PERSONAL_NUMBER = "personalNumber";
	private static final String NAME = "name";
	private static final String GIVEN_NAME = "givenName";
	private static final String SURNAME = "surname";
	private static final String IP_ADDRESS = "ipAddress";
	private static final Boolean MRTD = true;

	@Test
	void constructorTest() {
		final var senderReference = new RegisteredLetterResponse.SenderReference(INTERNAL_ID);
		final var user = new RegisteredLetterResponse.BankIdOrder.CompletionData.User(PERSONAL_NUMBER, NAME, GIVEN_NAME, SURNAME);
		final var device = new RegisteredLetterResponse.BankIdOrder.CompletionData.Device(IP_ADDRESS);
		final var stepUp = new RegisteredLetterResponse.BankIdOrder.CompletionData.StepUp(MRTD);
		final var completionData = new RegisteredLetterResponse.BankIdOrder.CompletionData(user, device, stepUp, SIGNATURE, OCSP_RESPONSE);
		final var bankIdOrder = new RegisteredLetterResponse.BankIdOrder(ORDER_REF, SIGN_STATUS, completionData);

		final var bean = new RegisteredLetterResponse(STATUS, SIGNED_AT, senderReference, CONTENT_KEY, bankIdOrder);
		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var senderReference = SenderReferenceBuilder.create()
			.withInternalId(INTERNAL_ID)
			.build();
		final var user = UserBuilder.create()
			.withGivenName(GIVEN_NAME)
			.withName(NAME)
			.withPersonalNumber(PERSONAL_NUMBER)
			.withSurname(SURNAME)
			.build();
		final var device = DeviceBuilder.create()
			.withIpAddress(IP_ADDRESS)
			.build();
		final var stepUp = StepUpBuilder.create()
			.withMrtd(MRTD)
			.build();
		final var completionData = CompletionDataBuilder.create()
			.withDevice(device)
			.withUser(user)
			.withOcspResponse(OCSP_RESPONSE)
			.withStepUp(stepUp)
			.withSignature(SIGNATURE)
			.build();
		final var bankIdOrder = BankIdOrderBuilder.create()
			.withCompletionData(completionData)
			.withOrderRef(ORDER_REF)
			.withStatus(SIGN_STATUS)
			.build();

		final var bean = RegisteredLetterResponseBuilder.create()
			.withBankIdOrder(bankIdOrder)
			.withContentKey(CONTENT_KEY)
			.withSenderReference(senderReference)
			.withSignedAt(SIGNED_AT)
			.withStatus(STATUS)
			.build();

		assertBean(bean);
	}

	private static void assertBean(final RegisteredLetterResponse bean) {
		assertThat(bean).isNotNull();
		assertThat(bean.contentKey()).isEqualTo(CONTENT_KEY);
		assertThat(bean.signedAt()).isEqualTo(SIGNED_AT);
		assertThat(bean.status()).isEqualTo(STATUS);
		assertThat(bean.senderReference()).isNotNull().satisfies(senderReferenceChild -> assertThat(senderReferenceChild.internalId()).isEqualTo(INTERNAL_ID));
		assertThat(bean.bankIdOrder()).isNotNull().satisfies(bankIdOrderChild -> {
			assertThat(bankIdOrderChild.orderRef()).isEqualTo(ORDER_REF);
			assertThat(bankIdOrderChild.status()).isEqualTo(SIGN_STATUS);
		});

		assertThat(bean.bankIdOrder().completionData()).isNotNull().satisfies(completionData -> {
			assertThat(completionData.user()).isNotNull().satisfies(user -> {
				assertThat(user.givenName()).isEqualTo(GIVEN_NAME);
				assertThat(user.name()).isEqualTo(NAME);
				assertThat(user.personalNumber()).isEqualTo(PERSONAL_NUMBER);
				assertThat(user.surname()).isEqualTo(SURNAME);
			});
			assertThat(completionData.stepUp()).isNotNull().satisfies(stepUp -> assertThat(stepUp.mrtd()).isEqualTo(MRTD));
			assertThat(completionData.device()).isNotNull().satisfies(device -> assertThat(device.ipAddress()).isEqualTo(IP_ADDRESS));
			assertThat(completionData.signature()).isEqualTo(SIGNATURE);
			assertThat(completionData.ocspResponse()).isEqualTo(OCSP_RESPONSE);
		});
	}
}
