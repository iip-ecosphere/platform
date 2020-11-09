package de.iip_ecosphere.platform.transport.connectors;

/**
 * Captures common transport parameter for all connector types.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportParameter {

    private String host;
    private int port;
    private int actionTimeout = 1000; // unclear default for now, may require different constructor
    private String clientId;
    private int keepAlive = 2000; // unclear default for now, may require different constructor

    /**
     * Creates a transport parameter instance.
     * 
     * @param host     the network name of the host
     * @param port     the TCP communication port of the host
     * @param clientId the unique client identifier
     */
    public TransportParameter(String host, int port, String clientId) {
        this.host = host;
        this.port = port;
        this.clientId = clientId;
    }

    /**
     * Returns the network name of the host.
     * 
     * @return the name
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the TCP communication port of the host.
     * 
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the timeout for individual send/receive actions.
     * 
     * @return the timeout in milliseconds
     */
    public int getActionTimeout() {
        return actionTimeout;
    }

    /**
     * Returns the time to keep a connection alive.
     * 
     * @return the time in milliseconds
     */
    public int getKeepAlive() {
        return keepAlive;
    }

    /**
     * Returns the unique client identifier.
     * 
     * @return the unique client identifier
     */
    public String getClientId() {
        return clientId;
    }

    // TODO per stream: authentication, TLS

}
