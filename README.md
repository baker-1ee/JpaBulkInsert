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

