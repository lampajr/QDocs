package com.polimi.proj.qdocs.support;

public class Directory implements StorageElement {

    private String folderName;

    public Directory(String folderName) {
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }
}
