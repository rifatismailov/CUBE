package com.example.folder.file;

public interface FileHandler {
    void setProgress(int progress);

    void showDirectory(String analogDir);

    void closeDialog();
    void onFinish();
}
