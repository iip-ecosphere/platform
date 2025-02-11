package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.json.JsonUtils;

/**
 * Implements the generic REST connector.
 * 
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 *             Determines whether SingleValue or SetValue is retrieved with or
 *             without parameters (WP = WithParameter)
 *             specificSettings.RequestType = {Single, SingleWP, Set, SetWP}
 */
@MachineConnector(hasModel = true, supportsModelStructs = false, supportsEvents = false, specificSettings = {
    "Endpoints", "RequestType" })
public abstract class RESTConnector<CO, CI> extends AbstractConnector<RESTItem, Object, CO, CI> {

    public static final String NAME = "REST";
    private static final Logger LOGGER = LoggerFactory.getLogger(RESTConnector.class);

    private MappingJackson2HttpMessageConverter jsonConverter;
    private RESTItem item = null;
    private ConnectorParameter params;

    /**
     * The descriptor of this connector (see META-INF/services).
     */
    public static class Descriptor implements ConnectorDescriptor {

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Class<?> getType() {
            return RESTConnector.class;
        }

    }

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public RESTConnector(ProtocolAdapter<RESTItem, Object, CO, CI>... adapter) {

        this(null, adapter);
    }

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param selector the adapter selector (<b>null</b> leads to a default selector
     *                 for the first adapter)
     * @param adapter  the protocol adapter(s)
     */
    @SafeVarargs
    public RESTConnector(AdapterSelector<RESTItem, Object, CO, CI> selector,
            ProtocolAdapter<RESTItem, Object, CO, CI>... adapter) {
        super(selector, adapter);
        configureModelAccess(new RESTModelAccess());

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();

        for (Class<? extends RESTServerResponse> responseClass : getResponseClasses()) {

            @SuppressWarnings("unchecked")
            Class<RESTServerResponse> type = (Class<RESTServerResponse>) responseClass;

            RESTServerResponseDeserializer<RESTServerResponse, ?> responseDeserializer = 
                    new RESTServerResponseDeserializer<>(type);

            module.addDeserializer(type, responseDeserializer);

        }

        mapper.registerModule(module);
        jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(mapper);

    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {

        this.params = params;
        specificSettings();
    }

    /**
     * Transfers the specific settings form ConnectorParameter to RESTConnector.
     */
    private void specificSettings() {

        Set<String> keys = params.getSpecificSettingKeys();
        RESTEndpointMap map = null;
        RESTEndpointMap mapWithLowerCaseKeys = new RESTEndpointMap();
        RESTEndpointMap finalMap = new RESTEndpointMap();

        ArrayList<String> endpointsInMap = new ArrayList<String>();
        ArrayList<RESTEndpoint> entpointItemIndexes = new ArrayList<RESTEndpoint>();

        for (String key : keys) {

            if (key.equals("Endpoints")) {

                Object endpoints = params.getSpecificSetting(key);
                System.out.println(endpoints);
                map = JsonUtils.fromJson(endpoints, RESTEndpointMap.class);

                removeDuplicateEndpoints(map, endpointsInMap, mapWithLowerCaseKeys, entpointItemIndexes);
                removeNonDuplicateStrings(endpointsInMap);
                resetKeysForDuplicateEndpoints(mapWithLowerCaseKeys, endpointsInMap, finalMap);

            }
        }

        addEndpointIndexes(finalMap, entpointItemIndexes);

        if (map == null) {
            LOGGER.info("RESTConnector:specificSettings(): No Endpoints found -> RESTItem cannot be created");
        } else {
            LOGGER.info("RESTConnector:specificSettings(): RESTEndpointMap created -> " + mapWithLowerCaseKeys);
            item = new RESTItem(finalMap);
            LOGGER.info("RESTConnector:specificSettings(): RESTItem created -> " + item);
        }
    }

    /**
     * Remove duplicate Endpoints and sets keys to lowercase.
     * 
     * @param map                  to remove duplicate Endpoints
     * @param endpointsInMap       empty ArrayList<String>
     * @param mapWithLowerCaseKeys empty RESTEndpointMap
     * @param entpointItemIndexes  empty ArrayList<RESTEndpoint>
     */
    private void removeDuplicateEndpoints(RESTEndpointMap map, ArrayList<String> endpointsInMap,
            RESTEndpointMap mapWithLowerCaseKeys, ArrayList<RESTEndpoint> entpointItemIndexes) {

        for (Map.Entry<String, RESTEndpoint> entry : map.entrySet()) {

            if (!endpointsInMap.contains(entry.getValue().getEndpoint())) {
                mapWithLowerCaseKeys.put(entry.getKey().toLowerCase(), entry.getValue());
            }

            endpointsInMap.add(entry.getValue().getEndpoint());

            System.out.println(entry.getValue().getEndpointIndex());

            if (entry.getValue().getEndpointIndex() != 0) {
                entry.getValue().setName(entry.getKey().toLowerCase());
                entpointItemIndexes.add(entry.getValue());
            }
        }
    }

    /**
     * Removes all non-duplicate Strings.
     * 
     * @param endpointsInMap ArrayList<String> to remove non-duplicate Strings
     */
    private void removeNonDuplicateStrings(ArrayList<String> endpointsInMap) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String item : endpointsInMap) {
            frequencyMap.put(item, frequencyMap.getOrDefault(item, 0) + 1);
        }
        Iterator<String> iterator = endpointsInMap.iterator();
        while (iterator.hasNext()) {
            if (frequencyMap.get(iterator.next()) == 1) {
                iterator.remove();
            }
        }
    }

    /**
     * Resets the keys for duplicate Endpoints.
     * 
     * @param mapWithLowerCaseKeys RESTEndpointMap with lowercase keys to reset keys
     *                             for duplicate Endpoints
     * @param endpointsInMap       ArrayList<String> containing duplicate Endpoints
     *                             as String
     * @param finalMap             RESTEndpointMap with reseted keys for duplicate
     *                             Endpoints
     */
    private void resetKeysForDuplicateEndpoints(RESTEndpointMap mapWithLowerCaseKeys, ArrayList<String> endpointsInMap,
            RESTEndpointMap finalMap) {

        for (Map.Entry<String, RESTEndpoint> entry : mapWithLowerCaseKeys.entrySet()) {

            System.out.println(entry.getKey());

            if (endpointsInMap.contains(entry.getValue().getEndpoint())) {

                String end = entry.getValue().getEndpoint();
                String[] split = end.split("/");
                String newKey = split[split.length - 1];

                System.out.println(entry.getKey() + " -> " + entry.getValue().getEndpointIndex());

                // entry.getValue().addItemIndex(entry.getKey(),
                // entry.getValue().getEndpointIndex());

                finalMap.put(newKey, entry.getValue());
            } else {
                finalMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Adds itemIndexes to finalMap.
     * @param finalMap to add itemIndexes
     * @param entpointItemIndexes ArrayList<RESTEndpoint> containing RESTEndpoints with endpointIndex
     */
    private void addEndpointIndexes(RESTEndpointMap finalMap, ArrayList<RESTEndpoint> entpointItemIndexes) {

        for (Map.Entry<String, RESTEndpoint> entry : finalMap.entrySet()) {

            for (RESTEndpoint end : entpointItemIndexes) {

                if (end.getEndpoint().equals(entry.getValue().getEndpoint())) {
                    entry.getValue().addItemIndex(end.getName(), end.getEndpointIndex());
                }
            }

            System.out.println(entry.getKey() + " -> " + entry.getValue().getEndpoint());
        }
    }

    @Override
    protected void disconnectImpl() throws IOException {
        this.params = null;
        this.item = null;
        LOGGER.info("RESTConnector:disconnectImpl()" + "\n" + "\n");
    }

    @Override
    protected void error(String arg0, Throwable arg1) {
        LOGGER.error(arg0, arg1);

    }

    @Override
    protected RESTItem read() throws IOException {
        String path = params.getEndpointPath();

        RESTEndpointMap endpointMap = item.getEndpointMap();

        for (Entry<String, RESTEndpoint> entry : endpointMap.entrySet()) {

            String key = entry.getKey().toLowerCase();
            String endpoint = entry.getValue().getEndpoint();
            String uri = path + endpoint;

            String responseType = null;

            if (entry.getValue().getSetType() != null) {
                responseType = entry.getValue().getSetType();
            } else {
                responseType = entry.getValue().getType();

                // NUr zu testen muss wieder raus
                if (responseType.equals("TestServerResponseInformationRootItem")) {
                    responseType = "TestServerResponseInformation";
                } else if (responseType.equals("TestServerResponseInformationInfoItem")) {
                    responseType = "TestServerResponseInformation";
                } else if (responseType.equals("TestServerResponseMeasurementSetItem")) {
                    responseType = "TestServerResponseMeasurementSet";
                }
            }

            Class<? extends RESTServerResponse>[] responseClasses = getResponseClasses();
            Class<? extends RESTServerResponse> responseClass = null;

            for (Class<? extends RESTServerResponse> response : responseClasses) {

                // System.out.println(response.getSimpleName());
                // System.out.println(responseType);
                if (response.getSimpleName().equals(responseType)) {
                    // System.out.println("Found response Type");
                    responseClass = response;
                    break;
                }
            }

            RestTemplate restTemplate = new RestTemplate(Collections.singletonList(jsonConverter));
            ResponseEntity<?> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null, responseClass);

            RESTServerResponse result = (RESTServerResponse) responseEntity.getBody();

            if (result != null) {

                item.setValue(key, result);
                LOGGER.info("RESTConnector.read(): " + uri + "[ " + responseType + " ] -> " + result);

            } else {
                LOGGER.info("RESTConnector.read(): Failed to read " + key + " from " + uri);
            }
        }

        return item;
    }

    @Override
    protected void writeImpl(Object data) throws IOException {

        if (data != null) {

            String qName = (String) data;
            String path = params.getEndpointPath();
            RESTEndpoint restEndpoint = item.getEndpointMap().get(qName);
            String endpoint = restEndpoint.getSimpleEndpoint();
            ResponseEntity<?> responseEntity = null;

            if (restEndpoint.getAsSingleValue()) {

                Object value = item.getValue(qName).getValueToWrite();

                String uri = path + endpoint + "?value=" + value;
                System.out.println("write to:" + uri);

                RestTemplate restTemplate = new RestTemplate();
                responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, null, String.class);

            } else {

                Object value = item.getValue(qName);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<?> requestEntity = new HttpEntity<>(value, headers);

                String uri = path + endpoint;
                System.out.println("write to:" + uri);

                RestTemplate restTemplate = new RestTemplate();
                responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
            }

            LOGGER.info("Response: " + responseEntity.getBody());

        }

    }

    /**
     * Returns an Array containig the specific RESTServerResponse classes.
     * 
     * @return Array containig the specific RESTServerResponse classes.
     */
    protected abstract Class<? extends RESTServerResponse>[] getResponseClasses();

    /**
     * Implements the model access for REST.
     * 
     * @author Christian Nikolajew
     */
    protected class RESTModelAccess extends AbstractModelAccess {

        private RESTInputConverter inputConverter = new RESTInputConverter();
        private RESTOutputConverter outputConverter = new RESTOutputConverter();

        /**
         * Creates an instance.
         */
        public RESTModelAccess() {
            super(RESTConnector.this);
        }

        @Override
        public Object get(String qName) throws IOException {
            Object result = new Object();
            result = item.getValue(qName);
            LOGGER.info("RESTModelAccess:get(" + qName + ") -> result: " + result);
            return result;
        }

        @Override
        public void set(String qName, Object value) throws IOException {
            RESTEndpoint end = item.getEndpointMap().get(qName);
            RESTServerResponse res = item.getValue(qName);

            if (end.getAsSingleValue()) {
                res.set("value", value);
                item.setValue(qName, res);
            } else {

                item.setValue(qName, (RESTServerResponse) value);
            }

            writeImpl(qName);

        }

        /**
         * Returns the input converter instance.
         * 
         * @return the input converter
         */
        public RESTInputConverter getInputConverter() {
            return inputConverter;
        }

        /**
         * Returns the output converter instance.
         * 
         * @return the output converter
         */
        public RESTOutputConverter getOutputConverter() {
            return outputConverter;
        }

        @Override
        public ConnectorParameter getConnectorParameter() {
            return params;
        }

        @Override
        public String topInstancesQName() {
            // Not used for REST
            return null;
        }

        @Override
        public String getQSeparator() {
            // Not used for REST
            return null;
        }

        @Override
        public Object call(String qName, Object... args) throws IOException {
            // Not used for REST
            return null;
        }

        @Override
        public <T> T getStruct(String qName, Class<T> type) throws IOException {
            // Not used for REST
            return null;
        }

        @Override
        public void setStruct(String qName, Object value) throws IOException {
            // Not used for REST

        }

        @Override
        public void registerCustomType(Class<?> cls) throws IOException {
            // Not used for REST
        }

        @Override
        public void monitor(int notificationInterval, String... qNames) throws IOException {
            // Not used for REST
        }

        @Override
        public void monitorModelChanges(int notificationInterval) throws IOException {
            // Not used for REST
        }

        @Override
        public ModelAccess stepInto(String name) throws IOException {
            // Not used for REST
            return null;
        }

        @Override
        public ModelAccess stepOut() {
            // Not used for REST
            return null;
        }

    }

    @Override
    public String supportedEncryption() {
        // Not used for REST
        return null;
    }

    @Override
    public String enabledEncryption() {
        // Not used for REST
        return null;
    }

}
