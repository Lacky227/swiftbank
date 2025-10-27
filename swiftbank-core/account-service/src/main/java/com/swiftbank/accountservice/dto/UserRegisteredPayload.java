package com.swiftbank.accountservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisteredPayload {
    private Long userId;
    LocalDateTime createdAt;
}
