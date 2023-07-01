package com.example.cube.socket;

import java.io.*;

public class FileRWByte {
    public byte[] fileByte(String url) {
        File file = new File(url);
        byte[] bytes = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bytes;
    }


    public void writeFile(String fileName, byte[] bytes) {
        File file = new File(fileName);
        try (FileOutputStream os = new FileOutputStream(file)) {
            os.write(bytes);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
