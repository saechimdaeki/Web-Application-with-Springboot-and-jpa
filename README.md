# Web Application with Springboot and jpa
### `김영한님의 스프링 부트와 JPA 활용을 수강하며 JPA 실무역량를 높이기 위한 실습저장소입니다.`

![image](https://user-images.githubusercontent.com/40031858/88916586-4c4e7800-d2a1-11ea-8b71-bf170b74f0d0.png)

![image](https://user-images.githubusercontent.com/40031858/88918910-3f338800-d2a5-11ea-96f2-33a9ad502a1b.png)

```
참고: 실무에서는 @ManyToMany 를 사용하지 말자
> @ManyToMany 는 편리한 것 같지만, 중간 테이블( CATEGORY_ITEM )에 컬럼을 추가할 수 없고, 세밀하게 쿼
리를 실행하기 어렵기 때문에 실무에서 사용하기에는 한계가 있다. 중간 엔티티( CategoryItem 를 만들고
@ManyToOne , @OneToMany 로 매핑해서 사용하자. 정리하면 대다대 매핑을 일대다, 다대일 매핑으로 풀어
내서 사용하자.
```

