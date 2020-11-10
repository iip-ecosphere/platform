package de.iip_ecosphere.platform.transport;

import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.impl.DirectMemoryTransferTransportConnector;
import de.iip_ecosphere.platform.transport.connectors.impl.PahoMqttV3TransportConnector;

/**
 * A factory for creating transport connector instances. This factory shall
 * ensure that the entire platform runs with the same connector instances,
 * however, provides at the same time the flexibility to exchange the creation
 * process.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TransportFactory {

    /**
     * Defines an internal factory implementation to create connectors.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface TransportFactoryImplementation {

        /**
         * Creates a connector instance.
         * 
         * @return the created connector instance
         */
        public TransportConnector createConnector();

    }

    private static TransportFactoryImplementation instance = new TransportFactoryImplementation() {

        @Override
        public TransportConnector createConnector() {
            return new PahoMqttV3TransportConnector();
        }
    };

    /**
     * Changes the factory implementation. May be replaced by an injection-based
     * mechanism, so far required for testing.
     * 
     * @param inst the factory implementation instance (ignored if <b>null</b>)
     * @return the factory implementation instance before calling this method
     */
    public static TransportFactoryImplementation setFactoryImplementation(TransportFactoryImplementation inst) {
        TransportFactoryImplementation old = instance;
        if (null != inst) {
            instance = inst;
        }
        return old;
    }

    /**
     * Creates a connector instance.
     * 
     * @return the created connector instance
     */
    public static TransportConnector createConnector() {
        return instance.createConnector();
    }
    
    /**
     * Creates a direct memory transfer connector instance.
     * 
     * @return the direct memory connector instance
     */
    public static TransportConnector createDirectMemoryConnector() {
        return new DirectMemoryTransferTransportConnector();
    }

}
