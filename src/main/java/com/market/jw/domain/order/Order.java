package com.market.jw.domain.order;

import com.market.jw.domain.customer.Customer;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    /**
     * 주문 생성 메서드
     */
    public static Order create(Customer customer, List<OrderItem> orderItems) {
        BigDecimal totalOrderPrice = orderItems.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .customer(customer)
                .totalPrice(totalOrderPrice)
                .orderDate(LocalDateTime.now())
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.orderItems.add(item);
        }

        return order;
    }

    /**
     * 주문 취소 (재고 복구)
     */
    public void cancel() {
        for (OrderItem item : orderItems) {
            item.getProduct().increaseStock(item.getQuantity());
        }
    }
}
