package com.swiftbank.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotRequest {
    private String email;

    private String locale;
    private String ipAddress;
    private String userAgent;
}
