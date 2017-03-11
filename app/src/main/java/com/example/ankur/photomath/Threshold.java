package com.example.ankur.photomath;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ankur on 10/3/17.
 */

public class Threshold extends AppCompatActivity {

    String Fileread;
    ImageView ivAfter, ivBefore;
    Button btnStart;
    Long start;
    Button mainScreen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grayscale_threshold);
        ivAfter = (ImageView) findViewById(R.id.ivAfter);
        ivBefore = (ImageView) findViewById(R.id.ivBefore);
        btnStart = (Button) findViewById(R.id.btnStart);
        mainScreen= (Button) findViewById(R.id.intentbtn);
        mainScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(Threshold.this,MainActivity.class);
                startActivity(i);
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = System.currentTimeMillis();
                Toast.makeText(Threshold.this, "Clicked", Toast.LENGTH_SHORT).show();
                File file = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DCIM);
                Fileread = file + "/" + "Camera_Demo/test1.jpg";


                Bitmap bitmap = BitmapFactory.decodeFile(Fileread);
                if (bitmap != null) {
                    Log.d("File ", "File is preset");
                    ivBefore.setImageBitmap(bitmap);
                    Threshold.OtsuAlgo img = new Threshold.OtsuAlgo();
                    img.execute(bitmap);
                }
            }
        });

    }

    class OtsuAlgo extends AsyncTask<Bitmap, Integer, Bitmap> {
        @Override
        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap src = params[0];
            int size = src.getWidth() * src.getHeight();
            int[] ImageBuffer = new int[size];
            src.getPixels(ImageBuffer, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
            int temp, threshold = 0;
            double minValue = Double.MAX_VALUE;
            int R, G, B;
//            src.recycle();
            int greyLevel[] = new int[256];
            for (int i = 0; i < size; i++) {
                temp = ImageBuffer[i];
                R = (temp >> 16) & 0xff;
                G = (temp >> 8) & 0xff;
                B = temp & 0xff;
                temp = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                ImageBuffer[i] = Color.argb(255, temp, temp, temp);
                greyLevel[temp] = (greyLevel[temp]) + 1;
            }
            for (int i = 45; i < 200; i++) {
                int sumB = sum(greyLevel, i, 0);
                int sumF = sum(greyLevel, 200, i);
                if(sumB==0 || sumF==0)
                continue;
                double weightB = sumB / 65536;
                double meanB = meanNumerator(greyLevel, i, 0) / sumB;
                double varianceB = variance(greyLevel, i, 0, meanB) / sumB;
                double weightF = sumF / 65536;
                double meanF = meanNumerator(greyLevel, 200, i) / sumF;
                double varianceF = variance(greyLevel, 200, i, meanF) / sumF;
                double classVariance = weightB * varianceB + weightF * varianceF;
                if (classVariance < minValue) {
                    minValue = classVariance;
                    threshold = i;
                }
            }
//            Toast.makeText(getApplicationContext(),"Threshold " + threshold + " milliseconds",Toast.LENGTH_SHORT);
            for (int index = 0; index < ImageBuffer.length; index++) {
                if (threshold < (ImageBuffer[index] & 0xff))
                    ImageBuffer[index] = Color.argb(255, 255, 255, 255);
            }
            Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
            dest.setPixels(ImageBuffer, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
            String Filewrite = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DCIM) + "/" + "Camera_Demo/testrewrite.jpg";
//            bitmap = Bitmap.createBitmap(ImageBuffer, width, height, Bitmap.Config.ARGB_8888);
            try {
                FileOutputStream out = new FileOutputStream(Filewrite);
                dest.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dest;
        }

        private double variance(int[] greyLevel, int limit, int start, double meanB) {
            double variance = 0;
            for (int index = start; index < limit; index++) {
                variance = variance + (Math.pow((index - meanB), 2) * greyLevel[index]);
            }
            return variance;
        }

        private int meanNumerator(int[] greyLevel, int limit, int start) {
            int sum = 0;
            for (int index = start; index < limit; index++) {
                sum = sum + index * greyLevel[index];
            }
            return sum;
        }

        private int sum(int[] greyLevel, int limit, int start) {
            int sum = 0;
            for (int i = start; i < limit; i++) {
                sum = sum + greyLevel[i];
            }
            return sum;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ivAfter.setImageBitmap(bitmap);
            Long stop = System.currentTimeMillis();
            float len = (stop - start);
            Toast.makeText(Threshold.this, "Task completed in " + len + " milliseconds",
                    Toast.LENGTH_LONG).show();
        }

    }
}