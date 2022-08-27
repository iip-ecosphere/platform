package de.iip_ecosphere.platform.support.identities;

import java.nio.charset.Charset;

/**
 * Initial abstraction of a identity token based on Eclipse Milo. Preliminary.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IdentityToken {
    
    /**
     * Defines the token type. See also the getters of {@link IdentityToken} or the methods of 
     * {@link IdentityTokenBuilder} on what data is needed.
     */
    public enum TokenType {
        
        /**
         * An anonymous authentication token.
         */
        ANONYMOUS,

        /**
         * An issued token (with token data and token encryption algorithm).
         */
        ISSUED,

        /**
         * An X509 token (with token data).
         */
        X509,

        /**
         * An user name token (with user name, token data and token encryption algorithm).
         */
        USERNAME
        
    }

    /**
     * Helps building identity tokens.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class IdentityTokenBuilder {
        
        private IdentityToken token;

        /**
         * Prevents external creation, only through builder.
         */
        private IdentityTokenBuilder() {
        }
        
        /**
         * Creates an untyped token that must be filled by {@link #setIssuedToken(byte[], String)}, 
         * {@link #setUsernameToken(String, byte[], String)}, or {@link #setX509Token(byte[])}. This also works
         * via {@link #newBuilder(String, String, byte[])} but without already passing data.
         * 
         * @return the builder instance
         */
        public static IdentityTokenBuilder newBuilder() {
            IdentityTokenBuilder builder = new IdentityTokenBuilder();
            builder.token = new IdentityToken(null, null, null);
            builder.token.type = TokenType.ANONYMOUS;
            return builder;
        }
        
        /**
         * Creates an {@link TokenType#ANONYMOUS anonymous} identity token.
         *
         * @param tokenPolicyId the token policy id
         * @param signatureAlgorithm the signature algorithm used to sign the token
         * @param signature the token signature
         * @return the builder instance
         */
        public static IdentityTokenBuilder newBuilder(String tokenPolicyId, String signatureAlgorithm, 
            byte[] signature) {
            IdentityTokenBuilder builder = new IdentityTokenBuilder();
            builder.token = new IdentityToken(tokenPolicyId, signatureAlgorithm, signature);
            builder.token.type = TokenType.ANONYMOUS;
            return builder;
        }
        
        /**
         * Turns this token into an issued token (only if underlying token is still {@link TokenType#ANONYMOUS}.
         * 
         * @param tokenData the actual token data 
         * @param tokenEncryptionAlgorithm the token encryption algorithm
         * @return <b>this</b> builder
         */
        public IdentityTokenBuilder setIssuedToken(byte[] tokenData, String tokenEncryptionAlgorithm) {
            if (token.type == TokenType.ANONYMOUS) {
                token.type = TokenType.ISSUED;
                token.tokenData = tokenData;
                token.tokenEncryptionAlgorithm = tokenEncryptionAlgorithm;
            }
            return this;
        }

        /**
         * Turns this token into an X509 token (only if underlying token is still {@link TokenType#ANONYMOUS}.
         * 
         * @param tokenData the actual X509 token data 
         * @return <b>this</b> builder
         */
        public IdentityTokenBuilder setX509Token(byte[] tokenData) {
            if (token.type == TokenType.ANONYMOUS) {
                token.type = TokenType.X509;
                token.tokenData = tokenData;
            }
            return this;
        }
        
        /**
         * Turns this token into an user name token (only if underlying token is still {@link TokenType#ANONYMOUS}.
         * 
         * @param userName the user name this token is issued for
         * @param tokenData the actual token data 
         * @param tokenEncryptionAlgorithm the token encryption algorithm
         * @return <b>this</b> builder
         */
        public IdentityTokenBuilder setUsernameToken(String userName, byte[] tokenData, 
            String tokenEncryptionAlgorithm) {
            if (token.type == TokenType.ANONYMOUS) {
                token.type = TokenType.USERNAME;
                token.userName = userName;
                token.tokenData = tokenData;
                token.tokenEncryptionAlgorithm = tokenEncryptionAlgorithm;
            }
            return this;
        }
        
        /**
         * Finally creates the token.
         * 
         * @return the token instance
         */
        public IdentityToken build() {
            return token;
        }
        
    }
    
    private String tokenPolicyId;
    private TokenType type;
    private String userName;
    private byte[] tokenData;
    private String tokenEncryptionAlgorithm;
    private String signatureAlgorithm;
    private byte[] signature;

    /**
     * Creates a signed identity token.
     *
     * @param tokenPolicyId the token policy id
     * @param signatureAlgorithm the signature algorithm used to sign the token
     * @param signature the token signature
     */
    private IdentityToken(String tokenPolicyId, String signatureAlgorithm, byte[] signature) {
        this.tokenPolicyId = tokenPolicyId;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signature = signature;
    }
    
    /**
     * Returns the policy id of the token.
     * 
     * @return the policy id
     */
    public String getTokenPolicyId() {
        return tokenPolicyId;
    }
    
    /**
     * Returns the signature algorithm used for signing the token.
     * 
     * @return the signature algorithm
     */
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }
    
    /**
     * Returns the signature.
     * 
     * @return the token signature
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Returns the token type determining whether {@link #getUserName()}, {@link #getTokenData()} or 
     * {@link #getTokenEncryptionAlgorithm()} shall be filled.
     * 
     * @return the token type
     */
    public TokenType getType() {
        return type;
    }

    /**
     * The user name, only if {@link TokenType#USERNAME}.
     * 
     * @return the user name, may be <b>null</b> for wrong token type
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the token data, for all token types except for {@link TokenType#ANONYMOUS}.
     * 
     * @return the token data, may be <b>null</b> for wrong token type
     */
    public byte[] getTokenData() {
        return tokenData;
    }

    /**
     * Returns the token data as String, for all token types except for {@link TokenType#ANONYMOUS}.
     * 
     * @return the token data as String, may be <b>null</b> for wrong token type
     */
    public String getTokenDataAsString() {
        String result = null;
        if (null != tokenData) {
            for (Charset cs : Charset.availableCharsets().values()) {
                if (cs.displayName().equals(tokenEncryptionAlgorithm)) {
                    result = new String(tokenData, cs);
                    break;
                }
            }
            if (null == result) {
                result = new String(tokenData);
            }
        }
        return result;
    }

    /**
     * Returns the token data as char array, for all token types except for {@link TokenType#ANONYMOUS}.
     * 
     * @return the token data as char array, may be <b>null</b> for wrong token type
     */
    public char[] getTokenDataAsCharArray() {
        String tokenData = getTokenDataAsString();
        return null == tokenData ? null : tokenData.toCharArray();
    }

    /**
     * The token encryption algorithm, for {@link TokenType#ISSUED} or {@link TokenType#USERNAME}.
     * 
     * @return the token encryption algorithm, may be <b>null</b> for wrong token type
     */
    public String getTokenEncryptionAlgorithm() {
        return tokenEncryptionAlgorithm;
    }
    
}
