package de.iip_ecosphere.platform.examples.energy.aggregator.server;

import de.iip_ecosphere.platform.examples.energy.aggregator.model.MappedModelUpdate;

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

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class EnergyAggregatorServer {

    private static final Logger LOG = LoggerFactory.getLogger(EnergyAggregatorServer.class);

    private final String host;
    private final int port;
    private final String serverName;

    private OpcUaServer server;
    private EnergyNamespace namespace;
    private volatile boolean running = false;
    private volatile boolean acceptingUpdates = false;

    public EnergyAggregatorServer(String host, int port, String serverName) {
        this.host = host;
        this.port = port;
        this.serverName = serverName;
    }

    public void start() throws Exception {
        EndpointConfiguration endpoint = EndpointConfiguration.newBuilder()
            .setBindAddress(host)
            .setBindPort(port)
            .setPath("/")                              // oktoflow's generated connector uses "/"
            .setSecurityPolicy(SecurityPolicy.None)
            .setSecurityMode(MessageSecurityMode.None)
            .addTokenPolicies(new UserTokenPolicy(
                "anonymous", UserTokenType.Anonymous, null, null, null))
            .build();

        OpcUaServerConfig config = OpcUaServerConfig.builder()
            .setApplicationUri("urn:iip:energy:aggregator:" + serverName)
            .setApplicationName(LocalizedText.english("Energy Aggregator"))
            .setEndpoints(Collections.singleton(endpoint))
            .setIdentityValidator(AnonymousIdentityValidator.INSTANCE)
            .build();

        server = new OpcUaServer(config);
        namespace = new EnergyNamespace(server);
        namespace.initialize();
        namespace.startup();                           // required: register the namespace

        server.startup().get(10, TimeUnit.SECONDS);
        running = true;
        acceptingUpdates = true;

        LOG.info("EnergyAggregatorServer started at {}", getEndpointUrl());
    }

    public void stop() {
        acceptingUpdates = false;
        if (namespace != null) {
            try {
                namespace.shutdown();
            } catch (Exception e) {
                LOG.warn("Error during namespace shutdown", e);
            }
        }
        if (server != null) {
            try {
                server.shutdown().get(5, TimeUnit.SECONDS);
                LOG.info("EnergyAggregatorServer stopped");
            } catch (Exception e) {
                LOG.warn("Error during server shutdown", e);
            }
        }
        running = false;
    }

    public void applyUpdate(MappedModelUpdate update) throws EnergyNamespace.UnknownTargetException {
        if (!acceptingUpdates) throw new IllegalStateException("Server is not accepting updates");
        namespace.applyUpdate(update);
    }

    public boolean isRunning() { return running; }

    public String getEndpointUrl() {
        return "opc.tcp://" + host + ":" + port + "/";
    }

    public EnergyNamespace getNamespace() { return namespace; }
}
