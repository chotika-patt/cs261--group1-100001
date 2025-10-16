package tu_store.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootApplication
public class DemoApplication {
	public DemoApplication(){
		System.out.println("============================================= Started! =============================================");
	}
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}