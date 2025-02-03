package test.de.iip_ecosphere.platform.connectors.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("TestServer/api/endpoints")
public class TestServerController {

    private String stringValue = "Hello World!";

    /**
     * Returns a TestServerResponsSingle for a given path.
     * 
     * @param path to TestServerResponsSingle
     * @return TestServerResponsSingle at path
     */
    @GetMapping()
    public TestServerResponsSingle getPath(@RequestParam("path") String path) {

        TestServerResponsSingle result = new TestServerResponsSingle();

        if (path.equals("string")) {
            result = getString();
        } else if (path.equals("short")) {
            result = getShort();
        } else if (path.equals("integer")) {
            result = getInteger();
        } else if (path.equals("long")) {
            result = getLong();
        } else if (path.equals("float")) {
            result = getFloat();
        } else if (path.equals("double")) {
            result = getDouble();
        }

        return result;
    }

    /**
     * Returns a TestServerResponseSet for given paths.
     * 
     * @param path to TestServerResponseSet
     * @return TestServerResponseSet at path
     */
    @GetMapping("/set")
    public TestServerResponseSet getPaths(@RequestParam("paths") String paths) {
        String[] valuePaths = paths.split(",");
        TestServerResponseSetItem[] items = new TestServerResponseSetItem[valuePaths.length];
        TestServerResponseSet result = new TestServerResponseSet();
        result.setContext("/api/v1/measurements/set");
        result.setTimestamp(getCurrentTimestamp());

        for (String path : valuePaths) {
            if (path.equals("string")) {

                TestServerResponseSetItem stringValue = new TestServerResponseSetItem();
                stringValue.setHref("/api/v1/measurements/string");
                stringValue.setId("string");
                stringValue.setName("String");
                stringValue.setValue(this.stringValue);
                stringValue.setUnit("");
                stringValue.setDescription("");
                items[0] = stringValue;

            } else if (path.equals("short")) {

                TestServerResponseSetItem shortValue = new TestServerResponseSetItem();
                shortValue.setHref("/api/v1/measurements/short");
                shortValue.setId("short");
                shortValue.setName("Short");
                shortValue.setValue(1);
                shortValue.setUnit("");
                shortValue.setDescription("");
                items[1] = shortValue;

            } else if (path.equals("integer")) {

                TestServerResponseSetItem integerValue = new TestServerResponseSetItem();
                integerValue.setHref("/api/v1/measurements/integer");
                integerValue.setId("integer");
                integerValue.setName("Integer");
                integerValue.setValue(100);
                integerValue.setUnit("");
                integerValue.setDescription("");
                items[2] = integerValue;

            } else if (path.equals("long")) {

                TestServerResponseSetItem longValue = new TestServerResponseSetItem();
                longValue.setHref("/api/v1/measurements/long");
                longValue.setId("long");
                longValue.setName("Long");
                longValue.setValue(10000);
                longValue.setUnit("");
                longValue.setDescription("");
                items[3] = longValue;

            } else if (path.equals("float")) {

                TestServerResponseSetItem floatValue = new TestServerResponseSetItem();
                floatValue.setHref("/api/v1/measurements/float");
                floatValue.setId("float");
                floatValue.setName("Float");
                floatValue.setValue((float) Math.PI);
                floatValue.setUnit("");
                floatValue.setDescription("");
                items[4] = floatValue;

            } else if (path.equals("double")) {

                TestServerResponseSetItem doubleValue = new TestServerResponseSetItem();
                doubleValue.setHref("/api/v1/measurements/double");
                doubleValue.setId("double");
                doubleValue.setName("Double");
                doubleValue.setValue((double) Math.PI);
                doubleValue.setUnit("");
                doubleValue.setDescription("");
                items[5] = doubleValue;
            }
        }

        result.setItems(items);

        return result;
    }

    /**
     * Get all values as TestServerResponseSet .
     * 
     * @return TestServerResponseSet containing all values.
     */
    @GetMapping("/all")
    public TestServerResponseSet getAll() {
        TestServerResponseSet result = new TestServerResponseSet();
        result.setContext("/api/v1/measurements/all");
        result.setTimestamp(getCurrentTimestamp());

        TestServerResponseSetItem[] items = new TestServerResponseSetItem[6];

        TestServerResponseSetItem stringValue = new TestServerResponseSetItem();
        stringValue.setHref("/api/v1/measurements/string");
        stringValue.setId("string");
        stringValue.setName("String");
        stringValue.setValue(this.stringValue);
        stringValue.setUnit("");
        stringValue.setDescription("");

        TestServerResponseSetItem shortValue = new TestServerResponseSetItem();
        shortValue.setHref("/api/v1/measurements/short");
        shortValue.setId("short");
        shortValue.setName("Short");
        shortValue.setValue(1);
        shortValue.setUnit("");
        shortValue.setDescription("");

        TestServerResponseSetItem integerValue = new TestServerResponseSetItem();
        integerValue.setHref("/api/v1/measurements/integer");
        integerValue.setId("integer");
        integerValue.setName("Integer");
        integerValue.setValue(100);
        integerValue.setUnit("");
        integerValue.setDescription("");

        TestServerResponseSetItem longValue = new TestServerResponseSetItem();
        longValue.setHref("/api/v1/measurements/long");
        longValue.setId("long");
        longValue.setName("Long");
        longValue.setValue(10000);
        longValue.setUnit("");
        longValue.setDescription("");

        TestServerResponseSetItem floatValue = new TestServerResponseSetItem();
        floatValue.setHref("/api/v1/measurements/float");
        floatValue.setId("float");
        floatValue.setName("Float");
        floatValue.setValue((float) Math.PI);
        floatValue.setUnit("");
        floatValue.setDescription("");

        TestServerResponseSetItem doubleValue = new TestServerResponseSetItem();
        doubleValue.setHref("/api/v1/measurements/double");
        doubleValue.setId("double");
        doubleValue.setName("Double");
        doubleValue.setValue((double) Math.PI);
        doubleValue.setUnit("");
        doubleValue.setDescription("");

        items[0] = stringValue;
        items[1] = shortValue;
        items[2] = integerValue;
        items[3] = longValue;
        items[4] = floatValue;
        items[5] = doubleValue;

        result.setItems(items);

        return result;
    }
    

    /**
     * Get a String as TestServerResponsSingle.
     * 
     * @return TestServerResponsSingle -> string
     */
    @GetMapping("/string")
    public TestServerResponsSingle getString() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/string");
        result.setId("string");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("String");
        result.setValue(stringValue);
        result.setUnit("");
        result.setDescription("");

        return result;
    }

    /**
     * Get a Short as TestServerResponsSingle.
     * 
     * @return TestServerResponsSingle -> short
     */
    @GetMapping("/short")
    public TestServerResponsSingle getShort() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/short");
        result.setId("short");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("Short");
        result.setValue((short) 1);
        result.setUnit("");
        result.setDescription("");

        return result;
    }

    /**
     * Get a Integer as TestServerResponsSingle.
     * 
     * @return TestServerResponsSingle -> integer
     */
    @GetMapping("/integer")
    public TestServerResponsSingle getInteger() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/integer");
        result.setId("integer");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("Integer");
        result.setValue(100);
        result.setUnit("");
        result.setDescription("");

        return result;
    }

    /**
     * Get a Long as TestServerResponsSingle.
     * 
     * @return TestServerResponsSingle -> long
     */
    @GetMapping("/long")
    public TestServerResponsSingle getLong() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/long");
        result.setId("long");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("Long");
        result.setValue(10000);
        result.setUnit("");
        result.setDescription("");

        return result;
    }

    /**
     * Get a Float as TestServerResponsSingle.
     * 
     * @return TestServerResponsSingle -> float
     */
    @GetMapping("/float")
    public TestServerResponsSingle getFloat() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/float");
        result.setId("float");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("Float");
        result.setValue((float) Math.PI);
        result.setUnit("");
        result.setDescription("");

        return result;
    }

    /**
     * Get a Double as TestServerResponsSingle.
     * 
     * @return TestServerResponsSingle ->double
     */
    @GetMapping("/double")
    public TestServerResponsSingle getDouble() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/double");
        result.setId("double");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("Double");
        result.setValue((double) Math.PI);
        result.setUnit("");
        result.setDescription("");

        return result;
    }
    
    /**
     * Put Endpoint to set a new Value for stringValue. 
     * 
     * @param value to set for stringValue
     * @return ResponseEntity<String> containing 
     */
    @PutMapping("/string")
    public ResponseEntity<String> updateStringValue(@RequestParam("value") String value) {
        
        stringValue = value;
        
        ResponseEntity<String> mes = ResponseEntity.ok("StringValue wurde aktualisiert: neuer Wert ist " + stringValue);
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
