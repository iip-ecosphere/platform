package de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

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
        image = ImageIO.read(new ByteArrayInputStream(bytes));
        
        return image;
    }

}
