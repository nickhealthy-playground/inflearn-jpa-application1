package jpabook.jpashop.repository.order.simplequery;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * DTO로 직접 조회하는 이 방식(V4)은 다른 곳에서 재사용 될 가능성이 낮고, 특정 케이스에 fix 되기 때문에,
 * 이렇게 별도의 패키지로 분리시키는 것이 좋다.
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {

    private final EntityManager em;

    /*
    * `select o from Order o`처럼 JPQL 의 select 절에서 조회할 수 있는 것은 엔티티나 값 타입(Embedded)만 바로 조회 가능하다.
    * 또한 V3 케이스처럼 조회된 엔티티를 생성자에 넘길 수 없다.
    * Order 엔티티와 OrderSimpleQueryDto 가 곧바로 매핑되지 않기 때문에 아래처럼 파라미터를 하나씩 모두 정확히 넘겨주어야 한다.
    */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.username, o.orderDate, o.status, d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }
}
