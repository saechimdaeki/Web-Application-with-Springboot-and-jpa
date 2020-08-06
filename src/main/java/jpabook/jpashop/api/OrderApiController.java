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
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;


@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;
    /* v1 ~ v3.1은 엔티티조회 */
    @GetMapping("/api/v1/orders") //엔티티를 조회해서 그대로반환
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

    @GetMapping("/api/v2/orders") //엔티티 조회후 dto로 변환
    public List<OrderDto> orderV2(){
        List<Order> orders=orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> result=orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }

    private final OrderQueryService orderQueryService;
    @GetMapping("/api/v3/orders") //order가 2배로 뻥튀기됨... distinct를 넣으면 해결!. findAllWithItem()메소드를 확인하자
    public List<jpabook.jpashop.service.query.OrderDto> orderV3(){
        /*
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
        /*
        List<OrderDto> result=orders.stream()

                .map(o ->new OrderDto(o))
                .collect(toList());
        return result;
        /*
         */
        return orderQueryService.orderV3();

    }

    @GetMapping("/api/v3.1/orders") //
    public List<OrderDto> orderV3_page(
            @RequestParam(value = "offset",defaultValue = "0") int offset,
            @RequestParam(value = "limit",defaultValue = "100") int limit){

        List<Order> orders=orderRepository.findAllWithMemberDelivery(offset,limit);
        
        List<OrderDto> result=orders.stream()
                .map(o ->new OrderDto(o))
                .collect(toList());

        return result;
    }

    /* v4에서 v6까지는 dto 직접 조회*/
    @GetMapping("/api/v4/orders") //jpa에서 dto를 직접조회
    public List<OrderQueryDto> ordersV4(){
            return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders") //일대다 관계인 컬렉션은 IN절을 활용해 메모리에서 미리조회해서 최적화
    public List<OrderQueryDto> ordersV5(){
        return orderQueryRepository.findAllByDto_optimization();
    }


    /**
     * 장점: Query: 1번
     *
     * 단점----------------------------
     * 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되
     * 므로 상황에 따라 V5 보다 더 느릴 수 도 있다.
     * 애플리케이션에서 추가 작업이 크다.
     * 페이징 불가능
     **/

    @GetMapping("/api/v6/orders") //join 결과를 그대로 조회후 어플리케이션에서 원하는모양으로 직접변환
    public List<OrderQueryDto> ordersV6(){
        List<OrderFlatDto> flats= orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
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
                    .collect(toList());
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

/**   p.s.
 * DTO로 조회하는 방법도 각각 장단이 있다. V4, V5, V6에서 단순하게 쿼리가 1번 실행된다고 V6이 항상
 * 좋은 방법인 것은 아니다.
 * V4는 코드가 단순하다. 특정 주문 한건만 조회하면 이 방식을 사용해도 성능이 잘 나온다. 예를 들어서 조회
 * 한 Order 데이터가 1건이면 OrderItem을 찾기 위한 쿼리도 1번만 실행하면 된다.
 * V5는 코드가 복잡하다. 여러 주문을 한꺼번에 조회하는 경우에는 V4 대신에 이것을 최적화한 V5 방식을 사
 * 용해야 한다. 예를 들어서 조회한 Order 데이터가 1000건인데, V4 방식을 그대로 사용하면, 쿼리가 총 1
 * + 1000번 실행된다. 여기서 1은 Order 를 조회한 쿼리고, 1000은 조회된 Order의 row 수다. V5 방식
 * 으로 최적화 하면 쿼리가 총 1 + 1번만 실행된다. 상황에 따라 다르겠지만 운영 환경에서 100배 이상의 성
 * 능 차이가 날 수 있다.
 * V6는 완전히 다른 접근방식이다. 쿼리 한번으로 최적화 되어서 상당히 좋아보이지만, Order를 기준으로 페
 * 이징이 불가능하다. 실무에서는 이정도 데이터면 수백이나, 수천건 단위로 페이징 처리가 꼭 필요하므로, 이
 * 경우 선택하기
 */
