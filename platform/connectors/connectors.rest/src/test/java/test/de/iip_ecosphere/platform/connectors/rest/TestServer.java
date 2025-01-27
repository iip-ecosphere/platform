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
    private String endDesSingleWP;
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
        endpointDescriptionSingle += "\"String\" : {\"endpoint\" : \"string\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"Short\" : {\"endpoint\" : \"short\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"Integer\" : {\"endpoint\" : \"integer\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"Long\" : {\"endpoint\" : \"long\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"Float\" : {\"endpoint\" : \"float\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"Double\" : {\"endpoint\" : \"double\", \"responseTypeIndex\" : \"0\"}";
        endpointDescriptionSingle += "}";
    }

    /**
     * Creates the endpointDescriptionSet.
     */
    private void createEndpointDescriptionSet() {
        endpointDescriptionSet = "{";
        endpointDescriptionSet += "\"All\" : {\"endpoint\" : \"all\", \"responseTypeIndex\" : \"0\"}";
        endpointDescriptionSet += "}";
    }

    /**
     * Creates the endpointDescriptionSingleWP.
     */
    private void createEndpointDescriptionSingleWP() {
        endDesSingleWP = "{";
        endDesSingleWP += "\"String\" : {\"endpoint\" : \"?path=string\", \"responseTypeIndex\" : \"0\"},";
        endDesSingleWP += "\"Short\" : {\"endpoint\" : \"?path=short\", \"responseTypeIndex\" : \"0\"},";
        endDesSingleWP += "\"Integer\" : {\"endpoint\" : \"?path=integer\", \"responseTypeIndex\" : \"0\"},";
        endDesSingleWP += "\"Long\" : {\"endpoint\" : \"?path=long\", \"responseTypeIndex\" : \"0\"},";
        endDesSingleWP += "\"Float\" : {\"endpoint\" : \"?path=float\", \"responseTypeIndex\" : \"0\"},";
        endDesSingleWP += "\"Double\" : {\"endpoint\" : \"?path=double\", \"responseTypeIndex\" : \"0\"}";
        endDesSingleWP += "}";
    }

    /**
     * Creates the endpointDescriptionSetWP.
     */
    private void createEndpointDescriptionSetWP() {
        endpointDescriptionSetWP = "{";
        endpointDescriptionSetWP += "\"AllWP\" : {\"endpoint\" : "
                + "\"set?paths=string,short,integer,long,float,double\", \"responseTypeIndex\" : \"0\"}";
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
