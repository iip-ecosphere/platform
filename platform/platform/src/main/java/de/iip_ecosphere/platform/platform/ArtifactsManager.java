/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.BasicContainerDescriptor;
import de.iip_ecosphere.platform.platform.cli.ServiceDeploymentPlan;
import de.iip_ecosphere.platform.services.environment.YamlArtifact;
import de.iip_ecosphere.platform.support.JarUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.Version;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * A class holding/providing information about available artifacts.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ArtifactsManager {
    
    private static final ArtifactsManager INSTANCE = new ArtifactsManager();
    
    private static WatchRunnable runnable;
    private Map<String, Artifact> artifacts = Collections.synchronizedMap(new TreeMap<>());
    private Map<Path, Artifact> artifactPaths = Collections.synchronizedMap(new HashMap<>());

    /**
     * Denotes the artifact kind.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum ArtifactKind {
        
        /**
         * A service implementation artifact (ZIP/JAR).
         */
        SERVICE_ARTIFACT,
        
        /**
         * A (generated) container and its descriptor.
         */
        CONTAINER,
        
        /**
         * Deployment plan for multiple services/resources, like in the CLI.
         */
        DEPLOYMENT_PLAN;
    }

    /**
     * Represents an artifact.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface Artifact {
        
        /**
         * Returns the kind of the artifact.
         * 
         * @return the kind
         */
        public ArtifactKind getKind();
        
        /**
         * Returns the id of the artifact.
         * 
         * @return the ID
         */
        public String getId();

        /**
         * Returns the name of the artifact.
         * 
         * @return the name
         */
        public String getName();

        /**
         * Returns the description of the artifact.
         * 
         * @return the description
         */
        public String getDescription();
        
        /**
         * Returns the access URI.
         * 
         * @return the access URI
         */
        public URI getAccessUri();

        /**
         * Returns the version of the artifact.
         * 
         * @return the version
         */
        public Version getVersion();
        
    }

    /**
     * Basic abstract artifact implementation.
     * 
     * @author Holger Eichelberger, SSE
     */
    protected abstract static class AbstractArtifact implements Artifact {

        private URI accessUri;

        /**
         * Creates a service artifact instance.
         * 
         * @param accessUri the access URI for the devices
         */
        protected AbstractArtifact(URI accessUri) {
            this.accessUri = accessUri;
        }

        @Override
        public URI getAccessUri() {
            return accessUri;
        }

    }

    /**
     * Represents a service artifact.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ServiceArtifact extends AbstractArtifact {
        
        private YamlArtifact descriptor;
        
        /**
         * Creates a service artifact instance.
         * 
         * @param descriptor the YAML descriptor
         * @param accessUri the access URI for the devices
         */
        private ServiceArtifact(YamlArtifact descriptor, URI accessUri) {
            super(accessUri);
            this.descriptor = descriptor;
        }

        @Override
        public ArtifactKind getKind() {
            return ArtifactKind.SERVICE_ARTIFACT;
        }

        @Override
        public String getId() {
            return descriptor.getId();
        }

        @Override
        public String getName() {
            return descriptor.getName();
        }

        @Override
        public String getDescription() {
            return "";
        }
        
        @Override
        public Version getVersion() {
            return descriptor.getVersion();
        }

    }
    
    /**
     * Represents a service artifact.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class DeploymentPlanArtifact extends AbstractArtifact {
        
        private ServiceDeploymentPlan plan;
        
        /**
         * Creates a service deployment plan instance.
         * 
         * @param plan the service deployment plan
         * @param accessUri the access URI for the devices
         */
        private DeploymentPlanArtifact(ServiceDeploymentPlan plan, URI accessUri) {
            super(accessUri);
            this.plan = plan;
        }

        @Override
        public ArtifactKind getKind() {
            return ArtifactKind.DEPLOYMENT_PLAN;
        }

        @Override
        public String getId() {
            return plan.getId();
        }

        @Override
        public String getName() {
            return plan.getApplication();
        }

        @Override
        public String getDescription() {
            return plan.getDescription();
        }
        
        @Override
        public Version getVersion() {
            return plan.getVersion();
        }

    }

    /**
     * Represents a service artifact.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class ContainerArtifact extends AbstractArtifact {
        
        private BasicContainerDescriptor descriptor;
        
        /**
         * Creates a service artifact instance.
         * 
         * @param descriptor the YAML descriptor
         * @param accessUri the access URI for the devices
         */
        private ContainerArtifact(BasicContainerDescriptor descriptor, URI accessUri) {
            super(accessUri);
            this.descriptor = descriptor;
        }

        @Override
        public ArtifactKind getKind() {
            return ArtifactKind.CONTAINER;
        }

        @Override
        public String getId() {
            return descriptor.getId();
        }

        @Override
        public String getName() {
            return descriptor.getName();
        }

        @Override
        public String getDescription() {
            return "";
        }
        
        @Override
        public Version getVersion() {
            return descriptor.getVersion();
        }
        
    }

    /**
     * Returns the instance of the artifacts manager.
     * 
     * @return the instance
     */
    public static ArtifactsManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Implements a thread palling for watch service events.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class WatchRunnable implements Runnable {

        private WatchService watch;
        private boolean running = true;
        private Path path;

        /**
         * Creates the runnable.
         * 
         * @param watch the watch to look for
         * @param path the watched path
         */
        private WatchRunnable(WatchService watch, Path path) {
            this.watch = watch;
            this.path = path;
        }
        
        @Override
        public void run() {
            WatchKey key = null;
            while (running) {
                try {
                    key = watch.poll();
                    if (key != null) {
                        for (WatchEvent<?> watchEvent : key.pollEvents()) {
                            // Get the type of the event
                            Kind<?> kind = watchEvent.kind();
                            if (ENTRY_CREATE == kind) {
                                INSTANCE.artifactCreated(toPath(watchEvent), null);
                            } else if (ENTRY_MODIFY == kind) {
                                INSTANCE.artifactModified(toPath(watchEvent));
                                watchEvent.context();
                            } else if (ENTRY_DELETE == kind) {
                                INSTANCE.artifactDeleted(toPath(watchEvent));
                            }
                        }
                    } 
                    TimeUtils.sleep(200);
                } catch (ClosedWatchServiceException e) {
                    running = false;
                }
            }
        }
        
        /**
         * Turns a watch event into a file system path.
         * 
         * @param event the event
         * @return the path
         */
        private Path toPath(WatchEvent<?> event) {
            Path result = null;
            Object context = event.context();
            if (null != context) {
                result = Paths.get(path.toString(), context.toString());
            }
            return result;
        }
        
        /**
         * Stops the runnable and the watch.
         */
        private void stop() {
            if (running) {
                running = false;
                try {
                    watch.close();
                } catch (IOException e) {
                    LoggerFactory.getLogger(ArtifactsManager.class)
                        .error("While stop watching for artifacts: {}", e.getMessage());
                }
            }
        }
        
    }

    /**
     * Populates the manager by scanning {@code path} for artifacts.
     * 
     * @param path the path to scan
     */
    public void scan(Path path) {
        scan(path.toFile());
    }

    /**
     * Populates the manager by scanning {@code path} for artifacts.
     * 
     * @param path the path to scan
     */
    public void scan(File path) {
        File[] files = path.listFiles();
        if (null != files) {
            for (File f: files) {
                if (f.isDirectory()) {
                    scan(f);
                } else {
                    artifactCreated(f.toPath(), null);
                }
            }
        }
    }
    
    /**
     * Starts watching for new artifacts.
     */
    static void startWatching() {
        PlatformSetup setup = PlatformSetup.getInstance();
        File artifactsFolder = setup.getArtifactsFolder();
        if (artifactsFolder.exists()) {
            Path path = PlatformSetup.getInstance().getArtifactsFolder().toPath();
            INSTANCE.scan(path);
            FileSystem fs = path.getFileSystem();
            try {
                WatchService watch = fs.newWatchService();
                path.register(watch, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                runnable = new WatchRunnable(watch, path);
                new Thread(runnable).start();
            } catch (IOException e) {
                LoggerFactory.getLogger(ArtifactsManager.class)
                    .error("While stop watching for artifacts: {}", e.getMessage());
            }
        } else {
            LoggerFactory.getLogger(ArtifactsManager.class)
                .error("Configured artifacts folder {} does not exist. Disabling watching for artifacts", 
                    artifactsFolder.getAbsolutePath());
        }
    }

    /**
     * Stops watching for new artifacts.
     */
    static void stopWatching() {
        if (null != runnable) {
            runnable.stop();
            runnable = null;
        }
    }

    /**
     * Informs the manager that an artifact was created. Called while watching the 
     * {@link PlatformSetup#getArtifactsFolder()}.
     * If the file is a service artifact, calls {@link PlatformAas#notifyArtifactRemoved(Artifact)} 
     * if the artifact was known. Does not add artifacts twice.
     * 
     * @param path the path to the artifact, ignored if <b>null</b>
     * @param accessUri the access URI for the devices, if <b>null</b> taken from path
     * @return the artifact (or <b>null</b> if none was created/added)
     */
    public Artifact artifactCreated(Path path, URI accessUri) {
        Artifact result = null;
        if (null != path && path.toFile().exists()) {
            Path pn = path.normalize();
            if (!artifactPaths.containsKey(pn)) {
                File file = path.toFile();
                String name = file.getName();
                if (null == accessUri) {
                    String prefix = PlatformSetup.getInstance().getArtifactsUriPrefix();
                    if (prefix != null && prefix.length() > 0) {
                        try {
                            accessUri = new URI(prefix + path.toString().replace('\\', '/'));
                        } catch (URISyntaxException e) {
                            LoggerFactory.getLogger(ArtifactsManager.class)
                                .warn("While creating artifact {}: {}", path, e.getMessage());
                        }
                    }
                    if (null == accessUri) {
                        accessUri = file.toURI(); // fallback
                    }
                }
                if (name.endsWith(".zip") || name.endsWith(".jar")) { // could be a service artifact
                    result = createForZip(path, accessUri);
                }
                if (name.endsWith(".yml")) { // could be a container descriptor
                    result = createForYaml(file, accessUri);
                }
            }
            if (null != result) {
                artifacts.put(result.getId(), result);
                artifactPaths.put(pn, result);
                PlatformAas.notifyArtifactCreated(result);
            }
        }
        return result;
    }
    
    /**
     * Tries to create an artifact instance for a ZIP/JAR file.
     * 
     * @param path the path to the file
     * @param accessUri the access URI for the devices
     * @return the artifact, may be <b>null</b> if none can be created
     */
    private Artifact createForZip(Path path, URI accessUri) {
        Artifact result = null;
        try {
            FileInputStream fis = new FileInputStream(path.toFile());
            InputStream is = JarUtils.findFile(fis, "deployment.yml");
            if (null != is) {
                YamlArtifact yml = YamlArtifact.readFromYaml(is); // closes is
                result = new ServiceArtifact(yml, accessUri);
            }
            fis.close();
        } catch (IOException e) {
            LoggerFactory.getLogger(ArtifactsManager.class)
                .error("Cannot read artifact {}: {}", path, e.getMessage());
        }
        return result;
    }

    /**
     * Tries to create an artifact instance for a YAML file.
     * 
     * @param file the file to create the artifact for
     * @param accessUri the access URI for the devices
     * @return the artifact, may be <b>null</b> if none can be created
     */
    private Artifact createForYaml(File file, URI accessUri) {
        Artifact result = null;
        BasicContainerDescriptor desc = BasicContainerDescriptor.readFromYamlFile(file);
        if (null != desc.getImageFile() && desc.getImageFile().length() > 0) {
            File f = new File(file.getParentFile(), desc.getImageFile());
            if (f.exists()) {
                result = new ContainerArtifact(desc, accessUri);
            } else {
                LoggerFactory.getLogger(ArtifactsManager.class).info("Cannot create container descriptor for {}: "
                    + "Container image file {} not found in same directory", file, desc.getImageFile());
            }
        }
        if (null == result) {
            try {
                ServiceDeploymentPlan plan = ServiceDeploymentPlan.readFromYaml(
                    ServiceDeploymentPlan.class, new FileInputStream(file));
                if (plan.getAssignments().size() > 0) {
                    result = new DeploymentPlanArtifact(plan, accessUri);
                }
            } catch (IOException e) {
                // cannot read, may just be a wrong thing
            }
        }
        return result;
    }

    /**
     * Informs the manager that an artifact was modified. Called while watching the 
     * {@link PlatformSetup#getArtifactsFolder()}. 
     * 
     * @param path the path to the artifact, ignored if <b>null</b>
     * @return the artifact (or <b>null</b> if none was modified)
     */
    public Artifact artifactModified(Path path) {
        Artifact result = null;
        if (null != path && path.toFile().exists()) {
            Path pn = path.normalize();
            result = artifactPaths.get(pn);
            if (null != result) {
                PlatformAas.notifyArtifactModified(result);
            }
        }
        return result;
    }

    /**
     * Informs the manager that an artifact was deleted. Called while watching the 
     * {@link PlatformSetup#getArtifactsFolder()}.
     * Calls {@link PlatformAas#notifyArtifactRemoved(Artifact)} if the artifact was known.
     * 
     * @param path the path to the artifact, ignored if <b>null</b>
     * @return the artifact (or <b>null</b> if none was deleted)
     */
    public Artifact artifactDeleted(Path path) {
        Artifact result = null;
        if (null != path) {
            Path pn = path.normalize();
            result = artifactPaths.remove(pn);
            if (null != result) {
                artifacts.remove(result.getId());
                PlatformAas.notifyArtifactDeleted(result);
            }
        }
        return result;
    }
    
    /**
     * Returns the known artifacts.
     * 
     * @return the artifacts (sorted by artifact id)
     */
    public Iterable<Artifact> artifacts() {
        return artifacts.values();
    }
    
    /**
     * Returns an artifact via its ID.
     * 
     * @param id the id
     * @return <b>null</b> if there is no artifact
     */
    public Artifact getArtifact(String id) {
        return artifacts.get(id);
    }

    /**
     * Returns the number of known artifacts.
     * 
     * @return the number of artifacts
     */
    public int getArtifactCount() {
        return artifacts.size();
    }

}
