package test.de.iip_ecosphere.platform.test.mqtt.hivemq;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Utility class for generating secure password hashes and salts compatible with HiveMQ.
 * <p>
 * This implementation utilizes the PBKDF2 (Password-Based Key Derivation Function 2)
 * algorithm with an HMAC-SHA-256 pseudo-random function. The resulting byte arrays
 * are converted to Hexadecimal strings to match HiveMQ's standard configuration format.
 * </p>
 * * @author Gemini
 */
public class HiveMqHasher {

    /**
     * The cryptographic algorithm family used for key derivation.
     */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /**
     * The number of cryptographic iterations. 
     * HiveMQ defaults to 100 to prioritize high-throughput MQTT connection performance.
     */
    private static final int ITERATIONS = 100;

    /**
     * The desired length of the derived key in bits.
     */
    private static final int KEY_LENGTH = 256;

    /**
     * The length of the random salt in bytes (16 bytes = 128 bits).
     */
    private static final int SALT_LENGTH = 16;

    /**
     * Application entry point demonstrating how to generate a HiveMQ-compatible 
     * salt and password hash.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Provide a password as first argument.");
            return;
        }
        try {
            // 1. Generate a unique, cryptographically secure random salt
            byte[] salt = generateSalt();
            
            // 2. Derive the password hash using the salt and parameters
            byte[] hash = hashPassword(args[0], salt);
            
            // 3. Convert raw byte arrays into Hex strings for HiveMQ storage
            String hexSalt = toHex(salt);
            String hexHash = toHex(hash);

            System.out.println("Password:   " + args[0]);
            System.out.println("Salt (Hex): " + hexSalt);
            System.out.println("Hash (Hex): " + hexHash);
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error generating HiveMQ password credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Hashes a plain-text password using the PBKDF2WithHmacSHA256 algorithm.
     *
     * @param password The plain-text password to be hashed.
     * @param salt     The cryptographically secure random salt unique to the user.
     * @return A {@code byte[]} representing the derived cryptographic key/hash.
     * @throws NoSuchAlgorithmException If the PBKDF2WithHmacSHA256 algorithm is not 
     * available in the environment.
     * @throws InvalidKeySpecException  If the provided key specification is invalid 
     * for the SecretKeyFactory.
     */
    public static byte[] hashPassword(String password, byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), 
                salt, 
                ITERATIONS, 
                KEY_LENGTH
        );
        
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Generates a cryptographically secure, random byte array to be used as a salt.
     * Each user profile should always be assigned a unique salt.
     *
     * @return A {@code byte[]} array containing the generated salt bytes.
     */
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Converts a raw byte array into its equivalent lowercase Hexadecimal String representation.
     * This utility matches the string format required by HiveMQ configuration files.
     *
     * @param array The {@code byte[]} array to convert.
     * @return A hexadecimal {@code String} representation of the input byte array.
     */
    public static String toHex(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}