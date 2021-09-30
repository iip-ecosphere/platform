package de.iip_ecosphere.platform.support.aas.basyx.basyx;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.eclipse.basyx.vab.exception.provider.ProviderException;
import org.eclipse.basyx.vab.protocol.api.IBaSyxConnector;
import org.eclipse.basyx.vab.protocol.basyx.CoderTools;
import org.eclipse.basyx.vab.protocol.basyx.server.VABBaSyxTCPInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaSyx connector class. Taken over from BaSyx.
 * 
 * @author kuhn, pschorn, schnicke
 *
 */
public class BaSyxConnector implements IBaSyxConnector {
    
    private Logger logger = LoggerFactory.getLogger(BaSyxConnector.class);
    private InetSocketAddress serverSocketAddress;
    private SocketChannel channelToProvider;
    private boolean keepOpen = false;

    /**
     * Constructor that creates a connection. 
     * 
     * This constructor connects to port at name.
     * 
     * @param hostName the name of the target host
     * @param port the port number
     */
    public BaSyxConnector(String hostName, int port) {
        this(hostName, port, false);
    }
    
    /**
     * Constructor that creates a connection. 
     * 
     * This constructor connects to port at name.
     * 
     * @param hostName the name of the target host
     * @param port the port number
     * @param keepOpen whether the connection shall be kept open or terminated per request
     */
    public BaSyxConnector(String hostName, int port, boolean keepOpen) {
        // Base constructor
        super();
        this.keepOpen = keepOpen;
        
        // Exception handling
        try {
            // Resolve address
            InetAddress       serverIPAddress     = InetAddress.getByName(hostName);
            serverSocketAddress = new InetSocketAddress(serverIPAddress, port);
        } catch (IOException e) {
            // Print stack trace
            logger.error("Exception in BaSyxConnector: " + e.getMessage()); // IIP-Ecosphere: Do not emit stacktrace
        }
    }
    
    /**
     * Close connection.
     */
    private void closeConnection() {
        // Try to close connection
        try {
            channelToProvider.close();
        } catch (IOException e) {
            // Print stack trace
            logger.error("Exception in closeConnection: " + e.getMessage()); // IIP-Ecosphere: Do not emit stacktrace
        }
    }

    /**
     * Invoke a BaSyx operation in a remote provider.
     * 
     * @param call the serialized call
     * @return the call result
     */
    protected synchronized String invokeBaSyx(byte[] call) {
        // Catch exceptions
        try {
            // Send byte array (BaSyx operation) via channel to provider
            ByteBuffer txBuffer = ByteBuffer.wrap(call);

            if (!keepOpen || (keepOpen && channelToProvider == null)) {
                // Channel to provider
                channelToProvider = SocketChannel.open();
                // - Setup channel: set to blocking and connect to provider
                channelToProvider.configureBlocking(true);
                channelToProvider.connect(serverSocketAddress);
            }

            channelToProvider.write(txBuffer);

            // Read response
            // - Wait for leading 4 byte header that contains frame length
            ByteBuffer rxBuffer1 = ByteBuffer.allocate(4);
            // System.out.println("RX1");
            readBytes(rxBuffer1, 4);
            // System.out.println("RX1-d");
            int frameSize = CoderTools.getInt32(rxBuffer1.array(), 0);

            // Wait for frame to arrive
            ByteBuffer rxBuffer2 = ByteBuffer.allocate(frameSize);
            // System.out.println("RX2");
            readBytes(rxBuffer2, frameSize);
            // System.out.println("RX2-d");
            byte[] rxFrame = rxBuffer2.array();

            // Return received data

            // Result check
            if ((rxFrame == null) || (rxFrame.length < 2)) {
                return null;
            }

            // - FIXME: Check result on position 0
            
            // Extract response
            int jsonResultLen = CoderTools.getInt32(rxFrame, 1);
            String jsonResult = new String(rxFrame, 1 + 4, jsonResultLen);

            // Close connection to prevent unused open channels
            if (!keepOpen) {
                closeConnection();
            }

            // Return result
            return jsonResult.toString();
        } catch (IOException e) {
            // Print stack trace
            logger.error("Exception in invokeBaSyx: " + e.getMessage()); // IIP-Ecosphere: Do not emit stacktrace
        }

        // Indicate error
        return null;
    }
    
    
    /**
     * Read a number of bytes.
     * 
     * @param bytes the buffer to read from
     * @param expectedBytes the number of bytes to read
     */
    protected void readBytes(ByteBuffer bytes, int expectedBytes) {
        // Exception handling
        try {
            // System.out.println("-Reading:"+expectedBytes);
            // Read bytes until buffer is full
            while (bytes.position() < expectedBytes) {
                // System.out.println("-Pos:"+bytes.position());
                channelToProvider.read(bytes);
            }
            // System.out.println("-Read:"+expectedBytes);
        } catch (IOException e) {
            // Output exception
            logger.error("Exception in readBytes: " + e.getMessage()); // IIP-Ecosphere: Do not emit stacktrace
        }
    }
    
    @Override
    public String getValue(String servicePath) {

        byte[] call = createCall(servicePath, VABBaSyxTCPInterface.BASYX_GET);

        // Invoke BaSyx call and return result
        return invokeBaSyx(call);
    }

    @Override
    public String setValue(String servicePath, String newValue) {

        byte[] call = createCall(servicePath, newValue, VABBaSyxTCPInterface.BASYX_SET);

        // Invoke BaSyx call and return result
        return invokeBaSyx(call);
    }
    
    @Override
    public String createValue(String servicePath, String newValue) throws ProviderException {

        byte[] call = createCall(servicePath, newValue, VABBaSyxTCPInterface.BASYX_CREATE);

        // Invoke BaSyx call and return result
        return invokeBaSyx(call);
    }

    @Override
    public String invokeOperation(String servicePath, String parameters) throws ProviderException {

        byte[] call = createCall(servicePath, parameters, VABBaSyxTCPInterface.BASYX_INVOKE);

        // Invoke BaSyx call and return result
        return invokeBaSyx(call);

    }

    @Override
    public String deleteValue(String servicePath) throws ProviderException {

        byte[] call = createCall(servicePath, VABBaSyxTCPInterface.BASYX_DELETE);

        // Invoke BaSyx call and return result
        return invokeBaSyx(call);
    }

    @Override
    public String deleteValue(String servicePath, String jsonObject) throws ProviderException {

        byte[] call = createCall(servicePath, jsonObject, VABBaSyxTCPInterface.BASYX_DELETE);

        // Invoke BaSyx call and return result
        return invokeBaSyx(call);
    }

    
    /**
     * Create non-parameterized call that can be used as an argument to the
     * invokeBaSyx function.
     * 
     * @param servicePath the path to the service
     * @param callType the call type
     * @return the call result
     */
    private byte[] createCall(String servicePath, byte callType) {
        // Create call
        byte[] call = new byte[4 + 1 + 4 + servicePath.length()];
        // - Encode size does not include leading four bytes
        CoderTools.setInt32(call, 0, call.length - 4);
        // - Encode operation GET
        CoderTools.setInt8(call, 4, callType);
        // - Encode path length and path
        CoderTools.setInt32(call, 5, servicePath.length());
        CoderTools.setString(call, 9, servicePath);

        return call;
    }

    
    /**
     * Create parameterized byte call that can be used as an argument to the
     * invokeBaSyx function.
     * 
     * @param servicePath the path to the service
     * @param newValue the value
     * @param callType the call type
     * @return the result of the call
     */
    private byte[] createCall(String servicePath, String newValue, byte callType) {

        // Create call
        byte[] call = new byte[4 + 1 + 4 + servicePath.length() + 4 + newValue.length()];
        // - Encode size does not include leading four bytes
        CoderTools.setInt32(call, 0, call.length - 4);
        // - Encode operation SET
        CoderTools.setInt8(call, 4, callType);
        // - Encode path
        CoderTools.setInt32(call, 5, servicePath.length());
        CoderTools.setString(call, 9, servicePath);
        // - Encode value
        CoderTools.setInt32(call, 9 + servicePath.length(), newValue.length());
        CoderTools.setString(call, 9 + servicePath.length() + 4, newValue);

        return call;
    }

    /**
     * Get string representation of endpoint for given path for debugging. 
     * @param path Requested path
     * @return String representing requested endpoint
     */
    @Override
    public String getEndpointRepresentation(String path) {
        return "basyx://" + serverSocketAddress.getHostString() + ":" + serverSocketAddress.getPort() + "/" + path;
    }
}
