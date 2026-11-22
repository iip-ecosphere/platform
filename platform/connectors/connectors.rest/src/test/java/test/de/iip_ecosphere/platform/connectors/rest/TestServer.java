package test.de.iip_ecosphere.platform.connectors.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("test.de.iip_ecosphere.platform.connectors.rest") 
public class TestServer {
    
    private ConfigurableApplicationContext context;

    private String endpointDescriptionSingle;
    private String endDesSingleWP;
    private String endpointDescriptionSet;
    private String endpointDescriptionSetWP;
    private String endpointPrefix;

    /**
     * Creates a TestServer instance.
     */
    public TestServer() {
        createEndpointDescriptionSingle();
        createEndpointDescriptionSingleWP();
        createEndpointDescriptionSet();
        createEndpointDescriptionSetWP();
    }

    /**
     * Starts the TestServer.
     */
    public void start() {
        context = SpringApplication.run(TestServer.class);
    }

    /**
     * Stops the TestServer.
     */
    public void stop() {
        if (context != null) {
            context.close();
        }
    }

    /**
     * Creates the endpointDescriptionSingle.
     */
    private void createEndpointDescriptionSingle() {
        endpointDescriptionSingle = "{";
        endpointDescriptionSingle += "\"string\" : {\"name\" : \"string\",\"endpoint\" : \"" 
                + endpoint("endpoints/string") + "\", " 
                + "\"type\" : \"TestServerResponsSingle\", \"asSingleValue\" : true},";
        endpointDescriptionSingle += "\"short\" : {\"name\" : \"short\",\"endpoint\" : \"" 
                + endpoint("endpoints/short") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endpointDescriptionSingle += "\"integer\" : {\"name\" : \"integer\",\"endpoint\" : \"" 
                + endpoint("endpoints/integer") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endpointDescriptionSingle += "\"long\" : {\"name\" : \"long\",\"endpoint\" : \"" 
                + endpoint("endpoints/long") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endpointDescriptionSingle += "\"float\" : {\"name\" : \"float\",\"endpoint\" : \"" 
                + endpoint("endpoints/float") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endpointDescriptionSingle += "\"double\" : {\"name\" : \"double\",\"endpoint\" : \"" 
                + endpoint("endpoints/double") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"}";
        endpointDescriptionSingle += "}";
    }
    
    /**
     * Adjusts the given {@code endpoint} by prefixing {@link #endpointPrefix} if not <b>null</b>.
     * 
     * @param endpoint the endpoint to modify
     * @return {@code endpoint} or {@code endpoint} prefixed by {@link #endpointPrefix}
     */
    private String endpoint(String endpoint) {
        return null == endpointPrefix ? endpoint : endpointPrefix + endpoint;
    }

    /**
     * Sets the endpoint prefix.
     * 
     * @param endpointPrefix the endpoint prefix, shall be a URL ending with "/" or <b>null</b> for none
     */
    public void setEndpointPrefix(String endpointPrefix) {
        this.endpointPrefix = endpointPrefix;
        createEndpointDescriptionSingle();
        createEndpointDescriptionSingleWP();
        createEndpointDescriptionSet();
        createEndpointDescriptionSetWP();
    }

    /**
     * Creates the endpointDescriptionSet.
     */
    private void createEndpointDescriptionSet() {
        endpointDescriptionSet = "{";
        endpointDescriptionSet += "\"all\" : {\"endpoint\" : \"" + endpoint("endpoints/all") + "\", "
                + "\"type\" : \"TestServerResponseSet\"}";
        endpointDescriptionSet += "}";
    }

    /**
     * Creates the endpointDescriptionSingleWP.
     */
    private void createEndpointDescriptionSingleWP() {
        endDesSingleWP = "{";
        endDesSingleWP += "\"string\" : {\"endpoint\" : \"" + endpoint("endpoints?path=string") + "\","
                + " \"type\" : \"TestServerResponsSingle\", \"asSingleValue\" : true},";
        endDesSingleWP += "\"short\" : {\"endpoint\" : \"" + endpoint("endpoints?path=short") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endDesSingleWP += "\"integer\" : {\"endpoint\" : \"" + endpoint("endpoints?path=integer") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endDesSingleWP += "\"long\" : {\"endpoint\" : \"" + endpoint("endpoints?path=long") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endDesSingleWP += "\"float\" : {\"endpoint\" : \"" + endpoint("endpoints?path=float") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endDesSingleWP += "\"double\" : {\"endpoint\" : \"" + endpoint("endpoints?path=double") + "\", "
                + "\"type\" : \"TestServerResponsSingle\"}";
        endDesSingleWP += "}";
    }

    /**
     * Creates the endpointDescriptionSetWP.
     */
    private void createEndpointDescriptionSetWP() {
        endpointDescriptionSetWP = "{";
        endpointDescriptionSetWP += "\"AllWP\" : {\"endpoint\" : "
                + "\"" + endpoint("endpoints/set?paths=string,short,integer,long,float,double") + "\", "
                + "\"type\" : \"TestServerResponseSet\"}";
        endpointDescriptionSetWP += "}";
    }

    /**
     * Getter for endpointDescriptionSingle.
     * 
     * @return endpointDescriptionSingle
     */
    public String getEndpointDescriptionSingle() {
        return endpointDescriptionSingle;
    }

    /**
     * Getter for endpointDescriptionSingleWP.
     * 
     * @return endpointDescriptionSingleWP
     */
    public String getEndpointDescriptionSingleWP() {
        return endDesSingleWP;
    }

    /**
     * Getter for endpointDescriptionSet.
     * 
     * @return endpointDescriptionSet
     */
    public String getEndpointDescriptionSet() {
        return endpointDescriptionSet;
    }

    /**
     * Getter for endpointDescriptionSetWP.
     * 
     * @return endpointDescriptionSetWP
     */
    public String getEndpointDescriptionSetWP() {
        return endpointDescriptionSetWP;
    }
}
