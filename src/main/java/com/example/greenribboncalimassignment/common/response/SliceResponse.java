package com.example.greenribboncalimassignment.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Slice;

import java.util.List;

@Schema(description = "무한 스크롤(Slice) 공통 응답")
public record SliceResponse<T>(
        @Schema(description = "조회된 데이터 목록")
        List<T> content,

        @Schema(description = "현재 페이지 번호 (0부터 시작)")
        int currentPage,

        @Schema(description = "페이지 크기")
        int size,

        @Schema(description = "다음 페이지 존재 여부 (true: 더 있음, false: 끝)")
        boolean hasNext
) {
    public static <T> SliceResponse<T> from(Slice<T> slice) {
        return new SliceResponse<>(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext()
        );
    }
}
