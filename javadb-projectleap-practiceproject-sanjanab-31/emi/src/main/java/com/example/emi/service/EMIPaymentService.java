package com.example.emi.service;
import com.example.emi.model.EMIPayment;
import com.example.emi.model.Loan;
import com.example.emi.repository.EMIPaymentRepository;
import com.example.emi.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.emi.service.EMIPaymentService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.List;
@Service
public class EMIPaymentService {
    private final EMIPaymentRepository emiPaymentRepository;
    private final LoanRepository loanRepository;
    public EMIPaymentService(EMIPaymentRepository emiPaymentRepository, LoanRepository loanRepository) {
        this.emiPaymentRepository = emiPaymentRepository;
        this.loanRepository = loanRepository;
    }
    @Transactional
    public void payEmi(Long paymentId) {
        payEmi(paymentId, null);
    }
    @Transactional
    public void payEmi(Long paymentId, java.math.BigDecimal amount) {
        payEmi(paymentId, amount, null);
    }
    @Transactional
    public void payEmi(Long paymentId, java.math.BigDecimal amount, String strategy) {
        EMIPayment emi = emiPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("EMI not found"));
        Loan loan = emi.getLoan();
        java.math.BigDecimal emiAmt = loan.getEmiAmount() != null ? loan.getEmiAmount() : java.math.BigDecimal.ZERO;
        java.math.BigDecimal toPay = amount != null ? amount : emiAmt;
        emi.setAmountPaid(toPay);
        emi.setPaymentDate(LocalDate.now());
        java.math.BigDecimal shortfall = emiAmt.subtract(toPay);
        if (shortfall.compareTo(java.math.BigDecimal.ZERO) <= 0) {
            emi.setStatus("PAID");
            emi.setExtraDue(java.math.BigDecimal.ZERO);
        } else {
            emi.setStatus("PARTIAL");
            emi.setExtraDue(shortfall);
        }
        emiPaymentRepository.save(emi);
        java.math.BigDecimal newRem = loan.getRemainingBalance().subtract(toPay).setScale(2, java.math.RoundingMode.HALF_UP);
        loan.setRemainingBalance(newRem.compareTo(java.math.BigDecimal.ZERO) < 0 ? java.math.BigDecimal.ZERO : newRem);
        if (shortfall.compareTo(java.math.BigDecimal.ZERO) > 0) {
            if ("EXTEND_TENURE".equalsIgnoreCase(strategy)) {
                List<EMIPayment> all = emiPaymentRepository.findByLoan_LoanId(loan.getLoanId());
                LocalDate lastDue = all.stream().map(EMIPayment::getDueDate).filter(Objects::nonNull).max(LocalDate::compareTo).orElse(LocalDate.now());
                EMIPayment extra = EMIPayment.builder()
                        .loan(loan)
                        .dueDate(lastDue.plusMonths(1))
                        .amountPaid(java.math.BigDecimal.ZERO)
                        .extraDue(shortfall)
                        .status("PENDING")
                        .build();
                emiPaymentRepository.save(extra);
                loan.setTenureMonths(loan.getTenureMonths() + 1);
                loan.setTotalPayable(loan.getTotalPayable().add(shortfall));
            } else {
        List<EMIPayment> pending = emiPaymentRepository.findByLoan_LoanId(loan.getLoanId());
        LocalDate emiDue = emi.getDueDate();
        EMIPayment next = pending.stream()
            .filter(p -> p.getDueDate() != null && emiDue != null && p.getDueDate().isAfter(emiDue) && (p.getStatus() == null || !"PAID".equalsIgnoreCase(p.getStatus())))
            .min(java.util.Comparator.comparing(EMIPayment::getDueDate))
            .orElse(null);
                if (next != null) {
                    java.math.BigDecimal existing = next.getExtraDue() != null ? next.getExtraDue() : java.math.BigDecimal.ZERO;
                    next.setExtraDue(existing.add(shortfall));
                    emiPaymentRepository.save(next);
                } else {
                    LocalDate lastDue = pending.stream().map(EMIPayment::getDueDate).filter(Objects::nonNull).max(LocalDate::compareTo).orElse(LocalDate.now());
                    EMIPayment extra = EMIPayment.builder()
                            .loan(loan)
                            .dueDate(lastDue.plusMonths(1))
                            .amountPaid(java.math.BigDecimal.ZERO)
                            .extraDue(shortfall)
                            .status("PENDING")
                            .build();
                    emiPaymentRepository.save(extra);
                }
            }
        }
        if (loan.getRemainingBalance().compareTo(java.math.BigDecimal.ZERO) == 0) {
            List<EMIPayment> pendingAll = emiPaymentRepository.findByLoan_LoanId(loan.getLoanId());
            for (EMIPayment p : pendingAll) {
                if (!"PAID".equalsIgnoreCase(p.getStatus())) {
                    p.setStatus("PAID");
                    if (p.getPaymentDate() == null) p.setPaymentDate(LocalDate.now());
                    if (p.getAmountPaid() == null) p.setAmountPaid(java.math.BigDecimal.ZERO);
                    p.setExtraDue(java.math.BigDecimal.ZERO);
                    emiPaymentRepository.save(p);
                }
            }
        }

        loanRepository.save(loan);
    }
    public List<EMIPayment> getPaymentsForLoan(Long loanId) {
        return emiPaymentRepository.findAll()
                .stream()
                .filter(p -> p.getLoan().getLoanId().equals(loanId))
                .toList();
    }

}
