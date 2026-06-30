package tcc.bes.api_monolito.shared.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class PayloadCipher {

    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final String secret;
    private final SecureRandom secureRandom = new SecureRandom();

    public PayloadCipher(@Value("${app.security.encryption-secret}") String secret) {
        this.secret = secret;
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            return encoder.encodeToString(iv) + "." + encoder.encodeToString(ciphertext);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not encrypt idempotency payload", ex);
        }
    }

    public String decrypt(String payload) {
        try {
            String[] parts = payload.split("\\.", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid encrypted payload");
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] iv = decoder.decode(parts[0]);
            byte[] ciphertext = decoder.decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not decrypt idempotency payload", ex);
        }
    }

    private SecretKeySpec key() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] key = Arrays.copyOf(digest.digest(secret.getBytes(StandardCharsets.UTF_8)), 32);
        return new SecretKeySpec(key, "AES");
    }
}
