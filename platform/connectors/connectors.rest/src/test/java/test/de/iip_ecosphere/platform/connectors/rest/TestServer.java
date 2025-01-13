package test.de.iip_ecosphere.platform.connectors.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("de.iip_ecosphere.platform.connectors.rest") 
@ComponentScan("test.de.iip_ecosphere.platform.connectors.rest") 
public class TestServer {
    
    private ConfigurableApplicationContext context;

    private String endpointDescriptionSingle;
    private String endpointDescriptionSingleWP;
    private String endpointDescriptionSet;
    private String endpointDescriptionSetWP;


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
        endpointDescriptionSingle += "\"String\" : {\"type\" : \"String\", \"endpoint\" : \"string\"},";
        endpointDescriptionSingle += "\"Short\" : {\"type\" : \"Short\", \"endpoint\" : \"short\"},";
        endpointDescriptionSingle += "\"Integer\" : {\"type\" : \"Integer\", \"endpoint\" : \"integer\"},";
        endpointDescriptionSingle += "\"Long\" : {\"type\" : \"Long\", \"endpoint\" : \"long\"},";
        endpointDescriptionSingle += "\"Float\" : {\"type\" : \"Float\", \"endpoint\" : \"float\"},";
        endpointDescriptionSingle += "\"Double\" : {\"type\" : \"Double\", \"endpoint\" : \"double\"}";
        endpointDescriptionSingle += "}";
    }

    /**
     * Creates the endpointDescriptionSet.
     */
    private void createEndpointDescriptionSet() {
        endpointDescriptionSet = "{";
        endpointDescriptionSet += "\"All\" : {\"type\" : \"ValueSet\", \"endpoint\" : \"all\"}";
        endpointDescriptionSet += "}";
    }

    /**
     * Creates the endpointDescriptionSingleWP.
     */
    private void createEndpointDescriptionSingleWP() {
        endpointDescriptionSingleWP = "{";
        endpointDescriptionSingleWP += "\"String\" : {\"type\" : \"String\", \"endpoint\" : \"?path=string\"},";
        endpointDescriptionSingleWP += "\"Short\" : {\"type\" : \"Short\", \"endpoint\" : \"?path=short\"},";
        endpointDescriptionSingleWP += "\"Integer\" : {\"type\" : \"Integer\", \"endpoint\" : \"?path=integer\"},";
        endpointDescriptionSingleWP += "\"Long\" : {\"type\" : \"Long\", \"endpoint\" : \"?path=long\"},";
        endpointDescriptionSingleWP += "\"Float\" : {\"type\" : \"Float\", \"endpoint\" : \"?path=float\"},";
        endpointDescriptionSingleWP += "\"Double\" : {\"type\" : \"Double\", \"endpoint\" : \"?path=double\"}";
        endpointDescriptionSingleWP += "}";
    }

    /**
     * Creates the endpointDescriptionSetWP.
     */
    private void createEndpointDescriptionSetWP() {
        endpointDescriptionSetWP = "{";
        endpointDescriptionSetWP += "\"AllWP\" : {\"type\" : \"ValueSet\", \"endpoint\" : "
                + "\"?paths=string,short,integer,long,float,double\"}";
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
        return endpointDescriptionSingleWP;
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
