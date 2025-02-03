package org.example;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private static final Logger LOGGER = Logger.getLogger(FileController.class.getName());

    private final String UPLOAD_DIR = "storage"; // Директорія для зберігання завантажених файлів

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            LOGGER.info("Uploading " + file.getOriginalFilename());
            Path path = Paths.get(UPLOAD_DIR, file.getOriginalFilename());
            Files.createDirectories(path.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;
                try (OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        totalBytesRead += bytesRead;
                        LOGGER.info("Progress: " + (totalBytesRead * 100 / file.getSize()) + "%");
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                LOGGER.info("File uploaded successfully: " + file.getOriginalFilename());
                return ResponseEntity.ok("File uploaded successfully: " + file.getOriginalFilename());
            }
        } catch (Exception e) {
            LOGGER.severe("Error uploading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/download/{filename}")
    public void downloadFile(@PathVariable String filename, HttpServletResponse response) {
        Path path = Paths.get(UPLOAD_DIR, filename);
        if (!Files.exists(path)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            long fileSize = Files.size(path);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setHeader("Content-Length", String.valueOf(fileSize));

            byte[] buffer = new byte[2048];
            int bytesRead;
            OutputStream outputStream = response.getOutputStream();
            long totalBytesRead = 0;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                totalBytesRead += bytesRead;
                int progress = (int) ((totalBytesRead * 100) / fileSize);
                response.setHeader("X-Progress", String.valueOf(progress));
                LOGGER.info("Progress: " + String.valueOf(progress) + "%");
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LOGGER.severe("Error downloading file: " + e.getMessage());
        }
    }
}
