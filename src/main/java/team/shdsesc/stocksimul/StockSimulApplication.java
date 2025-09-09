package team.shdsesc.stocksimul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StockSimulApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockSimulApplication.class, args);
    }
}
