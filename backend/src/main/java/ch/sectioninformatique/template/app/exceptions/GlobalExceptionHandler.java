package ch.sectioninformatique.template.app.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import ch.sectioninformatique.template.app.errors.ErrorResponse;

/**
 * GlobalExceptionHandler is a centralized exception handling component for the
 * application.
 *
 * This class uses Spring's @ControllerAdvice annotation to intercept exceptions
 * thrown across the entire application and answer every error with the same
 * body, {@link ErrorResponse} (also used by the security components through
 * {@code ErrorResponseWriter}, and by spring-auth).
 *
 * Messages are always resolved from the i18n bundles: clients never receive a
 * stack trace, a parser message or another implementation detail.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // ========================================================================
    // Custom Application Exceptions
    // ========================================================================

    /**
     * Handles AppException - the custom application exception.
     *
     * Resolves the exception's message via MessageKeyProvider when available,
     * otherwise returns a generic unexpected error message.
     *
     * @param ex The AppException instance
     * @return the exception's status and resolved message
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        if (ex instanceof MessageKeyProvider provider) {
            return build(ex.getStatus(), msg(provider.getMessageKey(), provider.getMessageArgs()));
        }
        return build(ex.getStatus(), msg("error.unexpected"));
    }

    // ========================================================================
    // Spring Framework Built-in Exceptions
    // ========================================================================

    /**
     * Handles AccessDeniedException - thrown by method security
     * ({@code @PreAuthorize}) when the user lacks a permission.
     *
     * @param ex The AccessDeniedException thrown by Spring Security
     * @return HTTP 403 and the permission denied message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, msg("error.accessDenied"));
    }

    /**
     * Bean validation failure on a {@code @Valid} request body. The top-level
     * message stays generic; per-field messages are exposed in {@code fieldErrors}.
     *
     * @param ex The MethodArgumentNotValidException from Spring
     * @return HTTP 400 with the invalid fields
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(),
                    error.getDefaultMessage() != null ? error.getDefaultMessage() : error.getField());
        }

        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, msg("error.validation.failed"), fieldErrors));
    }

    /**
     * Request body missing or not readable as JSON (malformed JSON, wrong value
     * type...). The parser's message is not exposed.
     *
     * @param ex The HttpMessageNotReadableException from Spring
     * @return HTTP 400 and a generic message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, msg("error.json.malformed"));
    }

    /**
     * Request body in a format the endpoint does not accept (e.g. not JSON).
     *
     * @param ex The HttpMediaTypeNotSupportedException from Spring
     * @return HTTP 415
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, msg("error.mediaTypeNotSupported"));
    }

    /**
     * HTTP method not supported by the URL (e.g. DELETE on a read-only resource).
     *
     * @param ex The HttpRequestMethodNotSupportedException from Spring
     * @return HTTP 405
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, msg("error.methodNotSupported", ex.getMethod()));
    }

    /**
     * A required query parameter is missing.
     *
     * Example: DELETE /users/{userId} without the 'global' parameter
     *
     * @param ex The MissingServletRequestParameterException from Spring
     * @return HTTP 400 with the parameter name
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, msg("error.missingParameter", ex.getParameterName()));
    }

    /**
     * A required header is missing.
     *
     * @param ex The MissingRequestHeaderException from Spring
     * @return HTTP 400 with the header name
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        return build(HttpStatus.BAD_REQUEST, msg("error.missingHeader", ex.getHeaderName()));
    }

    /**
     * The only cookie read by the API is the refresh token: without it the client
     * is not authenticated for the refresh operation (as in spring-auth).
     *
     * @param ex The MissingRequestCookieException from Spring
     * @return HTTP 401
     */
    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<ErrorResponse> handleMissingCookie(MissingRequestCookieException ex) {
        return build(HttpStatus.UNAUTHORIZED, msg("security.refresh.missing"));
    }

    /**
     * A path variable or query parameter cannot be converted to the expected type.
     *
     * Occurs, for instance, when an enum-typed query parameter receives a value
     * that matches no constant (e.g. {@code GET /users?state=foo}): the cause's
     * message, written by the application's converters, carries the accepted values.
     *
     * @param ex The MethodArgumentTypeMismatchException from Spring
     * @return HTTP 400 with the parameter name and value
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String reason = ex.getMostSpecificCause().getMessage();
        return build(HttpStatus.BAD_REQUEST, msg("error.parameter.invalid", ex.getName(), ex.getValue(), reason));
    }

    /**
     * No endpoint matches the URL.
     *
     * @param ex The NoHandlerFoundException or NoResourceFoundException from Spring
     * @return HTTP 404
     */
    @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
    public ResponseEntity<ErrorResponse> handleNoResource(Exception ex) {
        return build(HttpStatus.NOT_FOUND, msg("error.resourceNotFound"));
    }

    /**
     * Last-resort handler so that clients never receive a stack trace or an HTML page.
     *
     * Authentication exceptions are rethrown: Spring Security's exception
     * translation must turn them into 401 responses through the entry point.
     *
     * @param ex Any exception not handled above
     * @return HTTP 500 and a generic message
     * @throws Exception the exception itself if it is an authentication exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) throws Exception {
        if (ex instanceof AuthenticationException) {
            throw ex;
        }
        log.error("Unexpected error while processing request", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, msg("error.unexpected"));
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ErrorResponse.of(status, message));
    }

    private String msg(String key, Object... args) {
        // Fall back to the key itself when no bundle entry is found.
        return messageSource.getMessage(key, args, key, LocaleContextHolder.getLocale());
    }
}
