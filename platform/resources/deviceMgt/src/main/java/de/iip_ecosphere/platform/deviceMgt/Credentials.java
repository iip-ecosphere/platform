package de.iip_ecosphere.platform.deviceMgt;

/**
 * Credentials data class.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class Credentials {

    private String key;
    private String secret;

    /**
     * Constructor for the credentials.
     *
     * @param key    the key
     * @param secret the secret
     */
    public Credentials(String key, String secret) {
        this.key = key;
        this.secret = secret;
    }

    /**
     * Default constructor, is used for (de-)serialization.
     */
    public Credentials() {
    }

    /**
     * Get the key.
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key. Method is used for deserialization.
     *
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Get the secret.
     *
     * @return the secret
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the secret. Method is used for deserialization.
     *
     * @param secret the secret
     */
    public void setSecret(String secret) {
        this.secret = secret;
    }

    /**
     * To string method.
     *
     * @return string containing key and secret
     */
    @Override
    public String toString() {
        return "Credentials{" + "key='" + key + '\'' + ", secret='" + secret + '\'' + '}';
    }
}