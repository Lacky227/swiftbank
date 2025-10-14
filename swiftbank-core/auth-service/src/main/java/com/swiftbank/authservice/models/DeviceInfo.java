package com.swiftbank.authservice.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "devices")
public class DeviceInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId;
    private String deviceType;
    private String os;
    private String osVersion;
    private String appVersion;
    private String userAgent;
    private String ipAddress;

    private LocalDateTime registeredAt;
    private LocalDateTime lastUsedAt;
    private boolean trusted;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
