package com.polimi.proj.qdocs.support;


/**
 * @author Lamparelli Andrea
 * @author Chittò Pietro
 *
 * Class that represents a single directory in the user's filesystem
 */
public class MyDirectory extends StorageElement {

    private String directoryName;

    public MyDirectory(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
