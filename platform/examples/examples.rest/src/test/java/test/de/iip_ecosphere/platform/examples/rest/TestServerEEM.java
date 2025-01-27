package test.de.iip_ecosphere.platform.examples.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TestServerEEM {
    
    private ConfigurableApplicationContext context;

    private String endpointDescriptionSingle;
    private String endpointDescriptionSingleWP;
    private String endpointDescriptionSet;
    private String endpointDescriptionSetWP;


    /**
     * Creates a TestServer instance.
     */
    public TestServerEEM() {
        createEndpointDescriptionSingle();
        createEndpointDescriptionSingleWP();
        createEndpointDescriptionSet();
        createEndpointDescriptionSetWP();
        
    }

    /**
     * Starts the TestServer.
     */
    public void start() {
        context = SpringApplication.run(TestServerEEM.class);
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
        endpointDescriptionSingle += "\"TN\" : {\"endpoint\" : \"tn\", \"responseTypeIndex\" : \"1\"},";
        endpointDescriptionSingle += "\"f\" : {\"endpoint\" : \"f\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"U1\" : {\"endpoint\" : \"u1\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"U2\" : {\"endpoint\" : \"u2\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"U3\" : {\"endpoint\" : \"u3\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"U12\" : {\"endpoint\" : \"u12\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"U23\" : {\"endpoint\" : \"u23\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"U31\" : {\"endpoint\" : \"u31\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"I1\" : {\"endpoint\" : \"i1\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"I2\" : {\"endpoint\" : \"i2\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"I3\" : {\"endpoint\" : \"i3\", \"responseTypeIndex\" : \"0\"}";
        endpointDescriptionSingle += "}";
        
        System.out.println("createEndpointDescriptionSingle() -> " + endpointDescriptionSingle);
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
        endpointDescriptionSingleWP = "{";
        endpointDescriptionSingleWP += "\"f\" : {\"endpoint\" : \"?path=f\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"U1\" : {\"endpoint\" : \"?path=u1\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"U2\" : {\"endpoint\" : \"?path=u2\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"U3\" : {\"endpoint\" : \"?path=u3\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"U12\" : {\"endpoint\" : \"?path=u12\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"U23\" : {\"endpoint\" : \"?path=u23\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"U31\" : {\"endpoint\" : \"?path=u31\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"I1\" : {\"endpoint\" : \"?path=i1\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"I2\" : {\"endpoint\" : \"?path=i2\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"I3\" : {\"endpoint\" : \"?path=i3\", \"responseTypeIndex\" : \"0\"}";
        endpointDescriptionSingleWP += "}";
    }

    /**
     * Creates the endpointDescriptionSetWP.
     */
    private void createEndpointDescriptionSetWP() {
        endpointDescriptionSetWP = "{";
        endpointDescriptionSetWP += "\"AllWP\" : {\"endpoint\" : "
                + "\"?paths=f,u1,u2,u3,u12,u23,u31,i1,i2,i3\", \"responseTypeIndex\" : \"0\"}";
        endpointDescriptionSetWP += "}";
    }

    /**
     * Getter for endpointDescriptionSingle.
     * 
     * @return endpointDescriptionSingle
     */
    public String getEndpointDescriptionSingle() {
    	createEndpointDescriptionSingle();
    	
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
