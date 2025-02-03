package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import javax.imageio.ImageIO;

public class ImageProcessing {
    public static void main(String[] args) {
        try {
            // 1. Завантаження кольорового зображення
            File inputFile = new File("photo_2025-01-07 16.39.16.jpeg");
            BufferedImage colorImage = ImageIO.read(inputFile);

            // 2. Перетворення на чорно-біле
            BufferedImage grayscaleImage = new BufferedImage(
                    colorImage.getWidth(),
                    colorImage.getHeight(),
                    BufferedImage.TYPE_BYTE_GRAY
            );
            Graphics g = grayscaleImage.getGraphics();
            g.drawImage(colorImage, 0, 0, null);
            g.dispose();

            // 3. Зміна розміру зображення
            int newWidth = colorImage.getWidth(); // нова ширина
            int newHeight = colorImage.getHeight(); // нова висота
            Image scaledImage = grayscaleImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(scaledImage, 0, 0, null);
            g2d.dispose();

            // 4. Конвертація у Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Виведення результату
            System.out.println("Base64 зображення:");
            System.out.println(base64Image);

            // Додатково: Збереження результату для перевірки
            File outputFile = new File("output_image.jpg");
            ImageIO.write(resizedImage, "jpg", outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

