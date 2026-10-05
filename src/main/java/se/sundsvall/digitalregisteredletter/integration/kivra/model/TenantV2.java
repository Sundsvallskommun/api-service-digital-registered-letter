package se.sundsvall.digitalregisteredletter.integration.kivra.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
public record TenantV2(
	String name,
	@JsonProperty("company_id") List<CompanyId> companyIds) {

	@Builder
	public record CompanyId(
		String name,
		@JsonProperty("orgnr") String orgNr) {
	}

}
