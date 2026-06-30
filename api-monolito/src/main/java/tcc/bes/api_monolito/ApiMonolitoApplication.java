package tcc.bes.api_monolito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ApiMonolitoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiMonolitoApplication.class, args);
	}

}
