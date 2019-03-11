package com.polimi.proj.qdocs.support;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public static final String KEY = "key";
    public static final String FILENAME = "filename";
    public static final String CONTENT_TYPE = "contentType";
    public static final String SIZE = "size";
    public static final String TIME = "time";
    public static final String LAST_ACCESS = "lastAccess";
    public static final String OFFLINE = "offline";


    // file's attribute stored in the database
    private String key;
    private String filename;
    private String contentType;
    private String size;
    private String time;
    //private long lastAccess;
    private boolean offline;
    private StorageReference reference = null;

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
    public StorageReference getReference() {
        return reference;
    }

    public void setReference(StorageReference reference) {
        this.reference = reference;
    }

}
