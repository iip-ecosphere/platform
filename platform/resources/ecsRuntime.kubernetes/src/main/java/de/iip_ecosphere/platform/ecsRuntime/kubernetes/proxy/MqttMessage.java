package de.iip_ecosphere.platform.ecsRuntime.kubernetes.proxy;

import java.util.Date;

/**
 * A test data class.
 * 
 * @author Ahmad Alamoush, SSE
 */
public class MqttMessage {
    
    private String streamId;
    private String messageTxt;

    /**
     * Creates a MqttMessage instance.
     * 
     * @param streamId the streamId for the stream message
     * @param messageTxt the text for the message
     */
    public MqttMessage(String streamId, String messageTxt) {
        super();
        this.streamId = streamId;
        this.messageTxt = messageTxt;
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
    public String getMessageTxt() {
        return messageTxt;
    }

    /**
     * Set the method of the text for the message.
     *
     * @param messageTxt the text for the message
     */
    public void setResponse(String messageTxt) {
        this.messageTxt = messageTxt;
    } 
 
    /**
     * Generate and append id number for the stream id.
     *
     */
    public void generateStreamIdNo() {
        this.streamId = this.streamId + new Date().getTime();
    }   
}
