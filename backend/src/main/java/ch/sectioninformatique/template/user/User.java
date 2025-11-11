package ch.sectioninformatique.template.user;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ch.sectioninformatique.template.security.Role;

import org.springframework.security.core.GrantedAuthority;
import java.util.*;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing a user in the system.
 * This class implements Spring Security's UserDetails interface to provide
 * user authentication and authorization functionality. It maps to the 'users'
 * table in the database and contains all necessary user information including
 * personal details, credentials, and roles.
 */
@Data
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class User {

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

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @Builder.Default
    private Role mainRole = new Role();

    @ManyToMany(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Role> appSpecificRoles = new HashSet<>();

    public User(long id,
                String firstName,
                String lastName,
                String login,
                Date createdAt,
                Date updatedAt,
                boolean deleted,
                Role mainRole,
                Set<Role> appSpecificRoles) {
        super();
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.mainRole = mainRole;
        this.appSpecificRoles = appSpecificRoles;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        Set<Role> roleList = this.appSpecificRoles;
        roleList.add(this.mainRole);
        for (Role role : roleList) {
            authorities.addAll(role.getName().getGrantedAuthorities());
        }
        return authorities;
    }

    public String getUsername() {
        return login;
    }

    public boolean isAccountNonExpired() { return true; }

    public boolean isAccountNonLocked() { return true; }

    public boolean isCredentialsNonExpired() { return true; }

    public boolean isEnabled() { return !deleted; } // Optional tie-in

    public Role getMainRole() { return mainRole; }

    public List<String> getAppSpecificRolesString() {
        List<String> names = new ArrayList<>();
        for (Role role : appSpecificRoles) {
            names.add(role.getName().name());
        }
        return names;
    }

    public Set<Role> getAllRoles() {
        Set<Role> allRoles = new HashSet<>();
        if (appSpecificRoles != null) {
            allRoles.addAll(appSpecificRoles);
        }
        return allRoles;
    }

    public void setMainRole(Role role) { mainRole = role; }

    public void addAppSpecificRoles(Role role) { appSpecificRoles.add(role); }
}
