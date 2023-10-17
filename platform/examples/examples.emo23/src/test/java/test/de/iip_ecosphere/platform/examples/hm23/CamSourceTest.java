package test.de.iip_ecosphere.platform.examples.hm23;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

import de.iip_ecosphere.platform.examples.hm23.CamSource;

public class CamSourceTest {
    
    private static String tmpPath;
    
    private static String testFileName = "test2.png";
    
    private static String robotIpAddress = "192.168.2.21";
    // TODO getParameterCamIP now available after constructor and reconfigure was executed
    // TODO getParameterCamPort() now available after constructor and reconfigure was executed
    
    private static String robotUrl = "http://" + robotIpAddress + ":4242/current.jpg?type=color";
    
    static {
        tmpPath =  System.getProperty("java.io.tmpdir");                  
    }
    
    // checkstyle: stop exception type check
    
    /**
     * To test the camsource methods without having to run every other piece.
     * @param args Commandlinearguments
     * @throws IOException if connector creation fails
     */
    public static void main(String[] args) throws IOException {
        @SuppressWarnings("unused")
        CamSource camsource = new CamSource();
        String miageString = "";
        System.out.println(tmpPath);
        //InputStream stream = new 
        miageString = testingImageFromTemp();
        System.out.println("PRE WRIING + LENGTH_ " + miageString.length());
        PrintWriter out = new PrintWriter("filename.txt", "UTF-8");
        out.write(miageString);
        System.out.println("POST WRIING");
        
        // emergency tests with full paths :/
        try {
            String pythonPath = "C:\\Work\\Eclipse\\IIP-HM23\\examples.hm22\\src\\test\\java\\test\\de\\iip_ecosphere"
                + "\\platform\\examples\\hm23\\decodeTest.py";
            //String pythonExe = "C:/Users/AppData/Local/Continuum/Anaconda/python.exe";
            ProcessBuilder pb = new ProcessBuilder(
                Arrays.asList("C:/Users/weber/AppData/Local/Programs/Python/Python37/python", pythonPath, "--image", 
                    miageString));
            Process p = pb.start();
            System.out.println("Process STARTED");
            BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            System.out.println("Running Python starts: " + line);
            int exitCode = p.waitFor();
            System.out.println("Exit Code : " + exitCode);
            line = bfr.readLine();
            System.out.println("First Line: " + line);
            while ((line = bfr.readLine()) != null) {
                System.out.println("Python Output: " + line);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        out.close();
        
//        
//        BufferedImage img = null;
//        try {
//            img = ImageIO.read(new File(tmpPath + "test2.png"));
//        } catch (IOException e) {
//        }
//        //640x480
//        BufferedImage dest = img.getSubimage(210, 140, 230, 170); 
//        
//        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
//        op.filter(img, img);
//        
//        try {
//            ImageIO.write(dest, "jpg", new File(tmpPath + "0001ImageOut.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        
        //camsource.requestImage(miageString);
    }
    
    // checkstyle: resume exception type check
    
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
    /**
     * Resizes the image to make it fit for the QR scann, also confirms how to transform to and from 
     * String for the base64 encoded images.
     * @param encodedImage The image to resize as a base64 encoded bytearray in a string.
     * @return The resized result as a bufferd image.
     */
    @SuppressWarnings("unused")
    private static BufferedImage resizeImage(String encodedImage) {
        @SuppressWarnings("unused")
        byte[] bytes = Base64.getDecoder().decode(encodedImage);
        BufferedImage imagetest = base64StringToBufferdImage(encodedImage);
        try {
            ImageIO.write(imagetest, "png", new File(tmpPath + "00001ImageOut.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(imagetest.getHeight() + "   " + imagetest.getWidth());
        try {
            ImageIO.write(imagetest.getSubimage(1000, 1400, 1300, 1000), "png"
                    , new File(tmpPath + "00003ImageOut.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagetest.getSubimage(1000, 1400, 1300, 1000);
    }
    /**
     * Takes a base64 encoded bytearray of an image as a String and turns it into a BufferedImage.
     * @param imageString The image to be converted.
     * @return The conferted image of the String.
     */
    public static BufferedImage base64StringToBufferdImage(String imageString) {
        byte[] bytes = Base64.getDecoder().decode(imageString);
        BufferedImage imagetest = null;
        try {
            imagetest = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imagetest;
    }
    
    /**
     * Method to access the web-server of the robo cam, to receive the images for AI Task.
     * @return A Base64 encoded string version of the read byteArray of the image.
     * @throws IOException in case the request does not work as intended.
     */
    public static String requestRoboImage() throws IOException {
        String base64EncodedImage = "";
        URL url = new URL(robotUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        byte[] array = IOUtils.toByteArray(conn.getInputStream());
        base64EncodedImage = Base64.getEncoder().encodeToString(array);
        return base64EncodedImage;
    }
    

//
//    blackWhite = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
//    Graphics2D g2d = blackWhite.createGraphics();
//    g2d.drawImage(master, 0, 0, this);
//    g2d.dispose();
}
