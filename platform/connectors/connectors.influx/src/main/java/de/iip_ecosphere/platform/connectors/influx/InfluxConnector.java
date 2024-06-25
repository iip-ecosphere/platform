/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors.influx;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.ConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.MachineConnectorSupportedQueries;
import de.iip_ecosphere.platform.connectors.AbstractThreadedConnector;
import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.events.StringTriggerQuery;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;

/**
 * Implements the generic INFLUX connector.
 *
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger
 */
@MachineConnector(hasModel = false, supportsModelStructs = false, supportsEvents = false, specificSettings = 
    {"ORG", "BUCKET", "MEASUREMENT", "TAGS", "BATCH"})
@MachineConnectorSupportedQueries({StringTriggerQuery.class})
public class InfluxConnector<CO, CI> extends AbstractThreadedConnector<Object, Object, CO, CI, InfluxModelAccess> {

    public static final String NAME = "INFLUX";
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxConnector.class);
    private static final Object DUMMY = new Object();

    private InfluxDBClient client;
    private ConnectorParameter params;
    private String org;
    private String bucket;
    private String measurement;
    private Set<String> tags = new HashSet<>();
    private int batchSize = 1;

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
            return InfluxConnector.class;
        }

    }

    /**
     * Creates an instance and installs the protocol adapter.
     * 
     * @param adapter the protocol adapter
     */
    @SafeVarargs
    public InfluxConnector(ProtocolAdapter<Object, Object, CO, CI>... adapter) {
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
    public InfluxConnector(AdapterSelector<Object, Object, CO, CI> selector,
            ProtocolAdapter<Object, Object, CO, CI>... adapter) {
        super(selector, adapter);
        setModelAccessSupplier(() -> new InfluxModelAccess(this));
    }

    @Override
    public String getName() {
        return NAME;
    }

    // checkstyle: stop exception type check

    @Override
    protected void connectImpl(ConnectorParameter params) throws IOException {
        if (client == null) {
            this.params = params;
            String url = params.getSchema() + "://" + params.getHost();
            if (params.getPort() >= 0) {
                url += ":" + params.getPort();
            }
            if (params.getEndpointPath() != null) {
                url += "/" + params.getEndpointPath();
            }
            IdentityToken tok = params.getIdentityToken(ConnectorParameter.ANY_ENDPOINT);
            char[] dbToken = null;
            if (null != tok) {
                if (tok.getType() == TokenType.ISSUED || tok.getType() == TokenType.USERNAME) {
                    dbToken = tok.getTokenDataAsCharArray();
                }
            }
            org = params.getSpecificStringSetting("ORG");
            bucket = params.getSpecificStringSetting("BUCKET");
            measurement = params.getSpecificStringSetting("MEASUREMENT");
            String tmp = params.getSpecificStringSetting("TAGS");
            if (null != tmp) {
                Arrays.stream(tmp.split(",")).forEach(t -> tags.add(t));
            }
            tmp = params.getSpecificStringSetting("BATCH");
            if (null != tmp) {
                try {
                    batchSize = Integer.parseInt(tmp.toString());
                } catch (NumberFormatException e) {
                    LOGGER.info("Specific setting BATCH ignored: {}", e.getMessage());
                }
            }
            
            LOGGER.info("INFLUX connecting to " + url);
            try {
                client = InfluxDBClientFactory.create(url, dbToken, org, bucket);
            } catch (Exception e) {
                LOGGER.info("INFLUX connection failed: {}", e.getMessage());
                LOGGER.debug("INFLUX connection failed", e);
            }
        }
    }

    // checkstyle: resume exception type check

    @Override
    protected void installPollTask() { // do not poll
    }
    
    @Override
    protected void disconnectImpl() throws IOException {
        if (null != client) {
            client.close();
            client = null;
        }
    }

    @Override
    protected void error(String message, Throwable th) {
        LOGGER.error(message, th);
    }

    @Override
    protected Object read() throws IOException {
        return DUMMY; // regardless, if we are asked, we do not report the changes; typeTranslator will compose the data
    }

    @Override
    public void trigger(ConnectorTriggerQuery query) {
        if (query instanceof StringTriggerQuery) {
            final StringTriggerQuery q = (StringTriggerQuery) query;
            final Runnable run = () -> {
                QueryApi queryApi = client.getQueryApi();
                List<FluxTable> tables = queryApi.query(q.getQuery());
                InfluxModelAccess acc = getModelAccess();
                if (null != acc) {
                    long lastTime = -1;
                    for (FluxTable fluxTable : tables) {
                        List<FluxRecord> records = fluxTable.getRecords();
                        for (FluxRecord fluxRecord : records) {
                            try {
                                acc.setReadData(fluxRecord);
                                received(DEFAULT_CHANNEL, DUMMY, true);
                                acc.readCompleted();
                            } catch (IOException e) {
                                LoggerFactory.getLogger(getClass()).error("Cannot trigger connector {}: {}", getName(), 
                                    e.getMessage());
                            }
                            long thisTime = fluxRecord.getTime().toEpochMilli();
                            int delay = 0;
                            if (q.delay() > 0) {
                                delay = q.delay();
                            } else {
                                if (lastTime > 0) {
                                    delay = (int) (thisTime - lastTime);
                                }
                            }
                            TimeUtils.sleep(Math.max(1, delay));
                        }
                    }
                }
            };
            new Thread(run).start();
        }
    }

    // data must be measurement with columns set
    @Override
    protected void writeImpl(Object data) throws IOException {
        // protocoladapter is called before, data is in InfluxModelAccess.point
        InfluxModelAccess acc = getModelAccess();
        if (null != acc) {
            acc.writeCompleted();
        }
    }

    @Override
    public String supportedEncryption() {
        return null;
    }

    @Override
    public String enabledEncryption() {
        return null;
    }
    
    /**
     * Returns the INFLUX client instance.
     * 
     * @return the client instance
     */
    InfluxDBClient getClient() {
        return client;
    }
    
    /**
     * Returns the connector parameters.
     * 
     * @return the connector parameters
     */
    ConnectorParameter getParameter() {
        return params;
    }
    
    /**
     * Returns the source/target bucket.
     * 
     * @return the source/target bucket
     */
    String getBucket() {
        return bucket;
    }

    /**
     * Returns the data type/measurement name.
     * 
     * @return the data type/measurement name
     */
    String getMeasurement() {
        return measurement;
    }

    /**
     * Returns the tag names.
     * 
     * @return the tag names
     */
    Set<String> getTags() {
        return tags;
    }

    /**
     * Returns the configured batch size.
     * 
     * @return the batch size, batching is enabled if the value is greater than 1
     */
    int getBatchSize() {
        return batchSize;
    }

}
