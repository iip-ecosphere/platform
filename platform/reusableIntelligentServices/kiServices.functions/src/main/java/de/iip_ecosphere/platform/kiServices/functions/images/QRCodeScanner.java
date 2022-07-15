package de.iip_ecosphere.platform.kiServices.functions.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

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
public class QRCodeScanner {
    //Shall set the result folder appropriate for each operating system.
    static {
        resultScript = de.iip_ecosphere.platform.support.FileUtils.getTempDirectoryPath() + "qr.res";
    }
    
    private static String resultScript;
    /**
     * will probably not work on windows!(Set files location for windows, might be changed)
     * Maybe different way to enable debug information instead of deleting/adding 
     * .withProcessCustomizer(ProcessSupport.INHERIT_IO);. 
     */ 
    private static ScriptOwner qrScriptOwner = new ScriptOwner("hm22-qr", "src/main/python/qrScan", 
            "python-qr.zip", resultScript).withProcessCustomizer(ProcessSupport.INHERIT_IO);
    
    /**
     * Enables the QR code detection for base64 encoded images.
     * @param b64image Image as base 64 encoded string.
     * @return the result of the QR scan.
     * @throws IOException When the conversion from String to image fails.
     */
    public static String readQRCode(String b64image) throws IOException {
        BufferedImage image = null;
        image = ImageEncodingDecoding.base64StringToBufferdImage(b64image);

        String result = readQRCode(image);
        return result;
    }
    
    /**
     * Detects a QR code on bufferdImages.
     * Source: https://simplesolution.dev/java-read-qr-code-from-image-file-base64-zxing/
     * @param bufferedImage a BufferdImage to read a QR code from.
     * @return The content of the QR code as a String. If nothing could be detected the String will be empty.
     */
    public static String readQRCode(BufferedImage bufferedImage)  {
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
     * Call for python fallback script. Shall retry the detection!
     * @param b64Image The image.
     * @return The contents of the QR code as a String.
     */
    public static String pythonFallbackQRDetection(String b64Image) {
    // Fallback to python, write to filer
        System.out.println(qrScriptOwner.getResultFile());
        AtomicReference<String> resultRef = new AtomicReference<>("");
        try {
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
