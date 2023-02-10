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

import de.iip_ecosphere.platform.support.aas.Submodel;
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
    public static final String NAME_PROP_INSTANCEID = "instanceId";
    public static final String NAME_PROP_TIMESTAMP = "timestamp";

    /**
     * Called to notify that a new instance of the application <code>appId</code> is about to be started.
     * 
     * @param appId the application id
     * @return the id of the new instance to be passed on to the service starts, may be <b>null</b> 
     *    for default/legacy start
     */
    public static String notifyAppNewInstance(String appId) {
        AtomicReference<String> result = new AtomicReference<String>(null);
        ActiveAasBase.processNotification(NAME_SUBMODEL_APPINSTANCES, NotificationMode.SYNCHRONOUS, (sub, aas) -> {
            int newId = -1;
            for (SubmodelElement elt: sub.submodelElements()) {
                if (elt instanceof SubmodelElementCollection) {
                    SubmodelElementCollection coll = (SubmodelElementCollection) elt;
                    if (appId.equals(AasUtils.getPropertyValueAsStringSafe(coll, NAME_PROP_APPID, null))) {
                        newId = Math.max(newId, AasUtils.getPropertyValueAsIntegerSafe(coll, NAME_PROP_INSTANCEID, 0));
                        break;
                    }
                }
            }

            newId++; // the next instance
            String id = appId + "-" + newId;
            SubmodelElementCollectionBuilder cBuilder // get or create
                = sub.createSubmodelElementCollectionBuilder(NAME_SUBMODEL_APPINSTANCES, false, false);
            SubmodelElementCollectionBuilder dBuilder 
                = cBuilder.createSubmodelElementCollectionBuilder(fixId(id), false, false);
            dBuilder.createPropertyBuilder(NAME_PROP_APPID)
                .setValue(Type.STRING, appId)
                .build();
            dBuilder.createPropertyBuilder(NAME_PROP_INSTANCEID)
                .setValue(Type.STRING, newId)
                .build();
            dBuilder.createPropertyBuilder(NAME_PROP_TIMESTAMP)
                .setValue(Type.INTEGER, System.currentTimeMillis())
                .build();
            if (newId > 0) {
                result.set(String.valueOf(newId));
            }
        });

        return result.get();        
    }
    
    /**
     * Called to notify that an app instance was stopped.
     * 
     * @param appId the application id of the instance that was stopped
     * @param instanceId the instance id of the instance, may be <b>null</b> or empty for legacy application starts
     * @return the remaining instances
     */
    public static int notifyAppInstanceStopped(String appId, String instanceId) {
        final AtomicInteger result = new AtomicInteger(0);
        final String instId = null == instanceId ? "0" : instanceId;
        ActiveAasBase.processNotification(NAME_SUBMODEL_APPINSTANCES, NotificationMode.SYNCHRONOUS, (sub, aas) -> {
            String id = fixId(appId + "-" + instId);
            SubmodelElementCollection coll = sub.getSubmodelElementCollection(id);
            if (null != coll) {
                coll.deleteElement(id);
            }
            result.set(countAppInstances(appId, sub));
        });
        return result.get();
    }
    
    /**
     * Return the number of application instances with the given {@code appId}.
     * 
     * @param appId the application id to look for
     * @param sub the submodel to take the data from
     * @return the number of instances
     */
    static int countAppInstances(String appId, Submodel sub) {
        int result = 0;
        for (SubmodelElement elt: sub.submodelElements()) {
            if (elt instanceof SubmodelElementCollection) {
                SubmodelElementCollection coll = (SubmodelElementCollection) elt;
                if (appId.equals(AasUtils.getPropertyValueAsStringSafe(coll, NAME_PROP_APPID, null))) {
                    result++;
                }
            }
        }
        return result;
    }
}
