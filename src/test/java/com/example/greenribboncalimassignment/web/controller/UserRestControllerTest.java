package com.example.greenribboncalimassignment.web.controller;

import com.example.greenribboncalimassignment.common.response.SliceResponse;
import com.example.greenribboncalimassignment.service.proxy.ProxyRequestService;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRestController.class) // Controller만 로드
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProxyRequestService proxyRequestService;

    @Test
    @DisplayName("유저 진료 기록 조회 API 호출 성공")
    void get_user_treatments_success() throws Exception {
        // given
        Long userId = 1L;
        SliceResponse<ProxyRequestUnitResponse> emptySlice = SliceResponse.from(new SliceImpl<>(List.of()));

        // Service가 호출되면 빈 Slice를 반환하도록 설정
        given(proxyRequestService.getAvailableTreatments(eq(userId), any(Pageable.class)))
                .willReturn(emptySlice);

        // when & then
        mockMvc.perform(get("/api/users/{userId}/treatments", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").exists());
    }
}