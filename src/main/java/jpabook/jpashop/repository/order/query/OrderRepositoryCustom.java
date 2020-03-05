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
}
