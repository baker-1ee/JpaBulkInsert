package com.example.bookstore.domain.book.entity;

import com.example.bookstore.common.jpa.entity.BulkInsertEntityJ;
import com.example.bookstore.domain.book.dto.BookSaveDto;
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
public class BookJ extends BulkInsertEntityJ {

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

    public static BookJ from(BookSaveDto dto) {
        return BookJ.builder()
                .bookSeq(UUID.randomUUID().getMostSignificantBits())
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .publicationDate(dto.getPublicationDate())
                .price(dto.getPrice())
                .build();
    }

    public void updatePrice(BigDecimal price) {
        this.price = price;
    }

}
