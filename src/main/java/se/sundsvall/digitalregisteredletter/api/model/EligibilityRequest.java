package se.sundsvall.digitalregisteredletter.api.model;

import java.util.List;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

public record EligibilityRequest(
	List<@ValidUuid String> partyIds) {
}
