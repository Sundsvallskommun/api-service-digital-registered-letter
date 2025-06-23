package se.sundsvall.digitalregisteredletter.integration.db;

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanConstructor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEquals;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCode;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToString;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.AllOf.allOf;

import org.junit.jupiter.api.Test;

class SupportInfoTest {

	@Test
	void testBean() {
		org.hamcrest.MatcherAssert.assertThat(SupportInfo.class, allOf(
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

		var supportInfo = SupportInfo.create()
			.withSupportText(supportText)
			.withContactInformationUrl(supportInformationUrl)
			.withContactInformationEmail(supportInformationEmail)
			.withContactInformationPhoneNumber(supportInformationPhoneNumber);

		assertThat(supportInfo.getSupportText()).isEqualTo(supportText);
		assertThat(supportInfo.getContactInformationUrl()).isEqualTo(supportInformationUrl);
		assertThat(supportInfo.getContactInformationEmail()).isEqualTo(supportInformationEmail);
		assertThat(supportInfo.getContactInformationPhoneNumber()).isEqualTo(supportInformationPhoneNumber);

		assertThat(supportInfo).hasNoNullFieldsOrProperties();
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SupportInfo.create()).hasAllNullFieldsOrPropertiesExcept();
		assertThat(new SupportInfo()).hasAllNullFieldsOrPropertiesExcept();
	}
}
