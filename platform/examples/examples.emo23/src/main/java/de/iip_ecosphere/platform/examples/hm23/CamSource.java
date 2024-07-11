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

package de.iip_ecosphere.platform.examples.hm23;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.resources.FolderResourceResolver;
import de.iip_ecosphere.platform.support.resources.ResourceResolver;
import iip.datatypes.Command;
import iip.datatypes.CommandImpl;
import iip.datatypes.ImageInput;
import iip.datatypes.ImageInputImpl;
import iip.impl.CamImageSourceImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
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
    // combination for workaround: INCLUDE_IMAGE = false, STORE_LOCAL = true
    protected static final boolean INCLUDE_IMAGE = true; 
    protected static final boolean STORE_LOCAL = true;
    
    private Timer timer = new Timer();
    private String actImageId;
    private String lastRequestedUri = "";
    
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

    /**
     * Composes the image URL.
     * 
     * @param imgId the id of the image to fetch
     * @return the image URL
     */
    private String getImageUrl(String imgId) {
        String ip = getParameterCamIP();
        int port = getParameterCamPort();
        int robotId = getParameterRobotId();
        String portPart = "";
        if (port > 0) {
            portPart = ":" + port;
        }
        return "http://" + ip + portPart + "/IMG_" + robotId + "/test" + imgId + ".png";
    }

    @Override
    public void attachImageInputIngestor(DataIngestor<ImageInput> ingestor) {
        super.attachImageInputIngestor(ingestor);
        
        // -> mock for testing
        if (hasImageInputIngestor()) {
            if (Boolean.valueOf(System.getProperty("iip.app.hm23.camSource.timer", "false"))) {
                System.out.println("CamSource Testing Time started!<<<<<<<<<<<<<<<<<<<<<<<");
                timer.schedule(new TimerTask() {
                    
                    private int count = 0;
                    
                    /**
                     * Turns the image counter into a side name.
                     * 
                     * @return the side name
                     */
                    private String getSide() {
                        switch (count) {
                        case 0:
                            return "left";
                        case 1:
                            return "top";
                        case 2:
                            return "right";
                        default:
                            return "?";
                        }
                    }
                
                    @Override
                    public void run() {
                        Command rec = new CommandImpl();
                        System.out.println(">>> Taking Picture for AI");
                        rec.setCommand(Commands.SOURCE_TAKE_PICTURE.toString());
                        rec.setStringParam("0;" + getSide());
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
        case SOURCE_TAKE_PICTURE: //takes a picture and passes it on
            String param = data.getStringParam();
            int pos = param.indexOf(";");
            b64Image = requestBase64Image(enumCommand, param.substring(0, pos));
            if (null != b64Image) {
                imageData = new ImageInputImpl();
                imageData.setImage(INCLUDE_IMAGE ? b64Image : "");
                String imgUri = lastRequestedUri;
                if (!imgUri.contains(";")) {
                    imgUri = imgUri + ";" + imgUri;
                }
                imageData.setImageUri(imgUri);
                imageData.setRobotId(getParameterRobotId());
                imageData.setSide(param.substring(pos + 1));
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
     * Modifies the last requested URI.
     * 
     * @param lastRequestedUri the last requested URI
     */
    protected void setLastRequestedUri(String lastRequestedUri) {
        this.lastRequestedUri = lastRequestedUri;
    }

    /**
     * Requests a base64 image, either from the camera or from a default input.
     * 
     * @param command the command that caused this request
     * @param imageId the id denoting the image to obtain
     * @return the base64 image, may be <b>null</b> if no image is available
     */
    protected String requestBase64Image(Commands command, String imageId) {
        if (actImageId == null || !actImageId.equals(imageId)) { // don't send twice
            actImageId = imageId;
            System.out.println(">>>>>>>>>>>>> Processing Command for Cam Source " + command 
                + " " + getImageUrl(imageId));
            String b64Image = "";
            try {
                final int maxTryCount = 4;
                int tryCount = 0;
                while (true) { // terminates at tryCount == maxTryCount
                    try {
                        b64Image = requestImage(imageId);
                        break;
                    } catch (IOException e) {
                        tryCount++;
                        System.out.println(">>>>>>>>>>>>> No image, try " + tryCount + "/" + maxTryCount
                             + " " + e.getMessage());
                        TimeUtils.sleep(300);
                        if (tryCount == maxTryCount) {
                            throw e;
                        }
                    }
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(getClass()).error("Cannot obtain image {} Camera connected? "
                    + "Using fallback. {}", getImageUrl(imageId), e.getMessage());
                /*lastRequestedUri = "http://me.here.de/PhoenixContact.jpg";
                ImageInput gettingData = new ImageInputImpl();
                LoggerFactory.getLogger(getClass()).error("Cannot obtain image {} Camera connected? "
                    + "Using fallback. {}", getImageUrl(imageId), e.getMessage());
                AasUtils.resolveImage("resources/PhoenixContact.jpg",
                        ResourceLoader.getAllRegisteredResolver(RESOURCE_DEVICES_RESOLVER), 
                        true, (n, r, m) -> {
                            gettingData.setImage(r);
                        });
                b64Image = gettingData.getImage();*/
            }
            if (null == b64Image) {
                b64Image = getFallbackImage();
            }
            return b64Image;
        } else {
            return getFallbackImage();
        }
    }
    
    /**
     * Returns a fallback image.
     * 
     * @return the fallback image
     */
    private String getFallbackImage() {
        lastRequestedUri = "http://oktoflow.de/empty.jpg";
        LoggerFactory.getLogger(getClass()).error("Falling back. Camera connected?");
        String result;
        try {
            final int width = 2448;
            final int height = 2048;
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = img.getGraphics();
            g.setColor(Color.WHITE);
            g.drawRect(0, 0, width, height); // -1 does not care
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", bos);
            result = Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) {
            result = null; // shall not happen
        }
        return result;
    }
    
    /**
     * Method to access the web-server of the cam, to receive the images for AI Task.
     * 
     * @param imageId the id of the image to obtain
     * @return A Base64 encoded string version of the read byteArray of the image.
     * @throws IOException in case the request does not work as intended.
     */
    public String requestImage(String imageId) throws IOException {
        String imgUrl = getImageUrl(imageId);
        lastRequestedUri = imgUrl;
        URL url = new URL(imgUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        byte[] array = IOUtils.toByteArray(conn.getInputStream());
        if (STORE_LOCAL) {
            try {
                File localFile = new File(FileUtils.getTempDirectory(), "/imgCache/" + url.getFile());
                localFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(localFile);
                IOUtils.write(array, fos);
                fos.close();
                lastRequestedUri += ";" + localFile.toURI().toString();
            } catch (IOException e) {
                System.out.println("While caching: " + e.getMessage());
            }
        }
        return Base64.getEncoder().encodeToString(array);
    }
    
}
