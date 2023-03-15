package de.iip_ecosphere.platform.support.aas.basyx.basyx;

/*******************************************************************************
 * Copyright (C) 2021 the Eclipse BaSyx Authors
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import org.eclipse.basyx.vab.coder.json.provider.JSONProvider;
import org.eclipse.basyx.vab.exception.provider.ProviderException;
import org.eclipse.basyx.vab.modelprovider.api.IModelProvider;
import org.eclipse.basyx.vab.protocol.basyx.CoderTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider class that enables access to an IModelProvider via native BaSyx
 * protocol. Taken over from BaSyx to be able to run BaSyx with JDK 8.
 * 
 * @author kuhn, pschorn
 *
 */
public class VABBaSyxTCPInterface<ModelProvider extends IModelProvider> extends Thread {

    /**
     * BaSyx get command.
     */
    public static final byte BASYX_GET = 0x01;

    /**
     * BaSyx set command.
     */
    public static final byte BASYX_SET = 0x02;

    /**
     * BaSyx create command.
     */
    public static final byte BASYX_CREATE = 0x03;

    /**
     * BaSyx delete command.
     */
    public static final byte BASYX_DELETE = 0x04;

    /**
     * BaSyx invoke command.
     */
    public static final byte BASYX_INVOKE = 0x05;

    /**
     * BaSyx result 'OK' : 0x00.
     */
    public static final byte BASYX_RESULT_OK = 0x00;

    private static Logger logger = LoggerFactory.getLogger(VABBaSyxTCPInterface.class);

    /**
     * Reference to IModelProvider backend.
     */
    protected JSONProvider<ModelProvider> providerBackend = null;

    /**
     * Socket communication channel.
     */
    protected SocketChannel commChannel = null;

    /**
     * Constructor that accepts an already created server socket channel.
     * 
     * @param modelProviderBackend model provider backend
     * @param channel channel
     */
    public VABBaSyxTCPInterface(ModelProvider modelProviderBackend, SocketChannel channel) {
        // Store reference to channel and backend
        providerBackend = new JSONProvider<ModelProvider>(modelProviderBackend);
        commChannel = channel;
        setName(VABBaSyxTCPInterface.class.getName() + " " + System.currentTimeMillis());
        logger.debug("Socket created");
    }
    
    // checkstyle: stop method length check

    /**
     * Process input frame.
     * 
     * @param rxFrame received frame
     * @throws IOException if I/O operations fail
     */
    public void processInputFrame(byte[] rxFrame) throws IOException {
        // Create output streams
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Get command
        switch (rxFrame[0]) {

        case BASYX_GET: {
            // Get path string
            int pathLen = CoderTools.getInt32(rxFrame, 1);
            String path = new String(rxFrame, 1 + 4, pathLen);

            // Forward request to provider
            try {
                providerBackend.processBaSysGet(path, output);
            } catch (ProviderException e) {
                logger.debug("Exception in BASYX_GET", e);
                // Catch Exceptions from JSONProvider
                // No further action here, as the current version
                // of the TCP-Mapping states, that always Statuscode 0x00 
                // should be returned with Exceptions encoded in returned String
            }

            // System.out.println("Processed GET:"+path);

            // Send response frame
            output.flush();
            sendResponseFrame(output);

            break;
        }

        case BASYX_SET: {
            // Get path string length and value
            int pathLen = CoderTools.getInt32(rxFrame, 1);
            String path = new String(rxFrame, 1 + 4, pathLen);
            // Get value string length and value
            int jsonValueLen = CoderTools.getInt32(rxFrame, 1 + 4 + pathLen);
            String jsonValue = new String(rxFrame, 1 + 4 + pathLen + 4, jsonValueLen);

            // Invoke get operation
            try {
                providerBackend.processBaSysSet(path, jsonValue, output);
            } catch (ProviderException e) {
                logger.debug("Exception in BASYX_SET", e);
                // Catch Exceptions from JSONProvider
                // No further action here, as the current version
                // of the TCP-Mapping states, that always Statuscode 0x00 
                // should be returned with Exceptions encoded in returned String
            }

            // Send response frame
            output.flush();
            sendResponseFrame(output);

            break;
        }

        case BASYX_CREATE: {
            // Get path string length and value
            int pathLen = CoderTools.getInt32(rxFrame, 1);
            String path = new String(rxFrame, 1 + 4, pathLen);
            // Get value string length and value
            int jsonValueLen = CoderTools.getInt32(rxFrame, 1 + 4 + pathLen);
            String jsonValue = new String(rxFrame, 1 + 4 + pathLen + 4, jsonValueLen);

            // Invoke get operation
            try {
                providerBackend.processBaSysCreate(path, jsonValue, output);
            } catch (ProviderException e) {
                logger.debug("Exception in BASYX_CREATE", e);
                // Catch Exceptions from JSONProvider
                // No further action here, as the current version
                // of the TCP-Mapping states, that always Statuscode 0x00 
                // should be returned with Exceptions encoded in returned String
            }

            // Send response frame
            output.flush();
            sendResponseFrame(output);

            break;
        }

        case BASYX_DELETE: {
            // Get path string length and value
            int pathLen = CoderTools.getInt32(rxFrame, 1);
            String path = new String(rxFrame, 1 + 4, pathLen);

            // Get value string length and value if available; default is null value
            String jsonValue = "";
            try {
                int jsonValueLen = CoderTools.getInt32(rxFrame, 1 + 4 + pathLen);
                jsonValue = new String(rxFrame, 1 + 4 + pathLen + 4, jsonValueLen);

            } catch (ArrayIndexOutOfBoundsException e) {
                // pass, provide serialize null argument to processBaSysDelete to indicate that
                // an
                // entity should be removed
            }

            // Invoke delete operation
            try {
                providerBackend.processBaSysDelete(path, jsonValue, output);
            } catch (ProviderException e) {
                logger.debug("Exception in BASYX_DELETE", e);
                // Catch Exceptions from JSONProvider
                // No further action here, as the current version
                // of the TCP-Mapping states, that always Statuscode 0x00 
                // should be returned with Exceptions encoded in returned String
            }

            // Send response frame
            output.flush();
            sendResponseFrame(output);

            break;
        }

        case BASYX_INVOKE: {
            // Get path string length and value
            int pathLen = CoderTools.getInt32(rxFrame, 1);
            String path = new String(rxFrame, 1 + 4, pathLen);
            // Get value string length and value
            int jsonValueLen = CoderTools.getInt32(rxFrame, 1 + 4 + pathLen);
            String jsonValue = new String(rxFrame, 1 + 4 + pathLen + 4, jsonValueLen);
            // Invoke get operation
            try {
                providerBackend.processBaSysInvoke(path, jsonValue, output);
            } catch (ProviderException e) {
                logger.debug("Exception in BASYX_INVOKE", e);
                // Catch Exceptions from JSONProvider
                // No further action here, as the current version
                // of the TCP-Mapping states, that always Statuscode 0x00 
                // should be returned with Exceptions encoded in returned String
            }

            // Send response frame
            output.flush();
            sendResponseFrame(output);

            break;
        }

        default:
            throw new RuntimeException("Unknown BaSyx TCP command received");
        }
    }
    
    // checkstyle: resume method length check

    /**
     * Sends a response to the client that carries the JSON response.
     * 
     * @param byteArrayOutput the output
     * @throws IOException in case of I/O exceptions
     */
    private void sendResponseFrame(ByteArrayOutputStream byteArrayOutput) throws IOException {
        // Create response frame with positive response
        sendResponseFrame(byteArrayOutput, BASYX_RESULT_OK);
    }

    /**
     * Sends a response to the client that carries the JSON response.
     * 
     * @param byteArrayOutput the output
     * @param result the result
     * @throws IOException in case of I/O exceptions
     */
    private void sendResponseFrame(ByteArrayOutputStream byteArrayOutput, int result) throws IOException {
        // Create response frame
        byte[] encodedResult = byteArrayOutput.toByteArray();
        int resultFrameSize = encodedResult.length + 1;
        byte[] frameLength = new byte[4];
        byte[] encodedResultLength = new byte[4];
        CoderTools.setInt32(frameLength, 0, resultFrameSize + 4);
        CoderTools.setInt32(encodedResultLength, 0, encodedResult.length);

        // Place response frame in buffer
        ByteBuffer buffer = ByteBuffer.allocate(resultFrameSize + 4 + 4);
        buffer.put(frameLength);
        buffer.put((byte) result);
        buffer.put(encodedResultLength);
        buffer.put(encodedResult);
        ((Buffer) buffer).flip();

        // Transmit response frame
        commChannel.write(buffer);

        // Reset output stream
        byteArrayOutput.reset();
    }

    /**
     * Read a number of bytes.
     * 
     * @param bytes the buffer to read from
     * @param expectedBytes the expected bytes
     * @throws IOException if the information cannot be read
     */
    protected void readBytes(ByteBuffer bytes, int expectedBytes) throws IOException {
        // Read bytes until buffer is full
        while (bytes.position() < expectedBytes) {
            int res = commChannel.read(bytes);
            if (res == -1) {
                throw new ClosedChannelException();
            }
        }
    }

    /**
     * Thread main function.
     */
    @Override
    public void run() {
        // Run forever (until socket is closed)
        while (true) {
            // Process inputs
            try {
                // Read response
                // - Wait for leading 4 byte header that contains frame length
                ByteBuffer rxBuffer1 = ByteBuffer.allocate(4);
                readBytes(rxBuffer1, 4);
                int frameSize = CoderTools.getInt32(rxBuffer1.array(), 0);
                logger.debug("Read Frame with size: " + frameSize);

                // Wait for frame to arrive
                ByteBuffer rxBuffer2 = ByteBuffer.allocate(frameSize);
                readBytes(rxBuffer2, frameSize);
                byte[] rxFrame = rxBuffer2.array();

                // Process input frame
                processInputFrame(rxFrame);
            } catch (IOException e) {
                // End when TCP socket is closed
                if (e instanceof ClosedChannelException) {
                    break;
                }

                // Output error
                logger.error("Exception in run", e); 
                break; // already filled the logs in uncontrolled manner
            }
        }
        try {
            commChannel.close();
        } catch (IOException e) {
            logger.debug("Exception while closing the comChannel ", e);
        }

        logger.debug(getName() + " Socket closed");
    }
}
