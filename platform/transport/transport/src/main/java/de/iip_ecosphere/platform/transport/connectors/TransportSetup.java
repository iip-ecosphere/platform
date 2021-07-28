package de.iip_ecosphere.platform.transport.connectors;

import de.iip_ecosphere.platform.transport.connectors.TransportParameter.TransportParameterBuilder;

public class TransportSetup {
    
    private String host;
    private int port;
    private String password; // preliminary, AMQP
    private String user; // preliminary, AMQP
    
    /**
     * Returns the server/broker host name.
     * 
     * @return the server/broker host name.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the server/broker port number.
     * 
     * @return the server/broker port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the password.
     * 
     * @return the password (may be <b>null</b>, to be ignored then)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user name.
     * 
     * @return the user name (may be <b>null</b>, to be ignored then)
     */
    public String getUser() {
        return user;
    }

    /**
     * Defines the server/broker host name. [required by snakeyaml]
     * 
     * @param host the server/broker host name.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Defines the server/broker port number. [required by snakeyaml]
     * 
     * @param port the server/broker port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Defines the password. [required by snakeyaml]
     * 
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Defines the user name. [required by snakeyaml]
     * 
     * @param user the user name
     */
    public void setUser(String user) {
        this.user = user;
    }
    
    /**
     * Derives a transport parameter instance.
     * 
     * @return the transport parameter instance
     */
    public TransportParameter createParameter() {
        return TransportParameterBuilder.newBuilder(host, port)
            .setUser(user, password)
            .build();
    }

}
