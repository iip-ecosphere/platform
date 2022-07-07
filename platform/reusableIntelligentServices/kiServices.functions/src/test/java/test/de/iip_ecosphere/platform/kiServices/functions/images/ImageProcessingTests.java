package test.de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.iip_ecosphere.platform.kiServices.functions.images.ImageProcessing;
/**
 * Class to test the method of image processing.
 * @author Weber
 *
 */
public class ImageProcessingTests {
    
    private static final String TEST_FILE_FOLDER = "src/test/resources";
    
    private static final String TEST_FILE_PATH = TEST_FILE_FOLDER + "/testImage.jpg";
    
    private static final String TEST_FILE_OUT_PATH = TEST_FILE_FOLDER + "/testImageOut1.jpg";
    /**
     * Method to test the imageprocessing tasks in isolation.
     * @param args Args.
     */
    public static void main(String[] args) {
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
}
