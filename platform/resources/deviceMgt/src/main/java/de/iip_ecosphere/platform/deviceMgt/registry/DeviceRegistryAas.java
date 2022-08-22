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

package de.iip_ecosphere.platform.deviceMgt.registry;

import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonUtils;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.fixId;
import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.readString;

/**
 * A Asset Administration Shell for device registry functionalities.
 * Mostly called by {@link de.iip_ecosphere.platform.deviceMgt.DeviceManagement} and
 * devices itself.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryAas implements AasContributor {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_RESOURCES;

    public static final String NAME_COLL_DEVICE_REGISTRY = "deviceRegistry";

    public static final String NAME_PROP_DEVICE_RESOURCE = "resource";
    public static final String NAME_PROP_MANAGED_DEVICE_ID = "managedId";
    public static final String NAME_PROP_DEVICE_IP = "ip";

    public static final String NAME_OP_DEVICE_ADD = "addDevice";
    public static final String NAME_OP_DEVICE_REMOVE = "removeDevice";
    public static final String NAME_OP_IM_ALIVE = "imAlive";
    public static final String NAME_OP_SEND_TELEMETRY = "sendTelemetry";

    /**
     * Basically registers the aas for the device registry.
     *
     * @param aasBuilder the aasBuilder to contributeTo
     * @param iCreator an InvocablesCreator
     * @return null
     */
    @Override
    public Aas contributeTo(Aas.AasBuilder aasBuilder, InvocablesCreator iCreator) {
        Submodel.SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);

        SubmodelElementCollectionBuilder registryColl = smB
                .createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_REGISTRY, false, false);

        registryColl.createOperationBuilder(NAME_OP_DEVICE_ADD)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_DEVICE_ADD)))
                .addInputVariable("deviceId", Type.STRING)
                .addInputVariable("deviceIp", Type.STRING)
                .build(Type.NONE);

        registryColl.createOperationBuilder(NAME_OP_DEVICE_REMOVE)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_DEVICE_REMOVE)))
                .addInputVariable("deviceId", Type.STRING)
                .build(Type.NONE);

        registryColl.createOperationBuilder(NAME_OP_IM_ALIVE)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_IM_ALIVE)))
                .addInputVariable("deviceId", Type.STRING)
                .build(Type.NONE);

        registryColl.createOperationBuilder(NAME_OP_SEND_TELEMETRY)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_SEND_TELEMETRY)))
                .addInputVariable("deviceId", Type.STRING)
                .addInputVariable("telemetryData", Type.STRING)
                .build(Type.NONE);

        registryColl.build();

        smB.defer();
        return null;
    }

    /**
     * Defines the operations details.
     *
     * @param sBuilder the ProtocolServerBuilder
     */
    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_DEVICE_ADD),
                new JsonResultWrapper(p -> {
                    DeviceRegistrationResponse resp = DeviceRegistryFactory.getDeviceRegistry().addDevice(
                        readString(p), readString(p, 1));
                    return JsonUtils.toJson(resp);
                })
        );

        sBuilder.defineOperation(getQName(NAME_OP_DEVICE_REMOVE),
                new JsonResultWrapper(p -> {
                    DeviceRegistryFactory.getDeviceRegistry().removeDevice(readString(p));
                    return null;
                })
        );

        sBuilder.defineOperation(getQName(NAME_OP_IM_ALIVE),
                new JsonResultWrapper(p -> {
                    DeviceRegistryFactory.getDeviceRegistry().imAlive(readString(p));
                    return null;
                })
        );

        sBuilder.defineOperation(getQName(NAME_OP_SEND_TELEMETRY),
                new JsonResultWrapper(p -> {
                    DeviceRegistryFactory.getDeviceRegistry().sendTelemetry(readString(p), readString(p, 1));
                    return null;
                })
        );
    }

    /**
     * Returns a qualified name within this submodel.
     * 
     * @param name the name to be turned into a qualified name
     * @return the qualified name
     */
    private static String getQName(String name) {
        return NAME_COLL_DEVICE_REGISTRY + "_" + name;
    }

    /**
     * This aas is a {@code Kind.ACTIVE}.
     * @return Kind.ACTIVE.
     */
    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    /**
     * In case this code is used as library one doesn't want that the aas is setup.
     * @return {@code true} if code based is not used as a library and the aas should be deployed.
     */
    @Override
    public boolean isValid() {
        return null != DeviceRegistryFactory.getDeviceRegistry();
    }

    /**
     * Notify the DeviceRegistryAas if a device was added, so it can
     * manage and build dynamic parts for this device.
     *
     * @param managedId the internal/managed id, could be the same as resourceId
     * @param resourceId the resource id
     * @param resourceIp the ip address of the resource
     */
    public static void notifyDeviceAdded(String managedId, String resourceId, String resourceIp) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            Submodel.SubmodelBuilder resources = aas.createSubmodelBuilder(NAME_SUBMODEL, null);
            SubmodelElementCollectionBuilder registry = resources
                    .createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_REGISTRY, false, false);

            SubmodelElementCollectionBuilder device = resources
                .createSubmodelElementCollectionBuilder(fixId(resourceId), false, false);

            device.createPropertyBuilder(NAME_PROP_MANAGED_DEVICE_ID)
                    .setValue(Type.STRING, managedId)
                    .build();

            device.createPropertyBuilder(NAME_PROP_DEVICE_IP)
                    .setValue(Type.STRING, resourceIp)
                    .build();

            device.build();
            registry.build();
            resources.build();
        });
    }

    /**
     * Notify the DeviceRegistryAas if a device was removed, so it can
     * remove entries of the managed device.
     *
     * @param resourceId the id of the resource
     */
    public static void notifyDeviceRemoved(String resourceId) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            DeviceRegistryAasClient client = new DeviceRegistryAasClient();
            SubmodelElementCollection device = client.getDevice(fixId(resourceId));
            if (null != device) { // for emergency situations
                device.deleteElement(NAME_PROP_MANAGED_DEVICE_ID);
                device.deleteElement(NAME_PROP_DEVICE_IP);
            }
        });
    }

}
