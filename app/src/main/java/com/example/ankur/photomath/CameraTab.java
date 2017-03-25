package com.example.ankur.photomath;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Created by ankur on 15/2/17.
 */

public class CameraTab extends Fragment {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

    }

    private TextureView mTextureView;
    private ImageButton btnCapture;
    private File mGalleryFolder;
    private ToggleButton flashToggle;
    private Size mPreviewSize;
    private String mCameraId;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private static final int STATE_PICTURE_CAPTURED=2;
    private int mState;
    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setUpCamera(width, height);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            createCameraPreviewSession();
//            Toast.makeText(getActivity(), "Camera Opened", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;

        }
    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW:
//                    Toast.makeText(getActivity(), "STATE_PREVIEW", Toast.LENGTH_SHORT).show();
                    break;
                case STATE_WAIT_LOCK:
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED) {
//                        Toast.makeText(getActivity(), "Focus Locked Successfully", Toast.LENGTH_SHORT).show();
                        mState= STATE_PICTURE_CAPTURED;
//                        Toast.makeText(getActivity(), "STATE_PICTURE_CAPTURED", Toast.LENGTH_SHORT).show();
                        captureStillImage();
                    }
                    break;

            }
        }

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);

            process(result);
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);

            Toast.makeText(getActivity(), "Focus Locked Unsuccessful", Toast.LENGTH_SHORT).show();
        }
    };
    private  String mImageFileLocation;

    private void setUpCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                            Size LargestImageSize = getImageSize();
//                        Size LargestImageSize = Collections.max(
//                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
//                        new Comparator<Size>() {
//                            @Override
//                            public int compare(Size lhs, Size rhs) {
//                                return Long.signum(lhs.getWidth() * lhs.getHeight() -
//                                        rhs.getWidth() * rhs.getHeight());  // return largest image camera support
//                            }
//                        }
//                );
                mImageReader = ImageReader.newInstance(LargestImageSize.getWidth(),
                        LargestImageSize.getHeight(),
                        ImageFormat.JPEG, 1); // last parameter is how much image needs to be captured
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                        mBackgroundHandler);

                mPreviewSize = getPreferredPreviewSize();  // Match Sizes
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getImageSize() {
     return new Size(3264,1836); // approx 6 Megapixels of ratio 16:9
    }

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private static File mImageFile;
    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage()));
                }
            };

    private static class ImageSaver implements Runnable {
        private final Image mImage;

        private ImageSaver(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            if (bytes.length>0) {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(mImageFile);
                    fileOutputStream.write(bytes);
                    Bitmap bmp = Bitmap.createBitmap(BitmapFactory.decodeFile(mImageFile.getAbsolutePath()),100,100,200,200
                            );
                    bmp.getByteCount();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    mImage.close();
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_tab, container, false);

        mTextureView = (TextureView) rootView.findViewById(R.id.textureView);
        flashToggle = (ToggleButton) rootView.findViewById(R.id.ibtnFlash);
        btnCapture = (ImageButton) rootView.findViewById(R.id.Capture);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Pressed", Toast.LENGTH_SHORT).show();
                takephoto(v);
            }
        });
        final Boolean isFlashAvailabe = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!isFlashAvailabe) {
            flashToggle.setEnabled(false);
            flashToggle.setAlpha(0.4f);
        }
        flashToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                }

        });

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        openBackgroundThread();
        if (mTextureView.isAvailable()) {
            setUpCamera(mTextureView.getWidth(), mTextureView.getHeight());
            openCamera();

        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        closeBackgroundThread();
        super.onPause();
    }

    private Size getPreferredPreviewSize() {
//        List<Size> collectorSizes = new ArrayList<>();
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        Size size = new Size(point.x,point.y);
//        for (Size option : mapSizes) {
//            if (width > height) {
//                if (option.getWidth() > width &&
//                        option.getHeight() > height) {
//                    collectorSizes.add(option);
//                } else {
//                    if (option.getWidth() > height &&
//                            option.getHeight() > width) {
//                        collectorSizes.add(option);
//                    }
//                }
//            }
//
//        }
//        if (collectorSizes.size() > 0) {
//            return Collections.min(collectorSizes, new Comparator<Size>() {
//
//                @Override
//                public int compare(Size lhs, Size rhs) {
//                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getHeight());
//                }
//            });
//
//        }
        return size;
    }

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    if (mCameraDevice == null) {
                        return;
                    }
                    try {
                        mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                        mCameraCaptureSession = session;
                        mCameraCaptureSession.setRepeatingRequest(
                                mPreviewCaptureRequest,
                                mSessionCaptureCallback,
                                mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(getActivity(), "Create Capture session Failed", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera2  background thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void closeBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void takephoto(View view) {
        lockFocus();
        try {
            mImageFile = createImageFile();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void lockFocus() {
        mState = STATE_WAIT_LOCK;
        mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_START);
        try {
            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(),
                    mSessionCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unlockFocus() {
        mState = STATE_PREVIEW;
        mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
        try {
            mCameraCaptureSession.capture(mPreviewCaptureRequestBuilder.build(),
                    mSessionCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void captureStillImage() {
        try {
            CaptureRequest.Builder captureStillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureStillBuilder.addTarget(mImageReader.getSurface());
            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureStillBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            CameraCaptureSession.CaptureCallback captureCallback =
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            Toast.makeText(getActivity(), "ImageCaptured", Toast.LENGTH_SHORT).show();
// TODO Focus locking and unlocking creates issue in capturing image on oneplus 3.
                            unlockFocus();
                        }
                    };

            mCameraCaptureSession.capture(
                    captureStillBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        mGalleryFolder = new File(storageDirectory, "Cam Math");
        if(!mGalleryFolder.exists()) {
            mGalleryFolder.mkdirs();
        }
        File image = File.createTempFile(imageFileName,".jpg",mGalleryFolder);
        mImageFileLocation = image.getAbsolutePath();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap temp= BitmapFactory.decodeFile(mImageFileLocation,bmOptions);


        return image;

    }
}

