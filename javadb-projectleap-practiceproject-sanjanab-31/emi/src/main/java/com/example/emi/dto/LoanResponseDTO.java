package com.example.emi.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Builder
public class LoanResponseDTO {
    private Long loanId;
    private String loanType;
    private BigDecimal principal;
    private BigDecimal interestRate;
    private int tenureMonths;
    private BigDecimal emiAmount;
    private BigDecimal totalPayable;
    private BigDecimal remainingBalance;
    private LocalDate startDate;
    private Long userId;
    private String userName;
    private String userEmail;
}
