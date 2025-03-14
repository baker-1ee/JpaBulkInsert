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
@Table(name = "AutoIncrementedIdBook")
public class AutoIncrementedIdBookEntity extends BulkInsertEntity<Long> {

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

    @Column(length = 20)
    private BigDecimal price;

    public static AutoIncrementedIdBookEntity from(BookSaveVo vo) {
        return AutoIncrementedIdBookEntity.builder()
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
