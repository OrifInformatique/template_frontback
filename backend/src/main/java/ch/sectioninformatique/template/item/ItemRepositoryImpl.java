package ch.sectioninformatique.template.item;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
@Transactional
public class ItemRepositoryImpl implements ItemRepositoryPermanentDelete {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void deletePermanentlyById(Long id) {
        entityManager.createNativeQuery("DELETE FROM items WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
