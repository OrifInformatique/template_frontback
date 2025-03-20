package ch.sectioninformatique.template.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UserRepository interface is the repository for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);
    
    boolean existsByLogin(String login);
}

