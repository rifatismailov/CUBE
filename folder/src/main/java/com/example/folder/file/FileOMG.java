package com.example.folder.file;

public interface FileOMG {
    /**
     * Метод для відображення прогресу
     *
     * @positionId ID позиція у активності
     * @progress прогрес дії
     * @info інформація яка можливо виникла під час прогресу
     */
    void setProgressShow(String positionId, int progress, String info);

    /**
     * Метод який використовується під час завершення прогресу
     *
     * @positionId ID позиція у активності
     * @info інформація яка можливо виникла під час прогресу
     */
    void endProgress(String positionId, String info);
}
