package ch.sectioninformatique.template.auth;

import lombok.Builder;

/**
 * Body of spring-auth's {@code POST /oauth2/token}: the one-time code and user
 * id received in the redirect URL after an Azure login. The field names must
 * match spring-auth's {@code AuthCodeExchangeDto}.
 *
 * @param userId spring-auth id of the user who logged in
 * @param code   one-time authentication code
 */
@Builder
public record AuthCodeDto(
    Long userId,
    String code
) {}
