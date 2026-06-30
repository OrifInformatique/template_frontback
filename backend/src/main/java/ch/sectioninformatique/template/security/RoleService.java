package ch.sectioninformatique.template.security;

import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * Service layer for role-related operations.
 * Provides a clear boundary between controller logic and repository access.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Returns all roles available in the system.
     *
     * @return all Role entities
     */
    public Iterable<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    /**
     * Finds a role by its name.
     *
     * @param name role enum to search for
     * @return Optional containing the Role if found
     */
    public Optional<Role> findByName(RoleEnum name) {
        return roleRepository.findByName(name);
    }
}
