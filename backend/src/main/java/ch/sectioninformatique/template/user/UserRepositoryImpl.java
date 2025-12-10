package ch.sectioninformatique.template.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of custom repository methods for User entity.
 * This class provides the implementation for permanent deletion of users,
 * allowing permanent removal of user records from the database.
 */
@Repository
@Transactional
public class UserRepositoryImpl implements UserRepositoryPermanentDelete {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void deletePermanentlyById(Long id) {
        entityManager.createNativeQuery("DELETE FROM users WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}