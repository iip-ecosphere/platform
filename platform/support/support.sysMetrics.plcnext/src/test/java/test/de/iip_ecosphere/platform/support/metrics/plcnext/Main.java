package test.de.iip_ecosphere.platform.support.metrics.plcnext;

import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.metrics.SystemMetrics;
import de.iip_ecosphere.platform.support.metrics.plcnext.PlcNextSystemMetricsDescriptor;

/**
 * Simple main program for command line testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Main {
    
    /**
     * Simple main program for command line testing.
     * 
     * @param args
     */
    public static void main(String[] args) {
        PlcNextSystemMetricsDescriptor desc = new PlcNextSystemMetricsDescriptor();
        System.out.println("is enabled: " + desc.isEnabled() + " is fallback " + desc.isFallback());
        SystemMetrics metrics = desc.createInstance();
        for (int i = 0; i < 10; i++) {
            System.out.println("cpu cores: " + metrics.getNumCpuCores() + " case temp: "
                + metrics.getCaseTemperature() + " cpu tmp: " + metrics.getCpuTemperature());
            TimeUtils.sleep(1000);
        }
        metrics.close();
    }

}
