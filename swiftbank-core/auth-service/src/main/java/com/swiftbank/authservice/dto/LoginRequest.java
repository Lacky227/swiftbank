package com.swiftbank.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    private String email;
    private String password;

    private DeviceInfo deviceInfo;
    private String locale;
    private String timezone;
    private String ipAddress;

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
    }
}
