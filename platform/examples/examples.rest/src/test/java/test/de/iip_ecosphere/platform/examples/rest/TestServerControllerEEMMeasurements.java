package test.de.iip_ecosphere.platform.examples.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSingle;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSet;
import de.iip_ecosphere.platform.examples.rest.TestServerResponseMeasurementSetItem;


@RestController
@RequestMapping("TestServerEEM/api/measurements")
public class TestServerControllerEEMMeasurements {

    private TestServerValueMeasurement f;
    private TestServerValueMeasurement u1;
    private TestServerValueMeasurement u2;
    private TestServerValueMeasurement u3;
    private TestServerValueMeasurement u12;
    private TestServerValueMeasurement u23;
    private TestServerValueMeasurement u31;
    private TestServerValueMeasurement i1;
    private TestServerValueMeasurement i2;
    private TestServerValueMeasurement i3;
    
    /**
     * Constructor.
     */
    public TestServerControllerEEMMeasurements() {
        f = new TestServerValueMeasurement();
        f.setId("f");
        f.setName("f");
        f.setValue(50.000);
        f.setUnit("Hz");
        f.setDescription("Frequency");
        
        u1 = new TestServerValueMeasurement();
        u1.setId("u1");
        u1.setName("U1");
        u1.setValue(229.845);
        u1.setUnit("V");
        u1.setDescription("Effective value voltage U1");
    
        u2 = new TestServerValueMeasurement();
        u2.setId("u2");
        u2.setName("U2");
        u2.setValue(229.805);
        u2.setUnit("V");
        u2.setDescription("Effective value voltage U2");
              
        u3 = new TestServerValueMeasurement();
        u3.setId("u3");
        u3.setName("U3");
        u3.setValue(229.853);
        u3.setUnit("V");
        u3.setDescription("Effective value voltage U3");
               
        u12 = new TestServerValueMeasurement();
        u12.setId("u12");
        u12.setName("U12");
        u12.setValue(398.237);
        u12.setUnit("V");
        u12.setDescription("Effective value voltage U12");                        
        
        u23 = new TestServerValueMeasurement();
        u23.setId("u23");
        u23.setName("U23");
        u23.setValue(398.078);
        u23.setUnit("V");
        u23.setDescription("Effective value voltage U23");   
        
        u31 = new TestServerValueMeasurement();
        u31.setId("u31");
        u31.setName("U31");
        u31.setValue(398.279);
        u31.setUnit("V");
        u31.setDescription("Effective value voltage U31");                
        
        i1 = new TestServerValueMeasurement();
        i1.setId("i1");
        i1.setName("I1");
        i1.setValue(2.533);
        i1.setUnit("A");
        i1.setDescription("Effective value current I1");    
        
        i2 = new TestServerValueMeasurement();
        i2.setId("i2");
        i2.setName("I2");
        i2.setValue(2.468);
        i2.setUnit("A");
        i2.setDescription("Effective value current I2");    
        
        i3 = new TestServerValueMeasurement();
        i3.setId("i3");
        i3.setName("I3");
        i3.setValue(2.476);
        i3.setUnit("A");
        i3.setDescription("Effective value current I3");    
    }

    /**
     * Getter f.
     * 
     * @return f
     */
    @GetMapping("/f")
    public TestServerResponseMeasurementSingle getF() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(f);
        result.setContext("/api/v1/measurements/f");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter U1.
     * 
     * @return U1
     */
    @GetMapping("/u1")
    public TestServerResponseMeasurementSingle getU1() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(u1);
        result.setContext("/api/v1/measurements/u1");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter U2.
     * 
     * @return U2
     */
    @GetMapping("/u2")
    public TestServerResponseMeasurementSingle getU2() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(u2);
        result.setContext("/api/v1/measurements/u2");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter U3.
     * 
     * @return U3
     */
    @GetMapping("/u3")
    public TestServerResponseMeasurementSingle getU3() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(u3);
        result.setContext("/api/v1/measurements/u3");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter U12.
     * 
     * @return U12
     */
    @GetMapping("/u12")
    public TestServerResponseMeasurementSingle getU12() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(u12);
        result.setContext("/api/v1/measurements/u12");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter U23.
     * 
     * @return U23
     */
    @GetMapping("/u23")
    public TestServerResponseMeasurementSingle getU23() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(u23);
        result.setContext("/api/v1/measurements/u23");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter U31.
     * 
     * @return U31
     */
    @GetMapping("/u31")
    public TestServerResponseMeasurementSingle getU31() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(u31);
        result.setContext("/api/v1/measurements/u31");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter I1.
     * 
     * @return I1
     */
    @GetMapping("/i1")
    public TestServerResponseMeasurementSingle getI1() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(i1);
        result.setContext("/api/v1/measurements/i1");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter I2.
     * 
     * @return I2
     */
    @GetMapping("/i2")
    public TestServerResponseMeasurementSingle getI2() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(i2);
        result.setContext("/api/v1/measurements/i2");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }

    /**
     * Getter I3.
     * 
     * @return I3
     */
    @GetMapping("/i3")
    public TestServerResponseMeasurementSingle getI3() {
        TestServerResponseMeasurementSingle result = new TestServerResponseMeasurementSingle(i3);
        result.setContext("/api/v1/measurements/i3");
        result.setTimestamp(getCurrentTimestamp());

        return result;
    }



    /**
     * Returns a SingleValue for a given path.
     * 
     * @param path to SingleValue
     * @return SingleValue at path
     */
    @GetMapping("/single")
    public TestServerResponseMeasurementSingle getPath(@RequestParam("path") String path) {

        TestServerResponseMeasurementSingle value = new TestServerResponseMeasurementSingle();

        if (path.equals("f")) {
            value = getF();
        } else if (path.equals("u1")) {
            value = getU1();
        } else if (path.equals("u2")) {
            value = getU2();
        } else if (path.equals("u3")) {
            value = getU3();
        } else if (path.equals("u12")) {
            value = getU12();
        } else if (path.equals("u23")) {
            value = getU23();
        } else if (path.equals("u31")) {
            value = getU31();
        } else if (path.equals("i1")) {
            value = getI1();
        } else if (path.equals("i2")) {
            value = getI2();
        } else if (path.equals("i3")) {
            value = getI3();
        }

        return value;
    }

    /**
     * Get all values as TestServerResponseSet.
     * 
     * @return TestServerResponseSet containing all values.
     */
    @GetMapping("/all")
    public TestServerResponseMeasurementSet getAll() {

        TestServerResponseMeasurementSet result = new TestServerResponseMeasurementSet();
        result.setContext("/api/v1/measurements/all");
        result.setTimestamp(getCurrentTimestamp());

        TestServerResponseMeasurementSetItem[] items = new TestServerResponseMeasurementSetItem[10];

        items[0] = getFSet();
        items[1] = getU1Set();
        items[2] = getU2Set();
        items[3] = getU3Set();
        items[4] = getU12Set();
        items[5] = getU23Set();
        items[6] = getU31Set();
        items[7] = getI1Set();
        items[8] = getI2Set();
        items[9] = getI3Set();

        result.setItems(items);

        return result;
    }

    /**
     * Returns a TestServerResponseSet for given paths.
     * 
     * @param paths to TestServerResponseSet
     * @return TestServerResponseSet containing TestServerResponseSetItems for paths
     */
    @GetMapping("/set")
    public TestServerResponseMeasurementSet getPaths(@RequestParam("paths") String paths) {

        String[] valuePaths = paths.split(",");
        TestServerResponseMeasurementSetItem[] items = new TestServerResponseMeasurementSetItem[valuePaths.length];
        TestServerResponseMeasurementSet result = new TestServerResponseMeasurementSet();
        result.setContext("/api/v1/measurements/set");
        result.setTimestamp(getCurrentTimestamp());

        for (String path : valuePaths) {

            if (path.equals("f")) {

                items[0] = getFSet();

            } else if (path.equals("u1")) {

                items[1] = getU1Set();

            } else if (path.equals("u2")) {

                items[2] = getU2Set();

            } else if (path.equals("u3")) {

                items[3] = getU3Set();

            } else if (path.equals("u12")) {
  
                items[4] = getU12Set();
                
            } else if (path.equals("u23")) {

                items[5] = getU23Set();

            } else if (path.equals("u31")) {

                items[6] = getU31Set();

            } else if (path.equals("i1")) {

                items[7] = getI1Set();

            } else if (path.equals("i2")) {

                items[8] = getI2Set();

            } else if (path.equals("i3")) {

                items[9] = getI3Set();

            }

        }

        result.setItems(items);

        return result;
    }

    /**
     * Get f as TestServerResponseSetItem.
     * 
     * @return f
     */
    private TestServerResponseMeasurementSetItem getFSet() {

        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(f);
        result.setHref("/api/v1/measurements/f");

        return result;

    }

    /**
     * Get u1 as TestServerResponseSetItem.
     * 
     * @return u1
     */
    private TestServerResponseMeasurementSetItem getU1Set() {

        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(u1);
        result.setHref("/api/v1/measurements/u1");


        return result;
    }

    /**
     * Get u2 as TestServerResponseSetItem.
     * 
     * @return u2
     */
    private TestServerResponseMeasurementSetItem getU2Set() {

        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(u2);
        result.setHref("/api/v1/measurements/u2");

        return result;
    }

    /**
     * Get u3 as TestServerResponseSetItem.
     * 
     * @return u3
     */
    private TestServerResponseMeasurementSetItem getU3Set() {

        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(u3);
        result.setHref("/api/v1/measurements/u3");

        return result;
    }

    /**
     * Get u12 as TestServerResponseSetItem.
     * 
     * @return u12
     */
    private TestServerResponseMeasurementSetItem getU12Set() {

        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(u12);
        result.setHref("/api/v1/measurements/u12");
        
        return result;
    }

    /**
     * Get u23 as TestServerResponseSetItem.
     * 
     * @return u23
     */
    private TestServerResponseMeasurementSetItem getU23Set() {

        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(u23);
        result.setHref("/api/v1/measurements/u23");
        
        return result;
    }

    /**
     * Get u31 as TestServerResponseSetItem.
     * 
     * @return u31
     */
    private TestServerResponseMeasurementSetItem getU31Set() {

        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(u31);
        result.setHref("/api/v1/measurements/u31");
        
        return result;
    }
    
    /**
     * Get i1 as TestServerResponseSetItem.
     * 
     * @return i1
     */
    private TestServerResponseMeasurementSetItem getI1Set() {
        
        TestServerResponseMeasurementSetItem i1result = new TestServerResponseMeasurementSetItem(i1);
        i1result.setHref("/api/v1/measurements/i1");
        
        return i1result;
    }
    
    /**
     * Get i2 as TestServerResponseSetItem.
     * 
     * @return i2
     */
    private TestServerResponseMeasurementSetItem getI2Set() {
        
        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(i2);
        result.setHref("/api/v1/measurements/i2");
        
        return result;
    }
    
    /**
     * Get i3 as TestServerResponseSetItem.
     * 
     * @return i3
     */
    private TestServerResponseMeasurementSetItem getI3Set() {
        
        TestServerResponseMeasurementSetItem result = new TestServerResponseMeasurementSetItem(i3);
        result.setHref("/api/v1/measurements/i3");
        
        return result;
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
