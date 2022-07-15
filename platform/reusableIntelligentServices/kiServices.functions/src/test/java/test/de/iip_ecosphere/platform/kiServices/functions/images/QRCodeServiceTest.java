package test.de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.IOException;

import de.iip_ecosphere.platform.kiServices.functions.images.ImageEncodingDecoding;
import de.iip_ecosphere.platform.kiServices.functions.images.QRCodeScanner;
import test.de.iip_ecosphere.platform.kiServices.functions.AppTest;

public class QRCodeServiceTest {
    
    
    public static final String TEST_FILE_PATH = AppTest.TEST_FILE_FOLDER + "/testImageAsBytesb64";
    
    public static final String TEST_FILE_OUT_PATH = AppTest.TEST_FILE_FOLDER + "/testImageOut2.jpg";
    
//    static {
//        tmpPath =  System.getProperty("java.io.tmpdir");                  
//    }
    /**
     * Method to quickly test functionality.
     * @param args Args.
     */
    public static void main(String[] args) {
        String image = "";
        try {
            image = ImageEncodingDecoding.readBase64ImageFromBase64File(TEST_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * //Does not seem to work, is noted on the class itself, that windows might break it!
         */
        String test = QRCodeScanner.pythonFallbackQRDetection(image); 
        System.out.print(test);
    }
    /**
     * Test if the python fallback solution is running.
     * @param path the path to the file to read in.
     * @return the resutlt of the python qr scann.
     */
    public static String testPythonFallback(String path) {
        String output = "";
        String imageAsBase64 = "";
        try {
            imageAsBase64 = ImageEncodingDecoding.readBase64ImageFromBase64File(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        output = QRCodeScanner.pythonFallbackQRDetection(imageAsBase64);
        return output;
    }
    /**
     * Testing the java version of the QR code detection.
     * @param image the image to test the detection with.
     * @return the string that was detected.
     */
    public static String testJavaQRCodeDetection(BufferedImage image) {
        String qr = "";
        qr = QRCodeScanner.readQRCode(image);
        return qr;
    }
}