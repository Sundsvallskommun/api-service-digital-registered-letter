package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SupportInfoTest {
	private final static String SUPPORT_TEXT = "supportText";
	private final static String CONTACT_INFORMATION_URL = "contactInformationUrl";
	private final static String CONTACT_INFORMATION_EMAIL = "contactInformationEmail";
	private final static String CONTACT_INFORMATION_PHONE_NUMBER = "contactInformationPhoneNumber";

	@Test
	void supportInfoConstructorTest() {
		final var bean = new SupportInfo(SUPPORT_TEXT, CONTACT_INFORMATION_URL, CONTACT_INFORMATION_PHONE_NUMBER, CONTACT_INFORMATION_EMAIL);

		assertBean(bean);
	}

	@Test
	void supportInfoBuilderTest() {
		final var bean = SupportInfoBuilder.create()
			.withSupportText(SUPPORT_TEXT)
			.withContactInformationUrl(CONTACT_INFORMATION_URL)
			.withContactInformationEmail(CONTACT_INFORMATION_EMAIL)
			.withContactInformationPhoneNumber(CONTACT_INFORMATION_PHONE_NUMBER)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new SupportInfo(null, null, null, null)).hasAllNullFieldsOrProperties();
		assertThat(SupportInfoBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(SupportInfo bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.supportText()).isEqualTo(SUPPORT_TEXT);
		assertThat(bean.contactInformationUrl()).isEqualTo(CONTACT_INFORMATION_URL);
		assertThat(bean.contactInformationEmail()).isEqualTo(CONTACT_INFORMATION_EMAIL);
		assertThat(bean.contactInformationPhoneNumber()).isEqualTo(CONTACT_INFORMATION_PHONE_NUMBER);
	}
}
