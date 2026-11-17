
package medfinder.logic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordHasher {

    private static final int SALT_LENGTH = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                "Password cannot be null or empty."
            );
        }

        try {
            byte[] salt = new byte[SALT_LENGTH];
            RANDOM.nextBytes(salt);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);

            byte[] hashedPassword = digest.digest(
                password.getBytes(StandardCharsets.UTF_8)
            );

            String encodedSalt =
                Base64.getEncoder().encodeToString(salt);

            String encodedHash =
                Base64.getEncoder().encodeToString(hashedPassword);

            return encodedSalt + ":" + encodedHash;

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "SHA-256 algorithm is unavailable.", e
            );
        }
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }

        try {
            String[] parts = stored.split(":", -1);

            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            if (salt.length != SALT_LENGTH || expectedHash.length != 32) {
                return false;
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);

            byte[] actualHash = digest.digest(
                password.getBytes(StandardCharsets.UTF_8)
            );

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (IllegalArgumentException e) {
            return false;

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "SHA-256 algorithm is unavailable.", e
            );
        }
    }
}
