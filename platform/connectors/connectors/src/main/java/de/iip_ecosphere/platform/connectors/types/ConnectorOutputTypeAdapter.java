package de.iip_ecosphere.platform.connectors.types;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.transport.serialization.OutputTypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * Adapts a basic output translator/serializer for reuse.
 * 
 * @param <T> the target type (see {@link OutputTypeTranslator})
 *
 * @author Holger Eichelberger, SSE
 */
public class ConnectorOutputTypeAdapter<T> implements ConnectorOutputTypeTranslator<byte[], T> {

    private Serializer<T> serializer;
    private ModelAccess modelAccess;
    
    /**
     * Creates an instance.
     * 
     * @param serializer the serializer to adapt
     */
    public ConnectorOutputTypeAdapter(Serializer<T> serializer) {
        this.serializer = serializer;
    }

    @Override
    public T to(byte[] source) throws IOException {
        return serializer.from(source);
    }

    @Override
    public ModelAccess getModelAccess() {
        return modelAccess;
    }

    @Override
    public void setModelAccess(ModelAccess modelAccess) {
        this.modelAccess = modelAccess;
    }

    @Override
    public void initializeModelAccess() throws IOException {
    }

    @Override
    public Class<? extends byte[]> getSourceType() {
        return byte[].class;
    }

    @Override
    public Class<? extends T> getTargetType() {
        return serializer.getType();
    };
    
}
