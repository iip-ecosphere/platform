package test.de.oktoflow.platform.support.rest.spark;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.rest.Rest;
import de.iip_ecosphere.platform.support.rest.Rest.RestServer;
import de.iip_ecosphere.platform.support.rest.Rest.Route;
import de.iip_ecosphere.platform.support.rest.RestTarget;
import de.oktoflow.platform.support.rest.spark.SparkRest;

/**
 * Tests {@link Rest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class RestTest {

    /**
     * Tests basic REST functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testRest() throws IOException {
        Rest rest = Rest.getInstance();
        Assert.assertTrue(rest instanceof SparkRest);
        
        final String json = "{\"received\":true}";
        Route route = (req, res) -> { 
            res.setBody(json);
            res.setStatus(200);
            return res.getBody();
        };

        final String path = "/test/route";
        int port = NetUtils.getEphemeralPort();
        RestServer server = Rest.getInstance().createServer(port);
        server.definePost(path, route);
        server.defineGet(path, route);
        server.definePut(path, route);
        server.defineDelete(path, route);
        server.start();
        TimeUtils.sleep(500);
        
        RestTarget t = rest.createTarget("http://localhost:" + port + "/");
        String result = t.createRequest()
            .addPath("test")
            .addPath("route")
            .addQueryParam("param", "a", "b")
            .requestJson()
            .getAsString();
        Assert.assertEquals(json, result);
        t.createRequest()
            .addPath("test")
            .addPath("route")
            .requestJson()
            .put("{\"request\":\"abc\"");
        t.createRequest()
            .addPath("test")
            .addPath("route")
            .requestJson()
            .delete();
        
        server.stop(true);
    }

}
