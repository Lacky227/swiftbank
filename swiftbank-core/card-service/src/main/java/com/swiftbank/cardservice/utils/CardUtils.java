package com.swiftbank.cardservice.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class CardUtils {
    private static final Random RANDOM = new Random();
    private static final int CARD_LENGTH = 16;
    private static final String CARD_PREFIX = "4";

    public String generateCardNumber() {
        int numberToGenerate = CARD_LENGTH - CARD_PREFIX.length() - 1;

        StringBuilder prefixBuilder = new StringBuilder(CARD_PREFIX);
        for (int i = 0; i < numberToGenerate; i++) {
            prefixBuilder.append(RANDOM.nextInt(10));
        }
        String prefix = prefixBuilder.toString();
        int checkDigit = calculateLuhnCheckDigit(prefix);

        return prefix + checkDigit;
    }
    private int calculateLuhnCheckDigit(String prefix) {
        int sum = 0;
        boolean alternate = false;

        for (int i = prefix.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(prefix.substring(i, i + 1));

            if (alternate) {
                sum += n;
            } else {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
                sum += n;
            }
            alternate = !alternate;
        }

        int remainder = sum % 10;
        return (remainder == 0) ? 0 : (10 - remainder);
    }


    private boolean validateCardNumber(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }
}
