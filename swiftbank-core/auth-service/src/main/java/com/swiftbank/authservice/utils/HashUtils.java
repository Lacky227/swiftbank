package com.swiftbank.authservice.utils;

import lombok.experimental.UtilityClass;

import java.security.MessageDigest;
import java.util.HexFormat;

@UtilityClass
public class HashUtils {

    public String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
