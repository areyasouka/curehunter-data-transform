package com.curehunter.utils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Iterate files in a directory, returning each file only once.
 */
public class FileIterator implements Iterator<File> {

    private File parentDir;
    private Collection<File> seenFiles = new HashSet<File>();

    public FileIterator(File parentDir) {
        this.parentDir = parentDir;
    }

    public boolean hasNext() {
        synchronized (this.seenFiles) {
            return (nextInternal() != null);
        }
    }

    public File next() {
        synchronized (this.seenFiles) {
            File next = nextInternal();
            this.seenFiles.add(next);
            return next;
        }
    }

    private File nextInternal() {
        File childFiles[] = this.parentDir.listFiles();
        if (childFiles != null) {
            Arrays.sort(childFiles);
            for (int n = 0; n < childFiles.length; n++) {
                if (!this.seenFiles.contains(childFiles[n]) && childFiles[n].isFile()) {
                    return childFiles[n];
                }
            }
        }
        return null;
    }

    public void remove() {
    }
}
