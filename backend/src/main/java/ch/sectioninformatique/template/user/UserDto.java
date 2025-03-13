package ch.sectioninformatique.template.user;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder(toBuilder = true)
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String login;
    private String token;
    
    @Builder.Default
    private String role = "ROLE_USER";
    
    @Builder.Default
    private List<String> permissions = new ArrayList<>();
}


/*
 * @JsonIgnore explique que le champ password ne doit pas être inclus dans la réponse JSON
 * @JsonIgnoreProperties(ignoreUnknown = true) explique que les champs non définis dans la classe UserDto ne doivent pas provoquer d'erreur
 */