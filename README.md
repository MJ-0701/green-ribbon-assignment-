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