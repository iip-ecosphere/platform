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

package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Basic implementation of the service interface (aligned with Python). Implementing classes shall at least either
 * have a no-arg constructor setting up the full/fallback service information, a signle string argument constructor 
 * taking the service id or a constructor like {@link #AbstractService(String, InputStream)}.
 * The three types of constructors are recognized by {@link #createInstance(String, Class, String, String)} or 
 * {@link #createInstance(ClassLoader, String, Class, String, String)} to be used from generated service code.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractService implements Service {

    private String id;
    private String name;
    private Version version;
    private String description;
    private boolean isDeployable; 
    private ServiceKind kind;
    private ServiceState state;

    /**
     * Fallback constructor setting most fields to "empty" default values.
     * 
     * @param kind the service kind
     */
    protected AbstractService(ServiceKind kind) {
        this("", kind);
    }

    /**
     * Fallback constructor setting most fields to "empty" default values.
     * 
     * @param id the id of the service
     * @param kind the service kind
     */
    protected AbstractService(String id, ServiceKind kind) {
        this(id, "", new Version(0, 0, 0), "", true, kind);
    }

    // checkstyle: stop parameter number check
    
    /**
     * Creates an abstract service.
     * 
     * @param id the id of the service
     * @param name the name of the service
     * @param version the version of the service
     * @param description a description of the service, may be empty
     * @param isDeployable whether the service is decentrally deployable
     * @param kind the service kind
     */
    protected AbstractService(String id, String name, Version version, String description, boolean isDeployable, 
        ServiceKind kind) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.description = description;
        this.isDeployable = isDeployable;
        this.kind = kind;
        this.state = ServiceState.AVAILABLE;
    }

    // checkstyle: resume parameter number check

    /**
     * Creates an abstract service from YAML information.
     * 
     * @param yaml the service information as read from YAML
     */
    protected AbstractService(YamlService yaml) {
        this(yaml.getId(), yaml.getName(), yaml.getVersion(), yaml.getDescription(), yaml.isDeployable(), 
            yaml.getKind());
    }
    
    /**
     * Creates an abstract service from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    protected AbstractService(String serviceId, InputStream ymlFile) {
        this(YamlArtifact.readFromYamlSafe(ymlFile).getServiceSafe(serviceId));
    }

    /**
     * Convenience method for creating service instances via the default constructor using the class loader of this 
     * class.
     * 
     * @param <S> the service type (parent interface of <code>className</code>)
     * @param className the name of the service class (must implement {@link Service} and provide a no-argument 
     *     constructor)
     * @param cls the class to cast to
     * @return the service instance (<b>null</b> if the service cannot be found/initialized)
     */
    public static <S extends Service> S createInstance(String className, Class<S> cls) {
        return createInstance(AbstractService.class.getClassLoader(), className, cls, null, null);
    }

    /**
     * Convenience method for creating service instances using the class loader of this class.
     * 
     * @param <S> the service type (parent interface of <code>className</code>)
     * @param className the name of the service class (must implement {@link Service} and provide a no-argument 
     *     constructor)
     * @param cls the class to cast to
     * @param serviceId the id of the service as given in {@code deploymentDescFile} (may be <b>null</b>, then the 
     *     default constructor is invoked)
     * @param deploymentDescFile the resource name of the deployment descriptor containing a YAML artifact with the 
     *     service description (may be <b>null</b>, then the default constructor is invoked)
     * @return the service instance (<b>null</b> if the service cannot be found/initialized)
     */
    public static <S extends Service> S createInstance(String className, Class<S> cls, String serviceId, 
        String deploymentDescFile) {
        return createInstance(AbstractService.class.getClassLoader(), className, cls, serviceId, deploymentDescFile);
    }

    /**
     * Convenience method for creating service instances.
     * 
     * @param <S> the service type (parent interface of <code>className</code>)
     * @param loader the class loader to load the class with
     * @param className the name of the service class (must implement {@link Service} and provide a no-argument 
     *     constructor)
     * @param cls the class to cast to
     * @param serviceId the id of the service as given in {@code deploymentDescFile} (may be <b>null</b>, then the 
     *     default constructor is invoked)
     * @param deploymentDescFile the resource name of the deployment descriptor containing a YAML artifact with the 
     *     service description (may be <b>null</b>, then the default constructor is invoked)
     * @return the service instance (<b>null</b> if the service cannot be found/initialized)
     */
    public static <S extends Service> S createInstance(ClassLoader loader, String className, Class<S> cls, 
        String serviceId, String deploymentDescFile) {
        S result = null;
        try {
            Class<?> serviceClass = loader.loadClass(className);
            Object instance = null;
            if (null != serviceId && null != deploymentDescFile) {
                try {
                    Constructor<?> cons = serviceClass.getConstructor(String.class, InputStream.class);
                    InputStream desc = loader.getResourceAsStream(deploymentDescFile);
                    instance = cons.newInstance(serviceId, desc);
                    if (null != desc) {
                        desc.close();
                    }
                } catch (NoSuchMethodException e) {
                    // see null == instance
                } catch (InvocationTargetException e) {
                    LoggerFactory.getLogger(AbstractService.class).error("While instantiating " + className + ": " 
                        + e.getMessage() + ", falling back to default constructor");
                } catch (IOException e) {
                    LoggerFactory.getLogger(AbstractService.class).error("While instantiating " + className + " here "
                        + "loading descriptor " + deploymentDescFile + ": " + e.getMessage() + ", falling back to "
                        + "default constructor");
                }
            }
            if (null == instance && null != serviceId) {
                try {
                    Constructor<?> cons = serviceClass.getConstructor(String.class);
                    instance = cons.newInstance(serviceId);
                } catch (NoSuchMethodException e) {
                    // see null == instance
                } catch (InvocationTargetException e) {
                    LoggerFactory.getLogger(AbstractService.class).error("While instantiating " + className + ": " 
                        + e.getMessage() + ", falling back to default constructor");
                }
            }
            
            if (null == instance) {
                instance = serviceClass.newInstance();
            }
            result = cls.cast(instance);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
            LoggerFactory.getLogger(AbstractService.class).error("Cannot instantiate service of type '" 
                + className + "': " + e.getMessage() + ". Service will not be functional!");
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
    public Version getVersion() {
        return version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ServiceState getState() {
        return state;
    }

    @Override
    public void setState(ServiceState state) throws ExecutionException {
        this.state = state; // for now, no state machine checking
    }

    @Override
    public boolean isDeployable() {
        return isDeployable;
    }

    @Override
    public ServiceKind getKind() {
        return kind;
    }
    
    @Override
    public void activate() throws ExecutionException {
        if (getState() == ServiceState.PASSIVATED) {
            setState(ServiceState.RUNNING);
        }
    }

    @Override
    public void passivate() throws ExecutionException {
        if (getState() == ServiceState.RUNNING) {
            setState(ServiceState.PASSIVATED);
        }
    }

}
