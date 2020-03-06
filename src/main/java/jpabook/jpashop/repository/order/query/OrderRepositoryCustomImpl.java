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
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public List<Order> findAllPageWithMemberDeliveryByQuerydsl(int offset, int limit, OrderSearch orderSearch) {
        query = new JPAQueryFactory(em);

        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QDelivery delivery = QDelivery.delivery;

        // findAllWithItemByQuerydsl() 의 문제 해결 : 페이징 + 컬렉션 엔티티 조회는 대부분 이렇게 해결됨
        // 1. ~ToOne 관계는 fetch join 처리(batchsize 설정때문에 안해도 되지만, row도 안늘어나는데 굳이 네트웤을 더 타게할 필요는 없음)
        // 2. ~ToMany 컬렉션들은 지연 로딩으로 냅둔다.
        // 3. 지연로딩 성능 최적화를 위해서 설정에 default_batch_fetch_size (혹은 각각에 @BatchSize)를 부여한다(한번에 size만큼 in 쿼리)
        // => 사이즈에 따라 다르지만, 1 + N -> 1+ 1 로 최적화.
        // v3는 한방쿼리로 네트웤 1번에 가져오지만 데이터 자체의 중복이 많음(뻥튀기때문에 distict가 db에서 처리되지않으므로)
        // v3.1은 쿼리는 여러번 날려, 호출수가 증가해 여러번 네트웤을 타지만 데이터정규화가 잘되어 DB데이터 전송량은 줄음. 페이징도 가능해짐.
        // batchSize 를 적당한 크기로 잘 선택해줘야함. 100 ~ 1000 사이를 추천. 단 in쿼리는 DB에 따라 차이가 있으며, 순간 부하를 고려하면 Max는 1000.
        // 결국 v3 - v3.1은 트레이드오프를 잘 생각해 선택하면 된다. 단 페이징하려면 무조건 v3.1.

        return query.
                select(order)
                .from(order)
                .join(order.member, member).fetchJoin()
                .join(order.delivery, delivery).fetchJoin()
                .where(orderSearch.getOrderStatus() == null ? null : order.status.eq(orderSearch.getOrderStatus()),
                        !StringUtils.hasText(orderSearch.getMemberName()) ? null : member.name.contains(orderSearch.getMemberName()))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public List<OrderQueryDto> findAllPageDtoWithMemberDeliveryByQuerydsl(int offset, int limit, OrderSearch orderSearch) {
        query = new JPAQueryFactory(em);

        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QDelivery delivery = QDelivery.delivery;

        return query
                .select(Projections.constructor(OrderQueryDto.class,
                        order.id, order.member.name, order.orderDate, order.status, delivery.address))
                .from(order)
                .join(order.member, member)
                .join(order.delivery, delivery)
                .where(orderSearch.getOrderStatus() == null ? null : order.status.eq(orderSearch.getOrderStatus()),
                        !StringUtils.hasText(orderSearch.getMemberName()) ? null : member.name.contains(orderSearch.getMemberName()))
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public List<OrderItemQueryDto> findAllDtoByQuerydsl(Long orderId) {
        query = new JPAQueryFactory(em);

        QItem item = QItem.item;
        QOrderItem orderItem = QOrderItem.orderItem;

        return query
                .select(Projections.constructor(OrderItemQueryDto.class,
                        orderItem.order.id, orderItem.item.name, orderItem.orderPrice, orderItem.count))
                .from(orderItem)
                .join(orderItem.item, item)
                .where(orderItem.order.id.eq(orderId))
                .fetch();
    }

    @Override
    public List<OrderQueryDto> findOrderQueryDto(int offset, int limit, OrderSearch orderSearch) {
        List<OrderQueryDto> result = findAllPageDtoWithMemberDeliveryByQuerydsl(offset, limit, orderSearch);

        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findAllDtoByQuerydsl(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    @Override
    public List<OrderQueryDto> findOrderQueryDtoOptimization(int offset, int limit, OrderSearch orderSearch) {
        // 쿼리 1회
        List<OrderQueryDto> result = findAllPageDtoWithMemberDeliveryByQuerydsl(offset, limit, orderSearch);
        List<Long> orderIds = result.stream()
                .map(OrderQueryDto::getOrderId)
                .collect(Collectors.toList());

        // 쿼리 1회
        List<OrderItemQueryDto> orderItems = findAllDtoByQuerydslOptimization(orderIds);
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }

    @Override
    public List<OrderItemQueryDto> findAllDtoByQuerydslOptimization(List<Long> orderIds) {
        query = new JPAQueryFactory(em);

        QItem item = QItem.item;
        QOrderItem orderItem = QOrderItem.orderItem;

        return query
                .select(Projections.constructor(OrderItemQueryDto.class,
                        orderItem.order.id, orderItem.item.name, orderItem.orderPrice, orderItem.count))
                .from(orderItem)
                .join(orderItem.item, item)
                .where(orderItem.order.id.in(orderIds))
                .fetch();
    }

    @Override
    public List<OrderFlatDto> findOrderQueryDtoFlat(int offset, int limit, OrderSearch orderSearch) {
        query = new JPAQueryFactory(em);

        QOrder order = QOrder.order;
        QMember member = QMember.member;
        QDelivery delivery = QDelivery.delivery;
        QOrderItem orderItem = QOrderItem.orderItem;
        QItem item = QItem.item;

        return query
                .select(Projections.constructor(OrderFlatDto.class,
                        order.id, member.name, order.orderDate, order.status, delivery.address, item.name, orderItem.count, orderItem.orderPrice))
                .from(order)
                .join(order.member, member)
                .join(order.delivery, delivery)
                .join(order.orderItems, orderItem)
                .join(orderItem.item, item)
                .where(orderSearch.getOrderStatus() == null ? null : order.status.eq(orderSearch.getOrderStatus()),
                        !StringUtils.hasText(orderSearch.getMemberName()) ? null : member.name.contains(orderSearch.getMemberName()))
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
