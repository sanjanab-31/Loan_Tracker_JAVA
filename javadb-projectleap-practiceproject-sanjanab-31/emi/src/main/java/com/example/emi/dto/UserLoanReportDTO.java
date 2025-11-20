package com.example.emi.dto;
import lombok.*;
import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoanReportDTO {
    private Long userId;
    private String userName;
    private int totalLoans;
    private BigDecimal totalPrincipal;
    private BigDecimal totalPaid;
    private BigDecimal totalRemaining;
}
