package se.sundsvall.digitalregisteredletter.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LetterRequestTest {

	@Test
	void letterRequestConstructorTest() {
		var partyId = "partyId";
		var subject = "subject";
		var contentType = "contentType";
		var body = "body";

		var supportInfo = SupportInfoBuilder.create()
			.withSupportText("supportText")
			.withContactInformationUrl("contactInformationUrl")
			.withContactInformationEmail("contactInformationEmail")
			.withContactInformationPhoneNumber("contactInformationPhoneNumber")
			.build();

		var letterRequest = new LetterRequest(partyId, subject, supportInfo, contentType, body);

		assertThat(letterRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(letterRequest.partyId()).isEqualTo(partyId);
		assertThat(letterRequest.subject()).isEqualTo(subject);
		assertThat(letterRequest.contentType()).isEqualTo(contentType);
		assertThat(letterRequest.body()).isEqualTo(body);
		assertThat(letterRequest.supportInfo()).isNotNull();
	}

	@Test
	void letterRequestBuilderTest() {
		var partyId = "partyId";
		var subject = "subject";
		var contentType = "contentType";
		var body = "body";

		var supportInfo = SupportInfoBuilder.create()
			.withSupportText("supportText")
			.withContactInformationUrl("contactInformationUrl")
			.withContactInformationEmail("contactInformationEmail")
			.withContactInformationPhoneNumber("contactInformationPhoneNumber")
			.build();

		var letterRequest = LetterRequestBuilder.create()
			.withPartyId(partyId)
			.withSubject(subject)
			.withSupportInfo(supportInfo)
			.withContentType(contentType)
			.withBody(body)
			.build();

		assertThat(letterRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(letterRequest.partyId()).isEqualTo(partyId);
		assertThat(letterRequest.subject()).isEqualTo(subject);
		assertThat(letterRequest.contentType()).isEqualTo(contentType);
		assertThat(letterRequest.body()).isEqualTo(body);
		assertThat(letterRequest.supportInfo()).isNotNull();
	}
}
