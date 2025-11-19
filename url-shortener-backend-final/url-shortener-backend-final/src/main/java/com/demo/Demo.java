package com.demo;

// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

class Base62 {
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


public class Demo {
    public static void main(String[] args) {
        // PasswordEncoder encoder = new BCryptPasswordEncoder();
        // String pwd = "abc@123";
        // String encodedStr = encoder.encode(pwd);
        // System.out.println(encodedStr);

        // System.out.println(encoder.matches(pwd, "$2a$10$wVI0uT6hf8UF1ehU7VpA0OyR4RlVqtYtskT5up4z321YjUGwno5IE"));
        // System.out.println(encoder.matches(pwd, encodedStr));
        String newUrl = Base62.randomCode(5);
        System.out.println(newUrl);
    }
}
