package com.market.jw.domain.order;

import com.market.jw.domain.product.Product;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)  // ✅ 여러 개의 OrderItem이 하나의 Order에 속함
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)  // ✅ 하나의 Product는 여러 OrderItem에 속할 수 있음
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity; // ✅ 해당 상품의 주문 수량

    @Column(nullable = false)
    private BigDecimal totalPrice; // ✅ 개별 상품별 총 가격 (상품 가격 * 수량)

    /**
     * 주문 항목(OrderItem) 생성 메서드
     */
    public static OrderItem create(Order order, Product product, int quantity) {
        product.decreaseStock(quantity);
        return OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build();
    }

}
