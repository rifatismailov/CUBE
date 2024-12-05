package com.example.folder.file;

public interface DoHandler {
    void setProgress(String progress);

    void showDirectory(String analogDir);

    void closeDialog();
    void onFinish();
}
