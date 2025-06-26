package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * V1. 엔티티 직접 노출 - 좋은 방법X
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getUsername(); // LAZY 강제 초기화
            order.getDelivery().getAddress(); // LAZY 강제 초기화

            // orderItem, item 관계를 직접 초기화하면 Hibernate5JakartaModule 설정에 의해 엔티티를 JSON 으로 생성한다.
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); // LAZY 강제 초기화
        }

        return all;
    }

    /**
     * V2. 엔티티를 DTO로 변환(fetch join 사용 X)
     * 지연 로딩으로 너무 많은 SQL 발생(N + 1)
     *   - order 쿼리 한번 조회 시 -> rows 2개
     *   - 각 row 당 N번 조회 -> member(N), delivery(N), orderItem(N)
     *   - orderItem 컬렉션 조회 -> 2건 조회
     *     - 각 row 당 N번 조회 -> Item(N)
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result = orders.stream().map(OrderDto::new)
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class OrderDto {
        private final Long orderId;
        private final String name;
        private final LocalDateTime orderDate; // 주문시간
        private final OrderStatus orderStatus;
        private final Address address;
        private final List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getUsername();
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress();

            // 전송할 데이터와 엔티티는 확실히 구분해야 한다.
            // 여기서는 order.getOrderItems() 메서드를 호출하면 OrderItem 엔티티가 반환되므로 별도의 DTO를 만들어서 필요한 데이터만 추출한다.
            this.orderItems = order.getOrderItems().stream()
                    .map(OrderItemDto::new)
                    .collect(Collectors.toList());
        }
    }

    @Data
    static class OrderItemDto {

        private final String itemName; // 상품명
        private final int orderPrice; // 주문 가격
        private final int count; // 수량

        public OrderItemDto(OrderItem orderItem) {
            this.itemName = orderItem.getItem().getName();
            this.orderPrice = orderItem.getOrderPrice();
            this.count = orderItem.getCount();
        }
    }
}
