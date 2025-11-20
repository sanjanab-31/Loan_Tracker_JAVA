package com.example.emi.controller;
import com.example.emi.dto.LoanSummaryDTO;
import com.example.emi.model.EMIPayment;
import com.example.emi.model.Loan;
import com.example.emi.repository.EMIPaymentRepository;
import com.example.emi.service.EMIPaymentService;
import com.example.emi.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
@Controller
@RequestMapping("/api/emipayments")
public class EMIPaymentController {
    private final EMIPaymentService emiPaymentService;
    private final LoanService loanService;
    private final EMIPaymentRepository emiPaymentRepository;
    public EMIPaymentController(EMIPaymentService emiPaymentService,
                                LoanService loanService,
                                EMIPaymentRepository emiPaymentRepository) {
        this.emiPaymentService = emiPaymentService;
        this.loanService = loanService;
        this.emiPaymentRepository = emiPaymentRepository;
    }
    @PostMapping
    public ResponseEntity<EMIPayment> recordPayment(@RequestBody EMIPayment payment) {
        if (payment.getPaymentDate() == null) payment.setPaymentDate(LocalDate.now());
        EMIPayment saved = loanService.recordPayment(payment.getLoan().getLoanId(), payment);
        return ResponseEntity.created(URI.create("/api/emipayments/" + saved.getPaymentId())).body(saved);
    }
    @GetMapping("/loan/{id}")
    @ResponseBody
    public ResponseEntity<List<EMIPayment>> getPaymentsForLoan(@PathVariable Long id) {
        return ResponseEntity.ok(emiPaymentService.getPaymentsForLoan(id));
    }
    @PostMapping("/{id}/mark-paid")
    @ResponseBody
    public String markAsPaid(@PathVariable Long id) {
        EMIPayment payment = emiPaymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        payment.setStatus("PAID");
        payment.setPaymentDate(LocalDate.now());
        payment.setAmountPaid(payment.getLoan().getEmiAmount());
        emiPaymentRepository.save(payment);
        return "EMI marked as paid successfully!";
    }
    @PostMapping("/pay/{paymentId}")
    @ResponseBody
    public ResponseEntity<String> payEmi(@PathVariable("paymentId") Long paymentId,
                                         @RequestParam(required = false) java.math.BigDecimal amount,
                                         @RequestParam(required = false) String strategy) {
        emiPaymentService.payEmi(paymentId, amount, strategy);
        return ResponseEntity.ok("EMI payment successful for Payment ID: " + paymentId + " (amount: " + (amount != null ? amount : "EMI") + ", strategy:" + (strategy != null ? strategy : "DEFAULT") + ")");
    }
    @GetMapping("/view/{loanId}")
    public String viewEmiPayments(@PathVariable Long loanId, Model model, @RequestParam(required = false) String success, @RequestParam(required = false) String error) {
        Loan loan = loanService.findLoanById(loanId);
        if (loan == null) {
            throw new IllegalArgumentException("Loan not found for ID: " + loanId);
        }
        List<EMIPayment> payments = emiPaymentService.getPaymentsForLoan(loanId);
        LoanSummaryDTO summary = loanService.getLoanSummary(loanId);
        model.addAttribute("loan", loan);
        model.addAttribute("payments", payments != null ? payments : List.of());
        model.addAttribute("summary", summary);
        // optional flash messages
        if (success != null) model.addAttribute("success", success);
        if (error != null) model.addAttribute("error", error);
        return "emi-payments"; // template name
    }
    // Pay an EMI from the UI (form/button). If paymentId is not provided, pay the next pending EMI.
    @PostMapping("/view/{loanId}/pay")
    public String payFromView(@PathVariable Long loanId,
                              @RequestParam(required = false) Long paymentId,
                              RedirectAttributes redirectAttributes) {
        try {
            Long idToPay = paymentId;
            if (idToPay == null) {
                // find next pending payment
                List<EMIPayment> payments = emiPaymentRepository.findByLoan_LoanId(loanId);
                EMIPayment next = payments.stream()
                        .filter(p -> "PENDING".equalsIgnoreCase(p.getStatus()))
                        .min(java.util.Comparator.comparing(EMIPayment::getDueDate))
                        .orElse(null);
                if (next == null) {
                    redirectAttributes.addAttribute("error", "No pending EMI found to pay.");
                    return "redirect:/api/emipayments/view/" + loanId;
                }
                idToPay = next.getPaymentId();
            }
            emiPaymentService.payEmi(idToPay);
            redirectAttributes.addAttribute("success", "EMI payment successful!");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Failed to pay EMI: " + e.getMessage());
        }
        return "redirect:/api/emipayments/view/" + loanId;
    }
    // Early repay the entire remaining balance for a loan (UI action)
    @PostMapping("/view/{loanId}/early-pay")
    public String earlyPayFromView(@PathVariable Long loanId, RedirectAttributes redirectAttributes) {
        try {
            Loan loan = loanService.findLoanById(loanId);
            if (loan == null) throw new IllegalArgumentException("Loan not found");
            java.math.BigDecimal remaining = loan.getRemainingBalance() == null ? java.math.BigDecimal.ZERO : loan.getRemainingBalance();
            if (remaining.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                redirectAttributes.addAttribute("error", "Loan is already cleared.");
                return "redirect:/api/emipayments/view/" + loanId;
            }
            EMIPayment p = new EMIPayment();
            p.setLoan(loan);
            p.setAmountPaid(remaining);
            p.setPaymentDate(java.time.LocalDate.now());
            p.setStatus("PAID");
            loanService.recordPayment(loanId, p);
            redirectAttributes.addAttribute("success", "Loan paid off successfully.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Failed to early-pay loan: " + e.getMessage());
        }
        return "redirect:/api/emipayments/view/" + loanId;
    }
    // Add a manual payment (form) for a loan
    @PostMapping("/view/{loanId}/add")
    public String addPaymentFromView(@PathVariable Long loanId,
                                     @RequestParam(required = false) java.math.BigDecimal amountPaid,
                                     @RequestParam(required = false) String paymentDate,
                                     RedirectAttributes redirectAttributes) {
        try {
            Loan loan = loanService.findLoanById(loanId);
            if (loan == null) throw new IllegalArgumentException("Loan not found");

            EMIPayment p = new EMIPayment();
            p.setLoan(loan);
            p.setAmountPaid(amountPaid != null ? amountPaid : loan.getEmiAmount());
            if (paymentDate != null && !paymentDate.isBlank()) {
                p.setPaymentDate(java.time.LocalDate.parse(paymentDate));
            } else {
                p.setPaymentDate(java.time.LocalDate.now());
            }
            p.setStatus("PAID");
            loanService.recordPayment(loanId, p);
            redirectAttributes.addAttribute("success", "Manual payment recorded.");
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Failed to record payment: " + e.getMessage());
        }
        return "redirect:/api/emipayments/view/" + loanId;
    }
}
