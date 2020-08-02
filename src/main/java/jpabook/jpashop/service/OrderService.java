package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.DeliveryStatus;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.omg.CORBA.ORB;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId,Long itemId,int count){

        //엔티티 조회
        Member member= memberRepository.findOne(memberId);
        Item item= itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery=new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);
        //주문상품 생성
       OrderItem orderItem= OrderItem.createOrderItem(item,item.getPrice(),count);

       //OrderItem orderItem11 =new OrderItem();
       //orderItem11.setCount(); 이런식으로도 직접생성 할수있으니 다른식의 생성을 막아보자(OrderItem class를살펴보자)
        //protected 생성자로써 직접생성을 제약함.

        //주문 생성
        Order order=Order.createOrder(member,delivery,orderItem);

        //주문 저장
        orderRepository.save(order);

        return order.getId();
    }

    /**
     * 주문 취소
     */
    @Transactional
    public void cancelOrder(Long orderId){

        //주문 엔티티 조회
        Order order=orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();
    }

    //검색
    /*
    public List<Order> findOrders(OrderSearch orderSearch){
        return orderRepository.findAll(orderSearch);
    }

     */
}
