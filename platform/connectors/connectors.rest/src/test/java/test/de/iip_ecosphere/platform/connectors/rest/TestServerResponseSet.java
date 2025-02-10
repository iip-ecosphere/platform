package test.de.iip_ecosphere.platform.connectors.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.iip_ecosphere.platform.connectors.rest.RESTServerResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestServerResponseSet extends RESTServerResponse {

    @JsonProperty("context")
    private String context;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("items")
    private TestServerResponseSetItem[] items;
    
    /**
     * Constructor.
     */
    public TestServerResponseSet() {
        
    }
    
    /**
     * Getter for context.
     * 
     * @return context
     */
    public String getContext() {
        return context;
    }

    /**
     * Setter for context.
     * 
     * @param context to set
     */
    public void setContext(String context) {
        this.context = context;
    }
    
    /**
     * Getter for timestamp.
     * 
     * @return timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Setter for timestamp.
     * 
     * @param timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Getter for items.
     * 
     * @return items;
     */
    public TestServerResponseSetItem[] getItems() {
        return items;
    }
    
    /**
     * Setter for items.
     * 
     * @param items to set
     */
    public void setItems(TestServerResponseSetItem[] items) {
        this.items = items;
    }

    
    @Override
    public Object getValueToWrite() {
        return null;
    }

    @Override
    protected Class<?>[] getItemClasses() {
        // TODO Auto-generated method stub
        return new Class[] {TestServerResponseSetItem.class};
    }
}
