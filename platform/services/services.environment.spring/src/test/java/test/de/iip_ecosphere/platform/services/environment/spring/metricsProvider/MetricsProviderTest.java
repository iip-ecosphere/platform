package test.de.iip_ecosphere.platform.services.environment.spring.metricsProvider;

import org.junit.Ignore;
import org.junit.Test;

import de.iip_ecosphere.platform.services.environment.spring.metricsProvider.MetricsProvider;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Tests {@link MetricsProvider}.
 * 
 * @author Miguel Gomez
 */
public class MetricsProviderTest extends 
    test.de.iip_ecosphere.platform.services.environment.metricsProvider.MetricsProviderTest {

    @Override
    protected MetricsProvider createProvider(MeterRegistry registry) {
        return new MetricsProvider(registry);
    }
    
    /**
     * Tests {@link MetricsProvider} Gauge CRUD operations.
     */
    @Ignore("NPE, monitoring cleanup required")
    @Test
    public void testGaugeCrudOperations() {
        super.testGaugeCrudOperations();
    }

}
