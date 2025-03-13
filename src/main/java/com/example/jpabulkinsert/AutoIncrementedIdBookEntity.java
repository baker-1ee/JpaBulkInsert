package com.example.jpabulkinsert;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@ToString
@Builder
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

//    @Override
//    public Long getId() {
//        return bookSeq;
//    }

    public static AutoIncrementedIdBookEntity from(BookSaveVo vo) {
        return AutoIncrementedIdBookEntity.builder()
                .title(vo.getTitle())
                .author(vo.getAuthor())
                .publicationDate(vo.getPublicationDate())
                .price(vo.getPrice())
                .build();
    }
}
