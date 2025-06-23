package se.sundsvall.digitalregisteredletter.service.mapper;

import java.util.Optional;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.integration.db.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.SupportInfo;

public final class LetterMapper {

	private LetterMapper() {}

	public static LetterEntity toLetterEntity(final LetterRequest letterRequest) {
		return Optional.ofNullable(letterRequest).map(letter -> LetterEntity.create()
			.withBody(letter.body())
			.withContentType(letter.contentType())
			.withSupportInfo(toSupportInfo(letter.supportInfo())))
			.orElse(null);
	}

	public static SupportInfo toSupportInfo(final se.sundsvall.digitalregisteredletter.api.model.SupportInfo supportInfo) {
		return Optional.ofNullable(supportInfo).map(info -> SupportInfo.create()
			.withSupportText(info.supportText())
			.withContactInformationUrl(info.contactInformationUrl())
			.withContactInformationEmail(info.contactInformationEmail())
			.withContactInformationPhoneNumber(info.contactInformationPhoneNumber()))
			.orElse(null);
	}

}
