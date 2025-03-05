package com.example.folder;

/**
 * 1. **PDF (Portable Document Format)**
 * - Використовується для збереження документів у фіксованому вигляді.
 * - Може містити текст, зображення, гіперпосилання та вбудовані шрифти.
 * - Підтримується більшістю платформ і програм для перегляду документів.
 * <p>
 * 2. **DOC/DOCX (Microsoft Word Document)**
 * - Використовується у Microsoft Word.
 * - Може містити текст, таблиці, зображення, стилі та макроси.
 * - DOC – старий формат до 2007 року.
 * - DOCX – новий, більш стиснутий та ефективний формат.
 * <p>
 * 3. **TXT (Plain Text File)**
 * - Простий текстовий файл без форматування.
 * - Підтримується всіма редакторами, від Notepad до vim.
 * <p>
 * 4. **ODT (OpenDocument Text)**
 * - Формат текстових документів для LibreOffice та OpenOffice.
 * - Аналог Microsoft Word, але у відкритому форматі.
 * <p>
 * 5. **RTF (Rich Text Format)**
 * - Форматований текстовий файл, який підтримує базові стилі.
 * - Використовується для обміну текстами між різними текстовими редакторами.
 * <p>
 * 6. **XLS/XLSX (Microsoft Excel Spreadsheet)**
 * - Використовується для електронних таблиць.
 * - XLS – старий формат (до 2007).
 * - XLSX – сучасний формат, який використовує XML.
 * <p>
 * 7. **ODS (OpenDocument Spreadsheet)**
 * - Табличний формат для LibreOffice та OpenOffice.
 * - Відкритий аналог Excel.
 * <p>
 * 8. **CSV (Comma-Separated Values)**
 * - Текстовий формат, у якому дані розділені комами або іншими роздільниками.
 * - Використовується для експорту даних із таблиць.
 * <p>
 * 9. **PPT/PPTX (Microsoft PowerPoint Presentation)**
 * - Використовується для створення презентацій.
 * - PPT – старий формат, PPTX – новий на основі XML.
 * <p>
 * 10. **ODP (OpenDocument Presentation)**
 * - Презентаційний формат для LibreOffice та OpenOffice.
 * - Аналог Microsoft PowerPoint.
 * <p>
 * 11. **HTML/HTM (HyperText Markup Language)**
 * - Формат веб-сторінок.
 * - Використовується для зберігання розмітки веб-документів.
 * <p>
 * 12. **EPUB (Electronic Publication)**
 * - Популярний формат електронних книг.
 * - Підтримує текст, зображення та CSS.
 * <p>
 * 13. **MOBI (Mobipocket)**
 * - Формат електронних книг, який використовувався в старих пристроях Kindle.
 * - Вважається застарілим, але ще підтримується.
 * <p>
 * 14. **FB2 (FictionBook)**
 * - Формат електронних книг, популярний у країнах СНД.
 * - Використовується в багатьох рідерах.
 * <p>
 * 15. **MD (Markdown)**
 * - Формат розмітки для створення тексту з форматуванням.
 * - Використовується у документації, блогах, README-файлах.
 * <p>
 * 16. **TEX (LaTeX Document)**
 * - Формат для створення наукових та технічних документів.
 * - Використовується у математичних публікаціях, дисертаціях.
 * - Основний формат для роботи з LaTeX.
 */

public class GetFileIcon {
    public static int getIcon(String fileType) {
        int icon;
        switch (fileType) {
            case "FILE":
            case "file":
                icon = R.drawable.applicationxmswinurl_92784;
                break;
            case "DOC":
            case "DOCX":
                icon = R.drawable.xofficedocument_92775;
                break;
            case "XLS":
            case "XLSX":
                icon = R.drawable.xofficespreadsheet_92797;
                break;
            case "TXT":
            case "TEX":
                icon = R.drawable.textxgeneric_92794;
                break;
            case "PDF":
                icon = R.drawable.applicationpdf_92726;
                break;
            case "MD":
                icon = R.drawable.textxmarkdown_92778;
                break;
            case "PPT":
            case "PPTX":
                icon = R.drawable.xofficepresentation_92765;
                break;
            case "HTML":
            case "HTM":
                icon = R.drawable.applicationxml_92785;
                break;
            case "ODT":
            case "RTF":
            case "ODS":
            case "CSV":
            case "ODP":
            case "EPUB":
            case "MOBI":
            case "FB2":
                icon = R.drawable.ic_file_hex;
                break;
            case "JPG":
            case "JPEG":
            case "PNG":
            case "WEBP":
            case "BMP":
            case "GIF":
            case "HEIC":
            case "HEIF":
            case "TIFF":
            case "TIF":
                icon = R.drawable.imagexgeneric_92742;
                break;
            case "message":
                icon = R.drawable.message_icon_228461;
                break;
            default:
                icon = R.drawable.ic_file_hex;
                break;
        }
        return icon;
    }

}
