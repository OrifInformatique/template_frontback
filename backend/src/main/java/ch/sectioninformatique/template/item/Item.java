package ch.sectioninformatique.template.item;

import ch.sectioninformatique.template.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

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
    private Integer id;

    private String name;

    @Column(length=1000)
    private String description;

    @ManyToOne  // Change @OneToOne to @ManyToOne because a user can have multiple items
    @JoinColumn(name = "author_id")
    private User author;

    public Item() {
    }
    
    public Item(String name, String description, User author) {
        this.name = name;   
        this.description = description;
        this.author = author;
    }
}