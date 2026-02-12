package se.sundsvall.digitalregisteredletter.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;

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

	@Column(name = "org_number", nullable = false)
	private String orgNumber;

	@Column(name = "tenant_key", nullable = false)
	private String tenantKey;

	@Column(name = "municipality_id", nullable = false)
	private String municipalityId;

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

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final TenantEntity other)) {
			return false;
		}
		return Objects.equals(id, other.id) && Objects.equals(orgNumber, other.orgNumber) && Objects.equals(tenantKey, other.tenantKey) && Objects.equals(municipalityId, other.municipalityId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, orgNumber, tenantKey, municipalityId);
	}

	@Override
	public String toString() {
		return "TenantEntity [id=" + id
			+ ", orgNumber=" + orgNumber
			+ ", tenantKey=" + tenantKey
			+ ", municipalityId=" + municipalityId + "]";
	}
}
