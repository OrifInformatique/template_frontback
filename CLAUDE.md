# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository overview

This is a full-stack starter template with a React frontend and a Spring Boot backend, meant to be forked as the base for new applications. It ships with a `user` module (soft-delete, roles/permissions) and an `item` module (a worked CRUD example to copy when adding new domain modules). Authentication itself is **not** implemented here: it is delegated to an external `spring-auth` service (https://github.com/OrifInformatique/spring-auth) that this backend talks to over HTTP/WebClient.

Two independent projects live side by side:
- `backend/` — Spring Boot 3.5.8 / Java 21 REST API
- `frontend/` — React 19 SPA built with Webpack 5 and Tailwind

## Common commands

### Backend (`backend/`)

```bash
mvn spring-boot:run                 # run the app
mvn test                            # run all tests
mvn test -Dtest=ItemServiceTest     # run a single test class
mvn test -Dtest=ItemServiceTest#methodName   # run a single test method
mvn clean                           # remove target/ build artifacts
mvn package                         # build the .jar (also generates REST Docs via asciidoctor)
mvn validate                        # sanity-check project structure
```

Docker (preferred dev workflow):
```bash
docker compose build     # rebuild after changing ENVIRONMENT in .env
docker compose up        # start app + MariaDB containers
docker compose up -d     # same, detached
docker exec -it <container name> sh   # shell into a running container
```

Setup: copy `env-dist` → `.env` and `application.properties-dist` → `application.properties` (both are gitignored; never edit the `-dist` originals). `.env` controls `ENVIRONMENT` (dev/test/prod), the Tomcat port, DB URLs/credentials, the `spring-auth` base URL, Azure OAuth2 URLs, and JWT secret/lifetimes.

### Frontend (`frontend/`)

```bash
npm install
npm run serve             # webpack-dev-server on PORT from .env (default 4000), proxies /auth, /users, /tests to BACKEND_API_URL
npm run build              # production build
npm run storybook          # Storybook on :6006
npm run build-storybook
```

Setup: copy `.env-template` → `.env` and adjust `APP_ROOT` if not serving from `/`.

There is no configured test runner or linter in `frontend/package.json` — don't assume `npm test` or `npm run lint` exist.

## Architecture

### Backend: layered, module-per-domain

Code lives under `ch.sectioninformatique.template`, one package per domain (`user`, `item`, `auth`, `security`, `app`, `config`, `test`). Entry point: `AuthApplication.java` (run this class, not a differently-named `*Application`). Each domain module generally follows Controller → Service → Repository, e.g. for `item`:

- `ItemController` — `@RestController`, endpoints guarded with `@PreAuthorize("hasAuthority('item:read')")` etc.
- `ItemService` — business logic, authorization checks against the authenticated user (author-or-admin ownership checks), talks to repositories
- `ItemRepository` (+ `ItemRepositoryImpl` for custom queries) — Spring Data JPA
- `ItemExceptions` — nested custom exceptions for this domain, thrown from the service and caught by the global handler
- `ItemBuilder`, `ItemSeeder`, `ItemsDTO` — builder, dev data seeding, response DTO

**`item` is the canonical example module** — when adding a new domain, copy its shape rather than inventing a new pattern. `user` follows the same shape but is more complex (soft delete + dual role model, see below).

### Auth is delegated, not implemented locally

`auth/AuthController` does not perform authentication itself — it relays requests (login, register, refresh, Azure OAuth2 callback) to the external `spring-auth` service via `auth/AuthClient` (a Spring WebFlux `WebClient`, configured in `security/WebClientConfig`). `spring-auth`'s base URL comes from `SPRING_AUTH_URL` in `.env`. Locally-issued JWTs from `spring-auth` are then validated on every request by `security/JwtAuthFilter` + `security/UserAuthenticationProvider`, which populate the Spring Security context. See `doc/frontend_backend_auth_architecture.mmd`/`.png` and `doc/process-documentation.md` for full sequence diagrams (standard login, Azure OAuth2 login, refresh token flow).

Azure OAuth2 login is a three-party redirect dance (Frontend ↔ this Backend ↔ spring-auth ↔ Azure): the backend receives a temporary auth code at `/auth/auth-code`, exchanges it with spring-auth for tokens, stashes them in the HTTP session, then the frontend polls `GET /auth/tokens` to retrieve and clear them. This is why `SecurityConfig` sets `SessionCreationPolicy.ALWAYS` even though the API is otherwise stateless/JWT-based.

### Roles and permissions

`security/RoleEnum` (USER, MANAGER, ADMIN, LOCAL_APP_ROLE) each map to a fixed `EnumSet<PermissionEnum>` (`security/PermissionEnum`, e.g. `item:read`, `user:write`) and are converted to Spring Security `GrantedAuthority`s (`ROLE_*` plus the individual permission strings) via `getGrantedAuthorities()`. Controllers authorize with `@PreAuthorize("hasAuthority('item:write')")`-style expressions, sometimes combined with `hasRole(...)`.

`User` supports **two kinds of role**: a global `mainRole` (managed by `spring-auth`, promoted via `AuthClient.promoteToAdmin`) and a `Set<Role> appSpecificRoles` that is local to this app only (promoted purely against the local DB via `UserService.promoteToLocalAppRole`). Don't conflate the two when adding role-related logic.

`User` also supports soft delete (`deleted` flag, default repository queries exclude it) alongside a separate permanent/hard-delete path (`UserRepositoryPermanentDelete`, `DELETE /users/{id}/false/permanent`) that also cleans up the `users_app_specific_roles` join table first.

### Error handling and i18n

Domain exceptions extend `app/exceptions/AppException` (carries an HTTP status) and implement `MessageKeyProvider` (exposes an i18n message key + format args) rather than a hardcoded message. `app/exceptions/GlobalExceptionHandler` is the single `@ControllerAdvice` that catches these, resolves the key through Spring's `MessageSource` against the request locale, and returns a standardized `ErrorDto` JSON body. Auth failures (401/403) go through the security-specific equivalents, `UserAuthenticationEntryPoint` and `CustomAccessDeniedHandler`, which do the same message-key resolution.

Message bundles are split per domain: `resources/messages/{app,auth,item,security,user}/messages_{en,fr}.properties`. When adding a new exception, add message keys to the matching domain bundle in both languages rather than inlining a string. Locale is resolved by `config/LocaleConfig` (default `fr-FR`, overridable via a `lang` query param).

### Frontend: feature-based structure

`frontend/src/features/<name>/` groups everything for a feature: `index.jsx` (entry component), `ui/` (subviews), `api/` (HTTP calls), `locales/{en,fr}/<name>.json` (i18n namespace), and optionally `mocks/`. `frontend/src/common/` holds cross-feature layouts (`MainLayout`), reusable UI (`ui/`), hooks, and utils (`useLocalStorage`, `Redirect`, `fileUtils`).

Routing is centralized in `src/index.js` (React Router v7), nested under `MainLayout` except for standalone routes like `/testAPI`. `i18n.js` auto-discovers **every** `locales/<lang>/<namespace>.json` file across `common/` and `features/**` via `require.context` — dropping a new `locales/en/foo.json` + `locales/fr/foo.json` pair under a feature is enough to register a new i18n namespace, no manual wiring needed.

State: `zustand` is used for feature-local stores (e.g. `features/auth/authStore.jsx` holds the authenticated user/tokens). No global app-wide store — keep new stores scoped to their feature.

The dev server proxies `/auth`, `/users`, `/tests` requests to `BACKEND_API_URL` (`webpack.config.js`) — when adding a new backend module with its own top-level path, add it to that proxy `context` array or frontend API calls will hit webpack-dev-server instead of the backend.

## Notes

- Both `README.md`s (root, `backend/`, `frontend/`) contain more detailed one-time setup instructions (prerequisites, Docker walkthroughs, Azure OAuth2 sequence diagrams) — consult them for environment setup questions.
- `doc/process-documentation.md` has an in-depth, kept-up-to-date module-by-module reference with class/sequence diagrams for the backend; check it before making non-trivial backend architecture changes.
- Backend REST API docs are auto-generated from tests via Spring REST Docs + Asciidoctor (`mvn package`), output to `backend/target/generated-snippets-html` / `backend/docs`.
