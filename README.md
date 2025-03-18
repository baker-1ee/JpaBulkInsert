# Hibernate/JPA Batch Insert/Update

## 1. 개요

이 문서는 Hibernate/JPA를 사용하여 엔터티를 batch insert 및 batch update 해야 하는 이유와 방법에 대해 정리하였습니다.

batch insert/update는 여러 개의 SQL 문을 한 번의 네트워크 호출로 데이터베이스에 전송하여 애플리케이션의 네트워크 및 메모리 사용량을 최적화하는 기법입니다.

우선 Oracle DB를 사용하는 경우를 먼저 알아보고, MySQL DB를 사용하는 경우도 알아보겠습니다.

## 2. 들어가기 전에

### 2.1. 샘플 데이터 모델

아래는 예제에서 사용할 데이터 모델입니다.

대출 실행 되어 Loan 엔티티가 생성될 때, 대출 개월수 만큼의 LoanSchedule 엔티티가 생성됩니다.

Loan : LoanSchedule = 1 : N 의 부모-자식 관계이며, 연관관계 매핑되어 있습니다.

엔티티의 ID 생성 전략은 Oracle DB 에서 일반적으로 사용하는 시퀀스 전략입니다.

#### `Loan(대출)` 엔티티

```java

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "LOAN")
@SequenceGenerator(name = "LOAN_SQ_GEN", sequenceName = "SQ_LOAN", allocationSize = 1)
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOAN_SQ_GEN")
    @Column(name = "ID", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private LoanType type;

    @Column(name = "AMOUNT", nullable = false, scale = 25)
    private BigDecimal amount;

    @Column(name = "INTEREST_RATE", nullable = false, scale = 2, precision = 3)
    private BigDecimal interestRate;

    @Column(name = "DURATION_MONTHS", nullable = false)
    private int durationMonths;

    @Builder.Default
    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LoanSchedule> loanSchedules = new ArrayList<>();

    // Loan 생성 시, LoanSchedule 까지 생성
    public static Loan createCreditLoan(LoanSaveVo vo) {
        Loan loan = Loan.builder()
                .type(LoanType.CREDIT_LOAN)
                .amount(vo.getAmount())
                .interestRate(vo.getInterestRate())
                .durationMonths(vo.getDurationMonths())
                .build();
        loan.generateRepaymentSchedule();
        return loan;
    }

    // 대출 실행 시 월별 상환 일정 자동 생성
    private void generateRepaymentSchedule() {
        BigDecimal monthlyPayment = calculateMonthlyPayment();
        IntStream.rangeClosed(1, durationMonths)
                .mapToObj(month -> LoanSchedule.of(this, month, monthlyPayment))
                .forEach(loanSchedules::add);
    }

    // 대출 월 상환금 계산 (원금/기간) + (월 이자)
    private BigDecimal calculateMonthlyPayment() {
        return amount.divide(valueOf(durationMonths), 5, HALF_UP)
                .add(amount.multiply(interestRate
                        .divide(valueOf(100), 5, HALF_UP)
                        .divide(valueOf(12), 5, HALF_UP)));
    }
}
```

#### `LoanSchedule(대출상환일정)` 엔터티

```java

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "LOAN_SCHEDULE")
@SequenceGenerator(name = "LOAN_SCHEDULE_SQ_GEN", sequenceName = "SQ_LOAN_SCHEDULE", allocationSize = 1)
public class LoanSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOAN_SCHEDULE_SQ_GEN")
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "LOAN_ID", nullable = false)
    private Loan loan;

    @Column(name = "INSTALLMENT_NO", nullable = false)
    private int installmentNo;

    @Column(name = "PAYMENT_AMOUNT", nullable = false)
    private BigDecimal paymentAmount;

    // 대출 상환 일정 생성 팩토리 메서드
    public static LoanSchedule of(Loan loan, int installmentNo, BigDecimal paymentAmount) {
        return LoanSchedule.builder()
                .loan(loan)
                .installmentNo(installmentNo)
                .paymentAmount(paymentAmount)
                .build();
    }
}
```

대출 실행 서비스입니다.

Loan 엔티티가 LoanSchedule 엔티티의 List를 CascadeType.ALL 필드로 가지고 있기 때문에 Loan 엔티티가 저장될 때, 자식 엔티티인 LoanScheudle 엔티티도 저장 됩니다.

```java

@Service
@RequiredArgsConstructor
public class LoanExecuteService {

    private final LoanRepository loanRepository;

    @Transactional
    public Loan executeCreditLoan(LoanSaveVo vo) {
        Loan loan = Loan.createCreditLoan(vo);
        return loanRepository.save(loan);
    }

}
```

대출 실행 서비스의 테스트 코드입니다.

Loan 엔티티 2개를 생성하여 하나의 트랜잭션에서 신규 생성하여 저장합니다.

첫번째 Loan 엔티티의 대출 개월수가 3개월이므로 LoanSchedule 엔티티 3개가 저장되며,

두번째 Loan 엔티티의 대출 개월수가 2개월이므로 LoanSchedule 엔티티 2개 저장될 것입니다.

최종적으로 2개의 Loan 엔티티와 5개의 LoanSchedule 엔티티를 저장하게 됩니다.

```java

@Slf4j
@SpringBootTest
@Rollback(false)
class LoanExecuteServiceTest {
    @Autowired
    private LoanExecuteService loanExecuteService;

    @Test
    @Transactional
    void createCreditLoan() {
        loanExecuteService.executeCreditLoan(
                LoanSaveVo.builder()
                        .amount(BigDecimal.valueOf(10_000_000))
                        .interestRate(BigDecimal.valueOf(3.0))
                        .durationMonths(3)
                        .build()
        );
        loanExecuteService.executeCreditLoan(
                LoanSaveVo.builder()
                        .amount(BigDecimal.valueOf(2_000_000))
                        .interestRate(BigDecimal.valueOf(2.0))
                        .durationMonths(2)
                        .build()
        );
    }
}
```

### 2.2. SQL 로그 추적

참고로 Hibernate SQL 로그는 실제 DB에 실행되는 SQL이 아닙니다.

MySQL DB는 JDBC 연결 시 파라미터로 `profileSQL=true&logger=Slf4JLogger` 를 추가하면 실제 쿼리 로그를 Slf4J 로거로 연결할 수 있습니다.

Oracle DB에서는 별도로 지원하는 옵션이 없어서 `net.ttddyy:datasource-proxy:1.8` 라이브러리를 사용하여 DataSource를 프록싱하면

JDBC 레벨의 쿼리 로그를 Slf4J 로거에 연결할 수 있습니다.

```java

@Bean
public DataSource oracleProxyDataSource(DataSource oracleDataSource) {
    return ProxyDataSourceBuilder.create(oracleDataSource)
            .name("ORACLE-LOGGER")
            .logQueryBySlf4j(log.getName())
            .multiline()
            .asJson()
            .countQuery()
            .build();
}
```

## 3. JPA 기본 동작 확인

Hibernate는 기본적으로 배치를 활성화하지 않습니다.

아래의 테스트 코드를 실행해보겠습니다.

```java

@Slf4j
@SpringBootTest
@Rollback(false)
class LoanExecuteServiceTest {
    @Autowired
    private LoanExecuteService loanExecuteService;

    @Test
    @Transactional
    void createCreditLoan() {
        loanExecuteService.executeCreditLoan(
                LoanSaveVo.builder()
                        .amount(BigDecimal.valueOf(10_000_000))
                        .interestRate(BigDecimal.valueOf(3.0))
                        .durationMonths(3)
                        .build()
        );
        loanExecuteService.executeCreditLoan(
                LoanSaveVo.builder()
                        .amount(BigDecimal.valueOf(2_000_000))
                        .interestRate(BigDecimal.valueOf(2.0))
                        .durationMonths(2)
                        .build()
        );
    }
}
```

이 경우, 트랜잭션이 완료 되는 시점에 영속성 컨텍스트에 있던 쿼리들이 순차적으로 실행됩니다.

먼저 시퀀스를 순차적으로 채번해옵니다.

그리고, Loan 1번, LoanSchedule 3번, Loan 1번, LoanSchedule 2번의 INSERT 문이 개별적으로 실행됩니다.

```sql
Name
:ORACLE-LOGGER, Connection:4, Time:14, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["select sq_loan.nextval from dual"]
Params:[()]

Name:ORACLE-LOGGER, Connection:4, Time:7, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["select sq_loan_schedule.nextval from dual"]
Params:[()]

Name:ORACLE-LOGGER, Connection:4, Time:7, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["select sq_loan_schedule.nextval from dual"]
Params:[()]

Name:ORACLE-LOGGER, Connection:4, Time:2, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["select sq_loan_schedule.nextval from dual"]
Params:[()]

Name:ORACLE-LOGGER, Connection:4, Time:1, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["select sq_loan_schedule.nextval from dual"]
Params:[()]

Name:ORACLE-LOGGER, Connection:4, Time:1, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["select sq_loan.nextval from dual"]
Params:[()]

Name:ORACLE-LOGGER, Connection:4, Time:2, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["select sq_loan_schedule.nextval from dual"]
Params:[()]

Name:ORACLE-LOGGER, Connection:4, Time:1, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["select sq_loan_schedule.nextval from dual"]
Params:[()]

Name:ORACLE-LOGGER, Connection:4, Time:10, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["insert into loan (amount, duration_months, interest_rate, type, id) values (?, ?, ?, ?, ?)"]
Params:[(10000000,4,4.0,CREDIT_LOAN,1)]

Name:ORACLE-LOGGER, Connection:4, Time:3, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(1,1,2533300.00000,1)]

Name:ORACLE-LOGGER, Connection:4, Time:1, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(2,1,2533300.00000,2)]

Name:ORACLE-LOGGER, Connection:4, Time:1, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(3,1,2533300.00000,3)]

Name:ORACLE-LOGGER, Connection:4, Time:1, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(4,1,2533300.00000,4)]

Name:ORACLE-LOGGER, Connection:4, Time:0, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["insert into loan (amount, duration_months, interest_rate, type, id) values (?, ?, ?, ?, ?)"]
Params:[(2000000,2,2.0,CREDIT_LOAN,2)]

Name:ORACLE-LOGGER, Connection:4, Time:0, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(1,2,1003340.00000,5)]

Name:ORACLE-LOGGER, Connection:4, Time:1, Success:True
Type:Prepared, Batch:False, QuerySize:1, BatchSize:0
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(2,2,1003340.00000,6)]
```

N개의 SQL문이 N번의 네트워크 호출로 데이터베이스에 전송되고 있습니다.

이 부분을 최적화하기 위해 hibernate 에는 batch 설정을 지원합니다.

## 4. JPA Batch 설정 및 효과

배치를 활성화하려면 `hibernate.jdbc.batch_size` 속성을 설정해야 합니다.

#### application.yml 설정

```properties
spring.jpa.properties.hibernate.jdbc.batch_size=1000
```

이제 다시 테스트 코드를 실행하여 쿼리 결과를 확인해봅니다.

시퀀스 채번해오는 쿼리는 그대로이니 생략하였습니다.

INSERT 쿼리가 조금 달라졌습니다.

Loan 1번, LoanSchedule 1번, Loan 1번, LoanSchedule 1번으로 기존 7개의 SQL이 4개로 줄었습니다.

```sql
Name
:ORACLE-LOGGER, Connection:4, Time:15, Success:True
Type:Prepared, Batch:True, QuerySize:1, BatchSize:1
Query:["insert into loan (amount, duration_months, interest_rate, type, id) values (?, ?, ?, ?, ?)"]
Params:[(10000000,3,3.0,CREDIT_LOAN,1)]

Name:ORACLE-LOGGER, Connection:4, Time:4, Success:True
Type:Prepared, Batch:True, QuerySize:1, BatchSize:3
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(1,1,3358333.33333,1),(2,1,3358333.33333,2),(3,1,3358333.33333,3)]

Name:ORACLE-LOGGER, Connection:4, Time:1, Success:True
Type:Prepared, Batch:True, QuerySize:1, BatchSize:1
Query:["insert into loan (amount, duration_months, interest_rate, type, id) values (?, ?, ?, ?, ?)"]
Params:[(2000000,2,2.0,CREDIT_LOAN,2)]

Name:ORACLE-LOGGER, Connection:4, Time:0, Success:True
Type:Prepared, Batch:True, QuerySize:1, BatchSize:2
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(1,2,1003340.00000,4),(2,2,1003340.00000,5)]
```

여기서 한번 더 JPA 설정을 추가합니다.

```properties
spring.jpa.properties.hibernate.order_inserts:true
spring.jpa.properties.hibernate.order_updates:true
```

다시 테스트 코드를 실행하여 쿼리 결과를 확인해봅니다.

Loan 1번, LoanSchedule 1번으로 7개에서 4개로 줄었던 것이 2번으로 한번 더 최적화 되었습니다.

해당 설정을 하면 Hibernate가 같은 엔티티 타입끼리 묶어서 Batch Insert/Update 쿼리를 실행합니다.

```sql
Name
:ORACLE-LOGGER, Connection:4, Time:22, Success:True
Type:Prepared, Batch:True, QuerySize:1, BatchSize:2
Query:["insert into loan (amount, duration_months, interest_rate, type, id) values (?, ?, ?, ?, ?)"]
Params:[(10000000,3,3.0,CREDIT_LOAN,1),(2000000,2,2.0,CREDIT_LOAN,2)]

Name:ORACLE-LOGGER, Connection:4, Time:5, Success:True
Type:Prepared, Batch:True, QuerySize:1, BatchSize:5
Query:["insert into loan_schedule (installment_no, loan_id, payment_amount, id) values (?, ?, ?, ?)"]
Params:[(1,1,3358333.33333,1),(2,1,3358333.33333,2),(3,1,3358333.33333,3),(1,2,1003340.00000,4),(2,2,1003340.00000,5)]
```

## 5. Oracle DB와 MySQL 비교

지금까지 알아본 것처럼 Batch Insert 를 적용하면 그룹핑된 INSERT문이 적은 네트워크 비용으로 DB에 전송됩니다.

하지만, Oracle DB 에서는 배치로 전송된 쿼리는 결국 개별 INSERT 문으로 실행됩니다.

Oracle DB 는 MySQL DB와 다르게 Multi Value Insert 를 지원하지 않기 때문입니다.

우선 MySQL DB의 Batch Insert 부터 알아보겠습니다.

MySQL DB에서 기존 설정을 적용 한 뒤, JDBC URL에 `rewriteBatchedStatements=true` 옵션을 추가하면 Batch 쿼리가 JDBC 레벨에서 Multi-Value Insert 형태로
변환됩니다.

실제 MySQL DB에서 실행되는 쿼리는 아래와 같습니다.

```sql
insert into loan (amount, duration_months, interest_rate, type, id)
values (10000000, 3, 3.0, 'CREDIT_LOAN', 1),
       (2000000, 2, 2.0, 'CREDIT_LOAN', 2)
    insert
into loan_schedule (installment_no, loan_id, payment_amount, id)
values (1, 1, 3358333.33333, 1), (2, 1, 3358333.33333, 2), (3, 1, 3358333.33333, 3), (1, 2, 1003340.00000, 4), (2, 2, 1003340.00000, 5)
```

### 5-1. Oracle DB의 배치 처리 방식

Oracle의 JDBC 드라이버는 배치 Insert를 지원하지만, 실행 방식이 MySQL과 다릅니다.

Hibernate가 batch_size 만큼의 INSERT 문을 하나의 배치로 그룹핑하여 DB로 전송됩니다.

하지만, Oracle DB 내부에서는 여전히 개별적인 INSERT 문으로 실행됩니다.

즉, 네트워크 트래픽 절감 효과는 있지만, 실제 실행되는 SQL 문 수는 그대로 유지됩니다.

```sql
-- Hibernate는 배치 처리로 묶어서 보냄 (네트워크 트래픽 감소 효과)
INSERT INTO loan (amount, duration_months, interest_rate, type, id)
VALUES (?, ?, ?, ?, ?);
INSERT INTO loan_schedule (installment_no, loan_id, payment_amount, id)
VALUES (?, ?, ?, ?);
```

오라클에서도 INSERT ALL 문을 Native Query 로 구현하면 사용할 수는 있지만, 실제 테스트 해보니 삽입 건수가 100건 정도 되니 에러가 발생하였습니다.

```sql
INSERT
ALL 
    INTO loan (amount, duration_months, interest_rate, type, id) VALUES (10000000, 3, 3.0, 'CREDIT_LOAN', 1)
    INTO loan (amount, duration_months, interest_rate, type, id) VALUES (2000000, 2, 2.0, 'CREDIT_LOAN', 2)
SELECT 1
FROM DUAL;
```

### 5-2. MySQL의 배치 처리 방식

MySQL은 기본적으로 Multi-Value Insert를 지원하며, JDBC 드라이버에서 이를 활용할 수 있도록 설정이 가능합니다.

rewriteBatchedStatements=true 옵션을 활성화하면, JDBC 드라이버가 Batch Insert 문을 하나의 Multi-Value Insert 문으로 변환하여 실행

즉, 네트워크 트래픽 절감 + SQL 실행 성능 향상 모두 가능

### 5-3. Batch Insert 전/후 DB 별 성능 비교

Bulk Insert 적용 전/후 성능 비교 (Oracle vs MySQL)

| **건수**    | **DBMS**   | **Bulk Insert 적용 전 (ms)** | **Bulk Insert 적용 후 (ms)** | **성능 개선율 (%)** |
|-----------|------------|---------------------------|---------------------------|----------------|
| **1만 건**  | **Oracle** | 10,335 ms                 | 9,163 ms                  | **11.3%** 개선   |
| **1만 건**  | **MySQL**  | 3,871 ms                  | 992 ms                    | **74.4%** 개선   |
| **10만 건** | **Oracle** | 83,667 ms                 | 55,033 ms                 | **34.2%** 개선   |
| **10만 건** | **MySQL**  | 33,879 ms                 | 3,332 ms                  | **90.2%** 개선   |

Oracle은 Batch Insert 적용 후 속도 개선

MySQL은 Multi-Value Insert + JDBC Batch Processing을 활용한 최적화가 효과적

JPA를 활용한 대량 데이터 Insert 시 MySQL이 유리

## 6. 요약

| **DBMS**   | **Batch Insert 처리 방식**   | **Multi-Value Insert 지원** | **성능 최적화 방법**                          |
|------------|--------------------------|---------------------------|----------------------------------------|
| **Oracle** | 개별 INSERT 문으로 실행         | 미지원                       | INSERT ALL 사용 가능하나 불안정 (Hibernate 미지원) |
| **MySQL**  | Multi-Value Insert 변환 가능 | 지원                        | rewriteBatchedStatements=true 활성화      |

- Oracle에서는 네트워크 트래픽 감소는 되지만, SQL 문은 그대로 실행

- MySQL에서는 JDBC 드라이버 설정만으로 Multi-Value Insert로 최적화 가능

- Hibernate/JPA 환경에서 MySQL이 배치 Insert 성능 최적화 측면에서 더 유리함

다음 문서에서는 엔티티의 ID 생성 전략 별 Batch Insert 동작 방식의 차이 및 성능 비교에 대해 알아보겠습니다.

# JPA의 Bulk Insert : ID 생성 전략에 따른 성능 비교

JPA에서 `saveAll()`을 이용한 Bulk Insert 성능은 ID 생성 전략에 따라 크게 달라질 수 있습니다.

이 문서에서는 Auto Increment, Table/Sequence, 직접 할당 방식을 비교하고, 성능 최적화를 위한 설정 방법을 설명합니다.

## ID 생성 전략별 엔티티 구현 및 특징

### Auto Increment (IDENTITY 전략)

```java

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "AutoIncrementedIdBook")
public class AutoIncrementedIdBookEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookSeq;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private LocalDateTime publicationDate;

    @Column
    private BigDecimal price;

    public static AutoIncrementedIdBookEntity from(BookSaveVo vo) {
        return AutoIncrementedIdBookEntity.builder()
                .title(vo.getTitle())
                .author(vo.getAuthor())
                .publicationDate(vo.getPublicationDate())
                .price(vo.getPrice())
                .build();
    }
}
```

- 특징
    - `GenerationType.IDENTITY`는 데이터가 DB에 INSERT 되면 자동으로 ID를 생성

### Table/Sequence (TABLE 전략)

```java

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "SequenceIdBook")
@TableGenerator(
        name = "book_seq_generator",
        table = "sequence_table",
        pkColumnName = "name",
        valueColumnName = "next_val",
        pkColumnValue = "book_seq",
        allocationSize = 1
)
public class SequenceIdBookEntity {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "book_seq_generator")
    private Long bookSeq;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private LocalDateTime publicationDate;

    @Column
    private BigDecimal price;

    public static SequenceIdBookEntity from(BookSaveVo vo) {
        return SequenceIdBookEntity.builder()
                .title(vo.getTitle())
                .author(vo.getAuthor())
                .publicationDate(vo.getPublicationDate())
                .price(vo.getPrice())
                .build();
    }
}
```

- 특징

    - 시퀀스 값을 저장하는 별도의 sequence_table에서 ID를 가져옴
    - Insert 전에 시퀀스 값을 조회하는 쿼리가 필요하여 성능 저하 발생 가능

### 직접 할당 (Custom ID, UUID 방식)

```Java

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CustomIdBook")
public class CustomIdBookEntity {

    @Id
    @Column
    private Long bookSeq;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private LocalDateTime publicationDate;

    @Column
    private BigDecimal price;

    public static CustomIdBookEntity from(BookSaveVo vo) {
        return CustomIdBookEntity.builder()
                .bookSeq(SequenceHolder.nextValue())
                .title(vo.getTitle())
                .author(vo.getAuthor())
                .publicationDate(vo.getPublicationDate())
                .price(vo.getPrice())
                .build();
    }
}
```

- 특징

    - ID를 애플리케이션에서 직접 할당

## ID 생성 전략별 엔티티 등록 서비스 및 테스트 코드

- registerAutoIncrementedIdBooks
- registerSequenceIdBooks
- registerCustomIdBooks

```java

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookRegisterService {

    private final AutoIncrementedIdBookRepository autoIncrementedIdBookRepository;
    private final SequenceIdBookRepository sequenceIdBookRepository;
    private final CustomIdBookRepository customIdBookRepository;

    @Transactional
    public List<AutoIncrementedIdBookEntity> registerAutoIncrementedIdBooks(List<BookSaveVo> voList) {

        List<AutoIncrementedIdBookEntity> newBooks = voList.stream()
                .map(AutoIncrementedIdBookEntity::from)
                .collect(Collectors.toList());

        return autoIncrementedIdBookRepository.saveAll(newBooks);
    }

    @Transactional
    public List<SequenceIdBookEntity> registerSequenceIdBooks(List<BookSaveVo> voList) {

        List<SequenceIdBookEntity> newBooks = voList.stream()
                .map(SequenceIdBookEntity::from)
                .collect(Collectors.toList());

        return sequenceIdBookRepository.saveAll(newBooks);
    }

    @Transactional
    public List<CustomIdBookEntity> registerCustomIdBooks(List<BookSaveVo> voList) {

        List<CustomIdBookEntity> newBooks = voList.stream()
                .map(CustomIdBookEntity::from)
                .collect(Collectors.toList());

        return customIdBookRepository.saveAll(newBooks);
    }

}
```

- Test code

```java

@Slf4j
@SpringBootTest
class BookRegisterServiceTest {

    private final int DATA_SIZE = 3;

    @Autowired
    private BookRegisterService bookRegisterService;

    @Test
    @DisplayName("auto increment entity saveAll 테스트")
    void testAutoIncrementedIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<AutoIncrementedIdBookEntity> actual = bookRegisterService.registerAutoIncrementedIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        log.info("auto increment : {} ms", endTime - startTime);
    }

    @Test
    @DisplayName("sequence 채번 전략 entity saveAll 테스트")
    void testSequenceIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<SequenceIdBookEntity> actual = bookRegisterService.registerSequenceIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        log.info("sequence : {} ms", endTime - startTime);
    }

    @Test
    @DisplayName("id 직접할당 entity saveAll 테스트")
    void testCustomIdBookEntityRegisterService() {
        // given
        List<BookSaveVo> voList = getBookSaveVoList();

        // when
        long startTime = System.currentTimeMillis();
        List<CustomIdBookEntity> actual = bookRegisterService.registerCustomIdBooks(voList);
        long endTime = System.currentTimeMillis();

        // then
        assertThat(actual.size()).isEqualTo(DATA_SIZE);
        log.info("id : {} ms", endTime - startTime);
    }


    private List<BookSaveVo> getBookSaveVoList() {
        return IntStream.rangeClosed(1, DATA_SIZE)
                .mapToObj(i -> BookSaveVo
                        .builder()
                        .title("Hello JPA")
                        .author("JW")
                        .publicationDate(LocalDateTime.now())
                        .price(BigDecimal.valueOf(i))
                        .build())
                .collect(Collectors.toList());
    }
}
```

## ID 생성 전략별 쿼리 실행 흐름

### Auto Increment (IDENTITY 전략) 실행 쿼리

- 개별 insert 쿼리
- @Id 필드값은 insert 시점에 알 수 없음

```sql
insert into auto_incremented_id_book (author, price, publication_date, title)
values ('JW', 1, '2025-03-14 14:15:53.346', 'Hello JPA');
insert into auto_incremented_id_book (author, price, publication_date, title)
values ('JW', 2, '2025-03-14 14:15:53.347', 'Hello JPA');
insert into auto_incremented_id_book (author, price, publication_date, title)
values ('JW', 3, '2025-03-14 14:15:53.347', 'Hello JPA');
commit;
```

### Table/Sequence (TABLE 전략) 실행 쿼리

- ID를 미리 조회해야 해서 트랜잭션 지연 발생 가능

```sql
select tbl.next_val
from sequence_table tbl
where tbl.name = 'book_seq' for update;
update sequence_table
set next_val=4
where next_val = 3
  and name = 'book_seq';
commit;
select tbl.next_val
from sequence_table tbl
where tbl.name = 'book_seq' for update;
update sequence_table
set next_val=5
where next_val = 4
  and name = 'book_seq';
commit;
select tbl.next_val
from sequence_table tbl
where tbl.name = 'book_seq' for update;
update sequence_table
set next_val=6
where next_val = 5
  and name = 'book_seq';
commit;
insert into sequence_id_book (author, price, publication_date, title, book_seq)
values ('JW', 1, '2025-03-14 14:19:39.499', 'Hello JPA', 4);
insert into sequence_id_book (author, price, publication_date, title, book_seq)
values ('JW', 2, '2025-03-14 14:19:39.499', 'Hello JPA', 5);
insert into sequence_id_book (author, price, publication_date, title, book_seq)
values ('JW', 3, '2025-03-14 14:19:39.499', 'Hello JPA', 6);
commit;
```

### 직접 할당 (Custom ID) 실행 쿼리

- 영속화 시점에 ID값이 존재하므로 비영속(new) 엔티티인지 준영속(detached) 엔티티인지 확인이 필요
- DB에 ID값으로 조회하는 select 쿼리가 사전에 발생

```sql
select customidbo0_.book_seq         as book_seq1_1_0_,
       customidbo0_.author           as author2_1_0_,
       customidbo0_.price            as price3_1_0_,
       customidbo0_.publication_date as publicat4_1_0_,
       customidbo0_.title            as title5_1_0_
from custom_id_book customidbo0_
where customidbo0_.book_seq = 1;
select customidbo0_.book_seq         as book_seq1_1_0_,
       customidbo0_.author           as author2_1_0_,
       customidbo0_.price            as price3_1_0_,
       customidbo0_.publication_date as publicat4_1_0_,
       customidbo0_.title            as title5_1_0_
from custom_id_book customidbo0_
where customidbo0_.book_seq = 2;
select customidbo0_.book_seq         as book_seq1_1_0_,
       customidbo0_.author           as author2_1_0_,
       customidbo0_.price            as price3_1_0_,
       customidbo0_.publication_date as publicat4_1_0_,
       customidbo0_.title            as title5_1_0_
from custom_id_book customidbo0_
where customidbo0_.book_seq = 3;
insert into custom_id_book (author, price, publication_date, title, book_seq)
values ('JW', 1, '2025-03-14 14:22:57.422', 'Hello JPA', 1);
insert into custom_id_book (author, price, publication_date, title, book_seq)
values ('JW', 2, '2025-03-14 14:22:57.422', 'Hello JPA', 2);
insert into custom_id_book (author, price, publication_date, title, book_seq)
values ('JW', 3, '2025-03-14 14:22:57.422', 'Hello JPA', 3);
commit;
```

## JPA Batch Insert 설정 변경 및 효과 분석

- JPA에서 Batch Insert를 활성화하려면 다음 설정이 필요

- 여러 개의 데이터를 한 번에 Insert → 실행 속도 향상을 기대할 수 있음

  ```yaml
  spring:
    datasource:
      url: jdbc:mysql://localhost:3306/bulk?profileSQL=true&logger=Slf4JLogger&rewriteBatchedStatements=true
    jpa:
      properties:
        hibernate:
          order_inserts: true  # INSERT 문 정렬하여 Batch 처리
          order_updates: true  # UPDATE 문 정렬하여 Batch 처리
          jdbc:
            batch_size: 10000   # Batch Insert 크기 설정
  ```

### Auto Increment (IDENTITY 전략) 실행 쿼리

- 기존과 동일하게 개별 insert 쿼리
- Persistence Context 내부에서 엔티티를 식별할때는 엔티티 타입과 엔티티의 ID 값으로 엔티티를 식별하지만
- IDENTITY 의 경우 DB에 insert 문을 실행해야만 id 값을 확인 가능하기 때문에 batch insert 를 비활성화

```sql
insert into auto_incremented_id_book (author, price, publication_date, title)
values ('JW', 1, '2025-03-14 14:15:53.346', 'Hello JPA');
insert into auto_incremented_id_book (author, price, publication_date, title)
values ('JW', 2, '2025-03-14 14:15:53.347', 'Hello JPA');
insert into auto_incremented_id_book (author, price, publication_date, title)
values ('JW', 3, '2025-03-14 14:15:53.347', 'Hello JPA');
commit;
```

### Table/Sequence (TABLE 전략) 실행 쿼리

- ID를 미리 조회하는 select 쿼리는 변화 없음
- 개별 insert 쿼리는 Batch Insert 쿼리로 바뀜

```sql
select tbl.next_val
from sequence_table tbl
where tbl.name = 'book_seq' for update;
update sequence_table
set next_val=7
where next_val = 6
  and name = 'book_seq';
commit;
select tbl.next_val
from sequence_table tbl
where tbl.name = 'book_seq' for update;
update sequence_table
set next_val=8
where next_val = 7
  and name = 'book_seq';
commit;
select tbl.next_val
from sequence_table tbl
where tbl.name = 'book_seq' for update;
update sequence_table
set next_val=9
where next_val = 8
  and name = 'book_seq';
commit;
insert into sequence_id_book (author, price, publication_date, title, book_seq)
values ('JW', 1, '2025-03-14 14:32:08.537', 'Hello JPA', 7),
       ('JW', 2, '2025-03-14 14:32:08.538', 'Hello JPA', 8),
       ('JW', 3, '2025-03-14 14:32:08.538', 'Hello JPA', 9);
commit;

```

### 직접 할당 (Custom ID) 실행 쿼리

- insert 전에 id 값으로 데이터를 조회하는 select 쿼리는 그대로 발생
- 개별 insert 쿼리는 Batch Insert 쿼리로 바뀜

```sql
select customidbo0_.book_seq         as book_seq1_1_0_,
       customidbo0_.author           as author2_1_0_,
       customidbo0_.price            as price3_1_0_,
       customidbo0_.publication_date as publicat4_1_0_,
       customidbo0_.title            as title5_1_0_
from custom_id_book customidbo0_
where customidbo0_.book_seq = 1;
select customidbo0_.book_seq         as book_seq1_1_0_,
       customidbo0_.author           as author2_1_0_,
       customidbo0_.price            as price3_1_0_,
       customidbo0_.publication_date as publicat4_1_0_,
       customidbo0_.title            as title5_1_0_
from custom_id_book customidbo0_
where customidbo0_.book_seq = 2;
select customidbo0_.book_seq         as book_seq1_1_0_,
       customidbo0_.author           as author2_1_0_,
       customidbo0_.price            as price3_1_0_,
       customidbo0_.publication_date as publicat4_1_0_,
       customidbo0_.title            as title5_1_0_
from custom_id_book customidbo0_
where customidbo0_.book_seq = 3;
insert into custom_id_book (author, price, publication_date, title, book_seq)
values ('JW', 1, '2025-03-14 14:35:48.159', 'Hello JPA', 1),
       ('JW', 2, '2025-03-14 14:35:48.159', 'Hello JPA', 2),
       ('JW', 3, '2025-03-14 14:35:48.159', 'Hello JPA', 3);
commit;
```

## Batch Insert 설정 전/후 ID 생성 전략 별 속도 비교

- 10,000 건의 데이터를 saveAll 로 저장하는 속도 측정함
- auto increment 는 실행 쿼리가 동일했기 때문에 효과 없음
- Table 전략이나 직접 할당은 개별 Insert 쿼리가 Batch Insert 로 적용되어 속도 개선이 있음
- Table 전략은 시퀀스 할당 크기를 적절히 조정하면 insert 전에 시퀀스 채번해오는 쿼리가 줄어서 속도 개선 여지 있음
- 직접 할당 전략은 엔티티 매니저가 저장하려고 하는 엔티티가 비영속(new) 상태임을 알려줄 수 있다면 select 쿼리 자체를 제거 가능

| ID 생성 전략                     | Batch Insert 설정 적용 전 | Batch Insert 설정 적용 후 |
|------------------------------|----------------------|----------------------|
| Auto Increment (IDENTITY 전략) | 10초 (9,960 ms)       | 11초 (11,691 ms)      |
| Table/Sequence (TABLE 전략)    | 55초 (55,179 ms)      | 48초 (48,046 ms)      |
| 직접 할당 (Custom ID)            | 22초 (22,346 ms)      | 12초 (12,490 ms)      |

## SimpleJpaRepository 내부 코드 살펴보기

- JpaRepsitory.saveAll 을 하게 되면 SimpleJpaRepository 의 saveAll 이 호출됨

```java
// org.springframework.data.jpa.repository.support.SimpleJpaRepository
@Transactional
@Override
public <S extends T> List<S> saveAll(Iterable<S> entities) {

    Assert.notNull(entities, "Entities must not be null!");

    List<S> result = new ArrayList<>();

    for (S entity : entities) {
        result.add(save(entity));
    }

    return result;
}

@Transactional
@Override
public <S extends T> S save(S entity) {

    Assert.notNull(entity, "Entity must not be null.");

    if (entityInformation.isNew(entity)) {
        em.persist(entity);
        return entity;
    } else {
        return em.merge(entity);
    }
}
```

- 엔티티가 비영속(new) 인지 아닌지를 확인하는 부분이 entityInformation.isNew(entity)

- ID 값이 비어있으면 true 를 반환하고, 아닌 경우 DB에 조회하는게 기본 메커니즘

- 인터페이스를 따라가다보면 Persistable 인터페이스

  ```java
  public interface Persistable<ID> {
      @Nullable
      ID getId();
  
      boolean isNew();
  }
  ```

- 해당 인터페이스만 잘 구현하면 불필요한 DB 조회를 안할 수 있음

## Persistable Interface 구현 및 엔티티에 적용하기

- 신규 생성된 엔티티의 경우 isNew 메서드 호출 시 항상 true 를 반환
- DB에서 엔티티 조회 시점(PostLoad)에 isNew 값을 false 로 바꿈
- DB에 존재하는 기존 엔티티의 경우에는 isNew 메서드 호출 시 false 를 반환

```java

@MappedSuperclass
public abstract class BulkInsertEntity<T> implements Persistable<Long> {
    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    private void markIsNotNew() {
        isNew = false;
    }
}
```

### CustomIdBookEntity 에서 상속

- BulkInsertEntity 를 extends 하고 getId() 메서드만 override

```java

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CustomIdBook")
public class CustomIdBookEntity extends BulkInsertEntity<Long> {

    @Id
    @Column
    private Long bookSeq;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private LocalDateTime publicationDate;

    @Column(length = 20)
    private BigDecimal price;

    @Override
    public Long getId() {
        return bookSeq;
    }

    public static CustomIdBookEntity from(BookSaveVo vo) {
        return CustomIdBookEntity.builder()
                .bookSeq(SequenceHolder.nextValue())
                .title(vo.getTitle())
                .author(vo.getAuthor())
                .publicationDate(vo.getPublicationDate())
                .price(vo.getPrice())
                .build();
    }
}
```

### 직접 할당 (Custom ID) 실행 쿼리

- BulkInsertEntity 상속하니 불필요한 select 쿼리도 발생하지 않음

```sql
insert into custom_id_book (author, price, publication_date, title, book_seq)
values ('JW', 1, '2025-03-14 14:43:39.215', 'Hello JPA', 1),
       ('JW', 2, '2025-03-14 14:43:39.215', 'Hello JPA', 2),
       ('JW', 3, '2025-03-14 14:43:39.215', 'Hello JPA', 3);
commit;
```

## 최종 ID 생성 전략 별 속도 비교

- 10,000 건의 데이터를 saveAll 로 저장하는 속도 측정함
- ID 생성 전략을 직접 할당 후 BulkInsertEntity 상속 받는 엔티티가 가장 빠름

| ID 생성 전략                     | 기본              | Batch Insert 설정 적용 후 | BulkInsertEntity 상속 후 |
|------------------------------|-----------------|----------------------|-----------------------|
| Auto Increment (IDENTITY 전략) | 10초 (9,960 ms)  | 11초 (11,691 ms)      | 9초 (9,693 ms)         |
| Table/Sequence (TABLE 전략)    | 55초 (55,179 ms) | 48초 (48,046 ms)      | 50초 (50,283 ms)       |
| 직접 할당 (Custom ID)            | 22초 (22,346 ms) | 12초 (12,490 ms)      | 0.5초 (506 ms)         |

## 결론

- 엔티티의 ID 생성 전략을 선택할 때는 DB 독립적인 직접 할당 전략 추천
- Persistable Interface 를 구현해서 Bulk Insert 기능을 활용하면 대량 데이터 저장할 때도 속도 이슈가 없다

