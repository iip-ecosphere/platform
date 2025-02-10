package test.de.iip_ecosphere.platform.examples.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.iip_ecosphere.platform.examples.rest.TestServerResponsTariffNumber;

@RestController
@RequestMapping("TestServerEEM/api/tariff-number")
public class TestServerControllerEEMTariffNumber {


    private TestServerResponsTariffNumber tn1;
    private TestServerResponsTariffNumber tn2;
    
    /**
     * Constructor.
     */
    public TestServerControllerEEMTariffNumber() {
        tn1 = new TestServerResponsTariffNumber();
        tn1.setContext("/api/v1/measurements/tn1");
        tn1.setId("tn1");
        tn1.setTimestamp(getCurrentTimestamp());
        tn1.setName("TN1");
        tn1.setValue(3);
        tn1.setDescription("Tariff Number 1");
        
        tn2 = new TestServerResponsTariffNumber();
        tn2.setContext("");
        tn2.setId("");
        tn2.setTimestamp("");
        tn2.setName("");
        tn2.setValue(null);
        tn2.setDescription("");
    }
    
    /**
     * Getter for tn1.
     * 
     * @return tn1
     */
    @GetMapping("/tn1")
    public TestServerResponsTariffNumber getTn1() {

        return tn1;
    }
    
    /**
     * Getter for tn2.
     * 
     * @return tn2
     */
    @GetMapping("/tn2")
    public TestServerResponsTariffNumber getTn2() {

        return tn2;
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
        
        if (path.equals("tn1")) {

            result = tn1;
        }
        
        return result;
             
    }

    /**
     * Put Endpoint to set a new Value for tn.
     * 
     * @param value to set for stringValue
     * @return ResponseEntity<String> containing a response message
     */
    @PutMapping("/tn1")
    public ResponseEntity<String> updateTn(@RequestParam("value") int value) {

        tn1.set("value", value);
        ResponseEntity<String> mes = ResponseEntity.ok("tn wurde aktualisiert: neuer Wert ist " 
            + tn1.getValueToWrite());
        //System.out.println(mes);
        return mes;
    }
    
    /**
     * Put Endpoint to set new tn.
     * 
     * @return ResponseEntity<String> containing a response message
     */
    @PutMapping("/tn2")
    public ResponseEntity<String> updateTn(@RequestBody TestServerResponsTariffNumber newTn) {
        
        tn2 = newTn;
        ResponseEntity<String> mes = ResponseEntity.ok("tn wurde aktualisiert");
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
