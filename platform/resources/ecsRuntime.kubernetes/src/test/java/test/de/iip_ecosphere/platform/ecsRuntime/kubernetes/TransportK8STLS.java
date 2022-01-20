package test.de.iip_ecosphere.platform.ecsRuntime.kubernetes;

import test.de.iip_ecosphere.platform.transport.AbstractTransportConnectorTest.TransportParameterConfigurer;

/**
 * Tansport security TLS information.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class TransportK8STLS {
    private boolean tlsCheck;
    private TransportParameterConfigurer configurer;
    
    /**
     * Creates a TransportK8STLS instance.
     * 
     * @param tlsCheck use of the tls security
     * @param configurer the tls configurer
     * 
     */
    public TransportK8STLS(boolean tlsCheck, TransportParameterConfigurer configurer) {
        super();
        this.tlsCheck = tlsCheck;
        this.configurer = configurer;
    }
    
    /**
     * Returns using the tls security.
     * 
     * @return use of the tls security
     */
    public boolean isTlsCheck() {
        return tlsCheck;
    }
    
    /**
     * Set the use of the tls security.
     *
     * @param tlsCheck use of the tls security
     */
    public void setTlsCheck(boolean tlsCheck) {
        this.tlsCheck = tlsCheck;
    }
    
    /**
     * Returns the tls configurer.
     * 
     * @return the tls configurer
     */
    public TransportParameterConfigurer getConfigurer() {
        return configurer;
    }

    /**
     * Set the tls configurer.
     *
     * @param configurer the tls configurer
     */
    public void setConfigurer(TransportParameterConfigurer configurer) {
        this.configurer = configurer;
    }
    
    
}
