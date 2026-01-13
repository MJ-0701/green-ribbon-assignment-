package com.example.greenribboncalimassignment.web.controller;

import com.example.greenribboncalimassignment.common.response.ResultCode;
import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.service.proxy.ProxyRequestService;
import com.example.greenribboncalimassignment.web.dto.request.ProxyCreateRequest;
import com.example.greenribboncalimassignment.web.dto.response.ProxyCreateResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProxyRequestRestController.class)
class ProxyRequestRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProxyRequestService proxyRequestService;

    @Test
    @DisplayName("청구 대행 신청 API 호출 성공")
    void create_proxy_request_success() throws Exception {
        // given
        ProxyCreateRequest request = new ProxyCreateRequest(
                1L,
                GuaranteeType.NORMAL_POSTPAID,
                List.of(101L, 102L)
        );

        ProxyCreateResponse response = new ProxyCreateResponse(
                1L,
                ProxyStatus.PENDING,
                30000L,
                6000L
        );

        given(proxyRequestService.createProxyRequest(any(ProxyCreateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/proxy-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // 테스트 로그 출력
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.proxyRequestId").value(1L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        then(proxyRequestService).should().createProxyRequest(any(ProxyCreateRequest.class));
    }

    @Test
    @DisplayName("유효성 검증 실패: 병원 ID가 비어있으면 400 에러와 함께 구체적인 에러 응답(INVALID_INPUT_VALUE)을 반환한다.")
    void create_proxy_request_fail_validation() throws Exception {
        // given
        // hospitalIds가 빈 리스트 -> DTO의 @NotEmpty 위반
        ProxyCreateRequest invalidRequest = new ProxyCreateRequest(
                1L,
                GuaranteeType.NORMAL_POSTPAID,
                List.of()
        );

        // when & then
        mockMvc.perform(post("/api/proxy-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print()) // 로그 확인
                .andExpect(status().isBadRequest()) // HTTP 400 확인

                // GlobalExceptionHandler가 잡아낸 에러 코드 확인
                .andExpect(jsonPath("$.code").value(ResultCode.INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCode.INVALID_INPUT_VALUE.getMessage()))

                // 구체적으로 어떤 필드에서 에러가 났는지 확인
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("hospitalIds"));
    }
}