package com.example.emi.repository;
import com.example.emi.model.EMIPayment;
import com.example.emi.model.Loan;
import com.example.emi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByUser(User user);
    List<Loan> findByUser_UserId(Long userId);
}
