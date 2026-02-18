package se.sundsvall.digitalregisteredletter.integration.db.model;

import org.junit.jupiter.api.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

class SupportInformationTest {

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(SupportInformation.class, allOf(
			hasValidBeanConstructor(),
			hasValidGettersAndSetters(),
			hasValidBeanHashCode(),
			hasValidBeanEquals(),
			hasValidBeanToString()));
	}

	@Test
	void testBuilderMethods() {
		var supportText = "Support text";
		var supportInformationUrl = "https://support.example.com";
		var supportInformationEmail = "support@email.com";
		var supportInformationPhoneNumber = "123456789";

		var supportInformation = SupportInformation.create()
			.withSupportText(supportText)
			.withContactInformationUrl(supportInformationUrl)
			.withContactInformationEmail(supportInformationEmail)
			.withContactInformationPhoneNumber(supportInformationPhoneNumber);

		assertThat(supportInformation.getSupportText()).isEqualTo(supportText);
		assertThat(supportInformation.getContactInformationUrl()).isEqualTo(supportInformationUrl);
		assertThat(supportInformation.getContactInformationEmail()).isEqualTo(supportInformationEmail);
		assertThat(supportInformation.getContactInformationPhoneNumber()).isEqualTo(supportInformationPhoneNumber);

		assertThat(supportInformation).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SupportInformation.create()).hasAllNullFieldsOrPropertiesExcept();
		assertThat(new SupportInformation()).hasAllNullFieldsOrPropertiesExcept();
	}
}
