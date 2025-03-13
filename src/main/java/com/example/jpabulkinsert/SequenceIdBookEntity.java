package com.example.jpabulkinsert;

import com.example.jpabulkinsert.common.SequenceHolder;
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
@Table(name = "SequenceIdBook")
@TableGenerator(
        name = "book_seq_generator", // Hibernate에서 사용할 시퀀스 이름
        table = "sequence_table", //  시퀀스 역할을 할 테이블
        pkColumnName = "name", //  시퀀스 이름을 저장할 컬럼
        valueColumnName = "next_val", // 시퀀스 값을 저장할 컬럼
        pkColumnValue = "book_seq", //  해당 엔터티가 사용할 시퀀스 값
        allocationSize = 100 //  증가 값 설정
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

//    @Override
//    public Long getId() {
//        return bookSeq;
//    }

    public static SequenceIdBookEntity from(BookSaveVo vo) {
        return SequenceIdBookEntity.builder()
                .title(vo.getTitle())
                .author(vo.getAuthor())
                .publicationDate(vo.getPublicationDate())
                .price(vo.getPrice())
                .build();
    }
}
