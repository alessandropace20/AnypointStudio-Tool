package com.integrationtoolbox.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    @Test
    void roundTripEncryptDecrypt() throws Exception {
        char[] password = "StrongP@ss123!".toCharArray();
        byte[] plaintext = "SecretAPIKey12345".getBytes(StandardCharsets.UTF_8);

        String encrypted = EncryptionUtil.encrypt(password, plaintext);
        assertNotNull(encrypted, "Encrypted string must not be null");
        assertTrue(encrypted.startsWith("v1:"), "Payload must start with version prefix");

        byte[] decrypted = EncryptionUtil.decrypt(password, encrypted);
        assertArrayEquals(plaintext, decrypted, "Decrypted bytes must match original plaintext");
    }

    @Test
    void eachEncryptionProducesDifferentCiphertext() throws Exception {
        char[] password = "SamePassword".toCharArray();
        byte[] plaintext = "SamePlaintext".getBytes(StandardCharsets.UTF_8);

        String enc1 = EncryptionUtil.encrypt(password, plaintext);
        String enc2 = EncryptionUtil.encrypt(password, plaintext);
        assertNotEquals(enc1, enc2, "Two encryptions of the same plaintext must differ (random IV/salt)");
    }

    @Test
    void decryptWithWrongPasswordThrows() throws Exception {
        char[] password = "CorrectPassword".toCharArray();
        char[] wrongPwd = "WrongPassword".toCharArray();
        byte[] plaintext = "SomeData".getBytes(StandardCharsets.UTF_8);

        String encrypted = EncryptionUtil.encrypt(password, plaintext);
        assertThrows(Exception.class, () -> EncryptionUtil.decrypt(wrongPwd, encrypted),
                "Decryption with wrong password must throw");
    }

    @Test
    void decryptMalformedPayloadThrows() {
        char[] password = "AnyPassword".toCharArray();
        assertThrows(Exception.class, () -> EncryptionUtil.decrypt(password, "notvalid"),
                "Decryption of malformed payload must throw");
    }

    @Test
    void decryptTamperedCiphertextThrows() throws Exception {
        char[] password = "AnyPassword".toCharArray();
        byte[] plaintext = "Hello".getBytes(StandardCharsets.UTF_8);
        String encrypted = EncryptionUtil.encrypt(password, plaintext);

        // Corrupt the ciphertext part (last segment)
        String[] parts = encrypted.split(":");
        parts[3] = parts[3].substring(0, Math.max(1, parts[3].length() - 4)) + "XXXX";
        String tampered = String.join(":", parts);

        assertThrows(Exception.class, () -> EncryptionUtil.decrypt(password, tampered),
                "GCM authentication must fail on tampered ciphertext");
    }

    @Test
    void encryptEmptyBytesWorks() throws Exception {
        char[] password = "AnyPassword".toCharArray();
        byte[] empty = new byte[0];
        String encrypted = EncryptionUtil.encrypt(password, empty);
        byte[] decrypted = EncryptionUtil.decrypt(password, encrypted);
        assertArrayEquals(empty, decrypted);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello", "Hello, World!", "Ünïcödé çhàràctérs", "1234567890"})
    void roundTripVariousStrings(String value) throws Exception {
        char[] password = "TestPwd".toCharArray();
        byte[] plaintext = value.getBytes(StandardCharsets.UTF_8);
        byte[] decrypted = EncryptionUtil.decrypt(password, EncryptionUtil.encrypt(password, plaintext));
        assertEquals(value, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    void encryptedPayloadDoesNotContainPlaintext() throws Exception {
        char[] password = "SomePassword".toCharArray();
        String sensitiveValue = "VERY_SECRET_KEY_9876";
        String encrypted = EncryptionUtil.encrypt(password, sensitiveValue.getBytes(StandardCharsets.UTF_8));
        assertFalse(encrypted.contains(sensitiveValue), "Encrypted payload must not contain plaintext");
    }
}
