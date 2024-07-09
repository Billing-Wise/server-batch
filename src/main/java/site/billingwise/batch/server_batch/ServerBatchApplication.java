package site.billingwise.batch.server_batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ServerBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerBatchApplication.class, args);
	}

}
