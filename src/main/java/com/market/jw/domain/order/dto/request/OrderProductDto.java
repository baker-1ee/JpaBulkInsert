package com.market.jw.domain.order.dto.request;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderProductDto {
    private Long id;

    private String name;

    private BigDecimal price;
}
