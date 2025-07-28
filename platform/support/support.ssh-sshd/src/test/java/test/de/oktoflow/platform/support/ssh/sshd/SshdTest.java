package test.de.oktoflow.platform.support.ssh.sshd;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.rest.Rest;
import de.iip_ecosphere.platform.support.ssh.Ssh;
import de.iip_ecosphere.platform.support.ssh.Ssh.SshServer;
import de.oktoflow.platform.support.ssh.sshd.SshdSsh;

/**
 * Tests {@link Rest}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SshdTest {

    /**
     * Tests basic REST functions.
     * 
     * @throws IOException shall not occur
     */
    @Test
    public void testRest() throws IOException {
        Ssh ssh = Ssh.getInstance();
        Assert.assertTrue(ssh instanceof SshdSsh);

        SshServer server = ssh.createServer(new ServerAddress(Schema.SSH));
        server.setAuthenticator((u, p) -> true);
        server.setHostKey(new File("file.ser"));
        server.setShellInit(null); // ignore

        server.start(); // not supported on Windows, leave it for now
        server.isStarted();
        TimeUtils.sleep(500);
        server.stop(true);
        server.isStarted();
    }

}
