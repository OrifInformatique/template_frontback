package ch.sectioninformatique.template.item;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import jakarta.transaction.Transactional;

/**
 * Repository interface for managing Item entities in the database.
 * This interface extends Spring's CrudRepository to provide basic CRUD
 * operations
 * for the Item entity. It is automatically implemented by Spring Data JPA.
 */
@SuppressWarnings("null")
public interface ItemRepository extends CrudRepository<Item, Long> {

    Optional<Item> findById(Long id);

    @Query("SELECT u FROM Item u")
    List<Item> findAllIncludingDeleted();

    @Modifying
    @Transactional

    boolean existsById(Long id);

    @Modifying
    @Transactional
    @Query("DELETE FROM u Item WHERE u.id = :id")
    void deletePermanentlyById(Long id);
}
