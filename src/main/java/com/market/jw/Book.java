package com.market.jw;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public static Book from(BookSaveDto dto) {
        return Book.builder()
                .bookSeq(UUID.randomUUID().getMostSignificantBits())
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .publicationDate(dto.getPublicationDate())
                .price(dto.getPrice())
                .build();
    }
}
