package de.iip_ecosphere.platform.connectors.types;

import de.iip_ecosphere.platform.transport.serialization.TypeTranslator;

/**
 * Uses two {@link TypeTranslator} instances for channeled protocol adaptation.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * 
 * @author Holger Eichelberger, SSE
 */
public class ChannelTranslatingProtocolAdapter<O, I, CO, CI> extends TranslatingProtocolAdapter<O, I, CO, CI> 
    implements ChannelProtocolAdapter<O, I, CO, CI> {

    private String outputChannel;
    private String inputChannel;
    
    /**
     * Creates a translating protocol adapter.
     * 
     * @param outputChannel the name of the input channel. Further semantics is 
     *   implied/restrictions are imposed by the underlying protocol.
     * @param outputTranslator the output translator
     * @param inputChannel the name of the input channel. Further semantics is 
     *   implied/restrictions are imposed by the underlying protocol.
     * @param inputTranslator the input translator
     */
    public ChannelTranslatingProtocolAdapter(String outputChannel, 
        ConnectorOutputTypeTranslator<O, CO> outputTranslator, String inputChannel, 
        ConnectorInputTypeTranslator<CI, I> inputTranslator) {
        super(outputTranslator, inputTranslator);
        this.outputChannel = outputChannel;
        this.inputChannel = inputChannel;
    }

    @Override
    public String getInputChannel() {
        return inputChannel;
    }

    @Override
    public String getOutputChannel() {
        return outputChannel;
    }

}
