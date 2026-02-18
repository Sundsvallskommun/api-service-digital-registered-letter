package se.sundsvall.digitalregisteredletter.api.model;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SigningInfoTest {
	private static final String STATUS = "status";
	private static final OffsetDateTime SIGNED = OffsetDateTime.now();
	private static final String CONTENT_KEY = "contentKey";
	private static final String ORDER_REF = "orderRef";
	private static final String SIGNATURE = "signature";
	private static final String OCSP_RESPONSE = "ocspResponse";
	private static final String GIVEN_NAME = "givenName";
	private static final String NAME = "name";
	private static final String PERSONAL_IDENTITY_NUMBER = "personalIdentityNumber";
	private static final String SURNAME = "surname";
	private static final String IP_ADDRESS = "ipAddress";
	private static final Boolean MRTD = true;

	@Test
	void constructorTest() {
		final var bean = new SigningInfo(STATUS, SIGNED, CONTENT_KEY, ORDER_REF, SIGNATURE, OCSP_RESPONSE,
			new SigningInfo.User(PERSONAL_IDENTITY_NUMBER, NAME, GIVEN_NAME, SURNAME),
			new SigningInfo.Device(IP_ADDRESS),
			new SigningInfo.StepUp(MRTD));

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = SigningInfoBuilder.create()
			.withContentKey(CONTENT_KEY)
			.withDevice(DeviceBuilder.create()
				.withIpAddress(IP_ADDRESS)
				.build())
			.withOcspResponse(OCSP_RESPONSE)
			.withOrderRef(ORDER_REF)
			.withSignature(SIGNATURE)
			.withSigned(SIGNED)
			.withStatus(STATUS)
			.withStepUp(StepUpBuilder.create()
				.withMrtd(MRTD)
				.build())
			.withUser(UserBuilder.create()
				.withGivenName(GIVEN_NAME)
				.withName(NAME)
				.withPersonalIdentityNumber(PERSONAL_IDENTITY_NUMBER)
				.withSurname(SURNAME)
				.build())
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBeans() {
		assertThat(new SigningInfo.User(null, null, null, null)).hasAllNullFieldsOrProperties();
		assertThat(UserBuilder.create().build()).hasAllNullFieldsOrProperties();
		assertThat(new SigningInfo.Device(null)).hasAllNullFieldsOrProperties();
		assertThat(DeviceBuilder.create().build()).hasAllNullFieldsOrProperties();
		assertThat(new SigningInfo.StepUp(null)).hasAllNullFieldsOrProperties();
		assertThat(StepUpBuilder.create().build()).hasAllNullFieldsOrProperties();
		assertThat(new SigningInfo(null, null, null, null, null, null, null, null, null)).hasAllNullFieldsOrProperties();
		assertThat(SigningInfoBuilder.create().build()).hasAllNullFieldsOrProperties();
		assertThat(SupportInfoBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(SigningInfo bean) {
		assertThat(bean).isNotNull().usingRecursiveAssertion().hasNoNullFields();
		assertThat(bean.contentKey()).isEqualTo(CONTENT_KEY);
		assertThat(bean.device().ipAddress()).isEqualTo(IP_ADDRESS);
		assertThat(bean.ocspResponse()).isEqualTo(OCSP_RESPONSE);
		assertThat(bean.orderRef()).isEqualTo(ORDER_REF);
		assertThat(bean.signature()).isEqualTo(SIGNATURE);
		assertThat(bean.signed()).isEqualTo(SIGNED);
		assertThat(bean.status()).isEqualTo(STATUS);
		assertThat(bean.stepUp().mrtd()).isEqualTo(MRTD);
		assertThat(bean.user().givenName()).isEqualTo(GIVEN_NAME);
		assertThat(bean.user().name()).isEqualTo(NAME);
		assertThat(bean.user().personalIdentityNumber()).isEqualTo(PERSONAL_IDENTITY_NUMBER);
		assertThat(bean.user().surname()).isEqualTo(SURNAME);
	}
}
