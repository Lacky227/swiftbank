package com.swiftbank.authservice.dto;

import lombok.*;

@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String firstName;
    private String lastName;
    private String email;
    private String locale;
}
