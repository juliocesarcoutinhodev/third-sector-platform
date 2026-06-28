package br.com.toponesystem.thirdsector.auth.application.usecase;

import java.security.SecureRandom;

final class SecurePasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%&*";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;
    private static final int LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SecurePasswordGenerator() {}

    static String generate() {
        var password = new char[LENGTH];

        password[0] = UPPER.charAt(SECURE_RANDOM.nextInt(UPPER.length()));
        password[1] = LOWER.charAt(SECURE_RANDOM.nextInt(LOWER.length()));
        password[2] = DIGITS.charAt(SECURE_RANDOM.nextInt(DIGITS.length()));
        password[3] = SPECIAL.charAt(SECURE_RANDOM.nextInt(SPECIAL.length()));

        for (int i = 4; i < LENGTH; i++) {
            password[i] = ALL.charAt(SECURE_RANDOM.nextInt(ALL.length()));
        }

        for (int i = password.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = password[i];
            password[i] = password[j];
            password[j] = temp;
        }

        return new String(password);
    }
}
