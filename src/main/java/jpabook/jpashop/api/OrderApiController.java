package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@RestController
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryService orderQueryService;

    @GetMapping("/api/v1/orders")
    private List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByQuerydsl(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.forEach(o -> o.getItem().getName());
        }

        return all;
    }

    @GetMapping("/api/v2/orders")
    private List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByQuerydsl(new OrderSearch());

        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    // OSIV 껐을 때 추천하는 방
    @GetMapping("/api/v2/orders")
    private List<jpabook.jpashop.service.query.OrderDto> ordersV2_OSIV() {
        return orderQueryService.ordersV2_OSIV();
    }

    @GetMapping("/api/v3/orders")
    private List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItemByQuerydsl(new OrderSearch());

        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    @GetMapping("/api/v3.1/orders")
    private List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllPageWithMemberDeliveryByQuerydsl(offset, limit, new OrderSearch());

        return orders.stream()
                .map(OrderDto::new)
                .collect(toList());
    }

    @GetMapping("/api/v4/orders")
    private List<OrderQueryDto> ordersV4_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        return orderRepository.findOrderQueryDto(offset, limit, new OrderSearch());
    }

    @GetMapping("/api/v5/orders")
    private List<OrderQueryDto> ordersV5_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        return orderRepository.findOrderQueryDtoOptimization(offset, limit, new OrderSearch());
    }

    @GetMapping("/api/v6/orders")
    private List<OrderQueryDto> ordersV6_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<OrderFlatDto> flats = orderRepository.findOrderQueryDtoFlat(offset, limit, new OrderSearch());

        return flats.stream()
                .collect(groupingBy(o ->
                                new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                ))
                .entrySet().stream()
                .map(e -> new OrderQueryDto(
                        e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();
            this.orderDate = order.getOrderDate();
            this.address = order.getDelivery().getAddress();
            order.getOrderItems().forEach(o -> o.getItem().getName());
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
