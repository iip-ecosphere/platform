package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.connectors.Connector;
import de.iip_ecosphere.platform.connectors.ConnectorParameter;
import de.iip_ecosphere.platform.connectors.events.EventHandlingConnector;
import de.iip_ecosphere.platform.connectors.model.SharedSpace;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
import de.iip_ecosphere.platform.transport.serialization.TypeTranslators;

/**
 * Wraps multiple connector instances into a service. Implicitly reacts on parameter "inPath" and "outPath" as string 
 * to override dynamically the configured data path into the connector data. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class MultiConnectorServiceWrapper extends AbstractService {

    private static final Comparator<Class<?>> CLASS_COMPARATOR = (c1, c2) -> c1.getName().compareTo(c2.getName());
    
    private Map<Class<?>, Connector<?, ?, ?, ?>> inConnectors = new TreeMap<>(CLASS_COMPARATOR);
    private Map<Class<?>, Connector<?, ?, ?, ?>> outConnectors = new TreeMap<>(CLASS_COMPARATOR);
    private Supplier<ConnectorParameter> connParamSupplier;
    private Map<String, ParameterConfigurer<?>> paramConfigurers = new HashMap<>();
    private String outPath; // the runtime-reconfigured data path
    private String inPath; // the runtime-reconfigured data path
    private SharedSpace sharedSpace;

    /**
     * Creates a service wrapper instance.
     * 
     * @param yaml the service information as read from YAML
     * @param connParamSupplier the connector parameter supplier for connecting the underlying connector
     */
    public MultiConnectorServiceWrapper(YamlService yaml, Supplier<ConnectorParameter> connParamSupplier) {
        super(yaml);
        this.connParamSupplier = connParamSupplier; 
        
        AbstractService.addConfigurer(paramConfigurers, "outPath", String.class, TypeTranslators.STRING, 
            v -> setOutPath(v), () -> outPath, Starter.IIP_APP_PREFIX + getId() + ".outPath");
        AbstractService.addConfigurer(paramConfigurers, "inPath", String.class, TypeTranslators.STRING, 
            v -> setInPath(v), () -> inPath, Starter.IIP_APP_PREFIX + getId() + ".inPath");
    }
    
    /**
     * Adds a connector. Already registered connectors will be overwritten. Creates/enables a shared connector space
     * if available.
     * 
     * @param connector the connector to register (may be <b>null</b>, ignored)
     */
    public void addConnector(Connector<?, ?, ?, ?> connector) {
        if (null != connector) {
            if (inConnectors.isEmpty()) {
                sharedSpace = connector.createSharedSpace();
            } else {
                connector.enableSharedSpace(sharedSpace);
            }
            inConnectors.put(connector.getConnectorInputType(), connector);
            outConnectors.put(connector.getConnectorOutputType(), connector);
        }
    }
    
    /**
     * Returns whether any connectors were added.
     * 
     * @return {@code true} for empty, {@code false} else
     */
    public boolean isEmpty() {
        return inConnectors.isEmpty();
    }

    /**
     * Returns the event handling connector for the given (app) connector input type.
     * 
     * @param <CI> the (app) connector input type
     * @param ciCls the (app) connector input type class
     * @return the connector, may be <b>null</b> for none
     */
    public <CI> EventHandlingConnector getEventHandlingConnector(Class<CI> ciCls) {
        return inConnectors.get(ciCls);
    }

    /**
     * Returns all connectors.
     * 
     * @return the connectors (in a stable sequence)
     */
    protected Collection<Connector<?, ?, ?, ?>> getConnectors() {
        return inConnectors.values();
    }

    /**
     * Resolves cls to the declared type.
     * 
     * @param cls the class to resolve
     * @return the resolved class
     */
    protected static Class<?> resolve(Class<?> cls) {
        String name = cls.getName();
        String modName = name;
        final String mockPrefix = "iip.mock";
        final String mockSuffix = "Mock";
        if (modName.startsWith(mockPrefix) && modName.endsWith(mockSuffix)) { // in testing
            modName = modName.substring(mockPrefix.length()); 
            modName = modName.substring(0, modName.length() - mockSuffix.length()); 
            modName = "iip.datatypes" + modName; 
        }
        final String implSuffix = "Impl";
        if (modName.endsWith(implSuffix)) {
            modName = modName.substring(0, modName.length() - implSuffix.length()); 
        }
        if (!name.equals(modName)) {
            try {
                cls = Class.forName(modName);
            } catch (ClassNotFoundException e) {
            }
        }
        return cls;
    }
    
    /**
     * Returns the actual connector output type.
     * 
     * @param <CO> the declared connector output type
     * @param connectorOutType the connector output type
     * @return the actual output type
     */
    @SuppressWarnings("unchecked")
    protected static <CO> Class<? extends CO> getActualConnectorOutputType(Class<CO> connectorOutType) {
        Class<? extends CO> result = connectorOutType;
        if (result.isInterface()) {
            try {
                result = (Class<? extends CO>) Class.forName(connectorOutType.getName() + "Impl");
            } catch (ClassNotFoundException e) {
            }
        }
        return result;
    }
    
    /**
     * Calls {@link Connector#write(Object)} on {@code} data and handles the respective exception potentially thrown by 
     * the underlying connector.
     * 
     * @param data the data to write
     */
    public <CI> void send(Class<CI> cls, CI data) {
        try {
            @SuppressWarnings("unchecked")
            Connector<?, ?, ?, CI> conn = (Connector<?, ?, ?, CI>) inConnectors.get(cls);
            if (conn != null) {
                conn.write(data);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(this).error("Data loss, cannot send data: " + e.getMessage());
        }
    }
    
    /**
     * Attaches a reception {@code callback} to this connector. The {@code callback}
     * is called upon a reception. Handles the respective exception potentially thrown by the underlying connector.
     * 
     * @param callback the callback to attach
     */
    public <CO> void setReceptionCallback(ReceptionCallback<CO> callback) {
        try {
            @SuppressWarnings("unchecked")
            Connector<?, ?, CO, ?> conn = (Connector<?, ?, CO, ?>) outConnectors.get(callback.getType());
            conn.setReceptionCallback(callback);
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Data loss, cannot set reception callback: " + e.getMessage());
        }
    }
    
    @Override
    public void setState(ServiceState state) throws ExecutionException {
        doSetState(state);
        ConnectorParameter param = null;
        for (Connector<?, ?, ?, ?> connector : getConnectors()) {
            try {
                if (ServiceState.STARTING == state) {
                    if (null == param) {
                        param = connParamSupplier.get();
                    }
                    connector.connect(param);
                    // not needed, but generation may statically switch off notifications and prevent testing with
                    // different values
                    connector.enableNotifications(param.getNotificationInterval() == 0);
                    doSetState(ServiceState.RUNNING);
                } else if (ServiceState.STOPPING == state) {
                    connector.disconnect();
                    doSetState(ServiceState.STOPPED);
                } else if (ServiceState.UNDEPLOYING == state) {
                    connector.dispose();
                }
            } catch (IOException e) {
                throw new ExecutionException(e);
            }
        }
    }
    
    /**
     * Changes the state by calling {@link AbstractService#setState(ServiceState)}. Introduced, so that super 
     * functionality is made available to super-classes as-is.
     * 
     * @param state the new state
     * @throws ExecutionException if changing the state fails for some reason
     */
    protected void doSetState(ServiceState state) throws ExecutionException {
        super.setState(state);
    }
    
    @Override
    public void migrate(String resourceId) throws ExecutionException {
    }

    @Override
    public void update(URI location) throws ExecutionException {
    }

    @Override
    public void switchTo(String targetId) throws ExecutionException {
    }

    /**
     * Enable/disable polling (does not influence the polling timer).
     * 
     * @param enablePolling whether polling shall enabled
     * @see #enableNotifications(boolean)
     */
    public void enablePolling(boolean enablePolling) {
        getConnectors().forEach(connector -> connector.enablePolling(enablePolling));
    }

    /**
     * Enables/disables notifications/polling at all.
     * 
     * @param enableNotifications enable or disable notifications
     */
    public void enableNotifications(boolean enableNotifications) {
        getConnectors().forEach(connector -> connector.enableNotifications(enableNotifications));
    }

    @Override
    public ParameterConfigurer<?> getParameterConfigurer(String paramName) {
        return paramConfigurers.get(paramName);
    }
    
    @Override
    public Set<String> getParameterNames() {
        return paramConfigurers.keySet();
    }
    
    /**
     * Changes {@link #inPath}.
     * 
     * @param inPath the in path (ignored if <b>null</b> or empty)
     */
    private void setInPath(String inPath) {
        if (inPath != null && inPath.length() > 0) {
            this.inPath = inPath;
        }
    }
    
    /**
     * Changes {@link #outPath}.
     * 
     * @param outPath the out path (ignored if <b>null</b> or empty)
     */
    private void setOutPath(String outPath) {
        if (outPath != null && outPath.length() > 0) {
            this.outPath = outPath;
        }
    }
    
    /**
     * Returns the (eventually re-configured) data access path within the protocol.
     *  
     * @param cfgPath the configured path from the model
     * @return the path to use, may be {@code cfgPath}
     */
    public String getOutPath(String cfgPath) {
        String result = cfgPath;
        if (outPath != null) {
            result = outPath;
        }
        return result;
    }

    /**
     * Returns the (eventually re-configured) data access path within the protocol.
     *  
     * @param cfgPath the configured path from the model
     * @return the path to use, may be {@code cfgPath}
     */
    public String getInPath(String cfgPath) {
        String result = cfgPath;
        if (inPath != null) {
            result = inPath;
        }
        return result;
    }

}
