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
        endpointDescriptionSingle += "\"string\" : {\"name\" : \"string\",\"endpoint\" : \"endpoints/string\", "
                + "\"type\" : \"TestServerResponsSingle\", \"asSingleValue\" : true},";
        endpointDescriptionSingle += "\"short\" : {\"name\" : \"short\",\"endpoint\" : \"endpoints/short\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endpointDescriptionSingle += "\"integer\" : {\"name\" : \"integer\",\"endpoint\" : \"endpoints/integer\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endpointDescriptionSingle += "\"long\" : {\"name\" : \"long\",\"endpoint\" : \"endpoints/long\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endpointDescriptionSingle += "\"float\" : {\"name\" : \"float\",\"endpoint\" : \"endpoints/float\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endpointDescriptionSingle += "\"double\" : {\"name\" : \"double\",\"endpoint\" : \"endpoints/double\", "
                + "\"type\" : \"TestServerResponsSingle\"}";
        endpointDescriptionSingle += "}";
    }

    /**
     * Creates the endpointDescriptionSet.
     */
    private void createEndpointDescriptionSet() {
        endpointDescriptionSet = "{";
        endpointDescriptionSet += "\"string\" : {\"endpoint\" : \"endpoints/all\", "
                + "\"type\" : \"TestServerResponseSet\"},";
        endpointDescriptionSet += "\"short\" : {\"endpoint\" : \"endpoints/all\", "
                + "\"type\" : \"TestServerResponseSet\"},";
        endpointDescriptionSet += "\"integer\" : {\"endpoint\" : \"endpoints/all\", "
                + "\"type\" : \"TestServerResponseSet\"},";
        endpointDescriptionSet += "\"long\" : {\"endpoint\" : \"endpoints/all\", "
                + "\"type\" : \"TestServerResponseSet\"},";
        endpointDescriptionSet += "\"float\" : {\"endpoint\" : \"endpoints/all\", "
                + "\"type\" : \"TestServerResponseSet\"},";
        endpointDescriptionSet += "\"double\" : {\"endpoint\" : \"endpoints/all\", "
                + "\"type\" : \"TestServerResponseSet\"}";
        endpointDescriptionSet += "}";
    }

    /**
     * Creates the endpointDescriptionSingleWP.
     */
    private void createEndpointDescriptionSingleWP() {
        endDesSingleWP = "{";
        endDesSingleWP += "\"string\" : {\"endpoint\" : \"endpoints?path=string\","
                + " \"type\" : \"TestServerResponsSingle\", \"asSingleValue\" : true},";
        endDesSingleWP += "\"short\" : {\"endpoint\" : \"endpoints?path=short\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endDesSingleWP += "\"integer\" : {\"endpoint\" : \"endpoints?path=integer\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endDesSingleWP += "\"long\" : {\"endpoint\" : \"endpoints?path=long\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endDesSingleWP += "\"float\" : {\"endpoint\" : \"endpoints?path=float\", "
                + "\"type\" : \"TestServerResponsSingle\"},";
        endDesSingleWP += "\"double\" : {\"endpoint\" : \"endpoints?path=double\", "
                + "\"type\" : \"TestServerResponsSingle\"}";
        endDesSingleWP += "}";
    }

    /**
     * Creates the endpointDescriptionSetWP.
     */
    private void createEndpointDescriptionSetWP() {
        endpointDescriptionSetWP = "{";
        endpointDescriptionSetWP += "\"AllWP\" : {\"endpoint\" : "
                + "\"endpoints/set?paths=string,short,integer,long,float,double\", "
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
