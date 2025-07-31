package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.json.JsonUtils;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

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
        public Class<?> getConnectorType() {
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
        RESTEndpointMap lowerCaseMap = new RESTEndpointMap();

        for (String key : keys) {

            if (key.equals("SERVER_STRUCTURE")) {

                Object endpoints = params.getSpecificSetting(key);
                map = JsonUtils.fromJson(endpoints, RESTEndpointMap.class);

                for (RESTEndpointMap.Entry<String, RESTEndpoint> entry : map.entrySet()) {
                    lowerCaseMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
        }

        if (map == null) {
            LOGGER.info("RESTConnector:specificSettings(): No Endpoints found -> RESTItem cannot be created");
        } else {
            LOGGER.info("RESTConnector:specificSettings(): RESTEndpointMap created -> " + lowerCaseMap);
            item = new RESTItem(map);
            LOGGER.info("RESTConnector:specificSettings(): RESTItem created -> " + item);
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
            responseType = entry.getValue().getType();
            responseType += "RestType";

            Class<?>[] responseClasses = getResponseClasses();
            Class<?> responseClass = null;

            for (Class<?> response : responseClasses) {

                if (response.getSimpleName().equals(responseType)) {
                    responseClass = response;
                    break;
                }
            }

            RestTemplate restTemplate = null;
            ResponseEntity<?> responseEntity = null;
            HttpEntity<String> entity = null;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate = new RestTemplate();
            entity = new HttpEntity<>(headers);
            responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, responseClass);

            if (responseEntity != null) {

                Object result = (Object) responseEntity.getBody();
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

        if (item.getKeysToWriteSize() != 0) {
            
            HashMap<String, Object> toWrite = item.getKeysToWrite();
            
            for (Entry<String, Object> entry : toWrite.entrySet()) {
                String qName = entry.getKey();
                String path = params.getEndpointPath();
                RESTEndpoint restEndpoint = item.getEndpointMap().get(qName);
                String endpoint = restEndpoint.getSimpleEndpoint();
                ResponseEntity<?> responseEntity = null;
                
                if (restEndpoint.getAsSingleValue()) {

                    Object valueObject = item.getValue(qName);
                    Field valueField = null;
                    Object value = null;

                    try {
                        valueField = valueObject.getClass().getDeclaredField("value");
                        valueField.setAccessible(true);
                        value = valueField.get(valueObject);
                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                            | IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    String uri = path + endpoint + "?value=" + value;

                    RestTemplate restTemplate = new RestTemplate();
                    responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, null, String.class);

                } else {

                    Object value = item.getValue(qName);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<?> requestEntity = new HttpEntity<>(value, headers);

                    String uri = path + endpoint;

                    RestTemplate restTemplate = new RestTemplate();
                    responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
                }

                LOGGER.info("Response: " + responseEntity.getBody());
            }

        }

    }

    /**
     * Returns an Array containig the specific RESTServerResponse classes.
     * 
     * @return Array containig the specific RESTServerResponse classes.
     */
    protected abstract Class<?>[] getResponseClasses();

    /**
     * Implements the model access for REST.
     * 
     * @author Christian Nikolajew
     */
    protected class RESTModelAccess extends AbstractModelAccess {

        private RESTInputConverter inputConverter = new RESTInputConverter();
        private RESTOutputConverter outputConverter = new RESTOutputConverter();
        private Object objectToAccess = null;
        private String keyToWrite = null;

        /**
         * Creates an instance.
         */
        public RESTModelAccess() {
            super(RESTConnector.this);
        }

        /**
         * Creates an instance.
         * 
         * @param objectToAccess for RESTModelAccess
         */
        public RESTModelAccess(Object objectToAccess) {
            this();
            this.objectToAccess = objectToAccess;
        }

        @Override
        public Object get(String qName) throws IOException {
            Object result = new Object();

            if (objectToAccess != null) {
                try {

                    Field dataField = objectToAccess.getClass().getDeclaredField(qName);
                    dataField.setAccessible(true);
                    result = dataField.get(objectToAccess);

                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                        | IllegalAccessException e) {
                    throw new IOException(e);
                }
            } else {
                result = item.getValue(qName);
            }

            LOGGER.info("RESTModelAccess:get(" + qName + ") -> result: " + result);
            return result;
        }

        @Override
        public void set(String qName, Object value) throws IOException {

            if (objectToAccess != null) {

                Field dataField;
                try {
                    dataField = objectToAccess.getClass().getDeclaredField(qName);
                    dataField.setAccessible(true);
                    dataField.set(objectToAccess, value);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                        | IllegalAccessException e) {
                    throw new IOException(e);
                }

                item.addKeyToWrite(keyToWrite, value);

            } else {
                RESTEndpoint end = item.getEndpointMap().get(qName);

                if (end.getAsSingleValue()) {

                    Object valueObject = item.getValue(qName);
                    Field valueField = null;

                    try {
                        valueField = valueObject.getClass().getDeclaredField("value");
                        valueField.setAccessible(true);
                        valueField.set(valueObject, value);
                    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                            | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    
                    item.setValue(qName, valueObject);

                } else {
                    item.setValue(qName, (Object) value);
                }
                
                item.addKeyToWrite(qName, value);
            }

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

            name = name.toLowerCase();
            Object obj = item.getValue(name);
            RESTModelAccess result = new RESTModelAccess(obj);
            result.keyToWrite = name;

            return result;
        }

        @Override
        public ModelAccess stepOut() {

            return new RESTModelAccess();
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
