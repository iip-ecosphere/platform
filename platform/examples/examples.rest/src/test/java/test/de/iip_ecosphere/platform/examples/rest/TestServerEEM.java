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
    private String endpointDescriptionMixed;


    /**
     * Creates a TestServer instance.
     */
    public TestServerEEM() {
        createEndpointDescriptionSingle();
        createEndpointDescriptionSingleWP();
        createEndpointDescriptionSet();
        createEndpointDescriptionSetWP();
        createEndpointDescriptionMixed();
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
     * Creates the endpointDescriptionMixed.
     */
    private void createEndpointDescriptionMixed() {
        endpointDescriptionMixed = "{";
        endpointDescriptionMixed += "\"tn\" : {\"endpoint\" : \"tariff-number/tn\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionMixed += "\"f\" : {\"endpoint\" : \"measurements/f\", \"responseTypeIndex\" : \"1\"},";
        endpointDescriptionMixed += "\"u1\" : {\"endpoint\" : \"measurements/u1\", \"responseTypeIndex\" : \"1\"},";
        endpointDescriptionMixed += "\"u2\" : {\"endpoint\" : \"measurements/u2\", \"responseTypeIndex\" : \"1\"},";
        endpointDescriptionMixed += "\"u3\" : {\"endpoint\" : \"measurements/u3\", \"responseTypeIndex\" : \"1\"},";
        endpointDescriptionMixed += "\"all\" : {\"endpoint\" : \"measurements/all\", \"responseTypeIndex\" : \"2\"},";
        endpointDescriptionMixed += "\"information\" : {\"endpoint\" : \"information\", \"responseTypeIndex\" : \"3\"}";
        endpointDescriptionMixed += "}";
    }

    /**
     * Creates the endpointDescriptionSingle.
     */
    private void createEndpointDescriptionSingle() {
        endpointDescriptionSingle = "{";
        endpointDescriptionSingle += "\"tn\" : {\"endpoint\" : \"tariff-number/tn\", \"responseTypeIndex\" : \"1\"},";
        endpointDescriptionSingle += "\"f\" : {\"endpoint\" : \"measurements/f\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"u1\" : {\"endpoint\" : \"measurements/u1\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"u2\" : {\"endpoint\" : \"measurements/u2\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"u3\" : {\"endpoint\" : \"measurements/u3\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"u12\" : {\"endpoint\" : \"measurements/u12\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"u23\" : {\"endpoint\" : \"measurements/u23\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"u31\" : {\"endpoint\" : \"measurements/u31\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"i1\" : {\"endpoint\" : \"measurements/i1\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"i2\" : {\"endpoint\" : \"measurements/i2\", \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingle += "\"i3\" : {\"endpoint\" : \"measurements/i3\", \"responseTypeIndex\" : \"0\"}";
        endpointDescriptionSingle += "}";
        
        System.out.println("createEndpointDescriptionSingle() -> " + endpointDescriptionSingle);
    }

    /**
     * Creates the endpointDescriptionSet.
     */
    private void createEndpointDescriptionSet() {
        endpointDescriptionSet = "{";
        endpointDescriptionSet += "\"all\" : {\"endpoint\" : \"all\", \"responseTypeIndex\" : \"0\"}";
        endpointDescriptionSet += "}";
    }

    /**
     * Creates the endpointDescriptionSingleWP.
     */
    private void createEndpointDescriptionSingleWP() {
        endpointDescriptionSingleWP = "{";
        endpointDescriptionSingleWP += "\"tn\" : {\"endpoint\" : \"tariff-number?path=tn\","
                + " \"responseTypeIndex\" : \"1\"},";
        endpointDescriptionSingleWP += "\"f\" : {\"endpoint\" : \"measurements/single?path=f\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"u1\" : {\"endpoint\" : \"measurements/single?path=u1\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"u2\" : {\"endpoint\" : \"measurements/single?path=u2\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"u3\" : {\"endpoint\" : \"measurements/single?path=u3\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"u12\" : {\"endpoint\" : \"measurements/single?path=u12\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"u23\" : {\"endpoint\" : \"measurements/single?path=u23\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"u31\" : {\"endpoint\" : \"measurements/single?path=u31\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"i1\" : {\"endpoint\" : \"measurements/single?path=i1\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"i2\" : {\"endpoint\" : \"measurements/single?path=i2\","
                + " \"responseTypeIndex\" : \"0\"},";
        endpointDescriptionSingleWP += "\"i3\" : {\"endpoint\" : \"measurements/single?path=i3\","
                + " \"responseTypeIndex\" : \"0\"}";
        endpointDescriptionSingleWP += "}";
    }

    /**
     * Creates the endpointDescriptionSetWP.
     */
    private void createEndpointDescriptionSetWP() {
        endpointDescriptionSetWP = "{";
        endpointDescriptionSetWP += "\"allWP\" : {\"endpoint\" : "
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

    /**
     * Getter for endpointDescriptionMixed.
     * 
     * @return endpointDescriptionMixed
     */
    public String getEndpointDescriptionMixed() {
        return endpointDescriptionMixed;
    }
}
