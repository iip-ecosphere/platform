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
import iip.datatypes.Rec1;
import iip.interfaces.SimpleDataReceiverService;

public class SimpleReceiverMonikaImpl extends DefaultServiceImpl implements SimpleDataReceiverService{
	
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
    public void receiveRec1(Rec1 data) {
        System.out.println("TestApp03 RECEIVED " + data.getStringField() + " " + data.getIntField());
        
    	String fileName = "/testapp03_logs.txt";
    	
    	// Creating new file it there is none.
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
