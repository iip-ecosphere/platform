/**
 *******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.test.mqtt.moquette;

import java.util.HashMap;
import java.util.Map;

import io.moquette.broker.security.IAuthenticator;

/**
 * A simple static authenticator.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Authenticator implements IAuthenticator {

    private static Map<String, String> users = null;

    /**
     * Sets up a simple user and password authentication.
     * 
     * @param user the user to add
     * @param password the plaintext password of the user
     */
    static void setBasicAuth(String user, String password) {
        if (null == users) {
            users = new HashMap<>();
        }
        users.put(user, password);
    }
    
    /**
     * Returns whether this authenticator is initialized.
     * 
     * @return {@code true} for initialized, {@code false} else
     */
    public static boolean isInitialized() {
        return users != null;
    }
    
    /**
     * Clears this authenticator.
     */
    public static void clear() {
        users = null;
    }

    @Override
    public boolean checkValid(String clientId, String user, byte[] password) {
        return null != users && users.get(user) != null && users.get(user).equals(password);
    }

}
