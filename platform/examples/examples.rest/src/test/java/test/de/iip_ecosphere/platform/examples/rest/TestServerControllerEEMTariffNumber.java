package test.de.iip_ecosphere.platform.examples.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.iip_ecosphere.platform.examples.rest.TestServerResponsTariffNumber;

@RestController
@RequestMapping("TestServerEEM/api/tariff-number")
public class TestServerControllerEEMTariffNumber {


    private TestServerResponsTariffNumber tn;
    
    /**
     * Constructor.
     */
    public TestServerControllerEEMTariffNumber() {
        tn = new TestServerResponsTariffNumber();
        tn.setContext("/api/v1/measurements/tn");
        tn.setId("tn");
        tn.setTimestamp(getCurrentTimestamp());
        tn.setName("TN");
        tn.setValue(3);
        tn.setDescription("Tariff Number");
    }
    
    /**
     * Getter for tn.
     * 
     * @return tn
     */
    @GetMapping("/tn")
    public TestServerResponsTariffNumber getTn() {

        return tn;
    }
    
    /**
     * Getter for tn with path Parameter.
     * 
     * @param path to get
     * @return value for path
     */
    @GetMapping()
    public TestServerResponsTariffNumber getPath(@RequestParam("path") String path) {
        
        TestServerResponsTariffNumber result = null;
        
        if (path.equals("tn")) {

            result = tn;
        }
        
        return result;
             
    }

    /**
     * Put Endpoint to set a new Value for stringValue.
     * 
     * @param value to set for stringValue
     * @return ResponseEntity<String> containing
     */
    @PutMapping("/tn")
    public ResponseEntity<String> updateTn(@RequestParam("value") int value) {

        tn.set("value", value);
        ResponseEntity<String> mes = ResponseEntity.ok("tn wurde aktualisiert: neuer Wert ist " + tn.getValue());
        System.out.println(mes);
        return mes;
    }
    
    /**
     * Creates and returns a currentTimestamp.
     * 
     * @return currentTimestamp in format yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    private String getCurrentTimestamp() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String formattedTimestamp = format.format(date);
        return formattedTimestamp;
    }
}
