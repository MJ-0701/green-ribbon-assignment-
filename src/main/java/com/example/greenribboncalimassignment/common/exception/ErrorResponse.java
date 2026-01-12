package com.example.greenribboncalimassignment.common.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.List;

@Getter
@Builder
public class ErrorResponse {

    private String code;
    private String message;
    private List<ValidationError> errors; // @Valid 검증 에러 상세

    // 일반 에러 응답
    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    // Validation 에러 응답
    public static ErrorResponse of(String code, String message, BindingResult bindingResult) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .errors(ValidationError.of(bindingResult))
                .build();
    }

    @Getter
    @Builder
    public static class ValidationError {
        private String field;
        private String value;
        private String reason;

        public static List<ValidationError> of(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> ValidationError.builder()
                            .field(error.getField())
                            .value(String.valueOf(error.getRejectedValue()))
                            .reason(error.getDefaultMessage())
                            .build())
                    .toList();
        }
    }
}
