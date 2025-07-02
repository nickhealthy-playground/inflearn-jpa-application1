package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderFlatDto {

    private final Long orderId;
    private final String name;
    private final LocalDateTime orderDate; //주문시간
    private final Address address;
    private final OrderStatus orderStatus;

    private final String itemName;//상품 명
    private final int orderPrice; //주문 가격
    private final int count; //주문 수량

    public OrderFlatDto(Long orderId, String name, LocalDateTime orderDate,
                        OrderStatus orderStatus, Address address, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.itemName = itemName;
        this.orderPrice = orderPrice;
        this.count = count;
    }

}
