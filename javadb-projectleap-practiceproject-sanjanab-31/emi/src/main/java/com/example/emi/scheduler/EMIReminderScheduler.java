package com.example.emi.scheduler;
import com.example.emi.model.EMIPayment;
import com.example.emi.repository.EMIPaymentRepository;
import com.example.emi.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
@Component
public class EMIReminderScheduler {
    private final EMIPaymentRepository emiPaymentRepository;
    private final EmailService emailService;
    public EMIReminderScheduler(EMIPaymentRepository emiPaymentRepository, EmailService emailService) {
        this.emiPaymentRepository = emiPaymentRepository;
        this.emailService = emailService;
    }
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendReminders() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysLater = today.plusDays(3);
        List<EMIPayment> upcoming = emiPaymentRepository.findByStatusAndDueDateBetween("PENDING", today, threeDaysLater);
        for (EMIPayment payment : upcoming) {
            var loan = payment.getLoan();
            var user = loan.getUser();
            String subject = "Upcoming EMI Reminder";
            String text = String.format(
                    "Dear %s,\n\nYour EMI of ₹%.2f for your %s loan is due on %s.\n\nPlease make the payment on time.\n\n- Loan Tracker System",
                    user.getName(),
                    loan.getEmiAmount(),
                    loan.getLoanType(),
                    payment.getDueDate()
            );
            try {
                emailService.sendEmailAsync(user.getEmail(), subject, text);
            } catch (Exception ex) {
                org.slf4j.LoggerFactory.getLogger(EMIReminderScheduler.class)
                        .error("Failed to initiate async send for {}: {}", user.getEmail(), ex.getMessage());
            }
        }
        org.slf4j.LoggerFactory.getLogger(EMIReminderScheduler.class)
                .info("Reminder emails processed for {} upcoming EMIs.", upcoming.size());
    }
}
