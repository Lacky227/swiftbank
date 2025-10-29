package com.swiftbank.cardservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreatePayload {
    private String accountNumber;
    private Long userId;
}
