package com.swiftbank.accountservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationFundsRequest {
    private String accountNumber;
    private BigDecimal amount;
}
