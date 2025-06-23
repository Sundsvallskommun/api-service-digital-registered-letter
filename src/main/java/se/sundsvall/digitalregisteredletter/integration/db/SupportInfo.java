package se.sundsvall.digitalregisteredletter.integration.db;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class SupportInfo {

	private String supportText;

	private String contactInformationUrl;

	private String contactInformationEmail;

	private String contactInformationPhoneNumber;

	public static SupportInfo create() {
		return new SupportInfo();
	}

	public String getSupportText() {
		return supportText;
	}

	public void setSupportText(final String supportText) {
		this.supportText = supportText;
	}

	public SupportInfo withSupportText(final String supportText) {
		this.supportText = supportText;
		return this;
	}

	public String getContactInformationUrl() {
		return contactInformationUrl;
	}

	public void setContactInformationUrl(final String contactInformationUrl) {
		this.contactInformationUrl = contactInformationUrl;
	}

	public SupportInfo withContactInformationUrl(final String contactInformationUrl) {
		this.contactInformationUrl = contactInformationUrl;
		return this;
	}

	public String getContactInformationPhoneNumber() {
		return contactInformationPhoneNumber;
	}

	public void setContactInformationPhoneNumber(final String contactInformationPhoneNumber) {
		this.contactInformationPhoneNumber = contactInformationPhoneNumber;
	}

	public SupportInfo withContactInformationPhoneNumber(final String contactInformationPhoneNumber) {
		this.contactInformationPhoneNumber = contactInformationPhoneNumber;
		return this;
	}

	public String getContactInformationEmail() {
		return contactInformationEmail;
	}

	public void setContactInformationEmail(final String contactInformationEmail) {
		this.contactInformationEmail = contactInformationEmail;
	}

	public SupportInfo withContactInformationEmail(final String contactInformationEmail) {
		this.contactInformationEmail = contactInformationEmail;
		return this;
	}

	@Override
	public String toString() {
		return "SupportInfo{" +
			"supportText='" + supportText + '\'' +
			", contactInformationUrl='" + contactInformationUrl + '\'' +
			", contactInformationEmail='" + contactInformationEmail + '\'' +
			", contactInformationPhoneNumber='" + contactInformationPhoneNumber + '\'' +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		SupportInfo that = (SupportInfo) o;
		return Objects.equals(supportText, that.supportText) && Objects.equals(contactInformationUrl, that.contactInformationUrl) && Objects.equals(contactInformationEmail, that.contactInformationEmail)
			&& Objects.equals(contactInformationPhoneNumber, that.contactInformationPhoneNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(supportText, contactInformationUrl, contactInformationEmail, contactInformationPhoneNumber);
	}
}
