package se.sundsvall.digitalregisteredletter.integration.db.model;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;

@Entity
@Table(name = "tenant", indexes = {
	@Index(name = "idx_tenant_municipality_id", columnList = "municipality_id"),
}, uniqueConstraints = {
	@UniqueConstraint(name = "uk_tenant_org_municipality", columnNames = {
		"org_number", "municipality_id"
	})
})
public class TenantEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@Column(name = "org_number", nullable = false, length = 12)
	private String orgNumber;

	@Column(name = "tenant_key", nullable = false)
	private String tenantKey;

	@Column(name = "municipality_id", nullable = false, length = 4)
	private String municipalityId;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "modified")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime modified;

	@OneToMany(mappedBy = "tenant")
	private List<LetterEntity> letters;

	@PrePersist
	void onPersist() {
		this.created = now(systemDefault()).truncatedTo(MILLIS);
		this.modified = this.created;
	}

	@PreUpdate
	void onUpdate() {
		this.modified = now(systemDefault()).truncatedTo(MILLIS);
	}

	public static TenantEntity create() {
		return new TenantEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public TenantEntity withId(final String id) {
		this.id = id;
		return this;
	}

	public String getOrgNumber() {
		return orgNumber;
	}

	public void setOrgNumber(final String orgNumber) {
		this.orgNumber = orgNumber;
	}

	public TenantEntity withOrgNumber(final String orgNumber) {
		this.orgNumber = orgNumber;
		return this;
	}

	public String getTenantKey() {
		return tenantKey;
	}

	public void setTenantKey(final String tenantKey) {
		this.tenantKey = tenantKey;
	}

	public TenantEntity withTenantKey(final String tenantKey) {
		this.tenantKey = tenantKey;
		return this;
	}

	public String getMunicipalityId() {
		return municipalityId;
	}

	public void setMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
	}

	public TenantEntity withMunicipalityId(final String municipalityId) {
		this.municipalityId = municipalityId;
		return this;
	}

	public OffsetDateTime getCreated() {
		return created;
	}

	public void setCreated(final OffsetDateTime created) {
		this.created = created;
	}

	public TenantEntity withCreated(final OffsetDateTime created) {
		this.created = created;
		return this;
	}

	public OffsetDateTime getModified() {
		return modified;
	}

	public void setModified(final OffsetDateTime modified) {
		this.modified = modified;
	}

	public TenantEntity withModified(final OffsetDateTime modified) {
		this.modified = modified;
		return this;
	}

	public List<LetterEntity> getLetters() {
		return letters;
	}

	public void setLetters(final List<LetterEntity> letters) {
		this.letters = letters;
	}

	public TenantEntity withLetters(final List<LetterEntity> letters) {
		this.letters = letters;
		return this;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final TenantEntity other)) {
			return false;
		}
		return Objects.equals(id, other.id) && Objects.equals(orgNumber, other.orgNumber) && Objects.equals(tenantKey, other.tenantKey) && Objects.equals(municipalityId, other.municipalityId) && Objects.equals(created, other.created) && Objects.equals(
			modified, other.modified);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, orgNumber, tenantKey, municipalityId, created, modified);
	}

	@Override
	public String toString() {
		return "TenantEntity [id=" + id
			+ ", orgNumber=" + orgNumber
			+ ", tenantKey=" + tenantKey
			+ ", municipalityId=" + municipalityId
			+ ", created=" + created
			+ ", modified=" + modified
			+ ", letters=" + (letters != null ? letters.size() : 0) + "]";
	}
}
