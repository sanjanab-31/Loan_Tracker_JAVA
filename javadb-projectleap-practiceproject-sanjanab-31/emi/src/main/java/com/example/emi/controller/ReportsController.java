package com.example.emi.controller;
import com.example.emi.dto.LoanSummaryDTO;
import com.example.emi.dto.UserLoanReportDTO;
import com.example.emi.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/reports")
public class ReportsController {
    private final LoanService loanService;
    public ReportsController(LoanService loanService) {
        this.loanService = loanService;
    }
    // Loan summary across all loans for a user
    @GetMapping("/loans/user/{userId}")
    public ResponseEntity<LoanSummaryDTO> getLoanSummaryForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getLoanSummaryForUser(userId));
    }
    // Detailed user loan report
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserLoanReportDTO> getUserLoanReport(@PathVariable Long userId) {
        return ResponseEntity.ok(loanService.getUserLoanReport(userId));
    }
}
