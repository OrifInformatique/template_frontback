# API documentation generation (Spring REST Docs)

Companion guide to the [backend README](../backend/README.md). It describes **how** the template_frontback backend HTTP documentation is produced, verified, and published.

See also:

- [process-documentation.md](process-documentation.md): application structure, security, database, test execution
- [index.html](index.html): consumer-facing API reference (HTML, generated)
- [backend/src/asciidoc/index.adoc](../backend/src/asciidoc/index.adoc): AsciiDoc template (structure and snippet includes)

## Pipeline overview

Library: **Spring REST Docs** (`spring-restdocs-mockmvc` 3.0.2, see `backend/pom.xml`).

Source diagram: [process/restdocs-pipeline.drawio](process/restdocs-pipeline.drawio)

> PNG exports under `docs/process/export/` are optional. Regenerate them after editing the Draw.io sources (see [Regenerate PNG exports](#regenerate-png-exports)).

Tests **do not** generate `index.adoc`: only the AsciiDoc template is maintained by hand. HTTP examples come from the tests.

## Diagram 1: parent process

Source: [process/restdocs-generation.drawio](process/restdocs-generation.drawio) (page **"1. Generation documentation"**)

Summary:

1. Run integration tests with `document(...)` and, on happy paths, JSON contracts (`RestDocsSnippets`).
2. If a JSON field no longer matches the contract, the test fails: no HTML rebuilt from stale examples.
3. If tests pass, Asciidoctor assembles `index.adoc` and the snippets.
4. Maven writes HTML to `backend/target/generated-snippets-html/`.
5. Depending on the execution context, `docs/index.html` at the repository root is updated or not (see below).

## Diagram 2: tests and contracts

Source: [process/restdocs-generation.drawio](process/restdocs-generation.drawio) (page **"2. Tests and contracts"**)

Classes involved:

- `AuthControllerTest`
- `UserControllerTest`
- `TestControllerTest`
- `RoleControllerTest`
- `RestDocsSnippets` (centralized `requestFields` / `responseFields` contracts)
- `RestDocsSensitiveDataMasking` (masks JWT and refresh-token values in snippets)

Configuration:

```java
@AutoConfigureRestDocs(outputDir = "target/generated-snippets")
```

Each `document("auth/…", "users/…", "tests/…", or "roles/…", …, snippets)` call produces a folder under `backend/target/generated-snippets/`, for example:

```
backend/target/generated-snippets/auth/login/http-request.adoc
backend/target/generated-snippets/auth/login/request-fields.adoc
backend/target/generated-snippets/auth/login/response-fields.adoc
```

Each `document(...)` call also applies `maskSensitiveData()` before `prettyPrint()` so that snippets and `docs/index.html` never contain real JWT or cookie values (see below).

## Sensitive data masking

Integration tests use real JWT tokens (signed with `SECURITY_JWT_TOKEN_SECRET_KEY` from `.env` or local `application.properties`). Without masking, those values would be copied verbatim into `target/generated-snippets/` and `docs/index.html`.

`RestDocsSensitiveDataMasking` is an `OperationPreprocessor` applied in the controller test helpers:

```java
preprocessRequest(maskSensitiveData(), prettyPrint())
preprocessResponse(maskSensitiveData(), prettyPrint())
```

| Location | Example before | Placeholder after |
|---|---|---|
| `Authorization` header | `Bearer eyJhbGci...` | `Bearer {access-token}` |
| `Cookie` / `Set-Cookie` | `refresh_token=eyJhbGci...` | `refresh_token={refresh-token}` |
| JSON `token` / `accessToken` | `"eyJhbGci..."` | `"{jwt-access-token}"` |

Curly braces are used instead of angle brackets so placeholders remain visible in HTML (`<access-token>` would be swallowed as a tag inside `<code>` blocks).

Malformed examples used in 401 tests (for example `this.is.not.a.valid.token`) are left unchanged.

Unit tests: `RestDocsSensitiveDataMaskingTest`.

After changing masking rules, regenerate snippets and HTML (`verify` then `package`) and commit `docs/index.html` if it is versioned.

## Diagram 3: Asciidoctor assembly

Source: [process/restdocs-generation.drawio](process/restdocs-generation.drawio) (page **"3. Asciidoctor assembly"**)

At the top of `backend/src/asciidoc/index.adoc`:

```adoc
ifndef::snippets[]
:snippets: ../../target/generated-snippets
endif::[]
```

Then includes such as:

```adoc
include::{snippets}/auth/login/http-request.adoc[]
```

The Maven plugin `asciidoctor-maven-plugin` (phase `prepare-package`) reads `backend/src/asciidoc/index.adoc` and writes HTML to `backend/target/generated-snippets-html/` (`backend/pom.xml`, Maven attribute `<snippets>`).

## Diagram 4: test isolation (401)

Source: [process/restdocs-generation.drawio](process/restdocs-generation.drawio) (page **"4. Isolation tests 401"**)

Separate concern, related to test suite reliability (and therefore documentation), not HTML generation itself.

Related fixes in the template backend:

- `@AfterEach`: `SecurityContextHolder.clearContext()` in `AuthControllerTest`, `UserControllerTest`, and `TestControllerTest`
- `SecurityConfig` uses a single JWT filter chain; `SessionCreationPolicy.ALWAYS` is required for the Azure OAuth2 callback flow (`/auth/tokens`)

## Where does the HTML land?

All Maven commands below are run **on the host** from the **`backend/`** directory. MariaDB may run in Docker (`docker compose up -d db`).

| Command | Snippets | HTML output | `docs/index.html` updated? |
|---|---|---|---|
| `mvn test` | Yes (`target/generated-snippets/`) | No | No |
| `mvn clean package` | Yes | `target/generated-snippets-html/` | No (unless copied manually) |
| `cp target/.../index.html ../docs/` | n/a | n/a | **Yes** |

To version HTML at the repository root:

```bash
cp target/generated-snippets-html/index.html ../docs/index.html
```

## Common commands

From `backend/` with Java 21 and Maven 3.9 on the host, and MariaDB available (Docker or local). Database credentials come from your `.env` (see `env-dist` for local defaults; never reuse those values in production):

```bash
docker compose up -d db

# Load JWT, spring-auth URL, and other vars from .env (see env-dist)
set -a
source .env
set +a

# On the host, use localhost instead of the Docker service name "db"
export TEST_SPRING_DATASOURCE_URL="${TEST_SPRING_DATASOURCE_URL//:\/\/db:/:\/\/localhost:}"
export SPRING_DATASOURCE_USERNAME="$DB_USERNAME"
export SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD"

mvn -Dspring.profiles.active=test clean verify
mvn -Dspring.profiles.active=test clean package
cp target/generated-snippets-html/index.html ../docs/index.html
```

Run only the REST Docs-related tests:

```bash
mvn -Dspring.profiles.active=test test -Dtest=RestDocsSensitiveDataMaskingTest
mvn -Dspring.profiles.active=test test -Dtest=AuthControllerTest,UserControllerTest,TestControllerTest,RoleControllerTest
```

Inspect snippets:

```bash
ls target/generated-snippets/auth/login/
```

Open HTML locally:

```bash
# generated by Maven
xdg-open target/generated-snippets-html/index.html

# copy committed at the repository root
xdg-open ../docs/index.html
```

## Regenerate PNG exports

From the **repository root** (Docker required):

```bash
mkdir -p docs/process/export
for f in restdocs-pipeline restdocs-generation; do
  docker run --rm \
    -v "$PWD/docs/process:/data" \
    rlespinasse/drawio-export:v4.6.0 \
    -f png -t -s 2 -o /data/export /data/${f}.drawio
done
```

Options: `-t` transparent background, `-s 2` scale 2x. Edit `.drawio` files with [diagrams.net](https://app.diagrams.net/) or the Draw.io Integration extension.

After export, embed PNGs in this document if you want inline diagrams (paths under `docs/process/export/`).

## Maintenance rule

Any change to the pipeline or detailed processes must update the Draw.io files (`restdocs-pipeline.drawio`, `restdocs-generation.drawio`) **and** the PNGs in `docs/process/export/` **in the same commit** as the code or text documentation.

After an API change:

1. Update tests and `RestDocsSnippets` if JSON payloads change.
2. Regenerate snippets and HTML (`verify` then `package`).
3. Update `docs/index.html` if published documentation must follow.
4. Update this document or the Draw.io diagrams if the flow changes.

## Keeping documentation in sync manually

| File | Role | When to edit |
|---|---|---|
| `backend/src/asciidoc/index.adoc` | Structure and snippet includes | New documented endpoint, new section |
| Integration tests + `document(...)` | HTTP snippets | New endpoint, request/response change |
| `RestDocsSnippets` | JSON field contracts | Field added, removed, or renamed |
| `RestDocsSensitiveDataMasking` | Token/cookie placeholders in snippets | New sensitive header or JSON field to mask |
