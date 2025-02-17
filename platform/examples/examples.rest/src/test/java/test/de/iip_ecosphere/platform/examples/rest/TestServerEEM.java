package test.de.iip_ecosphere.platform.examples.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TestServerEEM {

    private ConfigurableApplicationContext context;
    private String endpointDescriptionMixed;

    private String generatedEndpointDescription;

    /**
     * Creates a TestServer instance.
     */
    public TestServerEEM() {
        createEndpointDescriptionMixed();
        createGeneratedEndpointDescription();
    }

    /**
     * Creates the generatedEndpointDescription.
     */
    private void createGeneratedEndpointDescription() {
        generatedEndpointDescription = "{"
                + "   \"f\": {"
                + "      \"name\": \"f\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/f\","
                + "      \"type\": \"TestServerResponseMeasurementSingle\""
                + "   },"
                + "   \"all\": {"
                + "      \"name\": \"all\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/all\","
                + "      \"type\": \"TestServerResponseMeasurementSetItem\""
                + "   },"
                + "   \"tn1\": {"
                + "      \"name\": \"tn1\","
                + "      \"asSingleValue\": true,"
                + "      \"endpoint\": \"tariff-number/tn1\","
                + "      \"type\": \"TestServerResponseTariffNumber\""
                + "   },"
                + "   \"tn2\": {"
                + "      \"name\": \"tn2\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"tariff-number/tn2\","
                + "      \"type\": \"TestServerResponseTariffNumber\""
                + "   },"
                + "   \"U1\": {"
                + "      \"name\": \"u1\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/u1\","
                + "      \"type\": \"TestServerResponseMeasurementSingle\""
                + "   },"
                + "   \"U2\": {"
                + "      \"name\": \"u2\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/u2\","
                + "      \"type\": \"TestServerResponseMeasurementSingle\""
                + "   },"
                + "   \"U3\": {"
                + "      \"name\": \"u3\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/u3\","
                + "      \"type\": \"TestServerResponseMeasurementSingle\""
                + "   },"
                + "   \"information\": {"
                + "      \"name\": \"information\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"information\","
                + "      \"type\": \"TestServerResponseInformation\""
                + "   }"
                + "}";

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
        endpointDescriptionMixed = "{"
                + "   \"f\": {"
                + "      \"name\": \"f\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/f\","
                + "      \"type\": \"TestServerResponseMeasurementSingle\""
                + "   },"
                + "   \"all\": {"
                + "      \"name\": \"all\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/all\","
                + "      \"type\": \"TestServerResponseMeasurementSetItem\""
                + "   },"
                + "   \"tn1\": {"
                + "      \"name\": \"tn1\","
                + "      \"asSingleValue\": true,"
                + "      \"endpoint\": \"tariff-number/tn1\","
                + "      \"type\": \"TestServerResponseTariffNumber\""
                + "   },"
                + "   \"tn2\": {"
                + "      \"name\": \"tn2\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"tariff-number/tn2\","
                + "      \"type\": \"TestServerResponseTariffNumber\""
                + "   },"
                + "   \"U1\": {"
                + "      \"name\": \"u1\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/u1\","
                + "      \"type\": \"TestServerResponseMeasurementSingle\""
                + "   },"
                + "   \"U2\": {"
                + "      \"name\": \"u2\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/u2\","
                + "      \"type\": \"TestServerResponseMeasurementSingle\""
                + "   },"
                + "   \"U3\": {"
                + "      \"name\": \"u3\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"measurements/u3\","
                + "      \"type\": \"TestServerResponseMeasurementSingle\""
                + "   },"
                + "   \"information\": {"
                + "      \"name\": \"information\","
                + "      \"asSingleValue\": false,"
                + "      \"endpoint\": \"information\","
                + "      \"type\": \"TestServerResponseInformation\""
                + "   }"
                + "}";


    }

    /**
     * Getter for endpointDescriptionMixed.
     * 
     * @return endpointDescriptionMixed
     */
    public String getEndpointDescriptionMixed() {
        return endpointDescriptionMixed;
    }

    /**
     * Getter for generatedEndpointDescription.
     * 
     * @return generatedEndpointDescription
     */
    public String getGeneratedEndpointDescription() {
        return generatedEndpointDescription;
    }
}
