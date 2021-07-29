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
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;
import de.iip_ecosphere.platform.support.iip_aas.ClassUtility;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;

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

    public static final String NAME_SUBMODEL = "services";
    public static final String NAME_SUBMODEL_RESOURCES = AasPartRegistry.NAME_SUBMODEL_RESOURCES;
    public static final String NAME_COLL_ARTIFACTS = "artifacts";
    public static final String NAME_COLL_SERVICES = "services";
    public static final String NAME_COLL_RELATIONS = "relations";
    public static final String NAME_SUBCOLL_PARAMETERS = "parameters";
    public static final String NAME_SUBCOLL_INPUT_DATA_CONN = "inputDataConnectors";
    public static final String NAME_SUBCOLL_OUTPUT_DATA_CONN = "outputDataConnectors";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_STATE = "state";
    public static final String NAME_PROP_KIND = "kind";
    public static final String NAME_PROP_VERSION = "version";
    public static final String NAME_PROP_DESCRIPTION = "description";
    public static final String NAME_PROP_TYPE = "type";
    public static final String NAME_PROP_RESOURCE = "resource";
    public static final String NAME_PROP_FROM = "from";
    public static final String NAME_PROP_TO = "to";
    public static final String NAME_PROP_ARTIFACT = "artifact";
    public static final String NAME_OP_SERVICE_START = "startService";
    public static final String NAME_OP_SERVICE_ACTIVATE = "activateService";
    public static final String NAME_OP_SERVICE_PASSIVATE = "passivateService";
    public static final String NAME_OP_SERVICE_MIGRATE = "migrateService";
    public static final String NAME_OP_SERVICE_UPDATE = "updateService";
    public static final String NAME_OP_SERVICE_SWITCH = "switchToService";
    public static final String NAME_OP_SERVICE_RECONF = "reconfigureService";
    public static final String NAME_OP_SERVICE_STOP = "stopService";
    public static final String NAME_OP_SERVICE_GET_STATE = "getServiceSate";
    public static final String NAME_OP_SERVICE_SET_STATE = "setServiceSate";
    public static final String NAME_OP_ARTIFACT_ADD = "addArtifact";
    public static final String NAME_OP_ARTIFACT_REMOVE = "removeArtifact";
    
    private static final String ID_SUBMODEL = null; // take the short name, shall become public and an URN later
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        ServiceManager mgr = ServiceFactory.getServiceManager();
        if (null != mgr) { // this shall not be needed, but if the Jar is present, the contributor will be executed 
            // operations contribute to the operation of the underlying resource (Service JVM or ECS Runtime JVM)
            SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL_RESOURCES, ID_SUBMODEL);
            SubmodelElementCollectionBuilder jB 
                = smB.createSubmodelElementCollectionBuilder(Id.getDeviceIdAas(), false, false);
        
            // probably relevant ops only
            createIdOp(jB, NAME_OP_SERVICE_START, iCreator);
            createIdOp(jB, NAME_OP_SERVICE_ACTIVATE, iCreator);
            createIdOp(jB, NAME_OP_SERVICE_PASSIVATE, iCreator);
            createIdOp(jB, NAME_OP_SERVICE_MIGRATE, iCreator, "location");
            createIdOp(jB, NAME_OP_SERVICE_UPDATE, iCreator, "location");
            createIdOp(jB, NAME_OP_SERVICE_SWITCH, iCreator, "newId");
            createIdOp(jB, NAME_OP_SERVICE_RECONF, iCreator, "values");
            createIdOp(jB, NAME_OP_SERVICE_STOP, iCreator);
            createIdOp(jB, NAME_OP_SERVICE_GET_STATE, iCreator);
            createIdOp(jB, NAME_OP_SERVICE_SET_STATE, iCreator, "state");
            
            // probably relevant ops only
            jB.createOperationBuilder(NAME_OP_ARTIFACT_ADD)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_ARTIFACT_ADD)))
                .addInputVariable("url", Type.STRING)
                .addOutputVariable("result", Type.STRING)
                .build();
            createIdOp(jB, NAME_OP_ARTIFACT_REMOVE, iCreator);
            jB.build();
    
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
     * @param params the params
     * @return the string array
     */
    private static String[] readStringArray(Object[] params) {
        String[] args = new String[params.length];
        for (int a = 0; a < args.length; a++) {
            args[a] = readString(params, a, "");
        }
        return args;
    }
    
    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_START), 
            new JsonResultWrapper(p -> {
                ServiceFactory.getServiceManager().startService(readStringArray(p)); 
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
                ServiceFactory.getServiceManager().stopService(readStringArray(p)); 
                return null;
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_GET_STATE), 
            new JsonResultWrapper(p -> { 
                return ServiceFactory.getServiceManager().getServiceState(readString(p)); 
            }
        ));
        sBuilder.defineOperation(getQName(NAME_OP_SERVICE_SET_STATE), 
            new JsonResultWrapper(p -> { 
                ServiceState state = ServiceState.valueOf(readString(p, 1, "")); // exception shall be caught by wrapper
                ServiceFactory.getServiceManager().setServiceState(readString(p), state); 
                return null;
            }
        ));

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
        if (null != desc.getArtifact()) { // defensive
            descriptorBuilder.createPropertyBuilder(NAME_PROP_ARTIFACT)
                .setValue(Type.STRING, desc.getArtifact().getId())
                .build();
        }
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
        
        addTypedData(descriptorBuilder, NAME_SUBCOLL_PARAMETERS, desc.getParameters());
        addTypedData(descriptorBuilder, NAME_SUBCOLL_INPUT_DATA_CONN, desc.getInputDataConnectors());
        addTypedData(descriptorBuilder, NAME_SUBCOLL_OUTPUT_DATA_CONN, desc.getOutputDataConnectors());
        
        descriptorBuilder.build();
        
        serviceBuilder.build();
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
            builder.build();
        });
    }

    /**
     * Is called when an artifact is removed.
     * 
     * @param desc the artifact descriptor 
     */
    public static void notifyArtifactRemoved(ArtifactDescriptor desc) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
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
        }
        for (TypedDataConnectorDescriptor c : service.getOutputDataConnectors()) {
            deleteSubmodelElement(coll, c.getName(), NAME_PROP_FROM);
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
     *     in {@link AbstractAasBase})
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
                        getLogger().error("Cannot write state for service `" + desc.getId() + "`: " + e.getMessage());
                    }
                } else {
                    getLogger().error("Service state change - cannot find property " + NAME_PROP_STATE 
                        + "for service `" + desc.getId());
                }
            } else {
                getLogger().error("Service state change - cannot find service `" + desc.getId() + "`");
            }
            // synchronous execution needed??
            if (ServiceState.AVAILABLE == old && ServiceState.RUNNING == act) {
                Reference serviceRef = elt.createReference();
                SubmodelElementCollectionBuilder connectionBuilder 
                    = sub.createSubmodelElementCollectionBuilder(NAME_COLL_RELATIONS, false, false); // create or get
                addRelationData(connectionBuilder, desc.getInputDataConnectors(), true, serviceRef);
                addRelationData(connectionBuilder, desc.getOutputDataConnectors(), false, serviceRef);
                connectionBuilder.build();
                if (!MetricsAasConstructor.containsMetrics(elt)) {
                    SubmodelElementCollectionBuilder serviceB = 
                            sub.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICES, false, false);
                    SubmodelElementCollectionBuilder subB =
                        serviceB.createSubmodelElementCollectionBuilder(fixId(desc.getId()), false, false);
                    MetricsAasConstructor.addProviderMetricsToAasSubmodel(subB, null, 
                        MetricsAasConstants.TRANSPORT_SERVICE_METRICS_CHANNEL, 
                        Id.getDeviceId(), ServiceFactory.getTransport());
                    subB.build();
                }
            } else if ((ServiceState.RUNNING == old  || ServiceState.FAILED == old) 
                && ServiceState.STOPPED == act) {
                removeRelations(desc, sub, null);
            } else if ((ServiceState.RUNNING == old  || ServiceState.FAILED == old) 
                && ServiceState.STOPPING == act) {
                MetricsAasConstructor.removeProviderMetricsFromAasSubmodel(elt);
            }
        });
    }

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
