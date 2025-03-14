package com.example.jpabulkinsert;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class SequenceIdBookEntity extends BulkInsertEntity<Long> {

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

    @Column(length = 20)
    private BigDecimal price;


    public static SequenceIdBookEntity from(BookSaveVo vo) {
        return SequenceIdBookEntity.builder()
                .title(vo.getTitle())
                .author(vo.getAuthor())
                .publicationDate(vo.getPublicationDate())
                .price(vo.getPrice())
                .build();
    }

    @Override
    public Long getId() {
        return bookSeq;
    }
}
