package com.example.emi.scheduler;
import com.example.emi.model.EMIPayment;
import com.example.emi.model.Loan;
import com.example.emi.model.User;
import com.example.emi.repository.EMIPaymentRepository;
import com.example.emi.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.*;
public class EMIReminderSchedulerTest {
    private EMIPaymentRepository emiPaymentRepository;
    private EmailService emailService;
    private EMIReminderScheduler scheduler;
    @BeforeEach
    void setup() {
        emiPaymentRepository = Mockito.mock(EMIPaymentRepository.class);
        emailService = Mockito.mock(EmailService.class);
        scheduler = new EMIReminderScheduler(emiPaymentRepository, emailService);
    }
    @Test
    void sendReminders_sendsUpcomingPending() {
        LocalDate today = LocalDate.now();
        User user = new User();
        user.setName("Sam");
        user.setEmail("sam@example.com");
    Loan loan = new Loan();
    loan.setLoanType("Car");
    loan.setEmiAmount(new java.math.BigDecimal("1500.00"));
    loan.setUser(user);
        EMIPayment p = new EMIPayment();
        p.setLoan(loan);
        p.setDueDate(today.plusDays(1));
        p.setStatus("PENDING");
        when(emiPaymentRepository.findByStatusAndDueDateBetween(eq("PENDING"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(p));
        scheduler.sendReminders();
        verify(emailService, times(1)).sendEmailAsync(eq("sam@example.com"), anyString(), anyString());
    }
}
