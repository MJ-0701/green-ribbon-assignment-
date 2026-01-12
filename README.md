# 놓친 보험금 청구 대행 시스템 (Green Ribbon Backend Assignment)

## 1. 프로젝트 개요
사용자의 진료 기록을 기반으로 놓친 보험금을 계산하고, 청구 대행 신청을 처리하는 REST API 시스템입니다.

### 🛠 기술 스택
- **Java 17**
- **Spring Boot 3.x**
- **JPA (Hibernate)**
- **QueryDSL 5.x**
- **H2 Database**

---

## 2. 도메인 및 엔티티 설계 의도 (Domain Modeling)

본 프로젝트는 비즈니스 로직의 응집도를 높이고 유지보수성을 확보하기 위해 **도메인 주도 설계(DDD)**의 사상을 일부 차용하여 패키지 구조와 엔티티를 설계했습니다.

### 2.1 패키지 구조
기능 단위가 아닌 **도메인 단위**로 패키지를 분리하여 비즈니스 관심사를 명확히 했습니다.
- `domain.user`: 사용자 및 사용자의 하위 데이터(진료 기록) 관리
- `domain.hospital`: 병원 마스터 데이터 관리
- `domain.proxy`: 핵심 비즈니스인 청구 대행(신청, 대행 단위) 관리

### 2.2 엔티티 설계 핵심 전략
1.  **Setter 사용 지양 & Builder 패턴 적용**: 불완전한 객체 생성을 막고, 객체의 일관성을 유지하기 위해 무분별한 Setter 사용을 제한했습니다.
2.  **생성자 접근 제어(`PROTECTED`)**: JPA 프록시 생성을 허용하되, 외부에서의 무분별한 `new` 생성을 방지하여 팩토리 메서드나 빌더 사용을 강제했습니다.
3.  **Rich Domain Model**: 비즈니스 로직(수수료 계산, 상태 변경 검증, 하위 엔티티 관리)을 서비스 계층이 아닌 **엔티티와 Enum 내부**로 가져와 객체지향적인 설계를 지향했습니다.

### 2.3 주요 엔티티 설계 상세

#### `UserTreatment` (유저 진료 기록)
- **설계 의도**: 진료 기록은 시간이 지나도 변하지 않아야 하는 **과거의 기록**입니다. 병원 정보가 바뀌더라도 당시의 기록은 보존되어야 하며, 동시에 유효한 병원 데이터와 연결되어야 합니다.
- **하이브리드 전략 (FK + Snapshot)**:
  - **무결성(Integrity)**: `Hospital` 엔티티와 **ManyToOne(FK) 관계**를 맺어 참조 무결성을 보장하고, 객체 그래프 탐색을 통해 최신 병원 정보에 접근 가능하도록 설계했습니다.
  - **이력 보존(History)**: 병원 이름이 변경되더라도 진료 당시의 기록이 왜곡되지 않도록 `hospitalName` 컬럼을 별도로 두어 **스냅샷(Snapshot)** 형태로 저장했습니다.

#### `ProxyRequest` (청구 대행 신청 - Aggregate Root)
- **역할**: 청구 대행 도메인의 **Aggregate Root**입니다. 하위 엔티티인 `ProxyRequestUnit`의 생명주기를 관리(`CascadeType.ALL`)하며, 대행 단위가 추가될 때마다 총 금액과 수수료를 재계산하는 비즈니스 메서드를 포함합니다.
- **연관관계**: `User`와 `N:1` 관계를 맺고 있으며, 상태 변경에 대한 최종 권한을 가집니다.

#### `ProxyRequestUnit` (병원별 대행 단위)
- **설계 의도**: 개별 병원에 대한 신청 내역을 관리합니다. 정규화(Normalization)를 위해 별도의 `hospitalId` 컬럼을 두지 않고, 원본 데이터인 `UserTreatment`를 참조하도록 설계했습니다.
- **데이터 추적성**: `UserTreatment`와 `ManyToOne` 관계를 맺음으로써, 해당 대행 신청이 "어떤 진료 기록"에 기반했는지 명확히 추적할 수 있으며, 병원 정보는 `unit.getUserTreatment().getHospital()`을 통해 참조합니다.

---

## 3. 비즈니스 로직 구현 전략

### 3.1 상태 전이(State Transition) 규칙 구현
복잡한 상태 변경 규칙(`PENDING` -> `IN_PROGRESS` 등)을 Service 계층의 `if-else` 문으로 처리할 경우 코드가 산재되어 유지보수가 어려워집니다. 이를 해결하기 위해 **Smart Enum** 패턴을 적용했습니다.

- **Smart Enum (`ProxyStatus`)**: 각 상태(Enum)가 자신이 이동 가능한 `allowedNextStates()` 목록을 직접 정의합니다.
- **검증 위임**: `ProxyRequest` 엔티티는 상태 변경 요청이 오면 `currentStatus.canTransitionTo(nextStatus)`를 호출하여 유효성을 검증합니다.

```java
// 예시: PENDING 상태는 IN_PROGRESS, CANCELLED 등으로만 이동 가능
PENDING("대행 신청 완료") {
    @Override
    public List<ProxyStatus> allowedNextStates() {
        return List.of(IN_PROGRESS, CANCELLED, DISCLAIMER);
    }
}
```

### 3.2 DB 매핑과 타입 안전성 (AttributeConverter)
- **요구사항**: DB에는 운영 및 데이터 확인의 용이성을 위해 한글 Description("대행 신청 완료", "일반 선불")이 저장되어야 합니다.
- **구현**: `AttributeConverter`를 활용하여 Java 코드에서는 **Enum의 타입 안전성**을 누리고, DB에는 **한글 설명**이 저장되도록 구현했습니다.

---

## 4. 시스템 아키텍처 및 공통 모듈 설계 (Common & Core)

안정적인 서비스 운영과 프론트엔드와의 원활한 협업을 위해 공통 관심사(Cross-cutting Concerns)를 모듈화했습니다.

### 4.1 표준 API 응답 전략 (Standardized API Response)
HTTP Status Code에 의존하는 것을 넘어, 비즈니스 로직의 성공/실패 여부를 명확히 전달하기 위해 **Soft 200 전략**을 채택했습니다.
- **구조**: 모든 응답은 `ApiResponse<T>` 래퍼 객체로 반환됩니다.
- **Status & Code 분리**:
  - `status` (int): `200`, `9999` 등 구체적인 상태 코드 (프론트엔드 분기 처리용)
  - `code` (String): `SUCCESS`, `SERVER_ERROR` 등 가독성 있는 식별 코드
- **이점**: 클라이언트는 항상 JSON 포맷을 보장받으며, `status` 필드만으로 에러 핸들링 로직을 일원화할 수 있습니다.

### 4.2 전역 예외 처리 (Global Exception Handling)
`@RestControllerAdvice`를 활용하여 예외를 중앙 집중적으로 관리합니다.
- **계층화된 예외 전략**:
  - **Service Layer**: 트랜잭션 롤백을 위해 `RuntimeException` 기반의 커스텀 예외(`BusinessException`)를 발생시킵니다.
  - **Web Layer**: `Exception.class`까지 포괄적으로 잡아내어, 예상치 못한 Checked Exception이 발생하더라도 클라이언트에게는 항상 약속된 JSON 포맷을 반환합니다.

### 4.3 관측 가능성 확보 (Observability & Logging)
운영 환경에서의 디버깅 효율성을 높이기 위해 로깅 시스템을 강화했습니다.
- **MDC (Mapped Diagnostic Context)**: 필터 단에서 요청마다 고유한 `UUID(Trace ID)`를 발급하여, 멀티 스레드 환경에서도 로그의 흐름을 완벽하게 추적합니다.
- **Content Caching**: `ContentCachingRequestWrapper`를 사용하여 `InputStream` 소실 없이 Request/Response Body 전체를 로깅하여 이슈 발생 시 원인을 즉각 파악할 수 있도록 했습니다.