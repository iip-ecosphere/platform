package de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
}
