package tu_store.demo;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import tu_store.demo.services.DatabaseFixService;

@SpringBootApplication
public class DemoApplication {
	public DemoApplication(){
		System.out.println("============================================= Started! =============================================");
	}
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
    public ApplicationRunner runner(DatabaseFixService databaseFixService) {
        return args -> {
            databaseFixService.update();
            System.out.println("Fixed DB Value.");
        };
    }
}
