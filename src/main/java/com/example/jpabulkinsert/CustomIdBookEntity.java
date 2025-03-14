package com.example.jpabulkinsert;

import com.example.jpabulkinsert.common.SequenceHolder;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
