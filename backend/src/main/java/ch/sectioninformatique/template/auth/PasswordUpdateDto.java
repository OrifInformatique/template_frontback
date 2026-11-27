package ch.sectioninformatique.template.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object (DTO) representing a password update input from the client.
 * 
 * This DTO encapsulates the old and new passwords as char arrays for security reasons:
 * - Using `char[]` instead of `String` reduces the risk of passwords lingering
 *   in immutable memory (String pool), which can be accessed in memory dumps.
 * 
 * Validation:
 * - `@NotNull` ensures that passwords are provided in the request body.
 */
/**
 * Body of {@code PUT /users/me/password}.
 *
 * @param oldPassword current password
 * @param newPassword new password, 8 to 72 characters (as required by spring-auth)
 */
public record PasswordUpdateDto(@NotNull char[] oldPassword, @NotNull @Size(min = 8, max = 72) char[] newPassword) {}