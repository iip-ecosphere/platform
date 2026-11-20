package de.oktoflow.platform.tools.energyAggregator.server;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.oktoflow.platform.tools.energyAggregator.model.MappedModelUpdate;

/**
 * OPC UA server of the energy aggregator (Eclipse Milo). Anonymous access without security, endpoint path
 * {@code /}, as used by the generated oktoflow OPC UA connector.
 * 
 * @author Kodjo Gilles Adjroloh
 * @author Rhoda Ikhuohon-Eboreime
 */
public class EnergyAggregatorServer {

    /**
     * Timeout for server startup in seconds.
     */
    private static final int STARTUP_TIMEOUT = 10;

    /**
     * Timeout for server shutdown in seconds.
     */
    private static final int SHUTDOWN_TIMEOUT = 5;

    /**
     * The logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EnergyAggregatorServer.class);

    /**
     * The host to bind to.
     */
    private final String host;

    /**
     * The port to bind to.
     */
    private final int port;

    /**
     * The machines to represent.
     */
    private final List<String> machines;

    /**
     * The Milo server instance, <b>null</b> if not started.
     */
    private OpcUaServer server;

    /**
     * The address space, <b>null</b> if not started.
     */
    private EnergyNamespace namespace;

    /**
     * Whether updates are currently accepted.
     */
    private volatile boolean acceptingUpdates;

    /**
     * Creates a server instance.
     * 
     * @param host the host to bind to
     * @param port the port to bind to
     * @param machines the ids of the machines to represent
     */
    public EnergyAggregatorServer(String host, int port, List<String> machines) {
        this.host = host;
        this.port = port;
        this.machines = List.copyOf(machines);
    }

    /**
     * Starts the server and creates the address space.
     * 
     * @throws IOException if the server cannot be started
     * @throws InterruptedException if interrupted while waiting for the server to start
     */
    public void start() throws IOException, InterruptedException {
        EndpointConfiguration endpoint = EndpointConfiguration.newBuilder()
            .setBindAddress(host)
            .setBindPort(port)
            .setPath("/")
            .setSecurityPolicy(SecurityPolicy.None)
            .setSecurityMode(MessageSecurityMode.None)
            .addTokenPolicies(new UserTokenPolicy("anonymous", UserTokenType.Anonymous, null, null, null))
            .build();
        OpcUaServerConfig config = OpcUaServerConfig.builder()
            .setApplicationUri("urn:iip:energy:aggregator:server")
            .setApplicationName(LocalizedText.english("Energy Aggregator"))
            .setEndpoints(Collections.singleton(endpoint))
            .setIdentityValidator(AnonymousIdentityValidator.INSTANCE)
            .build();
        server = new OpcUaServer(config);
        namespace = new EnergyNamespace(server, machines);
        namespace.initialize();
        namespace.startup(); // registers the namespace with the server, required for browsing
        try {
            server.startup().get(STARTUP_TIMEOUT, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new IOException("Cannot start OPC UA server at " + getEndpointUrl(), e);
        }
        acceptingUpdates = true;
        LOG.info("EnergyAggregatorServer started at {}", getEndpointUrl());
    }

    /**
     * Stops the server.
     */
    public void stop() {
        acceptingUpdates = false;
        if (null != namespace) {
            namespace.shutdown();
        }
        if (null != server) {
            try {
                server.shutdown().get(SHUTDOWN_TIMEOUT, TimeUnit.SECONDS);
                LOG.info("EnergyAggregatorServer stopped");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException e) {
                LOG.warn("Error during server shutdown: {}", e.getMessage());
            }
        }
    }

    /**
     * Applies a model update.
     * 
     * @param update the update
     * @throws UnknownTargetException if there is no variable for the device and target of {@code update}
     * @throws IllegalStateException if the server is not running
     */
    public void applyUpdate(MappedModelUpdate update) throws UnknownTargetException {
        if (!acceptingUpdates) {
            throw new IllegalStateException("Server is not accepting updates");
        }
        namespace.applyUpdate(update);
    }

    /**
     * Returns whether the server is running and accepts updates.
     * 
     * @return {@code true} for running, {@code false} else
     */
    public boolean isRunning() {
        return acceptingUpdates;
    }

    /**
     * Returns the endpoint URL of the server.
     * 
     * @return the endpoint URL
     */
    public String getEndpointUrl() {
        return "opc.tcp://" + host + ":" + port + "/";
    }

    /**
     * Returns the address space.
     * 
     * @return the address space, <b>null</b> if not started
     */
    public EnergyNamespace getNamespace() {
        return namespace;
    }

}
