package de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.util.TypeKey;

import au.com.jcloud.lxd.App;
import au.com.jcloud.lxd.bean.ImageConfig;
import au.com.jcloud.lxd.bean.LxdServerCredential;
import au.com.jcloud.lxd.enums.ContainerStateAction;
import au.com.jcloud.lxd.enums.LxdCall;
import au.com.jcloud.lxd.enums.RemoteServer;
import au.com.jcloud.lxd.model.Container;
import au.com.jcloud.lxd.model.Image;
import au.com.jcloud.lxd.model.response.AbstractResponse;
import au.com.jcloud.lxd.service.ILinuxCliService;
import au.com.jcloud.lxd.service.ILxdApiService;
import au.com.jcloud.lxd.service.ILxdService;
import au.com.jcloud.lxd.service.impl.LinuxCliServiceImpl;
import au.com.jcloud.lxd.service.impl.LxdApiServiceImpl;
import au.com.jcloud.lxd.service.impl.LxdServiceCliImpl;
import au.com.jcloud.lxd.service.impl.LxdServiceImpl;

public class LxdTestJlxdRemote {
	
	private static final Logger LOG = Logger.getLogger(App.class);
	

	public static void main(String[] args) throws IOException, InterruptedException {
		
		System.setProperty(ILxdApiService.CURL_URL_BASE_LOCAL, "curl -s --unix-socket /var/snap/lxd/common/lxd/unix.socket a");
		System.setProperty("snap_cert", "/home/linuxluca/snap/lxd/common/config/client.crt");
		System.setProperty("snap_key", "/home/linuxluca/snap/lxd/common/config/client.key");
		
		
		// instatiate connection to the LXD host
		ILxdService service = new LxdServiceImpl();
		ILxdApiService lxdApiService = new LxdApiServiceImpl();
		ILinuxCliService linuxCliService = new LinuxCliServiceImpl();
		lxdApiService.setLinuxCliService(linuxCliService);
		service.setLxdApiService(lxdApiService);
		String remoteHostAndPort = "192.168.178.112:8443";
		
		LxdServerCredential credential = new LxdServerCredential(remoteHostAndPort,
																System.getProperty("snap_cert"),
																System.getProperty("snap_key"));
		service.setLxdServerCredential(credential);
		
		String tarPath = "/home/linuxluca/git/platform/platform/resources/ecsRuntime.lxc/src/test/resources/ubuntu-jammy-amd64.tar.gz";
		
		Collection<Container> containers = service.loadContainerMap().values();
		LOG.info("containers=" + containers.size());
		for (Container container : containers) {
			LOG.info("container=" + container);
		}
		
		LOG.info(service.loadImageAliasMap());
//		service.createContainer("test101", "images:ubuntu/jammy");
//		Thread.sleep(1000);
//		Container con = service.loadContainer("test101");
//		Thread.sleep(5000);
//		LOG.info(con.getName());
//		LOG.info(service.loadContainerState(con.getName()));
//		service.changeContainerState(con.getName(), ContainerStateAction.START, false, false, "1000");
//		Thread.sleep(10000);
//		LOG.info(service.loadContainerState(con.getName()));
//		service.changeContainerState(con.getName(), ContainerStateAction.FREEZE, false, false, "1000");
//		Thread.sleep(1000);
//		LOG.info(service.loadContainerState(con.getName()));
//		service.changeContainerState(con.getName(), ContainerStateAction.UNFREEZE, false, false, "1000");
//		Thread.sleep(1000);
//		LOG.info(service.loadContainerState(con.getName()));
//		service.changeContainerState(con.getName(), ContainerStateAction.STOP, false, false, "1000");
//		Thread.sleep(5000);
//		LOG.info(service.loadContainerState(con.getName()));
//		
//		service.deleteContainer("test101");
		
		
	}
}