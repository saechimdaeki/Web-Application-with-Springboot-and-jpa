package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.hibernate.Hibernate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		/*  lombok setting test
		Hello hello=new Hello();
		hello.setData("hello");
		String data=hello.getData();
		System.out.println(data);
		 */

		SpringApplication.run(JpashopApplication.class, args);
	}

	@Bean //   "/api/v1/simple-orders" 에사용되는방법(안좋은방법예시) 이렇게 hibernate5Module 을사용하기보다는
	//DTO로 변환해서 반환하는것이 더더은방법
	Hibernate5Module hibernate5Module(){
		Hibernate5Module hibernate5Module=new Hibernate5Module();
		//hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING,true);
		return new Hibernate5Module();
	}

}
