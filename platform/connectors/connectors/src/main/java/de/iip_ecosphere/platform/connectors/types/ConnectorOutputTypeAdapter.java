package de.iip_ecosphere.platform.connectors.types;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.model.ModelAccessProvider;
import de.iip_ecosphere.platform.transport.serialization.OutputTypeTranslator;
import de.iip_ecosphere.platform.transport.serialization.Serializer;

/**
 * Adapts a basic output translator/serializer for reuse.
 * 
 * @param <T> the target type (see {@link OutputTypeTranslator})
 * @param <D> the protocol-specific data type for values (see {@link ModelAccessProvider}) 
 *
 * @author Holger Eichelberger, SSE
 */
public class ConnectorOutputTypeAdapter<T, D> implements ConnectorOutputTypeTranslator<byte[], T, D> {

    private Serializer<T> serializer;
    private ModelAccess<D> modelAccess;
    
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
    public ModelAccess<D> getModelAccess() {
        return modelAccess;
    }

    @Override
    public void setModelAccess(ModelAccess<D> modelAccess) {
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
