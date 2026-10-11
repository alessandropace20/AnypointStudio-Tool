package com.integrationtoolbox.security;

import java.nio.charset.StandardCharsets;

/**
 * Service layer wrapping EncryptionUtil for UI use.
 * Never logs or persists plaintext secrets.
 */
public class SecurityService {

    /**
     * Encrypts a plaintext string value with a password.
     * @param plaintext the value to encrypt (must not be empty)
     * @param password  the password (must not be empty)
     * @return the encoded encrypted payload
     */
    public String encryptValue(String plaintext, char[] password) throws Exception {
        if (plaintext == null || plaintext.isEmpty())
            throw new IllegalArgumentException("The value to encrypt must not be empty.");
        if (password == null || password.length == 0)
            throw new IllegalArgumentException("Password must not be empty.");
        return EncryptionUtil.encrypt(password, plaintext.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decrypts an encoded payload back to a plaintext string.
     * @param encryptedPayload the payload produced by encryptValue()
     * @param password         the password used during encryption
     * @return the decrypted plaintext
     * @throws Exception if the password is wrong or the payload is malformed/tampered
     */
    public String decryptValue(String encryptedPayload, char[] password) throws Exception {
        if (encryptedPayload == null || encryptedPayload.isBlank())
            throw new IllegalArgumentException("Encrypted payload must not be empty.");
        if (password == null || password.length == 0)
            throw new IllegalArgumentException("Password must not be empty.");
        byte[] decryptedBytes = EncryptionUtil.decrypt(password, encryptedPayload);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
