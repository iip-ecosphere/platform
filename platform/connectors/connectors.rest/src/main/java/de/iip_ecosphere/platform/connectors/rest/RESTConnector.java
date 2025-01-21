package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;

import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
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

        RESTServerResponseDeserializer<?, ?> responseDeserializer = new RESTServerResponseDeserializer<>(
                getResponseClass(), getItemClass());
        module.addDeserializer(getResponseClass(), responseDeserializer);

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

            RestTemplate restTemplate = new RestTemplate(Collections.singletonList(jsonConverter));
            ResponseEntity<?> responseEntity = restTemplate.exchange(uri, HttpMethod.GET, null, getResponseClass());

            RESTServerResponse result = (RESTServerResponse) responseEntity.getBody();

            if (result != null) {

                item.setValue(key, result);
                LOGGER.info("RESTConnector.read(): " + uri + " -> " + result);

            } else {
                LOGGER.info("RESTConnector.read(): Failed to read " + key + " from " + uri);
            }
        }

        return item;
    }

    @Override
    protected void writeImpl(Object data) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * Returns the specific ResponseClass derived from RESTServerResponseSingle or
     * RESTServerResponseSet.
     * 
     * @param <T1> the specific ResponseClass
     * @return the specific ResponseClass
     */
    protected abstract <T1 extends RESTServerResponse> Class<T1> getResponseClass();

    /**
     * Returns the specific inner Item Class of RESTServerResponse. If
     * RESTServerResponse don't have a inner Item Class null is returned.
     * 
     * @param <T2> the specific inner ItemClass
     * @return the specific inner ItemClass or null
     */
    protected abstract <T2> Class<T2> getItemClass();

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
            // TODO Auto-generated method stub

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
