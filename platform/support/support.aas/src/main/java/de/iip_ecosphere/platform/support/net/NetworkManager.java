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

package de.iip_ecosphere.platform.support.net;

import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * Simple functions for distributed network management. The aim is to reduce the port configuration effort. Access to 
 * this manager shall finally happen via AAS and may be of local or global scope. Thus, we do not define a server here
 * rather than the re-usable core functionality. The manager is just responsible for ports in {@link #getLowPort()} and 
 * {@link #getHighPort()}. The name is a bit broader than just port manager as additional abilities may follow.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface NetworkManager {
    
    /**
     * Returns a network port number for a certain key. The first request allocates the port. Receiving an address/port
     * does not imply that a server process is running there. Typically, the actor creating the server would first 
     * obtain an address/port, but in a distributed setting this sequence may not be guaranteed so that the client
     * may have to wait/block/queue until the server is available.
     * 
     * @param key a key indicating the use
     * @return the server address including the port number (including the server IP), not used by other processes
     * @throws IllegalArgumentException if the key may not be used, in particular if {@code key} is <b>null</b>
     */
    public ManagedServerAddress obtainPort(String key);
    
    /**
     * Releases the port. When all requesting parties released the port, the port will be ultimately freed. Usuall,
     * only clients with a {@link ManagedServerAddress#isNew() new} address shall call this method.
     * 
     * @param key a key indicating the use
     * @throws IllegalArgumentException if the key may not be used, in particular if {@code key} is <b>null</b>
     */
    public void releasePort(String key);
    
    /**
     * Returns whether the given address is in use/allocated by this manager.
     * 
     * @param address the address
     * @return {@code true} if the address is allocated within this manager, {@code false} else
     */
    public boolean isInUse(ServerAddress address);

    /**
     * Returns whether the given port is in use/allocated by this manager.
     * 
     * @param port the port 
     * @return {@code true} if the port is allocated within this manager, {@code false} else
     */
    public boolean isInUse(int port);
    
    /**
     * The minimum port handled by this manager.
     * 
     * @return the minimum port [1;65535], inclusive, lower/equal than {@link #getHighPort()}
     */
    public int getLowPort();

    /**
     * The maximum port handled by this manager.
     * 
     * @return the minimum port [1;65535], inclusive, higher/equal than {@link #getLowPort()}
     */
    public int getHighPort();

}
