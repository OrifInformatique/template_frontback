package ch.sectioninformatique.template.item;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

/**
 * Repository interface for managing Item entities in the database.
 * This interface extends Spring's CrudRepository to provide basic CRUD operations
 * for the Item entity. It is automatically implemented by Spring Data JPA.
 */
@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

/**Method to set an author null if the author is deleted 
 * @param authorId the ID of the author to set null 
*/
@Query("UPDATE Item i SET i.author = null WHERE i.author.id= :authorId")
@Modifying
@Transactional
void setAuthorNullByAuthorId(Long authorId);

}
