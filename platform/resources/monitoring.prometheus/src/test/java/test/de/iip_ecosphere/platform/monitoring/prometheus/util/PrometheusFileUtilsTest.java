package test.de.iip_ecosphere.platform.monitoring.prometheus.util;

import org.junit.Test;

import de.iip_ecosphere.platform.monitoring.prometheus.util.PrometheusFileUtils;

public class PrometheusFileUtilsTest {
    /** Start the Test.
     * 
     */
    @Test
    public void start() {
        String configpath = "src/main/resources/prometheus.yml";
        PrometheusFileUtils utils = new PrometheusFileUtils(configpath);
        utils.displayLines();
    }
}
