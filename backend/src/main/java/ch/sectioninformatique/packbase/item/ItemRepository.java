package ch.sectioninformatique.packbase.item;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/* @Repository annotation indicates that the class is a Bean communicating with a data source
 * CrudRepository is a Spring provided interface.
 */
@Repository
public interface ItemRepository extends CrudRepository<Item, Long> {

}
