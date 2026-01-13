package com.example.greenribboncalimassignment.domain.user.repository;

import com.example.greenribboncalimassignment.config.JpaConfig;
import com.example.greenribboncalimassignment.config.QueryDslConfig;
import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.proxy.entity.GuaranteeType;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequest;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyRequestUnit;
import com.example.greenribboncalimassignment.domain.proxy.entity.ProxyStatus;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import com.example.greenribboncalimassignment.web.dto.response.ProxyRequestUnitResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class}) // QueryDSL Config 주입
class UserTreatmentRepositoryTest {

    @Autowired
    private UserTreatmentRepository userTreatmentRepository;

    @Autowired
    private EntityManager em;

    private Users user;
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        // 기본 데이터 세팅
        hospital = Hospital.of("테스트병원");
        em.persist(hospital);

        user = Users.of("채명정");
        em.persist(user);
    }

    @Test
    @DisplayName("신청 가능 목록 조회: 이미 신청된 건은 제외하고, 취소된 건과 미신청 건만 조회된다.")
    void find_available_treatments_filter_exists() {
        // given
        // 1. [미신청] 진료기록 (조회 되어야 함)
        UserTreatment t1_clean = UserTreatment.of(user, hospital, LocalDate.now(), 10000L);
        em.persist(t1_clean);

        // 2. [신청 완료 - PENDING] 진료기록 (조회 안 되어야 함 - 필터링 대상)
        UserTreatment t2_pending = UserTreatment.of(user, hospital, LocalDate.now(), 20000L);
        em.persist(t2_pending);
        createProxyRequest(user, t2_pending, ProxyStatus.PENDING);

        // 3. [신청 취소 - CANCELLED] 진료기록 (조회 되어야 함 - 재신청 가능)
        UserTreatment t3_cancelled = UserTreatment.of(user, hospital, LocalDate.now(), 30000L);
        em.persist(t3_cancelled);
        createProxyRequest(user, t3_cancelled, ProxyStatus.CANCELLED);

        em.flush();
        em.clear(); // 영속성 컨텍스트 초기화 (실제 DB 조회 발생 유도)

        // when
        Slice<ProxyRequestUnitResponse> result = userTreatmentRepository.findAvailableTreatments(user.getId(), PageRequest.of(0, 10));

        // then
        List<ProxyRequestUnitResponse> content = result.getContent();

        assertThat(content).hasSize(2); // t1, t3 만 나와야 함

        // DTO 필드 검증
        assertThat(content).extracting("treatmentId")
                .containsExactlyInAnyOrder(t1_clean.getId(), t3_cancelled.getId());

        assertThat(content).extracting("treatmentId")
                .doesNotContain(t2_pending.getId()); // PENDING 상태인 t2는 없어야 한다.
    }


    // --- Helper Method ---
    private void createProxyRequest(Users user, UserTreatment treatment, ProxyStatus status) {
        // 1. 보장 타입 선택 (예: 일반 후불)
        GuaranteeType guaranteeType = GuaranteeType.NORMAL_POSTPAID;

        // 2. 도메인 로직을 이용해 수수료 계산
        long calculatedFee = guaranteeType.calculateFee(treatment.getAmount());

        // 3. Entity 생성 (계산된 수수료 주입)
        ProxyRequest request = ProxyRequest.builder()
                .user(user)
                .status(status)
                .guaranteeType(guaranteeType)
                .feeAmount(calculatedFee)
                .totalMissedAmount(treatment.getAmount())
                .build();

        em.persist(request);

        ProxyRequestUnit unit = ProxyRequestUnit.builder()
                .proxyRequest(request)
                .userTreatment(treatment)
                .missedAmount(treatment.getAmount())
                .build();

        em.persist(unit);
    }
}