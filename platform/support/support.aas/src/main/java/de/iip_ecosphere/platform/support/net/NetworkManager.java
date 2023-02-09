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
     * If given at the end of a key, indicates that the key is intended to be a prefix and keys starting with that 
     * prefix (longest match) shall all be mapped to the same address. Prefixes can be used in particular in 
     * {@link #reservePort(String, ServerAddress)} and (indirectly) in {@link #obtainPort(String)} to reserve a port
     * as well as in {@link #obtainPort(String)} and {@link #getPort(String)} as prefix of a concrete key to retrieve 
     * a port/address based on an already given prefix. Prefix registration can be released with 
     * {@link #releasePort(String)}.
     */
    public static final String PREFIX_SEPARATOR = ".";
    
    /**
     * Returns a network port number for a given key. The first request allocates the port. Receiving an address/port
     * does not imply that a server process is running there. Typically, the actor creating the server would first 
     * obtain an address/port, but in a distributed setting this sequence may not be guaranteed so that the client
     * may have to wait/block/queue until the server is available.
     * 
     * @param key a key indicating the use (may be a prefix, see {@link #PREFIX_SEPARATOR})
     * @return the server address including the port number (including the server IP), not used by other processes;  
     * @throws IllegalArgumentException if the key may not be used, in particular if {@code key} is <b>null</b>
     */
    public ManagedServerAddress obtainPort(String key);

    /**
     * Returns a network port number for a given key. Returns <b>null</b> if no key was registered, directly via
     * {@link #reservePort(String, ServerAddress)} or indirectly via {@link #obtainPort(String)}. If the managed
     * applies delegation to parent managers, the result may be provided by the parent manager (in contrast to 
     * {@link #isInUse(int)} and {@link #isInUse(ServerAddress)} which are supposed to be local only).
     * 
     * @param key a key indicating the use (may be a prefix, see {@link #PREFIX_SEPARATOR})
     * @return the server address including the port number (including the server IP), may be <code>bull</code> if
     *     no address/port was registered for {@code key}
     * @throws IllegalArgumentException if the key may not be used, in particular if {@code key} is <b>null</b>
     */
    public ManagedServerAddress getPort(String key);

    /**
     * Explicitly reserves a certain address for a given key. If reserved before {@link #obtainPort(String)}, this takes
     * precedence. Reserved addresses shall also be {@link #releasePort(String) released} if not used anymore.
     * Reserved addresses must not be within {@link #getLowPort()} and {@link #getHighPort()}, they must not even
     * be associated with the machine running this manager.
     * 
     * @param key a key indicating the use (may be a prefix, see {@link #PREFIX_SEPARATOR})
     * @param address the address to use
     * @return the server address including the port number (including the server IP), 
     *   {@link ManagedServerAddress#isNew()} is {@code true} if the key/address was not obtained/reserved before, 
     *   {@code false} if the key/address is also known. If {@code false} also {@link ManagedServerAddress#getHost()}
     *   or {@link ManagedServerAddress#getSchema()} may differ from {@code address}.
     * @throws IllegalArgumentException if the key may not be used, in particular if {@code key} is <b>null</b> or 
     *   if {@code address} is <b>null</b>
     */
    public ManagedServerAddress reservePort(String key, ServerAddress address);
    
    /**
     * Releases the port. When all requesting parties released the port, the port will be ultimately freed. Usuall,
     * only clients with a {@link ManagedServerAddress#isNew() new} address shall call this method.
     * 
     * @param key a key indicating the use (may be a prefix, see {@link #PREFIX_SEPARATOR})
     * @throws IllegalArgumentException if the key may not be used, in particular if {@code key} is <b>null</b>
     */
    public void releasePort(String key);
    
    /**
     * Registers the use of the specified key by an instance.
     * 
     * @param key a key indicating the use
     * @param hostId some identification of the using host, e.g., the IP address. May be empty,
     */
    public void registerInstance(String key, String hostId);

    /**
     * Unregisters the use of the specified key by an instance.
     * 
     * @param key a key indicating the use
     * @param hostId some identification of the using host, e.g., the IP address. May be empty,
     */
    public void unregisterInstance(String key, String hostId);
    
    /**
     * Returns the number of registered instances of the specified.
     * 
     * @param key a key indicating the use
     * @return the number of registered using instances
     */
    public int getRegisteredInstances(String key);

    /**
     * Returns whether the given address is in use/allocated by this manager.
     * 
     * @param address the address
     * @return {@code true} if the address is allocated within this manager, {@code false} else (also in failure case)
     * @throws IllegalArgumentException if {@code address} is <b>null</b>
     */
    public boolean isInUse(ServerAddress address);

    /**
     * Returns whether the given port is in use/allocated by this manager.
     * 
     * @param port the port 
     * @return {@code true} if the port is allocated within this manager, {@code false} else (also in failure case)
     */
    public boolean isInUse(int port);
    
    /**
     * The minimum port handled by this manager.
     * 
     * @return the minimum port [1;65535], inclusive, lower/equal than {@link #getHighPort()}; may be negative to 
     *   indicate invisible problems, e.g., during remote access
     */
    public int getLowPort();

    /**
     * The maximum port handled by this manager.
     * 
     * @return the minimum port [1;65535], inclusive, higher/equal than {@link #getLowPort()}; may be negative to 
     *   indicate invisible problems, e.g., during remote access
     */
    public int getHighPort();

    /**
     * Configures the network manager.
     * 
     * @param setup the setup instance
     */
    public void configure(NetworkManagerSetup setup);
    
}
