package se.sundsvall.digitalregisteredletter.integration.db.model;

import static jakarta.persistence.GenerationType.UUID;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.TimeZoneStorage;

@Entity
@Table(name = "signing_information")
public class SigningInfoEntity {

	@Id
	@GeneratedValue(strategy = UUID)
	@Column(name = "id", nullable = false, updatable = false, length = 36)
	private String id;

	@Column(name = "signed")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime signed;

	@Column(name = "internal_id", length = 36)
	private String internalId;

	@Column(name = "content_key", length = 36)
	private String contentKey;

	@Column(name = "order_ref", length = 36)
	private String orderRef;

	@Column(name = "status")
	private String status;

	@Column(name = "personal_number")
	private String personalNumber;

	@Column(name = "name")
	private String name;

	@Column(name = "given_name")
	private String givenName;

	@Column(name = "surname")
	private String surname;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "mrtd")
	private Boolean mrtd;

	@Column(name = "signature", columnDefinition = "longtext")
	private String signature;

	@Column(name = "ocsp_response", columnDefinition = "longtext")
	private String ocspResponse;

	public static SigningInfoEntity create() {
		return new SigningInfoEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SigningInfoEntity withId(String id) {
		this.id = id;
		return this;
	}

	public OffsetDateTime getSigned() {
		return signed;
	}

	public void setSigned(OffsetDateTime signed) {
		this.signed = signed;
	}

	public SigningInfoEntity withSigned(OffsetDateTime signed) {
		this.signed = signed;
		return this;
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}

	public SigningInfoEntity withInternalId(String internalId) {
		this.internalId = internalId;
		return this;
	}

	public String getContentKey() {
		return contentKey;
	}

	public void setContentKey(String contentKey) {
		this.contentKey = contentKey;
	}

	public SigningInfoEntity withContentKey(String contentKey) {
		this.contentKey = contentKey;
		return this;
	}

	public String getOrderRef() {
		return orderRef;
	}

	public void setOrderRef(String orderRef) {
		this.orderRef = orderRef;
	}

	public SigningInfoEntity withOrderRef(String orderRef) {
		this.orderRef = orderRef;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public SigningInfoEntity withStatus(String status) {
		this.status = status;
		return this;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPersonalNumber() {
		return personalNumber;
	}

	public void setPersonalNumber(String personalNumber) {
		this.personalNumber = personalNumber;
	}

	public SigningInfoEntity withPersonalNumber(String personalNumber) {
		this.personalNumber = personalNumber;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SigningInfoEntity withName(String name) {
		this.name = name;
		return this;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public SigningInfoEntity withGivenName(String givenName) {
		this.givenName = givenName;
		return this;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public SigningInfoEntity withSurname(String surname) {
		this.surname = surname;
		return this;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public SigningInfoEntity withIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		return this;
	}

	public Boolean getMrtd() {
		return mrtd;
	}

	public void setMrtd(Boolean mrtd) {
		this.mrtd = mrtd;
	}

	public SigningInfoEntity withMrtd(Boolean mrtd) {
		this.mrtd = mrtd;
		return this;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public SigningInfoEntity withSignature(String signature) {
		this.signature = signature;
		return this;
	}

	public String getOcspResponse() {
		return ocspResponse;
	}

	public void setOcspResponse(String ocspResponse) {
		this.ocspResponse = ocspResponse;
	}

	public SigningInfoEntity withOcspResponse(String ocspResponse) {
		this.ocspResponse = ocspResponse;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(contentKey, givenName, id, ipAddress, mrtd, name, ocspResponse, orderRef, personalNumber, internalId, signature, signed, status, surname);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final SigningInfoEntity other)) { return false; }
		return Objects.equals(contentKey, other.contentKey) && Objects.equals(givenName, other.givenName) && Objects.equals(id, other.id) && Objects.equals(ipAddress, other.ipAddress) && Objects.equals(mrtd, other.mrtd) && Objects.equals(name, other.name)
			&& Objects.equals(ocspResponse, other.ocspResponse) && Objects.equals(orderRef, other.orderRef) && Objects.equals(personalNumber, other.personalNumber) && Objects.equals(internalId, other.internalId) && Objects
				.equals(signature, other.signature) && Objects.equals(signed, other.signed) && Objects.equals(status, other.status) && Objects.equals(surname, other.surname);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("SigningInfoEntity [id=").append(id).append(", signed=").append(signed).append(", internalId=").append(internalId).append(", contentKey=").append(contentKey).append(", orderRef=").append(orderRef)
			.append(", status=").append(status).append(", personalNumber=").append(personalNumber).append(", name=").append(name).append(", givenName=").append(givenName).append(", surname=").append(surname).append(", ipAddress=").append(ipAddress).append(
				", mrtd=").append(mrtd).append(", signature=").append(signature).append(", ocspResponse=").append(ocspResponse).append("]");
		return builder.toString();
	}
}
