package jpabook.jpashop.service.query;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

// 핵심 비즈니스 로직과 뷰 관련 로직을 분리
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;

    public List<OrderDto> ordersV2_OSIV() {
        List<Order> orders = orderRepository.findAllByQuerydsl(new OrderSearch());

        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }
}
