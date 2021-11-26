package de.iip_ecosphere.platform.support.aas.basyx;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * A factory for creating an HTTPS client. By default, the client
 * is created for no verification and validation for self signed SSL.
 * 
 * @author haque (taken over from BaSyx tests)
 * @author Holger Eichelberger, SSE
 */
public class BaSyxJerseyHttpsClientFactory {

    public static final String TLS_V1 = "TLSv1";
    public static final String DEFAULT_PROTOCOL = TLS_V1;
    public static final HostnameVerifier ALLOW_ALL_HOSTS = new HostnameVerifier() {

        @Override
        public boolean verify(String hostname, SSLSession sslSession) {
            return true;
        }
        
    }; 
    
    private HostnameVerifier hostNameVerifier;
    private TrustManager[] trustManagers;
    private KeyManager[] keyManagers;
    private SecureRandom seed;
    private String protocol;
    
    /**
     * Creates a default HTTPS client factory with {@link #DEFAULT_PROTOCOL}, {@link #ALLOW_ALL_HOSTS}, new 
     * secure random seed for self-signed certificates and TSLv1 protocol.
     */
    public BaSyxJerseyHttpsClientFactory() {
        protocol = DEFAULT_PROTOCOL;
        hostNameVerifier = ALLOW_ALL_HOSTS;
        X509TrustManager selfTrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        trustManagers = new TrustManager[] {selfTrustManager};
        keyManagers = null;
        seed = new SecureRandom();
    }
    
    /**
     * Creates a default HTTPS client factory with {@link #DEFAULT_PROTOCOL}, {@link #ALLOW_ALL_HOSTS} and new 
     * secure random seed.
     * 
     * @param keyManagers the key managers
     * @param trustManagers the trust managers.
     */
    public BaSyxJerseyHttpsClientFactory(KeyManager[] keyManagers, TrustManager[] trustManagers) {
        this.protocol = DEFAULT_PROTOCOL;
        this.hostNameVerifier = ALLOW_ALL_HOSTS;
        this.keyManagers = keyManagers;
        this.trustManagers = trustManagers;
        this.seed = new SecureRandom();
    }
    
    /**
     * Creates a default HTTPS client factory for self-signed certificates and TSLv1 protocol.
     * 
     * @param protocol the protocol, e.g., TLSv1
     * @param hostNameVerifier the host name verifier
     * @param keyManagers the key managers
     * @param seed the context initialization seed
     * @param trustManagers the trust managers.
     */
    public BaSyxJerseyHttpsClientFactory(String protocol, HostnameVerifier hostNameVerifier, KeyManager[] keyManagers, 
        SecureRandom seed, TrustManager[] trustManagers) {
        this.protocol = protocol;
        this.hostNameVerifier = hostNameVerifier;
        this.keyManagers = keyManagers;
        this.trustManagers = trustManagers;
        this.seed = seed;
    }
    
    /**
     * Returns an HTTPS client.
     * 
     * @return the HTTPS client
     * @throws KeyManagementException in case that key management problems occur
     * @throws NoSuchAlgorithmException if the SSL algorithm is not available
     */
    public Client getJerseyHTTPSClient() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = getSslContext();
        return ClientBuilder.newBuilder()
            .sslContext(sslContext)
            .hostnameVerifier(hostNameVerifier)
            .build();
    }

    /**
     * Retrieves an SSL Context.
     * 
     * @return the SSL context
     * @throws KeyManagementException in case that key management problems occur
     * @throws NoSuchAlgorithmException if the protocol algorithm is not available
     */
    private SSLContext getSslContext() throws KeyManagementException, NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(keyManagers, trustManagers, seed);
        return sslContext;
    }
}
