package com.market.jw.domain.product;

import java.math.BigDecimal;

public class ProductFactory {

    /**
     * 일반 상품 생성
     */
    public static Product createStandardProduct(
            String name,
            BigDecimal price,
            int stockQuantity
    ) {
        return Product.builder()
                .name(name)
                .price(price)
                .stockQuantity(stockQuantity)
                .build();
    }

    /**
     * 할인 상품 생성
     */
    public static Product createDiscountProduct(
            String name,
            BigDecimal originalPrice,
            BigDecimal discountRate,
            int stockQuantity
    ) {
        BigDecimal discountedPrice = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate));
        return Product.builder()
                .name(name)
                .price(discountedPrice)
                .stockQuantity(stockQuantity)
                .build();
    }
    
}
