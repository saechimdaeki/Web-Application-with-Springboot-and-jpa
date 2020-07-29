package jpabook.jpashop;


import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     *
     * @param member
     * @return
     *
     * 커맨드와 쿼리를 분리하자
     */
    public Long save(Member member){
        em.persist(member);
        return member.getId();
    }
    public Member find(Long id){
        return em.find(Member.class,id);
    }
}
