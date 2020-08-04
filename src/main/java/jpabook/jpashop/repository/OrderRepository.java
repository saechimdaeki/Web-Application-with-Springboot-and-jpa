package jpabook.jpashop.repository;


import jpabook.jpashop.domain.Order;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class,id);
    }

    //jpa 동적으로 만들기

    /**
     * 1번째방법 String 으로 하는방법
     * 안쓰는게 좋은방법.
     */
    public List<Order> findAllByString(OrderSearch orderSearch){
        String jpql="select o from Order o join o.member m";
        boolean isFirstCondition=true;

        //주문 상태 검색
        if(orderSearch.getOrderStatus()!=null){
            if(isFirstCondition){
                jpql+=" where";
                isFirstCondition=false;
            }else{
                jpql+=" and";
            }
            jpql+=" o.status = :status";
        }

        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            if(isFirstCondition){
                jpql+=" where";
                isFirstCondition=false;
            }else{
                jpql += " and";
            }
            jpql+=" m.name like :name";
        }

        TypedQuery<Order> query=em.createQuery(jpql,Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus()!=null){
            query=query.setParameter("status",orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())){
            query=query.setParameter("name",orderSearch.getMemberName());
        }
        /* JPQL을 문자로 생성하는것은 정말번거롭고 실수가잦을수있다..*/
       return query.getResultList();
    }

     /**
     * 2번째 방법 JPA Criteria 방법
     * 참고: 김영한님 피셜로 이건 사람이 쓰라고 만든방법이아니라고함..(실무에서 유지보수 개어려움)
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb=em.getCriteriaBuilder();
        CriteriaQuery<Order> cq=cb.createQuery(Order.class);
        Root<Order> o=cq.from(Order.class);
        Join<Object,Object> m=o.join("member", JoinType.INNER);

        List<Predicate> criteria=new ArrayList<>();

        //주문 상태 검색
        if(orderSearch.getOrderStatus()!=null){
            Predicate status=cb.equal(o.get("status"),orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            Predicate name=cb.like(m.<String>get("name"),"%"+orderSearch.getMemberName()+"%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query=em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }


    /**
     * 3번째 방법 Querydsl 방법
     * 이방법이 실무에서 조건에 따라 실행되는 쿼리가달라지는 동적쿼리에서 많이사용되는방법
     */
    /*
    public List<Order> findAll(OrderSearch orderSearch){
        QOrder order=Qorder.order;
        QMember member=QMember.member;
        return query
                .select(order)
                .from(order)
                .join(order.member,member)
                .where(statusEq(orderSearch.getOrderStatus()),
                        nameLike(orderSearch.getMemberName()))
                .Limit(1000)
                .fetch();
    }
    */

    public List<Order> findAllWithMemberDelivery() {
       return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery",Order.class //100% 이해해야함. 실무에서 jpql을 사용하기위해선
        ).getResultList();
    }


    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o" + //db의 distinct키워드를 날려주고 엔티티가 중복인경우 그 중복을 걸러서 컬렉션에 넣어줌,
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch oi.item i",Order.class)
                //.setFirstResult(1)
                //.setMaxResults(100) 페이징 불가!
                .getResultList();
    }
}
