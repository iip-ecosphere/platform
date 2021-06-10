package test.de.iip_ecosphere.platform.support.fakeAas;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;

/**
 * A fake protocol server builder that does nothing (in case that the fake AAS is active in basic component tests).
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeProtocolServerBuilder implements ProtocolServerBuilder {

    @Override
    public Server build() {
        return new Server() {

            @Override
            public Server start() {
                return this;
            }

            @Override
            public void stop(boolean dispose) {
            }
            
        };
    }

    @Override
    public ProtocolServerBuilder defineOperation(String name, Function<Object[], Object> function) {
        return this;
    }

    @Override
    public ProtocolServerBuilder defineProperty(String name, Supplier<Object> get, Consumer<Object> set) {
        return this;
    }

    @Override
    public PayloadCodec createPayloadCodec() {
        return null;
    }

}
