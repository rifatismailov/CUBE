package com.example.folder;

public class GetFileIcon {
    public static int getIcon(String fileType) {
        int icon;
        switch (fileType) {
            case "FILE":
                icon = R.drawable.ic_file_hex;
                break;
            case "PDF":
                icon = R.drawable.applicationpdf_92726;
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
                icon = R.drawable.textxpo_92780;
                break;
            default:
                icon = R.drawable.applicationxmswinurl_92784;
                break;
        }
        return icon;
    }

}
