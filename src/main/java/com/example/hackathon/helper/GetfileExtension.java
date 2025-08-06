package com.example.hackathon.helper;

import java.io.File;

public class GetfileExtension {
    public String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot != -1 && lastDot < name.length() - 1) 
            ? name.substring(lastDot + 1).toLowerCase() 
            : "";
    }
    
    
}
