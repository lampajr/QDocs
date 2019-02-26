package com.polimi.proj.qdocs.support;

public class Directory implements StorageElement {

    private String directoryName;

    public Directory(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
