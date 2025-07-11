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
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
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

/**
 * 컬렉션 조회 최적화
 * xToMany(@OneToMany)
 * Order -> OrderItem
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    private final OrderQueryService orderQueryService;

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
                .collect(toList());

        return result;
    }

    /**
     * OSIV와 성능 최적화
     * - OSIV는 default 값 true
     * OSIV - true
     * - 트래픽이 많은 실시간 서비스에서 사용 X
     * - 데이터베이스 커넥션을 VIEW, Controller 단까지 가져감
     * - 장점: 지연로딩 로직 등을 VIEW, Controller 단에 작성 가능
     * - 단점: 커넥션 부족으로 성능 이슈 발생 가능
     * OSIV - false
     * - 트래픽이 많은 실시간 서비스에 적용
     * - 데이터베이스 커넥션이 트랜잭션 시작-끝 부분만 적용
     * - 장점: DB 커넥션 성능 최적화
     * - 단점: 커맨드 - 쿼리 분리 로직을 따로 가져가야함, 즉 지연 로딩 사용 불가
     *   - 서비스 계층에 로직을 모두 작성 or fetch join 적용(OrderQueryService 클래스 참고)
     */
//    @GetMapping("/api/v2.1/orders")
//    public List<jpabook.jpashop.service.query.OrderDto> ordersV2_1() {
//        return orderQueryService.disableOsivOrders();
//    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {

        /*
         * [findAllWithItem() 설명]
         * 1. 패치 조인으로 SQL 한번 실행
         * 2. 일대다 컬렉션 조회 시 distinct 사용 필요(하이버네이트5버전 -> spring 3.x, hibernate6 버전 이상은 중복 자동 제거)
         * - 일대다 관계에서 DB는 데이터 양이 증가하므로 distinct 키워드를 통해 중복을 제거함
         * - 하지만 JPA에서 발생한 쿼리는 실제 DB에선 중복 제거가 이뤄지지 않음
         * - JPA에서 PK(아래에선 Order 엔티티 PK)가 중복되는 값(엔티티)을 지우고 조회됨
         * 3. <중요!>: 일대다 컬렉션 조회에서 페이징 처리 불가능.
         * - 페이징 처리 시 모든 데이터를 읽어와 애플리케이션 임시 메모리에 적재 후 페이징 처리를 시도함
         * - 또한 데이터 정합성도 맞지 않을 가능성 있음
         * 4. 일대다 컬렉션 조회 시 다중 컬렉션 조회 X
         * - 이미 일대다 관계 조인으로 인해 데이터가 증가하였는데, 다른 N개의 데이터가 폭발적으로 증가하게 됨. -> 데이터 정합성 깨질 위험 높음
         */
        List<Order> orders = orderRepository.findAllWithItem();

        for (Order order : orders) {
            System.out.println("order ref= " + order + ", id: " + order.getId());
        }

        List<OrderDto> result = orders.stream().map(OrderDto::new)
                .collect(toList());

        return result;
    }

    /**
     * V3-1. 엔티티를 조회해서 DTO로 변환(fetch join 사용O) + default_batch_fetch_size 설정(IN 쿼리)
     * 1. 한계 돌파 방법
     * - xToOne 관계(다대일)에서는 조인해도 데이터가 증가하지 않으므로 fetch join으로 조회
     * - xToMany 일대다(컬렉션 조회) 조인에선 데이터가 증가하므로 지연 로딩으로 설정(fetch join 설정X)
     * - 컬렉션 관계는 hibernate.default_batch_fetch_size, @BatchSize로 최적화(IN 쿼리로 조회하도록 변경)
     *   - hibernate.default_batch_fetch_size: 글로벌 설정
     *   - @BatchSize: 개별 최적화
     * 2. 이렇게 설정하면 페이징 처리도 가능하고, 컬렉션 조회에서도 N + 1 문제와 데이터가 증가되어 중복되는 문제도 해결할 수 있다.
     * 3. 쿼리 호출 수가 N번 -> 1번
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_1Paging(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit) {

        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit); // xToOne 관계에서 만든 패치 조인(member, delivery)
        List<OrderDto> result = orders.stream().map(OrderDto::new)
                .collect(toList());

        return result;
    }

    /** V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
     * - 페이징 가능
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }


    /**
     * V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
     * - 페이징 가능
     * - ToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계인 OrderItem을 한꺼번에 조회
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }


    /**
     * V6: JPA에서 DTO로 직접 조회, 플랫 데이터 최적화(1 Query)
     * 장점
     * - 1번의 쿼리
     * 단점
     * - 페이징 불가능
     * - 애플리케이션에서 추가 작업이 크다.
     * - 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5 보다 더 느릴 수도 있다.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue())).collect(toList());
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
                    .collect(toList());
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
