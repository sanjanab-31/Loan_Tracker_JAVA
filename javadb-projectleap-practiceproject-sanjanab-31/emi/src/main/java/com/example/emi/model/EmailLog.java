package com.example.emi.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "email_logs")
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String recipient;
    private String subject;
    private String status;
    private LocalDateTime sentAt;
    @Column(length = 1000)
    private String errorMessage;
    public EmailLog() {}
    public EmailLog(String recipient, String subject, String status, LocalDateTime sentAt, String errorMessage) {
        this.recipient = recipient;
        this.subject = subject;
        this.status = status;
        this.sentAt = sentAt;
        this.errorMessage = errorMessage;
    }
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
