# Bulk Insert Entity

The `BulkInsertEntity` abstract class is designed to facilitate bulk insert operations in JPA entities.

It provides common fields and functionality required for bulk inserts, allowing entities to be saved in batches
using `saveAll` method provided by `JpaRepository`.

## Usage

To use the `BulkInsertEntity` abstract class:

1. Create your entity class by extending `BulkInsertEntity`.
2. Use `JpaRepository` provided by Spring Data JPA to save entities in bulk using the `saveAll` method.

## BulkInsertEntity

```java

@Getter
@ToString
@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class BulkInsertEntity implements Persistable<Long> {

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

    @CreatedDate
    @Column(nullable = false, updatable = false)
    protected LocalDateTime createdDatetime;

    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime updatedDatetime;

    @CreatedBy
    protected Long createdBy;

    @LastModifiedBy
    protected Long updatedBy;

}
```

## Example

```java

@Entity
@Getter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Books")
public class Book extends BulkInsertEntity {

    @Id
    @Column
    private Long bookSeq;

    @Column
    private String title;

    @Column
    private String author;

    @Column
    private LocalDateTime publicationDate;

    @Column(precision = 20, scale = 5)
    private BigDecimal price;

    @Override
    public Long getId() {
        return bookSeq;
    }

}
```

```java
public interface BookRepository extends JpaRepository<Book, Long> {
    // No additional methods required
}
```

```java

@Service
public class BookService {

    private final BookRepository repository;

    @Autowired
    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public void saveEntitiesInBulk(List<Book> entities) {
        repository.saveAll(entities);
    }
}
```

## Dependencies

- Spring Data JPA: For data access and repository support
- Lombok: For generating boilerplate code

## License

This project is licensed under the MIT License - see the LICENSE file for details.
