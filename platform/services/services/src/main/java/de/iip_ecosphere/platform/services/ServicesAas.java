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
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ClassUtility;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the AAS for the services. Container ids used as short AAS ids may be translated into ids that are
 * valid from the perspective of the AAS implementation. All nested elements also carry their original id in 
 * {@link #NAME_PROP_ID}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServicesAas implements AasContributor {

    public static final String NAME_SUBMODEL = "services";
    public static final String NAME_COLL_ARTIFACTS = "artifacts";
    public static final String NAME_COLL_SERVICES = "services";
    public static final String NAME_COLL_PARAMETERS = "parameters";
    public static final String NAME_COLL_INPUT_DATA_CONN = "inputDataConnectors";
    public static final String NAME_COLL_OUTPUT_DATA_CONN = "outputDataConnectors";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_STATE = "state";
    public static final String NAME_PROP_KIND = "kind";
    public static final String NAME_PROP_VERSION = "version";
    public static final String NAME_PROP_DESCRIPTION = "description";
    public static final String NAME_PROP_TYPE = "type";
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
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, ID_SUBMODEL);
        // ensure that these two do exist
        smB.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICES, false, false).build();
        smB.createSubmodelElementCollectionBuilder(NAME_COLL_ARTIFACTS, false, false).build();

        for (ArtifactDescriptor a : mgr.getArtifacts()) {
            addArtifact(smB, a);
        }
        for (ServiceDescriptor s : mgr.getServices()) {
            addService(smB, s);
        }

        // probably relevant ops only
        createIdOp(smB, NAME_OP_SERVICE_START, iCreator);
        createIdOp(smB, NAME_OP_SERVICE_ACTIVATE, iCreator);
        createIdOp(smB, NAME_OP_SERVICE_PASSIVATE, iCreator);
        createIdOp(smB, NAME_OP_SERVICE_MIGRATE, iCreator, "location");
        createIdOp(smB, NAME_OP_SERVICE_UPDATE, iCreator, "location");
        createIdOp(smB, NAME_OP_SERVICE_SWITCH, iCreator, "newId");
        createIdOp(smB, NAME_OP_SERVICE_RECONF, iCreator, "values");
        createIdOp(smB, NAME_OP_SERVICE_STOP, iCreator);
        createIdOp(smB, NAME_OP_SERVICE_GET_STATE, iCreator);
        createIdOp(smB, NAME_OP_SERVICE_SET_STATE, iCreator, "state");
        
        // probably relevant ops only
        smB.createOperationBuilder(NAME_OP_ARTIFACT_ADD)
            .setInvocable(iCreator.createInvocable(getQName(NAME_OP_ARTIFACT_ADD)))
            .addInputVariable("url", Type.STRING)
            .addOutputVariable("result", Type.STRING)
            .build();
        createIdOp(smB, NAME_OP_ARTIFACT_REMOVE, iCreator);

        smB.build();
        return null;
    }

    /**
     * Creates an operation with a String parameter "id" and optional string parameters and a result of type string. 
     * The operation name is derived from {@code name} applied to {@link #getQName(String)}.
     * 
     * @param smB the submodel builder
     * @param name the operation name
     * @param iCreator the invocables creator
     * @param otherParams other String parameters
     */
    private void createIdOp(SubmodelBuilder smB, String name, InvocablesCreator iCreator, String... otherParams) {
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
                ServiceFactory.getServiceManager().migrateService(readString(p), readUri(p, 1, EMPTY_URI)); 
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
        SubmodelElementCollectionBuilder cBuilder 
            = smB.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICES, false, false);
// Ref to artifact
        SubmodelElementCollectionBuilder dBuilder 
            = cBuilder.createSubmodelElementCollectionBuilder(fixId(desc.getId()), false, false);
        dBuilder.createPropertyBuilder(NAME_PROP_ID)
            .setValue(Type.STRING, desc.getId())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_NAME)
            .setValue(Type.STRING, desc.getName())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_STATE)
            .setValue(Type.STRING, desc.getState().toString())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_KIND)
            .setValue(Type.STRING, desc.getKind().toString())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_VERSION)
            .setValue(Type.STRING, desc.getVersion().toString())
            .build();
        dBuilder.createPropertyBuilder(NAME_PROP_DESCRIPTION)
            .setValue(Type.STRING, desc.getDescription())
            .build();
        
        addTypedData(dBuilder, NAME_COLL_PARAMETERS, desc.getParameters());
        addTypedData(dBuilder, NAME_COLL_INPUT_DATA_CONN, desc.getInputDataConnectors());
        addTypedData(dBuilder, NAME_COLL_OUTPUT_DATA_CONN, desc.getInputDataConnectors());
        
        dBuilder.build();
        
        cBuilder.build();
    }

    /**
     * Adds a typed data submodel elements collection to the given {@code builder}.
     * 
     * @param builder the builder to use as parent
     * @param name the name of the collection
     * @param descriptors the descriptors to add to the collection
     */
    private static void addTypedData(SubmodelElementCollectionBuilder builder, String name, 
        List<? extends TypedDataDescriptor> descriptors) {
        SubmodelElementCollectionBuilder pBuilder 
            = builder.createSubmodelElementCollectionBuilder(fixId(name), false, false);
        for (TypedDataDescriptor d : descriptors) {
            pBuilder.createPropertyBuilder(NAME_PROP_NAME)
                .setValue(Type.STRING, d.getName())
                .build();
            pBuilder.createPropertyBuilder(NAME_PROP_DESCRIPTION)
                .setValue(Type.STRING, d.getDescription())
                .build();
            if (null != d.getType()) {
                ClassUtility.addTypeSubModelElement(pBuilder, NAME_PROP_TYPE, d.getType());
            }
        }
        pBuilder.build();
    }

    /**
     * Reads the {@code index} argument from {@code} args as map of strings.
     * 
     * @param args the array to take the value from 
     * @param index the 0-based index into {@code} args
     * @param dflt default value if the {@code index} is wrong, there is no value/null ...
     * @return the map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> readMap(Object[] args, int index, Map<String, String> dflt) {
        Object param = index >= 0 && index < args.length ? args[index] : null;
        Map<String, String> result = dflt;
        if (null != param) {
            result = JsonUtils.fromJson(result, Map.class);
        }
        return result;
    }
    
    /**
     * Writes a map to JSON.
     * 
     * @param map the map
     * @return the JSON representation
     */
    public static String writeMap(Map<String, String> map) {
        return JsonUtils.toJson(map);
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
        });
    }
    
    /**
     * Is called when a service state changed.
     * 
     * @param desc the service descriptor 
     */
    public static void notifyServiceStateChanged(ServiceDescriptor desc) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            // other approach... link property against service descriptor while creation and reflect state
            // let's try this one for now
            SubmodelElementCollection elt = sub.getSubmodelElementCollection(NAME_COLL_SERVICES)
                .getSubmodelElementCollection(fixId(desc.getId()));
            if (null != elt) {
                Property prop = elt.getProperty(NAME_PROP_STATE);
                if (null != prop) {
                    try {
                        prop.setValue(desc.getState().toString());
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

}
