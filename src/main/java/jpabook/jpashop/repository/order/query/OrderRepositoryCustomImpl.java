package jpabook.jpashop.repository.order.query;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.QItem;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final EntityManager em;
    private JPAQueryFactory query;

    // 동적쿼리 해결법 : Querydsl
    @Override
    public List<Order> findAllByQuerydsl(OrderSearch orderSearch) {
        query = new JPAQueryFactory(em);

        QOrder order = QOrder.order;
        QMember member = QMember.member;

        return query.
                select(order)
                .from(order)
                .join(order.member, member)
                .where(orderSearch.getOrderStatus() == null ? null : order.status.eq(orderSearch.getOrderStatus()),
                        !StringUtils.hasText(orderSearch.getMemberName()) ? null : member.name.contains(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    // JPQL 동적쿼리 1 (String)
    @Override
    public List<Order> findAllByString(OrderSearch orderSearch) {
        String jpql = "select o From Order o join o.member m";
        boolean isFirst = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            jpql += " where";
            isFirst = false;
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirst) {
                jpql += " where";
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class).setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    // JPQL 동적쿼리 1 (Criteria)
    @Override
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);

        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    @Override
    public List<Order> findAllWithMemberDeliveryByQuerydsl(OrderSearch orderSearch) {
        query = new JPAQueryFactory(em);

        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QDelivery delivery = QDelivery.delivery;

        return query.
                select(order)
                .from(order)
                .join(order.member, member).fetchJoin()
                .join(order.delivery, delivery).fetchJoin()
                .where(orderSearch.getOrderStatus() == null ? null : order.status.eq(orderSearch.getOrderStatus()),
                        !StringUtils.hasText(orderSearch.getMemberName()) ? null : member.name.contains(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    @Override
    public List<SimpleOrderQueryDto> findAllDtoWithMemberDeliveryByQuerydsl(OrderSearch orderSearch) {
        query = new JPAQueryFactory(em);

        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QDelivery delivery = QDelivery.delivery;


        return query
                .select(Projections.constructor(SimpleOrderQueryDto.class,
                        order.id, order.member.name, order.orderDate, order.status, delivery.address))
                .from(order)
                .join(order.member, member)
                .join(order.delivery, delivery)
                .where(orderSearch.getOrderStatus() == null ? null : order.status.eq(orderSearch.getOrderStatus()),
                        !StringUtils.hasText(orderSearch.getMemberName()) ? null : member.name.contains(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    @Override
    public List<Order> findAllWithItemByQuerydsl(OrderSearch orderSearch) {

        query = new JPAQueryFactory(em);

        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QDelivery delivery = QDelivery.delivery;
        QOrderItem orderItem = QOrderItem.orderItem;
        QItem item = QItem.item;

        // 일대다 특성상 일(1)인 order가 뻥튀기됨. 고로 distinct는 DB에선 할 수 없고, JPA가 DB가 아닌, 어플리케이션에서 해준 것
        // collection fetch join 특징
        // 1. DB상에서 페이징 불가. 메모리상에서 페이징 처리하므로 매우 위험
        // 2. 1개만 사용하다. 2개부터는 데이터 부정합 가능성이 매우 높음

        return query
                .select(order).distinct()
                .from(order)
                .join(order.member, member).fetchJoin()
                .join(order.delivery, delivery).fetchJoin()
                .join(order.orderItems, orderItem).fetchJoin()
                .join(orderItem.item, item).fetchJoin()
                .where(orderSearch.getOrderStatus() == null ? null : order.status.eq(orderSearch.getOrderStatus()),
                        !StringUtils.hasText(orderSearch.getMemberName()) ? null : member.name.contains(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }
}
