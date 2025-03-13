package com.example.jpabulkinsert;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookSaveVo {
    
    private String title;

    private String author;

    private LocalDateTime publicationDate;

    private BigDecimal price;

}
