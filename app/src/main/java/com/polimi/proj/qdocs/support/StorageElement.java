package com.polimi.proj.qdocs.support;

import java.util.List;

/**
 * Interface that represents either a file or a directory,
 * used in order to display the hierarchy of the user's files
 */
public abstract class StorageElement {

    /**
     * Retrieve a MyFile object matching the filename passed as paramater
     * @param filename name of the file to retrieve
     * @return the MyFile instance if exists, null otherwise
     */
    public static MyFile retrieveFileByName(String filename, List<StorageElement> storageElements) {
        for(StorageElement el : storageElements) {
            if (el instanceof MyFile) {
                MyFile f = (MyFile) el;
                if (f.getFilename().equals(filename)) return f;
            }
        }
        return null;
    }

    /**
     * Retrieve a MyFile object matching the key passed as paramater
     * @param key key of the file to retrieve
     * @return the MyFile instance if exists, null otherwise
     */
    public static MyFile retrieveFileByKey(String key, List<StorageElement> storageElements) {
        for(StorageElement el : storageElements) {
            if (el instanceof MyFile) {
                MyFile f = (MyFile) el;
                if (f.getKey() != null && f.getKey().equals(key)) return f;
            }
        }
        return null;
    }

    /**
     * Retrieve a Directory object from the filesList attribute
     * @param name name to check, is unique for folders
     * @return the Directory obj
     */
    public static Directory retrieveDirectoryByName(String name, List<StorageElement> storageElements) {
        for(StorageElement el : storageElements) {
            if (el instanceof Directory) {
                Directory d = (Directory) el;
                if (d.getDirectoryName() != null && d.getDirectoryName().equals(name)) return d;
            }
        }
        return null;
    }
}
