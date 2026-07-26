package itc475.dndencountertool.mapper;

import itc475.dndencountertool.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Field mapping done within class definition
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}