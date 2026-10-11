package com.integrationtoolbox.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password-based AES-GCM encryption and decryption.
 * Algorithm: AES-256-GCM
 * Key derivation: PBKDF2WithHmacSHA256, 100 000 iterations, 16-byte salt
 * IV: 12-byte random nonce per encryption
 * Payload format: v1:base64(salt):base64(iv):base64(ciphertext+tag)
 * No secrets are ever logged by this class.
 */
public class EncryptionUtil {
    private static final int SALT_LENGTH = 16;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final int GCM_TAG_LENGTH = 128;
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final String FORMAT_VERSION = "v1";

    private EncryptionUtil() {}

    private static byte[] deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Encrypts plaintext bytes with a password.
     * @return encoded payload: v1:base64(salt):base64(iv):base64(ciphertext)
     */
    public static String encrypt(char[] password, byte[] plaintext) throws Exception {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        byte[] iv = new byte[IV_LENGTH];
        RANDOM.nextBytes(iv);
        byte[] key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] ciphertext = cipher.doFinal(plaintext);
        return FORMAT_VERSION + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(iv) + ":"
                + Base64.getEncoder().encodeToString(ciphertext);
    }

    /**
     * Decrypts a payload produced by {@link #encrypt}.
     * Throws on wrong password or tampered data.
     */
    public static byte[] decrypt(char[] password, String encrypted) throws Exception {
        if (encrypted == null || encrypted.isBlank()) {
            throw new IllegalArgumentException("Encrypted payload must not be empty.");
        }
        String[] parts = encrypted.split(":");
        if (parts.length != 4 || !FORMAT_VERSION.equals(parts[0])) {
            throw new IllegalArgumentException(
                    "Invalid or unsupported encrypted payload. Expected format: " + FORMAT_VERSION + ":salt:iv:ciphertext");
        }
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        byte[] iv = Base64.getDecoder().decode(parts[2]);
        byte[] ciphertext = Base64.getDecoder().decode(parts[3]);
        byte[] key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(ciphertext);
    }
}
