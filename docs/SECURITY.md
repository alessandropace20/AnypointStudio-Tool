# Security Design

The **EncryptionUtil** provides password‑based encryption for API keys and other secrets:

- **Algorithm**: AES‑GCM with a 256‑bit key.
- **Key derivation**: PBKDF2WithHmacSHA256, 100 000 iterations, 16‑byte random salt.
- **IV**: 12‑byte random nonce generated per encryption operation.
- **Payload format**: `Base64(salt):Base64(iv):Base64(ciphertext)`

### Security Guarantees
- Confidentiality: ciphertext cannot be decrypted without the correct password.
- Integrity: AES‑GCM provides authentication; tampered ciphertext throws an exception.
- No secrets are logged or persisted by the application.
- All cryptographic operations use BouncyCastle provider (included as a dependency).

### Operational Recommendations
- Use a strong, high‑entropy password.
- Store the encrypted payload (e.g., in a configuration file) but never store the password.
- Prefer short‑lived passwords; consider integrating with a password manager.

### Limitations
- The utility does not manage password storage or retrieval – that is left to the UI layer.
- No key rotation mechanism is implemented at this stage.
