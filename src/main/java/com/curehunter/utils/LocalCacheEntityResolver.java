package com.curehunter.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LocalCacheEntityResolver implements EntityResolver {
    private File dtdDirectory;
    
    public LocalCacheEntityResolver(File dtdDirectory) {
        this.dtdDirectory = dtdDirectory;
    }
    
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        if (systemId != null && dtdDirectory != null) {
            int lastSlash = systemId.lastIndexOf('/');
            String dtdFile = lastSlash != -1 ? systemId.substring(lastSlash + 1) : systemId; 
            File candidate = new File(dtdDirectory, dtdFile);
            if (candidate.exists() && candidate.isFile()) {
                InputSource source = new InputSource(new FileReader(candidate));
                source.setSystemId(candidate.getAbsolutePath());
                return source;
            } else {
                System.err.println("Entity not found in directory, forcing remote lookup: " + systemId + " (public: " + publicId + ")");
            }
        }
        return null;
    }
}
