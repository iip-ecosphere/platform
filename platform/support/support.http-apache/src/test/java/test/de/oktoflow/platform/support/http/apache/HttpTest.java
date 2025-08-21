package test.de.oktoflow.platform.support.http.apache;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.http.Http;
import de.iip_ecosphere.platform.support.http.HttpClient;
import de.iip_ecosphere.platform.support.http.HttpPost;
import de.iip_ecosphere.platform.support.http.HttpResponse;
import de.iip_ecosphere.platform.support.rest.Rest;
import de.iip_ecosphere.platform.support.rest.Rest.RestServer;
import de.iip_ecosphere.platform.support.rest.Rest.Route;
import de.oktoflow.platform.support.http.apache.ApacheHttp;

/**
 * Tests {@link Http}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HttpTest {

    /**
     * Tests basic HTTP functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testRest() throws IOException {
        Http http = Http.getInstance();
        Assert.assertTrue(http instanceof ApacheHttp);
        
        Rest rest = Rest.getInstance();
        ServerAddress addr = new ServerAddress(Schema.HTTP);
        RestServer server = rest.createServer(addr);
        final String path = "/test/route";
        Route route = (req, res) -> { 
            Assert.assertEquals("request", req.getBody());
            res.setBody("received");
            res.setStatus(200);
            return res.getBody();
        };        
        server.definePost(path, route);
        server.start();
        
        HttpPost req = http.createPost(addr.toServerUri() + path)
            .setEntity("request")
            .setHeader("Accept", "application/json");
        HttpClient client = http.createClient();
        HttpResponse res = client.execute(req);
        Assert.assertEquals("received", res.getEntityAsString());
        res.getReasonPhrase();
        res.getStatusCode(); // shall be 200 but who knows
        res.close();
        client.close();
        
        client = http.createPooledClient();
        res = client.execute(req);
        Assert.assertEquals("received", res.getEntityAsString());
        res.getReasonPhrase();
        res.getStatusCode(); // shall be 200 but who knows
        res.close();
        client.close();
    }

}
