package tu_store.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
	public DemoApplication(){
		System.out.println("============================================= Started! =============================================");
	}
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
}
