package com.swiftbank.cardservice.dto.dto;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardResponse {
    private String number;
    private int expiryMonth;
    private int expiryYear;
}
