package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderSearch;

import java.util.List;

public interface OrderRepositoryCustom {
    List<Order> findAllByQuerydsl(OrderSearch orderSearch);
    List<Order> findAllByCriteria(OrderSearch orderSearch);
    List<Order> findAllByString(OrderSearch orderSearch);

    List<Order> findAllWithMemberDeliveryByQuerydsl(OrderSearch orderSearch);
    List<SimpleOrderQueryDto> findAllDtoWithMemberDeliveryByQuerydsl(OrderSearch orderSearch);

    List<Order> findAllWithItemByQuerydsl(OrderSearch orderSearch);
    List<Order> findAllPageWithMemberDeliveryByQuerydsl(int offset, int limit, OrderSearch orderSearch);

    List<OrderQueryDto> findOrderQueryDto(int offset, int limit, OrderSearch orderSearch);
    List<OrderQueryDto> findAllPageDtoWithMemberDeliveryByQuerydsl(int offset, int limit, OrderSearch orderSearch);
    List<OrderItemQueryDto> findAllDtoByQuerydsl(Long orderId);

    List<OrderQueryDto> findOrderQueryDtoOptimization(int offset, int limit, OrderSearch orderSearch);
    List<OrderItemQueryDto> findAllDtoByQuerydslOptimization(List<Long> orderId);

    List<OrderFlatDto> findOrderQueryDtoFlat(int offset, int limit, OrderSearch orderSearch);

}
