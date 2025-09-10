package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LetterRequestTest {
	private static final String PARTY_ID = "partyId";
	private static final String SUBJECT = "subject";
	private static final String CONTENT_TYPE = "contentType";
	private static final String BODY = "body";
	private static final SupportInfo SUPPORT_INFO = SupportInfoBuilder.create()
		.withSupportText("supportText")
		.withContactInformationUrl("contactInformationUrl")
		.withContactInformationEmail("contactInformationEmail")
		.withContactInformationPhoneNumber("contactInformationPhoneNumber")
		.build();
	private static final Organization ORGANIZATION = OrganizationBuilder.create()
		.withName("name")
		.withNumber(12345)
		.build();

	@Test
	void constructorTest() {
		final var bean = new LetterRequest(PARTY_ID, SUBJECT, SUPPORT_INFO, ORGANIZATION, CONTENT_TYPE, BODY);

		assertBean(bean);
	}

	@Test
	void builderTest() {
		final var bean = LetterRequestBuilder.create()
			.withBody(BODY)
			.withContentType(CONTENT_TYPE)
			.withOrganization(ORGANIZATION)
			.withPartyId(PARTY_ID)
			.withSubject(SUBJECT)
			.withSupportInfo(SUPPORT_INFO)
			.build();

		assertBean(bean);
	}

	@Test
	void noDirtOnEmptyBean() {
		assertThat(new LetterRequest(null, null, null, null, null, null)).hasAllNullFieldsOrProperties();
		assertThat(LetterRequestBuilder.create().build()).hasAllNullFieldsOrProperties();
	}

	private static void assertBean(LetterRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.body()).isEqualTo(BODY);
		assertThat(bean.supportInfo()).usingRecursiveComparison().isEqualTo(SUPPORT_INFO);
		assertThat(bean.organization()).usingRecursiveComparison().isEqualTo(ORGANIZATION);
	}
}
