package com.example.emi.repository;
import com.example.emi.model.EMIPayment;
import com.example.emi.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;
@Repository
public interface EMIPaymentRepository extends JpaRepository<EMIPayment, Long> {
    List<EMIPayment> findByLoanOrderByDueDateAsc(Loan loan);
    List<EMIPayment> findByLoan_LoanId(Long loanId);
    List<EMIPayment> findByStatusAndDueDateBetween(String status, LocalDate start, LocalDate end);
}
