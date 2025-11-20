package com.example.emi.service;
import com.example.emi.dto.LoanSummaryDTO;
import com.example.emi.dto.UserLoanReportDTO;
import com.example.emi.model.*;
import com.example.emi.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import com.example.emi.model.Loan;
import com.example.emi.model.EMIPayment;
import com.example.emi.repository.LoanRepository;
import com.example.emi.repository.EMIPaymentRepository;
import java.util.List;
@Service
public class LoanService {
        private final LoanRepository loanRepository;
        private final UserRepository userRepository;
        private final EMIPaymentRepository emiPaymentRepository;
        private final EmailService emailService;
        public LoanService(LoanRepository loanRepository, UserRepository userRepository, EMIPaymentRepository emiPaymentRepository, EmailService emailService) {
                this.loanRepository = loanRepository;
                this.userRepository = userRepository;
                this.emiPaymentRepository = emiPaymentRepository;
                this.emailService = emailService;
        }
    public Loan createLoan(Loan loan) {
        BigDecimal principal = loan.getPrincipal();
        BigDecimal annualRate = loan.getInterestRate();
        int tenure = loan.getTenureMonths();
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12 * 100), 20, RoundingMode.HALF_UP);
        BigDecimal emi;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            emi = principal.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);
        } else {
            BigDecimal onePlusRPowerN = BigDecimal.ONE.add(monthlyRate).pow(tenure);
            emi = principal.multiply(monthlyRate).multiply(onePlusRPowerN)
                    .divide(onePlusRPowerN.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
        }
        BigDecimal total = emi.multiply(BigDecimal.valueOf(tenure)).setScale(2, RoundingMode.HALF_UP);
        loan.setEmiAmount(emi);
        loan.setTotalPayable(total);
        loan.setRemainingBalance(total);
        if (loan.getStartDate() == null) loan.setStartDate(LocalDate.now());
        Loan savedLoan = loanRepository.save(loan);
        List<EMIPayment> schedule = new ArrayList<>();
        LocalDate dueDate = loan.getStartDate();
        for (int i = 0; i < tenure; i++) {
            schedule.add(EMIPayment.builder()
                    .loan(savedLoan)
                    .dueDate(dueDate.plusMonths(i))
                    .amountPaid(BigDecimal.ZERO)
                    .status("PENDING")
                    .build());
        }
                emiPaymentRepository.saveAll(schedule);
                try {
                    emailService.sendLoanCreatedEmail(savedLoan);
                } catch (Exception ignored) {
                }

                return savedLoan;
    }
    @Transactional
    public EMIPayment recordPayment(Long loanId, EMIPayment payment) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        payment.setLoan(loan);
        payment.setStatus("PAID");
        if (payment.getPaymentDate() == null)
            payment.setPaymentDate(LocalDate.now());
        EMIPayment saved = emiPaymentRepository.save(payment);
        BigDecimal newBalance = loan.getRemainingBalance().subtract(payment.getAmountPaid())
                .setScale(2, RoundingMode.HALF_UP);
        loan.setRemainingBalance(newBalance.max(BigDecimal.ZERO));
        loanRepository.save(loan);
        return saved;
    }
    public LoanSummaryDTO getLoanSummaryForUser(Long userId) {
        List<Loan> userLoans = loanRepository.findByUser_UserId(userId);
        if (userLoans.isEmpty()) {
            return new LoanSummaryDTO(0, 0, BigDecimal.ZERO, BigDecimal.ZERO, null);
        }
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalRemaining = BigDecimal.ZERO;
        int totalEmis = 0;
        int paidEmis = 0;
        LocalDate nextDueDate = null;
        for (Loan loan : userLoans) {
            List<EMIPayment> payments = emiPaymentRepository.findByLoan_LoanId(loan.getLoanId());
            totalEmis += payments.size();
            paidEmis += (int) payments.stream().filter(p -> "PAID".equalsIgnoreCase(p.getStatus())).count();
            totalPaid = totalPaid.add(payments.stream()
                    .map(p -> p.getAmountPaid() == null ? BigDecimal.ZERO : p.getAmountPaid())
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            totalRemaining = totalRemaining.add(loan.getRemainingBalance() == null
                    ? BigDecimal.ZERO
                    : loan.getRemainingBalance());
            nextDueDate = payments.stream()
                    .filter(p -> "PENDING".equalsIgnoreCase(p.getStatus()))
                    .map(EMIPayment::getDueDate)
                    .min(Comparator.naturalOrder())
                    .orElse(nextDueDate);
        }
        return LoanSummaryDTO.builder()
                .totalEmis(totalEmis)
                .paidEmis(paidEmis)
                .totalPaid(totalPaid)
                .remainingBalance(totalRemaining)
                .nextDueDate(nextDueDate)
                .build();
    }
    public List<Loan> getLoansByUserId(Long userId) {
        return userRepository.findById(userId)
                .map(loanRepository::findByUser)
                .orElseGet(ArrayList::new);
    }
    public User findFullUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    public List<EMIPayment> getPaymentsForLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        return emiPaymentRepository.findByLoanOrderByDueDateAsc(loan);
    }
    public UserLoanReportDTO getUserLoanReport(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var loans = loanRepository.findByUser(user);
        int totalLoans = loans.size();
        BigDecimal totalPrincipal = loans.stream()
                .map(Loan::getPrincipal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = loans.stream()
                .map(Loan::getTotalPayable)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(loans.stream()
                        .map(Loan::getRemainingBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal totalRemaining = loans.stream()
                .map(Loan::getRemainingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return UserLoanReportDTO.builder()
                .userId(user.getUserId())
                .userName(user.getName())
                .totalLoans(totalLoans)
                .totalPrincipal(totalPrincipal)
                .totalPaid(totalPaid)
                .totalRemaining(totalRemaining)
                .build();
    }
    public LoanSummaryDTO getLoanSummary(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));
        List<EMIPayment> payments = emiPaymentRepository.findByLoan_LoanId(loanId);
        int totalEmis = payments.size();
        int paidEmis = (int) payments.stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getStatus()))
                .count();
        BigDecimal totalPaid = payments.stream()
                .map(p -> p.getAmountPaid() == null ? BigDecimal.ZERO : p.getAmountPaid())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingBalance = loan.getRemainingBalance() == null
                ? BigDecimal.ZERO
                : loan.getRemainingBalance();
        LocalDate nextDueDate = payments.stream()
                .filter(p -> "PENDING".equalsIgnoreCase(p.getStatus()))
                .map(EMIPayment::getDueDate)
                .min(LocalDate::compareTo)
                .orElse(null);
        return LoanSummaryDTO.builder()
                .totalEmis(totalEmis)
                .paidEmis(paidEmis)
                .totalPaid(totalPaid)
                .remainingBalance(remainingBalance)
                .nextDueDate(nextDueDate)
                .build();
    }
    public Loan findLoanById(Long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found with ID: " + loanId));
    }
}
