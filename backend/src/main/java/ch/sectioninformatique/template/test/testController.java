package ch.sectioninformatique.template.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing items in the system.
 * This controller provides endpoints for CRUD operations on items,
 * with appropriate security checks and authorization requirements.
 * All responses are automatically converted to JSON format.
 */
@RestController
public class testController {

    @Autowired
    private Environment environment;

    /**
     * Returns system information and environment variables.
     * This endpoint is used to verify that the application is running
     * and to display configuration information.
     *
     * @return A formatted string containing system information
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/")
    public String getHello() {
        return "<strong>Hello World !</strong><br>" +
               "<strong>JAVA_HOME : </strong>" + environment.getProperty("JAVA_HOME") + "<br>" +
               "<strong>Spring active profile : </strong>" + environment.getProperty("spring.profiles.active") + "<br>" +
               "<strong>Database used : </strong>" + environment.getProperty("spring.datasource.url");
    }

}
