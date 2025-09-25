package se.sundsvall.digitalregisteredletter.integration.db.model;

import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class SupportInformation {

	private String supportText;

	private String contactInformationUrl;

	private String contactInformationEmail;

	private String contactInformationPhoneNumber;

	public static SupportInformation create() {
		return new SupportInformation();
	}

	public String getSupportText() {
		return supportText;
	}

	public void setSupportText(final String supportText) {
		this.supportText = supportText;
	}

	public SupportInformation withSupportText(final String supportText) {
		this.supportText = supportText;
		return this;
	}

	public String getContactInformationUrl() {
		return contactInformationUrl;
	}

	public void setContactInformationUrl(final String contactInformationUrl) {
		this.contactInformationUrl = contactInformationUrl;
	}

	public SupportInformation withContactInformationUrl(final String contactInformationUrl) {
		this.contactInformationUrl = contactInformationUrl;
		return this;
	}

	public String getContactInformationPhoneNumber() {
		return contactInformationPhoneNumber;
	}

	public void setContactInformationPhoneNumber(final String contactInformationPhoneNumber) {
		this.contactInformationPhoneNumber = contactInformationPhoneNumber;
	}

	public SupportInformation withContactInformationPhoneNumber(final String contactInformationPhoneNumber) {
		this.contactInformationPhoneNumber = contactInformationPhoneNumber;
		return this;
	}

	public String getContactInformationEmail() {
		return contactInformationEmail;
	}

	public void setContactInformationEmail(final String contactInformationEmail) {
		this.contactInformationEmail = contactInformationEmail;
	}

	public SupportInformation withContactInformationEmail(final String contactInformationEmail) {
		this.contactInformationEmail = contactInformationEmail;
		return this;
	}

	@Override
	public String toString() {
		return "SupportInformation{" +
			"supportText='" + supportText + '\'' +
			", contactInformationUrl='" + contactInformationUrl + '\'' +
			", contactInformationEmail='" + contactInformationEmail + '\'' +
			", contactInformationPhoneNumber='" + contactInformationPhoneNumber + '\'' +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final var that = (SupportInformation) o;
		return Objects.equals(supportText, that.supportText) && Objects.equals(contactInformationUrl, that.contactInformationUrl) && Objects.equals(contactInformationEmail, that.contactInformationEmail)
			&& Objects.equals(contactInformationPhoneNumber, that.contactInformationPhoneNumber);
	}

	@Override
	public int hashCode() {
		return Objects.hash(supportText, contactInformationUrl, contactInformationEmail, contactInformationPhoneNumber);
	}
}
