/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.platform;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.fixId;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.platform.ArtifactsManager.Artifact;
import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasUtils;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationInstanceAasConstructor;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.transport.status.TaskUtils;

/**
 * The platform AAS contributor, in particular for the available artifacts.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAas implements AasContributor {

    public static final String NAME_SUBMODEL_ARTIFACTS = "Artifacts";
    public static final String NAME_SUBMODEL_STATUS = "Status";
    public static final String NAME_COLL_SERVICE_ARTIFACTS = "ServiceArtifacts";
    public static final String NAME_COLL_CONTAINER = "Container";
    public static final String NAME_COLL_DEPLOYMENT_PLANS = "DeploymentPlans";
    public static final String NAME_COLL_KNOWN_SERVICES = "KnownServices";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_DESCRIPTION = "description";
    public static final String NAME_PROP_URI = "uri";
    
    public static final String NAME_OPERATION_DEPLOY = "deployPlan";
    public static final String NAME_OPERATION_UNDEPLOY = "undeployPlan";
    public static final String NAME_OPERATION_UNDEPLOY_WITHID = "undeployPlanWithId";
    public static final String NAME_OPERATION_DEPLOY_ASYNC = "deployPlanAsync";
    public static final String NAME_OPERATION_UNDEPLOY_ASYNC = "undeployPlanAsync";
    public static final String NAME_OPERATION_UNDEPLOY_WITHID_ASYNC = "undeployPlanWithIdAsync";
    public static final String NAME_OPERATION_GET_TASK_STATUS = "getTaskStatus";
    
    private static final String PROGRESS_COMPONENT_ID = "IIP-Ecosphere Platform";
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL_ARTIFACTS, null);

        smB.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICE_ARTIFACTS, false, false).build();
        smB.createSubmodelElementCollectionBuilder(NAME_COLL_CONTAINER, false, false).build();
        smB.createSubmodelElementCollectionBuilder(NAME_COLL_DEPLOYMENT_PLANS, false, false).build();
        SubmodelElementCollectionBuilder b = smB.createSubmodelElementCollectionBuilder(
            NAME_COLL_KNOWN_SERVICES, false, false);
        for (Map.Entry<String, String> ep : ServiceAas.createAas().entrySet()) {
            b.createPropertyBuilder(fixId(ep.getKey()))
                .setValue(Type.STRING, ep.getValue())
                .build();
        }
        b.build();

        smB.createOperationBuilder(NAME_OPERATION_DEPLOY)
            .addInputVariable("url", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_DEPLOY))
            .build(Type.STRING);
        smB.createOperationBuilder(NAME_OPERATION_UNDEPLOY)
            .addInputVariable("url", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UNDEPLOY))
            .build(Type.NONE);
        smB.createOperationBuilder(NAME_OPERATION_UNDEPLOY_WITHID)
            .addInputVariable("url", Type.STRING)
            .addInputVariable("instanceId", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UNDEPLOY))
            .build(Type.NONE);
        smB.createOperationBuilder(NAME_OPERATION_DEPLOY_ASYNC)
            .addInputVariable("url", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_DEPLOY_ASYNC))
            .build(Type.STRING);
        smB.createOperationBuilder(NAME_OPERATION_UNDEPLOY_ASYNC)
            .addInputVariable("url", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UNDEPLOY_ASYNC))
            .build(Type.STRING);
        smB.createOperationBuilder(NAME_OPERATION_UNDEPLOY_WITHID_ASYNC)
            .addInputVariable("url", Type.STRING)
            .addInputVariable("instanceId", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UNDEPLOY_ASYNC))
            .build(Type.STRING);
        smB.createOperationBuilder(NAME_OPERATION_GET_TASK_STATUS)
            .addInputVariable("taskId", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_GET_TASK_STATUS))
            .build(Type.STRING);
        smB.build();
        
        // just that they are there
        aasBuilder.createSubmodelBuilder(NAME_SUBMODEL_STATUS, null).build();
        aasBuilder.createSubmodelBuilder(ApplicationInstanceAasConstructor.NAME_SUBMODEL_APPINSTANCES, null).build();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(NAME_OPERATION_DEPLOY, new JsonResultWrapper(p -> { 
            return deployPlan(AasUtils.readString(p));
        }));
        sBuilder.defineOperation(NAME_OPERATION_UNDEPLOY, new JsonResultWrapper(p -> { 
            return undeployPlan(AasUtils.readString(p), null);
        }));
        sBuilder.defineOperation(NAME_OPERATION_UNDEPLOY_WITHID, new JsonResultWrapper(p -> { 
            return undeployPlan(AasUtils.readString(p), AasUtils.readString(p, 1));
        }));
        sBuilder.defineOperation(NAME_OPERATION_DEPLOY_ASYNC, new JsonResultWrapper(p -> {
            return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, q -> deployPlan(AasUtils.readString(q)), p);
        }));
        sBuilder.defineOperation(NAME_OPERATION_UNDEPLOY_ASYNC, new JsonResultWrapper(p -> {
            return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, q -> undeployPlan(AasUtils.readString(q), null), p);
        }));
        sBuilder.defineOperation(NAME_OPERATION_UNDEPLOY_WITHID_ASYNC, new JsonResultWrapper(p -> {
            return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                q -> undeployPlan(AasUtils.readString(q), AasUtils.readString(q, 1)), p);
        }));
        sBuilder.defineOperation(NAME_OPERATION_GET_TASK_STATUS, new JsonResultWrapper(p -> {
            TaskData data = TaskRegistry.getTaskData(AasUtils.readString(p));
            return data != null && data != TaskRegistry.NO_TASK ? data.getStatus().toString() : null;
        }));
        
    }
    
    /**
     * Deploys a deployment plan.
     * 
     * @param url the URL of the deployment plan
     * @return the application instance id
     * @throws ExecutionException if the operation fails
     */
    static Object deployPlan(String url) throws ExecutionException {
        try {
            return CliBackend.deployPlan(CliBackend.toUri(url));
        } catch (URISyntaxException e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Undeploys a deployment plan.
     * 
     * @param url the URL of the deployment plan
     * @param instanceId the application instance id, may be empty or <b>null</b> for legacy/default starts
     * @return <b>null</b>
     * @throws ExecutionException if the operation fails
     */
    static Object undeployPlan(String url, String instanceId) throws ExecutionException {
        try {
            CliBackend.undeployPlan(CliBackend.toUri(url), instanceId);
            return null;
        } catch (URISyntaxException e) {
            throw new ExecutionException(e);
        }
    }

    @Override
    public Kind getKind() {
        return Kind.DYNAMIC;
    }

    @Override
    public boolean isValid() {
        return true;
    }
    

    /**
     * Called to notify that a new instance of the application <code>appId</code> is about to be started. [public 
     * for testing]
     * 
     * @param appId the application id
     * @param planId the id of the plan starting the application
     * @return the id of the new instance to be passed on to the service starts, may be <b>null</b> 
     *    for default/legacy start
     */
    public static String notifyAppNewInstance(String appId, String planId) {
        return ApplicationInstanceAasConstructor.notifyAppNewInstance(appId, planId);
    }
    
    /**
     * Called to notify that an app instance was stopped. [public for testing]
     * 
     * @param appId the application id of the instance that was stopped
     * @param instanceId the instance id of the instance, may be <b>null</b> or empty for legacy application starts
     * @return the remaining instances
     */
    public static int notifyAppInstanceStopped(String appId, String instanceId) {
        return ApplicationInstanceAasConstructor.notifyAppInstanceStopped(appId, instanceId);
    }

    /**
     * Called to notify that an artifact was created.
     * 
     * @param art the artifact
     */
    static void notifyArtifactCreated(Artifact art) {
        ActiveAasBase.processNotification(NAME_SUBMODEL_ARTIFACTS, (sub, aas) -> {
            
            String collName = null;
            switch (art.getKind()) {
            case SERVICE_ARTIFACT:
                collName = NAME_COLL_SERVICE_ARTIFACTS;
                break;
            case CONTAINER:
                collName = NAME_COLL_CONTAINER;
                break;
            case DEPLOYMENT_PLAN:
                collName = NAME_COLL_DEPLOYMENT_PLANS;
                break;
            default:
                collName = null;
                break;
            }
            if (null != collName) {
                SubmodelElementCollectionBuilder cBuilder // get or create
                    = sub.createSubmodelElementCollectionBuilder(collName, false, false);
                SubmodelElementCollectionBuilder dBuilder 
                    = cBuilder.createSubmodelElementCollectionBuilder(fixId(art.getId()
                        + "_" + art.getAccessUri().toString().hashCode()), false, true);
                dBuilder.createPropertyBuilder(NAME_PROP_ID)
                    .setValue(Type.STRING, art.getId())
                    .build();
                dBuilder.createPropertyBuilder(NAME_PROP_NAME)
                    .setValue(Type.STRING, art.getName())
                    .build();
                dBuilder.createPropertyBuilder(NAME_PROP_DESCRIPTION)
                    .setValue(Type.STRING, art.getDescription())
                    .build();
                dBuilder.createPropertyBuilder(NAME_PROP_URI)
                    .setValue(Type.STRING, art.getAccessUri().toString())
                    .build();
                dBuilder.build();
    
                cBuilder.build();
            }
        });
    }

    /**
     * Called to notify that an artifact was modified.
     * 
     * @param art the artifact
     */
    static void notifyArtifactModified(Artifact art) {
        // currently no action as there is nothing to update, late timestamps?
    }

    /**
     * Called to notify that an artifact was deleted.
     * 
     * @param art the artifact
     */
    static void notifyArtifactDeleted(Artifact art) {
        ActiveAasBase.processNotification(NAME_SUBMODEL_ARTIFACTS, (sub, aas) -> {
            SubmodelElementCollection coll = sub.getSubmodelElementCollection(fixId(art.getId()));
            if (null != coll) {
                sub.deleteElement(coll);
            }
        });
    }
    
}
