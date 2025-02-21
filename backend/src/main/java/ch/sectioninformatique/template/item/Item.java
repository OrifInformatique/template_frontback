package ch.sectioninformatique.template.item;

import jakarta.persistence.*;
import ch.sectioninformatique.template.user.User;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

/**
 * Data transfer object for the item
 */
@Data
@Table(name = "items")
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length=1000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    /**
     * Constructor for the Item class
     */
    public Item() {
    }
    
    /**
     * Constructor for the Item class
     * 
     * @param name The name of the item
     * @param description The description of the item
     * @param author The author of the item
     */
    public Item(String name, String description, User author) {
        this.name = name;   
        this.description = description;
        this.author = author;
    }

    // The @Data annotation from Lombok should generate these methods,
    // but let's add them explicitly to ensure they exist
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
}
