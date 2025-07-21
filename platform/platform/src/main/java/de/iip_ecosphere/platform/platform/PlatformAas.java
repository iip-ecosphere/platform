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

import static de.iip_ecosphere.platform.support.aas.AasUtils.fixId;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;

import de.iip_ecosphere.platform.platform.ArtifactsManager.Artifact;
import de.iip_ecosphere.platform.platform.ArtifactsManager.ArtifactKind;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.environment.services.TransportConverter;
import de.iip_ecosphere.platform.services.environment.services.TransportConverterFactory;
import de.iip_ecosphere.platform.support.TaskRegistry;
import de.iip_ecosphere.platform.support.TaskRegistry.TaskData;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementList.SubmodelElementListBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationInstanceAasConstructor;
import de.iip_ecosphere.platform.support.json.JsonResultWrapper;
import de.iip_ecosphere.platform.transport.status.ActionTypes;
import de.iip_ecosphere.platform.transport.status.TaskUtils;
import de.iip_ecosphere.platform.transport.status.TaskUtils.TaskCompletedPredicate;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * The platform AAS contributor, in particular for the available artifacts.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PlatformAas implements AasContributor {

    public static final String NAME_SUBMODEL_ARTIFACTS = AasPartRegistry.NAME_SUBMODEL_ARTIFACTS;
    public static final String NAME_SUBMODEL_STATUS = AasPartRegistry.NAME_SUBMODEL_STATUS;
    public static final String NAME_COLL_SERVICE_ARTIFACTS = "ServiceArtifacts";
    public static final String NAME_COLL_CONTAINER = "Container";
    public static final String NAME_COLL_DEPLOYMENT_PLANS = "DeploymentPlans";
    public static final String NAME_COLL_KNOWN_SERVICES = "KnownServices";
    public static final String NAME_PROP_ID = "id";
    public static final String NAME_PROP_NAME = "name";
    public static final String NAME_PROP_DESCRIPTION = "description";
    public static final String NAME_PROP_URI = "uri";
    public static final String NAME_PROP_ENABLED = "enabled";
    
    public static final String NAME_OPERATION_DEPLOY = "deployPlan";
    public static final String NAME_OPERATION_UNDEPLOY = "undeployPlan";
    public static final String NAME_OPERATION_UNDEPLOY_WITHID = "undeployPlanWithId";
    public static final String NAME_OPERATION_DEPLOY_ASYNC = "deployPlanAsync";
    public static final String NAME_OPERATION_UNDEPLOY_ASYNC = "undeployPlanAsync";
    public static final String NAME_OPERATION_UNDEPLOY_WITHID_ASYNC = "undeployPlanWithIdAsync";
    public static final String NAME_OPERATION_GET_TASK_STATUS = "getTaskStatus";
    public static final String NAME_OPERATION_UPLOAD = "upload";
    
    private static final String PROGRESS_COMPONENT_ID = "IIP-Ecosphere Platform";
    
    static final TaskCompletedPredicate DEPLOY_COMPLETED = (t, s) -> {
        if (s.getAction() == ActionTypes.PROCESS && ServiceManager.PROGRESS_COMPONENT_ID.equals(s.getId())) {
            t.incEventCount(); // 2 progress messages per service!
        }
        return t.maxEventCountReached() || s.getAction() == ActionTypes.ERROR;
    };
    
    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        AuthenticationDescriptor aDesc = getSubmodelAuthentication();
        SubmodelBuilder smB = AasPartRegistry.createSubmodelBuilderRbac(aasBuilder, NAME_SUBMODEL_ARTIFACTS);

        smB.createSubmodelElementCollectionBuilder(NAME_COLL_SERVICE_ARTIFACTS).build();
        smB.createSubmodelElementCollectionBuilder(NAME_COLL_CONTAINER).build();
        smB.createSubmodelElementCollectionBuilder(NAME_COLL_DEPLOYMENT_PLANS).build();

        smB.createOperationBuilder(NAME_OPERATION_DEPLOY)
            .addInputVariable("url", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_DEPLOY))
            .build(Type.STRING, aDesc);
        smB.createOperationBuilder(NAME_OPERATION_UNDEPLOY)
            .addInputVariable("url", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UNDEPLOY))
            .build(aDesc);
        smB.createOperationBuilder(NAME_OPERATION_UNDEPLOY_WITHID)
            .addInputVariable("url", Type.STRING)
            .addInputVariable("instanceId", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UNDEPLOY))
            .build(aDesc);
        smB.createOperationBuilder(NAME_OPERATION_DEPLOY_ASYNC)
            .addInputVariable("url", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_DEPLOY_ASYNC))
            .build(Type.STRING, aDesc);
        smB.createOperationBuilder(NAME_OPERATION_UNDEPLOY_ASYNC)
            .addInputVariable("url", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UNDEPLOY_ASYNC))
            .build(Type.STRING, aDesc);
        smB.createOperationBuilder(NAME_OPERATION_UNDEPLOY_WITHID_ASYNC)
            .addInputVariable("url", Type.STRING)
            .addInputVariable("instanceId", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UNDEPLOY_WITHID_ASYNC))
            .build(Type.STRING, aDesc);
        smB.createOperationBuilder(NAME_OPERATION_GET_TASK_STATUS)
            .addInputVariable("taskId", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_GET_TASK_STATUS))
            .build(Type.STRING, aDesc);
        smB.createOperationBuilder(NAME_OPERATION_UPLOAD)
            .addInputVariable("kind", Type.STRING)
            .addInputVariable("sequenceNr", Type.INTEGER)
            .addInputVariable("name", Type.STRING)
            .addInputVariable("data", Type.STRING)
            .setInvocable(iCreator.createInvocable(NAME_OPERATION_UPLOAD))
            .build(aDesc);
        smB.build();
        
        // just that they are there
        SubmodelBuilder statusBuilder = AasPartRegistry.createSubmodelBuilderRbac(aasBuilder, NAME_SUBMODEL_STATUS);
        PlatformSetup setup = PlatformSetup.getInstance();
        TransportConverter.addEndpointToAas(statusBuilder, TransportConverterFactory.getInstance().
            getGatewayEndpoint(setup.getAas(), setup.getTransport(), PlatformSetup.GATEWAY_PATH_STATUS));
        statusBuilder.build();
        AasPartRegistry.createSubmodelBuilderRbac(aasBuilder, 
            ApplicationInstanceAasConstructor.NAME_SUBMODEL_APPINSTANCES)
            .build();
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
            return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, q -> deployPlan(AasUtils.readString(q)), 
                DEPLOY_COMPLETED, p);
        }));
        sBuilder.defineOperation(NAME_OPERATION_UNDEPLOY_ASYNC, new JsonResultWrapper(p -> {
            return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, q -> undeployPlan(AasUtils.readString(q), null), 
                DEPLOY_COMPLETED, p);
        }));
        sBuilder.defineOperation(NAME_OPERATION_UNDEPLOY_WITHID_ASYNC, new JsonResultWrapper(p -> {
            return TaskUtils.executeAsTask(PROGRESS_COMPONENT_ID, 
                q -> undeployPlan(AasUtils.readString(q), AasUtils.readString(q, 1)), DEPLOY_COMPLETED, p);
        }));
        sBuilder.defineOperation(NAME_OPERATION_GET_TASK_STATUS, new JsonResultWrapper(p -> {
            TaskData data = TaskRegistry.getTaskData(AasUtils.readString(p));
            return data != null && data != TaskRegistry.NO_TASK ? data.getStatus().toString() : null;
        }));
        sBuilder.defineOperation(NAME_OPERATION_UPLOAD, new JsonResultWrapper(p -> {
            try {
                upload(ArtifactKind.valueOf(AasUtils.readString(p)), AasUtils.readInt(p, 1, -1), 
                    AasUtils.readString(p, 2), AasUtils.readString(p, 3));
                return null;
            } catch (IllegalArgumentException e) {
                throw new ExecutionException("kind: " + e.getMessage(), null);
            }
        }));
    }

    /**
     * Implements the upload of an artifact kind, possibly as chunks into the platform.
     * 
     * @param kind the kind of artifact being uploaded so that the platform can decide about the (final) 
     *   uploading location; deployment plans go into the {@link PlatformSetup#getArtifactsFolder() artifacts folder}, 
     *   all other kinds for now into the {@link PlatformSetup#getUploadFolder() upload folder}
     * @param sequenceNr the sequence number for transferring a larger file in chunks; 0 indicates a complete file to 
     *   be stored with the given {@code name} directly into the target folder determined by {@code kind}; a positive
     *   number indicates a chunk which may be transferred out of sequence. a negative number indicates the last chunk
     *   of the corresponding positive number and requests composing the chunks to a file which shall be stored  
     * @param name the name of the file with extension but without path
     * @param data the data chunk, UTF-8 encoded
     * @throws ExecutionException if uploading files for some reason
     */
    static void upload(ArtifactKind kind, int sequenceNr, String name, String data) throws ExecutionException {
        File temp = FileUtils.getTempDirectory();
        File uploadFolder;
        if (ArtifactKind.DEPLOYMENT_PLAN == kind) {
            uploadFolder = PlatformSetup.getInstance().getArtifactsFolder();
        } else {
            uploadFolder = PlatformSetup.getInstance().getUploadFolder();
        }
        File target = new File(uploadFolder, name);

        byte[] bytes = Base64.getDecoder().decode(data);
        try {
            if (0 == sequenceNr) {
                FileUtils.writeByteArrayToFile(target, bytes);
                LoggerFactory.getLogger(PlatformAas.class).info("Upload of {} to {} completed.", name, target);
            } else if (sequenceNr > 0) {
                FileUtils.writeByteArrayToFile(getChunkFile(temp, name, sequenceNr), bytes);
            } else {
                sequenceNr = -sequenceNr;
                File tmpFile = new File(temp, name);
                for (int i = 1; i < sequenceNr; i++) {
                    File chunkFile = getChunkFile(temp, name, i);
                    byte[] fileBytes = FileUtils.readFileToByteArray(chunkFile);
                    FileUtils.writeByteArrayToFile(tmpFile, fileBytes, i > 1);
                    FileUtils.deleteQuietly(chunkFile); // may fail
                }
                FileUtils.writeByteArrayToFile(tmpFile, bytes, true);
                // make visible if complete
                Files.copy(tmpFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING); 
                FileUtils.deleteQuietly(tmpFile); // may fail
                LoggerFactory.getLogger(PlatformAas.class).info("Upload to {} completed.", name, target);
            }
        } catch (IOException e) {
            throw new ExecutionException("Uploading file (" + kind + " seqNr " + sequenceNr + " " + name + "):" 
                + e.getMessage(), null);
        }
    }
    
    /**
     * Returns the file object of a chunk file.
     * 
     * @param folder the folder where the chunk file is/will be stored
     * @param name the name of the file
     * @param sequenceNr the sequence number
     * @return the file object
     */
    private static File getChunkFile(File folder, String name, int sequenceNr) {
        return new File(folder, name + "-" + sequenceNr);        
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
                    = sub.createSubmodelElementCollectionBuilder(collName);
                SubmodelElementListBuilder dBuilder 
                    = cBuilder.createSubmodelElementListBuilder(fixId(art.getId()
                        + "_" + art.getAccessUri().toString().hashCode()));
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
                dBuilder.createPropertyBuilder(NAME_PROP_ENABLED)
                    .setValue(Type.BOOLEAN, art.isEnabled())
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
