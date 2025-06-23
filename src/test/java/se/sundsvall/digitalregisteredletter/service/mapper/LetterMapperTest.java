package se.sundsvall.digitalregisteredletter.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.TestDataFactory.createLetterRequest;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toLetterEntity;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toSupportInfo;

import org.junit.jupiter.api.Test;
import se.sundsvall.digitalregisteredletter.api.model.SupportInfoBuilder;

class LetterMapperTest {

	@Test
	void toLetterEntityTest() {
		var letterRequest = createLetterRequest();

		var result = toLetterEntity(letterRequest);

		assertThat(result.getBody()).isEqualTo(letterRequest.body());
		assertThat(result.getContentType()).isEqualTo(letterRequest.contentType());
		assertThat(result.getSupportInfo()).satisfies(supportInfo -> {
			assertThat(supportInfo.getSupportText()).isEqualTo(letterRequest.supportInfo().supportText());
			assertThat(supportInfo.getContactInformationEmail()).isEqualTo(letterRequest.supportInfo().contactInformationEmail());
			assertThat(supportInfo.getContactInformationUrl()).isEqualTo(letterRequest.supportInfo().contactInformationUrl());
			assertThat(supportInfo.getContactInformationPhoneNumber()).isEqualTo(letterRequest.supportInfo().contactInformationPhoneNumber());
		});

	}

	@Test
	void toSupportInfoTest() {
		var supportInfo = SupportInfoBuilder.create()
			.withSupportText("supportText")
			.withContactInformationEmail("supportEmail")
			.withContactInformationUrl("supportUrl")
			.withContactInformationPhoneNumber("supportPhone")
			.build();

		var result = toSupportInfo(supportInfo);

		assertThat(result.getSupportText()).isEqualTo(supportInfo.supportText());
		assertThat(result.getContactInformationEmail()).isEqualTo(supportInfo.contactInformationEmail());
		assertThat(result.getContactInformationUrl()).isEqualTo(supportInfo.contactInformationUrl());
		assertThat(result.getContactInformationPhoneNumber()).isEqualTo(supportInfo.contactInformationPhoneNumber());
	}
}
