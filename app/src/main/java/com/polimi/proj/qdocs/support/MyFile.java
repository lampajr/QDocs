package com.polimi.proj.qdocs.support;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.storage.StorageReference;

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
 *      key : object.metadata[KEY_METADATA],
 * 		contentType : object.contentType,
 * 		size : object.size,
 * 		time : object.timeCreated
 * 	    lastAccess: initialized at object.timeCreated
 * 	};
 */

@IgnoreExtraProperties
public class MyFile extends StorageElement{

    static final String KEY = "key";
    static final String FILENAME = "filename";
    static final String CONTENT_TYPE = "contentType";
    static final String SIZE = "size";
    static final String TIME = "time";
    static final String LAST_ACCESS = "lastAccess";
    static final String OFFLINE = "offline";

    public static final String EMPTY_ELEMENT = "empty_element_59834823";
    public static MyFile emptyElement = new MyFile(EMPTY_ELEMENT, "", "", "", "", 0L, false);


    // file's attribute stored in the database
    private String key;
    private String filename;
    private String contentType;
    private String size;
    private String time;
    //private long lastAccess;
    private boolean offline;
    private StorageReference stReference = null;
    private DatabaseReference dbReference = null;

    public MyFile() {}

    public MyFile(String filename, String contentType, String key,
                  String size, String time, Long lastAccess, boolean offline) {
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
        this.time = time;
        this.key = key;
        this.lastAccess = lastAccess;
        this.offline = offline;
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

    public Long getLastAccess() {
        return lastAccess;
    }

    public boolean isOffline() {
        return offline;
    }

    @Exclude
    public StorageReference getStReference() {
        return stReference;
    }

    @Exclude
    public DatabaseReference getDbReference() {
        return dbReference;
    }

    public void setStReference(StorageReference stReference) {
        this.stReference = stReference;
    }

    public void setDbReference(DatabaseReference dbReference) {
        this.dbReference = dbReference;
    }

    public MyFile duplicate() {
        return new MyFile(filename, contentType, key, size, time, lastAccess, offline);
    }
}
