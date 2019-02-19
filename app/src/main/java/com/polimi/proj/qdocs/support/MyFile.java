package com.polimi.proj.qdocs.support;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * @author Andrea Lamparelli
 * @author Pietro Chittò
 *
 * Stores all the information about a single file
 * loaded from the Firebase Realtime Database
 */

@IgnoreExtraProperties
public class MyFile {

    private String key;
    private String filename;
    private String contentType;

    public MyFile() {}

    public MyFile(String filename, String contentType) {
        this.filename = filename;
        this.contentType = contentType;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Exclude
    public String getKey() {return  key;}

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }
}
