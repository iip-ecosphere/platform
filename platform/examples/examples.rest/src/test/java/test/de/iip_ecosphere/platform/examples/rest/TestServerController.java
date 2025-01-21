package test.de.iip_ecosphere.platform.examples.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.iip_ecosphere.platform.examples.rest.set.TestServerResponseSet;
import de.iip_ecosphere.platform.examples.rest.set.TestServerResponseSetItem;
import de.iip_ecosphere.platform.examples.rest.single.TestServerResponsSingle;

@RestController
@RequestMapping("TestServer/api/endpoints")
public class TestServerController {

    private Object f = 50.000;
    private Object u1 = 229.845;
    private Object u2 = 229.805;
    private Object u3 = 229.853;
    private Object u12 = 398.237;
    private Object u23 = 398.078;
    private Object u31 = 398.279;
    private Object i1 = 2.533;
    private Object i2 = 2.468;
    private Object i3 = 2.476;

    /**
     * Getter f.
     * 
     * @return f
     */
    @GetMapping("/f")
    public TestServerResponsSingle getF() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/f");
        result.setId("f");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("f");
        result.setValue(f);
        result.setUnit("HZ");
        result.setDescription("Frequency");

        return result;
    }

    /**
     * Getter U1.
     * 
     * @return U1
     */
    @GetMapping("/u1")
    public TestServerResponsSingle getU1() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/u1");
        result.setId("u1");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("U1");
        result.setValue(u1);
        result.setUnit("V");
        result.setDescription("Effective value voltage U1");

        return result;
    }

    /**
     * Getter U2.
     * 
     * @return U2
     */
    @GetMapping("/u2")
    public TestServerResponsSingle getU2() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/u2");
        result.setId("u2");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("U2");
        result.setValue(u2);
        result.setUnit("V");
        result.setDescription("Effective value voltage U2");

        return result;
    }

    /**
     * Getter U3.
     * 
     * @return U3
     */
    @GetMapping("/u3")
    public TestServerResponsSingle getU3() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/u3");
        result.setId("u3");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("U3");
        result.setValue(u3);
        result.setUnit("V");
        result.setDescription("Effective value voltage U3");

        return result;
    }

    /**
     * Getter U12.
     * 
     * @return U12
     */
    @GetMapping("/u12")
    public TestServerResponsSingle getU12() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/u12");
        result.setId("u12");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("U12");
        result.setValue(u12);
        result.setUnit("V");
        result.setDescription("Effective value voltage U12");

        return result;
    }

    /**
     * Getter U23.
     * 
     * @return U23
     */
    @GetMapping("/u23")
    public TestServerResponsSingle getU23() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/u23");
        result.setId("u23");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("U23");
        result.setValue(u23);
        result.setUnit("V");
        result.setDescription("Effective value voltage U23");

        return result;
    }

    /**
     * Getter U31.
     * 
     * @return U31
     */
    @GetMapping("/u31")
    public TestServerResponsSingle getU31() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/u31");
        result.setId("u31");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("U31");
        result.setValue(u31);
        result.setUnit("V");
        result.setDescription("Effective value voltage U31");

        return result;
    }

    /**
     * Getter I1.
     * 
     * @return I1
     */
    @GetMapping("/i1")
    public TestServerResponsSingle getI1() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/i1");
        result.setId("i1");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("I1");
        result.setValue(i1);
        result.setUnit("A");
        result.setDescription("Effective value current I1");

        return result;
    }

    /**
     * Getter I2.
     * 
     * @return I2
     */
    @GetMapping("/i2")
    public TestServerResponsSingle getI2() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/i2");
        result.setId("i2");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("I2");
        result.setValue(i2);
        result.setUnit("A");
        result.setDescription("Effective value current I2");

        return result;
    }

    /**
     * Getter I3.
     * 
     * @return I3
     */
    @GetMapping("/i3")
    public TestServerResponsSingle getI3() {
        TestServerResponsSingle result = new TestServerResponsSingle();
        result.setContext("/api/v1/measurements/i3");
        result.setId("i3");
        result.setTimestamp(getCurrentTimestamp());
        result.setName("I3");
        result.setValue(i3);
        result.setUnit("A");
        result.setDescription("Effective value current I3");

        return result;
    }

    /**
     * Returns a SingleValue for a given path.
     * 
     * @param path to SingleValue
     * @return SingleValue at path
     */
    @GetMapping("/single")
    public TestServerResponsSingle getPath(@RequestParam("path") String path) {

        TestServerResponsSingle value = new TestServerResponsSingle();

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
    public TestServerResponseSet getAll() {

        TestServerResponseSet result = new TestServerResponseSet();
        result.setContext("/api/v1/measurements/all");
        result.setTimestamp(getCurrentTimestamp());

        TestServerResponseSetItem[] items = new TestServerResponseSetItem[10];

        TestServerResponseSetItem f = new TestServerResponseSetItem();
        f.setHref("/api/v1/measurements/f");
        f.setId("f");
        f.setName("f");
        f.setValue(this.f);
        f.setUnit("HZ");
        f.setDescription("Frequency");

        TestServerResponseSetItem u1 = new TestServerResponseSetItem();
        u1.setHref("/api/v1/measurements/u1");
        u1.setId("u1");
        u1.setName("U1");
        u1.setValue(this.u1);
        u1.setUnit("V");
        u1.setDescription("Effective value voltage U1");

        TestServerResponseSetItem u2 = new TestServerResponseSetItem();
        u2.setHref("/api/v1/measurements/u2");
        u2.setId("u2");
        u2.setName("U2");
        u2.setValue(this.u2);
        u2.setUnit("V");
        u2.setDescription("Effective value voltage U2");

        TestServerResponseSetItem u3 = new TestServerResponseSetItem();
        u3.setHref("/api/v1/measurements/u3");
        u3.setId("u3");
        u3.setName("U3");
        u3.setValue(this.u3);
        u3.setUnit("V");
        u3.setDescription("Effective value voltage U3");

        TestServerResponseSetItem u12 = new TestServerResponseSetItem();
        u12.setHref("/api/v1/measurements/u12");
        u12.setId("u12");
        u12.setName("U12");
        u12.setValue(this.u12);
        u12.setUnit("V");
        u12.setDescription("Effective value voltage U12");

        TestServerResponseSetItem u23 = new TestServerResponseSetItem();
        u23.setHref("/api/v1/measurements/u23");
        u23.setId("u23");
        u23.setName("U23");
        u23.setValue(this.u23);
        u23.setUnit("V");
        u23.setDescription("Effective value voltage U23");

        TestServerResponseSetItem u31 = new TestServerResponseSetItem();
        u31.setHref("/api/v1/measurements/u31");
        u31.setId("u31");
        u31.setName("U31");
        u31.setValue(this.u31);
        u31.setUnit("V");
        u31.setDescription("Effective value voltage U31");

        TestServerResponseSetItem i1 = new TestServerResponseSetItem();
        i1.setHref("/api/v1/measurements/i1");
        i1.setId("i1");
        i1.setName("I1");
        i1.setValue(this.i1);
        i1.setUnit("A");
        i1.setDescription("Effective value voltage I1");

        TestServerResponseSetItem i2 = new TestServerResponseSetItem();
        i2.setHref("/api/v1/measurements/i2");
        i2.setId("i2");
        i2.setName("I2");
        i2.setValue(this.i2);
        i2.setUnit("A");
        i2.setDescription("Effective value voltage I2");

        TestServerResponseSetItem i3 = new TestServerResponseSetItem();
        i3.setHref("/api/v1/measurements/i3");
        i3.setId("i3");
        i3.setName("I3");
        i3.setValue(this.i3);
        i3.setUnit("A");
        i3.setDescription("Effective value voltage I3");

        items[0] = f;
        items[1] = u1;
        items[2] = u2;
        items[3] = u3;
        items[4] = u12;
        items[5] = u23;
        items[6] = u31;
        items[7] = i1;
        items[8] = i2;
        items[9] = i3;

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
    public TestServerResponseSet getPaths(@RequestParam("paths") String paths) {

        String[] valuePaths = paths.split(",");
        TestServerResponseSetItem[] items = new TestServerResponseSetItem[valuePaths.length];
        TestServerResponseSet result = new TestServerResponseSet();
        result.setContext("/api/v1/measurements/set");
        result.setTimestamp(getCurrentTimestamp());

        for (String path : valuePaths) {

            if (path.equals("f")) {

                TestServerResponseSetItem f = new TestServerResponseSetItem();
                f.setHref("/api/v1/measurements/f");
                f.setId("f");
                f.setName("f");
                f.setValue(this.f);
                f.setUnit("HZ");
                f.setDescription("Frequency");
                
                items[0] = f;
                
            } else if (path.equals("u1")) {

                TestServerResponseSetItem u1 = new TestServerResponseSetItem();
                u1.setHref("/api/v1/measurements/u1");
                u1.setId("u1");
                u1.setName("U1");
                u1.setValue(this.u1);
                u1.setUnit("V");
                u1.setDescription("Effective value voltage U1");

                items[1] = u1;

            } else if (path.equals("u2")) {

                TestServerResponseSetItem u2 = new TestServerResponseSetItem();
                u2.setHref("/api/v1/measurements/u2");
                u2.setId("u2");
                u2.setName("U2");
                u2.setValue(this.u2);
                u2.setUnit("V");
                u2.setDescription("Effective value voltage U2");

                items[2] = u2;

            } else if (path.equals("u3")) {

                TestServerResponseSetItem u3 = new TestServerResponseSetItem();
                u3.setHref("/api/v1/measurements/u3");
                u3.setId("u3");
                u3.setName("U3");
                u3.setValue(this.u3);
                u3.setUnit("V");
                u3.setDescription("Effective value voltage U3");

                items[3] = u3;
            } else if (path.equals("u12")) {
             
                TestServerResponseSetItem u12 = new TestServerResponseSetItem();
                u12.setHref("/api/v1/measurements/u12");
                u12.setId("u12");
                u12.setName("U12");
                u12.setValue(this.u12);
                u12.setUnit("V");
                u12.setDescription("Effective value voltage U12");
                items[4] = u12;
            } else if (path.equals("u23")) {
            
                TestServerResponseSetItem u23 = new TestServerResponseSetItem();
                u23.setHref("/api/v1/measurements/u23");
                u23.setId("u23");
                u23.setName("U23");
                u23.setValue(this.u23);
                u23.setUnit("V");
                u23.setDescription("Effective value voltage U23");
                items[5] = u23;
                
            } else if (path.equals("u31")) {
               
                TestServerResponseSetItem u31 = new TestServerResponseSetItem();
                u31.setHref("/api/v1/measurements/u31");
                u31.setId("u31");
                u31.setName("U31");
                u31.setValue(this.u31);
                u31.setUnit("V");
                u31.setDescription("Effective value voltage U31");
                items[6] = u31;
                
            } else if (path.equals("i1")) {
              
                TestServerResponseSetItem i1 = new TestServerResponseSetItem();
                i1.setHref("/api/v1/measurements/i1");
                i1.setId("i1");
                i1.setName("I1");
                i1.setValue(this.i1);
                i1.setUnit("A");
                i1.setDescription("Effective value voltage I1");
                items[7] = i1;
                
            } else if (path.equals("i2")) {
                
                TestServerResponseSetItem i2 = new TestServerResponseSetItem();
                i2.setHref("/api/v1/measurements/i2");
                i2.setId("i2");
                i2.setName("I2");
                i2.setValue(this.i2);
                i2.setUnit("A");
                i2.setDescription("Effective value voltage I2");
                items[8] = i2;
                
            } else if (path.equals("i3")) {
              
                TestServerResponseSetItem i3 = new TestServerResponseSetItem();
                i3.setHref("/api/v1/measurements/i3");
                i3.setId("i3");
                i3.setName("I3");
                i3.setValue(this.i3);
                i3.setUnit("A");
                i3.setDescription("Effective value voltage I3");
                items[9] = i3;
                
            }

        }

        result.setItems(items);

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
