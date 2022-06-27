package de.iip_ecosphere.platform.kiServices.functions;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import de.iip_ecosphere.platform.services.environment.ProcessSupport;
import de.iip_ecosphere.platform.services.environment.ProcessSupport.ScriptOwner;

/**
 * Offers the functionality of reading in qr codes.
 * 
 * @author Weber
 *
 */
public class QRCodeService {
    // will probably not work on windows!
    private ScriptOwner qrScriptOwner = new ScriptOwner("hm22-qr", "src/main/python/qrScan", 
         "python-qr.zip", "/tmp/qr.res");
    
    /**
     * Takes a base64 encoded byte-array of an image in form of a String and turns it into a BufferedImage for further processing.
     * @param imageString The image to be converted.
     * @return The image extracted from the String.
     * @throws IOException If there is an error while converting the byte-array string to an image.
     */
    public static BufferedImage base64StringToBufferdImage(String imageString) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(imageString);
        BufferedImage imagetest = null;
        imagetest = ImageIO.read(new ByteArrayInputStream(bytes));
        
        return imagetest;
    }

    /**
     * Detects a QR code on bufferdImages.
     * Source: https://simplesolution.dev/java-read-qr-code-from-image-file-base64-zxing/
     * @param bufferedImage a BufferdImage to read a QR code from.
     * @return The content of the QR code as a String. If nothing could be detected the String will be empty.
     */
    public String readQRCode(BufferedImage bufferedImage)  {
        String encodedContent = null;
        try {
            BufferedImageLuminanceSource bufferedImageLuminanceSource = new BufferedImageLuminanceSource(bufferedImage);
            HybridBinarizer hybridBinarizer = new HybridBinarizer(bufferedImageLuminanceSource);
            BinaryBitmap binaryBitmap = new BinaryBitmap(hybridBinarizer);
            MultiFormatReader multiFormatReader = new MultiFormatReader();
            Result result = multiFormatReader.decode(binaryBitmap);
            encodedContent = result.getText();
        } catch (NotFoundException e) {
            System.out.println("NO QR CODE FOUND!!");
            encodedContent = "";
        }
        return encodedContent;
    }
    /**
     * Gray scales a BufferedImage to potentially improve QR scan quality.
     * @param bufferedImage The image to be converted.
     * @return The original image as gray scale.
     */
    public BufferedImage grayScaleImage (BufferedImage bufferedImage) {
        
        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(bufferedImage, bufferedImage);
        
        return bufferedImage;
    }
    /**
     * Call for python fallback script. Shall retry the detection!
     * @param b64Image The image.
     * @return The contents of the qr code as a String.
     */
    public String pythonFallbackQRDetection (String b64Image) {
    // Fallback to python, write to filer
        AtomicReference<String> resultRef = new AtomicReference<>("");
        try {
            System.out.println("TRYING QR");
            File f = File.createTempFile("hm22-qr", "b64");
            FileUtils.writeStringToFile(f, b64Image, StandardCharsets.UTF_8);
            ProcessSupport.callPython(qrScriptOwner, "qr_scanner.py", qr -> {
                System.out.println("PYTHON QR " + qr);
                if (qr.isEmpty()) {
                    resultRef.set(qr);
                    System.out.println(">>> Python override QR scan: " + resultRef.get());
                }
            }, "-i", f.getAbsolutePath());
            System.out.println(">>> Python process worked!");
            f.delete();
        } catch (IOException e) {
            System.out.println(">>> Python fallback error : " + e.getMessage());
        }
        return resultRef.get();
    }
}
