package se.sundsvall.digitalregisteredletter.service;

import static java.util.Optional.ofNullable;
import static org.zalando.problem.Status.NOT_FOUND;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.digitalregisteredletter.api.model.Attachments;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.SigningInfo;
import se.sundsvall.digitalregisteredletter.integration.db.RepositoryIntegration;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;
import se.sundsvall.digitalregisteredletter.service.util.IdentifierUtil;

@Service
public class LetterService {

	private final KivraIntegration kivraIntegration;
	private final PartyIntegration partyIntegration;
	private final RepositoryIntegration repositoryIntegration;
	private final LetterMapper letterMapper;

	public LetterService(
		final KivraIntegration kivraIntegration,
		final PartyIntegration partyIntegration,
		final RepositoryIntegration repositoryIntegration,
		final se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper letterMapper) {

		this.kivraIntegration = kivraIntegration;
		this.partyIntegration = partyIntegration;
		this.repositoryIntegration = repositoryIntegration;
		this.letterMapper = letterMapper;
	}

	public Letter sendLetter(final String municipalityId, final LetterRequest letterRequest, final Attachments attachments) {
		final var username = IdentifierUtil.getAdUser();
		final var legalId = partyIntegration.getLegalIdByPartyId(municipalityId, letterRequest.partyId()); // Verify that match for party in request exists as there is no point in persisting entity otherwise

		final var letterEntity = repositoryIntegration.persistLetter(municipalityId, username, letterRequest, attachments); // Create a new entity in database for the letter
		final var status = kivraIntegration.sendContent(letterEntity, legalId); // Send letter to Kivra
		repositoryIntegration.updateStatus(letterEntity, status); // Update entity with status from Kivra response

		return letterMapper.toLetter(letterEntity);
	}

	public Letter getLetter(final String municipalityId, final String letterId) {
		final var letterEntity = repositoryIntegration.getLetterEntity(municipalityId, letterId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));

		return letterMapper.toLetter(letterEntity);
	}

	public Letters getLetters(final String municipalityId, final LetterFilter filter, final Pageable pageable) {
		final var page = repositoryIntegration.getPagedLetterEntities(municipalityId, filter, pageable);

		return letterMapper.toLetters(page);
	}

	public SigningInfo getSigningInformation(final String municipalityId, final String letterId) {
		final var letterEntity = repositoryIntegration.getLetterEntity(municipalityId, letterId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));

		return ofNullable(letterMapper.toSigningInfo(letterEntity.getSigningInformation()))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Signing information beloning to letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));
	}
}
