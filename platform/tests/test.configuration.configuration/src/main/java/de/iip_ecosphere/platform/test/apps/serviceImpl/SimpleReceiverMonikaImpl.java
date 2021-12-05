/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.test.apps.serviceImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

import de.iip_ecosphere.platform.services.environment.DefaultServiceImpl;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.Rec13;
import iip.interfaces.SimpleDataReceiver3Service;

/**
 * A simple receiver implementation just printing out the received data.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleReceiverMonikaImpl extends DefaultServiceImpl implements SimpleDataReceiver3Service {

    /**
     * Fallback constructor.
     */
    public SimpleReceiverMonikaImpl() {
        super(ServiceKind.SINK_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimpleReceiverMonikaImpl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void receiveRec13(Rec13 data) {
        System.out.println("TestApp03 RECEIVED " + data.getStringField() + " " + data.getIntField());
            	
    	// Creating new file
        String fileName = System.getProperty("user.home") + "/testapp03_logs.txt";
    	File file = new File(fileName);
    	
    	if (Files.notExists(Paths.get(fileName))) {
        	try {
    			file.createNewFile();
    			FileOutputStream oFile = new FileOutputStream(file, false); 
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		} 
    	}
    	// Appending data and miliseconds to the file. 
    	long timestamp = System.currentTimeMillis();
    	String date = LocalDate.now().toString();    	
    	String contentToAppend = "\nReceivered data on: " + date + ", " + String.valueOf(timestamp);
    	
	    try {
			Files.write(
			  Paths.get(fileName), 
			  contentToAppend.getBytes(), 
			  StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }    
}
