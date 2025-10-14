package com.swiftbank.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String password;
    private DeviceInfo deviceInfo;
    private String locale;
    private String timezone;
    private String country;
    private boolean acceptTerms;
    private boolean marketingConsent;
    private String registrationSource;
    private String signupTimestamp;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeviceInfo {
        private String deviceId;
        private String deviceType;
        private String os;
        private String osVersion;
        private String appVersion;
        private String userAgent;
        private String ipAddress;
    }
}
