package org.nguyennn.account_svc.adapter.in.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Generic pagination container for API responses
 *
 * @param <T> The type of items in the paginated response
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Paginated<T>(
        @NotNull(message = "Items list cannot be null")
        @Valid
        List<T> items,
        @NotNull(message = "Metadata cannot be null")
        Metadata metadata) {

    public record Metadata(
            @NotNull(message = "Pagination information is required")
            @Valid
            PaginationInfo pagination) {
    }

    public record PaginationInfo(
            @PositiveOrZero(message = "Offset cannot be negative")
            long offset,
            @Min(value = 1, message = "Limit must be at least 1")
            int limit,
            @PositiveOrZero(message = "Total count cannot be negative")
            long totalCount,
            @Min(value = 1, message = "Current page must be at least 1")
            int currentPage,
            @Min(value = 1, message = "Page count must be at least 1")
            int pageCount,
            @PositiveOrZero(message = "Previous offset cannot be negative")
            Long previousOffset,
            @PositiveOrZero(message = "Next offset cannot be negative")
            Long nextOffset
    ) {

        public PaginationInfo(long offset, int limit, long totalCount) {
            this(
                    offset,
                    limit,
                    totalCount,
                    limit > 0 ? (int) (offset / limit) + 1 : 1,
                    limit > 0 ? (int) Math.ceil((double) totalCount / limit) : 1,
                    offset > 0 ? Math.max(0, offset - limit) : null,
                    offset + limit < totalCount ? offset + limit : null
            );
        }
    }
}