package org.example;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class QRCodeGenerator {

    public static void generateQRCodeImage(String jsonData, int width, int height, String filePath) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode(jsonData, BarcodeFormat.QR_CODE, width, height, hintMap);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    public static void main(String[] args) {
        try {
            // Ваш JSON
            String jsonData = "{\"userId\":\"H652882302\",\"name\":\"Rifat\",\"lastName\":\"Ismailov\",\"password\":\"uidgt65TG3e\"}";

            // Генерація QR-коду
            generateQRCodeImage(jsonData, 350, 350, "QRCode.png");
            System.out.println("QR-код згенеровано успішно.");
        } catch (Exception e) {
            System.err.println("Помилка при генерації QR-коду: " + e.getMessage());
        }
    }
}

