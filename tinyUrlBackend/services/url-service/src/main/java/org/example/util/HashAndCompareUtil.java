package org.example.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class HashAndCompareUtil {
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String hash(String planText) {
        return passwordEncoder.encode(planText);
    }

    public static boolean compare(String planText, String hashedString) {
        return passwordEncoder.matches(planText, hashedString);
    }
}
