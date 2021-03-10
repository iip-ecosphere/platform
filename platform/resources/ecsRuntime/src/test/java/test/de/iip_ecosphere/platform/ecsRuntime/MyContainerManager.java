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

package test.de.iip_ecosphere.platform.ecsRuntime;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.ecsRuntime.AbstractContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.services.Version;

/**
 * A test container manager.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyContainerManager extends AbstractContainerManager<MyContainerDesciptor> {

    private static int id = 0; 
    
    /**
     * Creates a container id.
     * 
     * @return the container id
     */
    private String createId() {
        return "c-" + id++;
    }
    
    @Override
    public String addContainer(String location) throws ExecutionException {
        String sId = createId();
        return super.addContainer(sId, new MyContainerDesciptor(sId, "cName", new Version(0, 1)));
    }

    @Override
    public void startContainer(String id) throws ExecutionException {
        checkId(id, id);
        getContainer(id, "id", "start").setState(ContainerState.DEPLOYED);
    }

    @Override
    public void stopContainer(String id) throws ExecutionException {
        checkId(id, id);
        getContainer(id, "id", "stop").setState(ContainerState.STOPPED);
    }

    @Override
    public void updateContainer(String id, String location) throws ExecutionException {
        checkId(id, id);
        // we may do some parallel change here, but for testing without functionality?
    }
    
    @Override
    public void undeployContainer(String id) throws ExecutionException {
        MyContainerDesciptor cnt = getContainer(id, "id", "undeploy");
        super.undeployContainer(id);
        cnt.setState(ContainerState.UNKOWN); // do afterwards as super may throw exception
    }

    @Override
    public void migrateContainer(String containerId, String location) throws ExecutionException {
        // we may do some parallel change here, but for testing without functionality?
        MyContainerDesciptor cnt = getContainer(containerId, "containerId", "migrate");
        // on "target machine"
        String targetId = createId();
        MyContainerDesciptor tCnt = new MyContainerDesciptor(targetId, cnt.getName(), cnt.getVersion());
        super.addContainer(targetId, tCnt);
        // get rid of container here
        super.migrateContainer(containerId, location);
        tCnt.setState(ContainerState.DEPLOYED);
    }
    
}