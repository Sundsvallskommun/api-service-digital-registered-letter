package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SupportInfoTest {

	@Test
	void supportInfoConstructorTest() {
		var supportText = "supportText";
		var contactInformationUrl = "contactInformationUrl";
		var contactInformationEmail = "contactInformationEmail";
		var contactInformationPhoneNumber = "contactInformationPhoneNumber";

		var supportInfo = new SupportInfo(supportText, contactInformationUrl, contactInformationPhoneNumber, contactInformationEmail);

		assertThat(supportInfo).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(supportInfo.supportText()).isEqualTo(supportText);
		assertThat(supportInfo.contactInformationUrl()).isEqualTo(contactInformationUrl);
		assertThat(supportInfo.contactInformationEmail()).isEqualTo(contactInformationEmail);
		assertThat(supportInfo.contactInformationPhoneNumber()).isEqualTo(contactInformationPhoneNumber);
	}

	@Test
	void supportInfoBuilderTest() {
		var supportText = "supportText";
		var contactInformationUrl = "contactInformationUrl";
		var contactInformationEmail = "contactInformationEmail";
		var contactInformationPhoneNumber = "contactInformationPhoneNumber";

		var supportInfo = SupportInfoBuilder.create()
			.withSupportText(supportText)
			.withContactInformationUrl(contactInformationUrl)
			.withContactInformationEmail(contactInformationEmail)
			.withContactInformationPhoneNumber(contactInformationPhoneNumber)
			.build();

		assertThat(supportInfo).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(supportInfo.supportText()).isEqualTo(supportText);
		assertThat(supportInfo.contactInformationUrl()).isEqualTo(contactInformationUrl);
		assertThat(supportInfo.contactInformationEmail()).isEqualTo(contactInformationEmail);
		assertThat(supportInfo.contactInformationPhoneNumber()).isEqualTo(contactInformationPhoneNumber);
	}

}
