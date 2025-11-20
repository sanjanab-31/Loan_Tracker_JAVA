package com.example.emi.dto;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanSummaryDTO {
    private int totalEmis;
    private int paidEmis;
    private BigDecimal totalPaid;
    private BigDecimal remainingBalance;
    private LocalDate nextDueDate;
}
