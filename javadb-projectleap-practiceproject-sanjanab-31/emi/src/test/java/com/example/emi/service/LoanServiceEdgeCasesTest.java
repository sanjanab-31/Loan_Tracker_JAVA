package com.example.emi.service;
import com.example.emi.model.Loan;
import com.example.emi.repository.EMIPaymentRepository;
import com.example.emi.repository.LoanRepository;
import com.example.emi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class LoanServiceEdgeCasesTest {
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EMIPaymentRepository emiPaymentRepository;
    @InjectMocks
    private LoanService loanService;
    @Test
    void createLoan_zeroInterest_shouldCalculateSimpleDivisionEmi() {
        Loan loan = Loan.builder()
                .principal(new BigDecimal("12000"))
                .interestRate(BigDecimal.ZERO)
                .tenureMonths(12)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan l = invocation.getArgument(0);
            l.setLoanId(10L);
            return l;
        });
    when(emiPaymentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Loan saved = loanService.createLoan(loan);
        assertNotNull(saved.getEmiAmount());
        BigDecimal expectedEmi = new BigDecimal("12000").divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
        assertEquals(0, expectedEmi.compareTo(saved.getEmiAmount()));
    }
    @Test
    void createLoan_oneMonthTenure_shouldSetTotalEqualToEmi() {
        Loan loan = Loan.builder()
                .principal(new BigDecimal("5000"))
                .interestRate(new BigDecimal("12"))
                .tenureMonths(1)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(emiPaymentRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Loan saved = loanService.createLoan(loan);
        assertNotNull(saved.getEmiAmount());
        assertEquals(0, saved.getTotalPayable().compareTo(saved.getEmiAmount()));
    }
}
