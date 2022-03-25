package test.de.iip_ecopshere.platform.monitoring.prometheus.service;

import java.util.Optional;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.monitoring.prometheus.PrometheusLifecycleDescriptor;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

public class PrometheusLifecycleDescriptorTest {
    /** Start the Test.
     * 
     */
    @Test
    public void start() {
        ServiceLoader<LifecycleDescriptor> loader = ServiceLoader.load(LifecycleDescriptor.class);
        Optional<PrometheusLifecycleDescriptor> pml = ServiceLoaderUtils
            .stream(loader)
            .filter(d-> d instanceof PrometheusLifecycleDescriptor)
            .map(PrometheusLifecycleDescriptor.class::cast)
            .findFirst();
        Assert.assertTrue(pml.isPresent());
        pml.get().startup(new String[] {});
        TimeUtils.sleep(50000);
        pml.get().shutdown();
        pml.get().deleteWorkingFiles();
    }
}
