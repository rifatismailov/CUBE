package com.example.folder;

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
            case "ZIP":
                icon = R.drawable.ic_archive;
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
