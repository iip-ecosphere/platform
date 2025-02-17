package de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import de.iip_ecosphere.platform.connectors.AbstractConnector;
import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.model.AbstractModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.identities.IdentityStore;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
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

            if (key.equals("Endpoints")) {

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

        Schema schema = params.getSchema();

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

            if (schema == Schema.HTTPS) {

                entity = getHttpsEntity(headers, restTemplate);
                responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, responseClass);

            } else if (schema == Schema.HTTP) {
                restTemplate = new RestTemplate();
                entity = new HttpEntity<>(headers);
                responseEntity = restTemplate.exchange(uri, HttpMethod.GET, entity, responseClass);
            } else {
                LOGGER.info("unknown schema for REST: use HTTP or HTTPS");
            }

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

    /**
     * Creats SSL encription for HTTPS.
     * 
     * @param headers
     * @param restTemplate
     * @return
     */
    private HttpEntity<String> getHttpsEntity(HttpHeaders headers, RestTemplate restTemplate) {

        KeyStore keystore;
        try {
            keystore = IdentityStore.getInstance().getKeystoreFile(params.getKeystoreKey());
            IdentityToken token = params.getIdentityToken(params.getEndpointPath());
            KeyManagerFactory keyManager = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());


            keyManager.init(keystore, params.getKeystoreKey().toCharArray());
            TrustManagerFactory trustManager = 
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManager.init(keystore);
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);
            CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(sslContext).build();
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            restTemplate = new RestTemplate(factory);

            headers.set("Authorization", token.getTokenDataAsString());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return entity;
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

                Object value = item.getSimpleValueToWrite(qName);

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

            if (end.getAsSingleValue()) {
                item.addSimpleValuesToWrite(qName, value);
            } else {
                item.setValue(qName, (Object) value);
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
