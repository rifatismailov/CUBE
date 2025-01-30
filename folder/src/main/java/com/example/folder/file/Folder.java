package com.example.folder.file;

public interface Folder {
    /**
     * Метод addFile використовується коли ви вибрали файл для відправки на сервер
     */
    void addFile(String messageId, String url, String encFile, String has);

    /**
     * Метод updateItem використовується коли ви завантажили файл і він оновлює інформацію про файл
     */
    void updateItem(int position, String positionId, String url, String has);
}
