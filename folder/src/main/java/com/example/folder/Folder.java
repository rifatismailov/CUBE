package com.example.folder;

public interface Folder {
    void addFile(String messageId,String url,String encFile, String has);
    void updateItem(int position, String url, String has);
}
