package ch.sectioninformatique.template.user;

public class UserDto {
    private String username;
    private String email;

    // Default constructor
    public UserDto() {
    }

    // Constructor with all fields
    public UserDto(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Convert User entity to UserDto
    public static UserDto fromUser(User user) {
        return new UserDto(user.getUsername(), user.getEmail());
    }

    // Convert UserDto to User entity
    public User toUser() {
        User user = new User();
        user.setFullName(this.username);
        user.setEmail(this.email);
        return user;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}