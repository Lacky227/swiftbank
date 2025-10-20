package com.swiftbank.notificationservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPayload {
    private String email;
    private String resetToken;
    private String locale;
}
