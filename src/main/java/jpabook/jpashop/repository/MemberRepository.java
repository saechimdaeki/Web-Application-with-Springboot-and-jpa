package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    //@PersistenceContext //스프링이 이 entitymanager만들어서 injection해줌
    private final EntityManager em; //스프링 data jpa에서 이런식 지원가능

    /* 이런식으로 직접주입받는것도 가능
    @PersistenceUnit
    private EntityManagerFactory emf;
     */



    public void save(Member member){
        em.persist(member);
    }

    public Member findOne(Long id){
        return em.find(Member.class,id);
    }

    public List<Member> findAll(){
        // jpql은 sql과달리 entity객체를 대상으로 쿼리함
        List<Member> result=em.createQuery("select m from Member m",Member.class)
        .getResultList();

        return result;
    }
    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name=:name",Member.class)
                .setParameter("name",name)
                .getResultList();
    }
}
