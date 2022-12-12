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

package de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.BasicContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.lxc.LxcContainerDescriptor;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Implements a container descriptor for lxc-based container management.
 * 
 * @author Luca Schulz, SSE
 */
public class LxcContainerDescriptor extends BasicContainerDescriptor {
    
    public static final String PORT_PLACEHOLDER = "${port}";
    public static final String PORT_PLACEHOLDER_1 = "${port_1}";
    private static int instanceCount = 0;

    // internal
    private int instance = instanceCount++;
    private String lxcId;

    // configurable
    private String lxcImageAlias;
    private String lxcZip;
    private String networkMode;
    private ArrayList<String> exposedPorts = new ArrayList<String>();
    private ArrayList<String> env = new ArrayList<String>();
        
    /**
     * Creates a container descriptor instance.
     */
    public LxcContainerDescriptor() {
    }
    
    /**
     * Creates a container descriptor instance.
     * 
     * @param id the container id
     * @param name the (file) name of the container
     * @param version the version of the container
     * @param uri the URI where the descriptor was loaded from
     * @throws IllegalArgumentException if {@code id}, {@code name}, {@code version} or {@code uri} is invalid, e.g., 
     *     <b>null</b> or empty
     */
    protected LxcContainerDescriptor(String id, String name, Version version, URI uri) {
        super(id, name, version, uri);
    }
    
    /**
     * Defines the LXC container's id for LXC its the fingerprint in specific.
     * @param lxcId
     */
    public void setId(String lxcId) {
        this.lxcId = lxcId;
    }
    
    /**
     * Returns the LXC container's id for LXC its the fingerprint in specific.
     * @return LXC id
     */
    public String getId() {
        return this.lxcId;
    }
    /**
     * Defines the name of the LXC image.
     * @param lxcImageAlias
     */
    public void setLxcImageAlias(String lxcImageAlias) {
        this.lxcImageAlias = lxcImageAlias;
    }
    
    /**
     * Returns the name of the LXC image.
     * @return lxcImageAlias
     */
    public String getLxcImageAlias() {
        return this.lxcImageAlias;
    }
    /**
     * Returns the name of the LXC image zip.
     * @return lxcZip
     */
	public String getLxcZip() {
		return lxcZip;
	}
	/**
     * Defines the name of the LXC image zip.
     * @param lxcZip
     */
	public void setLxcZip(String lxcZip) {
		this.lxcZip = lxcZip;
	}
	
    /**
     * Defines the exposed ports.
     * @param exposedPorts the exposed ports
     */
    public void setExposedPorts(ArrayList<String> exposedPorts) {
        if (null != exposedPorts) {
            this.exposedPorts = exposedPorts;
        }
    }
    
    /**
     * Returns the ports exposed by the container.
     * @return the exposed ports
     */
    public ArrayList<String> getExposedPorts() {
        return this.exposedPorts;
    }

    /**
     * Instantiates the exposed by the container.
     * @param port to replace {@link #PORT_PLACEHOLDER}
     * @param port1 to replace {@link #PORT_PLACEHOLDER_1}
     * @return the exposed ports
     */
//    public List<ExposedPort> instantiateExposedPorts(int port, int port1) {
//        ArrayList<ExposedPort> result = new ArrayList<ExposedPort>();
//        String tmpPort = String.valueOf(port);
//        String tmpPort1 = String.valueOf(port1);
//        for (String e: exposedPorts) {
//            String tmp = e.replace(PORT_PLACEHOLDER, tmpPort);
//            tmp = tmp.replace(PORT_PLACEHOLDER_1, tmpPort1);
//            int pos = tmp.indexOf('/');
//            String iPort;
//            String iProtocol;
//            if (pos > 0) {
//                iPort = tmp.substring(0, pos);
//                iProtocol = tmp.substring(pos + 1);
//                if ("DEFAULT".equals(iProtocol)) {
//                    iProtocol = InternetProtocol.DEFAULT.name();
//                }
//            } else {
//                iPort = tmp;
//                iProtocol = InternetProtocol.TCP.name();
//            }
//            try {
//                result.add(new ExposedPort(Integer.parseInt(iPort), InternetProtocol.valueOf(iProtocol)));
//            } catch (IllegalArgumentException ex) {
//            }
//        }
//        return result;
//    }

    /**
     * Defines the environment settings to start the container.
     * @param env the environment settings, may contain {@link #PORT_PLACEHOLDER} to be replaced by the dynamic port 
     *    of the AAS implementation server of the service manager
     */
    public void setEnv(ArrayList<String> env) {
        if (null != env) {
            this.env = env;
        }
    }
    
    /**
     * Returns the network mode.
     * 
     * @return the network mode, may be <b>null</b> for none
     */
    public String getNetworkMode() {
        return networkMode;
    }
    
    /**
     * Returns the plain environment settings to start the container.
     * @return the environment settings, may contain {@link #PORT_PLACEHOLDER}}
     */
    public ArrayList<String> getEnv() {
        return this.env;
    }

    /**
     * Defines the network mode. [snakeyaml]
     * 
     * @param networkMode the network mode, may be <b>null</b> for none
     */
    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }
    
    /**
     * Returns the substituted environment variable settings to start the container.
     * @param port the port to substitute {@link #PORT_PLACEHOLDER} 
     * @param port1 the port to substitute {@link #PORT_PLACEHOLDER_1} 
     * @return the instantiated environment variable settings 
     */
    public List<String> instantiateEnv(int port, int port1) {
        List<String> result = new ArrayList<String>();
        for (String s : env) {
            result.add(s.replace(PORT_PLACEHOLDER, String.valueOf(port))
                .replace(PORT_PLACEHOLDER_1, String.valueOf(port1)));
        }
        return result;
    }

    /**
     * Returns whether a dynamic port for a placeholder is required.
     * 
     * @param placeholder the name of the placeholder
     * @return {@code true} for dynamic port, {@code false} else
     */
    public boolean requiresPort(String placeholder) {
        boolean result = false;
        for (String s : env) {
            if (s.contains(placeholder)) {
                result = true;
                break;
            }
        }
        if (!result) {
            for (String e : exposedPorts) {
                if (e.contains(placeholder)) {
                    result = true;
                    break;
                }
            }
            
        }
        return result;
    }
    
    /**
     * Returns the key for the network manager.
     * 
     * @return the key
     */
    public String getNetKey() {
        return Id.getDeviceId() + "_" + lxcImageAlias + "_" + instance;
    }

    /**
     * Returns the key for the network manager.
     * 
     * @return the key
     */
    public String getNetKey1() {
        return Id.getDeviceId() + "_" + lxcImageAlias + "_1_" + instance;
    }
    
    

    /**
     * Returns a LxcContainerDescriptor with a information from a yaml file.
     * @param file yaml file
     * @return LxcContainerDescriptor (may be <b>null</b>)
     */
    public static LxcContainerDescriptor readFromYamlFile(File file) {
        LxcContainerDescriptor result = null;
        InputStream in;
        try {
            in = new FileInputStream(file);
            result = readFromYaml(in, file.toURI());
        } catch (FileNotFoundException e) {
            LoggerFactory.getLogger(LxcContainerDescriptor.class).error(
                "Reading container descriptor: " + e.getMessage());
        }
        return result;
    }
    
    /**
     * Returns a LxcContainerDescriptor with a information from a yaml file.
     * @param in an inout stream with Yaml contents (may be <b>null</b>)
     * @param uri the URI the descriptor was read from
     * @return LxcContainerDescriptor (may be <b>null</b>)
     */
    public static LxcContainerDescriptor readFromYaml(InputStream in, URI uri) {
        LxcContainerDescriptor result = null;
        if (in != null) {
            try {
                result = AbstractSetup.readFromYaml(LxcContainerDescriptor.class, in);
                result.setUri(uri);
            } catch (IOException e) {
                LoggerFactory.getLogger(LxcContainerDescriptor.class).error(
                    "Reading container descriptor: " + e.getMessage());
            }
        }
        return result;
    }
    
    /**
     * Turns a full container image name with optional registry, repository and version into its repository/name.
     * 
     * @param imgName the image name
     * @return the repository
     */
    public static String getRepository(String imgName) {
        String result = imgName;
        String tag = getTag(imgName);
        if (LxcSetup.isNotEmpty(tag)) {
            int pos = imgName.lastIndexOf(':');
            if (pos > 0) {
                result = imgName.substring(0, pos);
            }
        }
        String reg = getRegistry(result);
        if (LxcSetup.isNotEmpty(reg)) {
            result = result.substring(reg.length() + 1);
        }
        return result;
    }
    
    /**
     * Turns a full container image name with optional registry, repository and version into its repository/name.
     * 
     * @param imgName the image name
     * @return the repository
     */
    public static String getRegistry(String imgName) {
        String result;
        int pos = imgName.indexOf('/');
        if (pos > 0) {
            int lastPos = imgName.lastIndexOf('/');
            if (pos != lastPos) {
                result = imgName.substring(0, pos);
            } else {
                result = "";
            }
        } else {
            result = "";
        }
        return result;
    }

    /**
     * Turns a full container image name with optional registry, repository and version into its (version) tag.
     * 
     * @param imgName the image name
     * @return the tag, may be empty
     */
    public static String getTag(String imgName) {
        String result;
        int pos = imgName.lastIndexOf('/'); // repo/registry before?
        if (pos > 0) {
            imgName = imgName.substring(pos + 1);
        }
        pos = imgName.lastIndexOf(':');
        if (pos > 0) {
            result = imgName.substring(pos + 1);
        } else {
            result = "";
        }
        return result;
    }
    
    /**
     * Tests reading a Docker container file.
     * 
     * @param args arguments, first is taken as file name
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            LxcContainerDescriptor desc = readFromYamlFile(new File(args[0]));
            System.out.println("Descriptor (may be hash): " + desc);
        } else {
            System.out.println("First arg must be file to read");
        }
    }

}