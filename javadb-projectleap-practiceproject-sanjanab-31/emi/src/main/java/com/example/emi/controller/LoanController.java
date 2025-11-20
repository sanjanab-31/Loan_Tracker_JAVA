    package com.example.emi.controller;
    import com.example.emi.dto.LoanResponseDTO;
    import com.example.emi.dto.LoanSummaryDTO;
    import com.example.emi.model.Loan;
    import com.example.emi.model.User;
    import com.example.emi.service.LoanService;
    import com.example.emi.service.EMIPaymentService;
    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.servlet.mvc.support.RedirectAttributes;
    import java.util.List;
    @RestController
    @RequestMapping("/api/loans")
    public class LoanController {
        private final LoanService loanService;
        private final EMIPaymentService emiPaymentService;
        public LoanController(LoanService loanService, EMIPaymentService emiPaymentService) {
            this.loanService = loanService;
            this.emiPaymentService = emiPaymentService;
        }
        @PostMapping
        public ResponseEntity<LoanResponseDTO> createLoan(@RequestBody Loan loan) {
            Loan saved = loanService.createLoan(loan);
            User user = saved.getUser();
            LoanResponseDTO dto = LoanResponseDTO.builder()
                    .loanId(saved.getLoanId())
                    .loanType(saved.getLoanType())
                    .principal(saved.getPrincipal())
                    .interestRate(saved.getInterestRate())
                    .tenureMonths(saved.getTenureMonths())
                    .emiAmount(saved.getEmiAmount())
                    .totalPayable(saved.getTotalPayable())
                    .remainingBalance(saved.getRemainingBalance())
                    .startDate(saved.getStartDate())
                    .userId(user != null ? user.getUserId() : null)
                    .userName(user != null ? user.getName() : null)
                    .userEmail(user != null ? user.getEmail() : null)
                    .build();

            return ResponseEntity.ok(dto);
        }
        @GetMapping("/user/{userId}")
        public ResponseEntity<List<LoanResponseDTO>> getLoansByUser(@PathVariable Long userId) {
            List<Loan> loans = loanService.getLoansByUserId(userId);
            User user = loanService.findFullUser(userId);
            List<LoanResponseDTO> dtos = loans.stream()
                    .map(loan -> LoanResponseDTO.builder()
                            .loanId(loan.getLoanId())
                            .loanType(loan.getLoanType())
                            .principal(loan.getPrincipal())
                            .interestRate(loan.getInterestRate())
                            .tenureMonths(loan.getTenureMonths())
                            .emiAmount(loan.getEmiAmount())
                            .totalPayable(loan.getTotalPayable())
                            .remainingBalance(loan.getRemainingBalance())
                            .startDate(loan.getStartDate())
                            .userId(user.getUserId())
                            .userName(user.getName())
                            .userEmail(user.getEmail())
                            .build())
                    .toList();
            return ResponseEntity.ok(dtos);
        }
        @GetMapping("/{loanId}/summary")
        public ResponseEntity<LoanSummaryDTO> getLoanSummary(@PathVariable Long loanId) {
            return ResponseEntity.ok(loanService.getLoanSummary(loanId));
        }
        @GetMapping("/{loanId}/calculate-emi")
        public ResponseEntity<LoanResponseDTO> calculateEmi(@PathVariable Long loanId) {
            Loan loan = loanService.findLoanById(loanId);
            BigDecimal principal = loan.getPrincipal();
            BigDecimal annualRate = loan.getInterestRate();
            int tenure = loan.getTenureMonths() != null ? loan.getTenureMonths() : 0;
            BigDecimal emi = BigDecimal.ZERO;
            BigDecimal total = BigDecimal.ZERO;
            if (principal != null && annualRate != null && tenure > 0) {
                BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), 20, RoundingMode.HALF_UP);
                BigDecimal onePlusRPowerN = BigDecimal.ONE.add(monthlyRate).pow(tenure);
                emi = principal.multiply(monthlyRate).multiply(onePlusRPowerN)
                        .divide(onePlusRPowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
                total = emi.multiply(BigDecimal.valueOf(tenure)).setScale(2, RoundingMode.HALF_UP);
            }
            LoanResponseDTO dto = LoanResponseDTO.builder()
                    .loanId(loan.getLoanId())
                    .loanType(loan.getLoanType())
                    .principal(loan.getPrincipal())
                    .interestRate(loan.getInterestRate())
                    .tenureMonths(loan.getTenureMonths())
                    .emiAmount(emi)
                    .totalPayable(total)
                    .remainingBalance(loan.getRemainingBalance())
                    .startDate(loan.getStartDate())
                    .userId(loan.getUser() != null ? loan.getUser().getUserId() : null)
                    .userName(loan.getUser() != null ? loan.getUser().getName() : null)
                    .userEmail(loan.getUser() != null ? loan.getUser().getEmail() : null)
                    .build();
            return ResponseEntity.ok(dto);
        }
        @PostMapping("/emi/pay/{paymentId}")
        public String payEmi(@PathVariable Long paymentId, RedirectAttributes redirectAttributes) {
            try {
                emiPaymentService.payEmi(paymentId); 
                redirectAttributes.addFlashAttribute("success", "EMI payment successful!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to pay EMI: " + e.getMessage());
            }
            return "redirect:/dashboard/1";
        }
    }
