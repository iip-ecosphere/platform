package test.de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.services.TypedDataDescriptor;

/**
 * A simple data descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyTypedDataDescriptor implements TypedDataDescriptor {

    private String name;
    private String description;
    private Class<?> type;

    /**
     * Creates a data descriptor.
     * 
     * @param name the name
     * @param description the description
     * @param type the type
     */
    MyTypedDataDescriptor(String name, String description, Class<?> type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public String getDescription() {
        return description;
    }
    
}