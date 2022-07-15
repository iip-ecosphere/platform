package test.de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import de.iip_ecosphere.platform.kiServices.functions.images.ImageEncodingDecoding;
import de.iip_ecosphere.platform.kiServices.functions.images.ImageProcessing;
/**
 * Class to test the method of image processing.
 * @author Weber
 *
 */
public class ImageProcessingTests {
    
    public static final String TEST_FILE_FOLDER = "src/test/resources";
    
    public static final String TEST_FILE_PATH = TEST_FILE_FOLDER + "/testImage.jpg";
    
    public static final String TEST_FILE_OUT_PATH = TEST_FILE_FOLDER + "/testImageOut1.jpg";
    /**
     * Method to test the imageprocessing tasks in isolation.
     * @param args Args.
     */
    public static void main(String[] args) {
        
        try {
            createB64ImageStringFromImage(TEST_FILE_FOLDER + "/testImageQR1.jpg");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(TEST_FILE_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedImage result = ImageProcessing.thresholdImage(image, 75);
        try {
            ImageIO.write(result, "jpg", new File(TEST_FILE_OUT_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Utility method to create new test data from images, will be saved to the testimage folder.
     * @param path Path to the image to use.
     * @throws IOException If the process fails to find, load or write to a file.
     */
    public static void createB64ImageStringFromImage(String path) throws IOException {
        String base64EncodedImage = ImageEncodingDecoding.readImageAsBase64String(path);
        FileOutputStream file = new FileOutputStream(new File(QRCodeServiceTest.TEST_FILE_PATH));
        file.write(base64EncodedImage.getBytes());
    }
}
