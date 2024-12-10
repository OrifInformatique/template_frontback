package ch.sectioninformatique.template.item;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;

/* @Data annotation from the Lombok library automatically adds getters and setters */
@Data
/* @Entity annotation indicates that this class corresponds to a database table */
@Entity
public class Item {

    /* @Id annotation indicates the table's primary key */
    @Id
    /* @GeneratedValue anotation indicates an auto-generated value */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* In MariaDB, this will create a Varchar(255) field */
    private String name;

    /* In MariaDB, this will create a Varchar(1000) field */
    @Column(length=1000)
    private String description;

    /* Constructors */
    public Item() {
        super();
    }
    public Item(String name) {
        super();
        this.name = name;
    }
}
