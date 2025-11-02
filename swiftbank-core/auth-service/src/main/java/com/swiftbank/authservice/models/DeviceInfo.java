package com.swiftbank.authservice.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.registeredAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
        this.trusted = true;
    }
}
