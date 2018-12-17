package com.polimi.proj.qdocs.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.polimi.proj.qdocs.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScannerActivity extends AppCompatActivity {

    private static final String TAG = "SCANNER";
    private static final int REQUEST_CAMERA_PERMISSION = 2;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private Button takePictureButton;
    private TextureView textureView;

    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureRequestBuilder;

    private Size imageDimension;
    private ImageReader imageReader;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    // coordinates of the most recent touch on the screen
    private double previousX=0.0, previousY=0.0;

    // listener on the Texture View
    private final TextureView.SurfaceTextureListener textureListener =
            new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            // called whenever the surface of the textureView is available
            // open the camera
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // to implement
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //to implement
        }
    };

    // callback on the state of the Camera Device
    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // the camera has been successfully opened
            Log.d(TAG, "Camera " + camera.getId() + " opened!");
            cameraDevice = camera;
            // create the camera preview on the textureView
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "Camera " + camera.getId() + " disconnected");
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "the following error occurred " + error);
            cameraDevice = null;
        }
    };

    // state callback on the camera capture session
    private final CameraCaptureSession.StateCallback sessionStateCallback =
            new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            if (cameraDevice == null) {
                return;
            }
            cameraCaptureSession = session;
            Log.d(TAG, "Configuration succeed");
            updatePreview();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Configuration failed!");
        }
    };

    // callback on the capture session
    private final CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            //++++++++TO IMPLEMENT++++++++++//
            // this method is called when the capture of the image goes well
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        textureView = findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        takePictureButton = findViewById(R.id.btn_takepicture);
        setupTakePictureButton();

        setSwipeListener();
    }

    /**
     * set a swipe listener on the texture view such that whenever
     * the user swipe from left to right the screen the login activity
     * will appear
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setSwipeListener() {
        textureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        previousX = event.getX();
                        previousY = event.getY();
                        Log.d(TAG, "pressed " + previousX + " " + previousY);
                        break;
                    case MotionEvent.ACTION_UP:
                        float x, y;
                        x = event.getX();
                        y = event.getY();
                        Log.d(TAG, "SWIPE");
                        if (x > previousX + 80.0 && (y < previousY +50 || y > previousY-50)) {
                            startLoginActivity();
                        }
                        break;
                }
                return true;
            }
        });
    }

    /**
     * start the LoginActivity
     */
    private void startLoginActivity() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    /**
     * setup the takePictureButton adding the onClickListener
     */
    private void setupTakePictureButton() {
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    /**
     * start the background thread and create the handler
     * from the background thread Looper
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CAMERA BACKGROUND");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * stop the background thread
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            Log.e(TAG, "Error occurred stopping the background thread: " + e.getMessage());
        }
    }

    /**
     * open the main camera of the current device
     */
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[0];

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            // check the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        }
        catch (CameraAccessException e) {
            Log.e(TAG, "Error occurred accessing the Camera: "+ e.getMessage());
        }
    }

    /**
     * close the camera device
     */
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    /**
     * create the camera preview on the texture view
     */
    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();

            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());

            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface), sessionStateCallback, null);

        }
        catch (CameraAccessException e) {
            Log.e(TAG, "Error occurred accessing the camera: " + e.getMessage());
        }
    }

    /**
     * update the camera preview on the texture view
     */
    private void updatePreview() {
        if (cameraDevice == null) {
            Log.e(TAG, "Error occurred during update preview: CameraDevice is null");
            return;
        }
        try {
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        }
        catch (CameraAccessException e) {
            Log.e(TAG, "Error occurred accessing the camera: " + e.getMessage());
        }
    }

    /**
     * take a picture of the screen provided by the camera
     */
    private void takePicture() {
        if (cameraDevice == null) {
            Log.e(TAG, "Error occurred during take picture event: CameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            Size jpegSize = map.getOutputSizes(ImageFormat.JPEG)[0];

            int width = 640, height = 480;

            if (jpegSize != null) {
                width = jpegSize.getWidth();
                height = jpegSize.getHeight();
            }

            ImageReader imageReader = ImageReader.newInstance(width, height,
                    ImageFormat.JPEG, 1);

            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(imageReader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(
                    CameraDevice.TEMPLATE_STILL_CAPTURE);

            // orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {

                    try (Image image = reader.acquireLatestImage()) {
                        decodeImage(image);
                    }
                }
            };

            imageReader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureCallback, mBackgroundHandler);
                    }
                    catch (CameraAccessException e) {
                        Log.e(TAG, "Error occurred accessing the camera: " + e.getMessage());
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "Configuration failed during capture session");
                }
            }, mBackgroundHandler);
        }
        catch (CameraAccessException e) {
            Log.e(TAG, "Error occurred accessing the camera: " + e.getMessage());
        }
    }

    /**
     * decode the qr code inside the image
     * @param image subject to decode
     */
    private void decodeImage(Image image) {
        // TODO
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
            else {
                // PERMISSION DENIED
                Log.e(TAG, "Permission to the camera denied!");
                Toast.makeText(this,
                        "Sorry, without the camera permission you cannot use this app",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "On Resume");
        startBackgroundThread();
        if (textureView.isAvailable()) {openCamera();}
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "On Pause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
}
