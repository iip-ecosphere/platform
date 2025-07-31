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
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.domain.WriteConsistency;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import de.iip_ecosphere.platform.connectors.AdapterSelector;
import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.MachineConnector;
import de.iip_ecosphere.platform.connectors.MachineConnectorSupportedQueries;
import de.iip_ecosphere.platform.connectors.AbstractPluginConnectorDescriptor;
import de.iip_ecosphere.platform.connectors.AbstractThreadedConnector;
import de.iip_ecosphere.platform.connectors.events.ConnectorTriggerQuery;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery;
import de.iip_ecosphere.platform.connectors.events.StringTriggerQuery;
import de.iip_ecosphere.platform.connectors.events.SimpleTimeseriesQuery.TimeKind;
import de.iip_ecosphere.platform.connectors.types.ProtocolAdapter;
import de.iip_ecosphere.platform.connectors.types.RecordCompletePredicate;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.identities.IdentityToken;
import de.iip_ecosphere.platform.support.identities.IdentityToken.TokenType;
import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

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
@MachineConnectorSupportedQueries({StringTriggerQuery.class, SimpleTimeseriesQuery.class})
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
    private RecordCompletePredicate recordComplete = RecordCompletePredicate.DEFAULT;

    /**
     * The descriptor of this connector (see META-INF/services).
     */
    public static class Descriptor extends AbstractPluginConnectorDescriptor<Object, Object> {

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public Class<?> getConnectorType() {
            return InfluxConnector.class;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        protected <O, I, CO, CI, S extends AdapterSelector <Object, Object, CO, CI>, 
            A extends ProtocolAdapter<Object, Object, CO, CI>> Connector<Object, Object, CO, CI> 
            createConnectorImpl(S selector, Supplier<ConnectorParameter> params, A... adapter) {
            return new InfluxConnector<CO, CI>(selector, adapter);
        }
        
        @Override
        protected String initId(String id) {
            return PLUGIN_ID_PREFIX + "influx-v2";
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
                IdentityToken tok = params.getIdentityToken(ConnectorParameter.ANY_ENDPOINT);
                if (null != tok) {
                    if (tok.getType() == TokenType.ISSUED) {
                        client = InfluxDBClientFactory.create(url, tok.getTokenDataAsCharArray(), org, bucket);
                        LOGGER.info("INFLUX connected to " + url + " by token");
                    } else if (tok.getType() == TokenType.USERNAME) {
                        client = InfluxDBClientFactory.createV1(url, tok.getUserName(), tok.getTokenDataAsCharArray(), 
                            bucket, null, WriteConsistency.ONE);
                        LOGGER.info("INFLUX connected to " + url + " by username/password");
                    } else {
                        LOGGER.error("INFLUX connector cannot handle identity token type " + tok.getType() + "!");
                    }
                }
                if (null == client) {
                    LOGGER.error("INFLUX not connected!");
                }
            } catch (Exception e) {
                LOGGER.error("INFLUX connection failed: {}", e.getMessage());
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
    
    /**
     * Turns a query time specification into a range query part.
     * 
     * @param start start or stop
     * @param time the time value
     * @param kind the kind
     * @param addSeparator wheter a separator shall be added
     * @return the range part
     */
    private String toRangePart(boolean start, int time, TimeKind kind, boolean addSeparator) {
        // https://docs.influxdata.com/influxdb/cloud/query-data/influxql/explore-data/time-and-timezone/#relative-time
        String result = "";
        if (kind != TimeKind.UNSPECIFIED) {
            result = start ? "start:" : "end:";
            result += time;
            switch (kind) {
            case RELATIVE_WEEKS:
                result += "w";
                break;
            case RELATIVE_DAYS:
                result += "d";
                break;
            case RELATIVE_HOURS:
                result += "h";
                break;
            case RELATIVE_MINUTES:
                result += "m";
                break;
            case RELATIVE_SECONDS:
                result += "s";
                break;
            case RELATIVE_MILLISECONDS:
                result += "ms";
                break;
            case RELATIVE_MICROSECONDS:
                result += "u";
                break;
            default:
                break;
            }
        }
        if (addSeparator) {
            result += ",";
        }
        return result;
    }

    @Override
    public void trigger(ConnectorTriggerQuery query) {
        String qString = null;
        int qDelay = null != query ? query.delay() : 0;
        if (query instanceof StringTriggerQuery) {
            qString = ((StringTriggerQuery) query).getQuery();
        } else if (query instanceof SimpleTimeseriesQuery) {
            SimpleTimeseriesQuery q = (SimpleTimeseriesQuery) query;
            qString = "from(bucket:\"" + getBucket() + "\") ";
            final TimeKind startKind = q.getStartKind();
            final TimeKind endKind = q.getEndKind();
            if (startKind != TimeKind.UNSPECIFIED && endKind != TimeKind.UNSPECIFIED) {
                qString += " |> range( ";
                qString += toRangePart(true, q.getStart(), startKind, startKind != TimeKind.UNSPECIFIED);
                qString += toRangePart(false, q.getEnd(), endKind, false);
                qString += ") ";
            } else {
                qString += " |> range(start:0) ";
            }
            qString += "|> group(columns: [\"_time\"], mode:\"by\") ";
            qString += "|> sort(columns:[\"_time\"])";
        }
        if (qString != null) {
            final String queryString = qString;
            final Runnable run = () -> {
                QueryApi queryApi = client.getQueryApi();
                List<FluxTable> tables = queryApi.query(queryString);
                InfluxModelAccess acc = getModelAccess();
                if (null != acc) {
                    long lastTime = -1;
                    Instant lastRecordTime = null;
                    Map<String, Object> values = new HashMap<>();
                    for (FluxTable fluxTable : tables) {
                        List<FluxRecord> records = fluxTable.getRecords();
                        for (FluxRecord fluxRecord : records) {
                            String field = fluxRecord.getField();
                            Instant time = fluxRecord.getTime();
                            if (null != field) {
                                if (lastRecordTime != null && !lastRecordTime.equals(time) 
                                    || recordComplete.isComplete(values, field)) {
                                    flush(acc, values);
                                } 
                                values.put(field, fluxRecord.getValue());
                            }
                            long thisTime = time.toEpochMilli();
                            int delay = 0;
                            if (qDelay > 0) {
                                delay = qDelay;
                            } else {
                                if (lastTime > 0) {
                                    delay = (int) (thisTime - lastTime);
                                }
                            }
                            TimeUtils.sleep(Math.max(1, delay));
                        }
                    }
                    if (!values.isEmpty()) {
                        flush(acc, values);
                    }
                }
            };
            new Thread(run).start();
        }
    }
    
    /**
     * Flushes aggregated/collected values per record.
     * 
     * @param acc the model access instance
     * @param values the values
     */
    private void flush(InfluxModelAccess acc, Map<String, Object> values) {
        try {
            acc.setReadData(values);
            received(DEFAULT_CHANNEL, DUMMY, true);
            acc.readCompleted();
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot trigger connector {}: {}", 
                getName(), e.getMessage());
        }
        values.clear();
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
