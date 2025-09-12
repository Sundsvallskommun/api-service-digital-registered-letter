package se.sundsvall.digitalregisteredletter.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.addLetter;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toLetter;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toLetterEntity;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toLetters;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toOrganizationEntity;
import static se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper.toUserEntity;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.digitalregisteredletter.api.model.Attachments;
import se.sundsvall.digitalregisteredletter.api.model.Letter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Letters;
import se.sundsvall.digitalregisteredletter.api.model.LettersBuilder;
import se.sundsvall.digitalregisteredletter.api.model.Organization;
import se.sundsvall.digitalregisteredletter.integration.db.LetterRepository;
import se.sundsvall.digitalregisteredletter.integration.db.OrganizationRepository;
import se.sundsvall.digitalregisteredletter.integration.db.UserRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;
import se.sundsvall.digitalregisteredletter.integration.kivra.KivraIntegration;
import se.sundsvall.digitalregisteredletter.integration.party.PartyIntegration;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;
import se.sundsvall.digitalregisteredletter.service.util.IdentifierUtil;

@Service
public class LetterService {

	private final LetterRepository letterRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final AttachmentMapper attachmentMapper;

	private final PartyIntegration partyIntegration;
	private final KivraIntegration kivraIntegration;

	public LetterService(final LetterRepository letterRepository,
		final OrganizationRepository organizationRepository,
		final UserRepository userRepository,
		final AttachmentMapper attachmentMapper,
		final PartyIntegration partyIntegration,
		final KivraIntegration kivraIntegration) {

		this.letterRepository = letterRepository;
		this.organizationRepository = organizationRepository;
		this.userRepository = userRepository;
		this.attachmentMapper = attachmentMapper;
		this.partyIntegration = partyIntegration;
		this.kivraIntegration = kivraIntegration;
	}

	public String sendLetter(final String municipalityId, final LetterRequest letterRequest, final Attachments attachments) {
		final var username = IdentifierUtil.getAdUser();
		final var letter = persistLetter(municipalityId, username, letterRequest, attachments);
		final var legalId = partyIntegration.getLegalIdByPartyId(municipalityId, letterRequest.partyId());
		final var status = kivraIntegration.sendContent(letter, legalId);

		letter.setStatus(status);
		letterRepository.save(letter);

		return letter.getId();
	}

	LetterEntity persistLetter(final String municipalityId, final String username, final LetterRequest letterRequest, final Attachments attachments) {
		final var attachmentEntities = attachmentMapper.toAttachmentEntities(attachments);
		final var letterEntity = toLetterEntity(letterRequest)
			.withAttachments(attachmentEntities)
			.withMunicipalityId(municipalityId)
			.withStatus("NEW");

		letterEntity.setOrganization(retrieveOrganization(letterRequest.organization(), letterEntity));
		letterEntity.setUser(retrieveUser(username, letterEntity));

		return letterRepository.save(letterEntity);
	}

	/**
	 * Method fetches and uses existing entity for organization if found, otherwise it creates a new entity. The
	 * incoming letter entity is then attached to the organization entity.
	 *
	 * @param  organization the organization to fetch or create a database entity for
	 * @param  letterEntity the letter entity to add to the organization
	 * @return              The entity representation of the incoming organization with the LetterEntity added to it.
	 */
	private OrganizationEntity retrieveOrganization(Organization organization, LetterEntity letterEntity) {
		return organizationRepository.findByNumber(organization.number())
			.map(oe -> addLetter(oe, letterEntity))
			.orElse(toOrganizationEntity(organization, letterEntity));
	}

	/**
	 * Method fetches and uses existing entity for user if found, otherwise it creates a new entity. The
	 * incoming letter entity is then attached to the user entity.
	 *
	 * @param  username     the username of the user to fetch or create a database entity for
	 * @param  letterEntity the letter entity to add to the user
	 * @return              The entity representation of the incoming username with the LetterEntity added to it.
	 */
	private UserEntity retrieveUser(String username, LetterEntity letterEntity) {
		return userRepository.findByUsernameIgnoreCase(username)
			.map(ue -> addLetter(ue, letterEntity))
			.orElse(toUserEntity(username, letterEntity));
	}

	public Letter getLetter(final String municipalityId, final String letterId) {
		final var letter = letterRepository.findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Letter with id '%s' and municipalityId '%s' not found".formatted(letterId, municipalityId)));

		return toLetter(letter);
	}

	public Letters getLetters(final String municipalityId, final Pageable pageable) {
		final var page = letterRepository.findAllByMunicipalityIdAndDeleted(municipalityId, false, pageable);

		return LettersBuilder.create()
			.withMetaData(PagingAndSortingMetaData.create().withPageData(page))
			.withLetters(toLetters(page.getContent()))
			.build();
	}
}
