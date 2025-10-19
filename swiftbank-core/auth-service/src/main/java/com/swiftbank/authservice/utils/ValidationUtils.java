package com.swiftbank.authservice.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtils {
    private final String EMAIL_PATTERN =
            "^(?=.{10,50}$)[A-Za-z0-9](?!.*[._+-]{2})[A-Za-z0-9+_.-]*[A-Za-z0-9]@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.(com|org|ua|net)$";
    private final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\-\\[\\]{};':\"\\\\|,.<>/?]).{8,30}$";
    private final String NAME_PATTERN =
            "^[A-Za-zА-Яа-яЇїІіЄєҐґ\\-\\s]+$";

    public boolean emailInvalid(String email) {
        if (email == null || email.isEmpty()) return true;
        return !email.matches(EMAIL_PATTERN);
    }

    public boolean passwordInvalid(String password) {
        if (password == null || password.isEmpty()) return true;
        if (password.matches(".*[а-яА-ЯїЇєЄіІґҐ].*")) return true;
        return !password.matches(PASSWORD_PATTERN);
    }

    public boolean firstNameInvalid(String name) {
        return name == null || name.isEmpty() || !name.matches(NAME_PATTERN);
    }
    public boolean lastNameInvalid(String name) {
        return name != null && !name.isEmpty() && !name.matches(NAME_PATTERN);
    }
}
