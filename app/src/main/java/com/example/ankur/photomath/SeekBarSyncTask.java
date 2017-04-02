package com.example.ankur.photomath;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

/**
 * Created by ankur on 28/3/17.
 */

public class SeekBarSyncTask extends AsyncTask<Object,Integer,Bitmap> {
    @Override
    protected Bitmap doInBackground(Object... params) {
        Bitmap src = (Bitmap) params[0];    // grayscale image is passed
        Bitmap dest = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        int threshold = 200-(int) params[1];  // minimum threshold can be 50 & max 200
        int width =src.getWidth();
        int height = src.getHeight();
        int size = width*height;
        int ImageBuffer [] =new int[size];
        src.getPixels(ImageBuffer,0,width,0,0,width,height);
        int pixel, temp;
        for (int i=0;i<size;i++){
            temp = ImageBuffer[i];
            pixel = (temp >> 16) & 0xff;
            if(pixel>threshold){
                ImageBuffer[i]=Color.argb(255, 255,255,255);
            } else {
                ImageBuffer[i] = Color.argb(255, temp, temp, temp);
            }
        }
        dest.reconfigure(width,height,src.getConfig());
        dest.setPixels(ImageBuffer,0,width,0,0,width,height);
        return dest;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        SeekActivity.preview.setImageBitmap(bitmap);
    }
}
