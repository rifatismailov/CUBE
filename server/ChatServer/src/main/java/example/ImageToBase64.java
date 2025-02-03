package example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class ImageToBase64 {
    public static void main(String[] args) throws IOException {
        String imagePath = "path_to_image.jpg"; // Шлях до вашого зображення
        int newWidth = 300; // Ширина для стиснення
        int newHeight = 300; // Висота для стиснення

        // Завантаження зображення
        BufferedImage originalImage = ImageIO.read(new File(imagePath));

        // Зміна розміру
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        // Перетворення у Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", outputStream);
        String base64Image = Base64.getEncoder().encodeToString(outputStream.toByteArray());

        // Вивід Base64
        System.out.println("Base64 зображення: " + base64Image);

        // Надсилання на сервер (приклад: через HTTP)
        // sendToServer(base64Image);
    }

    // Метод для надсилання Base64 на сервер
    public static void sendToServer(String base64Image) {
        // Тут реалізуйте передачу Base64-рядка на сервер
        System.out.println("Передача на сервер: " + base64Image);
    }
}
