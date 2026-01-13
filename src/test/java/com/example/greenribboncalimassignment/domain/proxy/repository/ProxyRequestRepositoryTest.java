package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.config.JpaConfig;
import com.example.greenribboncalimassignment.config.QueryDslConfig;
import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class})
class ProxyRequestRepositoryTest {

    @Autowired
    private ProxyRequestRepository proxyRequestRepository;

    @Autowired
    private EntityManager em;

    private Users user;

    @BeforeEach
    void setUp() {
        user = Users.of("채명정");
        em.persist(user);
    }

    @Test
    @DisplayName("진행 중 확인(True): PENDING 상태인 신청 건이 있으면 True를 반환한다.")
    void exists_ongoing_request_true() {
        // given
        ProxyRequest request = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        // 초기 상태 PENDING
        em.persist(request);

        // when
        boolean exists = proxyRequestRepository.existsOngoingRequest(user.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("진행 중 확인(False): COMPLETED(종결) 상태인 신청 건만 있으면 False를 반환한다.")
    void exists_ongoing_request_false() {
        // given
        ProxyRequest request = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        request.updateStatus(ProxyStatus.IN_PROGRESS);
        request.updateStatus(ProxyStatus.FEE_CLAIM);
        request.updateStatus(ProxyStatus.COMPLETED); // 종결 처리
        em.persist(request);

        // when
        boolean exists = proxyRequestRepository.existsOngoingRequest(user.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("진행 중 확인(False): CANCELLED(취소) 상태인 신청 건만 있으면 False를 반환한다.")
    void exists_ongoing_request_false_cancelled() {
        // given
        ProxyRequest request = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        request.updateStatus(ProxyStatus.CANCELLED); // 취소 처리
        em.persist(request);

        // when
        boolean exists = proxyRequestRepository.existsOngoingRequest(user.getId());

        // then
        assertThat(exists).isFalse();
    }
}