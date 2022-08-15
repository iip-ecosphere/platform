package test.de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.IOException;

import de.iip_ecosphere.platform.kiServices.functions.images.ImageEncodingDecoding;
import de.iip_ecosphere.platform.kiServices.functions.images.QRCodeScanner;
import test.de.iip_ecosphere.platform.kiServices.functions.ImageTests;

public class QRCodeServiceTest {
    
    
    public static final String TEST_FILE_PATH = ImageTests.TEST_FILE_FOLDER + "/testImageAsBytesb64";
    
    public static final String TEST_FILE_OUT_PATH = ImageTests.TEST_FILE_FOLDER + "/testImageOut2.jpg";
    
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
        test = "";
        try {
            test = testJavaQRCodeDetection(ImageEncodingDecoding.base64StringToBufferdImage(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(test);
    }
    /**
     * Test if the python fallback solution is running.
     * @param base64Image the image in base64 version.
     * @return the resutlt of the python qr scann.
     */
    public static String testPythonFallback(String base64Image) {
        String output = "";
        output = QRCodeScanner.pythonFallbackQRDetection(base64Image);
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