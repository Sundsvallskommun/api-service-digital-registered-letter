package se.sundsvall.digitalregisteredletter.integration.db.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.sql.Blob;
import java.util.Objects;

@Entity
@Table(name = "attachment")
public class AttachmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false, length = 36)
	private String id;

	@Column(name = "file_name", nullable = false)
	private String fileName;

	@Column(name = "content_type", length = 50)
	private String contentType;

	@Basic(fetch = FetchType.LAZY)
	@Lob
	@Column(name = "content", columnDefinition = "longblob")
	private Blob content;

	public static AttachmentEntity create() {
		return new AttachmentEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public AttachmentEntity withId(final String id) {
		this.id = id;
		return this;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public AttachmentEntity withFileName(final String fileName) {
		this.fileName = fileName;
		return this;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	public AttachmentEntity withContentType(final String contentType) {
		this.contentType = contentType;
		return this;
	}

	public Blob getContent() {
		return content;
	}

	public void setContent(Blob content) {
		this.content = content;
	}

	public AttachmentEntity withContent(Blob content) {
		this.content = content;
		return this;
	}

	@Override
	public String toString() {
		return "AttachmentEntity{" +
			"id='" + id + '\'' +
			", fileName='" + fileName + '\'' +
			", contentType='" + contentType + '\'' +
			", content='" + content + '\'' +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		AttachmentEntity that = (AttachmentEntity) o;
		return Objects.equals(id, that.id) && Objects.equals(fileName, that.fileName) && Objects.equals(contentType, that.contentType) && Objects.equals(content, that.content);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fileName, contentType, content);
	}
}
