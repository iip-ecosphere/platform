package de.oktoflow.platform.tools.energyAggregator.server;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNodeContext;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.oktoflow.platform.tools.energyAggregator.mapping.OpcEnergyModelMapper;
import de.oktoflow.platform.tools.energyAggregator.model.MappedModelUpdate;
import de.oktoflow.platform.tools.energyAggregator.model.OpcUaQuality;

/**
 * OPC UA address space of the energy aggregator. Structure:
 * <pre>
 * Objects
 *   EnergyAggregator
 *     Machines
 *       &lt;machine&gt;
 *         AcActivePowerTotal           (Float, W)
 *         AcActiveEnergyTotalImportLp  (Float, Wh)
 * </pre>
 * Browse names are the plain variable names so that generic and generated clients can find the variables by
 * name below the machine folder. Variables start with status Bad_WaitingForInitialData until a value arrives.
 * 
 * @author Kodjo Gilles Adjroloh
 * @author Rhoda Ikhuohon-Eboreime
 */
public class EnergyNamespace extends ManagedNamespaceWithLifecycle {

    /**
     * The namespace URI.
     */
    public static final String NAMESPACE_URI = "urn:iip:energy:aggregator";

    /**
     * OPC UA status code value for Good (not available as named constant in Milo 0.6).
     */
    private static final long STATUS_GOOD = 0x00000000L;

    /**
     * OPC UA status code value for Uncertain (not available as named constant in Milo 0.6).
     */
    private static final long STATUS_UNCERTAIN = 0x40000000L;

    /**
     * The logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EnergyNamespace.class);

    /**
     * The machines represented in the address space.
     */
    private final List<String> machines;

    /**
     * The variable nodes, keyed by device id and target ({@code device.target}).
     */
    private final Map<String, UaVariableNode> nodesByKey = new ConcurrentHashMap<>();

    /**
     * The timestamps of the last applied values, keyed like {@link #nodesByKey}.
     */
    private final Map<String, Instant> lastAppliedTimestamps = new ConcurrentHashMap<>();

    /**
     * The subscription model enabling clients to monitor the variables.
     */
    private final SubscriptionModel subscriptionModel;

    /**
     * Creates the namespace.
     * 
     * @param server the OPC UA server
     * @param machines the ids of the machines to represent
     */
    public EnergyNamespace(OpcUaServer server, List<String> machines) {
        super(server, NAMESPACE_URI);
        this.machines = List.copyOf(machines);
        this.subscriptionModel = new SubscriptionModel(server, this);
    }

    @Override
    public void onDataItemsCreated(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsCreated(dataItems);
    }

    @Override
    public void onDataItemsModified(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsModified(dataItems);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> dataItems) {
        subscriptionModel.onDataItemsDeleted(dataItems);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> monitoredItems) {
        subscriptionModel.onMonitoringModeChanged(monitoredItems);
    }

    /**
     * Creates the folders and variables of the address space. Call before {@link #startup()}.
     */
    public void initialize() {
        UaNodeContext ctx = getNodeContext();
        UaFolderNode root = folder(ctx, "EnergyAggregator", "Energy Aggregator");
        // make the top folder visible under the standard Objects folder (inverse Organizes reference)
        root.addReference(new Reference(root.getNodeId(), Identifiers.Organizes, 
            Identifiers.ObjectsFolder.expanded(), false));
        UaFolderNode machinesFolder = folder(ctx, "Machines", "Machines");
        root.addOrganizes(machinesFolder);
        for (String machineId : machines) {
            UaFolderNode machineFolder = folder(ctx, machineId, machineId);
            machinesFolder.addOrganizes(machineFolder);
            addVariable(machineFolder, machineId, OpcEnergyModelMapper.POWER_TARGET);
            addVariable(machineFolder, machineId, OpcEnergyModelMapper.ENERGY_TARGET);
        }
        LOG.info("EnergyNamespace initialized with {} variables", nodesByKey.size());
    }

    /**
     * Creates and registers a folder node.
     * 
     * @param ctx the node context
     * @param id the node id/browse name
     * @param name the display name
     * @return the folder node
     */
    private UaFolderNode folder(UaNodeContext ctx, String id, String name) {
        UaFolderNode folder = new UaFolderNode(ctx, newNodeId(id), newQualifiedName(id), 
            LocalizedText.english(name));
        getNodeManager().addNode(folder);
        return folder;
    }

    /**
     * Creates and registers a read-only Float variable node below {@code parent}.
     * 
     * @param parent the parent folder (the machine)
     * @param deviceId the device id
     * @param target the target variable name, used as browse and display name
     */
    private void addVariable(UaFolderNode parent, String deviceId, String target) {
        String nodeKey = toKey(deviceId, target);
        UaVariableNode node = UaVariableNode.builder(getNodeContext())
            .setNodeId(newNodeId(nodeKey))
            .setBrowseName(newQualifiedName(target))
            .setDisplayName(LocalizedText.english(target))
            .setDataType(Identifiers.Float)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .setAccessLevel(AccessLevel.toValue(AccessLevel.READ_ONLY))
            .setUserAccessLevel(AccessLevel.toValue(AccessLevel.READ_ONLY))
            .setMinimumSamplingInterval(0.0)
            .build();
        node.setValue(new DataValue(new Variant(0.0f), new StatusCode(StatusCodes.Bad_WaitingForInitialData), 
            DateTime.MIN_VALUE, DateTime.now()));
        getNodeManager().addNode(node);
        parent.addOrganizes(node);
        nodesByKey.put(nodeKey, node);
    }

    /**
     * Applies a model update to the respective variable. Updates older than the last applied value are ignored.
     * 
     * @param update the update
     * @throws UnknownTargetException if there is no variable for the device and target of {@code update}
     * @throws IllegalArgumentException if {@code update} is <b>null</b>
     */
    public void applyUpdate(MappedModelUpdate update) throws UnknownTargetException {
        if (null == update) {
            throw new IllegalArgumentException("update must not be null");
        }
        String nodeKey = toKey(update.deviceId(), update.target());
        UaVariableNode node = nodesByKey.get(nodeKey);
        if (null == node) {
            throw new UnknownTargetException(nodeKey);
        }
        Instant incoming = update.timestamp();
        lastAppliedTimestamps.compute(nodeKey, (key, previous) -> {
            Instant result = previous;
            if (null == previous || incoming.isAfter(previous)) {
                node.setValue(new DataValue(new Variant((float) update.value()), toStatusCode(update.quality()),
                    new DateTime(incoming), DateTime.now()));
                result = incoming;
            } else {
                LOG.warn("Rejecting older update for '{}': {} <= {}", key, incoming, previous);
            }
            return result;
        });
    }

    /**
     * Returns the number of variables in the address space.
     * 
     * @return the number of variables
     */
    public int size() {
        return nodesByKey.size();
    }

    /**
     * Turns device id and target into a node key.
     * 
     * @param deviceId the device id
     * @param target the target variable name
     * @return the node key
     */
    private static String toKey(String deviceId, String target) {
        return deviceId + "." + target;
    }

    /**
     * Translates an OPC UA quality into an OPC UA status code.
     * 
     * @param quality the quality, may be <b>null</b>
     * @return the status code, Bad_NoData for <b>null</b>
     */
    public static StatusCode toStatusCode(OpcUaQuality quality) {
        StatusCode result;
        if (null == quality) {
            result = new StatusCode(StatusCodes.Bad_NoData);
        } else {
            result = switch (quality) {
            case GOOD -> new StatusCode(STATUS_GOOD);
            case UNCERTAIN -> new StatusCode(STATUS_UNCERTAIN);
            case BAD_NOT_CONNECTED -> new StatusCode(StatusCodes.Bad_NotConnected);
            case BAD_INVALID_DATA -> new StatusCode(StatusCodes.Bad_InvalidArgument);
            };
        }
        return result;
    }

}
