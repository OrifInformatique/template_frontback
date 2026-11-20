# APP PACKAGE

## Table of Contents
- [Errors](#errors)
    - [ErrorResponse.java](#errorresponsejava)
    - [ErrorResponseWriter.java](#errorresponsewriterjava)
- [Exceptions](#exceptions)
    - [AppException.java](#appexceptionjava)
    - [AppMessageKeyException.java](#appmessagekeyexceptionjava)
    - [GlobalExceptionHandler.java](#globalexceptionhandlerjava)
    - [MessageKeyProvider.java](#messagekeyproviderjava)
        - [getMessageKey()](#getmessagekey)
        - [getMessageArgs()](#getmessageargs)
        - [NO_ARGS](#no_args)

## *Errors*
### ErrorResponse.java

`ErrorResponse` is the body of **every** error response of the API, whatever
component produces it: `GlobalExceptionHandler`, the JWT filter, the
authentication entry point and the access denied handler (through
`ErrorResponseWriter`). It is the same structure as spring-auth's, so clients
only have one error format to handle.

It contains :

* `timestamp` : when the error occurred (ISO-8601 UTC instant)
* `status` : HTTP status code
* `error` : HTTP reason phrase (e.g. `Unauthorized`)
* `message` : localized, human-readable message
* `fieldErrors` : validation messages keyed by field name, **only present on validation errors** (`@JsonInclude(NON_NULL)`)

```json
{
  "timestamp": "2026-11-20T15:23:02.338Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid or missing authentication token"
}
```

Clients never receive a stack trace, a parser message or another
implementation detail: messages always come from the i18n bundles.

### ErrorResponseWriter.java

`ErrorResponseWriter` writes an `ErrorResponse` directly to the servlet response.

It is used by the components running before Spring MVC (`JwtAuthFilter`,
`UserAuthenticationEntryPoint`, `CustomAccessDeniedHandler`), where
`@ExceptionHandler` methods do not apply. Its `write(request, response, status,
messageKey)` method resolves the message in the language of the request: the
`lang` parameter if given, otherwise the application's `LocaleResolver`
(`Accept-Language` header).

---
## *Exceptions*
### AppException.java

`AppException` is the base exception class used for application-specific errors.

It extends `RuntimeException` and stores the HTTP status that should be returned when the exception is handled by the API.

It contains :

* `status`

The `status` field contains the HTTP status associated with the error.

The `getStatus()` method returns this status so it can be used when creating the HTTP response.

Other application exceptions can extend `AppException` to provide a specific HTTP status for their errors.

---

### AppMessageKeyException.java

`AppMessageKeyException` is a generic application exception used when an error needs a specific message key and optional arguments.

It extends `AppException` and implements `MessageKeyProvider`.

It contains :

* `messageKey`,
* `messageArgs`

The `messageKey` identifies the translated error message that should be displayed.

The `messageArgs` contains optional values used to format the translated message.

The `getMessageKey()` and `getMessageArgs()` methods provide the message key and its arguments to the error handling system.

The exception also inherits the HTTP status from `AppException`.


---

### GlobalExceptionHandler.java

`GlobalExceptionHandler` is a centralized exception handler for the application.

It uses `@ControllerAdvice` to catch exceptions from all controllers and answer
them with an `ErrorResponse`. Messages are translated with `MessageSource`
according to the current locale.

| Exception | Status | Message |
|---|---|---|
| `AppException` | the exception's status | its message key and arguments (`MessageKeyProvider`), otherwise `error.unexpected` |
| `AccessDeniedException` (`@PreAuthorize`) | `403` | `error.accessDenied` |
| `MethodArgumentNotValidException` (`@Valid`) | `400` | `error.validation.failed`, plus `fieldErrors` |
| `HttpMessageNotReadableException` (malformed JSON) | `400` | `error.json.malformed` (the parser's message is not exposed) |
| `HttpMediaTypeNotSupportedException` | `415` | `error.mediaTypeNotSupported` |
| `HttpRequestMethodNotSupportedException` | `405` | `error.methodNotSupported` |
| `MissingServletRequestParameterException` | `400` | `error.missingParameter` |
| `MissingRequestHeaderException` | `400` | `error.missingHeader` |
| `MissingRequestCookieException` (refresh token) | `401` | `security.refresh.missing` |
| `MethodArgumentTypeMismatchException` | `400` | `error.parameter.invalid` (with the accepted values of enum parameters) |
| `NoHandlerFoundException`, `NoResourceFoundException` | `404` | `error.resourceNotFound` |
| any other exception | `500` | `error.unexpected` (logged; authentication exceptions are rethrown to Spring Security) |

---

### MessageKeyProvider.java

`MessageKeyProvider` is an interface used by exceptions to provide a message key and optional arguments for localized error messages.

It allows the global exception handler to retrieve the correct translated message.

It contains :

* `getMessageKey()`,
* `getMessageArgs()`,
* `NO_ARGS`

### `getMessageKey()`

Returns the key used to find the corresponding translated message in the application's message files.

### `getMessageArgs()`

Returns optional arguments used to format the translated message.

By default, it returns an empty array through `NO_ARGS`.

### `NO_ARGS`

A shared empty `Object` array used when an exception does not require any message arguments.

Classes implementing `MessageKeyProvider` can override `getMessageArgs()` when their translated message requires additional values.


---