package org.nguyennn.account_svc.adapter.in.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * A standardized API response format for all REST endpoints
 *
 * @param <T> The type of data contained in the response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        String status,
        String message,
        T data,
        Object errors,
        Instant timestamp
) {
    public ApiResponse(String status, String message, T data) {
        this(status, message, data, null, Instant.now());
    }

    public ApiResponse(String status, String message, T data, Object errors) {
        this(status, message, data, errors, Instant.now());
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "Operation completed successfully", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    public static ApiResponse<?> error(String message) {
        return new ApiResponse<>("error", message, null);
    }

    public static ApiResponse<?> error(String message, Object errors) {
        return new ApiResponse<>("error", message, null, errors);
    }

    public static ApiResponse<?> error(String message, Object errors, Instant timestamp) {
        return new ApiResponse<>("error", message, null, errors, timestamp);
    }
}
