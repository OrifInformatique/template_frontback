package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.security.Role;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * UserBuilder class is used to build User objects.
 */
public class UserBuilder {

    private long id;
    private String firstName;
    private String lastName;
    private String login;
    private String password;
    private Date createdAt;
    private Date updatedAt;
    private Set<Role> roles = new HashSet<>();

    public UserBuilder() {
    }

    /**
     * Set the ID of the user.
     * 
     * @param id The ID of the user
     * @return The UserBuilder
     */
    public UserBuilder setId(long id) {
        this.id = id;
        return this;
    }

    /**
     * Set the first name of the user.
     * 
     * @param firstName The first name of the user
     * @return The UserBuilder
     */
    public UserBuilder setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Set the last name of the user.
     * 
     * @param lastName The last name of the user
     * @return The UserBuilder
     */
    public UserBuilder setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Set the login of the user.
     * 
     * @param login The login of the user
     * @return The UserBuilder
     */
    public UserBuilder setLogin(String login) {
        this.login = login;
        return this;
    }

    /**
     * Set the password of the user.
     * 
     * @param password The password of the user
     * @return The UserBuilder
     */
    public UserBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Set the creation date of the user.
     * 
     * @param createdAt The creation date of the user
     * @return The UserBuilder
     */
    public UserBuilder setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Set the update date of the user.
     * 
     * @param updatedAt The update date of the user
     * @return The UserBuilder
     */
    public UserBuilder setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Set the roles of the user.
     * 
     * @param roles The roles of the user
     * @return The UserBuilder
     */
    public UserBuilder setRoles(Set<Role> roles) {
        this.roles = roles;
        return this;
    }
    
    /**
     * Add a role to the user.
     * 
     * @param role The role to add
     * @return The UserBuilder
     */
    public UserBuilder addRole(Role role) {
        this.roles.add(role);
        return this;
    }
    
    /**
     * Build the User object.
     * 
     * @return The User object
     */
    public User build() {
        return new User(
            this.id,
            this.firstName,
            this.lastName,
            this.login,
            this.password,
            this.createdAt,
            this.updatedAt,
            this.roles
        );
    }
}
