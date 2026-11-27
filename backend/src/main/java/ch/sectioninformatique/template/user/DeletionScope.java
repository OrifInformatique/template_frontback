package ch.sectioninformatique.template.user;

import java.util.Locale;

/**
 * Scope of a user deletion ({@code DELETE /users/{id}?scope=}).
 */
public enum DeletionScope {
    /** Only the local user of this application (default). */
    LOCAL,
    /** The global user in spring-auth, and the local user. */
    GLOBAL;

    /**
     * Parses a request parameter value, case-insensitively.
     *
     * @param value the parameter value
     * @return the matching scope
     * @throws IllegalArgumentException if the value matches no scope
     */
    public static DeletionScope fromParam(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "expected one of: local, global");
        }
    }
}
