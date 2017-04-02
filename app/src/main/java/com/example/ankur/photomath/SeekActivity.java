package com.example.ankur.photomath;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;


public class SeekActivity extends AppCompatActivity {
    SeekBar seekBar;
    static boolean crashed = false;
    FloatingActionButton capture;
    Button set, reset;
    static ImageView preview;
    Bitmap intentBitmap;
    int crashedThreshold;

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
        final Intent i = new Intent(SeekActivity.this,MainActivity.class);
        if (crashed) {
            crashed = false;
            byte[] byteArray = getIntent().getByteArrayExtra("image");
            crashedThreshold = getIntent().getIntExtra("crashed Threshold", 0);
            intentBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            preview.setImageBitmap(intentBitmap);
            Toast.makeText(this, "Something went wrong Please select proper colour range", Toast.LENGTH_SHORT).show();
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
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                new SeekBarSyncTask().execute(intentBitmap, progress);
                CameraTab.threshold = 200 - progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
}
