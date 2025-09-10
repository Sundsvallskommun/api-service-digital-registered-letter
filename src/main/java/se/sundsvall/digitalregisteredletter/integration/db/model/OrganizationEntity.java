package se.sundsvall.digitalregisteredletter.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "organization", indexes = {
	@Index(name = "idx_number", columnList = "number")
}, uniqueConstraints = {
	@UniqueConstraint(name = "uk_number", columnNames = {
		"number"
	})
})
public class OrganizationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "organization")
	private List<LetterEntity> letters = new ArrayList<>();

	@Column(name = "number", nullable = false)
	private Integer number;

	@Column(name = "name", nullable = false)
	private String name;

	public static OrganizationEntity create() {
		return new OrganizationEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public OrganizationEntity withId(String id) {
		this.id = id;
		return this;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public OrganizationEntity withNumber(Integer number) {
		this.number = number;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public OrganizationEntity withName(String name) {
		this.name = name;
		return this;
	}

	public List<LetterEntity> getLetters() {
		return letters;
	}

	public void setLetters(List<LetterEntity> letters) {
		this.letters = letters;
	}

	public OrganizationEntity withLetters(List<LetterEntity> letters) {
		this.letters = letters;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, letters, name, number);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final OrganizationEntity other)) { return false; }
		return Objects.equals(id, other.id) && Objects.equals(letters, other.letters) && Objects.equals(name, other.name) && Objects.equals(number, other.number);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("OrganizationEntity [id=").append(id).append(", letters=").append(letters).append(", number=").append(number).append(", name=").append(name).append("]");
		return builder.toString();
	}

}
