package ch.sectioninformatique.template.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler is a centralized exception handling component for the
 * application.
 * 
 * This class uses Spring's @ControllerAdvice annotation to intercept exceptions
 * thrown
 * across the entire application and provide consistent, formatted error
 * responses to clients.
 * 
 * Benefits:
 * - Centralizes exception handling logic (DRY principle)
 * - Ensures consistent error response format across all endpoints
 * - Reduces boilerplate try-catch blocks in controllers
 * - Provides detailed error information for debugging
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Builds a standardized error response entity.
     * 
     * This helper method creates a consistent response format for all exceptions,
     * including timestamp, HTTP status code, error type, and detailed message.
     * 
     * @param status  The HTTP status code to return
     * @param message The error message to display to the client
     * @return ResponseEntity containing the formatted error response
     */
    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(
                Map.of(
                        "timestamp", LocalDateTime.now().toString(),
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", message));
    }

    // ========================================================================
    // Custom Application Exceptions
    // ========================================================================

    /**
     * Handles AppException - the custom application exception.
     * 
     * Returns the exception's status and message as-is, allowing developers
     * to customize responses directly when throwing exceptions.
     * 
     * @param ex The AppException instance containing status and message
     * @return ResponseEntity with the exception's status and message
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Object> handleAppException(AppException ex) {
        return buildResponse(ex.getStatus(), ex.getMessage());
    }

    // ========================================================================
    // Spring Framework Built-in Exceptions
    // ========================================================================

    /**
     * Handles AccessDeniedException - thrown when user lacks permission for an
     * action.
     * 
     * Returns HTTP 403 Forbidden with a user-friendly message indicating
     * that the user doesn't have permission to access the requested resource.
     * 
     * @param ex The AccessDeniedException thrown by Spring Security
     * @return ResponseEntity with HTTP 403 and permission denied message
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied: You do not have permission to access this resource");
    }

    /**
     * Handles MethodArgumentNotValidException - thrown when @Valid validation
     * fails.
     * 
     * Collects ALL validation errors (not just the first one) and returns them
     * in two formats:
     * 1. A combined message for backward compatibility
     * 2. A detailed fieldErrors map for granular client-side handling
     * 
     * Example response:
     * {
     * "message": "email: Invalid email format; age: Must be between 18 and 120",
     * "fieldErrors": {
     * "email": "Invalid email format",
     * "age": "Must be between 18 and 120"
     * }
     * }
     * 
     * @param ex The MethodArgumentNotValidException from Spring validation
     * @return ResponseEntity with HTTP 400 and all validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationErrors(MethodArgumentNotValidException ex) {
        // Collect ALL field errors, not just the first one
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

        // Create a single message combining all field errors for backward compatibility
        String combinedMessage = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((e1, e2) -> e1 + "; " + e2)
                .orElse("Validation failed");

        // Build response with both message and detailed fieldErrors
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", combinedMessage);
        response.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles HttpMediaTypeNotSupportedException - thrown when client sends
     * unsupported media type.
     * 
     * Occurs when request Content-Type is not supported by the endpoint
     * (e.g., sending XML when endpoint expects JSON).
     * 
     * @param ex The HttpMediaTypeNotSupportedException from Spring
     * @return ResponseEntity with HTTP 415 and unsupported media type message
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
    }

    /**
     * Handles MissingServletRequestParameterException - thrown when required
     * request parameter is missing.
     * 
     * Occurs when an endpoint requires a query parameter or form parameter that
     * wasn't provided.
     * 
     * Example: GET /api/search without required 'query' parameter
     * 
     * @param ex The MissingServletRequestParameterException from Spring
     * @return ResponseEntity with HTTP 400 and parameter name in error message
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getParameterName() + " parameter is missing");
    }

    /**
     * Handles HttpMessageNotReadableException - thrown when request body cannot be
     * parsed.
     * 
     * This typically occurs with malformed JSON, missing required fields, or type
     * mismatches.
     * This handler attempts to provide specific, actionable error messages based on
     * the
     * underlying parsing error, making debugging easier for clients.
     * 
     * Examples of specific messages:
     * - "JSON is incomplete - missing closing bracket or quote"
     * - "JSON contains invalid character - check for unescaped quotes or missing
     * commas"
     * - "Invalid value type for a field - check your data types match the schema"
     * - "Empty or missing request body"
     * 
     * @param ex The HttpMessageNotReadableException from Spring
     * @return ResponseEntity with HTTP 400 and specific parsing error guidance
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleMalformedJson(HttpMessageNotReadableException ex) {
        String message = "Malformed or missing JSON request body";

        // Extract more specific error information if available
        Throwable cause = ex.getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            // Provide more specific guidance based on the parsing error
            if (causeMessage != null) {
                if (causeMessage.contains("Unexpected end-of-input")) {
                    message = "JSON is incomplete - missing closing bracket or quote";
                } else if (causeMessage.contains("Unexpected character")) {
                    message = "JSON contains invalid character - check for unescaped quotes or missing commas";
                } else if (causeMessage.contains("cannot deserialize")) {
                    message = "Invalid value type for a field - check your data types match the schema";
                } else if (causeMessage.contains("No content to map")) {
                    message = "Empty or missing request body";
                }
            }
        }

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }
}
