package test.alien.com.phototest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.alibaba.fastjson.JSON;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import test.alien.com.phototest.bean.CameraSize;

/**
 * this is made from android developer
 * Created by alien on 16/5/19.
 */
public class MainActivityGoogleDemon extends Activity implements CameraPreview.OnSurfaceCallBack {
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Camera mCamera;
    private CameraPreview mPreview;
    private RecyclerView imgRecycler;
    private ImageAdapter imageAdapter;
    private Spinner spinner;
    private FrameLayout preview;
    private  boolean isSafeToTake = true;
    private List<String> picList  = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_google);

        if (!checkCameraHardware(this)) return;
        // Create an instance of Camera
        mCamera = getCameraInstance();
        // Create our Preview view and set it as the content of our activity.
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        spinner = (Spinner) findViewById(R.id.spinner);
        imgRecycler = (RecyclerView) findViewById(R.id.imgRecycler);
        imageAdapter = new ImageAdapter(this);
        imgRecycler.setLayoutManager(new LinearLayoutManager(this));
        imgRecycler.setAdapter(imageAdapter);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(parameters);
                }else{
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                }
            }
        });

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isSafeToTake) mCamera.takePicture(null, null, mPicture);
                        isSafeToTake =false;
                    }
                }
        );

        Button clearCache  = (Button) findViewById(R.id.clearCache);
        clearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "MyCameraApp");

                if (!mediaStorageDir.exists()) return;
                mediaStorageDir.delete();
                if(mediaStorageDir.listFiles() == null) return;
                for (File file : mediaStorageDir.listFiles()) {
                    file.delete();
                }
                picList.clear();
                imageAdapter.refershData(picList);

            }
        });
        final List<String> strList = new ArrayList<>();
        for (Iterator iterator = mCamera.getParameters().getSupportedPictureSizes().iterator(); iterator.hasNext(); ) {
            Camera.Size size = (Camera.Size) iterator.next();
            CameraSize cameraSize = new CameraSize();
            cameraSize.setHeight(size.height);
            cameraSize.setWidth(size.width);
            String jsonStr = JSON.toJSONString(cameraSize);
            strList.add(jsonStr);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                CameraSize cameraSize = JSON.parseObject(strList.get(i), CameraSize.class);
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureSize(cameraSize.getWidth(), cameraSize.getHeight());
                mCamera.setParameters(parameters);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        initialSdPic();
    }


    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Check if this device has a camera
     * <p/>
     * If your application does not specifically require a camera using a manifest declaration,
     * you should check to see if a camera is available at runtime.
     * To perform this check, use the PackageManager.hasSystemFeature() method,
     * as shown in the example code below:
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mCamera  == null){
            mCamera = getCameraInstance();
            mPreview.getHolder().addCallback(mPreview);
        }
        mPreview = new CameraPreview(this, mCamera,  this);
        preview.addView(mPreview);
    }


    @Override
    protected void onPause() {
        super.onPause();
        preview.removeView(mPreview);

    }

    /**
     * Caution: Remember to release the Camera object by calling the Camera.release()
     * when your application is done using it! If your application does not properly release the camera,
     * all subsequent attempts to access the camera, including those by your own application,
     * will fail and may cause your or other applications to be shut down.
     */
    @Override
    protected void onDestroy() {
        picList.clear();
        releaseCamera();
        super.onDestroy();
    }

    @Override
    public void onSurfaceCreate(SurfaceHolder holder) {
    }

    @Override
    public void onSurfaceDestroy(SurfaceHolder holder) {
        releaseCamera();
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }


    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                isSafeToTake = true;
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap realImage = BitmapFactory.decodeByteArray(data,0, data.length);
                ExifInterface exif = new ExifInterface(pictureFile.toString());
                if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                    realImage = rotate(realImage, 90);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                    realImage = rotate(realImage, 270);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                    realImage = rotate(realImage, 180);
                } else if (exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")) {
                    realImage = rotate(realImage, 90);
                }
                realImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
                imageAdapter.addItem(pictureFile.getPath());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
            isSafeToTake = true;
        }
    };

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }


    /**
     * Create a File for saving an image or video
     */
    private  File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }


    private void initialSdPic() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) return;
        File[] allfiles = mediaStorageDir.listFiles();
        if (allfiles == null) return;

        // initial img file path list
        for (File file : allfiles) {
            int idx = file.getPath().lastIndexOf(".");
            if (idx <= 0) {
                continue;
            }
            String suffix = file.getPath().substring(idx);
            if (suffix.toLowerCase().equals(".jpg") ||
                    suffix.toLowerCase().equals(".jpeg") ||
                    suffix.toLowerCase().equals(".bmp") ||
                    suffix.toLowerCase().equals(".png") ||
                    suffix.toLowerCase().equals(".gif")) {
                picList.add(file.getPath());
            }
        }
        if (picList.size() != 0) imageAdapter.refershData(picList);
    }

}
