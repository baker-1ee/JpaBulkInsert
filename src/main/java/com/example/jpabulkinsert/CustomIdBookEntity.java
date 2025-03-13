package com.example.jpabulkinsert;

import com.example.jpabulkinsert.common.SequenceHolder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@Builder
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

//    @Override
//    public Long getId() {
//        return bookSeq;
//    }

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
