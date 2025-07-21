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

package de.iip_ecosphere.platform.ecsRuntime;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.net.UriResolver;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * A basic re-usable implementation of the container manager. Implementations shall override at least 
 * {@link #undeployContainer(String)}, {@link #migrateContainer(String, String)}
 * and call the implementation of this class to perform the changes.
 *
 * @param <C> the actual type of container descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractContainerManager<C extends ContainerDescriptor> implements ContainerManager {

    private Map<String, C> containers = Collections.synchronizedMap(new HashMap<>());

    @Override
    public Set<String> getIds() {
        return containers.keySet();
    }

    @Override
    public Collection<C> getContainers() {
        return containers.values();
    }

    @Override
    public C getContainer(String id) {
        return containers.get(id);
    }
    
    @Override
    public String getId(URI location) {
        String result = null;
        URI loc = location.normalize();
        for (C desc : containers.values()) {
            if (desc.getUri().equals(loc)) {
                result = desc.getId();
                break;
            }
        }
        return result;
    }
    
    @Override
    public ContainerState getState(String id) {
        ContainerState result = ContainerState.UNKNOWN;
        if (null != id) {
            ContainerDescriptor d = containers.get(id);
            if (null != d) {
                result = d.getState();
            } else {
                try {
                    String uriId = new URI(id).normalize().toString();
                    for (C desc : containers.values()) {
                        if (desc.getUri().toString().equals(uriId)) {
                            result = desc.getState();
                            break;
                        }
                    }
                } catch (URISyntaxException e) {
                    // input not an URI, ignore
                }
            }
        }
        return result;
    }
    
    /**
     * Adds a container.
     * 
     * @param id the container id
     * @param descriptor the container descriptor
     * @return {@code id}
     * @throws ExecutionException in case that the id is invalid or already known
     */
    protected String addContainer(String id, C descriptor) throws ExecutionException {
        checkId(id, "id");
        if (containers.containsKey(id)) {
            throwExecutionException(null, "Container id '" + id + "' " + EXC_ALREADY_KNOWN);
        }
        containers.put(id, descriptor);
        EcsAas.notifyContainerAdded(descriptor);
        return id;
    }

    @Override
    public void undeployContainer(String id) throws ExecutionException {
        checkId(id, "id");
        if (!containers.containsKey(id)) {
            throwExecutionException("Undeploying container " + id, "Container is not known. Cannot migrate container.");
        }
        ContainerDescriptor desc = containers.get(id);
        if (ContainerState.AVAILABLE == desc.getState() || ContainerState.STOPPED == desc.getState()) {
            containers.remove(id);
            EcsAas.notifyContainerRemoved(desc);
        } else {
            throwExecutionException("Undeploying container " + id, "Container is in state " + desc.getState() 
                + ". Cannot undeploy container.");
        }
    }
    
    /**
     * Changes the container state and notifies {@link EcsAas}.
     * 
     * @param container the container
     * @param state the new state
     * @throws ExecutionException if changing the state fails
     * @see ContainerState#validateTransition(ContainerState, ContainerState)
     */
    protected void setState(BasicContainerDescriptor container, ContainerState state) throws ExecutionException {
        ContainerState.validateTransition(container.getState(), state);
        container.setState(state);
        EcsAas.notifyContainerStateChanged(container);
    }
    
    /**
     * Returns a service descriptor.
     * 
     * @param id the service id, or the URI used to add the container as fallback
     * @param idText the id text to be passed to {@link #checkId(String, String)}
     * @param activityText a description of the activity the service is requested for to construct an exception if 
     *   the service does not exist
     * @return the service (not <b>null</b>)
     * @throws ExecutionException if id is invalid or the service is unknown
     */
    protected C getContainer(final String id, String idText, String activityText) 
        throws ExecutionException {
        checkId(id, idText);
        C result = containers.get(id);
        if (null == result) {
            Optional<C> fallback = containers.values()
                .stream()
                .filter(c -> id.equals(c.getUri().toString()))
                .findAny();
            if (fallback.isPresent()) {
                result = fallback.get();
            }
        }
        if (null == result) {
            throwExecutionException(null, "Container id '" + id + "' is not known. Cannot " + activityText 
                + " container.");
        }
        return result;
    }
    
    /**
     * Checks the given {@code id} for basic validity.
     * 
     * @param id the id to check
     * @param text the text to include into the exception
     * @throws ExecutionException if {@code id} is not considered valid
     */
    protected void checkId(String id, String text) throws ExecutionException {
        if (null == id || id.length() == 0) {
            throwExecutionException(null, "Container " + text + "must be given (not null or empty)");
        }
    }
    
    @Override
    public void migrateContainer(String id, String resourceId) throws ExecutionException {
        checkId(id, "id");
        if (!containers.containsKey(id)) {
            throwExecutionException("Migrating container " + id, "Container is not known. Cannot migrate container.");
        }
        ContainerDescriptor desc = containers.get(id);
        if (ContainerState.DEPLOYED == desc.getState()) {
            stopContainer(id);
        } else {
            throwExecutionException("Migrating container " + id, "Container is in state " + desc.getState() 
                + ". Cannot undeploy container.");
        }
    }
    
    // checkstyle: stop exception type check
    
    /**
     * Throws an execution exception for the given throwable.
     * @param action the actual action to log (may be <b>null</b> or empty)
     * @param th the throwable
     * @throws ExecutionException the constructed exception
     */
    protected void throwExecutionException(String action, Throwable th) throws ExecutionException {
        if (action != null && action.length() > 0) {
            action = action + ":";
        } else {
            action = "";
        }
        LoggerFactory.getLogger(getClass()).error(action + th.getMessage());
        throw new ExecutionException(th);
    }

    // checkstyle: resume exception type check

    /**
     * Throws an execution exception for the given message.
     * @param action the actual action to log (may be <b>null</b> or empty)
     * @param message the message for the exception
     * @throws ExecutionException the constructed exception
     */
    protected void throwExecutionException(String action, String message) throws ExecutionException {
        if (action != null && action.length() > 0) {
            action = action + ":";
        } else {
            action = "";
        }
        LoggerFactory.getLogger(getClass()).error(action + message);
        throw new ExecutionException(message, null);
    }

    /**
     * Resolves an URI taking {@link EcsSetup#getArtifactInfixes()} into account.
     * 
     * @param uri the default/base URI to be resolved
     * @param downloadDir the directory to store resolved files, may be <b>null</b> for temporary files
     * @return the resolve file
     * @throws IOException if the {@code uri} ultimately cannot be resolved
     */
    protected File resolveUri(URI uri, File downloadDir) throws IOException {
        EcsSetup setup = EcsFactory.getSetup();
        File result = null;
        List<String> infixes = setup.getArtifactInfixes();
        for (String infix : infixes) {
            String tmp = uri.toString();
            int pos = tmp.lastIndexOf('.');
            if (pos > 0) {
                while (infix.startsWith("-")) {
                    infix = infix.substring(1);
                }
                tmp = tmp.substring(0, pos) + "-" + infix + tmp.substring(pos);
                try {
                    result = UriResolver.resolveToFile(new URI(tmp), downloadDir);
                    if (result.exists()) {
                        break;
                    } else {
                        result = null;
                    }
                } catch (IOException e) {
                    LoggerFactory.getLogger(getClass()).info("Cannot resolve {}. Trying with further artifact infixes "
                        + "from {} or the base URI {}", tmp, uri, infixes);
                } catch (URISyntaxException e) {
                    LoggerFactory.getLogger(getClass()).info("Cannot resolve {} as this is not an URI ({}). Trying "
                        + "with further artifact infixes from {} or the base URI {}", tmp, e.getMessage(), 
                        uri, infixes);
                }
            } else {
                LoggerFactory.getLogger(getClass()).warn("Cannot apply artifact infixes {} as base URI does not "
                    + "seem to point to a file.", infixes, uri);
                break;
            }
        }
        if (null == result) {
            result = UriResolver.resolveToFile(uri, downloadDir);
        }
        return result;
    }

}
