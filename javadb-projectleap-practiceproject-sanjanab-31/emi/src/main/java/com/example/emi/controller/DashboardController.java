package com.example.emi.controller;
import com.example.emi.dto.LoanSummaryDTO;
import com.example.emi.model.EMIPayment;
import com.example.emi.model.Loan;
import com.example.emi.model.User;
import com.example.emi.service.EMIPaymentService;
import com.example.emi.service.EmailService;
import com.example.emi.service.LoanService;
import com.example.emi.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
@Controller
public class DashboardController {
    private final UserService userService;
    private final LoanService loanService;
    private final EmailService emailService;
    private final EMIPaymentService emiPaymentService;
    public DashboardController(UserService userService, LoanService loanService, EmailService emailService, EMIPaymentService emiPaymentService) {
        this.userService = userService;
        this.loanService = loanService;
        this.emailService = emailService;
        this.emiPaymentService = emiPaymentService;
    }
    @GetMapping("/test-email")
    public String testMail(Model model) {
        emailService.sendEmail(
                "sanjana.b0831@gmail.com",
                "Test Email Log",
                "This is a test to check if email logging works fine."
        );
        model.addAttribute("msg", "Email test sent successfully!");
        return "redirect:/dashboard/1";
    }
    @GetMapping("/dashboard/{userId}")
    public String showDashboard(@PathVariable Long userId, Model model) {
        User user = userService.getUserById(userId);
        List<Loan> loans = loanService.getLoansByUserId(userId);
        LoanSummaryDTO summary = loanService.getLoanSummaryForUser(userId);
        model.addAttribute("user", user);
        model.addAttribute("loans", (loans == null) ? List.of() : loans);
        model.addAttribute("summary", summary);
        return "dashboard";
    }
    @GetMapping("/loan/{loanId}/emipayment")
    public String viewEmiPayments(@PathVariable Long loanId, Model model) {
        Loan loan = loanService.findLoanById(loanId);
        List<EMIPayment> payments = emiPaymentService.getPaymentsForLoan(loanId);
        LoanSummaryDTO summary = loanService.getLoanSummary(loanId);
        model.addAttribute("loan", loan);
        model.addAttribute("payments", payments);
        model.addAttribute("summary", summary);
        return "emi-payments"; 
    }
}
