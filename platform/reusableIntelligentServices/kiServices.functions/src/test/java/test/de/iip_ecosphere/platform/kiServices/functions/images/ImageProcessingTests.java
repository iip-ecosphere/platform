package test.de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import de.iip_ecosphere.platform.kiServices.functions.images.ImageEncodingDecoding;
import de.iip_ecosphere.platform.kiServices.functions.images.ImageProcessing;
import test.de.iip_ecosphere.platform.kiServices.functions.AppTest;
/**
 * Class to test the method of image processing.
 * @author Weber
 *
 */
public class ImageProcessingTests {
    
    public static final String TEST_FILE_PATH = AppTest.TEST_FILE_FOLDER + "/testImage.jpg";
    
    public static final String TEST_FILE_OUT_PATH = AppTest.TEST_FILE_FOLDER + "/testImageOut1.jpg";
    /**
     * Method to test the imageprocessing tasks in isolation.
     * @param args Args.
     */
    public static void main(String[] args) {
        
        try {
            createB64ImageStringFromImage(AppTest.TEST_FILE_FOLDER + "/testImageQR1.jpg");
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
    
    /**
     * Testing the grayscaling of images, not much possible automated, just to make sure its not broken.
     * @param image the image to be grayscaled.
     * @return the inputimage in grayscale.
     */
    public static BufferedImage testGrayscaling(BufferedImage image) {
        ImageProcessing.grayScaleImage(image);
        return image;
    }
    /**
     * Testing turning an image into black and white, no real assertion possible thus testing if it breaks.
     * @param image the image to be turned into black and white.
     * @param threshold the color value will be used to separate into black and white.
     * @return the Image in black and white.
     */
    public static BufferedImage testThresholdingImage(BufferedImage image, int threshold) {
        image = ImageProcessing.thresholdImage(image, threshold);
        return image;
    }
    /**
     * Tests the rescaling function of the imageprocessing class. Only testing for exception as the result cannot be 
     * evalutated automaticaly.
     * @param image the image to be resacled.
     * @param height the target height of the new image.
     * @param width the target width of the new image.
     * @return the image with its new dimensions.
     */
    public static BufferedImage testRescalingOfImage(BufferedImage image, int height, int width) {
        image = ImageProcessing.rescaleImage(image, height, width);
        return image;
    }
}
