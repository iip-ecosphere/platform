package test.de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;

/**
 * Identity output translator.
 * 
 * @author Holger Eichelberger, SSE
 */
public class IdentityOutputTranslator extends AbstractConnectorOutputTypeTranslator<Object, Object> {

    @Override
    public void initializeModelAccess() throws IOException {
    }

    @Override
    public Class<? extends Object> getSourceType() {
        return Object.class;
    }

    @Override
    public Class<? extends Object> getTargetType() {
        return Object.class;
    }

    @Override
    public Object to(Object source) throws IOException {
        return source;
    }
    
}