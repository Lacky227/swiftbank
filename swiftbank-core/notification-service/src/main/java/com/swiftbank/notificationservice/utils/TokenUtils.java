package com.swiftbank.notificationservice.utils;

import lombok.experimental.UtilityClass;
import java.util.UUID;


@UtilityClass
public class TokenUtils {
    public String generateToken() {
        return UUID.randomUUID().toString();
    }
}
