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
@Table(name = "user", indexes = {
	@Index(name = "idx_username", columnList = "username")
}, uniqueConstraints = {
	@UniqueConstraint(name = "uk_username", columnNames = {
		"username"
	})
})
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", length = 36)
	private String id;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "user")
	private List<LetterEntity> letters = new ArrayList<>();

	@Column(name = "username", nullable = false)
	private String username;

	public static UserEntity create() {
		return new UserEntity();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public UserEntity withId(String id) {
		this.id = id;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public UserEntity withUsername(String username) {
		this.username = username;
		return this;
	}

	public List<LetterEntity> getLetters() {
		return letters;
	}

	public void setLetters(List<LetterEntity> letters) {
		this.letters = letters;
	}

	public UserEntity withLetters(List<LetterEntity> letters) {
		this.letters = letters;
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, letters, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (!(obj instanceof final UserEntity other)) { return false; }
		return Objects.equals(id, other.id) && Objects.equals(letters, other.letters) && Objects.equals(username, other.username);
	}

	@Override
	public String toString() {
		final var builder = new StringBuilder();
		builder.append("UserEntity [id=").append(id)
			.append(", letters=").append(letters)
			.append(", username=").append(username).append("]");
		return builder.toString();
	}
}
