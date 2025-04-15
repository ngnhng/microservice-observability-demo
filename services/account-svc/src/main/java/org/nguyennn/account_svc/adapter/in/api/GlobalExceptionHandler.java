package org.nguyennn.account_svc.adapter.in.api;

import io.quarkus.logging.Log;
import jakarta.annotation.Nonnull;
import jakarta.ws.rs.NotFoundException;
import org.nguyennn.account_svc.application.exceptions.AccountNotFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Global exception handler for REST endpoints
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    @Nonnull
    public Response toResponse(@Nonnull Exception exception) {
        return switch (exception) {
            case NotFoundException notFoundException ->
                    buildErrorResponse(Response.Status.NOT_FOUND, notFoundException.getMessage(), "route.not.found");
            case AccountNotFoundException ignored ->
                    buildErrorResponse(Response.Status.NOT_FOUND, exception.getMessage(), "entity.not.found");
            case ConstraintViolationException constraintViolationException ->
                    handleValidationException(constraintViolationException);
            case IllegalArgumentException ignored ->
                    buildErrorResponse(Response.Status.BAD_REQUEST, exception.getMessage(), "invalid.argument");
            default -> {
                Log.error("Unexpected error occurred", exception);
                yield buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "internal.error");
            }
        };
    }

    @Nonnull
    private Response buildErrorResponse(@Nonnull Response.Status status, @Nonnull String message, @Nonnull String code) {
        ErrorDetails errorDetails = new ErrorDetails(code, "server", message);
        ApiResponse<?> errorResponse = ApiResponse.error(message, errorDetails);
        return Response.status(status).entity(errorResponse).build();
    }

    @Nonnull
    private Response handleValidationException(ConstraintViolationException ex) {
        ErrorDetails errorDetails = new ErrorDetails("validation.error", "request", "Validation failed");

        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            // Extract just the field name from the property path
            String fieldName = propertyPath.contains(".") ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : propertyPath;

            errorDetails.addFieldError(fieldName, violation.getMessage());
        }

        ApiResponse<?> errorResponse = ApiResponse.error("Validation error", errorDetails);
        return Response.status(Response.Status.BAD_REQUEST).entity(errorResponse).build();
    }
}