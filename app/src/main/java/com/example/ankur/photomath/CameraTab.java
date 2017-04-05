package com.example.ankur.photomath;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContentResolverCompat;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.R.attr.data;
import static android.R.attr.theme;


/**
 * Created by ankur on 15/2/17.
 */

public class CameraTab extends Fragment {
    static Activity av;
    static long startTime;
    static long stopTime;
    static int threshold;
    static boolean thresholdSet=false;
    FloatingActionButton Capture;
    public static ImageView preview;
    private Uri mImageUri;
    public static final int REQUEST_CODE = 1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.camera_tab, container, false);
        Capture = (FloatingActionButton) rootView.findViewById(R.id.Capture);
        preview = (ImageView) rootView.findViewById(R.id.ImageView);
        av=getActivity();
        Bitmap bitmap = null;


        Capture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photo = null;
                try {
                    photo = createTemporaryFile("picture", ".jpg");
//                    photo.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mImageUri = Uri.fromFile(photo);
                cam.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(cam, REQUEST_CODE);
            }
        });
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            startTime=System.currentTimeMillis();
            getActivity().getContentResolver().notifyChange(mImageUri,null);
            ContentResolver cr = getActivity().getContentResolver();
            Bitmap bitmap = null;
            try {
                bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);

            }catch (IOException e){
                e.printStackTrace();
            }
            Bitmap copy =BitmapFactory.decodeFile(mImageUri.getPath());
            int height= copy.getHeight();
            int width=copy.getWidth();
            copy = Bitmap.createBitmap(copy,0,660,width,600);
            OtsuAlgo otsu=new OtsuAlgo();
            otsu.execute(copy);



        }

    }

    public static void toastMessage(){
        CameraTab.stopTime =System.currentTimeMillis();
        float time=(float)((stopTime-startTime)/1000);
        Toast.makeText(av,String.format("%8.4f s",time), Toast.LENGTH_SHORT).show();
    }

  private File createTemporaryFile(String part, String ext) throws IOException{
        File tempDir =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+File.separator+"Cam Math");
        if(!tempDir.exists()){
            tempDir.mkdirs();
        }else {
            String[] children = tempDir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(tempDir, children[i]).delete();
            }
        }
        return  File.createTempFile(part,ext,tempDir);
    }
}



