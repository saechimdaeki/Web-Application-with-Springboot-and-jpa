package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
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

/**
 * xToOne (ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    @GetMapping("/api/v1/simple-orders") //entity반환하는 안좋은방법
    public List<Order> ordersV1(){
        List<Order> all=orderRepository.findAllByString(new OrderSearch());
        for(Order order:all){
            order.getMember().getName(); //강제 lazy 로딩. (order.getMember())는 프록시객체. getName을붙임으로써 강제초기화시킴
            order.getDelivery().getAddress(); //lazy 강제초기화
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders") //엔티티를 DTO로 변환한 방법 (쿼라가 1+n+n번 실행됨)
    public List<SimpleOrderDto> orderV2(){
        //ORDER 2개
        // N+1 문제 -> 1 + 회원N +배송 N (성능이 좋지는않음...)
        // `order` 조회1번
        // `order -> member` 지연로딩 조회 N번
        // `order -> delivery` 지연 로딩 조회 N번
        // 예) order 결과가 4개면 최악의 경우 1+4+4번 실행된다
        // 지연로딩은 영속성 컨텍스트에서 조회하므로 , 이미 조회된 경우 쿼리를생략한다.
         List<Order> orders=orderRepository.findAllByString(new OrderSearch());

        List<SimpleOrderDto> result=orders.stream()
                .map(o -> new SimpleOrderDto(o))
        .collect(Collectors.toList());

        return result;
    }
    @Data
    static class SimpleOrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId=order.getId();
            name=order.getMember().getName(); //LAZY 초기화
            orderDate=order.getOrderDate();
            orderStatus=order.getStatus();
            address=order.getDelivery().getAddress(); //LAZY 초기화
        }
    }
}
