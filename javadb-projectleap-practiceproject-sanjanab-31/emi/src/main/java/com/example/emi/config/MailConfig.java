package com.example.emi.config;
import com.example.emi.config.NoOpMailSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
@Configuration
public class MailConfig {
    // When app.mail.enabled is false (or missing) provide a NoOp JavaMailSender
    @Bean
    @ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
    public JavaMailSender noOpMailSender() {
        return new NoOpMailSender();
    }
}
