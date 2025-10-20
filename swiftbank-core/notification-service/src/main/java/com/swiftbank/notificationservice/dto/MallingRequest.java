package com.swiftbank.notificationservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MallingRequest {
    private String email;
    private String subject;
    private String link;
    private String locale;
}
