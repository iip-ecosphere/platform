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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.ServiceStub;
import de.iip_ecosphere.platform.services.environment.switching.ServiceBase;
import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * A basic re-usable implementation of the service manager. Implementations shall override at least 
 * {@link #switchToService(String, String)}, {@link #migrateService(String, String)}
 * and call the implementation of this class to perform the changes. Implementations shall call the notify methods 
 * in {@link ServicesAas}.
 *
 * @param <A> the actual type of the artifact descriptor
 * @param <S> the actual type of the service descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServiceManager<A extends AbstractArtifactDescriptor<S>, 
    S extends AbstractServiceDescriptor<A>> implements ServiceManager {

    private Map<String, A> artifacts = Collections.synchronizedMap(new HashMap<>());

    /**
     * Returns the available connector testing predicate.
     * 
     * @return the predicate
     */
    protected abstract Predicate<TypedDataConnectorDescriptor> getAvailablePredicate();
    
    @Override
    public Set<String> getArtifactIds() {
        return artifacts.keySet();
    }
    
    @Override
    public Collection<A> getArtifacts() {
        return artifacts.values();
    }
    
    @Override
    public Set<String> getServiceIds() {
        Set<String> result = new HashSet<>();
        for (A a : getArtifacts()) {
            result.addAll(a.getServiceIds());
        }
        return result;
    }

    @Override
    public Collection<S> getServices() {
        Set<S> result = new HashSet<>();
        for (A a : getArtifacts()) {
            result.addAll(a.getServices());
        }
        return result;
    }

    @Override
    public A getArtifact(String artifactId) {
        return null == artifactId ? null : artifacts.get(artifactId);
    }
    
    @Override
    public S getService(String serviceId) {
        S result = null;
        for (A a : getArtifacts()) {
            result = a.getService(serviceId);
            if (null != result) {
                break;
            }
        }
        return result;
    }
    
    @Override
    public ServiceState getServiceState(String serviceId) {
        ServiceState result = ServiceState.UNKNOWN;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getState();
        }
        return result;
    }
    
    /**
     * Adds an artifact.
     * 
     * @param artifactId the artifact id
     * @param descriptor the service descriptor
     * @return {@code artifactId}
     * @throws ExecutionException in case that the id is invalid or already known
     */
    protected String addArtifact(String artifactId, A descriptor) throws ExecutionException {
        checkId(artifactId, "artifactId");
        if (artifacts.containsKey(artifactId)) {
            throw new ExecutionException("Artifact id '" + artifactId + "' " + EXC_ALREADY_KNOWN, null);
        }
        artifacts.put(artifactId, descriptor);
        ServicesAas.notifyArtifactAdded(descriptor);
        return artifactId;
    }

    @Override
    public void removeArtifact(String artifactId) throws ExecutionException {
        checkId(artifactId, "artifactId");
        if (!artifacts.containsKey(artifactId)) {
            final String aId = artifactId;
            Optional<A> fallback = artifacts.values()
                .stream()
                .filter(a -> aId.equals(a.getUri().toString()))
                .findAny();
            if (fallback.isPresent()) {
                artifactId = fallback.get().getId();
            }
        }
        if (!artifacts.containsKey(artifactId)) {
            throw new ExecutionException("Artifact id '" + artifactId 
                + "' is not known. Cannot remove artifact.", null);
        }
        A aDesc = artifacts.remove(artifactId);
        ServicesAas.notifyArtifactRemoved(aDesc);
    }
    
    
    /**
     * Returns whether the given {@code id} is structurally valid, i.e., not <b>null</b> and not empty.
     * 
     * @param id the id to check
     * @return {@code true} if {@code id} is valid, {@code false} else
     */
    protected static final boolean isValidId(String id) {
        return null != id && id.length() > 0;
    }

    /**
     * Returns whether the given {@code id} is structurally valid, i.e., not <b>null</b> and not empty, but not 
     * {@code butId}.
     * 
     * @param id the id to check
     * @param butId the id to exclude
     * @return {@code true} if {@code id} is valid, {@code false} else
     */
    protected static final boolean isValidIdBut(String id, String butId) {
        return isValidId(id) && !id.equals(butId);
    }

    /**
     * Checks the given {@code id} for basic validity.
     * 
     * @param id the id to check
     * @param text the text to include into the exception
     * @throws ExecutionException if {@code id} is not considered valid
     * @see #isValidId(String)
     */
    protected static void checkId(String id, String text) throws ExecutionException {
        if (!isValidId(id)) {
            throw new ExecutionException(text + "must be given (not null or empty)", null);
        }
    }
    
    @Override
    public void switchToService(String serviceId, String targetId) throws ExecutionException {
        checkId(serviceId, "id");
        checkId(serviceId, "targetId");
        if (!serviceId.equals(targetId)) {
            stopService(serviceId);
            startService(targetId);
        }
    }

    @Override
    public void migrateService(String serviceId, String resourceId) throws ExecutionException {
        checkId(serviceId, "serviceId");
        S cnt = getServiceDescriptor(serviceId, "serviceId", "migrate");
        if (ServiceState.RUNNING == cnt.getState()) {
            stopService(serviceId);
        } else {
            throw new ExecutionException("Service " + serviceId + " is in state " + cnt.getState() 
                + ". Cannot migrate service.", null);
        }
    }
    
    @Override
    public void passivateService(String serviceId) throws ExecutionException {
        S service = getServiceDescriptor(serviceId, "serviceId", "passivate");
        ServiceStub stub = service.getStub();
        if (ServiceState.RUNNING == service.getState() || null == stub) {
            setState(service, ServiceState.PASSIVATING);
            stub.passivate();
            setState(service, ServiceState.PASSIVATED);
        } else {
            throw new ExecutionException("Cannot passivate service '" + serviceId + "'as it is in state " 
                + service.getState() + "/not running.", null);
        }
    }
    
    @Override
    public void activateService(String serviceId) throws ExecutionException {
        S service = getServiceDescriptor(serviceId, "serviceId", "activate");
        ServiceStub stub = service.getStub();
        if (ServiceState.PASSIVATED == service.getState() || null == stub) {
            stub.activate();
            setState(service, ServiceState.RUNNING);
        } else {
            throw new ExecutionException("Cannot passivate as service is in state " + service.getState(), null);
        }
    }

    
    @Override
    public void reconfigureService(String serviceId, Map<String, String> values) throws ExecutionException {
        S service = getServiceDescriptor(serviceId, "serviceId", "reconfigure");
        ServiceStub stub = service.getStub();
        if (stub != null) {
            ServiceState state = service.getState();
            setState(service, ServiceState.RECONFIGURING);
            stub.reconfigure(values);
            setState(service, state);
        } else {
            throw new ExecutionException("Cannot reconfigure service '" + serviceId + "'as it is in state " 
                + service.getState() + "/not running.", null);
        }
    }
    
    /**
     * Returns the service stub for implementing the service operations.
     * 
     * @param service the service to return the stub for
     * @return the stub, may be <b>null</b> if the service is not running
     */
    protected ServiceStub getStub(S service) {
        return service.getStub();
    }

    /**
     * Returns a service descriptor.
     * 
     * @param artifactId the artifact id
     * @param idText the id text to be passed to {@link #checkId(String, String)}
     * @param activityText a description of the activity the service is requested for to construct an exception if 
     *   the service does not exist
     * @return the service (not <b>null</b>)
     * @throws ExecutionException if id is invalid or the service is unknown
     */
    protected A getArtifactDescriptor(String artifactId, String idText, String activityText) throws ExecutionException {
        checkId(artifactId, idText);
        A result = artifacts.get(artifactId);
        if (null == result) {
            throw new ExecutionException("Artifact id '" + artifactId + "' is not known. Cannot " + activityText 
                + " service.", null);
        }
        return result;
    }

    /**
     * Returns a service descriptor.
     * 
     * @param serviceId the service id
     * @param idText the id text to be passed to {@link #checkId(String, String)}
     * @param activityText a description of the activity the service is requested for to construct an exception if 
     *   the service does not exist
     * @return the service (not <b>null</b>)
     * @throws ExecutionException if id is invalid or the service is unknown
     */
    protected S getServiceDescriptor(String serviceId, String idText, String activityText) throws ExecutionException {
        checkId(serviceId, idText);
        S result = getService(serviceId);
        if (null == result) {
            throw new ExecutionException("Service id '" + serviceId + "' is not known. Cannot " + activityText 
                + " service.", null);
        }
        return result;
    }

    @Override
    public void setServiceState(String serviceId, ServiceState state) throws ExecutionException {
        setState(getServiceDescriptor(serviceId, "serviceId", "setState"), state);
    }
    
    /**
     * Changes the service state and notifies {@link ServicesAas}.
     * 
     * @param service the service
     * @param state the new state
     * @throws ExecutionException if changing the state fails
     */
    protected void setState(ServiceDescriptor service, ServiceState state) throws ExecutionException {
        ServiceState old = service.getState();
        service.setState(state);
        ServicesAas.notifyServiceStateChanged(old, state, service); 
    }
    
    @Override
    public List<TypedDataDescriptor> getParameters(String serviceId) {
        List<TypedDataDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getParameters();
        }
        return result;
    }

    @Override
    public List<TypedDataConnectorDescriptor> getInputDataConnectors(String serviceId) {
        List<TypedDataConnectorDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getInputDataConnectors();
        }
        return result;
    }

    @Override
    public List<TypedDataConnectorDescriptor> getOutputDataConnectors(String serviceId) {
        List<TypedDataConnectorDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getOutputDataConnectors();
        }
        return result;
    }

    /**
     * Sorts the given list by the dependencies specified in the deployment descriptor.
     * 
     * @param serviceIds the service ids to sort
     * @param start sequence for service start
     * @return the sorted service ids
     */
    protected String[] sortByDependency(String[] serviceIds, boolean start) {
        List<ServiceDescriptor> services = new ArrayList<ServiceDescriptor>();
        for (String s : serviceIds) {
            services.add(getService(s));
        }
        
        Predicate<TypedDataConnectorDescriptor> available;
        if (start) {
            available = getAvailablePredicate();
        } else {
            available = d -> true;
        }
        
        return sortByDependency(services, getServices(), available, !start)
            .stream()
            .map(s -> s.getId())
            .toArray(size -> new String[size]);
    }
    
    /**
     * Sorts a list of services by their dependencies, considering prerequisite input nodes outside the own ensemble.
     * Ensemble nodes are simply listed after their ensemble leader. [public, static for testing]
     * 
     * @param <S> the service type
     * @param services the services to sort
     * @param localServices all known local services including {@code services}
     * @param available the available predicate
     * @param reverse reverse the order (for stopping)
     * @return the list of sorted services
     */
    public static <S extends ServiceDescriptor> List<S> sortByDependency(List<S> services, 
        Collection<? extends ServiceDescriptor> localServices, Predicate<TypedDataConnectorDescriptor> available, 
        boolean reverse) {
        List<S> result = new ArrayList<S>();

        // idea... sort services by their output connections/dependencies adding first those that have no dependencies.
        // for all other, add them only if 1) ensemble leader has already been added (for ensemble members) or 2) all
        // non ensemble-connections (assuming that they will be available after ensemble start) are available
        // collect all ensemble-internal connectors
        Set<String> ensembleConnections = new HashSet<>();
        for (ServiceDescriptor s : services) {
            // empty for non-ensemble leaders, connections for ensemble leaders, ensemble-members just repeat the 
            // information; might be ok to use only non ensemble-members, but no guarantee that services also contains
            // all ensemble leaders
            ensembleConnections.addAll(AbstractServiceDescriptor.ensembleConnectorNames(s));
        }
        Set<String> internalConnections = AbstractServiceDescriptor.internalConnectorNames(localServices);
        internalConnections.removeAll(ensembleConnections);

        // process the services, exclude the ensemble connections
        Set<ServiceDescriptor> processed = new HashSet<ServiceDescriptor>();
        Set<String> avail = new HashSet<>();
        int before;
        boolean externalPrio = true;
        do {
            before = result.size();
            for (S sd : services) {
                boolean ok = true;
                if (processed.contains(sd)) {
                    continue;
                }
                if (null != sd.getEnsembleLeader()) { 
                    // ensemble leader must be started before, hull dependencies are "mapped" to ensemble leader
                    ok = processed.contains(sd.getEnsembleLeader());
                } else {
                    for (TypedDataConnectorDescriptor out : sd.getOutputDataConnectors()) {
                        String outName = out.getName();
                        if (externalPrio && internalConnections.contains(outName)) {
                            ok = false;
                            break; // defer to later stage
                        }
                        // ensemble-internal and already known available connections must not be tested
                        if (!ensembleConnections.contains(outName) && !avail.contains(outName)) {
                            ok = available.test(out);
                            if (ok) {
                                avail.add(outName);
                            } else {
                                LoggerFactory.getLogger(AbstractServiceManager.class).warn("Service prerequisite " 
                                    + out + " not available from service " + sd.getId());
                                break;
                            }
                        }             
                    }
                }
                if (ok) {
                    result.add(sd);
                    processed.add(sd);
                }
            }
            externalPrio = false;
        } while (before != result.size() && result.size() != services.size());
        // just add the remaining which may be parts of cycles
        for (S sd : services) {
            if (!processed.contains(sd)) {
                result.add(sd);
            }
        }
        if (reverse) {
            Collections.reverse(result);
        }
        return result;
    }
    
    /**
     * Returns whether {@code coll} contains {@code id}.
     * 
     * @param coll the collection to search within
     * @param id the id to search for, may be <b>null</b> or empty
     * @return {@code false} if {@link #isValidId(String) the id is not valid}, whether it is contained in 
     *     {@code coll} else
     */
    private static final boolean containsIdSafe(Collection<String> coll, String id) {
        boolean result;
        if (isValidId(id)) {
            result = coll.contains(id);
        } else {
            result = false;
        }
        return result;
    }
    
    /**
     * Wraps a {@link TypedDataConnectorDescriptor} to adjust the service (depending from the direction of 
     * traversal/creation).
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class TypedDataConnection implements TypedDataConnectorDescriptor {
        
        private TypedDataConnectorDescriptor connector;
        private String service;
        private boolean isInput;

        /**
         * Creates an instance.
         * 
         * @param connector the connector to be wrapped
         * @param service the service id to override the service id in {@code connector}, may be <b>null</b> for the 
         *     result of {@link TypedDataConnectorDescriptor#getService()}
         * @param isInput whether this is an input connection
         */
        public TypedDataConnection(TypedDataConnectorDescriptor connector, String service, boolean isInput) {
            this.connector = connector;
            this.service = service;
            this.isInput = isInput;
        }
        
        @Override
        public String getName() {
            return connector.getName();
        }
        
        @Override
        public String getService() {
            return null == service ? connector.getService() : service;
        }

        @Override
        public Class<?> getType() {
            return connector.getType();
        }

        @Override
        public String getDescription() {
            return connector.getDescription();
        }

        @Override
        public String getId() {
            return connector.getId();
        }

        /**
         * Is this an input connector.
         * 
         * @return {@code true} for an input connector, {@code false} for an output connector
         */
        public boolean isInput() {
            return isInput;
        }

        /**
         * Is this an output connector.
         * 
         * @return {@code true} for an output connector, {@code false} for an input connector 
         */
        public boolean isOutput() {
            return !isInput;
        }

        @Override
        public String getFunction() {
            return connector.getFunction();
        }
        
    }

    /**
     * Determines the external connections of the given services.
     * 
     * @param mgr the service manager
     * @param serviceIds the services to determine the connections for
     * @return the external connections
     */
    public static Set<TypedDataConnection> determineExternalConnections(ServiceManager mgr, 
        String... serviceIds) {
        Set<TypedDataConnection> result = new HashSet<TypedDataConnection>();
        Set<String> ids = CollectionUtils.addAll(new HashSet<>(), serviceIds);
        
        // collect artifacts, determine outgoing connections
        Set<ArtifactDescriptor> artifacts = new HashSet<>();
        for (String id : serviceIds) {
            ServiceDescriptor service = mgr.getService(id);
            if (null != service && service.isTopLevel()) {
                artifacts.add(service.getArtifact());
                for (TypedDataConnectorDescriptor c: service.getDataConnectors()) {
                    if (isValidIdBut(c.getService(), id) && !containsIdSafe(ids, c.getService())) {
                        result.add(new TypedDataConnection(c, null, service.getInputDataConnectors().contains(c)));
                    }
                }
            }
        }
        
        // determine incoming channels for all services in serviceIds
        
        for (ArtifactDescriptor a : artifacts) {
            for (ServiceDescriptor s: a.getServices()) {
                if (s.isTopLevel() && !containsIdSafe(ids, s.getId())) { // no connections within the given serviceIds
                    for (TypedDataConnectorDescriptor c: s.getDataConnectors()) {
                        if (!s.getId().equals(c.getService()) && containsIdSafe(ids, c.getService())) {
                            result.add(new TypedDataConnection(c, s.getId(), s.getInputDataConnectors().contains(c)));
                        }
                    }
                }
            }
        }
        
        return result;
    }

    /**
     * Determines all connections within {@code serviceIds}.
     * 
     * @param mgr the service manager
     * @param serviceIds the services to determine the connections for
     * @return the internal connections
     */
    public static Set<TypedDataConnection> determineInternalConnections(ServiceManager mgr, 
        String... serviceIds) {
        Set<TypedDataConnection> result = new HashSet<TypedDataConnection>();
        Set<String> ids = CollectionUtils.addAll(new HashSet<>(), serviceIds);

        Set<ArtifactDescriptor> artifacts = new HashSet<>();
        for (String id : serviceIds) {
            ServiceDescriptor service = mgr.getService(id);
            if (null != service && service.isTopLevel()) {
                artifacts.add(service.getArtifact());
            }
        }

        for (ArtifactDescriptor a : artifacts) {
            for (ServiceDescriptor s: a.getServices()) {
                if (s.isTopLevel()) {
                    if (containsIdSafe(ids, s.getId())) { 
                        for (TypedDataConnectorDescriptor c: s.getOutputDataConnectors()) {
                            result.add(new TypedDataConnection(c, null, false));
                        }
                    }
                    for (TypedDataConnectorDescriptor c: s.getInputDataConnectors()) {
                        if (containsIdSafe(ids, c.getService())) {
                            result.add(new TypedDataConnection(c, null, true));
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determines all connections characterizing the functional output/input services in {@code serviceIds}.
     * 
     * @param mgr the service manager
     * @param serviceIds the services to determine the connections for
     * @return the internal connections
     */
    public static Set<TypedDataConnection> determineFunctionalConnections(ServiceManager mgr, 
        String... serviceIds) {
        Set<TypedDataConnection> result = new HashSet<TypedDataConnection>();
        Set<String> ids = CollectionUtils.addAll(new HashSet<>(), serviceIds);

        Set<ArtifactDescriptor> artifacts = new HashSet<>();
        for (String id : serviceIds) {
            ServiceDescriptor service = mgr.getService(id);
            if (null != service && service.isTopLevel()) {
                artifacts.add(service.getArtifact());
            }
        }

        for (ArtifactDescriptor a : artifacts) {
            for (ServiceDescriptor s: a.getServices()) {
                if (s.isTopLevel()) {
                    if (containsIdSafe(ids, s.getId())) {
                        if (ServiceKind.SOURCE_SERVICE == s.getKind()) {
                            for (TypedDataConnectorDescriptor c: s.getOutputDataConnectors()) {
                                result.add(new TypedDataConnection(c, null, false));
                            }
                        } else {
                            for (TypedDataConnectorDescriptor c: s.getInputDataConnectors()) {
                                result.add(new TypedDataConnection(c, null, true));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Returns only top-level services from {@code serviceIds}.
     * 
     * @param mgr the service manager to take the descriptors from
     * @param serviceIds the service ids to filter out
     * @return {@code serviceIds} or a subset of {@code serviceIds}
     */
    public static String[] topLevel(ServiceManager mgr, String... serviceIds) {
        List<String> result = new ArrayList<>();
        for (String id: serviceIds) {
            ServiceDescriptor desc = mgr.getService(id);
            if (null != desc && desc.isTopLevel()) {
                result.add(id);
            }
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Returns only non-server services from {@code serviceIds}, i.e., only instances
     * that can be identified as server are pruned.
     * 
     * @param mgr the service manager to take the descriptors from
     * @param serviceIds the service ids to filter out
     * @return {@code serviceIds} or a subset of {@code serviceIds}
     */
    public static String[] pruneServers(ServiceManager mgr, String... serviceIds) {
        List<String> result = new ArrayList<>();
        for (String id: serviceIds) {
            ServiceDescriptor desc = mgr.getService(id);
            if (null == desc) {
                desc = mgr.getService(ServiceBase.getServiceId(id));
            }
            if (null == desc || (null != desc && desc.getKind() != ServiceKind.SERVER)) {
                result.add(id);
            }
        }
        return result.toArray(new String[result.size()]);
    }
    
    /**
     * Checks for service instances regarding application id and application instance id. If needed, creates instances 
     * using existing service descriptors as templates via 
     * {@link #instantiateFromTemplate(AbstractServiceDescriptor, String)}.
     * 
     * @param sId service ids to check
     */
    protected void checkServiceInstances(String[] sId) {
        for (String id: sId) {
            if (null == getService(id)) {
                S template = null;
                String appInstanceId = ServiceBase.getApplicationInstanceId(id);
                String appId = ServiceBase.getApplicationId(id);
                String serviceId = ServiceBase.getServiceId(id);
                if (AbstractSetup.isNotEmpty(appInstanceId)) {
                    template = getService(ServiceBase.composeId(serviceId, appId));
                }
                if (null == template) {
                    for (String tId: getServiceIds()) {
                        S s = getService(tId);
                        if (s.getServiceId().equals(serviceId) && s.getApplicationId().equals(appId)) {
                            template = s;
                            break;
                        }
                    }
                }
                if (null == template) {
                    template = getService(serviceId);
                }
                if (null == template) {
                    LoggerFactory.getLogger(getClass()).warn("No service found for id {}, also no template found "
                        + "to instantiate.", id);
                } else {
                    instantiateFromTemplate(template, id);
                }
            }
        }
    }

    /**
     * Creates a service instance descriptor from {@code template}. Application and application instance id shall
     * match {@code template}.
     * 
     * @param template the service descriptor template
     * @param serviceId the service id, usually including application id and application instance id
     * @return the instantiated descriptor
     */
    protected abstract S instantiateFromTemplate(S template, String serviceId);
    
    /**
     * Clears the internal data. [testing]
     */
    public void clear() {
        artifacts.clear();
    }
    
    @Override
    public int getServiceInstanceCount(String serviceId) {
        int result = 0;
        String aId = ServiceBase.getApplicationId(serviceId);
        String sId = ServiceBase.getServiceId(serviceId);
        if (null == aId || aId.length() == 0) {
            if (null != getService(sId)) {
                result = 1;
            }
        } else {
            for (S service : getServices()) {
                if (aId.equals(service.getApplicationId()) && sId.equals(service.getServiceId())) {
                    result++;
                }
            }
        }
        return result;
    }

}
