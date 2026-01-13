package com.example.greenribboncalimassignment.service.proxy;

import com.example.greenribboncalimassignment.common.exception.BusinessException;
import com.example.greenribboncalimassignment.common.response.ResultCode;
import com.example.greenribboncalimassignment.domain.user.repository.UserTreatmentRepository;
import com.example.greenribboncalimassignment.domain.user.repository.UsersRepository;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ProxyRequestServiceTest {

    @InjectMocks
    private ProxyRequestService proxyRequestService;

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private UserTreatmentRepository userTreatmentRepository;

    @Test
    @DisplayName("진료기록 조회 성공: 유저가 존재하면 Repository를 호출하여 결과를 반환한다.")
    void get_available_treatments_success() {
        // given
        Long userId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);

        given(usersRepository.existsById(userId)).willReturn(true); // 유저 존재함

        Slice<ProxyRequestUnitResponse> expectedSlice = new SliceImpl<>(List.of());
        given(userTreatmentRepository.findAvailableTreatments(userId, pageable))
                .willReturn(expectedSlice);

        // when
        Slice<ProxyRequestUnitResponse> result = proxyRequestService.getAvailableTreatments(userId, pageable);

        // then
        assertThat(result).isEqualTo(expectedSlice);
        then(userTreatmentRepository).should().findAvailableTreatments(userId, pageable); // 호출 확인
    }

    @Test
    @DisplayName("진료기록 조회 실패: 유저가 존재하지 않으면 USER_NOT_FOUND 예외가 발생한다.")
    void get_available_treatments_fail_user_not_found() {
        // given
        Long userId = 999L;
        PageRequest pageable = PageRequest.of(0, 10);

        given(usersRepository.existsById(userId)).willReturn(false); // 유저 없음

        // when & then
        assertThatThrownBy(() -> proxyRequestService.getAvailableTreatments(userId, pageable))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("resultCode", ResultCode.USER_NOT_FOUND);

        // Repository는 호출되지 않아야 함
        then(userTreatmentRepository).shouldHaveNoInteractions();
    }
}