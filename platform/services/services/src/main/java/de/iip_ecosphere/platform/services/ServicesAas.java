/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstants;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstructor;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.ClassUtility;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.transport.Transport;
import de.iip_ecosphere.platform.transport.connectors.TransportSetup;
import de.iip_ecosphere.platform.transport.status.ActionTypes;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the AAS for the services. Container ids used as short AAS ids may be translated into ids that are
 * valid from the perspective of the AAS implementation. All nested elements also carry their original id in 
 * {@link #NAME_PROP_ID}. 
 * 
 * The created submodels may be used standalone or deployed to a common server. In the second case, parts of the 
 * submodels will be complemented incrementally, e.g., the relations by the services started or the resources
 * by the ECS runtime.
 * 
 * This class builds the submodel services ({@link #NAME_SUBMODEL}):
 * <ul>
 *  <li>A submodel elements collection "services" {@link #NAME_COLL_SERVICES} containing all declared services with 
 *    their input and output types using the service id as ID.</li>
 *  <li>A submodel elements collection "artifacts" {@link #NAME_COLL_ARTIFACTS} with all artifacts implementing the 
 *    services using the artifact id as ID.</li>
 *  <li>A submodel elements collection "relations" {@link #NAME_COLL_RELATIONS} with all relations connecting the 
 *    services with the channel name as ID. This part is aimed at a quick lookup whether a related service is already 
 *    there.</li>
 * </ul>
 * Moreover, this class builds the parts of the submodel resources ({@link #NAME_SUBMODEL_RESOURCES}), containing 
 * submodel elements named according to the Unique JVM identifier of this process containing the provided operations. 
 * This submodel is complemented by the ECSruntime with more resource specific information.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicesAas implements AasContributor {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_SERVICES;
    public static final String NAME_SUBMODEL_RESOURCES = AasPartRegistry.NAME_SUBMODEL_RESOURCES;
    public static final String NAME_COLL_ARTIFACTS = "artifacts";
    public static final String NAME_COLL_SERVICES = AasPartRegistry.NAME_COLLECTION_SERVICES;
    public static final String NAME_COLL_RELATIONS = "relations";
    public static final String NAME_COLL_SERVICE_MANAGERS = "serviceManagers";
    public static final String NAME_SUBCOLL_PARAMETERS = "parameters";
    public static final String NAME_SUBCOLL_INPUT_DATA_CONN = "inputDataConnectors";
    public static final String NAME_SUBCOLL_OUTPUT_DATA_CONN = "outputDataConnectors";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_APPLICATION_ID = "applicationId";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_STATE = "state";
    public static final String NAME_PROP_KIND = "kind";
    public static final String NAME_PROP_VERSION = "version";
    public static final String NAME_PROP_DESCRIPTION = "description";
    public static final String NAME_PROP_TYPE = "type";
    public static final String NAME_PROP_RESOURCE = "resource";
    public static final String NAME_PROP_FROM = "from";
    public static final String NAME_PROP_FROM_RESOURCE = "fromResource";
    public static final String NAME_PROP_TO = "to";
    public static final String NAME_PROP_TO_RESOURCE = "toResource";
    public static final String NAME_PROP_ARTIFACT = "artifact";
    public static final String NAME_PROP_TOPLEVEL = "topLevel";
    public static final String NAME_PROP_SERVICE_AAS = "serviceAas";
    public static final String NAME_PROP_DEVICE_AAS = AasPartRegistry.NAME_PROP_DEVICE_AAS;
    public static final String NAME_PROP_IN_CLEANUP = "inCleanup";
    public static final String NAME_PROP_SUPPORTED_APPIDS = "supportedAppIds";
    public static final String NAME_OP_SERVICE_START = "startService";
    public static final String NAME_OP_SERVICE_START_TASK = "startServiceAsTask";
    public static final String NAME_OP_SERVICE_START_WITH_OPTS = "startServiceWithOptions";
    public static final String NAME_OP_SERVICE_START_WITH_OPTS_TASK = "startServiceWithOptionsAsTask";
    public static final String NAME_OP_SERVICE_ACTIVATE = "activateService";
    public static final String NAME_OP_SERVICE_PASSIVATE = "passivateService";
    public static final String NAME_OP_SERVICE_MIGRATE = "migrateService";
    public static final String NAME_OP_SERVICE_UPDATE = "updateService";
    public static final String NAME_OP_SERVICE_SWITCH = "switchToService";
    public static final String NAME_OP_SERVICE_RECONF = "reconfigureService";
    public static final String NAME_OP_SERVICE_STOP = "stopService";
    public static final String NAME_OP_SERVICE_STOP_TASK = "stopServiceAsTask";
    public static final String NAME_OP_SERVICE_GET_STATE = "getServiceSate";
    public static final String NAME_OP_SERVICE_SET_STATE = "setServiceSate";
    public static final String NAME_OP_ARTIFACT_ADD = "addArtifact";
    public static final String NAME_OP_ARTIFACT_REMOVE = "removeArtifact";
    public static final String NAME_OP_SERVICE_INSTANCE_COUNT  = "getServiceInstanceCount";
    public static final String NAME_OP_SERVICE_STATE_COUNT  = "getServiceStateCount";
    
    private static final String ID_SUBMODEL = null; // take the short name, shall become public and an URN later
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        ServiceManager mgr = ServiceFactory.getServiceManager();
        if (null != mgr) { // this shall not be needed, but if the Jar is present, the contributor will be executed 
            // operations contribute to the operation of the underlying resource (Service JVM or ECS Runtime JVM)
            SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL_RESOURCES, ID_SUBMODEL);
            SubmodelElementCollectionBuilder deviceB 
                = smB.createSubmodelElementCollectionBuilder(Id.getDeviceIdAas(), false, false);
            // #115 remove as legacy
            if (!deviceB.hasElement(NAME_PROP_SUPPORTED_APPIDS)) { // just keep the first one, avoid overriding
                createServiceManagerSubmodelElements(deviceB, iCreator); // legacy
            }

            SubmodelElementCollectionBuilder svcMgrsB 
                = deviceB.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICE_MANAGERS, false, false);
            SubmodelElementCollectionBuilder svcMgrB
                = svcMgrsB.createSubmodelElementCollectionBuilder(Id.getEnvIdAas(), false, false);
            createServiceManagerSubmodelElements(svcMgrB, iCreator);
            svcMgrB.build();
            svcMgrsB.build();
            
            deviceB.build();
    
            smB.defer(); // join with ecsRuntime if present, build done by AAS
    
            // service structures go into own part
            
            smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, ID_SUBMODEL);
            // ensure that these collections do exist
            smB.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICES, false, false).build();
            smB.createSubmodelElementCollectionBuilder(NAME_COLL_ARTIFACTS, false, false).build();
            smB.createSubmodelElementCollectionBuilder(NAME_COLL_RELATIONS, false, false).build();
    
            for (ArtifactDescriptor a : mgr.getArtifacts()) {
                addArtifact(smB, a);
            }
            for (ServiceDescriptor s : mgr.getServices()) {
                addService(smB, s);
            }
    
            smB.build();
        }
        return null;
    }
    
    /**
     * Creates the elements representing a service manager.
     * 
     * @param builder the target builder
     * @param iCreator the invocables creator for binding (remote) property and operation implementations.
     */
    private void createServiceManagerSubmodelElements(SubmodelElementCollectionBuilder builder, 
        InvocablesCreator iCreator) {
        createIdOp(builder, NAME_OP_SERVICE_START, iCreator);
        createIdOp(builder, NAME_OP_SERVICE_START_TASK, iCreator, "taskId");
        createIdOp(builder, NAME_OP_SERVICE_START_WITH_OPTS, iCreator, "options");
        createIdOp(builder, NAME_OP_SERVICE_START_WITH_OPTS_TASK, iCreator, "taskId", "options");
        createIdOp(builder, NAME_OP_SERVICE_ACTIVATE, iCreator);
        createIdOp(builder, NAME_OP_SERVICE_PASSIVATE, iCreator);
        createIdOp(builder, NAME_OP_SERVICE_MIGRATE, iCreator, "location");
        createIdOp(builder, NAME_OP_SERVICE_UPDATE, iCreator, "location");
        createIdOp(builder, NAME_OP_SERVICE_SWITCH, iCreator, "newId");
        createIdOp(builder, NAME_OP_SERVICE_RECONF, iCreator, "values");
        createIdOp(builder, NAME_OP_SERVICE_STOP, iCreator);
        createIdOp(builder, NAME_OP_SERVICE_STOP_TASK, iCreator, "taskId");
        createIdOp(builder, NAME_OP_SERVICE_GET_STATE, iCreator);
        createIdOp(builder, NAME_OP_SERVICE_INSTANCE_COUNT, iCreator);
        createIdOp(builder, NAME_OP_SERVICE_SET_STATE, iCreator, "state");
        
        // probably relevant ops only
        builder.createOperationBuilder(NAME_OP_ARTIFACT_ADD)
            .setInvocable(iCreator.createInvocable(getQName(NAME_OP_ARTIFACT_ADD)))
            .addInputVariable("url", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .build();
        builder.createOperationBuilder(NAME_OP_SERVICE_INSTANCE_COUNT)
            .setInvocable(iCreator.createInvocable(getQName(NAME_OP_SERVICE_INSTANCE_COUNT)))
            .addInputVariable("id", Type.STRING)
            .build(Type.INTEGER);
        builder.createOperationBuilder(NAME_OP_SERVICE_STATE_COUNT)
            .setInvocable(iCreator.createInvocable(getQName(NAME_OP_SERVICE_STATE_COUNT)))
            .addInputVariable("state", Type.STRING)
            .build(Type.INTEGER);
        createIdOp(builder, NAME_OP_ARTIFACT_REMOVE, iCreator);
        builder.createPropertyBuilder(NAME_PROP_SUPPORTED_APPIDS)
            .setValue(Type.STRING, String.join(",", ServiceFactory.getSetup().getSupportedAppIds()))
            .build();
    }

    /**
     * Creates an operation with a String parameter "id" and optional string parameters and a result of type string. 
     * The operation name is derived from {@code name} applied to {@link #getQName(String)}.
     * 
     * @param smB the submodel elements collection builder
     * @param name the operation name
     * @param iCreator the invocables creator
     * @param otherParams other String parameters
     */
    private void createIdOp(SubmodelElementCollectionBuilder smB, String name, InvocablesCreator iCreator, 
        String... otherParams) {
        OperationBuilder oBuilder = smB.createOperationBuilder(name)
            .setInvocable(iCreator.createInvocable(getQName(name)))
            .addInputVariable(NAME_PROP_ID, Type.STRING);
        for (String p : otherParams) {
            oBuilder.addInputVariable(p, Type.STRING);
        }
        oBuilder.addOutputVariable("result", Type.STRING);
        oBuilder.build();
    }

    /**
     * Returns the qualified name for an operation/property implementation.
     * 
     * @param elementName the element name
     * @return the qualified name
     */
    public static String getQName(String elementName) {
        return NAME_SUBMODEL + "_" + elementName;
    }

    /**
     * Reads all params as string array.
     * 
     * @param args the arguments
     * @param index the index of the argument to read from {@code args}
     * @return the string array
     */
    private static String[] readStringArray(Object[] args, int index) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        return null == param ? new String[0] : JsonUtils.fromJson(param.toString(), String[].class);
    }
    
    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_START), 
            new JsonResultWrapper(p -> {
                ServiceFactory.getServiceManager().startService(readStringArray(p, 0)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_START_WITH_OPTS), 
            new JsonResultWrapper(p -> {
                ServiceFactory.getServiceManager().startService(readMap(p, 1, null), readStringArray(p, 0)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_ACTIVATE), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().activateService(readString(p)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_PASSIVATE), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().passivateService(readString(p)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_MIGRATE), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().migrateService(readString(p), readString(p, 1)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_UPDATE), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().updateService(readString(p), readUri(p, 1, EMPTY_URI)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_SWITCH), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().switchToService(readString(p), readString(p, 1)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_RECONF), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().reconfigureService(readString(p, 0, ""), readMap(p, 1, null)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_STOP), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().stopService(readStringArray(p, 0)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_GET_STATE), 
            new JsonResultWrapper(p -> ServiceFactory.getServiceManager().getServiceState(readString(p))
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_INSTANCE_COUNT), 
            new JsonResultWrapper(p -> ServiceFactory.getServiceManager().getServiceInstanceCount(readString(p))
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_STATE_COUNT), 
            new JsonResultWrapper(p -> getServiceStateCount(readString(p, 0, "")) 
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_SET_STATE), 
            new JsonResultWrapper(p -> { 
                ServiceState state = ServiceState.valueOf(readString(p, 1, "")); // exception -> wrapper
                ServiceFactory.getServiceManager().setServiceState(readString(p), state); 
                return null;
            }
        ));
        contributeArtifactTo(sBuilder);
        contributeTaskTo(sBuilder);
    }
    
    /**
     * Returns the number of service instances in the given state. Preliminarily in here, may be moved into the service
     * manager
     * 
     * @param state the state
     * @return the number of service instances
     */
    private static int getServiceStateCount(String state) {
        ServiceManager mgr = ServiceFactory.getServiceManager();
        ServiceState st = ServiceState.valueOf(state); // exception -> wrapper
        int count = 0;
        for (String id : mgr.getServiceIds()) {
            if (st == mgr.getServiceState(id)) {
                count++;
            }
        }
        return count; 
    }
    
    /**
     * Further operation contributions for artifacts.
     * 
     * @param sBuilder the server protocol builder
     */
    private void contributeArtifactTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_ARTIFACT_ADD), 
            new JsonResultWrapper(p -> { 
                return ServiceFactory.getServiceManager().addArtifact(readUri(p, 0, EMPTY_URI)); 
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_ARTIFACT_REMOVE), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().removeArtifact(readString(p)); 
                return null;
            }
        ));
    }

    /**
     * Further task operation contributions.
     * 
     * @param sBuilder the server protocol builder
     */
    private void contributeTaskTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_START_TASK), 
            new JsonResultWrapper(p -> {
                ServiceFactory.getServiceManager().startService(readStringArray(p, 0)); 
                return null;
            }, p -> readString(p, 1) // taskId
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_START_WITH_OPTS_TASK), 
            new JsonResultWrapper(p -> {
                ServiceFactory.getServiceManager().startService(readMap(p, 2, null), readStringArray(p, 0)); 
                return null;
            }, p -> readString(p, 1) // taskId
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_STOP_TASK), 
            new JsonResultWrapper(p -> { 
                ServiceFactory.getServiceManager().stopService(readStringArray(p, 0)); 
                return null;
            }, p -> readString(p, 1) // taskId
        ));
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    /**
     * Adds an artifact to the given submodel builder.
     * 
     * @param smB represents the submodel in creation
     * @param desc the descriptor to be added
     */
    private static void addArtifact(SubmodelBuilder smB, ArtifactDescriptor desc) {
        SubmodelElementCollectionBuilder cBuilder // get or create
            = smB.createSubmodelElementCollectionBuilder(NAME_COLL_ARTIFACTS, false, false);

        SubmodelElementCollectionBuilder dBuilder 
            = cBuilder.createSubmodelElementCollectionBuilder(fixId(desc.getId()), false, false);
        dBuilder.createPropertyBuilder(NAME_PROP_ID)
            .setValue(Type.STRING, desc.getId())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_NAME)
            .setValue(Type.STRING, desc.getName())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_RESOURCE)
            .setValue(Type.STRING, Id.getDeviceIdAas())
            .build();
        dBuilder.build();

        cBuilder.build();
    }

    /**
     * Adds an service to the given submodel builder.
     * 
     * @param smB represents the submodel in creation
     * @param desc the descriptor to be added
     */
    private static void addService(SubmodelBuilder smB, ServiceDescriptor desc) {
        SubmodelElementCollectionBuilder serviceBuilder 
            = smB.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICES, false, false);

        // Ref to artifact
        SubmodelElementCollectionBuilder descriptorBuilder 
            = serviceBuilder.createSubmodelElementCollectionBuilder(fixId(desc.getId()), false, false);
        descriptorBuilder.createPropertyBuilder(NAME_PROP_ID)
            .setValue(Type.STRING, desc.getId())
            .build();
        descriptorBuilder.createPropertyBuilder(NAME_PROP_APPLICATION_ID)
            .setValue(Type.STRING, desc.getApplicationId())
            .build();
        if (null != desc.getArtifact()) { // defensive
            descriptorBuilder.createPropertyBuilder(NAME_PROP_ARTIFACT)
                .setValue(Type.STRING, desc.getArtifact().getId())
                .build();
        }
        descriptorBuilder.createPropertyBuilder(NAME_PROP_TOPLEVEL)
            .setValue(Type.BOOLEAN, desc.isTopLevel())
            .build();
        descriptorBuilder.createPropertyBuilder(NAME_PROP_NAME)
            .setValue(Type.STRING, desc.getName())
            .build();
        descriptorBuilder.createPropertyBuilder(NAME_PROP_STATE)
            .setValue(Type.STRING, desc.getState().toString())
            .build();
        descriptorBuilder.createPropertyBuilder(NAME_PROP_KIND)
            .setValue(Type.STRING, desc.getKind().toString())
            .build();
        descriptorBuilder.createPropertyBuilder(NAME_PROP_VERSION)
            .setValue(Type.STRING, desc.getVersion().toString())
            .build();
        descriptorBuilder.createPropertyBuilder(NAME_PROP_DESCRIPTION)
            .setValue(Type.STRING, desc.getDescription())
            .build();
        descriptorBuilder.createPropertyBuilder(NAME_PROP_RESOURCE)
            .setValue(Type.STRING, Id.getDeviceIdAas())
            .build();
        Registry reg = AasPartRegistry.getIipAasRegistry();
        AasPartRegistry.addServiceAasEndpointProperty(reg, descriptorBuilder, NAME_PROP_SERVICE_AAS, desc.getId());
        AasPartRegistry.addDeviceAasEndpointProperty(reg, descriptorBuilder, NAME_PROP_DEVICE_AAS, Id.getDeviceIdAas());
        addTypedData(descriptorBuilder, NAME_SUBCOLL_PARAMETERS, desc.getParameters());
        addTypedData(descriptorBuilder, NAME_SUBCOLL_INPUT_DATA_CONN, desc.getInputDataConnectors());
        addTypedData(descriptorBuilder, NAME_SUBCOLL_OUTPUT_DATA_CONN, desc.getOutputDataConnectors());
        
        descriptorBuilder.build();
        
        serviceBuilder.build();
        Transport.sendServiceStatus(ActionTypes.ADDED, desc.getId());
    }
    
    /**
     * Adds a typed data submodel elements collection to the given {@code builder}.
     * 
     * @param builder the builder to use as parent
     * @param name the name of the collection
     * @param descriptors the descriptors to add to the parent collection
     */
    private static void addTypedData(SubmodelElementCollectionBuilder builder, String name, 
        List<? extends TypedDataDescriptor> descriptors) {
        SubmodelElementCollectionBuilder pBuilder 
            = builder.createSubmodelElementCollectionBuilder(fixId(name), false, false);
        for (TypedDataDescriptor d : descriptors) {
            SubmodelElementCollectionBuilder dBuilder 
                = builder.createSubmodelElementCollectionBuilder(fixId(d.getName()), false, false);
            dBuilder.createPropertyBuilder(NAME_PROP_NAME)
                .setValue(Type.STRING, d.getName())
                .build();
            dBuilder.createPropertyBuilder(NAME_PROP_DESCRIPTION)
                .setValue(Type.STRING, d.getDescription())
                .build();
            if (null != d.getType()) {
                ClassUtility.addTypeSubModelElement(dBuilder, NAME_PROP_TYPE, d.getType());
            }
            dBuilder.build();
        }
        pBuilder.build();
    }

    /**
     * Adds data to {@link #NAME_COLL_RELATIONS}.
     * 
     * @param builder the builder to use as parent
     * @param descriptors the descriptors to add to the parent collection
     * @param input whether we are processing input or output descriptors
     * @param serviceRef the reference to the using service
     */
    private static void addRelationData(SubmodelElementCollectionBuilder builder, 
        List<? extends TypedDataConnectorDescriptor> descriptors, boolean input, Reference serviceRef) {
        for (TypedDataConnectorDescriptor d : descriptors) {
            SubmodelElementCollectionBuilder dBuilder 
                = builder.createSubmodelElementCollectionBuilder(fixId(d.getName()), false, false);
            String name = input ? NAME_PROP_TO : NAME_PROP_FROM;
            dBuilder.createReferenceElementBuilder(name)
                .setValue(serviceRef)
                .build();
            name = input ? NAME_PROP_TO_RESOURCE : NAME_PROP_FROM_RESOURCE;
            dBuilder.createPropertyBuilder(name)
                .setValue(Type.STRING, Id.getDeviceIdAas())
                .build();
            dBuilder.defer();
        }
    }
    
    /**
     * Returns an availability predicate functor to determine whether typed data connector descriptors do exist in 
     * the AAS.
     * 
     * @param timeout the timeout in ms within the request shall be repeated without directly failing
     * @param retryDelay time delay in ms after which a failed request shall be re-tried
     * @param input whether input or output side of the relation shall be queried
     * @return the predictate for testing
     */
    public static Predicate<TypedDataConnectorDescriptor> createAvailabilityPredicate(int timeout, int retryDelay, 
        boolean input) {
        return new Predicate<TypedDataConnectorDescriptor>() {

            private SubmodelElementCollection rels;
            
            @Override
            public boolean test(TypedDataConnectorDescriptor conn) {
                boolean found = false;
                long start = System.currentTimeMillis();
                do {
                    if (null == rels) {
                        try {
                            Submodel submodel = ActiveAasBase.getSubmodel(ServicesAas.NAME_SUBMODEL);
                            rels = submodel.getSubmodelElementCollection(ServicesAas.NAME_COLL_RELATIONS);
                        } catch (IOException e) {
                        }
                    }
                    if (null != rels) {
                        rels.update(); // entries in collection may change, force update
                        SubmodelElementCollection rel = rels.getSubmodelElementCollection(fixId(conn.getName()));
                        if (null != rel) {
                            String name = input ? NAME_PROP_TO : NAME_PROP_FROM;
                            found = (null != rel.getReferenceElement(name));
                        }
                    }
                    if (!found && timeout > 0) {
                        TimeUtils.sleep(retryDelay);
                    }
                } while (!found && System.currentTimeMillis() - start < timeout);
                return found;
            }
        };
    }
    
    /**
     * Is called when an artifact is added.
     * 
     * @param desc the artifact descriptor 
     */
    public static void notifyArtifactAdded(ArtifactDescriptor desc) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            SubmodelBuilder builder = aas.createSubmodelBuilder(NAME_SUBMODEL, ID_SUBMODEL);
            addArtifact(builder, desc);
            for (ServiceDescriptor s : desc.getServices()) {
                addService(builder, s);
            }
            for (ServiceDescriptor s : desc.getServers()) {
                addService(builder, s);
            }
        });
        Transport.sendServiceArtifactStatus(ActionTypes.ADDED, desc.getId());
    }

    /**
     * Is called when an artifact is removed.
     * 
     * @param desc the artifact descriptor 
     */
    public static void notifyArtifactRemoved(ArtifactDescriptor desc) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            removeArtifact(sub, desc);
        });
    }
    
    /**
     * Removes the artifact {@code desc} from the submodel {@code sub}. Removes also all services.
     * 
     * @param sub the submodel
     * @param desc the artifact descriptor to remove
     */
    private static void removeArtifact(Submodel sub, ArtifactDescriptor desc) {
        SubmodelElementCollection coll = sub.getSubmodelElementCollection(NAME_COLL_SERVICES);
        for (String sId : desc.getServiceIds()) {
            coll.deleteElement(fixId(sId));
        }
        coll = sub.getSubmodelElementCollection(NAME_COLL_ARTIFACTS);
        coll.deleteElement(fixId(desc.getId()));
        coll = sub.getSubmodelElementCollection(NAME_COLL_RELATIONS);
        for (ServiceDescriptor s : desc.getServices()) {
            removeRelations(s, sub, coll);
        }
        Transport.sendServiceArtifactStatus(ActionTypes.REMOVED, desc.getId());
    }
    
    /**
     * Called when the service manager disappears. 
     */
    public static void notifyManagerRemoved() {
        ActiveAasBase.processNotification(NAME_SUBMODEL, NotificationMode.SYNCHRONOUS, (sub, aas) -> {
            ServiceManager mgr = ServiceFactory.getServiceManager();
            for (ArtifactDescriptor a : mgr.getArtifacts()) {
                removeArtifact(sub, a);
            }
        });
    }

    /**
     * Remove the relations for {@code service}.
     * 
     * @param service the service
     * @param sub the submodel for {@link #NAME_SUBMODEL}.
     * @param coll the {@link #NAME_COLL_RELATIONS relations} submodel elements collection, may be <b>null</b> then
     *   {@code sub} is queried for the collection
     * @return {@code coll} or the queried collection
     */
    private static SubmodelElementCollection removeRelations(ServiceDescriptor service, Submodel sub, 
        SubmodelElementCollection coll) {
        if (null == coll) {
            coll = sub.getSubmodelElementCollection(NAME_COLL_RELATIONS);
        }
        for (TypedDataConnectorDescriptor c : service.getInputDataConnectors()) {
            deleteSubmodelElement(coll, c.getName(), NAME_PROP_TO);
            deleteSubmodelElement(coll, c.getName(), NAME_PROP_TO_RESOURCE);
        }
        for (TypedDataConnectorDescriptor c : service.getOutputDataConnectors()) {
            deleteSubmodelElement(coll, c.getName(), NAME_PROP_FROM);
            deleteSubmodelElement(coll, c.getName(), NAME_PROP_FROM_RESOURCE);
        }
        return coll;
    }

    /**
     * Safely deletes a submodel element in a nested collection.
     * 
     * @param coll the parent collection
     * @param name name the child collection
     * @param elt element the element to delete
     */
    private static void deleteSubmodelElement(SubmodelElementCollection coll, String name, String elt) {
        SubmodelElementCollection c = coll.getSubmodelElementCollection(fixId(name));
        String eltId = fixId(elt);
        if (null != c && c.getElement(eltId) != null) {
            c.deleteElement(eltId);
        }
    }

    /**
     * Is called when a service state changed.
     * 
     * @param old the previous state before the change
     * @param act the actual state after the change
     * @param desc the service descriptor (depending on implementation, may have the new state or not)
     */
    public static void notifyServiceStateChanged(ServiceState old, ServiceState act, ServiceDescriptor desc) {
        notifyServiceStateChanged(old, act, desc, null);
    }
    
    /**
     * Is called when a service state changed.
     * 
     * @param old the previous state before the change
     * @param act the actual state after the change
     * @param desc the service descriptor (depending on implementation, may have the new state or not)
     * @param mode explicit notification mode to be used (if <b>null</b>, use the mode defined 
     *     in {@link ActiveAasBase})
     */
    public static void notifyServiceStateChanged(ServiceState old, ServiceState act, ServiceDescriptor desc, 
        NotificationMode mode) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, mode, (sub, aas) -> {
            // other approach... link property against service descriptor while creation and reflect state
            // let's try this one for now
            SubmodelElementCollection services = sub.getSubmodelElementCollection(NAME_COLL_SERVICES);
            SubmodelElementCollection elt = services.getSubmodelElementCollection(fixId(desc.getId()));
            if (null != elt) {
                Property prop = elt.getProperty(NAME_PROP_STATE);
                if (null != prop) {
                    try {
                        prop.setValue(act.toString());
                    } catch (ExecutionException e) {
                        getLogger().error("Cannot write state for service `{}`: {}", desc.getId(), e.getMessage());
                    }
                } else {
                    getLogger().error("Service state change - cannot find property {} for service `{}`", 
                        NAME_PROP_STATE, desc.getId());
                }
            } else {
                getLogger().error("Service state change - cannot find service `{}`", desc.getId());
            }
            // synchronous execution needed??
            getLogger().info("Handling service state change `{}`: {} -> {}", desc.getId(), old, act);
            if (ServiceState.AVAILABLE == old && ServiceState.STARTING == act) {
                registerMetrics(desc, sub, elt);
                Transport.sendServiceStatus(ActionTypes.CHANGED, desc.getId());
            } else if (ServiceState.STARTING == old && ServiceState.RUNNING == act) {
                setupRelations(desc, sub, elt);
                Transport.sendServiceStatus(ActionTypes.CHANGED, desc.getId());
            } else if ((ServiceState.RUNNING == old  || ServiceState.FAILED == old) 
                && ServiceState.STOPPED == act) {
                removeRelations(desc, sub, null);
                Transport.sendServiceStatus(ActionTypes.REMOVED, desc.getId());
            } else if ((ServiceState.RUNNING == old  || ServiceState.FAILED == old) 
                && ServiceState.STOPPING == act) {
                MetricsAasConstructor.removeProviderMetricsFromAasSubmodel(elt);
                Transport.sendServiceStatus(ActionTypes.CHANGED, desc.getId());
            } else if (old != act) {
                Transport.sendServiceStatus(ActionTypes.CHANGED, desc.getId());
            }
        });
    }
    
    /**
     * Registers metrics for a changed service.
     * 
     * @param desc the service descriptor
     * @param sub the submodel
     * @param elt the element representing the service
     */
    private static void registerMetrics(ServiceDescriptor desc, Submodel sub, SubmodelElementCollection elt) {
        if (!MetricsAasConstructor.containsMetrics(elt)) {
            SubmodelElementCollectionBuilder serviceB = 
                    sub.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICES, false, false);
            SubmodelElementCollectionBuilder subB =
                serviceB.createSubmodelElementCollectionBuilder(fixId(desc.getId()), false, false);
            
            String devId = Id.getDeviceId();
            TransportSetup tSetup = ServiceFactory.getTransport();
            MetricsAasConstructor.addProviderMetricsToAasSubmodel(subB, null, 
                MetricsAasConstants.TRANSPORT_SERVICE_METRICS_CHANNEL, devId, tSetup);
            MetricsAasConstructor.addServiceMetricsToAasSubmodel(subB, null, 
                MetricsAasConstants.TRANSPORT_SERVICE_METRICS_CHANNEL, devId, tSetup);

            subB.build();
        }
    }
    
    /**
     * Sets up the relations for a starting service.
     * 
     * @param desc the service descriptor
     * @param sub the submodel
     * @param elt the element representing the service
     */
    private static void setupRelations(ServiceDescriptor desc, Submodel sub, SubmodelElementCollection elt) {
        Reference serviceRef = elt.createReference();
        SubmodelElementCollectionBuilder connectionBuilder 
            = sub.createSubmodelElementCollectionBuilder(NAME_COLL_RELATIONS, false, false); // create or get
        addRelationData(connectionBuilder, desc.getInputDataConnectors(), true, serviceRef);
        addRelationData(connectionBuilder, desc.getOutputDataConnectors(), false, serviceRef);
        connectionBuilder.build();
    }

    /**
     * Removes a specific device.
     * 
     * @param deviceId the device id
     * @see #removeDevice(Submodel, String)
     */
    public static void removeDevice(String deviceId) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, NotificationMode.SYNCHRONOUS, (sub, aas) -> {
            removeDevice(sub, deviceId);
        });
    }

    /**
     * Removes a specific device from {@code sub}.
     * 
     * @param sub the submodel to delete from
     * @param deviceId the device id
     */
    public static void removeDevice(Submodel sub, String deviceId) {
        String aasDeviceId = fixId(deviceId);

        Predicate<SubmodelElementCollection> pred = ActiveAasBase.createPropertyPredicate(NAME_PROP_RESOURCE, 
            aasDeviceId, "While deleting resource " + deviceId);
        SubmodelElementCollection coll = sub.getSubmodelElementCollection(NAME_COLL_ARTIFACTS);
        ActiveAasBase.clearCollection(coll, pred);
        coll = sub.getSubmodelElementCollection(NAME_COLL_SERVICES);
        ActiveAasBase.clearCollection(coll, pred);
        coll = sub.getSubmodelElementCollection(NAME_COLL_RELATIONS);
        clearRelations(coll, aasDeviceId, deviceId);
    }

    /**
     * Clears a relations collection from elements with the given resource property and value {@code aasDeviceId}.
     * 
     * @param coll the collection to be cleared
     * @param aasDeviceId the AAS device id
     * @param deviceId the readable device ID
     */
    private static void clearRelations(SubmodelElementCollection coll, String aasDeviceId, String deviceId) {
        if (null != coll) {
            for (SubmodelElement e: coll.elements()) {
                if (e instanceof SubmodelElementCollection) {
                    SubmodelElementCollection ec = (SubmodelElementCollection) e;
                    try {
                        if (aasDeviceId.equals(ec.getProperty(NAME_PROP_FROM_RESOURCE).getValue())) {
                            ec.deleteElement(NAME_PROP_FROM_RESOURCE);
                            ec.deleteElement(NAME_PROP_FROM);
                        } else if (aasDeviceId.equals(ec.getProperty(NAME_PROP_TO_RESOURCE).getValue())) {
                            ec.deleteElement(NAME_PROP_TO_RESOURCE);
                            ec.deleteElement(NAME_PROP_TO);
                        }
                    } catch (ExecutionException ex) {
                        LoggerFactory.getLogger(ServicesAas.class).error("While deleting device {}: {} ", 
                            deviceId, ex.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * Sets the cleanup flag to {@code true}.
     * 
     * @param sm the submodel to set the cleanup flag on
     * @param deviceId the device ID of the device to set into cleanup
     */
    public static void setCleanup(Submodel sm, String deviceId) {
        String aasDeviceId = fixId(deviceId);
        SubmodelElementCollection sec = sm.getSubmodelElementCollection(aasDeviceId);
        if (null != sec) {
            Property prop = sec.getProperty(NAME_PROP_IN_CLEANUP);
            if (null != prop) {
                try {
                    prop.setValue(true);
                } catch (ExecutionException e) {
                    LoggerFactory.getLogger(ServicesAas.class).warn("Cannot set {}:{}", 
                            NAME_PROP_IN_CLEANUP, e.getMessage());
                }
            } // if not there, we are anyway in cleanup
        }
    }
    
    /**
     * Defers the creation of {@code smBuilder} if the device is still in cleanup.
     * 
     * @param smBuilder the device submodel builder
     */
    public static void deferForCleanup(SubmodelElementCollectionBuilder smBuilder) {
        boolean inCleanup = false;
        PropertyBuilder builder = smBuilder.createPropertyBuilder(NAME_PROP_IN_CLEANUP);
        do {
            try {
                Object tmp = builder.getValue();
                if (tmp instanceof Boolean) {
                    inCleanup = (Boolean) tmp;
                    if (inCleanup) {
                        LoggerFactory.getLogger(ServicesAas.class).info(
                            "Device in cleanup. Deferring AAS creation by 500 ms.");
                        TimeUtils.sleep(500);
                    }
                }
            } catch (ExecutionException e) {
                // assume that there is no AAS, ok to go on
            }
        } while (inCleanup);
        builder.setValue(Type.BOOLEAN, false); // finally, we are not in cleanup (anymore)
    }
    
    // TODO EcsAAS/ServiceAAS deferForCleanup in buildup

    /**
     * Returns the logger instance.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ServicesAas.class);
    }

    @Override
    public boolean isValid() {
        // if the Jar is present, the contributor will be executed although the factory may not be there (optional)
        // also relevant if used as library only, e.g., in platform
        return ServiceFactory.getServiceManager() != null; 
    }

}
