/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace;

import java.util.List;
import java.util.UUID;

import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.core.ValueRanks;
import org.eclipse.milo.opcua.sdk.server.Lifecycle;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.BaseEventTypeNode;
import org.eclipse.milo.opcua.sdk.server.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExpandedNodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.structured.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector;
import de.iip_ecosphere.platform.support.TimeUtils;
import test.de.iip_ecosphere.platform.connectors.opcuav1.DataTypeDictionaryManager;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ushort;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.ubyte;

/**
 * Implements an OPC namespace for testing. This class is based on the Milo examples.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Namespace extends ManagedNamespaceWithLifecycle {

    public static final String NAMESPACE_URI = "urn:eclipse:milo:hello-world";
    public static final String QNAME_TOP_FOLDER;
    public static final String QNAME_VAR_LOT_SIZE;
    public static final String QNAME_VAR_POWER_CONSUMPTION;
    public static final String QNAME_VAR_STRUCT;
    public static final String QNAME_EVENT_NODE;
    public static final String QNAME_METHOD_START;
    public static final String QNAME_METHOD_END;
    public static final String VENDOR_NAME = "Phoenix Contact";

    private static final String NAME_TOP_FOLDER = "HelloWorld";
    private static final String NAME_VAR_LOT_SIZE = "lotSize";
    private static final String NAME_VAR_POWER_CONSUMPTION = "powerConsumption";
    private static final String NAME_VAR_STRUCT = "vendor";
    private static final String NAME_EVENT_NODE = "events";
    private static final String NAME_METHOD_START = "startMachine";
    private static final String NAME_METHOD_END = "endMachine";
    
    static {
        QNAME_TOP_FOLDER = OpcUaConnector.TOP_OBJECTS + OpcUaConnector.SEPARATOR_CHAR + NAME_TOP_FOLDER;
        QNAME_VAR_LOT_SIZE = QNAME_TOP_FOLDER + OpcUaConnector.SEPARATOR_CHAR + NAME_VAR_LOT_SIZE;
        QNAME_VAR_POWER_CONSUMPTION = QNAME_TOP_FOLDER + OpcUaConnector.SEPARATOR_CHAR + NAME_VAR_POWER_CONSUMPTION;
        QNAME_VAR_STRUCT = QNAME_TOP_FOLDER + OpcUaConnector.SEPARATOR_CHAR + NAME_VAR_STRUCT;
        QNAME_EVENT_NODE = NAME_EVENT_NODE;
        QNAME_METHOD_START = QNAME_TOP_FOLDER + OpcUaConnector.SEPARATOR_CHAR + NAME_METHOD_START;
        QNAME_METHOD_END = QNAME_TOP_FOLDER + OpcUaConnector.SEPARATOR_CHAR + NAME_METHOD_END;
    }

    /**
     * Defines the lot size argument of the {@link ReconfigureMethod}.
     */
    private static final Argument LOT_SIZE = new Argument(
        "x",
        Identifiers.Integer,
        ValueRanks.Scalar,
        null,
        new LocalizedText("The new lot size.")
    );
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SubscriptionModel subscriptionModel;
    private final DataTypeDictionaryManager dictionaryManager;
    private volatile Thread eventThread;
    private volatile boolean keepPostingEvents = true;

    private UaVariableNode lotSize;
    private UaVariableNode powConsumption;

    /**
     * Creates a test namespace.
     * 
     * @param server the server to create the namespace for
     */
    public Namespace(OpcUaServer server) {
        super(server, NAMESPACE_URI);
        subscriptionModel = new SubscriptionModel(server, this);

        dictionaryManager = new DataTypeDictionaryManager(getNodeContext(), NAMESPACE_URI);

        getLifecycleManager().addLifecycle(dictionaryManager);
        getLifecycleManager().addLifecycle(subscriptionModel);

        getLifecycleManager().addStartupTask(this::createAndAddNodes);

        getLifecycleManager().addLifecycle(new Lifecycle() {
            @Override
            public void startup() {
                startBogusEventNotifier();
            }

            @Override
            public void shutdown() {
                try {
                    keepPostingEvents = false;
                    eventThread.interrupt();
                    eventThread.join();
                } catch (InterruptedException ignored) {
                    // ignored
                }
            }
        });
    }

    // checkstyle: stop exception type check

    /**
     * Creates the nodes for in the namespace.
     */
    private void createAndAddNodes() {
        UaFolderNode folderNode = createFolder(null, NAME_TOP_FOLDER);        
        lotSize = createVariable(folderNode, NAME_VAR_LOT_SIZE, Identifiers.Integer, new Variant(1), 
            AccessLevel.READ_WRITE);
        powConsumption = createVariable(folderNode, NAME_VAR_POWER_CONSUMPTION, Identifiers.Double, new Variant(0.1), 
            AccessLevel.READ_ONLY);
        addMethod(folderNode, NAME_METHOD_START, "Starts the machine.", n -> new StartProcessingMethod(n));
        addMethod(folderNode, NAME_METHOD_END, "Stops the machine.", n -> new StopProcessingMethod(n));

        try {
            VendorStruct.registerType(getServer(), getNamespaceIndex(), dictionaryManager);
            VendorStruct value = new VendorStruct(VENDOR_NAME, 2020, true);
            addCustomStructTypeVariable(folderNode, NAME_VAR_STRUCT, VendorStruct.TYPE_ID, 
                VendorStruct.BINARY_ENCODING_ID, value);
        } catch (Exception e) {
            logger.warn("Failed to register custom struct type", e);
        }
    }

    // checkstyle: resume exception type check
    
    /**
     * A method to stop processing (of a virtual machine).
     * 
     * @author Holger Eichelberger, SSE
     */
    public class StartProcessingMethod extends AbstractMethodInvocationHandler {

        /**
         * Creates a start processing method.
         * 
         * @param node the declaring method node
         */
        public StartProcessingMethod(UaMethodNode node) {
            super(node);
        }
        
        @Override
        public Argument[] getInputArguments() {
            return new Argument[]{};
        }
        
        @Override
        public Argument[] getOutputArguments() {
            return new Argument[]{};
        }
        
        @Override
        protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) {
            powConsumption.setValue(new DataValue(new Variant(10.1)));
            logger.info("Machine started, power consumption changed to 10.1");
            return new Variant[]{};
        }
        
    }
    
    /**
     * A method to stop processing (of a virtual machine).
     * 
     * @author Holger Eichelberger, SSE
     */
    public class StopProcessingMethod extends AbstractMethodInvocationHandler {

        /**
         * Creates a stop processing method.
         * 
         * @param node the declaring method node
         */
        public StopProcessingMethod(UaMethodNode node) {
            super(node);
        }
        
        @Override
        public Argument[] getInputArguments() {
            return new Argument[]{};
        }
        
        @Override
        public Argument[] getOutputArguments() {
            return new Argument[]{};
        }
        
        @Override
        protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) {
            powConsumption.setValue(new DataValue(new Variant(0.1)));
            lotSize.setValue(new DataValue(new Variant(1)));
            logger.info("Machine stopped, power consumption changed to 0.1, lot size to 1");
            return new Variant[]{};
        }
        
    }

    /**
     * A method to stop processing (of a virtual machine).
     * 
     * @author Holger Eichelberger, SSE
     */
    public class ReconfigureMethod extends AbstractMethodInvocationHandler {
        
        /**
         * Creates a start processing method.
         * 
         * @param node the declaring method node
         */
        public ReconfigureMethod(UaMethodNode node) {
            super(node);
        }
        
        @Override
        public Argument[] getInputArguments() {
            return new Argument[]{LOT_SIZE};
        }
        
        @Override
        public Argument[] getOutputArguments() {
            return new Argument[]{};
        }
        
        @Override
        protected Variant[] invoke(InvocationContext invocationContext, Variant[] inputValues) {
            lotSize.setValue(new DataValue(inputValues[0]));
            logger.info("Machine reconfigured, lot size changed");
            return new Variant[]{};
        }
        
    }

    /**
     * Creates a folder node.
     * 
     * @param parent the parent folder (may be <b>null</b> for top-level parent)
     * @param name the name of the node (we will just use that also as nodeId, qualified name and localized name)
     * @return the created folder node
     */
    private UaFolderNode createFolder(UaFolderNode parent, String name) {
        UaFolderNode result = new UaFolderNode(
            getNodeContext(),
            newNodeId(name),
            newQualifiedName(name),
            LocalizedText.english(name)
        );

        getNodeManager().addNode(result);
        if (null != parent) {
            parent.addOrganizes(result);
        } else {
            // Make sure our new folder shows up under the server's Objects folder.
            result.addReference(new Reference(
                    result.getNodeId(),
                Identifiers.Organizes,
                Identifiers.ObjectsFolder.expanded(),
                false
            ));
        }
        return result;
    }
    
    /**
     * Creates a variable.
     * 
     * @param parent the parent folder containing the new variable
     * @param name the name of the variable (we will just use that also as nodeId, qualified name and localized name)
     * @param type the type of the variable
     * @param value the initial value
     * @param access the access level
     * @return the created variable node
     */
    private UaVariableNode createVariable(UaFolderNode parent, String name, NodeId type, Variant value, 
        ImmutableSet<AccessLevel> access) {
        UaVariableNode result = new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
            .setNodeId(newNodeId(parent.getBrowseName().getName() + "/" + name))
            .setAccessLevel(access)
            .setUserAccessLevel(access)
            .setBrowseName(newQualifiedName(name))
            .setDisplayName(LocalizedText.english(name))
            .setDataType(type)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        result.setValue(new DataValue(value));

        //node.getFilterChain().addLast(new AttributeLoggingFilter(AttributeId.Value::equals));

        getNodeManager().addNode(result);
        parent.addOrganizes(result);
        
        return result;
    }
    
    /**
     * Adds a method declaration to {@code parent}.
     * 
     * @param parent the parent node
     * @param name the name of the method
     * @param description a textual description of the method
     * @param creator the method creator functor
     */
    private void addMethod(UaFolderNode parent, String name, String description, MethodCreator<?> creator) {
        UaMethodNode methodNode = UaMethodNode.builder(getNodeContext())
            .setNodeId(newNodeId(parent.getBrowseName().getName() + "/" + name))
            .setBrowseName(newQualifiedName(name))
            .setDisplayName(new LocalizedText(null, name))
            .setDescription(LocalizedText.english(description))
            .build();

        AbstractMethodInvocationHandler method = creator.create(methodNode);
        //SqrtMethod sqrtMethod = new SqrtMethod(methodNode);
        methodNode.setInputArguments(method.getInputArguments());
        methodNode.setOutputArguments(method.getOutputArguments());
        methodNode.setInvocationHandler(method);

        getNodeManager().addNode(methodNode);

        methodNode.addReference(new Reference(
            methodNode.getNodeId(),
            Identifiers.HasComponent,
            parent.getNodeId().expanded(),
            false
        ));
    }

    
    // checkstyle: stop exception type check

    /**
     * Adds a variable of a custom type.
     * 
     * @param parent the parent folder containing the new variable
     * @param name the name of the variable (we will just use that also as nodeId, qualified name and localized name)
     * @param type the type of the variable
     * @param encoding the encoding type of the variable
     * @param value the initial value
     * @throws Exception in case of a problem (called methods also just throw an Exception)
     */
    private void addCustomStructTypeVariable(UaFolderNode parent, String name, ExpandedNodeId type, 
        ExpandedNodeId encoding, Object value) throws Exception {
        NodeId dataTypeId = type.toNodeIdOrThrow(getServer().getNamespaceTable());
        NodeId binaryEncodingId = encoding.toNodeIdOrThrow(getServer().getNamespaceTable());

        UaVariableNode customStructTypeVariable = UaVariableNode.builder(getNodeContext())
            .setNodeId(newNodeId(parent.getBrowseName().getName() + "/" + name))
            .setAccessLevel(AccessLevel.READ_WRITE)
            .setUserAccessLevel(AccessLevel.READ_WRITE)
            .setBrowseName(newQualifiedName(name))
            .setDisplayName(LocalizedText.english(name))
            .setDataType(dataTypeId)
            .setTypeDefinition(Identifiers.BaseDataVariableType)
            .build();

        ExtensionObject xo = ExtensionObject.encodeDefaultBinary(
            getServer().getSerializationContext(),
            value,
            binaryEncodingId
        );

        customStructTypeVariable.setValue(new DataValue(new Variant(xo)));

        getNodeManager().addNode(customStructTypeVariable);

        customStructTypeVariable.addReference(new Reference(
            customStructTypeVariable.getNodeId(),
            Identifiers.Organizes,
            parent.getNodeId().expanded(),
            false
        ));
    }

    // checkstyle: resume exception type check
    
    /**
     * Starts an event notifier.
     */
    private void startBogusEventNotifier() {
        // Set the EventNotifier bit on Server Node for Events.
        UaNode serverNode = getServer()
            .getAddressSpaceManager()
            .getManagedNode(Identifiers.Server)
            .orElse(null);

        if (serverNode instanceof ServerTypeNode) {
            ((ServerTypeNode) serverNode).setEventNotifier(ubyte(1));

            // Post a bogus Event every couple seconds
            eventThread = new Thread(() -> {
                while (keepPostingEvents) {
                    try {
                        BaseEventTypeNode eventNode = getServer().getEventFactory().createEvent(
                            newNodeId(UUID.randomUUID()),
                            Identifiers.BaseEventType
                        );

                        eventNode.setBrowseName(new QualifiedName(1, NAME_EVENT_NODE));
                        eventNode.setDisplayName(LocalizedText.english(NAME_EVENT_NODE));
                        eventNode.setEventId(ByteString.of(new byte[]{0, 1, 2, 3}));
                        eventNode.setEventType(Identifiers.BaseEventType);
                        eventNode.setSourceNode(serverNode.getNodeId());
                        eventNode.setSourceName(serverNode.getDisplayName().getText());
                        eventNode.setTime(DateTime.now());
                        eventNode.setReceiveTime(DateTime.NULL_VALUE);
                        eventNode.setMessage(LocalizedText.english("event message!"));
                        eventNode.setSeverity(ushort(2));

                        //noinspection UnstableApiUsage
                        getServer().getEventBus().post(eventNode);

                        eventNode.delete();
                    } catch (UaException e) {
                        logger.error("Error creating EventNode: {}", e.getMessage(), e);
                    }

                    //noinspection BusyWait
                    TimeUtils.sleep(500);
                }
            }, "bogus-event-poster");

            eventThread.start();
        }
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

}
