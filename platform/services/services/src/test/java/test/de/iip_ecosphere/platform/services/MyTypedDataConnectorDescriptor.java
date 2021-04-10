package test.de.iip_ecosphere.platform.services;

import de.iip_ecosphere.platform.services.TypedDataConnectorDescriptor;

/**
 * A simple connector descriptor.
 * 
 * @author Holger Eichelberger, SSE
 */
class MyTypedDataConnectorDescriptor extends MyTypedDataDescriptor implements TypedDataConnectorDescriptor {

    /**
     * Creates a data descriptor.
     * 
     * @param name the name
     * @param description the description
     * @param type the type
     */
    MyTypedDataConnectorDescriptor(String name, String description, Class<?> type) {
        super(name, description, type);
    }

}