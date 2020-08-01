# Web Application with Springboot and jpa
### `김영한님의 스프링 부트와 JPA 활용을 수강하며 JPA 실무역량를 높이기 위한 실습저장소입니다.`

### [h2 1.4.199 버젼으로진행. ](https://h2database.com/h2-2019-03-13.zip)
![image](https://user-images.githubusercontent.com/40031858/88916586-4c4e7800-d2a1-11ea-8b71-bf170b74f0d0.png)

![image](https://user-images.githubusercontent.com/40031858/88918910-3f338800-d2a5-11ea-96f2-33a9ad502a1b.png)

```
참고: 실무에서는 @ManyToMany 를 사용하지 말자
> @ManyToMany 는 편리한 것 같지만, 중간 테이블( CATEGORY_ITEM )에 컬럼을 추가할 수 없고, 세밀하게 쿼
리를 실행하기 어렵기 때문에 실무에서 사용하기에는 한계가 있다. 중간 엔티티( CategoryItem 를 만들고
@ManyToOne , @OneToMany 로 매핑해서 사용하자. 정리하면 대다대 매핑을 일대다, 다대일 매핑으로 풀어
내서 사용하자.
```
#### 엔티티 설계시 주의점
##### 엔티티에는 가급적 Setter를 사용하지 말자
#### Setter가 모두 열려있다. 변경 포인트가 너무 많아서, 유지보수가 어렵다. 나중에 리펙토링으로 Setter 제거
#### 모든 연관관계는 지연로딩으로 설정!
#### 즉시로딩( EAGER )은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 특히 JPQL을 실행할 때 N+1
#### 문제가 자주 발생한다.
#### 실무에서 모든 연관관계는 지연로딩( LAZY )으로 설정해야 한다.
#### 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
#### @XToOne(OneToOne, ManyToOne) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.
#### 컬렉션은 필드에서 초기화 하자.
#### 컬렉션은 필드에서 바로 초기화 하는 것이 안전하다.
#### null 문제에서 안전하다.
#### 하이버네이트는 엔티티를 영속화 할 때, 컬랙션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다. 
#### 만약 getOrders() 처럼 임의의 메서드에서 컬력션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다. 
#### 따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.
```
Member member = new Member();
System.out.println(member.getOrders().getClass());
em.persist(team);
System.out.println(member.getOrders().getClass());
//출력 결과
class java.util.ArrayList
class org.hibernate.collection.internal.PersistentBag
```