/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.examples.hm22;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.support.aas.AasUtils;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceLoader;
import de.iip_ecosphere.platform.support.resources.ResourceResolver;
import iip.datatypes.Command;
import iip.datatypes.CommandImpl;
import iip.datatypes.ImageInput;
import iip.datatypes.ImageInputImpl;
import iip.impl.CamImageSourceImpl;
import de.iip_ecosphere.platform.kiServices.functions.images.QRCodeScanner;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Robot, image and QR data source.
 * 
 * @author Alexander Weber, SSE
 */
public class CamSource extends CamImageSourceImpl {

    // file-based fallback for local execution, shall be packaged and resolvable via
    // remove "resources/" as prefix if existing on resource name to relocate into given path
    // prefix is needed when accessing the classpath resource
    protected static final ResourceResolver RESOURCE_DEVICES_RESOLVER 
        = new FolderResourceResolver("./resources/software", "resources/");

    //STATIC ip to call robot images
    private static String robotIpAddress = "192.168.2.21";
    // TODO getParameterCamIP now available after constructor and reconfigure was executed
    // TODO getParameterCamPort() now available after constructor and reconfigure was executed
    
    private static String robotUrl = "http://" + robotIpAddress + ":4242/current.jpg?type=color";
    
    private Timer timer = new Timer();
    
    /**
     * Fallback constructor.
     */
    public CamSource() {
        super(ServiceKind.SOURCE_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public CamSource(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    @Override
    public void attachImageInputIngestor(DataIngestor<ImageInput> ingestor) {
        super.attachImageInputIngestor(ingestor);
        
        // -> mock for testing
        if (hasImageInputIngestor()) {
            if (Boolean.valueOf(System.getProperty("iip.app.hm22.camSource.timer", "false"))) {
                System.out.println("CamSource Testing Time started!<<<<<<<<<<<<<<<<<<<<<<<");
                timer.schedule(new TimerTask() {
                    
                    private int count = 0;
                
                    @Override
                    public void run() {
                        Command rec = new CommandImpl();
                        if (0 == count) {
                            System.out.println(">>> Doing QR Scann");
                            rec.setCommand(Commands.SOURCE_DO_QR_SCAN.toString());
                        } else {
                            System.out.println(">>> Doing Takeing Picture for AI");
                            rec.setCommand(Commands.SOURCE_TAKE_PICTURE.toString());
                        }
                        count++;
                        if (count >= 3) {
                            count = 0;
                        }
                        processCommand(rec);
                    }
                }, 0, 40000);
            }
        }
    }

    @Override
    public void processCommand(Command data) {
        Commands enumCommand = Commands.valueOfSafe(data);
        ImageInput imageData = null;
        String b64Image;

        switch (enumCommand) {
        case SOURCE_DO_QR_SCAN:
            b64Image = requestBase64Image(enumCommand);
            if (null != b64Image) {
                imageData = robotQRScan(b64Image);
            }
            break;
        case SOURCE_TAKE_PICTURE: //takes a picture and passes it on
            b64Image = requestBase64Image(enumCommand);
            if (null != b64Image) {
                imageData = new ImageInputImpl();
                imageData.setImage(b64Image);
                imageData.setQrCodeDetected(false);
            }
            break;
        default:
            break;
        }
        if (imageData != null) {
            System.out.println("CAM SEND PIC: " + imageData);
            ingestImageInput(imageData);
        }
    }

    /**
     * Requests a base64 image, either from the camera or from a default input.
     * 
     * @param command the command that caused this request
     * @return the base64 image, may be <b>null</b> if no image is available
     */
    protected String requestBase64Image(Commands command) {
        System.out.println(">>>>>>>>>>>>> Processing Command for Cam Source " + command);
        String b64Image = "";
        try {
            b64Image = requestRoboImage();
        } catch (IOException e) {
            ImageInput gettingData = new ImageInputImpl();
            LoggerFactory.getLogger(getClass()).error("Cannot obtain image. Camera connected? {}", e.getMessage());
            //b64Image = testingImageFromTemp(); //currently not needed, might be trouble if left in
            AasUtils.resolveImage("resources/PhoenixContact.jpg",
                    ResourceLoader.getAllRegisteredResolver(RESOURCE_DEVICES_RESOLVER), 
                    true, (n, r, m) -> {
                        gettingData.setImage(r);
                    });
            b64Image = gettingData.getImage();
        }
        if (null == b64Image) {
            LoggerFactory.getLogger(getClass()).error("Cannot obtain image. Camera connected?");
        }
        return b64Image;
    }
    
    /**
     * Delegates the scanning of the QR Code and prepares the imageData to be send through the ingestor. 
     * [public for testing]
     * 
     * @param b64Image the image to scann.
     * @return a prepared ImageInputData object, ready to be send on.
     */
    public ImageInput robotQRScan(String b64Image) {
        AtomicBoolean found = new AtomicBoolean(true);
        ImageInput data = new ImageInputImpl();
        AtomicReference<String> resultRef = new AtomicReference<>("");
            
        try {
            resultRef.set(QRCodeScanner.readQRCode(b64Image));
        } catch (IOException e) {
            e.printStackTrace();
        } //if no Code is read -> empty string
        if (resultRef.get().isEmpty()) {
            found.set(false);
        } else {
            System.out.println(">>>The QR scan returned text: " + resultRef.get());
        }
        
        data.setImage(b64Image);

        // Fallback to python, write to file
        if (!found.get()) {
            QRCodeScanner.pythonFallbackQRDetection(b64Image);
        }
        
        /*
         * As the AAS Carries the product id at the end, i can be retrieved by splitting with 
         * "/" and returning the last part 
         */ 
        String[] urlParts = resultRef.get().split("/");
        data.setProductId(urlParts[urlParts.length - 1]);
        data.setQrCodeDetected(found.get());
        return data;
    }
    
    /**
     * Method to access the web-server of the robo cam, to receive the images for AI Task.
     * @return A Base64 encoded string version of the read byteArray of the image.
     * @throws IOException in case the request does not work as intended.
     */
    private String requestRoboImage() throws IOException {
        String base64EncodedImage = "";
        URL url = new URL(robotUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        byte[] array = IOUtils.toByteArray(conn.getInputStream());
        //outArrayStream(array);
        base64EncodedImage = Base64.getEncoder().encodeToString(array);
        return base64EncodedImage;
    }
    
}
