# 놓친 보험금 청구 대행 시스템 (Green Ribbon Backend Assignment)

## 1. 실행 방법

### 환경 요구사항
- **Java**: JDK 17
- **Framework**: Spring Boot 3.x
- **Database**: H2 Database (In-Memory Mode)

### 실행 명령어
프로젝트 루트 경로에서 아래 명령어를 실행합니다.
```bash 
# Mac/Linux 
./gradlew bootRun 
# Windows 
./gradlew.bat bootRun
```
### 초기 데이터 (Auto Data Init)

애플리케이션 실행 시 `DataInitializer`가 동작하여 테스트를 위한 초기 데이터를 자동으로 생성합니다.

* **User**: 15명 (테스트 타겟: `userId=1` 채명정)
* **Hospital**: 10개 (테스트 타겟: `hospitalId=1` 세브란스병원)
* **UserTreatment**: 30건 이상의 랜덤 진료 기록 및 채명정님의 고정 진료 기록

### 접속 정보
- **H2 Console**: http://localhost:8080/h2-console
  - **JDBC URL**: `jdbc:h2:mem:green_ribbon;MODE=MySQL`
  - **User / Password**: `user` / `user`
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

---

## 2. 엔티티 설계 의도 (ERD)

### ERD 구조
```mermaid
erDiagram
    USERS ||--o{ USER_TREATMENT : "1:N"
    USERS ||--o{ PROXY_REQUEST : "1:N"
    HOSPITAL ||--o{ USER_TREATMENT : "1:N"
    
    PROXY_REQUEST ||--o{ PROXY_REQUEST_UNIT : "1:N (Cascade)"
    PROXY_REQUEST ||--o{ PROXY_REQUEST_HISTORY : "1:N"
    
    USER_TREATMENT ||--o{ PROXY_REQUEST_UNIT : "Referenced"

    USERS {
        long user_id PK
        string name
    }
    HOSPITAL {
        long hospital_id PK
        string name
    }
    USER_TREATMENT {
        long treatment_id PK
        string hospital_name "Snapshot"
        date treatment_date
        long amount
    }
    PROXY_REQUEST {
        long proxy_id PK
        string status "Enum (PENDING...)"
        string guarantee_type "Enum (NORMAL_PREPAID...)"
        long total_missed_amount
        long fee_amount
    }
    PROXY_REQUEST_UNIT {
        long unit_id PK
        long missed_amount "Snapshot"
    }
   ```
### 주요 설계 포인트

1. **ProxyRequest (Aggregate Root)**
* 청구 대행 신청의 주체입니다. `ProxyRequestUnit`과 `ProxyRequestHistory`의 생명주기를 `Cascade.ALL`과 `orphanRemoval`로 관리하여 데이터 무결성을 보장합니다.


2. **UserTreatment (진료기록 스냅샷)**
* 병원 정보가 변경되더라도 과거의 진료 사실은 변하지 않아야 하므로, `hospitalName`을 별도 컬럼으로 저장하여 불변성을 확보했습니다.


3. **ProxyRequestUnit (N:M 해소 & Snapshot)**
* 하나의 신청서에 여러 진료 기록이 포함될 수 있는 구조입니다. 신청 시점의 `missedAmount`(누락 금액)를 저장하여 추후 진료비 변동에 영향을 받지 않도록 설계했습니다.



---

## 3. 비즈니스 로직 배치 이유 (Entity vs Service)

본 프로젝트는 **도메인 주도 설계(DDD)**의 사상을 반영하여, 핵심 비즈니스 로직을 **Entity**에 응집시켰습니다.

### Entity (Rich Domain Model)

데이터 상태를 변경하는 **핵심 판단**은 엔티티가 직접 수행합니다.

* **상태 전이 제어**: `validateCanCancel()`과 같이 상태 변경 가능 여부를 판단하는 로직은 `ProxyRequest` 내부에 구현되어 있습니다.
* **데이터 캡슐화**: 상태(`status`)나 금액(`feeAmount`) 필드에 대한 무분별한 Setter를 막고, 의도가 명확한 메서드(`addUnit`, `updateStatus`)를 통해서만 데이터를 변경하도록 제한했습니다.

### Service (Application Layer)

서비스 계층은 **비즈니스 흐름을 조율(Orchestration)**하는 역할에 집중했습니다.

* **트랜잭션 관리**: `@Transactional`을 사용하여 작업의 원자성을 보장합니다.
* **중복 신청 방지**: `validateHospitalAvailability` 메서드를 통해 이미 처리된(COMPLETED, DISCLAIMER) 병원이 포함된 요청을 사전에 차단하는 정책 검증을 수행합니다.

---

## 4. 상태 전이 규칙 구현 방식

복잡한 상태 관리를 `if-else` 분기문이 아닌, **Enum 스스로가 제어**하도록 구현했습니다.

### 4.1 Smart Enum (State Machine)

`ProxyStatus` Enum 내부에 `allowedNextStates`(이동 가능한 다음 상태 목록)를 정의했습니다.

* **검증**: 상태 변경 요청 시 `currentStatus.canTransitionTo(nextStatus)`를 호출하여, 허용되지 않은 경로(예: `PENDING` -> `COMPLETED`)로의 변경을 원천 차단합니다.

### 4.2 자동 상태 전이 (선불 타입 처리)

* **로직**: '선불(Prepaid)' 보장 타입은 수수료 청구 단계 없이 바로 완료되어야 합니다.
* **구현**: `ProxyRequest.updateStatus` 메서드 내부에서 `GuaranteeType`이 선불이고 변경하려는 상태가 `FEE_CLAIM`인 경우, **자동으로 `COMPLETED`로 상태를 전이**시키고 이력을 남깁니다.

---

## 5. 고민했던 포인트와 해결 방법

### 1) 조회 성능 최적화와 불필요한 로딩 방지 (QueryDSL Projection)
* **Problem**: 다수의 테이블 조인이 필요한 신청 내역 조회 시, 일반적인 `Fetch Join`은 연관된 엔티티를 모두 영속성 컨텍스트에 올리기 때문에 메모리 효율이 떨어지고 불필요한 컬럼까지 조회하는 문제가 있었습니다.
* **Solution**: **QueryDSL의 DTO Projection**을 적용했습니다. 엔티티 전체를 조회하지 않고 화면에 필요한 데이터만 `Q-DTO`로 선별적으로 조회(`SELECT`)하여 성능을 최적화했습니다. 또한 `findAvailableTreatments`에서는 `JPAExpressions`를 활용한 서브쿼리로 복잡한 필터링 조건을 깔끔하게 해결했습니다.

### 2) 수수료 정책 관리 (Smart Enum vs Strategy Pattern)
* **Problem**: 보장 타입별로 상이한 수수료 계산 로직을 구현해야 했습니다. 확장성을 고려하면 **전략 패턴(Interface + Bean)**이 적합해 보였으나, 현재 요구사항인 단순 곱셈 연산을 위해 인터페이스와 여러 구현체 클래스를 만드는 것은 **과도한 엔지니어링(Over-engineering)**이라는 고민이 들었습니다.
* **Solution**: **Smart Enum** 방식을 채택했습니다. `GuaranteeType` Enum 내부에 데이터(요율)와 행위(계산 메서드)를 함께 응집시켜 코드의 복잡도를 낮추고 유지보수성을 높였습니다. (단, 추후 DB 조회 등 외부 의존성이 필요해질 경우 전략 패턴으로 리팩토링할 계획입니다.)

---

## 6. 테스트 curl

애플리케이션 실행 시 생성되는 **User ID: 1(채명정), Hospital ID: 1(세브란스병원)** 데이터를 기준으로 작성되었습니다.

#### 1. 유저 진료 기록 조회

유저 1이 가진 "신청 가능한" 진료 기록을 조회합니다.

```bash 
curl -X GET "http://localhost:8080/api/users/1/treatments?page=0&size=20" -H "accept: */*"
```
#### 2. 청구 대행 신청

유저 1이 병원 1(세브란스병원)의 진료 기록들에 대해 청구 대행을 신청합니다.
```bash 
curl -X POST "http://localhost:8080/api/proxy-requests" -H "Content-Type: application/json" -d "{ \"userId\": 1, \"guaranteeType\": \"NORMAL_POSTPAID\", \"hospitalIds\": [1] }"
```
#### 3. 청구 대행 목록 조회
```bash 
curl -X GET "http://localhost:8080/api/proxy-requests?userId=1" -H "accept: */*"
```
#### 4. 신청 상세 조회
```bash 
curl -X GET "http://localhost:8080/api/proxy-requests/1" -H "accept: */*"
```
#### 5. 상태 변경 (진행중)
관리자가 신청 건을 `IN_PROGRESS`(서류 수집 중)로 변경합니다.
```bash 
curl -X PATCH "http://localhost:8080/api/proxy-requests/1/status" -H "Content-Type: application/json" -d '{ "status": "IN_PROGRESS" }'
```
#### 6. 신청 취소 (실패 테스트)
`IN_PROGRESS` 상태에서는 취소가 불가능하므로 **400(4203) 에러(CANCEL_RESTRICTED)**가 발생해야 합니다.
```bash 
curl -X DELETE "http://localhost:8080/api/proxy-requests/1" -H "accept: */*"
```