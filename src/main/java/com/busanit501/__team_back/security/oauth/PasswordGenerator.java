package com.busanit501.__team_back.security.oauth;

import java.security.SecureRandom;

final class PasswordGenerator {
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final SecureRandom RND = new SecureRandom();

    private PasswordGenerator() {}

    static String random64() {
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < 64; i++) sb.append(ALPHABET.charAt(RND.nextInt(ALPHABET.length())));
        return sb.toString();
    }
}

