package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.security.Role;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.security.core.GrantedAuthority;

import java.util.*;

import lombok.Data;

/**
 * User class represents the user entity in the database.
 */
@Data
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private long id;

    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Column(unique = true, length = 100, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        for (Role role : this.roles) {
            authorities.addAll(role.getName().getGrantedAuthorities());
        }
        return authorities;
    }
    
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    /**
     * Constructor for the User class.
     * 
     * @param id The ID of the user
     * @param firstName The first name of the user
     * @param lastName The last name of the user
     * @param login The login of the user
     * @param password The password of the user
     * @param createdAt The creation date of the user
     * @param updatedAt The update date of the user
     * @param roles The roles of the user
     */
    public User(long id,
                String firstName, 
                String lastName, 
                String login,
                String password, 
                Date createdAt, 
                Date updatedAt,
                Set<Role> roles
                )
                
    {
        super();
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roles = roles;
    }

    /**
     * Get the password for the user.
     * 
     * @return The password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Get the username for the user.
     * 
     * @return The username
     */
    @Override
    public String getUsername() {
        return login;
    }

    /**
     * Check if the account is non expired.
     * 
     * @return True if the account is non expired, false otherwise
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Check if the account is non locked.
     * 
     * @return True if the account is non locked, false otherwise
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Check if the credentials are non expired.
     * 
     * @return True if the credentials are non expired, false otherwise
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Check if the account is enabled.
     * 
     * @return True if the account is enabled, false otherwise
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Get the role for the user.
     * 
     * @return The role
     */
    public Role getRole() {
       return roles.iterator().next();
    }
}
