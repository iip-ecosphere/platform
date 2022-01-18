package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

/**
 * A test data class.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class TransportMessage {
    
    private String streamId;
    private byte[] messageByte;
    private String requestWatch;

    /**
     * Creates an empty TransportMessage instance.
     * 
     */
    public TransportMessage() {
    }
    
    /**
     * Creates a TransportMessage instance.
     * 
     * @param requestWatch is the Request Watch Type for the stream message
     * @param streamId the streamId for the stream message
     * @param messageByte the text for the message in bytes
     */
    public TransportMessage(String streamId, byte[] messageByte, String requestWatch) {
        super();
        this.streamId = streamId;
        this.messageByte = messageByte;
        this.requestWatch = requestWatch;
    }

    /**
     * Returns the Request Watch Type for the stream message.
     * 
     * @return the RequestWatchType for the stream message
     */
    public String getRequestWatch() {
        return requestWatch;
    }

    /**
     * Set the RequestWatchType for the stream message.
     *
     * @param requestWatch is the Request Watch Type for the stream message
     */
    public void setRequestWatch(String requestWatch) {
        this.requestWatch = requestWatch;
    }

    /**
     * Returns the streamId for the stream message.
     * 
     * @return the streamId for the stream message
     */
    public String getStreamId() {
        return streamId;
    }

    /**
     * Set the streamId for the stream message.
     *
     * @param streamId is the streamId for the stream message
     */
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }
    
    /**
     * Returns the text for the message.
     * 
     * @return the messageTxt
     */
    public byte[] getMessageByte() {
        return messageByte; 
    }

    /**
     * Set the method of the text for the message.
     *
     * @param messageByte the text for the message
     */
    public void setMessageByte(byte[] messageByte) {
        this.messageByte = messageByte;
    } 
 
    /**
     * Generate and append id number for the stream id.
     *
     */
    public void generateStreamIdNo() {
        this.streamId = this.streamId + System.nanoTime();
    }   
}
