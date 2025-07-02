package se.sundsvall.digitalregisteredletter.integration.db;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

	@Column(name = "subject")
	private String subject;

	@Column(name = "party_id", length = 36)
	private String partyId;

	@Column(name = "deleted", nullable = false)
	private boolean deleted = Boolean.FALSE;

	@Column(name = "created")
	private OffsetDateTime created;

	@Column(name = "updated")
	private OffsetDateTime updated;

	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name = "supportText", column = @Column(name = "support_text")),
		@AttributeOverride(name = "contactInformationUrl", column = @Column(name = "support_information_url")),
		@AttributeOverride(name = "contactInformationEmail", column = @Column(name = "support_information_email")),
		@AttributeOverride(name = "contactInformationPhoneNumber", column = @Column(name = "support_information_phone"))
	})
	private SupportInfo supportInfo;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "letter_id", referencedColumnName = "id", nullable = false)
	private List<AttachmentEntity> attachments = new ArrayList<>();

	@PrePersist
	void onPersist() {
		this.created = OffsetDateTime.now();
		this.updated = this.created;
	}

	@PreUpdate
	void onUpdate() {
		this.updated = OffsetDateTime.now();
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

	public static LetterEntity create() {
		return new LetterEntity();
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
	public String toString() {
		return "LetterEntity{" +
			"id='" + id + '\'' +
			", municipalityId='" + municipalityId + '\'' +
			", body='" + body + '\'' +
			", contentType='" + contentType + '\'' +
			", status='" + status + '\'' +
			", subject='" + subject + '\'' +
			", partyId='" + partyId + '\'' +
			", deleted=" + deleted +
			", created=" + created +
			", updated=" + updated +
			", supportInfo=" + supportInfo +
			", attachments=" + attachments +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		LetterEntity that = (LetterEntity) o;
		return deleted == that.deleted && Objects.equals(id, that.id) && Objects.equals(municipalityId, that.municipalityId) && Objects.equals(body, that.body) && Objects.equals(contentType, that.contentType)
			&& Objects.equals(status, that.status) && Objects.equals(subject, that.subject) && Objects.equals(partyId, that.partyId) && Objects.equals(created, that.created) && Objects.equals(updated,
				that.updated) && Objects.equals(supportInfo, that.supportInfo) && Objects.equals(attachments, that.attachments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, municipalityId, body, contentType, status, subject, partyId, deleted, created, updated, supportInfo, attachments);
	}
}
