package com.example.greenribboncalimassignment.web.controller;

import com.example.greenribboncalimassignment.common.response.ApiResponse;
import com.example.greenribboncalimassignment.service.proxy.ProxyRequestService;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02. 유저 (User)", description = "유저 정보 및 진료 기록 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserRestController {

    private final ProxyRequestService proxyRequestService;

    @Operation(summary = "3.5 유저 진료 기록 조회", description = "대행 신청이 가능한 유저의 진료 기록 목록을 조회합니다. (이미 신청된 건은 제외됨)")
    @GetMapping("/{userId}/treatments")
    public ResponseEntity<ApiResponse<Slice<ProxyRequestUnitResponse>>> getUserTreatments(
            @Parameter(description = "유저 ID", example = "1")
            @PathVariable Long userId,

            @Parameter(description = "페이징 정보 (기본값: 20개, 최신순)", hidden = true)
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Slice<ProxyRequestUnitResponse> treatments = proxyRequestService.getAvailableTreatments(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(treatments));
    }
}
