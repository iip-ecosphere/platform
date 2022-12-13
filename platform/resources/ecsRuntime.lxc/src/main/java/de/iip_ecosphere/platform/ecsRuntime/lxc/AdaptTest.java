package de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import au.com.jcloud.lxd.enums.RemoteServer;
import au.com.jcloud.lxd.service.ILxdApiService;
import de.iip_ecosphere.platform.ecsRuntime.lxc.LxcContainerManager.FactoryDescriptor;
import de.iip_ecosphere.platform.support.net.UriResolver;
import io.micrometer.core.ipc.http.ReactorNettySender;

public class AdaptTest {
	public static void main(String[] args) {
		
		System.setProperty(ILxdApiService.CURL_URL_BASE_LOCAL, "curl -s --unix-socket /var/snap/lxd/common/lxd/unix.socket a");
		System.setProperty("snap_cert", "/home/linuxluca/snap/lxd/common/config/client.crt");
		System.setProperty("snap_key", "/home/linuxluca/snap/lxd/common/config/client.key");
		
		Lxc lxc = new Lxc();
		
	
		LxcContainerManager test= new LxcContainerManager();
		LxcContainerDescriptor desc = new LxcContainerDescriptor();
		String workingDir = System.getProperty("user.dir");
		String homeDir = workingDir + "/src/test/resources/";
		String imageLocationStr = "file://" + workingDir + "/src/test/resources/";
        URI location;
//		try {
//			location = new URI(imageLocationStr);
//			test.addContainer(location);
//			Thread.sleep(15000);
//			test.startContainer("test-container");
//			Thread.sleep(10000);
//			test.freezeContainer("test-container");
//			Thread.sleep(1000);
//			test.unfreezeContainer("test-container");
//			Thread.sleep(1000);
//			test.stopContainer("test-container");
//			Thread.sleep(10000);
//			test.deleteContainer("test-container");
//		} catch (URISyntaxException | ExecutionException | InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		
//		System.out.println(lxc.getLxcHost() + ":" + lxc.getLxcPort());
//		System.out.println(lxc.getLxcImageYamlFilename());
//		System.out.println(lxc.getDownloadDirectory());
//        
//        try {
//			location = new URI(imageLocationStr);
//			System.out.println(test.addContainerFromTemplate(location));
//			Thread.sleep(10000);
//			test.deleteContainer("test-container");
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
        
        try {
        	location = new URI(imageLocationStr);
			test.addContainer(location);
		} catch (ExecutionException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
