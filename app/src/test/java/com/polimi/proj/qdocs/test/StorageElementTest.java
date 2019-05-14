package com.polimi.proj.qdocs.test;

import com.polimi.proj.qdocs.support.MyDirectory;
import com.polimi.proj.qdocs.support.MyFile;
import com.polimi.proj.qdocs.support.StorageElement;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StorageElementTest {

    private StorageElement file1;
    private StorageElement file2;
    private StorageElement file3;
    private StorageElement directory1;
    private StorageElement directory2;
    private List<StorageElement> elements;

    @Before
    public void setUp() throws Exception {
        file1 = new MyFile("file1", "image/jpeg", "0", "64", "2019-6-3", 1L, false);
        file2 = new MyFile("file2", "application/pdf", "1", "32", "2019-7-3", 2L, false);
        file3 = new MyFile("file3", "audio/mp3", "2", "128", "2019-8-3", 3L, false);
        directory1 = new MyDirectory("directory1");
        directory2 = new MyDirectory("directory2");
        elements = new ArrayList<>();
        elements.add(file1);
        elements.add(file2);
        elements.add(file3);
        elements.add(directory1);
        elements.add(directory2);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void compareTo() {
        int res = file1.compareTo(file2);
        assertEquals(-1, res);

        res = file2.compareTo(file1);
        assertEquals(1, res);

        res = directory1.compareTo(directory2);
        assertEquals(0, res);
    }

    @Test
    public void retrieveFileByName() {
        MyFile res = StorageElement.retrieveFileByName("file4", elements);
        assertNull(res);

        res = StorageElement.retrieveFileByName("directory1", elements);
        assertNull(res);

        res = StorageElement.retrieveFileByName("file1", elements);
        assertEquals(file1, res);
    }

    @Test
    public void retrieveFileByKey() {
        MyFile res = StorageElement.retrieveFileByKey("4", elements);
        assertNull(res);

        res = StorageElement.retrieveFileByKey("-1", elements);
        assertNull(res);

        res = StorageElement.retrieveFileByKey("2", elements);
        assertEquals(file3, res);
    }

    @Test
    public void retrieveDirectoryByName() {
        MyDirectory res = StorageElement.retrieveDirectoryByName("directory1", elements);
        assertEquals(directory1, res);

        res = StorageElement.retrieveDirectoryByName("directory3", elements);
        assertNull(res);
    }
}