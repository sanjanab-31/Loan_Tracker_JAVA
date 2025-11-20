package com.example.emi.service;
import com.example.emi.model.EMIPayment;
import com.example.emi.model.Loan;
import com.example.emi.repository.EMIPaymentRepository;
import com.example.emi.repository.LoanRepository;
import com.example.emi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {
    @Mock
    private LoanRepository loanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EMIPaymentRepository emiPaymentRepository;
    @InjectMocks
    private LoanService loanService;
    @BeforeEach
    void setup() {
    }
    @Test
    void createLoan_shouldCalculateEmi_andGenerateSchedule() {
        Loan loan = Loan.builder()
                .principal(new BigDecimal("100000"))
                .interestRate(new BigDecimal("10"))
                .tenureMonths(12)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan l = invocation.getArgument(0);
            l.setLoanId(1L);
            return l;
        });
        doAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            assertNotNull(arg);
            return null;
        }).when(emiPaymentRepository).saveAll(any());
        Loan saved = loanService.createLoan(loan);
        assertNotNull(saved.getEmiAmount(), "EMI should be calculated");
        assertNotNull(saved.getTotalPayable(), "Total payable should be set");
        BigDecimal expectedTotal = saved.getEmiAmount().multiply(BigDecimal.valueOf(saved.getTenureMonths())).setScale(2, BigDecimal.ROUND_HALF_UP);
        assertEquals(0, expectedTotal.compareTo(saved.getTotalPayable()));
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(emiPaymentRepository, times(1)).saveAll(any());
    }
    @Test
    void recordPayment_shouldMarkPaymentAndReduceLoanRemaining() {
        Long loanId = 1L;
        Loan loan = Loan.builder()
                .loanId(loanId)
                .remainingBalance(new BigDecimal("12000.00"))
                .build();
        EMIPayment payment = EMIPayment.builder()
                .amountPaid(new BigDecimal("1000.00"))
                .dueDate(LocalDate.now())
                .build();
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(emiPaymentRepository.save(any(EMIPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var saved = loanService.recordPayment(loanId, payment);
        assertEquals("PAID", saved.getStatus());
        assertEquals(0, loan.getRemainingBalance().compareTo(new BigDecimal("11000.00")));
        verify(emiPaymentRepository, times(1)).save(any(EMIPayment.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }
}
