/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.basyx2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.AuthenticationDescriptor;
import de.iip_ecosphere.platform.support.aas.SetupSpec;
import de.iip_ecosphere.platform.support.aas.SetupSpec.AasComponent;
import de.iip_ecosphere.platform.support.aas.SetupSpec.ComponentSetup;
import de.iip_ecosphere.platform.support.aas.SetupSpec.State;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.basyx2.apps.common.AssetServerKeyStoreDescriptor;
import de.iip_ecosphere.platform.support.function.IORunnable;
import de.iip_ecosphere.platform.support.net.KeyStoreDescriptor;

/**
 * Basic implementation of the the {@link AasServer}.
 * 
 * @author Holger Eichelberger, SSE
 */
abstract class BaSyxAbstractAasServer implements AasServer {

    public enum ServerType {
        REGISTRY,
        REPOSITORY,
        COMBINED
    }
    
    private static final boolean DEBUG = false;
    private SetupSpec spec;
    private ServerType type;
    private ConfigurableApplicationContext aasRepoCtx;
    private ConfigurableApplicationContext smRepoCtx;
    private ConfigurableApplicationContext aasRegistryCtx;
    private ConfigurableApplicationContext smRegistryCtx;
    private List<IORunnable> actionsAfterStart;
    
    /**
     * Creates a new BaSyx AAS server.
     * 
     * @param spec the setup specification
     * @param type the server type
     * @param options optional server options
     */
    BaSyxAbstractAasServer(SetupSpec spec, ServerType type, String... options) {
        this.spec = spec;
        this.type = type;
    }
    
    /**
     * Adds actions to be executed after starting all servers.
     * 
     * @param actions the actions
     * @return <b>this</b> for chaining
     */
    public BaSyxAbstractAasServer addActionsAfterStart(List<IORunnable> actions) {
        if (null == actionsAfterStart) {
            actionsAfterStart = new ArrayList<>();
        }
        actionsAfterStart.addAll(actions);
        return this;
    }
    
    /**
     * Returns the class to use implementing the AAS repository application/server. 
     * 
     * @return the class
     */
    protected abstract Class<?> getAasRepositoryAppClass();

    /**
     * Returns the class to use implementing the submodel repository application/server. 
     * 
     * @return the class
     */
    protected abstract Class<?> getSmRepositoryAppClass();

    /**
     * Returns the class to use implementing the AAS registry application/server. 
     * 
     * @return the class
     */
    protected abstract Class<?> getAasRegistryAppClass();

    /**
     * Returns an optional application context initializer for AAS registries.
     * 
     * @return the initializer, may be <b>null</b> for none
     */
    protected ApplicationContextInitializer<ConfigurableApplicationContext> getAasRegistryAppInitializer() {
        return null;
    }

    /**
     * Returns the class to use implementing the submodel registry application/server. 
     * 
     * @return the class
     */
    protected abstract Class<?> getSmRegistryAppClass();

    /**
     * Returns an optional application context initializer for submodel registries.
     * 
     * @return the initializer, may be <b>null</b> for none
     */
    protected ApplicationContextInitializer<ConfigurableApplicationContext> getSmRegistryAppInitializer() {
        return null;
    }    
    
    /**
     * Creates a context.
     * 
     * @param cls the class to create the context for
     * @param port the server port
     * @return the context
     */
    protected static ConfigurableApplicationContext createContext(Class<?> cls, int port) {
        return createContext(cls, port, null);
    }    
    
    /**
     * Creates a context.
     * 
     * @param cls the class to create the context for
     * @param port the server port
     * @return the context
     */
    protected static ConfigurableApplicationContext createContext(Class<?> cls, int port, 
        AppConfigurer configurer) {
        if (DEBUG) {
            configurer.addDebugging();
        }
        return createContext(cls, port, configurer, null);
    }
    
    /**
     * Supports Spring app configuration.
     */
    protected static class AppConfigurer {
        
        private List<String> args = new ArrayList<>();
        private List<String> profiles = new ArrayList<>();
        private List<ApplicationContextInitializer<ConfigurableApplicationContext>> initializers = new ArrayList<>();
        
        /**
         * Adds a context initializer.
         * 
         * @param initializer the initializer instance, may be <b>null</b> for none
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addInitializer(
            ApplicationContextInitializer<ConfigurableApplicationContext> initializer) {
            if (null != initializer) {
                initializers.add(initializer);
            }
            return this;
        }
        
        /**
         * Adds a application initializer that registers a singled bean of type {@code cls} with the given 
         * {@code instance}.
         * 
         * @param <T> the type of the bean
         * @param cls the class of the bean representing the type
         * @param instance the instance to register as bean
         * @return <b>this</b> for chaining
         */
        public <T> AppConfigurer addBeanRegistrationInitializer(Class<T> cls, T instance) {
            if (null != instance) {
                addInitializer(new ApplicationContextInitializer<ConfigurableApplicationContext>() {
                    
                    @Override
                    public void initialize(ConfigurableApplicationContext applicationContext) {
                        applicationContext.getBeanFactory().registerSingleton(cls.getCanonicalName(), instance);
                    }
                    
                });
            }
            return this;
        }
        
        /**
         * Adds the spring tomcat server port.
         * 
         * @param port the port
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addPort(int port) {
            args.add("--server.port=" + port);
            return this;
        }

        /**
         * Adds the spring configuration (file) name.
         * 
         * @param name the name (without extension
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addConfigName(String name) {
            args.add("--spring.config.name=" + name);
            return this;
        }
        
        /**
         * Adds spring application debugging flags.
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addDebugging() {
            args.add("--debug");
            return this;
        }
        
        /**
         * Adds Java SSL debugging (System property).
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addJavaSslDebugging() {
            System.setProperty("javax.net.debug", "ssl:handshake");        
            return this;
        }
        
        /**
         * Adds Java HTTP client debugging (System property).
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addJavaHttpClientDebugging() {
            System.setProperty("jdk.httpclient.HttpClient.log", "all");
            System.setProperty("jdk.internal.httpclient.debug", "true");
            return this;
        }
        
        /**
         * Adds an application argument.
         * 
         * @param arg the argument
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addArg(String arg) {
            args.add(arg);
            return this;
        }
        
        /**
         * Adds spring web startup debugging flags.
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addWebStartupDebugging() {
            args.add("--logging.level.org.springframework.web=DEBUG");
            args.add("--logging.level.org.springframework.web.server.adapter.HttpWebHandlerAdapter=TRACE");
            args.add("--logging.level.org.springframework.web.reactive.function.client.ExchangeFunctions=trace");
            args.add("--spring.codec.log-request-details=true");
            args.add("--spring.mvc.log-request-details=true");
            return this;
        }

        /**
         * Adds spring security debugging flags.
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addSpringSecurityDebugging() {
            args.add("--logging.level.org.springframework.security=DEBUG");
            return this;
        }
        
        /**
         * Enables/disables BaSyx authorization.
         * 
         * @param enable enable or disable
         * @return <b>this</b> for chaining
         */
        public AppConfigurer setBasyxAuthorization(boolean enable) {
            args.add("--registry.authorization=" + (enable ? "Enabled" : "Disabled"));
            args.add("--basyx.feature.authorization.enabled=" + enable);
            return this;
        }

        /**
         * Adds extensive startup bean debugging.
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addStartupBeanDebugging() {
            args.add("--logging.level.org.springframework.beans.factory=DEBUG"); 
            return this;
        }
        
        /**
         * Adds additional application profiles.
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addAdditionalProfiles(String... profiles) {
            for (String p : profiles) {
                if (null != p) {
                    this.profiles.add(p);
                }
            }
            return this;
        }
        
        /**
         * Sets up the application testing in oktoflow (profile, disable BaSyx authorization).
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addTestingProfile() {
            profiles.add("test");
            setBasyxAuthorization(false);
            return this;
        }
        
        /**
         * Adds a keystore.
         * 
         * @param kstore the keystore to be used, may be <b>null</b>
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addKeystore(KeyStoreDescriptor kstore) {
            return addBeanRegistrationInitializer(KeyStoreDescriptor.class, kstore);
        }

        /**
         * Adds a keystore as {@link AssetServerKeyStoreDescriptor}.
         * 
         * @param kstore the keystore to be used, may be <b>null</b>
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addAssetServerKeystore(KeyStoreDescriptor kstore) {
            if (null != kstore) {
                addBeanRegistrationInitializer(AssetServerKeyStoreDescriptor.class, 
                    new AssetServerKeyStoreDescriptor(kstore)); 
            }
            return this;
        }

        /**
         * Adds an authenticaton descriptor.
         * 
         * @param auth the authentication descriptor to be used, may be <b>null</b>
         * 
         * @return <b>this</b> for chaining
         */
        public AppConfigurer addAuthenticaton(AuthenticationDescriptor auth) {
            return addBeanRegistrationInitializer(AuthenticationDescriptor.class, auth);
        }

        /**
         * Configures the given {@code app}.
         * 
         * @param app the app to configure
         */
        private void configure(SpringApplication app) {
            if (!profiles.isEmpty()) {
                app.setAdditionalProfiles(profiles.toArray(new String[profiles.size()]));
            }
            for (ApplicationContextInitializer<ConfigurableApplicationContext> i : initializers) {
                app.addInitializers(i);
            }
        }

        /**
         * Returns the arguments for starting a spring application.
         * 
         * @return the arguments
         */
        private String[] getArgs() {
            return args.toArray(new String[args.size()]);
        }

    }

    /**
     * Creates a default context configurer.
     * 
     * @return the configurer
     */
    static AppConfigurer createConfigurer() {
        return createConfigurer(null, null);
    }

    /**
     * Creates a default context configurer.
     * 
     * @param setup the component setup corresponding to the application, may be <b>null</b> for none
     * @return the configurer
     */
    static AppConfigurer createConfigurer(ComponentSetup setup) {
        return createConfigurer(null, setup);
    }

    /**
     * Creates a default context configurer with {@code initializer}.
     * 
     * @param initializer the initializer, may be <b>null</b> for none
     * @return the configurer
     */
    static AppConfigurer createConfigurer(
        ApplicationContextInitializer<ConfigurableApplicationContext> initializer) {
        return createConfigurer(initializer, null);
    }

    /**
     * Creates a default context configurer with {@code initializer}.
     * 
     * @param initializer the initializer, may be <b>null</b> for none
     * @param setup the component setup corresponding to the application, may be <b>null</b> for none
     * @return the configurer
     */
    static AppConfigurer createConfigurer(
        ApplicationContextInitializer<ConfigurableApplicationContext> initializer, ComponentSetup setup) {
        AppConfigurer result = new AppConfigurer()
            .addInitializer(initializer);
        if (null != setup) {
            result.addKeystore(setup.getKeyStore());
            result.addAuthenticaton(setup.getAuthentication());
        }
        if (DEBUG) {
            result.addDebugging();
            //result.addWebStartupDebugging();
            //result.addSpringSecurityDebugging();
            //result.addStartupBeanDebugging();
            //result.addJavaSslDebugging();
        }
        if (isJUnitTest()) {
            result.addTestingProfile();
        } else {
            result.setBasyxAuthorization(false); // TODO preliminary
        }
        return result;
    }
    
    /**
     * Returns whether this thread is running in Junit.
     * 
     * @return {@code true} for junit, {@code false} else
     */
    public static boolean isJUnitTest() {  
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }           
        }
        return false;
    }
    
    /**
     * Creates a context with initialization.
     * 
     * @param cls the class to create the context for (may be <b>null</b> for none)
     * @param port the server port
     * @param configurer the application configurer
     * @param stateConsumer a function handling state changes on starting instances
     * @return the context (may be <b>null</b>)
     */
    protected static ConfigurableApplicationContext createContext(Class<?> cls, int port, 
        AppConfigurer configurer, Consumer<State> stateConsumer) {
        ConfigurableApplicationContext result = null;
        if (null != cls) {
            if (null == configurer) {
                configurer = createConfigurer();
            }
            LoggerFactory.getLogger(BaSyxAbstractAasServer.class).info("Starting {} on port {}", 
                cls.getSimpleName(), port);
            SpringApplication app = new SpringApplication(cls);
            if (null != configurer) {
                configurer.configure(app);
            }
            configurer.addPort(port);
            configurer.addConfigName(cls.getSimpleName());
            result = app.run(null == configurer ? new String[0] : configurer.getArgs());
            if (null != stateConsumer) {
                stateConsumer.accept(State.RUNNING);
            }
        }
        return result;
    }
    
    @Override
    public void deploy(Aas aas) throws IOException {
        BaSyxDeploymentRecipe.deploy(spec, aas);
    }
    
    @Override
    public void deploy(Aas aas, Submodel submodel) throws IOException {
        BaSyxRegistry registry = new BaSyxRegistry(spec);
        registry.createAas(aas, "");
        registry.createSubmodel(aas, submodel);
        registry.register(aas, submodel, "");
    }
    
    /**
     * Returns whether a server process shall be started based on the given state.
     * 
     * @param state the state
     * @return {@code true} for start
     */
    static boolean shallStart(State state) {
        return state == State.STOPPED;
    }

    @Override
    public AasServer start() {
        if (type == ServerType.COMBINED || type == ServerType.REPOSITORY) {
            if (shallStart(spec.getAasRepositoryState())) {
                aasRepoCtx = createContext(getAasRepositoryAppClass(), spec.getAasRepositoryEndpoint().getPort(), 
                    createConfigurer(spec.getSetup(AasComponent.AAS_REPOSITORY)), 
                    s -> spec.notifyAasRepositoryStateChange(s));
            }
            if (shallStart(spec.getSubmodelRepositoryState())) {
                smRepoCtx = createContext(getSmRepositoryAppClass(), spec.getSubmodelRepositoryEndpoint().getPort(), 
                    createConfigurer(spec.getSetup(AasComponent.SUBMODEL_REPOSITORY)), 
                        s -> spec.notifySubmodelRepositoryStateChange(s));
            }
        }
        if ((type == ServerType.COMBINED && !spec.areRegistriesRunning()) || type == ServerType.REGISTRY) {
            if (shallStart(spec.getAasRegistryState())) {
                aasRegistryCtx = createContext(getAasRegistryAppClass(), spec.getAasRegistryEndpoint().getPort(),  
                    createConfigurer(getAasRegistryAppInitializer(), spec.getSetup(AasComponent.AAS_REGISTRY)), 
                        s -> spec.notifyAasRegistryStateChange(s));
            }
            if (shallStart(spec.getSubmodelRegistryState())) {
                smRegistryCtx = createContext(getSmRegistryAppClass(), spec.getSubmodelRegistryEndpoint().getPort(), 
                    createConfigurer(getSmRegistryAppInitializer(), spec.getSetup(AasComponent.SUBMODEL_REGISTRY))
                        .addAssetServerKeystore(spec.getAssetServerKeyStore()), 
                        s -> spec.notifySubmodelRegistryStateChange(s));
                spec.notifySubmodelRegistryStateChange(State.RUNNING);
            }
        }
        if (null != actionsAfterStart) {
            for (IORunnable a : actionsAfterStart) {
                try {
                    a.run();
                } catch (IOException e) {
                    LoggerFactory.getLogger(BaSyxAbstractAasServer.class).error("Cannot execute start action: {}", 
                        e.getMessage());
                }
            }
        }
        return this;
    }
    
    /**
     * Closes an application context.
     * 
     * @param ctx the context, may be <b>null</b> (ignored then)
     * @param stateConsumer optional state consumer to be called when {@code ctx} is closed, may be <b>null</b>
     */
    static void close(ConfigurableApplicationContext ctx, Consumer<State> stateConsumer) {
        if (null != ctx) {
            LoggerFactory.getLogger(BaSyxAbstractAasServer.class).info("Stopping context {}", ctx.getDisplayName());
            ctx.close();
            if (null != stateConsumer) {
                stateConsumer.accept(State.STOPPED);
            }
        }
    }

    @Override
    public void stop(boolean dispose) {
        close(aasRepoCtx, s -> spec.notifyAasRepositoryStateChange(s));
        close(smRepoCtx, s -> spec.notifySubmodelRepositoryStateChange(s));
        close(aasRegistryCtx, s -> spec.notifyAasRegistryStateChange(s));
        close(smRegistryCtx, s -> spec.notifySubmodelRegistryStateChange(s));
    }

}
