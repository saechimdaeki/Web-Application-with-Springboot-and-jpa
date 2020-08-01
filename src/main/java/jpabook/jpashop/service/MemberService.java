package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true) //readonly=true이면 jpa최적화가 더 도움이됨. (읽기전용에는 읽기용모드로만)
@RequiredArgsConstructor  //final 있는 필드만 생성자만들어줌.
public class MemberService {

    private final MemberRepository memberRepository;

    /* set메소드 인젝션 방법
    @Autowired
    public void setMemberRepository(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }
    */

    /* 생성자 인젝션 방법
    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        //요즘 권장하는 injection 방법.(생성자 인젝션)
        // 생성자가 하나있는경우에는 @Autowired안써도됨.(스프링이자동으로해주기때문)
    }
     */

    /**
     * 회원가입
     * @param member
     * @return
     * */
    @Transactional //이것은 쓰기용이니 따로 명시하자
    public Long join(Member member){

        validateDuplicateMember(member); //중복회원검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member){
        //exception
        List<Member> findMembers=memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다");
        }
    }

    //회원 전체 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }


    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }
}
