package test.de.iip_ecosphere.platform.connectors.rest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.iip_ecosphere.platform.connectors.rest.RESTServerResponseValue;

@RestController
@RequestMapping("TestServer/api/endpoints")
public class TestServerController {

    /**
     * Returns a SingleValue for a given path.
     * 
     * @param path to SingleValue
     * @return SingleValue at path
     */
    @GetMapping("/single")
    public SingleValue getPath(@RequestParam("path") String path) {

        SingleValue value = new SingleValue();

        if (path.equals("string")) {
            value.setValue(getStringValue());
        } else if (path.equals("short")) {
            value.setValue(getShortValue());
        } else if (path.equals("integer")) {
            value.setValue(getIntegerValue());
        } else if (path.equals("long")) {
            value.setValue(getLongValue());
        } else if (path.equals("float")) {
            value.setValue(getFloatValue());
        } else if (path.equals("double")) {
            value.setValue(getDoubleValue());
        }

        return value;
    }

    

    /**
     * Returns a RESTerverValueSet for given paths.
     * 
     * @param paths to SetValue(s)
     * @return RESTerverValueSet containen SetValues at paths
     */
    @GetMapping("/set")
    public ValueSet getPaths(@RequestParam("paths") String paths) {

        String[] valuePaths = paths.split(",");
        ValueSet valueSet = new ValueSet();
        ArrayList<RESTServerResponseValue> values = new ArrayList<RESTServerResponseValue>();

        for (String path : valuePaths) {

            if (path.equals("string")) {
                values.add(getStringValue());
            } else if (path.equals("short")) {
                values.add(getShortValue());
            } else if (path.equals("integer")) {
                values.add(getIntegerValue());
            } else if (path.equals("long")) {
                values.add(getLongValue());
            } else if (path.equals("float")) {
                values.add(getFloatValue());
            } else if (path.equals("double")) {
                values.add(getDoubleValue());
            }
        }
        
        valueSet.setValues(values);
        return valueSet;
    }


    /**
     * Get all values as RestServerValueSet.
     * 
     * @return RestServerValueSet containing all values.
     */
    @GetMapping("/all")
    public ValueSet getAll() {
        ValueSet valueSet = new ValueSet();

        ArrayList<RESTServerResponseValue> values = new ArrayList<RESTServerResponseValue>();
        values.add(getStringValue());
        values.add(getShortValue());
        values.add(getIntegerValue());
        values.add(getLongValue());
        values.add(getFloatValue());
        values.add(getDoubleValue());
        
        valueSet.setValues(values);
        
        return valueSet;
    }


    /**
     * Get a String as SingleValue.
     * 
     * @return SingleValue -> string
     */
    @GetMapping("/string")
    public SingleValue getString() {
        SingleValue result = new SingleValue();
        ResponseValue value = getStringValue();
        result.setValue(value);
        return result;
    }

    /**
     * Get a Short as SingleValue.
     * 
     * @return SingleValue -> short
     */
    @GetMapping("/short")
    public SingleValue getShort() {
        SingleValue result = new SingleValue();
        ResponseValue value = getShortValue();
        result.setValue(value);
        return result;
    }


    /**
     * Get a Integer as SingleValue.
     * 
     * @return SingleValue -> integer
     */
    @GetMapping("/integer")
    public SingleValue getInteger() {
        SingleValue result = new SingleValue();
        ResponseValue value = getIntegerValue();
        result.setValue(value);
        return result;
    }


    /**
     * Get a Long as SingleValue.
     * 
     * @return SingleValue -> long
     */
    @GetMapping("/long")
    public SingleValue getLong() {
        SingleValue result = new SingleValue();
        ResponseValue value = getLongValue();
        result.setValue(value);
        return result;
    }


    /**
     * Get a Float as SingleValue.
     * 
     * @return SingleValue -> float
     */
    @GetMapping("/float")
    public SingleValue getFloat() {
        SingleValue result = new SingleValue();
        ResponseValue value = getFloatValue();
        result.setValue(value);
        return result;
    }


    /**
     * Get a Double as SingleValue.
     * 
     * @return SingleValue -> souble
     */
    @GetMapping("/double")
    public SingleValue getDouble() {
        SingleValue result = new SingleValue();
        ResponseValue value = getDoubleValue();
        result.setValue(value);
        return result;
    }


    /**
     * Creates and returns a String SingleValue for testing.
     * 
     * @return SingleValue -> string
     */
    private ResponseValue getStringValue() {
        ResponseValue value = new ResponseValue();
        value.setEndpointPath("/api/endpoints/string");
        value.setTimestamp(getCurrentTimestamp());
        //value.setType(Type.String);
        value.setValue("Hello World");
        value.setName("String");
        return value;
    }

    /**
     * Creates and returns a Short SingleValue for testing.
     * 
     * @return SingleValue -> short
     */
    private ResponseValue getShortValue() {
        ResponseValue value = new ResponseValue();
        value.setEndpointPath("/api/endpoints/short");
        value.setTimestamp(getCurrentTimestamp());
        //value.setType(Type.Short);
        value.setValue("1");
        value.setName("Short");
        return value;
    }

    /**
     * Creates and returns a Integer SingleValue for testing.
     * 
     * @return SingleValue -> integer
     */
    private ResponseValue getIntegerValue() {
        ResponseValue value = new ResponseValue();
        value.setEndpointPath("/api/endpoints/integer");
        value.setTimestamp(getCurrentTimestamp());
        //value.setType(Type.Integer);
        value.setValue("1000");
        value.setName("Integer");
        return value;
    }

    /**
     * Creates and returns a Long SingleValue for testing.
     * 
     * @return SingleValue -> long
     */
    private ResponseValue getLongValue() {
        ResponseValue value = new ResponseValue();
        value.setEndpointPath("/api/endpoints/long");
        value.setTimestamp(getCurrentTimestamp());
        //value.setType(Type.Long);
        value.setValue("1000000");
        value.setName("Long");
        return value;
    }

    /**
     * Creates and returns a Float SingleValue for testing.
     * 
     * @return SingleValue -> float
     */
    private ResponseValue getFloatValue() {
        ResponseValue value = new ResponseValue();
        value.setEndpointPath("/api/endpoints/float");
        value.setTimestamp(getCurrentTimestamp());
        //value.setType(Type.Float);
        value.setValue((float) Math.PI);
        value.setName("Float");
        return value;
    }

    /**
     * Creates and returns a Double SingleValue for testing.
     * 
     * @return SingleValue -> double
     */
    private ResponseValue getDoubleValue() {
        ResponseValue value = new ResponseValue();
        value.setEndpointPath("/api/endpoints/double");
        value.setTimestamp(getCurrentTimestamp());
        //value.setType(Type.Double);
        value.setValue(Math.PI);
        value.setName("Double");
        return value;
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
