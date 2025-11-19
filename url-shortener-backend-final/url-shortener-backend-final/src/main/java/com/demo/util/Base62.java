package com.demo.util;

import java.security.SecureRandom;

public final class Base62 {
    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RNG = new SecureRandom();

    private Base62() {}

    public static String randomCode(int length) {
        char[] out = new char[length];          // Create array for result
        for (int i = 0; i < length; i++) {      // Loop for each character
            out[i] = ALPHABET[RNG.nextInt(ALPHABET.length)]; // Pick random character
        }
        return new String(out);                 // Convert to String
    }
}