package com.market.jw.domain.order;

import com.market.jw.domain.order.dto.request.OrderProductDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;

    /**
     * 고객의 주문 목록 조회
     *
     * @param customerId 고객 아이디
     * @return 주문 목록
     */
    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomer_Id(customerId);
    }

    /**
     * 고객 주문 생성
     *
     */
    @Transactional
    public List<Order> createOrders(Long customerId, List<OrderProductDto>) {

    }
}
