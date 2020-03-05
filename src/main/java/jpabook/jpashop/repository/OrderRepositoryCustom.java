package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;

import java.util.List;

public interface OrderRepositoryCustom {
    List<Order> findAllByQuerydsl(OrderSearch orderSearch);
    List<Order> findAllByCriteria(OrderSearch orderSearch);
    List<Order> findAllByString(OrderSearch orderSearch);

    List<Order> findAllWithMemberDeliveryByQuerydsl(OrderSearch orderSearch);
}
