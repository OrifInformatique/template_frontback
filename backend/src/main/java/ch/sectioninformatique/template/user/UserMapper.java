package ch.sectioninformatique.template.user;


import ch.sectioninformatique.template.auth.signup.SignUpDto;
import ch.sectioninformatique.template.user.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    @Mapping(target = "password", ignore = true)
    User signUpToUser(SignUpDto signUpDto);

}
