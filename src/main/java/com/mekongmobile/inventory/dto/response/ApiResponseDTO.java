package com.mekongmobile.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Cấu trúc Response chuẩn hóa toàn hệ thống theo api-contract.md (Mục 1.3)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {

    private boolean success;
    private T data;
    private MetaDTO meta;
    private ErrorDTO error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponseDTO<T> ok(T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponseDTO<T> ok(T data, MetaDTO meta) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .data(data)
                .meta(meta)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponseDTO<T> fail(String code, String message, Object details) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .error(new ErrorDTO(code, message, details))
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetaDTO {
        private int page;
        private int limit;
        private long total;
        private int totalPages;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorDTO {
        private String code;
        private String message;
        private Object details;
    }
}
