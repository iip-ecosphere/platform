package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;

/**
 * Identity input translator.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IdentityInputTranslator extends AbstractConnectorInputTypeTranslator<Object, Object> {

    @Override
    public Class<? extends Object> getSourceType() {
        return Object.class;
    }

    @Override
    public Class<? extends Object> getTargetType() {
        return Object.class;
    }

    @Override
    public Object from(Object data) throws IOException {
        return data;
    }
    
}