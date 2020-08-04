package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> all=orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems=order.getOrderItems();
            orderItems.stream().forEach(o ->o.getItem().getName());
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> orderV2(){
        List<Order> orders=orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result=orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }

    @GetMapping("/api/v3/orders") //order가 2배로 뻥튀기됨... distinct를 넣으면 해결!. findAllWithItem()메소드를 확인하자
    public List<OrderDto> orderV3(){
        List<Order> orders=orderRepository.findAllWithItem();

        /*
        for(Order order:orders)
            System.out.println("order ref="+order+"id="+order.getId());
        */

        /**
        /* 장점: 패치조인으로 SQL이 1번만 실행됨
        *  단점: 페이지 불가능
        *   컬렉션 페치 조인을 사용하면 페이징이 불가능하다. 하이버네이트는 경고 로그를 남기면서 모든 데이
            터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다).
        **/
        List<OrderDto> result=orders.stream()

                .map(o ->new OrderDto(o))
                .collect(Collectors.toList());
        return result;
    }



    @Getter
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order){
            orderId=order.getId();
            name=order.getMember().getName();
            orderDate=order.getOrderDate();
            orderStatus=order.getStatus();
            address=order.getDelivery().getAddress();
            orderItems=order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }
    @Data
    static class OrderItemDto{

        private String itemName;// 상품 명
        private int orderPrice; // 주문 가격
        private int count; //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName=orderItem.getItem().getName();
            orderPrice=orderItem.getOrderPrice();
            count=orderItem.getCount();
        }
    }
}
