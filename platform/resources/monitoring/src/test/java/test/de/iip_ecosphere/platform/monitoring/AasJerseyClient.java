package test.de.iip_ecosphere.platform.monitoring;

import static de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstants.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.CounterRepresentation;
import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.GaugeRepresentation;
import de.iip_ecosphere.platform.services.environment.metricsProvider.meterRepresentation.TimerRepresentation;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Timer;

/**
 * Class to test the AAS's functionality.<br>
 * This class serves as the main test for the prototype and requires the AAS
 * server to be running to work correctly. If all the requirements are met, this
 * class will proceed to retrieve the different properties and test the REST
 * methods to modify custom meters and configuration.
 * 
 * @author Miguel Gomez
 */
public class AasJerseyClient {

    private Submodel sm;
    
    /**
     * Executes the Client.<br>
     * This process, which requires a running AAS and protocol server, retrieves the
     * AAS and will then proceed to collect all the properties stored within. The
     * different meters will then be printed on screen. After printing all meters,
     * this process will modify some custom meters and retrieve them once more to
     * assert that the update worked. Finally, the base unit configurations for the
     * disk and the memory will be modified.
     * 
     * @param args command line arguments, never used
     * @throws IOException if retrieving the AAS fails
     * @throws ExecutionException if accessing the AAS fails
     */
    public static void main(String[] args) throws IOException, ExecutionException {
        AasJerseyClient client = new AasJerseyClient();
        client.initialize();

        // TESTING THE GET FUNCTIONS
        System.out.println("Connection established successfully, retrieving data...");
        System.out.println();
        client.retrieveBaseProperties();
        //client.testLists();
        client.testBasics();
        client.testRest();
        client.testConfiguration();
    }
    
    /**
     * Initializes the client.
     * 
     * @throws IOException if retrieving the AAS fails
     */
    private void initialize() throws IOException {
        // Get endpoint reference
        Endpoint registry = new Endpoint(new ServerAddress(Schema.HTTP, AasServer.REGISTRY_PORT_NO),
                AasPartRegistry.DEFAULT_REGISTRY_ENDPOINT);

        // Retrieve AAS and Submodel
        AasFactory factory = AasFactory.getInstance();
        Aas aas = factory.obtainRegistry(registry).retrieveAas(AasServer.AAS_URN);
        sm = aas.getSubmodel(AasServer.SM_NAME);
    }

    /**
     * Retrieves base properties.
     * 
     * @throws ExecutionException if accessing the AAS fails
     */
    private void retrieveBaseProperties() throws ExecutionException {
        // Base Properties
        System.out.println("Base properties:");
        System.out.println(sm.getProperty(AasServer.PROP_NAME).getValue());
        System.out.println(sm.getProperty(AasServer.PROP_VERSION).getValue());
        System.out.println(sm.getProperty(AasServer.PROP_DESCRIPTION).getValue());
        System.out.println();
    }
    
    /**
     * Tests meter lists.
     * 
     * @throws ExecutionException if accessing the AAS fails
     */
    /*private void testLists() throws ExecutionException {
        JsonArray gaugesList = retrieveArray(sm.getProperty(GAUGE_LIST).getValue());
        JsonArray counterList = retrieveArray(sm.getProperty(COUNTER_LIST).getValue());
        JsonArray timerList = retrieveArray(sm.getProperty(TIMER_LIST).getValue());
        JsonArray taggedList = retrieveArray(sm.getProperty(TAGGED_METER_LIST).getValue());
        JsonArray simpleList = retrieveArray(sm.getProperty(SIMPLE_METER_LIST).getValue());

        System.out.println("Meter lists:");
        System.out.println("\tGauges list:");
        for (int i = 0; i < gaugesList.size(); i++) {
            System.out.println("\t\t" + gaugesList.getString(i));
        }
        System.out.println("\tCounters list:");
        for (int i = 0; i < counterList.size(); i++) {
            System.out.println("\t\t" + counterList.getString(i));
        }
        System.out.println("\tTimer list:");
        for (int i = 0; i < timerList.size(); i++) {
            System.out.println("\t\t" + timerList.getString(i));
        }
        System.out.println("\tTagged meter list:");
        for (int i = 0; i < taggedList.size(); i++) {
            System.out.println("\t\t" + taggedList.getString(i));
        }
        System.out.println("\tSimple meter list:");
        for (int i = 0; i < simpleList.size(); i++) {
            System.out.println("\t\t" + simpleList.getString(i));
        }
        System.out.println();
    }*/

    /**
     * Tests the basic functionalities, i.e., counters, gauges and timers.
     * 
     * @throws ExecutionException if accessing the AAS fails
     */
    private void testBasics() throws ExecutionException {
        // Prepare auxiliary variables
        ArrayList<JsonObject> objects;
        ArrayList<JsonArray> arrays;
        JsonObject meter;
        String[] tags;

        // Counters
        ArrayList<Counter> counters = new ArrayList<Counter>();
        objects = new ArrayList<JsonObject>();
        arrays = new ArrayList<JsonArray>();
        objects.add(retrieveObject(sm.getProperty(AasServer.SUPPLIER_COUNTER_ID).getValue()));
        objects.add(retrieveObject(sm.getProperty(AasServer.CONSUMER_COUNTER_ID).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_GC_MEMORY_ALLOCATED).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_GC_MEMORY_PROMOTED).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_CLASSES_UNLOADED).getValue()));
        arrays.add(retrieveArray(sm.getProperty(LOGBACK_EVENTS).getValue()));
        for (JsonObject jo : objects) {
            counters.add(CounterRepresentation.parseCounter(jo));
        }
        for (JsonArray ja : arrays) {
            for (int i = 0; i < ja.size(); i++) {
                tags = retrieveTags(ja.getJsonObject(i).getJsonArray(TAGS_ATTR));
                meter = ja.getJsonObject(i).getJsonObject(METER_ATTR);
                counters.add(CounterRepresentation.parseCounter(meter, tags));
            }
        }
        System.out.println("Counters:");
        for (Counter c : counters) {
            System.out.println(c.getId() + " >>> " + c.count());
        }
        System.out.println();

        // Gauges
        ArrayList<Gauge> gauges = new ArrayList<Gauge>();
        objects = new ArrayList<JsonObject>();
        arrays = new ArrayList<JsonArray>();
        retrieveGauges(objects, arrays);
        for (JsonObject jo : objects) {
            gauges.add(GaugeRepresentation.parseGauge(jo));
        }
        for (JsonArray ja : arrays) {
            for (int i = 0; i < ja.size(); i++) {
                tags = retrieveTags(ja.getJsonObject(i).getJsonArray(TAGS_ATTR));
                meter = ja.getJsonObject(i).getJsonObject(METER_ATTR);
                gauges.add(GaugeRepresentation.parseGauge(meter, tags));
            }
        }
        System.out.println("Gauges:");
        for (Gauge g : gauges) {
            System.out.println(g.getId() + " >>> " + g.value());
        }
        System.out.println();

        // Timers
        ArrayList<Timer> timers = new ArrayList<Timer>();
        objects = new ArrayList<JsonObject>();
        arrays = new ArrayList<JsonArray>();
        objects.add(retrieveObject(sm.getProperty(AasServer.SUPPLIER_TIMER_ID).getValue()));
        objects.add(retrieveObject(sm.getProperty(AasServer.CONSUMER_TIMER_ID).getValue()));
        arrays.add(retrieveArray(sm.getProperty(JVM_GC_PAUSE).getValue()));
        for (JsonObject jo : objects) {
            timers.add(TimerRepresentation.parseTimer(jo));
        }
        for (JsonArray ja : arrays) {
            for (int i = 0; i < ja.size(); i++) {
                tags = retrieveTags(ja.getJsonObject(i).getJsonArray(TAGS_ATTR));
                meter = ja.getJsonObject(i).getJsonObject(METER_ATTR);
                timers.add(TimerRepresentation.parseTimer(meter, tags));
            }
        }
        System.out.println("Timers:");
        for (Timer t : timers) {
            System.out.println(t.getId() + " >>> CNT > " + t.count() + " || MAX > " + t.max(t.baseTimeUnit())
                    + " || TOT > " + t.totalTime(t.baseTimeUnit()));
        }
        System.out.println();
    }
    
    /**
     * Retrieves gauges.
     * 
     * @param objects the objects
     * @param arrays the arrays
     * @throws ExecutionException if accessing the AAS fails
     */
    private void retrieveGauges(ArrayList<JsonObject> objects, ArrayList<JsonArray> arrays) throws ExecutionException {
        objects.add(retrieveObject(sm.getProperty(AasServer.SUPPLIER_GAUGE_ID).getValue()));
        objects.add(retrieveObject(sm.getProperty(AasServer.CONSUMER_GAUGE_ID).getValue()));
        objects.add(retrieveObject(sm.getProperty(AasServer.CONSUMER_RECV_ID).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_GC_LIVE_DATA_SIZE).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_GC_MAX_DATA_SIZE).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_THREADS_DAEMON).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_THREADS_LIVE).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_THREADS_PEAK).getValue()));
        objects.add(retrieveObject(sm.getProperty(PROCESS_CPU_USAGE).getValue()));
        objects.add(retrieveObject(sm.getProperty(PROCESS_START_TIME).getValue()));
        objects.add(retrieveObject(sm.getProperty(PROCESS_UPTIME).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_CPU_USAGE).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_DISK_FREE).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_DISK_TOTAL).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_DISK_USABLE).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_DISK_USED).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_MEMORY_FREE).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_MEMORY_TOTAL).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_MEMORY_USAGE).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_MEMORY_USED).getValue()));
        objects.add(retrieveObject(sm.getProperty(SYSTEM_CPU_COUNT).getValue()));
        objects.add(retrieveObject(sm.getProperty(JVM_CLASSES_LOADED).getValue()));
        arrays.add(retrieveArray(sm.getProperty(JVM_THREADS_STATES).getValue()));
        arrays.add(retrieveArray(sm.getProperty(JVM_MEMORY_COMMITTED).getValue()));
        arrays.add(retrieveArray(sm.getProperty(JVM_MEMORY_MAX).getValue()));
        arrays.add(retrieveArray(sm.getProperty(JVM_MEMORY_USED).getValue()));
        arrays.add(retrieveArray(sm.getProperty(JVM_BUFFER_MEMORY_USED).getValue()));
        arrays.add(retrieveArray(sm.getProperty(JVM_BUFFER_TOTAL_CAPACITY).getValue()));
        arrays.add(retrieveArray(sm.getProperty(JVM_BUFFER_COUNT).getValue()));
    }

    /**
     * Testing the rest functionality. 
     * 
     * @throws ExecutionException if accessing the AAS fails
     */
    private void testRest() throws ExecutionException {
        // Retrieving the sample objects
        JsonObject gaugeObj = retrieveObject(sm.getProperty(AasServer.REST_GAUGE_ID).getValue());
        JsonObject counterObj = retrieveObject(sm.getProperty(AasServer.REST_COUNTER_ID).getValue());
        JsonObject timerObj = retrieveObject(sm.getProperty(AasServer.REST_TIMER_ID).getValue());
        Gauge gauge = GaugeRepresentation.parseGauge(gaugeObj);
        Counter counter = CounterRepresentation.parseCounter(counterObj);
        Timer timer = TimerRepresentation.parseTimer(timerObj);

        // Printing the sample objects initial values
        System.out.println("Initial state of REST sample objects:");
        printRestSampleObjects(gauge, counter, timer);

        // Changing the values
        ((GaugeRepresentation) gauge).setValue(12.32);
        counter.increment(12.34);
        timer.record(1, TimeUnit.SECONDS);
        timer.record(3, TimeUnit.SECONDS);
        timer.record(2, TimeUnit.SECONDS);

        // Retrieving the updaters
        String gUpdater = ((GaugeRepresentation) gauge).getUpdater().toString();
        String cUpdater = ((CounterRepresentation) counter).getUpdater().toString();
        String tUpdater = ((TimerRepresentation) timer).getUpdater().toString();

        // Creating and sending a bad request
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("name", AasServer.REST_GAUGE_ID);
        job.add("value", "potato");
        String fakeUpdater = job.build().toString();
        try {
            sm.getOperation(UPDATE + AasServer.REST_GAUGE_ID).invoke(fakeUpdater);
        } catch (ExecutionException ee) {
        }

        // Requesting the update
        sm.getOperation(UPDATE + AasServer.REST_GAUGE_ID).invoke(gUpdater);
        sm.getOperation(UPDATE + AasServer.REST_COUNTER_ID).invoke(cUpdater);
        sm.getOperation(UPDATE + AasServer.REST_TIMER_ID).invoke(tUpdater);

        // Retrieving the updated samples
        gaugeObj = retrieveObject(sm.getProperty(AasServer.REST_GAUGE_ID).getValue());
        counterObj = retrieveObject(sm.getProperty(AasServer.REST_COUNTER_ID).getValue());
        timerObj = retrieveObject(sm.getProperty(AasServer.REST_TIMER_ID).getValue());
        gauge = GaugeRepresentation.parseGauge(gaugeObj);
        counter = CounterRepresentation.parseCounter(counterObj);
        timer = TimerRepresentation.parseTimer(timerObj);

        // Printing the sample objects updated values
        System.out.println("Updated state of REST sample objects:");
        printRestSampleObjects(gauge, counter, timer);

        // Requesting the deletion
        sm.getOperation(DELETE + AasServer.REST_GAUGE_ID).invoke();
        sm.getOperation(DELETE + AasServer.REST_COUNTER_ID).invoke();
        sm.getOperation(DELETE + AasServer.REST_TIMER_ID).invoke();

        // Printing the sample objects after deletion (all should be null)
        System.out.println("Updated state of REST sample objects:");
        System.out.println(sm.getProperty(AasServer.REST_GAUGE_ID).getValue());
        System.out.println(sm.getProperty(AasServer.REST_COUNTER_ID).getValue());
        System.out.println(sm.getProperty(AasServer.REST_TIMER_ID).getValue());
        System.out.println();

        // Testing reaction to invalid cases
        try {
            sm.getOperation(UPDATE + AasServer.REST_GAUGE_ID).invoke(fakeUpdater);
        } catch (ExecutionException ee) {
        }
        try {
            sm.getOperation(DELETE + AasServer.REST_GAUGE_ID).invoke();
        } catch (ExecutionException ee) {
        }
    }
    
    /**
     * Tests the configuration options.
     * 
     * @throws ExecutionException if accessing the AAS fails
     */
    private void testConfiguration() throws ExecutionException {
        // Testing the configuration options
        JsonObject memoryObj = retrieveObject(sm.getProperty(SYSTEM_MEMORY_TOTAL).getValue());
        JsonObject diskObj = retrieveObject(sm.getProperty(SYSTEM_DISK_TOTAL).getValue());
        Meter memory = GaugeRepresentation.parseGauge(memoryObj);
        Meter disk = GaugeRepresentation.parseGauge(diskObj);

        // Printing the initial value
        System.out.println("Printing memory and disk base units before update");
        System.out.println("Memory Base Unit >>> " + memory.getId().getBaseUnit());
        System.out.println("Disk Base Unit >>> " + disk.getId().getBaseUnit());

        // Preparing the update info
        JsonObjectBuilder memoryJob = Json.createObjectBuilder().add("unit", "bytes");
        JsonObjectBuilder diskJob = Json.createObjectBuilder().add("unit", "kilobytes");

        // Requesting the update
        sm.getOperation(SET_MEMORY_BASE_UNIT).invoke(memoryJob.build().toString());
        sm.getOperation(SET_DISK_BASE_UNIT).invoke(diskJob.build().toString());

        // Retrieve the configurable items again
        memoryObj = retrieveObject(sm.getProperty(SYSTEM_MEMORY_TOTAL).getValue());
        diskObj = retrieveObject(sm.getProperty(SYSTEM_DISK_TOTAL).getValue());
        memory = GaugeRepresentation.parseGauge(memoryObj);
        disk = GaugeRepresentation.parseGauge(diskObj);

        // Printing the updated value
        System.out.println("Printing memory and disk base units after update");
        System.out.println("Memory Base Unit >>> " + memory.getId().getBaseUnit());
        System.out.println("Disk Base Unit >>> " + disk.getId().getBaseUnit());
    }

    /**
     * Creates a JSON Object from a retrieved String.
     * 
     * @param json String representing the JSON Object
     * @return a JsonObject parsed from the String
     */
    private static JsonObject retrieveObject(Object json) {
        return Json.createReader(new StringReader(String.valueOf(json))).readObject();
    }

    /**
     * Creates a JSON Array from a retrieved String.
     * 
     * @param json String representing the JSON Array
     * @return a JsonArray parsed from the String
     */
    private static JsonArray retrieveArray(Object json) {
        return Json.createReader(new StringReader(String.valueOf(json))).readArray();
    }

    /**
     * Retrieves the Tags stored in a JsonArray as a String array needed for the
     * method.
     * 
     * @param tagsArr JsonArray with the tags
     * @return String array with the retrieved tags
     */
    private static String[] retrieveTags(JsonArray tagsArr) {
        String[] tags = new String[tagsArr.size()];
        for (int i = 0; i < tagsArr.size(); i++) {
            tags[i] = tagsArr.getString(i);
        }

        return tags;
    }

    /**
     * Used to print the values of the retrieved custom meters before and after
     * updating them.
     * 
     * @param gauge gauge to be printed
     * @param counter counter to be printed
     * @param timer timer to be printed
     */
    private static void printRestSampleObjects(Gauge gauge, Counter counter, Timer timer) {
        System.out.println(gauge.getId() + " >>> " + gauge.value());
        System.out.println(counter.getId() + " >>> " + counter.count());
        System.out.println(timer.getId() + " >>> CNT > " + timer.count() + " || MAX > " 
            + timer.max(timer.baseTimeUnit()) + " || TOT > " + timer.totalTime(timer.baseTimeUnit()));
    }

}
