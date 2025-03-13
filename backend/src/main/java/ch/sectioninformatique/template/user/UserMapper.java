package ch.sectioninformatique.template.user;

import ch.sectioninformatique.template.auth.signup.SignUpDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", source = "role.name")
    @Mapping(target = "permissions", source = "authorities", qualifiedByName = "authoritiesToPermissions")
    UserDto toUserDto(User user);

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User signUpToUser(SignUpDto signUpDto);

    @Named("authoritiesToPermissions")
    default List<String> authoritiesToPermissions(java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
        if (authorities == null) return null;
        return authorities.stream()
            .map(auth -> auth.getAuthority())
            .collect(Collectors.toList());
    }
}
