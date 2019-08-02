package com.polimi.proj.qdocs.support;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({Log.class, Bitmap.class})

public class UtilityTest {

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Log.class);
        PowerMockito.mockStatic(Bitmap.class);
        Bitmap bitmap0 = Mockito.mock(Bitmap.class);
        when(Bitmap.createBitmap(anyInt(), anyInt(), any(Bitmap.Config.class))).thenReturn(bitmap0);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void generateQrCode() throws WriterException {

        String key = "key";
        Bitmap bitmap = Utility.generateQrCode(key);
        assertNotNull(bitmap);
    }

    @Test
    public void generateCode() {
        String code = Utility.generateCode();
        assertNotNull(code);
    }

}