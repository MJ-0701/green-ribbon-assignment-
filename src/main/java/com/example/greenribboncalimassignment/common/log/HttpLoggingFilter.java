package com.example.greenribboncalimassignment.common.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // H2 Console 등 로깅 불필요한 경로 제외
        if (request.getRequestURI().startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Body를 여러 번 읽기 위해 래핑
        ContentCachingRequestWrapper wrappingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappingResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        // 다음 필터 실행
        filterChain.doFilter(wrappingRequest, wrappingResponse);

        long duration = System.currentTimeMillis() - startTime;

        // 로깅 수행
        String requestBody = new String(wrappingRequest.getContentAsByteArray(), StandardCharsets.UTF_8);
        String responseBody = new String(wrappingResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

        log.info("[HTTP] {} {} ({}ms) | Status: {}",
                request.getMethod(),
                request.getRequestURI(),
                duration,
                response.getStatus()
        );

        if (!requestBody.isBlank()) log.info("[REQ Body] {}", requestBody);
        if (!responseBody.isBlank()) log.info("[RES Body] {}", responseBody);

        wrappingResponse.copyBodyToResponse();
    }
}
