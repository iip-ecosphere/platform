package test.de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.iip_ecosphere.platform.kiServices.functions.images.ImageEncodingDecoding;
import test.de.iip_ecosphere.platform.kiServices.functions.AppTest;
/**
 * A class dedicated to testing the methods of ImageEndiginDecoding.
 * @author Weber
 *
 */
public class ImageEncodingDecodingTests {
    
    public static final String TEST_FILE_PATH = AppTest.TEST_FILE_FOLDER + "/testImage.jpg";
    
    public static final String TEST_FILE_OUT_PATH = 
            AppTest.TEST_FILE_FOLDER + "/testImageOutEndcoding1.jpg";
    
    /**
     * Will first try to read the image as base64, if no exception happens, will try to turn the string back into image.
     * If both things work will try to write the image to confirm it actually is an image!
     * (to resources//testImageOutEndcoding1.jpg )
     * @param imagePath the Path to the testImage.
     */
    public static void testImageToBase64String(String imagePath) {
        String imageString = "";
        BufferedImage image = null;
        try {
            //unsure about the assertion to make
            imageString  = ImageEncodingDecoding.readImageAsBase64String(imagePath);
          //As validation that it worked, as it would break otherwise
            image = ImageEncodingDecoding.base64StringToBufferdImage(imageString); 
            ImageIO.write(image, ".jpg", new File(TEST_FILE_OUT_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * A method to test the conversion of a base64 encoded byte string back into an bufferd image.
     * @param base64Image the base64 encoded String of the image
     * @return image the buffered image derived from the input string.
     */
    public static BufferedImage testBase64StringToBufferedImage(String base64Image) {
        BufferedImage image = null;
        try {
            image = ImageEncodingDecoding.base64StringToBufferdImage(base64Image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}