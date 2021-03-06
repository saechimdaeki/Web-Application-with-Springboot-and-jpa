package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded /* 내장타입 사용했다고 표시*/
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member") //order에잇는 memberfield에의해 매핑됨.
    private List<Order> orders=new ArrayList<>();

    /* 멤버입장에서 리스트는 일대 다 관계 */


}
