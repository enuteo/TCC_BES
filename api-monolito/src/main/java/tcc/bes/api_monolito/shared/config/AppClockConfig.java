package tcc.bes.api_monolito.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppClockConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
