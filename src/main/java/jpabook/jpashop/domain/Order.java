package jpabook.jpashop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;
    /* order와 member는 다 대 일 관계계 */

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<OrderItem> orderItems=new ArrayList<>();
    /* X TO many는 기본이 lazy. */

    /*
    아래와 같은 상황의 경우
    persist(orderItemA)
    persist(orderItemB)
    persist(orderItemC)
    persist(order)
=========================
    persist(order)
    cascade할경우 아래의 persist(order)만하면된다
    cascade는 persist를 전파함.

     */
    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    //order_date로 바뀜.
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문상태 [ORDER,CANCEL]

    //== 연관관계 메소드==//
    public void setMember(Member member){
        this.member=member;
        member.getOrders().add(this);
        /*
         member.getOrders().add(order);
        order.setMember(member); 이뜻임.
         */
    }

    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery){
        this.delivery=delivery;
        delivery.setOrder(this);
    }

}
