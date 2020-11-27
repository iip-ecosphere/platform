package de.iip_ecosphere.platform.transport.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

import de.iip_ecosphere.platform.transport.TransportFactory;

/**
 * Configures the {@link TransportFactory}.
 * 
 * @author Holger Eichelberger, SSE
 */
@ConfigurationProperties(prefix = "transport")
public class TransportFactoryConfiguration {
    
    private String mainTransportClassName = "";
    private String interProcessClassName = "";
    private String directMemoryClassName = "";

    /**
     * Returns the class name of the main transport connector class.
     * 
     * @return the class name (empty if not configured)
     */
    public String getMainTransportClassName() {
        return mainTransportClassName;
    }

    /**
     * Returns the class name of the inter-process transport connector class.
     * 
     * @return the class name (empty if not configured)
     */
    public String getInterProcessClassName() {
        return interProcessClassName;
    }

    /**
     * Returns the class name of the direct-memory transport connector class.
     * 
     * @return the class name (empty if not configured)
     */
    public String getDirectMemoryClassName() {
        return directMemoryClassName;
    }

    // setters required for @ConfigurationProperties

    /**
     * Changes the class name of the main transport connector class.
     * 
     * @param mainTransportClassName the class name
     */
    public void setMainTransportClassName(String mainTransportClassName) {
        this.mainTransportClassName = mainTransportClassName;
    }

    /**
     * Changes the class name of the inter-process transport connector class.
     * 
     * @param interProcessClassName the class name
     */
    public void setInterProcessClassName(String interProcessClassName) {
        this.interProcessClassName = interProcessClassName;
    }

    /**
     * Changes the class name of the direct-memory transport connector class.
     * 
     * @param directMemoryClassName the class name
     */
    public void setDirectMemoryClassName(String directMemoryClassName) {
        this.directMemoryClassName = directMemoryClassName;
    }
    
}
