package de.iip_ecosphere.platform.support.aas.basyx;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.eclipse.basyx.vab.protocol.http.connector.HTTPConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTPS Connector class which can be used for creating an HTTPS Client. 
 * 
 * @author haque (taken over from BaSyx tests)
 * @author Holger Eichelberger, SSE
 */
public class BaSyxHTTPSConnector extends HTTPConnector {
    
    private static Logger logger = LoggerFactory.getLogger(BaSyxHTTPSConnector.class);
    
    /**
     * Initiates an HTTPSConnector with given address and HTTPS factory.
     * 
     * @param address the server address
     * @param factory the HTTPS factory
     */
    public BaSyxHTTPSConnector(String address, BaSyxJerseyHttpsClientFactory factory) {
        super(address);
        setHttpsClient(factory);
    }
    
    /**
     * Initiates an HTTPSConnector with given address and media type.
     * 
     * @param address the server address
     * @param mediaType the media type
     * @param factory the HTTPS factory
     */
    public BaSyxHTTPSConnector(String address, String mediaType, BaSyxJerseyHttpsClientFactory factory) { 
        super(address, mediaType);
        setHttpsClient(factory);
    }
    
    /**
     * Configures the client so that it can run with HTTPS protocol.
     * 
     * @param factory the HTTPS factory
     */
    private void setHttpsClient(BaSyxJerseyHttpsClientFactory factory) {
        try {
            this.client = factory.getJerseyHTTPSClient();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error("Cannot create a HTTPS client: " + e.getMessage());
        }
    }
}
