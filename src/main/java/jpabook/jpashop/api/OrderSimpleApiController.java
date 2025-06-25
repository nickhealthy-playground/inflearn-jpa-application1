package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * xToOne(ManyToOne, OneToOne) 관계 최적화
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출 문제점 - 실무 사용X
     * 양방향 관계 문제 발생
     * - jackson 라이브러리가 json을 설정하는 과정에서 엔티티 순환 참조 문제 발생
     * - 한쪽 방향 @JsonIgnore 설정 필요
     * Hibernate5Module 모듈 등록, LAZY = null 처리
     * - 해당 라이브러리를 등록하지 않음녀 LAZY 로딩으로 실제 엔티티 대신 프록시 객체가 존재하므로 jackson 라이브러리는 예외를 발생하게 됨
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getUsername(); // LAZY 강제 초기화
            order.getDelivery().getAddress(); // LAZY 강제 초기화
        }

        return all;
    }
}
