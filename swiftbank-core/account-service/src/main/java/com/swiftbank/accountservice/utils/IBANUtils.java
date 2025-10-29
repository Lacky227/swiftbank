package com.swiftbank.accountservice.utils;

import com.swiftbank.accountservice.repository.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.iban4j.UnsupportedCountryException;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class IBANUtils {
    public String generateIBAN(String countryCode, String bankCode, AccountRepository accountRepository) {
        String accountNumber = generateUniqueAccountNumber(accountRepository);
        String ibanNumber;
        try {
            Iban iban = new Iban.Builder()
                    .countryCode(CountryCode.valueOf(countryCode))
                    .bankCode(bankCode)
                    .accountNumber(accountNumber)
                    .build();
            ibanNumber = iban.toString();
        } catch (UnsupportedCountryException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid IBAN provided: " + e.getMessage());
        }
        return ibanNumber;
    }
    private String generateUniqueAccountNumber(AccountRepository accountRepository) {
        String prefix = "2600";
        long randomNumber = ThreadLocalRandom.current().nextLong(100_000_000_000_000L);
        String suffix = String.format("%015d", randomNumber);

        String accountNumber = prefix + suffix;

        if (accountRepository.existsAccountByNumber(accountNumber)) {
            return generateUniqueAccountNumber(accountRepository);
        }
        return accountNumber;
    }
}
