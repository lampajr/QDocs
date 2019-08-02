package com.polimi.proj.qdocs.model;

import android.util.Log;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyFileTest {

    private static MyFile file;
    private static String filename = "file";
    private static String contentType = "img";
    private static String key = "10";
    private static String size = "100";
    private static String time = "20";
    private static Long lastAcces = 1L;
    private static boolean isOffline = true;

    @BeforeClass
    public static void setUp() throws Exception {

        file = new MyFile(filename, contentType, key, size, time, lastAcces, isOffline);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        file = null;
    }

    @Test
    public void getKey() {
        String fileKey = file.getKey();
        assertEquals(key, fileKey);
    }

    @Test
    public void getFilename() {
        String fileFilename = file.getFilename();
        assertEquals(filename, fileFilename);
    }

    @Test
    public void getContentType() {
        String fileContentType = file.getContentType();
        assertEquals(contentType, fileContentType);
    }

    @Test
    public void getSize() {
        String fileSize = file.getSize();
        assertEquals(size, fileSize);
    }

    @Test
    public void getTime() {
        String fileTime = file.getTime();
        assertEquals(time, fileTime);
    }

    @Test
    public void getLastAccess() {
        Long fileLastAccess = file.getLastAccess();
        assertEquals(lastAcces, fileLastAccess);
    }

    @Test
    public void isOffline() {
        boolean fileIsOffline = file.isOffline();
        assertEquals(isOffline, fileIsOffline);
    }


    @Test
    public void duplicate() {
        MyFile file2 = file.duplicate();
        assertEquals(key, file2.getKey());
        assertEquals(filename, file2.getFilename());
        assertEquals(contentType, file2.getContentType());
        assertEquals(size, file2.getSize());
        assertEquals(time, file2.getTime());
        assertEquals(lastAcces, file2.getLastAccess());
        assertEquals(isOffline, file2.isOffline());
    }

}