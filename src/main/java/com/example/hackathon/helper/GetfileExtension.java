package com.example.hackathon.helper;

import java.io.File;
public class GetfileExtension {
    public String getFileExtension(File file) {
        if (file == null || !file.isFile()) {
            return "";
        }

        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot != -1 && lastDot < name.length() - 1)
            ? name.substring(lastDot + 1).toLowerCase()
            : "";
    }
}
