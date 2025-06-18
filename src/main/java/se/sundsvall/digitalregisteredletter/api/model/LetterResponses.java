package se.sundsvall.digitalregisteredletter.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.digitalregisteredletter.support.Builder;

@Builder
@Schema(description = "Paginated response containing a list of letters")
public record LetterResponses(

	@JsonProperty("_meta") @Schema(implementation = PagingAndSortingMetaData.class, accessMode = READ_ONLY) PagingAndSortingMetaData metaData,

	@ArraySchema(schema = @Schema(implementation = LetterResponse.class, accessMode = READ_ONLY)) List<LetterResponse> letters

) {

}
