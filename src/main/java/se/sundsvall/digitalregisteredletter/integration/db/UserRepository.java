package se.sundsvall.digitalregisteredletter.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.digitalregisteredletter.integration.db.model.UserEntity;

@CircuitBreaker(name = "userRepository")
public interface UserRepository extends JpaRepository<UserEntity, String> {

	Optional<UserEntity> findByUsernameIgnoreCase(final String username);
}
