package se.sundsvall.digitalregisteredletter.integration.db;

import static se.sundsvall.digitalregisteredletter.Constants.STATUS_NEW;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.digitalregisteredletter.api.model.LetterFilter;
import se.sundsvall.digitalregisteredletter.api.model.LetterRequest;
import se.sundsvall.digitalregisteredletter.api.model.Organization;
import se.sundsvall.digitalregisteredletter.integration.db.model.AttachmentEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.LetterEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.OrganizationEntity;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;
import se.sundsvall.digitalregisteredletter.service.mapper.AttachmentMapper;
import se.sundsvall.digitalregisteredletter.service.mapper.LetterMapper;

@Component
public class RepositoryIntegration {

	private final AttachmentMapper attachmentMapper;
	private final AttachmentRepository attachmentRepository;
	private final LetterRepository letterRepository;
	private final OrganizationRepository organizationRepository;
	private final UserRepository userRepository;
	private final LetterMapper letterMapper;

	public RepositoryIntegration(
		final AttachmentMapper attachmentMapper,
		final AttachmentRepository attachmentRepository,
		final LetterRepository letterRepository,
		final OrganizationRepository organizationRepository,
		final UserRepository userRepository,
		final LetterMapper letterMapper) {

		this.attachmentMapper = attachmentMapper;
		this.attachmentRepository = attachmentRepository;
		this.letterRepository = letterRepository;
		this.organizationRepository = organizationRepository;
		this.userRepository = userRepository;
		this.letterMapper = letterMapper;
	}

	/**
	 * Method for creating an entity matching provided data
	 *
	 * @param  municipalityId municipality "owning" the message
	 * @param  username       name of user that sends the message
	 * @param  letterRequest  request with data for the message
	 * @param  attachments    attachments connected to the message
	 * @return                a persisted entity representation of the data that has been provided to the function
	 */
	@Transactional
	public LetterEntity persistLetter(final String municipalityId, final LetterRequest letterRequest, final List<MultipartFile> attachments) {
		final var letterEntity = letterMapper.toLetterEntity(letterRequest)
			.withAttachments(attachmentMapper.toAttachmentEntities(attachments))
			.withMunicipalityId(municipalityId)
			.withStatus(STATUS_NEW);

		letterEntity.setOrganization(retrieveOrganizationEntity(letterRequest.organization(), letterEntity));
		letterEntity.setUser(retrieveUserEntity(letterEntity));

		return letterRepository.save(letterEntity);
	}

	/**
	 * Method fetches and uses existing entity for organization if found, otherwise it creates a new entity. The incoming
	 * letter entity is then attached to the organization entity.
	 *
	 * @param  organization the organization to fetch or create a database entity for
	 * @param  letterEntity the letter entity to add to the organization
	 * @return              The entity representation of the incoming organization with the LetterEntity added to it.
	 */
	private OrganizationEntity retrieveOrganizationEntity(Organization organization, LetterEntity letterEntity) {
		return organizationRepository.findByNumber(organization.number())
			.map(organizationEntity -> letterMapper.addLetter(organizationEntity, letterEntity))
			.orElse(letterMapper.toOrganizationEntity(organization, letterEntity));
	}

	/**
	 * Method fetches and uses existing entity for user if found, otherwise it creates a new entity. The incoming letter
	 * entity is then attached to the user entity.
	 *
	 * @param  letterEntity the letter entity to add to the user
	 * @return              The entity representation of the incoming username with the LetterEntity added to it.
	 */
	private UserEntity retrieveUserEntity(final LetterEntity letterEntity) {
		var username = Identifier.get().getValue();
		return userRepository.findByUsernameIgnoreCase(username)
			.map(userEntity -> letterMapper.addLetter(userEntity, letterEntity))
			.orElse(letterMapper.toUserEntity(username, letterEntity));
	}

	/**
	 * Method updates the provided letter entity with provided status
	 *
	 * @param letterEntity letter entity to update
	 * @param status       value of status to update to
	 */
	@Transactional
	public void updateStatus(LetterEntity letterEntity, String status) {
		letterEntity.setStatus(status);
		letterRepository.save(letterEntity);
	}

	/**
	 * Method returns an optional letter entity matching sent in municipality id and letter id (or if no match is found, an
	 * Optional.empty)
	 *
	 * @param  municipalityId municipality id to match against
	 * @param  letterId       letter id to match against
	 * @return                an optional letter entity (or optional empty if not found)
	 */
	public Optional<LetterEntity> getLetterEntity(final String municipalityId, final String letterId) {
		return letterRepository.findByIdAndMunicipalityIdAndDeleted(letterId, municipalityId, false);
	}

	/**
	 * Method returns a page based result of letter entities matching provided municipality id and optionally provided
	 * filters
	 *
	 * @param  municipalityId municipality id to match against
	 * @param  filter         filter object containing optional filters to use when retriving result
	 * @param  pageable       pageable object providing data regarding the page and size to retrieve
	 * @return                a paged result matching provided parameters
	 */
	public Page<LetterEntity> getPagedLetterEntities(final String municipalityId, final LetterFilter filter, final Pageable pageable) {
		return letterRepository.findAllByFilter(municipalityId, filter, false, pageable);
	}

	/**
	 * Method does a soft deletion of letter entity matching provided id if found, otherwise it does nothing
	 *
	 * @param id letter entity to be soft deleted
	 */
	@Transactional
	public void softDeleteLetterEntity(String id) {
		letterRepository.findById(id)
			.ifPresent(letterEntity -> {
				letterEntity.setDeleted(true);
				letterRepository.save(letterEntity);
			});
	}

	/**
	 * Method returns an optional attachment entity matching municipality id, letter id and attachment id
	 *
	 * @param  municipalityId municipality id to match against
	 * @param  letterId       letter id to match against
	 * @param  attachmentId   attachment id to match against
	 * @return                an optional attachment entity (or optional empty if not found)
	 */
	public Optional<AttachmentEntity> getAttachmentEntity(final String municipalityId, final String letterId, final String attachmentId) {
		return attachmentRepository.findByIdAndLetterIdAndLetter_MunicipalityId(attachmentId, letterId, municipalityId);
	}
}
