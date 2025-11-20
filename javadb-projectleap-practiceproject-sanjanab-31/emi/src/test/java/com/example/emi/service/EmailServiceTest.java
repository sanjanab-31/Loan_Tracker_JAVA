package com.example.emi.service;
import com.example.emi.config.NoOpMailSender;
import com.example.emi.model.EmailLog;
import com.example.emi.repository.EmailLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.thymeleaf.TemplateEngine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
public class EmailServiceTest {
    private EmailLogRepository emailLogRepository;
    private Environment env;
    private TemplateEngine templateEngine;
    @BeforeEach
    void setup() {
        emailLogRepository = Mockito.mock(EmailLogRepository.class);
        env = Mockito.mock(Environment.class);
        templateEngine = Mockito.mock(TemplateEngine.class);
    }
    @Test
    void sendEmail_whenDisabled_persistsSkipped() {
        when(env.getProperty("app.mail.enabled", "false")).thenReturn("false");
    EmailService emailService = new EmailService(new NoOpMailSender(), emailLogRepository, env, templateEngine);
        emailService.sendEmail("test@example.com", "sub", "body");
        ArgumentCaptor<EmailLog> captor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository, times(1)).save(captor.capture());
        EmailLog log = captor.getValue();
        assertThat(log.getRecipient()).isEqualTo("test@example.com");
        assertThat(log.getStatus()).isEqualTo("SKIPPED");
    }
    @Test
    void sendEmail_whenEnabled_persistsSuccess() {
        when(env.getProperty("app.mail.enabled", "false")).thenReturn("true");
    EmailService emailService = new EmailService(new NoOpMailSender(), emailLogRepository, env, templateEngine);
        emailService.sendEmail("user@x.com", "hello", "body");
        ArgumentCaptor<EmailLog> captor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository, times(1)).save(captor.capture());
        EmailLog log = captor.getValue();
        assertThat(log.getRecipient()).isEqualTo("user@x.com");
        assertThat(log.getStatus()).isEqualTo("SUCCESS");
    }
}
