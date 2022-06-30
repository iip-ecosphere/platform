package test.de.iip_ecosphere.platform.kiServices.functions.images;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import de.iip_ecosphere.platform.kiServices.functions.images.QRCodeScanner;

public class QRCodeServiceTest {
    
    private static String tmpPath;
    
    private static String testFileName = "hm22-qr1975022415332487959b64";
    
    static {
        tmpPath =  System.getProperty("java.io.tmpdir");                  
    }
    /**
     * Method to quickly test functionality.
     * @param args Args.
     */
    public static void main(String[] args) {
        String image = testingImageFromTemp();
        QRCodeScanner service = new QRCodeScanner();
        /**
         * //Does not seem to work, is noted on the class itself, that windows might break it!
         */
        service.pythonFallbackQRDetection(image); 
    }
    
    /**
     * Reads an Textfile from Tmp which contains an image in byte array form. 
     * @return The String containing the bytes of the image.
     */
    private static String testingImageFromTemp() {
        InputStream readImage;
        byte[] imageBytes = new byte[0];
        try {
            readImage = new FileInputStream(tmpPath + "" + testFileName);
            imageBytes = IOUtils.toByteArray(readImage);
            readImage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(imageBytes, StandardCharsets.UTF_8);
        //return Base64.getEncoder().encodeToString(imageBytes);
    }
}
