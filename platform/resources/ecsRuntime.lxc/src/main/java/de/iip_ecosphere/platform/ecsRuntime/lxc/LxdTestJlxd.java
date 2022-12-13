package de.iip_ecosphere.platform.ecsRuntime.lxc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.util.TypeKey;

import au.com.jcloud.lxd.App;
import au.com.jcloud.lxd.LxdConstants;
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

public class LxdTestJlxd {
	
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
		String remoteHostAndPort = "localhost";
		
		LxdServerCredential credential = new LxdServerCredential(remoteHostAndPort,
																System.getProperty("snap_cert"),
																System.getProperty("snap_key"));
		service.setLxdServerCredential(credential);	

		// Change from hard-coded to dynamic path
		String tarPath = System.getProperty("user.dir") + "/src/test/resources/lxd.tar.xz";
		String rootfsPath = System.getProperty("user.dir") + "/src/test/resources/rootfs.squashfs";

		                            /**                Working Section                   **/
		
		// show some server information
		
//		System.out.println(service.getLxdServerCredential());
//		System.out.println(service.loadServerInfo());
		

		// list all containers and the corresponding data, do some container operations
//		Collection<Container> containers = service.loadContainerMap().values();
//		LOG.info("containers=" + containers.size());
//		LOG.info("containers=" + containers);
//		for (Container container : containers) {
//			if(container.getStatus().equalsIgnoreCase("RUNNING")) {
//				LOG.info("container=" + container.getName());
//			}
//		}
		
		// works fine but need to find alternative to Thread.sleep() that is more dynamic
		//Find a way to use local images as well, from repo or from filepath 
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
		
		//Works fine, container has to be stopped
//		service.renameContainer("ubuntuRename", "con1");

		//Works unless there is no image with the same fingerprint already
//		linuxCliService.executeLinuxCmd("lxc image import " + tarPath + " " + rootfsPath + " --alias test");
		
//		System.out.println(linuxCliService.executeLinuxCmdWithResultLines("lxc remote list"));
		
//		System.out.println(service.loadImage("99d3c3ac16bf").getArchitecture());
		
		//Tested for shutdown and works but not for install tested below
//		String[] sArray = {"sudo apt-get install openssh-client"};
//		service.execOnContainer("u1", sArray, "", null);
		
		//Doesnt work with service.function() but with executeLinuxCmd()
//		System.out.println(linuxCliService.executeLinuxCmdWithResultLines("lxc snapshot con1 snap01"));
//		service.deleteSnapshot("u1", "snap01");
		
//		System.out.println(linuxCliService.executeLinuxCmd("lxc launch local:99d3c3ac16bf"));	
		
//		linuxCliService.executeLinuxCmd("lxc launch piRemote:amdImage");

		
                                   /**                Non-Working Section                   **/


		//Empty atm.
		
		
									/**                Try-Out Section                   **/
		
//		ProcessBuilder processBuilder = new ProcessBuilder("/bin/sh", "-c", "lxc init ubuntuSSH");
//		
//		Process process = processBuilder.start();
//		process.getOutputStream().close();
//		
//		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//		StringBuilder builder = new StringBuilder();
//		String line = null;
//		while ( (line = reader.readLine()) != null) {
//			builder.append(line);
//			builder.append(System.getProperty("line.separator"));
//		}
//		String result = builder.toString();
//		
//		System.out.println(result);

//		System.out.println(service.loadContainer("c1").getArchitecture());
		//service.createContainer("testRemote", "piRemote:amdImage");
//		System.out.println(linuxCliService.executeLinuxCmdWithResultLines("lxc launch piRemote:amdImage test --debug"));

//		linuxCliService.executeLinuxCmd("lxc image copy piRemote:amdImage local: --alias test");
		service.createContainer("testLaptop", "lxd-laptop:a5b7d073427e");
		service.loadContainerState("test").getStatus();
		
	}
}