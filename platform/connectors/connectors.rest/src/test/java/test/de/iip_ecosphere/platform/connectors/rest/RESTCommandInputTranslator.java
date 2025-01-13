package test.de.iip_ecosphere.platform.connectors.rest;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

public class RESTCommandInputTranslator<O> extends AbstractConnectorInputTypeTranslator<RESTCommand, O> {

    private Class<? extends O> sourceType;

    /**
     * Creates a new machine command input translator.
     * 
     * @param sourceType the source type
     */
    public RESTCommandInputTranslator(Class<? extends O> sourceType) {
        this.sourceType = sourceType;
    }

    
    @Override
    public Class<? extends O> getSourceType() {
        return sourceType;
    }

    @Override
    public Class<? extends RESTCommand> getTargetType() {
        return RESTCommand.class;
    }

    @Override
    public O from(RESTCommand data) throws IOException {
       // AbstractModelAccess access = (AbstractModelAccess) getModelAccess();  
        return null;
    }

}
