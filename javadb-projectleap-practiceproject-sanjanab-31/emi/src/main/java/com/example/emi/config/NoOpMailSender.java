package com.example.emi.config;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import java.io.InputStream;
/**
 * A minimal JavaMailSender implementation which does nothing.
 * Useful for local/dev when real email sending is disabled.
 */
public class NoOpMailSender implements JavaMailSender {
    @Override
    public MimeMessage createMimeMessage() {
        return null;
    }
    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) {
        return null;
    }
    @Override
    public void send(MimeMessage mimeMessage) {
        // no-op
    }
    @Override
    public void send(MimeMessage... mimeMessages) {
        // no-op
    }
    @Override
    public void send(MimeMessagePreparator mimeMessagePreparator) {
        // no-op
    }
    @Override
    public void send(MimeMessagePreparator... mimeMessagePreparators) {
        // no-op
    }
    @Override
    public void send(SimpleMailMessage simpleMessage) {
        // no-op
    }
    @Override
    public void send(SimpleMailMessage... simpleMessages) {
        // no-op
    }
}
