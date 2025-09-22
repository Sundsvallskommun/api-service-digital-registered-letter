package se.sundsvall.digitalregisteredletter.integration.db.model;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Optional.ofNullable;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.dept44.requestid.RequestId;

@Entity
@Table(name = "letter")
public class LetterEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false, length = 36)
	private String id;

	@Column(name = "municipality_id", length = 4)
	private String municipalityId;

	@Column(name = "body", columnDefinition = "LONGTEXT")
	private String body;

	@Column(name = "content_type", length = 50)
	private String contentType;

	@Column(name = "status", length = 40)
	private String status;

	@Column(name = "request_id", length = 36)
	private String requestId;

	@Column(name = "subject")
	private String subject;

	@Column(name = "party_id", length = 36)
	private String partyId;

	@Column(name = "deleted", nullable = false)
	private boolean deleted = Boolean.FALSE;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@Embedded
	@AttributeOverride(name = "supportText", column = @Column(name = "support_text"))
	@AttributeOverride(name = "contactInformationUrl", column = @Column(name = "support_information_url"))
	@AttributeOverride(name = "contactInformationEmail", column = @Column(name = "support_information_email"))
	@AttributeOverride(name = "contactInformationPhoneNumber", column = @Column(name = "support_information_phone"))
	private SupportInfo supportInfo;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "letter_id", referencedColumnName = "id", nullable = false, foreignKey = @ForeignKey(name = "fk_attachment_letter"))
	private List<AttachmentEntity> attachments = new ArrayList<>();

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_letter"))
	private UserEntity user;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "organization_id", foreignKey = @ForeignKey(name = "fk_organization_letter"))
	private OrganizationEntity organization;

	@PrePersist
	void onPersist() {
		this.created = now(systemDefault()).truncatedTo(MILLIS);
		this.updated = this.created;
		this.requestId = RequestId.get();
	}

	@PreUpdate
	void onUpdate() {
		this.updated = now(systemDefault()).truncatedTo(MILLIS);
		this.requestId = RequestId.get();
	}

	public static LetterEntity create() {
		return new LetterEntity();
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public LetterEntity withSubject(final String subject) {
		this.subject = subject;
		return this;
	}

	public String getPartyId() {
		return partyId;
	}

	public void setPartyId(final String partyId) {
		this.partyId = partyId;
	}

	public LetterEntity withPartyId(final String partyId) {
		this.partyId = partyId;
		return this;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(final UserEntity user) {
		this.user = user;
	}

	public LetterEntity withUser(final UserEntity user) {
		this.user = user;
		return this;
	}

	public OrganizationEntity getOrganization() {
		return organization;
	}

	public void setOrganization(final OrganizationEntity organization) {
		this.organization = organization;
	}

	public LetterEntity withOrganization(final OrganizationEntity organization) {
		this.organization = organization;
		return this;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public LetterEntity withDeleted(boolean deleted) {
		this.deleted = deleted;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public LetterEntity withId(final String id) {
		this.id = id;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}

	public LetterEntity withCreated(final OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(OffsetDateTime updated) {
		this.updated = updated;
	}

	public LetterEntity withUpdated(final OffsetDateTime updated) {
		this.updated = updated;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LetterEntity withStatus(final String status) {
		this.status = status;
		return this;
	}

	public String getRequestId() {
		return requestId;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public LetterEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public String getBody() {
		return body;
	}

	public void setBody(final String body) {
		this.body = body;
	}

	public LetterEntity withBody(final String body) {
		this.body = body;
		return this;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public LetterEntity withContentType(final String contentType) {
		this.contentType = contentType;
		return this;
	}

	public SupportInfo getSupportInfo() {
		return supportInfo;
	}

	public void setSupportInfo(final SupportInfo supportInfo) {
		this.supportInfo = supportInfo;
	}

	public LetterEntity withSupportInfo(final SupportInfo supportInfo) {
		this.supportInfo = supportInfo;
		return this;
	}

	public List<AttachmentEntity> getAttachments() {
		return attachments;
	}

	public void setAttachments(final List<AttachmentEntity> attachments) {
		this.attachments = attachments;
	}

	public LetterEntity withAttachments(final List<AttachmentEntity> attachments) {
		this.attachments = attachments;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(attachments, body, contentType, created, deleted, id, municipalityId, organization, partyId, requestId, status, subject, supportInfo, updated, user);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final LetterEntity other)) { return false; }
		return Objects.equals(attachments, other.attachments) && Objects.equals(body, other.body) && Objects.equals(contentType, other.contentType) && Objects.equals(created, other.created) && deleted == other.deleted && Objects.equals(id, other.id)
			&& Objects.equals(municipalityId, other.municipalityId) && Objects.equals(ofNullable(organization).map(OrganizationEntity::getId).orElse(null), ofNullable(other.organization).map(OrganizationEntity::getId).orElse(null)) && Objects.equals(
				partyId, other.partyId) && Objects.equals(requestId, other.requestId) && Objects.equals(status, other.status) && Objects.equals(subject, other.subject) && Objects.equals(supportInfo, other.supportInfo) && Objects.equals(updated,
					other.updated) && Objects.equals(ofNullable(user).map(UserEntity::getId).orElse(null), ofNullable(other.user).map(UserEntity::getId).orElse(null));
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("LetterEntity [id=").append(id).append(", municipalityId=").append(municipalityId).append(", body=").append(body).append(", contentType=").append(contentType).append(", status=").append(status).append(", requestId=").append(
			requestId).append(", subject=").append(subject).append(", partyId=").append(partyId).append(", deleted=").append(deleted).append(", created=").append(created).append(", updated=").append(updated).append(", supportInfo=").append(supportInfo)
			.append(", attachments=").append(attachments).append(", user=").append(ofNullable(user).map(UserEntity::getId).orElse(null)).append(", organization=").append(ofNullable(organization).map(OrganizationEntity::getId).orElse(null)).append("]");
		return builder.toString();
	}

}
