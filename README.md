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


```
참고: 주문 서비스의 주문과 주문 취소 메서드를 보면 비즈니스 로직 대부분이 엔티티에 있다. 서비스 계층
은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다. 이처럼 엔티티가 비즈니스 로직을 가지고 객체 지
향의 특성을 적극 활용하는 것을 도메인 모델 패턴(http://martinfowler.com/eaaCatalog/
domainModel.html)이라 한다. 반대로 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분
의 비즈니스 로직을 처리하는 것을 트랜잭션 스크립트 패턴(http://martinfowler.com/eaaCatalog/
transactionScript.html)이라 한다.
```
---
```
> 참고: 요구사항이 정말 단순할 때는 폼 객체( MemberForm ) 없이 엔티티( Member )를 직접 등록과 수정 화면
에서 사용해도 된다. 하지만 화면 요구사항이 복잡해지기 시작하면, 엔티티에 화면을 처리하기 위한 기능이
점점 증가한다. 결과적으로 엔티티는 점점 화면에 종속적으로 변하고, 이렇게 화면 기능 때문에 지저분해진
엔티티는 결국 유지보수하기 어려워진다.

> 실무에서 엔티티는 핵심 비즈니스 로직만 가지고 있고, 화면을 위한 로직은 없어야 한다. 화면이나 API에 맞
는 폼 객체나 DTO를 사용하자. 그래서 화면이나 API 요구사항을 이것들로 처리하고, 엔티티는 최대한 순수
하게 유지하자.
```


## 변경 감지와 병합(merge)
> 정말 중요한 내용
>
>준영속 엔티티?
>
>영속성 컨텍스트가 더는 관리하지 않는 엔티티를 말한다.
>(여기서는 itemService.saveItem(book) 에서 수정을 시도하는 Book 객체다. Book 객체는 이미 DB
>에 한번 저장되어서 식별자가 존재한다. 이렇게 임의로 만들어낸 엔티티도 기존 식별자를 가지고 있으면 준
>영속 엔티티로 볼 수 있다.)
>준영속 엔티티를 수정하는 2가지 방법
>변경 감지 기능 사용
>병합( merge ) 사용

### 변경 감지 기능 사용
```
@Transactional
void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
 Item findItem = em.find(Item.class, itemParam.getId()); //같은 엔티티를 조회한다.
 findItem.setPrice(itemParam.getPrice()); //데이터를 수정한다.
}

영속성 컨텍스트에서 엔티티를 다시 조회한 후에 데이터를 수정하는 방법
트랜잭션 안에서 엔티티를 다시 조회, 변경할 값 선택 트랜잭션 커밋 시점에 변경 감지(Dirty Checking)
이 동작해서 데이터베이스에 UPDATE SQL 실행
```

### 병합 사용
#### 병합은 준영속 상태의 엔티티를 영속 상태로 변경할 때 사용하는 기능이다
```
@Transactional
void update(Item itemParam) { //itemParam: 파리미터로 넘어온 준영속 상태의 엔티티
 Item mergeItem = em.merge(item);
}
```
## 병합 동작 방식

>1.merge() 를 실행한다.
>
>2.파라미터로 넘어온 준영속 엔티티의 식별자 값으로 1차 캐시에서 엔티티를 조회한다.
>
>2-1. 만약 1차 캐시에 엔티티가 없으면 데이터베이스에서 엔티티를 조회하고, 1차 캐시에 저장한다.
>
>3.조회한 영속 엔티티( mergeMember )에 member 엔티티의 값을 채워 넣는다. (member 엔티티의 모든 값
을 mergeMember에 밀어 넣는다. 이때 mergeMember의 “회원1”이라는 이름이 “회원명변경”으로 바
뀐다.)
>
>4.영속 상태인 mergeMember를 반환한다.

### 병합시 동작 방식을 간단히 정리
>1.준영속 엔티티의 식별자 값으로 영속 엔티티를 조회한다.
>
>2.영속 엔티티의 값을 준영속 엔티티의 값으로 모두 교체한다.(병합한다.)
>
>3.트랜잭션 커밋 시점에 변경 감지 기능이 동작해서 데이터베이스에 UPDATE SQL이 실행
>
>주의: 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만, 병합을 사용하면 모든 속성이
변경된다. 병합시 값이 없으면 null 로 업데이트 할 위험도 있다. (병합은 모든 필드를 교체한다.)

> 참고: save() 메서드는 식별자를 자동 생성해야 정상 동작한다. 여기서 사용한 Item 엔티티의 식별자는
자동으로 생성되도록 @GeneratedValue 를 선언했다. 따라서 식별자 없이 save() 메서드를 호출하면
persist() 가 호출되면서 식별자 값이 자동으로 할당된다. 반면에 식별자를 직접 할당하도록 @Id 만 선언
했다고 가정하자. 이 경우 식별자를 직접 할당하지 않고, save() 메서드를 호출하면 식별자가 없는 상태로
persist() 를 호출한다. 그러면 식별자가 없다는 예외가 발생한다.
> 참고: 실무에서는 보통 업데이트 기능이 매우 재한적이다. 그런데 병합은 모든 필드를 변경해버리고, 데이터
가 없으면 null 로 업데이트 해버린다. 병합을 사용하면서 이 문제를 해결하려면, 변경 폼 화면에서 모든 데
이터를 항상 유지해야 한다. 실무에서는 보통 변경가능한 데이터만 노출하기 때문에, 병합을 사용하는 것이
오히려 번거롭다.


# `가장 좋은 해결 방법`
>엔티티를 변경할 때는 항상 변경 감지를 사용하세요
>
>컨트롤러에서 어설프게 엔티티를 생성하지 마세요.
>
>트랜잭션이 있는 서비스 계층에 식별자( id )와 변경할 데이터를 명확하게 전달하세요.(파라미터 or dto)
>
>트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티를 조회하고, 엔티티의 데이터를 직접 변경하세요.
>
>트랜잭션 커밋 시점에 변경 감지가 실행됩니다.

----
# API PART

>주의: 지연 로딩(LAZY)을 피하기 위해 즉시 로딩(EARGR)으로 설정하면 안된다!
> 
>즉시 로딩 때문에 연관관계가 필요 없는 경우에도 데이터를 항상 조회해서 성능 문제가 발생할 수 있다. 
>
>즉시 로딩으로 설정하면 성능튜닝이 매우 어려워 진다.
>
> 항상 지연 로딩을 기본으로 하고, 성능 최적화가 필요한 경우에는 페치 조인(fetch join)을 사용해라

```
엔티티를 DTO로 변환하거나, DTO로 바로 조회하는 두가지 방법은 각각 장단점이 있다. 둘중 상황에 따라
서 더 나은 방법을 선택하면 된다. 엔티티로 조회하면 리포지토리 재사용성도 좋고, 개발도 단순해진다. 따라
서 권장하는 방법은 다음과 같다.
쿼리 방식 선택 권장 순서
1. 우선 엔티티를 DTO로 변환하는 방법을 선택한다.
2. 필요하면 페치 조인으로 성능을 최적화 한다. 대부분의 성능 이슈가 해결된다.
3. 그래도 안되면 DTO로 직접 조회하는 방법을 사용한다.
4. 최후의 방법은 JPA가 제공하는 네이티브 SQL이나 스프링 JDBC Template을 사용해서 SQL을 직접 사
용한다.
```
----

> 컬렉션 페치 조인을 사용하면 페이징이 불가능하다. 하이버네이트는 경고 로그를 남기면서 모든 데이
터를 DB에서 읽어오고, 메모리에서 페이징 해버린다(매우 위험하다).
> 
> 컬렉션 페치 조인은 1개만 사용할 수 있다. 컬렉션 둘 이상에 페치 조인을 사용하면 안된다. 데이터가
부정합하게 조회될 수 있다.

===
## 컬렉션을 fetch join하면 페이징이 불가능하다.
- 컬렉션을 패치 조인하면 일대다 조인이 발생하므로 데이터가 예측할 수 없이 증가한다.
- 일대다에서 일(1)을 기준으로 페이징을 하는 것이 목적이다. 그런데 데이터는 다(N)을 기준으로 row가생성된다
- Order를 기준으로 페이징하고 싶은데,다(N)인 OrderItem을 조인하면 OrderItem이 기준이 되어버린다.
- 이 경우 하이버네이트는 경고 로그를 남기고 모든 DB데이터를 읽어서 메모리에서 페이징을 시도한다.
- (최악의 경우 장애로 이어질수있다.)

## 한계돌파
### 그러면 페이징+ 컬렉션 엔티티를 함께조회하려면 어떻게 해야할까?

> 먼저 ToOne(OneToOne,ManyToOne)관계를 모두 fetch join한다. ToOne관계는 row수를 증가시키지 않으므로 페이징쿼리에 영향을 주지않음.
>
> 컬렉션은 지연로딩으로 조회한다
>
> 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size, @BatchSize를 적용한다.
>
> hibernate.default_batch_fetch_size: 글로벌설정
>
>@BatchSize: 개별 최적화
>
>이 옵션을 사용하면 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size만큼 IN쿼리로 조회함.

```
장점: 
1. 쿼리 호출 수가 1+N -> 1+1로 최적화된다.
2. 조인보다 DB데이터 전송량이 최적화 된다 
3. 패치 조인 방식과 비교해서 쿼리호출수가 약간 증가하지만 ,DB데이터 전송량이 감소
4. 컬렉션 패치조인은 페이징이 불가능하지만 이방법은 페이징이 가능

결론:  ToOne관계는 fetch join해도 페이징에 영향을 주지않는다.
따라서 ToOne관계는 fetch join으로 쿼리수를줄이고 해결하고, 나머지는 hibernate.defaultbatch_fetch_size
로 최적화하자.
```

>참고:default_batch_fetch_size 의 크기는 적당한 사이즈를 골라야 하는데, 100~1000 사이를 선택
하는 것을 권장한다. 이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절 파라미터를 1000으
로 제한하기도 한다. 1000으로 잡으면 한번에 1000개를 DB에서 애플리케이션에 불러오므로 DB에 순간
부하가 증가할 수 있다. 하지만 애플리케이션은 100이든 1000이든 결국 전체 데이터를 로딩해야 하므로
메모리 사용량이 같다. 1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션이든 순간 부
하를 어디까지 견딜 수 있는지로 결정하면 된다.

------

###  `권장 순서`
```
엔티티 조회 방식으로 우선 접근

    페치조인으로 쿼리 수를 최적화

    컬렉션 최적화
        페이징 필요 hibernate.default_batch_fetch_size , @BatchSize 로 최적화
        페이징 필요X 페치 조인 사용

엔티티 조회 방식으로 해결이 안되면 DTO 조회 방식 사용
DTO 조회 방식으로 해결이 안되면 NativeSQL or 스프링 JdbcTemplate
```
>참고: 엔티티 조회 방식은 페치 조인이나, hibernate.default_batch_fetch_size , @BatchSize 같이
>
>코드를 거의 수정하지 않고, 옵션만 약간 변경해서, 다양한 성능 최적화를 시도할 수 있다. 반면에 DTO를 직
>
>접 조회하는 방식은 성능을 최적화 하거나 성능 최적화 방식을 변경할 때 많은 코드를 변경해야 한다.
>
> 참고: 개발자는 성능 최적화와 코드 복잡도 사이에서 줄타기를 해야 한다. 항상 그런 것은 아니지만, 보통 성
>
>능 최적화는 단순한 코드를 복잡한 코드로 몰고간다.
> 엔티티 조회 방식은 JPA가 많은 부분을 최적화 해주기 때문에, 단순한 코드를 유지하면서, 성능을 최적화 할
>
>수 있다.
> 반면에 DTO 조회 방식은 SQL을 직접 다루는 것과 유사하기 때문에, 둘 사이에 줄타기를 해야 한다


---

## OSIV와 성능 최적화
- Open Session In View:하이버네이트
- Open EntityManager In View: JPA

## OSIV ON
![image](https://user-images.githubusercontent.com/40031858/89491074-587a8e00-d7e9-11ea-80a3-6e4b9781a370.png)

- spring.jpa.open-in-view: true가 기본값

### OSIV 전략은 트랜잭션 시작처럼 최초 데이터베이스 커넥션 시작 시점부터 API 응답이 끝날 때 까지 영속성
### 컨텍스트와 데이터베이스 커넥션을 유지한다. 그래서 지금까지 View Template이나 API 컨트롤러에서 지
### 연 로딩이 가능했던 것이다.
### 지연 로딩은 영속성 컨텍스트가 살아있어야 가능하고, 영속성 컨텍스트는 기본적으로 데이터베이스 커넥션
### 을 유지한다. 이것 자체가 큰 장점이다.
### 그런데 이 전략은 너무 오랜시간동안 데이터베이스 커넥션 리소스를 사용하기 때문에, 실시간 트래픽이 중
### 요한 애플리케이션에서는 커넥션이 모자랄 수 있다. 이것은 결국 장애로 이어진다.
### 예를 들어서 컨트롤러에서 외부 API를 호출하면 외부 API 대기 시간 만큼 커넥션 리소스를 반환하지 못하
### 고, 유지해야 한다.

## OSIV OFF
![image](https://user-images.githubusercontent.com/40031858/89495849-1f93e680-d7f4-11ea-89e2-42c0934e16fb.png)

- spring.jpa.open-in-view: false OSIV종료

### OSIV를 끄면 트랜잭션을 종료할 때 영속성 컨텍스트를 닫고, 데이터베이스 커넥션도 반환한다. 따라서 커
### 넥션 리소스를 낭비하지 않는다.
### OSIV를 끄면 모든 지연로딩을 트랜잭션 안에서 처리해야 한다. 따라서 지금까지 작성한 많은 지연 로딩 코
### 드를 트랜잭션 안으로 넣어야 하는 단점이 있다. 그리고 view template에서 지연로딩이 동작하지 않는다.
### 결론적으로 트랜잭션이 끝나기 전에 지연 로딩을 강제로 호출해 두어야 한다

--- 
# 결론:
> 고객서비스의 실시간 API는 OSIV를 끄고, ADMIN처럼 커넥션을 많이 사용하지않는 곳에서는 
> OSIV를 키자.

## Spring DATA JPA
https://spring.io/projects/spring-data

> 스프링 데이터 JPA는 JpaRepository라는 인터페이스를 제공하는데,
>
> 여가에 기본적인 CRUD기능이 모두제공된다.
>findByName 처럼 일반화 하기 어려운 기능도 메서드 이름으로 정확한 JPQL 쿼리를 실행한다.
>
>select m from member m where m.name = :name
>개발자는 인터페이스만 만들면 된다. 구현체는 스프링 데이터 JPA가 애플리케이션 실행시점에 주입해준다.
>
>스프링 데이터 JPA는 스프링과 JPA를 활용해서 애플리케이션을 만들때 정말 편리한 기능을 많이 제공한
다. 
>
>단순히 편리함을 넘어서 때로는 마법을 부리는 것 같을 정도로 놀라운 개발 생산성의 세계로 우리를 이끌
어 준다.
>
>하지만 스프링 데이터 JPA는 JPA를 사용해서 이런 기능을 제공할 뿐이다. 결국 JPA 자체를 잘 이해하는
것이 가장 중요하다.(중요중요쓰~)

## QueryDSL
http://www.querydsl.com/
### 실무에서 조건에 따라 실행되는쿼리가 달라지는 동적쿼리를 사용한다. 이럴때 사용

[QueryDsl gradle설정하는법](https://velog.io/@aidenshin/Querydsl-Gradle-%EC%84%A4%EC%A0%95)


#### querydsl세팅을 마친 build.gradle 파일
```
plugins {
	id 'org.springframework.boot' version '2.1.16.RELEASE'
	id 'io.spring.dependency-management' version '1.0.9.RELEASE'
	id "com.ewerk.gradle.plugins.querydsl" version "1.0.10"
	id 'java'
}

apply plugin: "com.ewerk.gradle.plugins.querydsl"

group = 'jpabook'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '8'

configurations {
	compileOnly {
		extendsFrom annotationProcessor
	}
}
apply plugin: 'io.spring.dependency-management'
apply plugin: "com.ewerk.gradle.plugins.querydsl"
repositories {
	mavenCentral()
}

dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
	implementation 'org.springframework.boot:spring-boot-starter-web'
	implementation 'org.springframework.boot:spring-boot-devtools'
	implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.5.6'
	implementation 'com.querydsl:querydsl-jpa'
	implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
	compileOnly 'org.projectlombok:lombok'
	runtimeOnly 'com.h2database:h2'
	annotationProcessor 'org.projectlombok:lombok'
	testImplementation 'org.springframework.boot:spring-boot-starter-test'

	//querydsl 추가
	implementation 'com.querydsl:querydsl-jpa'
//querydsl 추가
	implementation 'com.querydsl:querydsl-apt'
}

def querydslDir = "$buildDir/generated/querydsl"
querydsl {
	library = "com.querydsl:querydsl-apt"
	jpa = true
	querydslSourcesDir = querydslDir
}
sourceSets {
	main {
		java {
			srcDirs = ['src/main/java', querydslDir]
		}
	}
}
compileQuerydsl{
	options.annotationProcessorPath = configurations.querydsl
}
configurations {
	querydsl.extendsFrom compileClasspath
}
```

>Querydsl은 SQL(JPQL)과 모양이 유사하면서 자바 코드로 동적 쿼리를 편리하게 생성할 수 있다.
>
>실무에서는 복잡한 동적 쿼리를 많이 사용하게 되는데, 이때 Querydsl을 사용하면 높은 개발 생산성을 얻
>
>으면서 동시에 쿼리 오류를 컴파일 시점에 빠르게 잡을 수 있다.
>
>꼭 동적 쿼리가 아니라 정적 쿼리인 경우에도 다음과 같은 이유로 Querydsl을 사용하는 것이 좋다.
>직관적인 문법
>
>컴파일 시점에 빠른 문법 오류 발견
>
>코드 자동완성
>
>코드 재사용(이것은 자바다)
>
>JPQL new 명령어와는 비교가 안될 정도로 깔끔한 DTO 조회를 지원한다.
>
>Querydsl은 JPQL을 코드로 만드는 빌더 역할을 할 뿐이다. 따라서 JPQL을 잘 이해하면 금방 배울 수 있
다.
>
>Querydsl은 JPA로 애플리케이션을 개발 할 때 선택이 아닌 필수라 생각한다.

