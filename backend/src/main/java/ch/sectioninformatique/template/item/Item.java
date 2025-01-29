package ch.sectioninformatique.template.item;

import jakarta.persistence.*;
import ch.sectioninformatique.template.user.User;

import lombok.Data;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
/* @Table annotation rename table name in the Database */
@Table(name = "items")
/* @Entity annotation indicates that this class corresponds to a database table */
@Entity
public class Item {

    /* @Id annotation indicates the table's primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length=1000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private User author;

    public Item() {
    }
    
    public Item(String name, String description, User author) {
        this.name = name;   
        this.description = description;
        this.author = author;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
}