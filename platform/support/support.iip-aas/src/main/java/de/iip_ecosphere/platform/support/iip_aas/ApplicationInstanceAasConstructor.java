/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import de.iip_ecosphere.platform.support.OsUtils;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.fixId;

/**
 * A class to construct the AAS submodel for application instances. This class is intentionally not an AAS rather
 * than it provides the way to construct and modify it. The {@link ApplicationInstancesAasClient} is related.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ApplicationInstanceAasConstructor {

    public static final String NAME_SUBMODEL_APPINSTANCES = "ApplicationInstances";
    public static final String NAME_PROP_APPID = "appId";
    public static final String NAME_PROP_PLANID = "planId";
    public static final String NAME_PROP_INSTANCEID = "instanceId";
    public static final String NAME_PROP_TIMESTAMP = "timestamp";

    /**
     * Called to notify that a new instance of the application <code>appId</code> is about to be started.
     * 
     * @param appId the application id
     * @param planId the id of the deployment plan starting the plan
     * @return the id of the new instance to be passed on to the service starts, may be <b>null</b> 
     *    for default/legacy start
     */
    public static String notifyAppNewInstance(String appId, String planId) {
        AtomicReference<String> result = new AtomicReference<String>(null);
        ActiveAasBase.processNotification(NAME_SUBMODEL_APPINSTANCES, NotificationMode.SYNCHRONOUS, (sub, aas) -> {
            // -1 is legacy, may fail when further app uses same services
            int newId = Boolean.valueOf(OsUtils.getPropertyOrEnv("iip.firstWithoutAppId", "false")) 
                && sub.getSubmodelElementsCount() == 0 ? -1 : 0; 
            String propMaxId = AasUtils.fixId(appId + "_max");
            Property propMax = sub.getProperty(propMaxId);
            if (null == propMax) {
                newId = 0;
                SubmodelBuilder builder = aas.createSubmodelBuilder(sub.getIdShort(), sub.getIdentification());
                builder.createPropertyBuilder(propMaxId)
                    .setValue(Type.INTEGER, newId)
                    .build();
                builder.build();
            } else {
                newId = AasUtils.getPropertyValueAsIntegerSafe(sub, propMaxId, 0) + 1; // the next instance
                AasUtils.setPropertyValueSafe(sub, propMaxId, newId);
            }

            SubmodelElementCollectionBuilder dBuilder 
                = sub.createSubmodelElementCollectionBuilder(getAasAppInstanceId(appId, newId), false, false);
            dBuilder.createPropertyBuilder(NAME_PROP_APPID)
                .setValue(Type.STRING, appId)
                .build();
            dBuilder.createPropertyBuilder(NAME_PROP_PLANID)
                .setValue(Type.STRING, planId)
                .build();
            dBuilder.createPropertyBuilder(NAME_PROP_INSTANCEID)
                .setValue(Type.INTEGER, newId)
                .build();
            dBuilder.createPropertyBuilder(NAME_PROP_TIMESTAMP)
                .setValue(Type.INT64, System.currentTimeMillis())
                .build();
            dBuilder.build();
            if (newId > 0) {
                result.set(String.valueOf(newId));
            }
        });

        return result.get();        
    }

    /**
     * Returns the AAS application instance id.
     * 
     * @param appId the application id
     * @param instanceId the instance id
     * @return the application instance id ready to be used as id short in an AAS
     */
    private static String getAasAppInstanceId(String appId, int instanceId) {
        return getAasAppInstanceId(appId, String.valueOf(instanceId));
    }
    
    /**
     * Returns the AAS application instance id.
     * 
     * @param appId the application id
     * @param instanceId the instance id
     * @return the application instance id ready to be used as id short in an AAS
     */
    private static String getAasAppInstanceId(String appId, String instanceId) {
        return fixId(appId + "-" + instanceId);
    }
    
    /**
     * Called to notify that an app instance was stopped.
     * 
     * @param appId the application id of the instance that was stopped
     * @param instanceId the instance id of the instance, may be <b>null</b> or empty for legacy application starts
     * @return the remaining instances (across all deployment plans)
     */
    public static int notifyAppInstanceStopped(String appId, String instanceId) {
        final AtomicInteger result = new AtomicInteger(0);
        final String instId = null == instanceId || instanceId.length() == 0 ? "0" : instanceId;
        ActiveAasBase.processNotification(NAME_SUBMODEL_APPINSTANCES, NotificationMode.SYNCHRONOUS, (sub, aas) -> {
            sub.deleteElement(getAasAppInstanceId(appId, instId));
            result.set(countAppInstances(appId, null, sub));
        });
        return result.get();
    }
    
    /**
     * Return the number of application instances with the given {@code appId}.
     * 
     * @param appId the application id to look for
     * @param planId the deployment plan id to filter for, may be empty or <b>null</b> to not filter for 
     *     deployment plans
     * @param sub the submodel to take the data from
     * @return the number of instances
     */
    static int countAppInstances(String appId, String planId, Submodel sub) {
        int result = 0;
        if (null != sub && appId != null) {
            for (SubmodelElement elt: sub.submodelElements()) {
                if (elt instanceof SubmodelElementCollection) {
                    SubmodelElementCollection coll = (SubmodelElementCollection) elt;
                    if (appId.equals(AasUtils.getPropertyValueAsStringSafe(coll, NAME_PROP_APPID, null))) {
                        if (null == planId || planId.isEmpty() 
                            || planId.equals(AasUtils.getPropertyValueAsStringSafe(coll, NAME_PROP_PLANID, null))) {
                            result++;
                        }
                    }
                }
            }
        }
        return result;
    }
}
