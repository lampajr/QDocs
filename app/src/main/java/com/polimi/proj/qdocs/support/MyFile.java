package com.polimi.proj.qdocs.support;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * @author Andrea Lamparelli
 * @author Pietro Chittò
 *
 * Stores all the information about a single file
 * loaded from the Firebase Realtime Database
 *
 * structure of the object that describes the file:
 * var fileObj = {
 *      filename : filename,
 *      key : code,
 * 		contentType : object.contentType,
 * 		size : object.size,
 * 		time : object.timeCreated
 * 	};
 */

@IgnoreExtraProperties
public class MyFile {

    // file's attribute stored in the database
    private String key;
    private String filename;
    private String contentType;
    private String size;
    private String time;

    public MyFile() {}

    public MyFile(String filename, String contentType, String key, String size, String time) {
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.time = time;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public String getSize() {
        return size;
    }

    public String getTime() {
        return time;
    }
}
