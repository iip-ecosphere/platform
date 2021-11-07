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

package de.iip_ecosphere.platform.deviceMgt.s3mock;

import de.iip_ecosphere.platform.deviceMgt.DeviceMgtSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageServerSetup;
import de.iip_ecosphere.platform.deviceMgt.storage.StorageFactory;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import io.findify.s3mock.S3Mock;

/**
 * A lifecycle descriptor to start the S3Mock server if desired.
 * 
 * @author Holger Eichelberger, SSE
 */
public class S3StorageLifecycleDescriptor implements LifecycleDescriptor {

    private S3Mock api;
    
    @Override
    public void startup(String[] args) {
        DeviceMgtSetup setup = StorageFactory.getInstance().getSetup();
        if (null != setup) {
            StorageServerSetup serverSetup = setup.getStorageServer();
            if (null != serverSetup && serverSetup.getPort() > 0) {
                S3Mock.Builder builder = new S3Mock.Builder().withPort(serverSetup.getPort());
                if (null == serverSetup.getPath() || serverSetup.getPath().toString().length() == 0) {
                    builder.withInMemoryBackend();
                } else {
                    builder.withFileBackend(serverSetup.getPath().getAbsolutePath());
                }
                api = builder.build();
                api.start();
            }
        }
    }

    @Override
    public void shutdown() {
        if (null != api) {
            api.shutdown();
        }
    }

    @Override
    public Thread getShutdownHook() {
        return null;
    }

    @Override
    public int priority() {
        return INIT_PRIORITY; // preliminary
    }

}
