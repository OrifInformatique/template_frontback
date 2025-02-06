package ch.sectioninformatique.template.auth.signup;

public record SignUpDto (String firstName, String lastName, String login, char[] password) { }
