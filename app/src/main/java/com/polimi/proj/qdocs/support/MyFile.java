package com.polimi.proj.qdocs.support;

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

    private String filename;
    private String format;
    private Long size;

    public MyFile() {}

    public MyFile(String filename, String format, Long size) {
        this.filename = filename;
        this.format = format;
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public String getFormat() {
        return format;
    }

    public Long getSize() {
        return size;
    }
}
