package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;

import java.util.Collections;
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

        for (String key : keys) {

            if (key.equals("Endpoints")) {

                Object endpoints = params.getSpecificSetting(key);
                //endpoints = endpoints.toString().toLowerCase();
                System.out.println(endpoints);
                map = JsonUtils.fromJson(endpoints, RESTEndpointMap.class);

            }

        }

        if (map == null) {
            LOGGER.info("RESTConnector:specificSettings(): No Endpoints found -> RESTItem cannot be created");
        } else {
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
            int responseClassIndex = entry.getValue().getResponseTypeIndex();
            

            Class<? extends RESTServerResponse> responseClass = getResponseClasses()[responseClassIndex];

            RestTemplate restTemplate = new RestTemplate(Collections.singletonList(jsonConverter));
            ResponseEntity<?> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null, 
                    responseClass);



            RESTServerResponse result = (RESTServerResponse) responseEntity.getBody();
            

            if (result != null) {

                item.setValue(key, result);
                LOGGER.info("RESTConnector.read(): " + uri + "[ " + responseClassIndex + " ] -> " + result);

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
                responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, null, 
                        String.class);
                
            } else {
                
                Object value = item.getValue(qName);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<?> requestEntity = new HttpEntity<>(value, headers);
                
                String uri = path + endpoint;
                System.out.println("write to:" + uri);
                
                RestTemplate restTemplate = new RestTemplate();
                responseEntity = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, 
                        String.class);
            }
            
            

//            RestTemplate restTemplate = new RestTemplate();
//
//            String requestBody = "";
//            HttpHeaders headers = new HttpHeaders();
//
//            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
//            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);

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
