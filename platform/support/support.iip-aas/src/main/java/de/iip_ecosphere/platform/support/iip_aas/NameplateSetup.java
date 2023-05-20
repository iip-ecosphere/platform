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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.CollectionUtils;
import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Registry;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.FurtherInformation.FurtherInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.GeneralInformation.GeneralInformationBuilder;
import de.iip_ecosphere.platform.support.aas.types.technicaldata.TechnicalDataSubmodel.TechnicalDataSubmodelBuilder;
import de.iip_ecosphere.platform.support.iip_aas.ApplicationSetup.Address;
import de.iip_ecosphere.platform.support.iip_aas.config.AbstractSetup;

/**
 * Describes static information about a device in the style of an ZVEI Digital Nameplate for industrial equipment V1.0.
 * This class is intentionally neither a base nor a derived class of {@link ApplicationSetup} as this class shall
 * (somewhen) follow the spec, {@link ApplicationSetup} shall then follow the software nameplate.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NameplateSetup {

    public static final String SUBMODEL_SERVICES = "services";
    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_PORT = "port";
    public static final String PROPERTY_HOST = "host";
    public static final String PROPERTY_NETMASK = "netmask";
    public static final String PROPERTY_VERSION = "version";
    
    private String manufacturerName;
    private String manufacturerProductDesignation;
    // TODO complete me
    private String productImage = "";
    private String manufacturerLogo = "";
    private Address address = new Address(); // not official
    private List<Service> services = new ArrayList<>();
    
    /**
     * For snakeyaml.
     */
    public NameplateSetup() {
    }

    /**
     * Copy constructor.
     * 
     * @param setup the instance to copy from
     */
    public NameplateSetup(NameplateSetup setup) {
        this.address = new Address(setup.address);
        this.productImage = setup.productImage;
        this.manufacturerLogo = setup.manufacturerLogo;
        this.manufacturerName = setup.manufacturerName;
        this.manufacturerProductDesignation = setup.manufacturerProductDesignation;
    }
    
    /**
     * Represents a device-provided service.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class Service {
        
        private String key;
        private int port;
        private String host;
        private String netmask;
        private Version version;
        
        /**
         * Returns the unique logical key within this device to access this service.
         * 
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * Changes the unique logical key within this device to access this service. [snakeyaml]
         * 
         * @param key the key
         */
        public void setKey(String key) {
            this.key = key;
        }
        
        /**
         * Return the port to address this service.
         * 
         * @return the port the port
         */
        public int getPort() {
            return port;
        }
        
        /**
         * Changes the port to address this service.
         * 
         * @param port the port
         */
        public void setPort(int port) {
            this.port = port;
        }

        /**
         * Returns the IP address/name of the hosting device. If none was specified, 
         * {@link NetUtils#getOwnIP(String)} is called with {@link #getNetmask()}.
         * 
         * @return the IP address/name
         */
        public String getHost() {
            if (null == host) {
                host = NetUtils.getOwnIP(NetUtils.getNetMask(netmask, host));
            }
            return host;
        }

        /**
         * Defines the IP address/name of the hosting device. [snakeyaml]
         * 
         * @param host the IP address/name to set
         */
        public void setHost(String host) {
            this.host = host;
        }

        /**
         * Returns the optional netmask to enable a safe detection in {@link #getHost()} if
         * no host was given.
         * 
         * @return the netmask (may be <b>null</b>)
         */
        public String getNetmask() {
            return netmask;
        }

        /**
         * Defines the optional netmask.
         * 
         * @param netmask the netmask to set
         */
        public void setNetmask(String netmask) {
            this.netmask = netmask;
        }

        /**
         * Returns the optional service/protocol version offered.
         * 
         * @return the version (may be <b>null</b>)
         */
        public Version getVersion() {
            return version;
        }

        /**
         * Defines the optional service/protocol version offered.
         * 
         * @param version the version to set
         */
        public void setVersion(String version) {
            if (null == version) {
                this.version = null;
            } else {
                this.version = new Version(version);
            }
        }

        /**
         * Defines the optional service/protocol version offered.
         * 
         * @param version the version to set
         */
        public void setVersion(Version version) {
            this.version = version;
        }

    }

    /**
     * Returns the manufacturer name.
     * 
     * @return the manufacturer name
     */
    public String getManufacturerName() {
        return manufacturerName;
    }

    /**
     * Changes the manufacturer name.
     * 
     * @param manufacturerName the manufacturer name
     */
    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }
    
    /**
     * Returns the manufacturer product designation.
     * 
     * @return the designation
     */
    public String getManufacturerProductDesignation() {
        return manufacturerProductDesignation;
    }

    /**
     * Returns the (first specified) service with the given {@code key}.
     * 
     * @param key the key
     * @return the service, may be <b>null</b> for none
     */
    public Service getService(String key) {
        Service svc = null;
        for (Service s : getServices()) {
            if (s.getKey().equals(key)) {
                svc = s;
                break;
            }
        }
        return svc;
    }
    
    /**
     * Returns the services.
     * 
     * @return the services
     */
    public List<Service> getServices() {
        return services;
    }

    /**
     * Returns the given services as map.
     * 
     * @param services the services (may be <b>null</b>)
     * @return the services as map, indexed by their keys
     */
    public static Map<String, Service> getServicesAsMap(List<Service> services) {
        Map<String, Service> result = new HashMap<>();
        if (null != services) {
            for (Service s : services) {
                result.put(s.getKey(), s);
            }
        }
        return result;
    }

    /**
     * Changes the services. [snakeyaml]
     * 
     * @param services the services
     */
    public void setServices(List<Service> services) {
        this.services = services;
    }

    /**
     * Changes the manufacturer product designation.
     * 
     * @param manufacturerProductDesignation the designation
     */
    public void setManufacturerProductDesignation(String manufacturerProductDesignation) {
        this.manufacturerProductDesignation = manufacturerProductDesignation;
    } 

    /**
     * Returns the address.
     * 
     * @return the address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Changes the address.
     * 
     * @param address the address
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Returns the optional product image.
     * 
     * @return the image (local resolvable name or URI to image)
     */
    public String getProductImage() {
        return productImage;
    }

    /**
     * Changes the optional product image.
     * 
     * @param productImage the image (local resolvable name or URI to image)
     */
    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    /**
     * Returns the optional manufacturer logo.
     * 
     * @return the logo (local resolvable name or URI to image)
     */
    public String getManufacturerLogo() {
        return manufacturerLogo;
    }

    /**
     * Defines the optional manufacturer logo.
     * 
     * @param manufacturerLogo the logo (local resolvable name or URI to image)
     */
    public void setManufacturerLogo(String manufacturerLogo) {
        this.manufacturerLogo = manufacturerLogo;
    }
    
    // incomplete

    /**
     * Creates an AAS for this nameplate setup.
     * 
     * @param urn the URN of the AAS to create
     * @param id the id short to create
     * @param further further build steps on the AAS, may be <b>null</b>
     * @return the AAS
     */
    public Aas createAas(String urn, String id, Consumer<AasBuilder> further) {
        AasFactory factory = AasFactory.getInstance();
        Aas aas = null;
        try {
            aas = AasPartRegistry.retrieveAas(urn);
        } catch (IOException e) {
            // not there, ok
            try {
                AasBuilder aasBuilder = factory.createAasBuilder(id, urn);
                TechnicalDataSubmodelBuilder tdBuilder = aasBuilder.createTechnicalDataSubmodelBuilder(null);
                GeneralInformationBuilder giBuilder = tdBuilder.createGeneralInformationBuilder(
                    LangString.create(getManufacturerName()).getDescription(), 
                    LangString.create(getManufacturerProductDesignation()), "", "");
                PlatformAas.createAddress(giBuilder, getAddress()); // inofficial, not in Generic Frame
                AasUtils.resolveImage(getProductImage(), AasUtils.CLASSPATH_RESOURCE_RESOLVER, false, 
                    (n, r, m) -> giBuilder.addProductImageFile(n, r, m));
                AasUtils.resolveImage(getManufacturerLogo(), AasUtils.CLASSPATH_RESOURCE_RESOLVER, true, 
                    (n, r, m) -> giBuilder.setManufacturerLogo(r, m));
                giBuilder.build();
                final GregorianCalendar now = new GregorianCalendar();
                XMLGregorianCalendar cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
                FurtherInformationBuilder fiBuilder = tdBuilder.createFurtherInformationBuilder(cal);
                fiBuilder.build();
                tdBuilder.createTechnicalPropertiesBuilder().build();
                tdBuilder.createProductClassificationsBuilder().build();
                tdBuilder.build();
                SubmodelBuilder sub = aasBuilder.createSubmodelBuilder(SUBMODEL_SERVICES, null);
                if (null != getServices()) {
                    for (Service s: getServices()) {
                        SubmodelElementCollectionBuilder smcb = sub
                            .createSubmodelElementCollectionBuilder(s.getKey(), false, false);
                        smcb.createPropertyBuilder(PROPERTY_KEY)
                            .setValue(Type.STRING, s.getKey())
                            .build();
                        smcb.createPropertyBuilder(PROPERTY_PORT)
                            .setValue(Type.INTEGER, s.getPort())
                            .build();
                        smcb.createPropertyBuilder(PROPERTY_HOST)
                            .setValue(Type.STRING, s.getHost())
                            .build();
                        smcb.createPropertyBuilder(PROPERTY_NETMASK)
                            .setValue(Type.STRING, s.getNetmask())
                            .build();
                        smcb.createPropertyBuilder(PROPERTY_VERSION)
                            .setValue(Type.STRING, null == s.getVersion() ? null : s.getVersion().toString())
                            .build();
                        smcb.build();
                    }
                }
                sub.build();
                if (null != further) {
                    further.accept(aasBuilder);
                }
                aas = aasBuilder.build();
                AasPartRegistry.remoteDeploy(CollectionUtils.addAll(new ArrayList<Aas>(), aas));
            } catch (IOException | DatatypeConfigurationException e1) {
                LoggerFactory.getLogger(getClass()).error("Creating nameplate AAS: {}", e.getMessage());
            }
        }
        return aas;
    }

    /**
     * Preliminary way to find the nameplate YML.
     * 
     * @return the setup representing the nameplate YML
     * @throws IOException if the setup file cannot be read
     */
    public static NameplateSetup obtainNameplateSetup() throws IOException {
        InputStream is = AasUtils.CLASSPATH_RESOURCE_RESOLVER.resolve("nameplate.yml"); // preliminary
        if (null == is) {
            try {
                is = new FileInputStream("src/test/resources/nameplate.yml");
            } catch (IOException e) {
                LoggerFactory.getLogger(NameplateSetup.class).info("Checking AAS for id {}", Id.getDeviceId());
                is = AasUtils.CLASSPATH_RESOURCE_RESOLVER.resolve(Id.getDeviceId().toUpperCase() + ".yml");
            }
        }
        return AbstractSetup.readFromYaml(NameplateSetup.class, is); // closes is
    }
    
    /**
     * Reads the AAS back as nameplate setup. Currently, we read only the services back.
     * 
     * @param aas the AAS to read out (may be <b>null</b>)
     * @return the nameplate setup
     */
    public static NameplateSetup readFromAas(Aas aas)  {
        NameplateSetup result = new NameplateSetup();
        if (null != aas) {
            Submodel sub = aas.getSubmodel(SUBMODEL_SERVICES);
            if (null != sub) {
                result.setServices(readServices(sub));
            }
            // TODO nameplate
        }
        return result;
    }

    /**
     * Reads services from the given {@code sub}model.
     * 
     * @param sub the submodel
     * @return the services
     */
    private static List<Service> readServices(Submodel sub) {
        List<Service> result = new ArrayList<>();
        for (SubmodelElement elt : sub.submodelElements()) {
            if (elt instanceof SubmodelElementCollection) {
                SubmodelElementCollection coll = (SubmodelElementCollection) elt;
                String key = getStringProperty(coll, PROPERTY_KEY);
                Integer port = null;
                Property p = coll.getProperty(PROPERTY_PORT);
                if (null != p) {
                    try {
                        Object val = p.getValue();
                        if (val instanceof Integer) {
                            port = (Integer) val;
                        }
                    } catch (ExecutionException e) {
                        
                    }
                }
                if (null != key && null != port) {
                    Service svc = new Service();
                    svc.setKey(key);
                    svc.setPort(port);
                    svc.setHost(getStringProperty(coll, PROPERTY_HOST));
                    svc.setNetmask(getStringProperty(coll, PROPERTY_NETMASK));
                    svc.setVersion(getStringProperty(coll, PROPERTY_VERSION));
                    result.add(svc);
                }
            }
        }
        return result;
    }

    /**
     * Returns the value of a string property.
     * 
     * @param coll the collection to take the property from
     * @param idShort the short id of the property
     * @return the value or <b>null</b>
     */
    private static String getStringProperty(SubmodelElementCollection coll, String idShort) {
        String result = null;
        Property p = coll.getProperty(idShort);
        if (null != p) {
            try {
                Object val = p.getValue();
                if (val instanceof String) {
                    result = (String) val;
                }
            } catch (ExecutionException e) {
                
            }
        }
        return result;
    }
    
    /**
     * Resolves the nameplate setup from the IIP-Ecosphere platform AAS via the device id.
     * 
     * @return the nameplate setup, may be <b>null</b> if there is none
     * @throws IOException if resolving the AASs fails for some reason
     */
    public static NameplateSetup resolveFromAas() throws IOException {
        NameplateSetup result = null;
        String address = null;
        Aas aas = AasPartRegistry.retrieveIipAas();
        if (null != aas) {
            Submodel resSub = aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES);
            if (null != resSub) {
                SubmodelElementCollection device = resSub.getSubmodelElementCollection(Id.getDeviceIdAas());
                Property prop = device.getProperty(AasPartRegistry.NAME_PROP_DEVICE_AAS);
                if (null != prop) {
                    try {
                        address = (String) prop.getValue();
                    } catch (ExecutionException e) {
                        LoggerFactory.getLogger(NameplateSetup.class).warn(
                            "Cannot read value of AAS my device entry: {}", e.getMessage());
                    }
                }
            }
        }
        if (null != address) {
            aas = resolve(address);
            result = readFromAas(aas);
        }
        return result;
    }

    /**
     * Resolves the service with the given {@code key} from the IIP-Ecosphere platform AAS.
     * 
     * @param key the key of the service to resolve, may be <b>null</b> and leads to <b>null</b>
     * @return the service, may be <b>null</b> for none
     */
    public static Service resolveServiceFromAas(String key) {
        Service result = null;
        if (null != key) {
            try {
                NameplateSetup np = NameplateSetup.resolveFromAas();
                if (null != np) {
                    result = np.getService(key);
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(NameplateSetup.class).warn(
                    "Cannot resolve service {}: {}", key, e.getMessage());
            }
        }
        return result;
    }
    
    /**
     * Resolves an {@code identifier} to the respective AAS.
     * 
     * @param identifier the identifier, URN, URL to resolve
     * @return the resolved AAS or <b>null</b> for none
     * @throws IOException if the resolution failed
     */
    public static Aas resolve(String identifier) throws IOException {
        Registry reg = AasFactory.getInstance().obtainRegistry(
            AasPartRegistry.getSetup().getRegistryEndpoint());
        return reg.retrieveAas(identifier);
    }

}
