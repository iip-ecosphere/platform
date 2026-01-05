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

package de.iip_ecosphere.platform.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import de.iip_ecosphere.platform.support.logging.LoggerFactory;
import de.iip_ecosphere.platform.support.net.LocalNetworkManagerImpl;

/**
 * A local network manager that persists its registrations. This specialized network manager is
 * intended for testing only, not for production use!
 * 
 * @author Holger Eichelberger, SSE
 */
public class PersistentLocalNetworkManagerImpl extends LocalNetworkManagerImpl {

    @Override
    protected void notifyChanged() {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(PersistentLocalNetworkManagerDescriptor.getFile()));
            writeTo(oos);
            oos.close();
        } catch (IOException e) {
            LoggerFactory.getLogger(getClass()).error("Cannot write persistent local network manager data: {}", 
                e.getMessage());
        }
    }

}
