package com.example.emi.service;
import com.example.emi.model.EmailLog;
import com.example.emi.repository.EmailLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;
    private final TemplateEngine templateEngine;
    private final boolean mailEnabled;
    public EmailService(JavaMailSender mailSender, EmailLogRepository emailLogRepository,
                        org.springframework.core.env.Environment env, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
        String enabled = env.getProperty("app.mail.enabled", "false");
        this.mailEnabled = Boolean.parseBoolean(enabled);
        this.templateEngine = templateEngine;
    }
    private static final int MAX_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 1000L; 
    public void sendEmail(String to, String subject, String text) {
        if (!mailEnabled) {
            log.warn("Mail disabled (app.mail.enabled=false). Skipping send to {}", to);
            persistLog(to, subject, "SKIPPED", "Mail disabled by configuration");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        Exception lastEx = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                mailSender.send(message);
                log.info("Email sent to {} (attempt {})", to, attempt);
                persistLog(to, subject, "SUCCESS", null);
                return;
            } catch (Exception e) {
                lastEx = e;
                log.warn("Attempt {} to send email to {} failed: {}", attempt, to, e.getMessage());
                try {
                    Thread.sleep(BASE_BACKOFF_MS * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        String err = lastEx == null ? "unknown" : lastEx.getMessage();
        log.error("Failed to send email to {} after {} attempts: {}", to, MAX_ATTEMPTS, err);
        persistLog(to, subject, "FAILED", err);
    }
    @Async
    public void sendEmailAsync(String to, String subject, String text) {
        sendEmail(to, subject, text);
    }
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        if (!mailEnabled) {
            log.warn("Mail disabled (app.mail.enabled=false). Skipping HTML send to {}", to);
            persistLog(to, subject, "SKIPPED", "Mail disabled by configuration");
            return;
        }
        Exception lastEx = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                mailSender.send(mimeMessage);
                log.info("HTML email sent to {} (attempt {})", to, attempt);
                persistLog(to, subject, "SUCCESS", null);
                return;
            } catch (Exception e) {
                lastEx = e;
                log.warn("Attempt {} to send HTML email to {} failed: {}", attempt, to, e.getMessage());
                try {
                    Thread.sleep(BASE_BACKOFF_MS * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        String err = lastEx == null ? "unknown" : lastEx.getMessage();
        log.error("Failed to send HTML email to {} after {} attempts: {}", to, MAX_ATTEMPTS, err);
        persistLog(to, subject, "FAILED", err);
    }
    public void sendWelcomeEmail(com.example.emi.model.User user) {
        if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
        Context ctx = new Context();
        ctx.setVariable("name", user.getName() == null ? "User" : user.getName());
        String html = templateEngine.process("email/welcome", ctx);
        sendHtmlEmail(user.getEmail(), "Welcome to Loan Tracker", html);
    }
    public void sendLoanCreatedEmail(com.example.emi.model.Loan loan) {
        if (loan == null || loan.getUser() == null || loan.getUser().getEmail() == null) return;
        Context ctx = new Context();
        ctx.setVariable("name", loan.getUser().getName() == null ? "User" : loan.getUser().getName());
        ctx.setVariable("loanId", loan.getLoanId());
        ctx.setVariable("emiAmount", loan.getEmiAmount());
        ctx.setVariable("tenure", loan.getTenureMonths());
        String html = templateEngine.process("email/loan-created", ctx);
        sendHtmlEmail(loan.getUser().getEmail(), "New Loan Created", html);
    }

    private void persistLog(String to, String subject, String status, String errorMessage) {
        try {
            emailLogRepository.save(new EmailLog(
                    to,
                    subject,
                    status,
                    LocalDateTime.now(),
                    errorMessage
            ));
        } catch (Exception e) {
            log.error("Failed to persist EmailLog for {}: {}", to, e.getMessage());
        }
    }
}
