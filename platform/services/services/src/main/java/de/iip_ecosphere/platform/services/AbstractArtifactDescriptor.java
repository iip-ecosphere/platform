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

package de.iip_ecosphere.platform.services;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Basic re-usable implementation of {@link ArtifactDescriptor}.
 * 
 * @param <S> the service descriptor type
 * @author Holger Eichelberger, SSE
 */
public class AbstractArtifactDescriptor<S extends AbstractServiceDescriptor<?>> implements ArtifactDescriptor {

    private String id;
    private String name;
    private URI uri;
    private Map<String, S> services = Collections.synchronizedMap(new HashMap<>());
    private Map<String, S> servers = Collections.synchronizedMap(new HashMap<>());
    
    /**
     * Creates an artifact descriptor.
     * 
     * @param id the artifact id
     * @param name the (file) name
     * @param uri the URI the artifact was loaded from
     * @param services the contained services
     * @param servers the container servers
     */
    protected AbstractArtifactDescriptor(String id, String name, URI uri, List<S> services, List<S> servers) {
        this.id = id;
        this.name = name;
        this.uri = null == uri ? null : uri.normalize();
        this.services = createMapping(services);
        this.servers = createMapping(servers);
    }
    
    /**
     * Creates a id-descriptor mapping for the given {@code services}.
     * 
     * @param services the services
     * @return the mapping
     */
    protected Map<String, S> createMapping(List<S> services) {
        Map<String, S> result = new HashMap<>();
        for (S s : services) {
            result.put(s.getId(), s);
            s.setArtifact(this);
        }
        return result;
    }
    
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public Set<String> getServiceIds() {
        return services.keySet();
    }

    @Override
    public Collection<? extends S> getServices() {
        return services.values();
    }
    
    @Override
    public Collection<? extends S> getServers() {
        return servers.values();
    }

    @Override
    public S getService(String serviceId) {
        return null == serviceId ? null : services.get(serviceId);
    }

    @Override
    public S getServer(String serverId) {
        return null == serverId ? null : servers.get(serverId);
    }

    /**
     * Adds a service.
     * 
     * @param service the service to be added
     */
    protected void addService(S service) {
        services.put(service.getId(), service);
    }

}
