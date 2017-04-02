package com.example.ankur.photomath;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.example.ankur.photomath.CameraTab.av;

/**
 * Created by ankur on 10/3/17.
 */



    class OtsuAlgo extends AsyncTask<Bitmap, Void, ArrayList> {
        @Override
        protected ArrayList<Bitmap> doInBackground(Bitmap... params) {
            Bitmap src = params[0];  // cropped colour image is passed
            int size = src.getWidth() * src.getHeight();
            int[] ImageBuffer = new int[size];
            src.getPixels(ImageBuffer, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
            int temp;
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
            src.setPixels(ImageBuffer,0,src.getWidth(),0,0,src.getWidth(),src.getHeight());     // converted to grayscale image

            if (!CameraTab.thresholdSet) {
                for (int i = 2; i <=250 ; i++)
                 {
                    int sumB = sum(greyLevel, i, 0);
                    int sumF = sum(greyLevel, 200, i);
                    if (sumB == 0 || sumF == 0)
                        continue;
                    double weightB = (double)sumB /size ;
                    double meanB = meanNumerator(greyLevel, i, 0) / sumB;
                    double varianceB = variance(greyLevel, i, 0, meanB) / sumB;
                    double weightF = sumF /(double)size;
                    double meanF = meanNumerator(greyLevel, 200, i) / sumF;
                    double varianceF = variance(greyLevel, 200, i, meanF) / sumF;
                    double classVariance = weightB * varianceB + weightF * varianceF;
                    if (classVariance < minValue) {
                        minValue = classVariance;
                        CameraTab.threshold = i;
                        System.out.println("Threshold is :\t"+CameraTab.threshold+" Class Variance is :\t"+classVariance);
                    }

                }
            }

//           Low Pass Filtering
            for (int index = 0; index < ImageBuffer.length; index++) {
                if (CameraTab.threshold < (ImageBuffer[index] & 0xff))
                    ImageBuffer[index] = Color.argb(255, 255, 255, 255);
            }
            Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
            dest.setPixels(ImageBuffer, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());
            String Filewrite = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DCIM) + "/" + "Cam Math/testrewrite.jpg";
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
            ArrayList<Bitmap> retutrnParam = new ArrayList<>(2);
            retutrnParam.add(dest);     //  low pass filtered image
            retutrnParam.add(src);      //  grayscale image
            return retutrnParam;
        }

        private strictfp double variance(int[] greyLevel, int limit, int start, double meanB) {
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
        protected void onPostExecute(ArrayList params) {
            super.onPostExecute(params);
            CameraTab.preview.setImageBitmap((Bitmap) params.get(0));
            new GrayscaleThreshold().execute ((Bitmap)params.get(0),(Bitmap)params.get(1));
            Toast.makeText(av, "Threshold is : "+CameraTab.threshold, Toast.LENGTH_SHORT).show();

            }

    }