package com.momenta.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple SHA-256 password hashing.
 * Good enough for an academic desktop project; not intended for
 * production-grade security (no per-user salt / key stretching).
 */
public class PasswordUtil {

    public static String hash(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainText.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public static boolean matches(String plainText, String hash) {
        return hash != null && hash.equals(hash(plainText));
    }
}
