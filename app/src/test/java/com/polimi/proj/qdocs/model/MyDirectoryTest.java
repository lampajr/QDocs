package com.polimi.proj.qdocs.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyDirectoryTest {

    private MyDirectory dir;
    private String name;

    @Before
    public void setUp() throws Exception {
        dir = new MyDirectory(name);
    }

    @After
    public void tearDown() throws Exception {
        dir = null;
    }

    @Test
    public void getDirectoryName() {
        String dirname = dir.getDirectoryName();
        assertEquals(name, dirname);
    }
}