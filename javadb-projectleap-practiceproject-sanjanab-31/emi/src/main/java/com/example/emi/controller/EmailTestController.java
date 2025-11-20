package com.example.emi.controller;
import com.example.emi.service.EmailService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/email")
public class EmailTestController {
    private final EmailService emailService;
    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }
    @GetMapping("/test")
    public String sendTestEmail(@RequestParam String to) {
        emailService.sendEmail(to, "Loan Tracker Test Email",
                "Hello from Loan Tracker!\n\nThis is a test email from your EMI Reminder System.");
        return "Test email sent to: " + to;
    }
}
