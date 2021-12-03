package test.de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;

/**
 * A simple connector descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MyTypedDataConnectorDescriptor extends MyTypedDataDescriptor implements TypedDataConnectorDescriptor {

    private String id;
    private String service;
    
    /**
     * Creates a data descriptor.
     * 
     * @param id the id
     * @param name the name
     * @param description the description
     * @param type the type
     * @param service the id of the connected/target service
     */
    public MyTypedDataConnectorDescriptor(String id, String name, String description, Class<?> type, String service) {
        super(name, description, type);
        this.id = id;
        this.service = service;
    }

    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String toString() {
        return "Conn " + id + " " + getName() + " -> " + service;
    }

    @Override
    public String getService() {
        return service;
    }

}