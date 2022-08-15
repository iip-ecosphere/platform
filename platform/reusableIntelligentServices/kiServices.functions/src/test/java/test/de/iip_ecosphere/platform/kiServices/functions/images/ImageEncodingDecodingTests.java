package test.de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.IOException;

import de.iip_ecosphere.platform.kiServices.functions.images.ImageEncodingDecoding;
import test.de.iip_ecosphere.platform.kiServices.functions.ImageTests;
/**
 * A class dedicated to testing the methods of ImageEndiginDecoding.
 * @author Weber
 *
 */
public class ImageEncodingDecodingTests {
    
    public static final String TEST_FILE_PATH = ImageTests.TEST_FILE_FOLDER + "/testImage.jpg";
    
    public static final String TEST_FILE_OUT_PATH = 
            ImageTests.TEST_FILE_FOLDER + "/testImageOutEndcoding1.jpg";
    
    /**
     * Tests the method to read in an image as a base64 string. Returns the string.
     * @return the converted image.
     */
    public static String testReadImageToBase64String() {
        String imageString = "";
        try {
            //unsure about the assertion to make
            imageString  = ImageEncodingDecoding.readImageAsBase64String(ImageEncodingDecodingTests.TEST_FILE_PATH);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageString;
    }
    /**
     * Tests the method to read in an image as a bufferedImage and returns that.
     * @return The image as BufferedImage or null if it cannot be read.
     */
    public static BufferedImage testReadingInImageAsBufferedImage() {
        BufferedImage image = null;
        
        try {
            image = ImageEncodingDecoding.readBufferedImageFromFile(ImageEncodingDecodingTests.TEST_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return image;
    }
    /**
     * Tests if a base64 String can be read in from a base64 file as used in this project.
     * @return the string from the file. It will be empty if the file cannot be read properly.
     */
    public static String testReadingBase64AsString() {
        String image = "";
        
        try {
            image = ImageEncodingDecoding.readBase64ImageFromBase64File(QRCodeServiceTest.TEST_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return image;
    }
    
    /**
     * Tests if a base64 file can be read and output as a BufferedImage.
     * @return image the buffered image derived from the input string.
     */
    public static BufferedImage testReadBase64StringAsBufferedImage() {
        BufferedImage image = null;
        try {
            image = ImageEncodingDecoding.readBase64FileAsBufferedImage(QRCodeServiceTest.TEST_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    /**
     * Tests the conversion of bufferedImages to base64 Strings. Firstly reads in an imgae, converts
     *  it and tried to revert that.
     * @return The image string.
     */
    public static String testBufferedImageToBase64() {
        BufferedImage image = null;
        String imageString = "";
        
        try {
            image = ImageEncodingDecoding.readBufferedImageFromFile(ImageEncodingDecodingTests.TEST_FILE_PATH);
            imageString = ImageEncodingDecoding.bufferedImageToBase64String(image);
            image = ImageEncodingDecoding.base64StringToBufferdImage(imageString); // to confirm the conversion.
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return imageString;
    }
    
    
    
    /**
     * Testing main method.
     * @param args args.
     */
    public static void main(String[] args) {
        BufferedImage image = testReadBase64StringAsBufferedImage();
        System.out.println(image == null);
        String imageString = testReadImageToBase64String();
        System.out.println(imageString.isEmpty());
        imageString = testReadingBase64AsString();
        System.out.println(imageString.isEmpty());
        image = testReadingInImageAsBufferedImage();
        System.out.println(image == null);
    }
}
