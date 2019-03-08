package com.polimi.proj.qdocs.support;


/**
 * @author Lamparelli Andrea
 * @author Chittò Pietro
 *
 * Class that represents a single directory in the user's filesystem
 */
public class Directory extends StorageElement {

    private String directoryName;

    public Directory(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
