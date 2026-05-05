package ch.sectioninformatique.template.app.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import jakarta.transaction.Transactional;

@NoRepositoryBean
public interface SoftDeleteRepository<T, ID extends Serializable> extends JpaRepository<T, ID>{
    
    @Query(value="SELECT e FROM #{#entityName} e WHERE e.deleted = true")
    List<T> findAllDeleted();


    @Query(value="SELECT e FROM #{#entityName} e WHERE e.deleted = true AND e.id = :id")
    Optional<T> findDeletedByIdDeleted(ID id);


    @Query(value="SELECT e FROM #{#entityName} e WHERE e.id = :id")
    Optional<T> findByIdWithDeleted(ID id);


    @Query(value="SELECT e FROM #{#entityName} e")
    List<T> findAllWithDeleted();
    

    @Modifying
    @Transactional
    @Query(value="UPDATE #{#entityName} e SET e.deleted = false WHERE e.id = :id")
    void restoreById(ID id);

    @Modifying
    @Transactional  
    @Query(value="UPDATE #{#entityName} e SET e.deleted = true WHERE e.id = :id")
    void softDeleteByID(ID id);

    @Modifying
    @Transactional
    @Query(value="DELETE FROM #{#entityName} e WHERE e.id = :id")
    void hardDeleteById(ID id);



}
