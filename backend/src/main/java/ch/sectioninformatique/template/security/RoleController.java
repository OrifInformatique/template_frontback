package ch.sectioninformatique.template.security;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/roles")
public class RoleController {
    private final RoleService roleService;

    RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Retrieves all roles in the system.
     *
     * @return A ResponseEntity containing the list of all roles
     */
    @GetMapping("/all")
    public ResponseEntity<?> getRoles() {
        return ResponseEntity.ok().body(roleService.getAllRoles());
    }
}
