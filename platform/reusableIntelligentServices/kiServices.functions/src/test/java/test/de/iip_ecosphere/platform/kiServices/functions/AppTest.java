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

package test.de.iip_ecosphere.platform.kiServices.functions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.kiServices.functions.images.ImageEncodingDecoding;
import test.de.iip_ecosphere.platform.kiServices.functions.images.ImageEncodingDecodingTests;
import test.de.iip_ecosphere.platform.kiServices.functions.images.ImageProcessingTests;
import test.de.iip_ecosphere.platform.kiServices.functions.images.QRCodeServiceTest;

/**
 * Template test.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AppTest {
    
    public static final String TEST_FILE_FOLDER = "src/test/resources";
    
    /**
     * Template test.
     */
    @Test
    public void testEncodingDecoding() {
        BufferedImage image = ImageEncodingDecodingTests.testReadBase64StringAsBufferedImage();
        Assert.assertTrue(image != null);
        String imageString = ImageEncodingDecodingTests.testReadImageToBase64String();
        Assert.assertFalse(imageString.isEmpty());
        imageString = ImageEncodingDecodingTests.testReadingBase64AsString();
        Assert.assertFalse(imageString.isEmpty());
        image = ImageEncodingDecodingTests.testReadingInImageAsBufferedImage();
        Assert.assertTrue(image != null);
    }
    
    /**
     * Testing QR Code functionality.
     */
    @Test
    public void testQRFunctionality() {
        String base64Iamge = null;
        BufferedImage image = null;
        try {
            //the image for the qr test is in base64 format thus the need to load it like this
            base64Iamge = ImageEncodingDecoding.readBase64ImageFromBase64File(QRCodeServiceTest.TEST_FILE_PATH);
            image = ImageEncodingDecoding.base64StringToBufferdImage(base64Iamge);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String qr = QRCodeServiceTest.testJavaQRCodeDetection(image);
        Assert.assertTrue(qr.equals("https://aas.uni-h.de/0016"));
        qr = "";
        //qr = QRCodeServiceTest.testPythonFallback(base64Iamge);
        //Assert.assertTrue(qr.equals("https://aas.uni-h.de/0016"));
    }
    
    /**
     * Testing image processing.
     */
    @Test
    public void testImageProcesssing() {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(ImageProcessingTests.TEST_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage gray = ImageProcessingTests.testGrayscaling(image);
        Assert.assertTrue(gray != null);
        BufferedImage rescale = ImageProcessingTests.testRescalingOfImage(image, 500, 500);
        Assert.assertTrue(rescale != null);
        BufferedImage blackWhite = ImageProcessingTests.testThresholdingImage(image, 120);
        Assert.assertTrue(blackWhite != null);
    }
}
