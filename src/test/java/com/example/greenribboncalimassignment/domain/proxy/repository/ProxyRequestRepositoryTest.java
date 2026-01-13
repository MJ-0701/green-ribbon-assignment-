package com.example.greenribboncalimassignment.domain.proxy.repository;

import com.example.greenribboncalimassignment.config.JpaConfig;
import com.example.greenribboncalimassignment.config.QueryDslConfig;
import com.example.greenribboncalimassignment.domain.hospital.entity.Hospital;
import com.example.greenribboncalimassignment.domain.proxy.entity.*;
import com.example.greenribboncalimassignment.domain.user.entity.UserTreatment;
import com.example.greenribboncalimassignment.domain.user.entity.Users;
import com.example.greenribboncalimassignment.web.dto.response.ProxyDetailResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;

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

    @Test
    @DisplayName("상세 조회 성공: 신청정보(Info), 병원내역(Units), 이력(Histories)이 한번에 조회되고 정렬된다.")
    void find_proxy_detail_success() throws InterruptedException {
        // given
        // 1. 유저 & 병원 세팅
        Users user = Users.of("채명정");
        em.persist(user);

        Hospital hospitalA = Hospital.of("서울병원");
        Hospital hospitalB = Hospital.of("경기병원");
        em.persist(hospitalA);
        em.persist(hospitalB);

        // 2. 진료 기록 세팅
        UserTreatment t1 = UserTreatment.of(user, hospitalA, LocalDate.now().minusDays(5), 10000L);
        UserTreatment t2 = UserTreatment.of(user, hospitalB, LocalDate.now().minusDays(1), 20000L);
        em.persist(t1);
        em.persist(t2);

        // 3. 청구 대행 신청서(Root) 생성
        ProxyRequest request = ProxyRequest.of(user, GuaranteeType.NORMAL_POSTPAID);
        // 테스트 편의상 필드 강제 주입 (총액 등) - 실제론 addUnit으로 계산됨
        // 여기선 조회 쿼리 매핑만 볼 것이므로 연관관계만 잘 맺으면 됨
        ProxyRequestUnit unit1 = ProxyRequestUnit.builder().userTreatment(t1).missedAmount(10000L).build();
        ProxyRequestUnit unit2 = ProxyRequestUnit.builder().userTreatment(t2).missedAmount(20000L).build();

        request.addUnit(unit1);
        request.addUnit(unit2);
        em.persist(request);

        // 4. 이력(History) 생성 - 정렬 확인을 위해 시간차를 두고 2개 저장
        ProxyRequestHistory history1 = ProxyRequestHistory.create(request, null, ProxyStatus.PENDING, "최초 신청");
        em.persist(history1);

        // 시간차 부여 (OS마다 정밀도가 다를 수 있어 안전하게 sleep)
        Thread.sleep(100);

        ProxyRequestHistory history2 = ProxyRequestHistory.create(request, ProxyStatus.PENDING, ProxyStatus.IN_PROGRESS, "처리 시작");
        em.persist(history2);

        em.flush();
        em.clear(); // 쿼리가 실제로 나가는지 확인하기 위해 영속성 컨텍스트 초기화

        // when
        ProxyDetailResponse result = proxyRequestRepository.findProxyDetail(request.getId());

        // then
        // 1. Info 검증
        assertThat(result).isNotNull();
        assertThat(result.proxyInfo().userName()).isEqualTo("채명정");
        assertThat(result.proxyInfo().guaranteeType()).isEqualTo(GuaranteeType.NORMAL_POSTPAID);
        assertThat(result.proxyInfo().status()).isEqualTo(ProxyStatus.PENDING); // 초기상태

        // 2. Units 검증 (2건)
        assertThat(result.units()).hasSize(2);
        assertThat(result.units())
                .extracting("hospitalName")
                .containsExactlyInAnyOrder("서울병원", "경기병원");

        // 3. Histories 검증 (2건, 최신순 정렬)
        assertThat(result.histories()).hasSize(2);
        assertThat(result.histories().get(0).reason()).isEqualTo("처리 시작"); // 나중에 넣은게 먼저 나와야 함 (DESC)
        assertThat(result.histories().get(0).status()).isEqualTo(ProxyStatus.IN_PROGRESS);

        assertThat(result.histories().get(1).reason()).isEqualTo("최초 신청");
        assertThat(result.histories().get(1).status()).isEqualTo(ProxyStatus.PENDING);
    }

    @Test
    @DisplayName("상세 조회 실패: 존재하지 않는 ID로 조회 시 null을 반환한다.")
    void find_proxy_detail_fail_not_found() {
        // given
        Long invalidId = 9999L;

        // when
        ProxyDetailResponse result = proxyRequestRepository.findProxyDetail(invalidId);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("목록 조회 성공: 특정 유저의 신청 내역만 최신순으로 조회된다.")
    void find_all_by_user_id_success() throws InterruptedException {
        // given
        // 1. 유저 2명 생성 (데이터 격리 확인용)
        Users userA = Users.of("채명정");
        Users userB = Users.of("홍길동");
        em.persist(userA);
        em.persist(userB);

        // 2. UserA의 신청서 2개 생성 (시간차를 두고 생성)
        ProxyRequest requestA1 = ProxyRequest.of(userA, GuaranteeType.NORMAL_POSTPAID);
        em.persist(requestA1);

        // 정렬 테스트를 위해 시간차 부여
        Thread.sleep(50);

        ProxyRequest requestA2 = ProxyRequest.of(userA, GuaranteeType.NORMAL_PREPAID);
        em.persist(requestA2);

        // 3. UserB의 신청서 1개 생성 (조회되면 안 됨)
        ProxyRequest requestB1 = ProxyRequest.of(userB, GuaranteeType.NORMAL_POSTPAID);
        em.persist(requestB1);

        em.flush();
        em.clear();

        // when
        List<ProxyDetailResponse.ProxyInfoDto> result = proxyRequestRepository.findAllByUserId(userA.getId());

        // then
        // 1. 개수 검증 (UserA의 것만 2개여야 함)
        assertThat(result).hasSize(2);

        // 2. 정렬 검증 (나중에 만든 A2가 먼저 나와야 함 - 내림차순)
        assertThat(result.get(0).proxyRequestId()).isEqualTo(requestA2.getId());
        assertThat(result.get(0).guaranteeType()).isEqualTo(GuaranteeType.NORMAL_PREPAID);

        assertThat(result.get(1).proxyRequestId()).isEqualTo(requestA1.getId());
        assertThat(result.get(1).guaranteeType()).isEqualTo(GuaranteeType.NORMAL_POSTPAID);

        // 3. 필터링 검증 (UserB의 데이터는 없어야 함)
        boolean hasUserB = result.stream()
                .anyMatch(dto -> dto.userName().equals("홍길동"));
        assertThat(hasUserB).isFalse();
    }
}