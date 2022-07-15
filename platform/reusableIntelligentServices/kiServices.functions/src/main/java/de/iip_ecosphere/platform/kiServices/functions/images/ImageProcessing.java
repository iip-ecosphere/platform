package de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.Image;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;
/**
 * Utility class for the processing of images.
 * @author Weber
 *
 */
public class ImageProcessing {
    /**
     * Gray scales a BufferedImage.
     * @param bufferedImage The image to be converted.
     * @return The original image as gray scale.
     */
    public static BufferedImage grayScaleImage(BufferedImage bufferedImage) {
        
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(bufferedImage, bufferedImage);
        
        return bufferedImage;
    }
    
    /**
     * Converts an image to a binary one based on given threshold. A lower threshold will result
     * in more of the image being completely white.
     * @param image the bufferedimage to convert.
     * @param threshold the threshold in [0,255]
     * @return a new BufferedImage instance of TYPE_BYTE_GRAY with only 0'S and 255's
     */
    public static BufferedImage thresholdImage(BufferedImage image, int threshold) {
        image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        image.getGraphics().drawImage(image, 0, 0, null);
        WritableRaster raster = image.getRaster();
        int[] pixels = new int[image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            raster.getPixels(0, y, image.getWidth(), 1, pixels);
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < threshold) {
                    pixels[i] = 0;
                } else {
                    pixels[i] = 255;
                }
            }
            raster.setPixels(0, y, image.getWidth(), 1, pixels);
        }
        return image;
    }
    /**
     * To shrink the pixelcount in an image without changing its content. 
     * @param image The image to rescale.
     * @param targetHeight The pixel hight it should have.
     * @param targetWidth The pixel width it should have.
     * @return A smaller version of the original image.
     */
    public static BufferedImage rescaleImage(BufferedImage image, int targetHeight, int targetWidth) {
        Image resultingImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }
}
