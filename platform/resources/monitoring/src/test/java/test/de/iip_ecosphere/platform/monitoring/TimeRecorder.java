package test.de.iip_ecosphere.platform.monitoring;

import static de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstants.*;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

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
import io.micrometer.core.instrument.Timer;

/**
 * Class to determine the response times.<br>
 * This class connects to the AAS similarly to the AasJerseyClientV2, but
 * instead of printing the values on screen, it records the time required to
 * retrieve and parse the data.
 * 
 * @author Miguel Gomez
 */
public class TimeRecorder {

    // Set to determine the loop size
    private static final int LOOP = 200;

    private Submodel sm;
    
    // Prepare auxiliary variables
    private ArrayList<JsonObject> objects;
    private ArrayList<JsonArray> arrays;
    private ArrayList<Counter> counters;
    private ArrayList<Gauge> gauges;
    private ArrayList<Timer> timers;
    private JsonObject meter;
    private String[] tags;
    private long start;
    private long retrieve;
    private long parse;

    // Accumulation variables
    private long counterRT = 0;
    private long counterPT = 0;
    private long counterTT = 0;
    private long gaugeRT = 0;
    private long gaugePT = 0;
    private long gaugeTT = 0;
    private long timerRT = 0;
    private long timerPT = 0;
    private long timerTT = 0;

    /**
     * Executes the TimeRecorder.<br>
     * This class will connect to the AAS and retrieve all the metrics, recording
     * the time required to both retrieve the data and parse it. The mean time is
     * calculated and printed on screen after the execution for each of the
     * different types of meters. This entire process is repeated {@code LOOP}
     * amount of times before printing the mean, ensuring that the data is more
     * accurate.
     * 
     * @param args command line arguments, never used
     * @throws IOException        if it occurs when retrieving the registry
     * @throws ExecutionException if it occurs when retrieving the properties
     */
    public static void main(String[] args) throws IOException, ExecutionException {
        TimeRecorder rec = new TimeRecorder();
        rec.initialize();
        System.out.println("Connection established successfully, retrieving data...");
        System.out.println();
        rec.measure();
        rec.printResults();
    }
    
    /**
     * Initializes the measurement.
     * 
     * @throws IOException        if it occurs when retrieving the registry
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
     * Initializes and measures.
     * 
     * @throws ExecutionException if it occurs when retrieving the properties
     */
    private void measure() throws ExecutionException {
        for (int k = 0; k < LOOP; k++) {
            counters = new ArrayList<Counter>();
            objects = new ArrayList<JsonObject>();
            arrays = new ArrayList<JsonArray>();

            start = System.nanoTime();
            objects.add(retrieveObject(sm.getProperty(AasServer.SUPPLIER_COUNTER_ID).getValue()));
            objects.add(retrieveObject(sm.getProperty(AasServer.CONSUMER_COUNTER_ID).getValue()));
            objects.add(retrieveObject(sm.getProperty(JVM_GC_MEMORY_ALLOCATED).getValue()));
            objects.add(retrieveObject(sm.getProperty(JVM_GC_MEMORY_PROMOTED).getValue()));
            objects.add(retrieveObject(sm.getProperty(JVM_CLASSES_UNLOADED).getValue()));
            arrays.add(retrieveArray(sm.getProperty(LOGBACK_EVENTS).getValue()));
            retrieve = System.nanoTime();

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
            parse = System.nanoTime();

            counterRT += (retrieve - start) / counters.size();
            counterPT += (parse - retrieve) / counters.size();
            counterTT += (parse - start) / counters.size();

            gauges = new ArrayList<Gauge>();
            objects = new ArrayList<JsonObject>();
            arrays = new ArrayList<JsonArray>();

            start = System.nanoTime();
            add();
            retrieve = System.nanoTime();
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
            parse = System.nanoTime();

            gaugeRT += (retrieve - start) / gauges.size();
            gaugePT += (parse - retrieve) / gauges.size();
            gaugeTT += (parse - start) / gauges.size();

            timers = new ArrayList<Timer>();
            objects = new ArrayList<JsonObject>();
            arrays = new ArrayList<JsonArray>();

            start = System.nanoTime();
            objects.add(retrieveObject(sm.getProperty(AasServer.SUPPLIER_TIMER_ID).getValue()));
            objects.add(retrieveObject(sm.getProperty(AasServer.CONSUMER_TIMER_ID).getValue()));
            arrays.add(retrieveArray(sm.getProperty(JVM_GC_PAUSE).getValue()));
            retrieve = System.nanoTime();
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
            parse = System.nanoTime();

            timerRT += (retrieve - start) / timers.size();
            timerPT += (parse - retrieve) / timers.size();
            timerTT += (parse - start) / timers.size();
        }
    }

    /**
     * Print out the results.
     */
    private void printResults() {
        System.out.println("Printing mean results for " + LOOP + " loops:");
        System.out.println();
        System.out.println("Counter Retrieval Time: " + counterRT / LOOP);
        System.out.println("Counter Parsing Time: " + counterPT / LOOP);
        System.out.println("Counter Total Time: " + counterTT / LOOP);
        System.out.println();
        System.out.println("Gauge Retrieval Time: " + gaugeRT / LOOP);
        System.out.println("Gauge Parsing Time: " + gaugePT / LOOP);
        System.out.println("Gauge Total Time: " + gaugeTT / LOOP);
        System.out.println();
        System.out.println("Timer Retrieval Time: " + timerRT / LOOP);
        System.out.println("Timer Parsing Time: " + timerPT / LOOP);
        System.out.println("Timer Total Time: " + timerTT / LOOP);
    }

    /**
     * Adds AAS property values to objects and arrays.
     * 
     * @throws ExecutionException if some submodel operation fails
     */
    private void add() throws ExecutionException {
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

}
