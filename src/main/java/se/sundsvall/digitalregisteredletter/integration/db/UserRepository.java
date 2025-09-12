package se.sundsvall.digitalregisteredletter.integration.db;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

	Optional<UserEntity> findByUsernameIgnoreCase(final String username);
}
