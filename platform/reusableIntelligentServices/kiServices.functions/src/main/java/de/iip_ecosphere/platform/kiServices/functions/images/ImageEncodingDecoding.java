package de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;

public class ImageEncodingDecoding {
    
    /**
     * Takes a base64 encoded byte-array of an image in form of a String and turns it into a BufferedImage for 
     * further processing.
     * @param imageString The image to be converted.
     * @return The image extracted from the String.
     * @throws IOException If there is an error while converting the byte-array string to an image.
     */
    public static BufferedImage base64StringToBufferdImage(String imageString) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(imageString);
        BufferedImage image = null;
        ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
        image = ImageIO.read(byteInput);
        byteInput.close();
        return image;
    }
    
    /**
     * Utility function to turn a buffered image into a base64 encoded byte string for transportation.
     * @param bimage the buffered image to convert
     * @return the image in string form
     */
    public static String bufferedImageToBase64String(BufferedImage bimage) {
        String image = "";
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bimage, "jpg", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] array = baos.toByteArray();
        image = Base64.getEncoder().encodeToString(array);
        return image;
    }
    
    /**
     * Utility to read an BufferedImage from a base64 file.
     * @param inPath the location of the file.
     * @return the BufferedImage.
     * @throws IOException If the file cannot be found or the content does not match.
     */
    public static BufferedImage readBase64FileAsBufferedImage(String inPath) throws IOException {
        BufferedImage image = null;
        String imageString = readBase64ImageFromBase64File(inPath);
        image = base64StringToBufferdImage(imageString);
        
        return image;
    }
    
    /**
     * Utility to read an BufferedImage from an image file. Using ImageIO.
     * @param inPath the location of the File.
     * @return the read Image.
     * @throws IOException If the file cannot be found or cannot be read properly.
     */
    public static BufferedImage readBufferedImageFromFile(String inPath) throws IOException {
        BufferedImage image = null;
        
        image = ImageIO.read(new File(inPath));
        
        return image;
    }
    
    /**
     * Utility method to create new test data from images, will be saved to the testimage folder.
     * @param inPath Path to the image to use.
     * @return String of base64 encoded Image.
     * @throws IOException If the process fails to find, load or write to a file.
     */
    public static String readImageAsBase64String(String inPath) throws IOException {
        String base64EncodedImage = "";
        FileInputStream inputstream = new FileInputStream(new File(inPath));
        byte[] array = IOUtils.toByteArray(inputstream);
        inputstream.close();
        base64EncodedImage = Base64.getEncoder().encodeToString(array);
        return base64EncodedImage;
    }
    /**
     * Reads a base64 encoded String out of a file.
     * @param path the path to the file.
     * @return the base64 encoded String.
     * @throws IOException If the file does not exists or cannot be read.
     */
    public static String readBase64ImageFromBase64File(String path) throws IOException {
        InputStream readImage;
        byte[] imageBytes = new byte[0];
        readImage = new FileInputStream(path);
        imageBytes = IOUtils.toByteArray(readImage);
        readImage.close();
        return new String(imageBytes, StandardCharsets.UTF_8);
    }
}
