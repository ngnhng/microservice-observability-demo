package org.nguyennn.account_svc.adapter.in.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents detailed error information for API responses
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorDetails(
        String code,
        String source,
        String detail,
        List<FieldError> fieldErrors
) {
    public ErrorDetails(@Nonnull String code,
                        @Nonnull String source,
                        @Nullable String detail,
                        @Nullable List<FieldError> fieldErrors) {
        this.code = code;
        this.source = source;
        this.detail = detail;
        this.fieldErrors = fieldErrors != null ? fieldErrors : new ArrayList<>();
    }

    public ErrorDetails(@Nonnull String code, @Nonnull String source, @Nullable String detail) {
        this(code, source, detail, new ArrayList<>());
    }

    public void addFieldError(@Nonnull String field, @Nonnull String message) {
        this.fieldErrors.add(new FieldError(field, message));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record FieldError(String field, String message) {
    }
}
