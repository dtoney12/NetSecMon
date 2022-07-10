package netSecMon.attempt;

import netSecMon.Status;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.LocalDate;
import static java.time.Month.*;

@Configuration
public class AttemptStartupScript {

    @Bean
    CommandLineRunner commandLineRunner(AttemptRepository repository) {
        return args -> {
            Attempt attempt1 = new Attempt(
                    1L,
                    "www.amazon.com",
                    LocalDate.of(2000, JANUARY,4).toString(),
                    Status.RUNNING
            );
            repository.save(attempt1);
        };
    }
}
