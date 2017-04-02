package com.example.ankur.photomath;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;


public class SeekActivity extends AppCompatActivity {
    SeekBar seekBar;
    static boolean crashed = false;
    FloatingActionButton capture;
    Button set, reset;
    static ImageView preview;
    Bitmap intentBitmap;
    int crashedThreshold;
    private Uri mImageUri;
    public static final int REQUEST_CODE = 4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        capture = (FloatingActionButton) findViewById(R.id.Capture);
        set = (Button) findViewById(R.id.btnSet);
        reset = (Button) findViewById(R.id.btnReset);
        seekBar.setMax(150);
        preview = (ImageView) findViewById(R.id.SettingsImageView);
        final Intent i = new Intent(SeekActivity.this, MainActivity.class);
        if (crashed) {
            crashed = false;
            byte[] byteArray = getIntent().getByteArrayExtra("image");
            crashedThreshold = getIntent().getIntExtra("crashed Threshold", 0);
            intentBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            preview.setImageBitmap(intentBitmap);
            Toast.makeText(this, "Something went wrong Please select proper colour range", Toast.LENGTH_SHORT).show();
        }
        if (intentBitmap == null) {
            set.setEnabled(false);
        }
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraTab.thresholdSet = true;
                Toast.makeText(SeekActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                startActivity(i);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraTab.thresholdSet = false;
                Toast.makeText(SeekActivity.this, "Reset Successfully", Toast.LENGTH_SHORT).show();
                startActivity(i);
            }
        });

        capture.setOnClickListener(new View.OnClickListener() {

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
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (intentBitmap != null) {
                    CameraTab.threshold = 200 - progress;
                    new SeekBarSyncTask().execute(intentBitmap, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (intentBitmap == null) {
                    Toast.makeText(SeekActivity.this, "Image is not set", Toast.LENGTH_SHORT).show();

                }

            }
        });

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            getContentResolver().notifyChange(mImageUri,null);
            ContentResolver cr = getContentResolver();
            Bitmap bitmap = null;
            try {
                bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, mImageUri);

            }catch (IOException e){
                e.printStackTrace();
            }
            intentBitmap =BitmapFactory.decodeFile(mImageUri.getPath());
//            int height= intentBitmap.getHeight();
            int width=intentBitmap.getWidth();
            intentBitmap=Bitmap.createBitmap(intentBitmap,0,660,width,600);
            int size = intentBitmap.getWidth() * intentBitmap.getHeight();
            int[] ImageBuffer = new int[size];
            intentBitmap.getPixels(ImageBuffer, 0, intentBitmap.getWidth(), 0, 0, intentBitmap.getWidth(), intentBitmap.getHeight());
            int temp;
            int R, G, B;
//            src.recycle();
            for (int i = 0; i < size; i++) {
                temp = ImageBuffer[i];
                R = (temp >> 16) & 0xff;
                G = (temp >> 8) & 0xff;
                B = temp & 0xff;
                temp = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                ImageBuffer[i] = Color.argb(255, temp, temp, temp);
            }
            intentBitmap.setPixels(ImageBuffer,0,intentBitmap.getWidth(),0,0,intentBitmap.getWidth(),intentBitmap.getHeight());
            preview.setImageBitmap(intentBitmap);
            set.setEnabled(true);
        }

    }

    public File createTemporaryFile(String part, String ext) throws IOException{
        File tempDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+File.separator+"Cam Math");
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
