package com.example.ankur.photomath;

/**
 * Created by ankur on 5/3/17.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GrayscaleThreshold extends AppCompatActivity {
    String Fileread;
    ImageView ivAfter, ivBefore;
    Button btnStart;
    Long start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grayscale_threshold);
        ivAfter = (ImageView) findViewById(R.id.ivAfter);
        ivBefore = (ImageView) findViewById(R.id.ivBefore);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = System.currentTimeMillis();
                Toast.makeText(GrayscaleThreshold.this, "Clicked", Toast.LENGTH_SHORT).show();
                File file = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DCIM);
                Fileread = file + "/" + "Camera_Demo/test1.jpg";


                Bitmap bitmap = BitmapFactory.decodeFile(Fileread);
                if (bitmap != null) {
                    Log.d("File ", "File is preset");
                    ivBefore.setImageBitmap(bitmap);
                    ImageHandler img = new ImageHandler();
                    img.execute(bitmap);
                }
            }
        });

    }

    class ImageHandler extends AsyncTask<Bitmap, Integer, Bitmap> {
        @Override

        protected Bitmap doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            ArrayList<Bitmap> horizontalList = new ArrayList<>();
            ArrayList<Bitmap> verticalList = new ArrayList<>();
            int temp, threshold = 90;
            int R, G, B;
            int columns = bitmap.getWidth();
            int rows = bitmap.getHeight();
            int[] ImageBuffer = new int[columns * rows];
            bitmap.getPixels(ImageBuffer, 0, columns, 0, 0, columns, rows);
//            bitmap.recycle();
            for (int i = 0; i < columns * rows; i++) {
                temp = ImageBuffer[i];
                R = (temp >> 16) & 0xff;
                G = (temp >> 8) & 0xff;
                B = temp & 0xff;
                temp = (int) (0.299 * R + 0.587 * G + 0.114 * B);
                if (temp > threshold) {
                    temp = 255;
                }
                ImageBuffer[i] = Color.argb(255, temp, temp, temp);
            }
            // Image as output
            // bitmap=bitmap.createBitmap(ImageBuffer,columns,rows, Bitmap.Config.ARGB_8888);

            //  column scan and trim
            boolean columnbit[] = columnscan(ImageBuffer, columns, rows, threshold);
            int colstart = 0, rowstart = 0;
            int colend = 0, rowend = 0;
            for (int i = 0; i < columns; i++) {
                if (columnbit[i]) {
                    colstart = i;
                    break;
                }
            }
            for (int i = columns - 1; i >= 0; i--) {
                if (columnbit[i]) {
                    colend = i;
                    break;
                }
            }
            boolean[] temp2 = new boolean[colend - colstart + 1];
            ImageBuffer = verticalTrim(ImageBuffer, colstart, colend, rows, columnbit);
            System.arraycopy(columnbit, colstart, temp2, 0, colend - colstart + 1);
            columnbit = temp2;
            temp2 = null;
//            bitmap = bitmap.createBitmap(ImageBuffer, colend - colstart + 1, rows, Bitmap.Config.ARGB_8888);
            columns = colend - colstart + 1;

            //  row scan and trim
            boolean rowbit[] = rowscan(ImageBuffer, columns, rows, threshold);
            for (int i = 0; i <= rows; i++) {
                if (rowbit[i]) {
                    rowstart = i;
                    break;
                }
            }
            for (int i = rows - 1; i >= 0; i--) {
                if (rowbit[i]) {
                    rowend = i;
                    break;
                }
            }
            ImageBuffer = horizontalTrim(ImageBuffer, rowstart, rowend, columns, rowbit);
            temp2 = new boolean[rowend - rowstart + 1];
            System.arraycopy(rowbit, rowstart, temp2, 0, rowend - rowstart + 1);
            rowbit = temp2;
            temp2 = null;
            bitmap = bitmap.createBitmap(ImageBuffer, columns, rowend - rowstart + 1, Bitmap.Config.ARGB_8888);
            ImageBuffer = null;
            String Filewrite = Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DCIM) + "/" + "Camera_Demo/testrewrite.jpg";
//            bitmap = Bitmap.createBitmap(ImageBuffer, width, height, Bitmap.Config.ARGB_8888);
            try {
                FileOutputStream out = new FileOutputStream(Filewrite);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Horizontal character seperation starts here
            int startpoint = 0;
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            boolean endSetFlag = false;
            Bitmap tempBitmapHolder = null;
            while (startpoint < columnbit.length - 1) {
                int i;
                endSetFlag = false;
                for (i = startpoint; i < columnbit.length; i++) {
                    if (columnbit[i]) {               // if black pixel column found
                        colstart = i;
                        break;
                    }
                }
                for (; i < columnbit.length; i++) {
                    if (!columnbit[i]) {              // if no black pixel column found
                        colend = i;
                        endSetFlag = true;
                        break;
                    }
                }
                if (endSetFlag == false && i == (columnbit.length)) {
                    colend = i;
                }
                tempBitmapHolder = Bitmap.createBitmap(bitmap, colstart, 0, colend - colstart, height);
                horizontalList.add(tempBitmapHolder);
                startpoint = colend + 1;
            }
            columnbit = null;
            rowbit = null;
            System.gc();
            boolean rowstartflag = false;
            Bitmap tempHorizontalHolder;
            Outer:
            for (int count = 0; count < horizontalList.size(); count++) {
                tempHorizontalHolder = horizontalList.get(count);
                width = tempHorizontalHolder.getWidth();
                height = tempHorizontalHolder.getHeight();
                ImageBuffer = new int[width * height];
                tempHorizontalHolder.getPixels(ImageBuffer, 0, width, 0, 0, width, height);
                rowbit = rowscan(ImageBuffer, width, height, threshold);
                startpoint = 0;
                int partOf = 0;
                while (startpoint < rowbit.length) {
                    int i = 0;
                    endSetFlag = false;
                    rowstartflag = false;
                    for (i = startpoint; i < rowbit.length; i++) {
                        if (rowbit[i]) {               // if black pixel row found
                            rowstart = i;
                            rowstartflag = true;
                            break;
                        }
                    }

                    for (; i < rowbit.length; i++) {
                        if (!rowbit[i]) {              // if no black pixel row found
                            rowend = i - 1;
                            endSetFlag = true;
                            break;
                        }
                    }
                    if (rowstartflag == false)
                        continue Outer;
                    if ((endSetFlag == false) && (i == (rowbit.length))) {
                        rowend = i - 1;
                    }
                    height = rowend - rowstart;
                    if (height > 10) {
                        if (height * 3 < width && partOf == 0) {  // To avoid minus getting streched in the image
                            if (rowstart + width / 2 <= rowend + 10) {
                                tempBitmapHolder = doInvert(Bitmap.createBitmap(tempHorizontalHolder, 0, 0, width, width));
                            } else {
                                tempBitmapHolder = doInvert(Bitmap.createBitmap(tempHorizontalHolder, 0, rowstart - width / 2, width, width));
                            }
                        } else {
                            tempBitmapHolder = doInvert(Bitmap.createBitmap(tempHorizontalHolder, 0, rowstart, width, height));
                        }
                        // differentiate between divide & -
                        if ((tempBitmapHolder.getWidth() > (3 * tempBitmapHolder.getHeight())) && partOf > 0)
                            tempBitmapHolder = RotateBitmap(tempBitmapHolder, -45);
                        verticalList.add(BITMAP_RESIZER(tempBitmapHolder, 28, 28));
                        partOf++;
                    }
                    startpoint = rowend + 1;
                }
            }
            horizontalList.clear();
            for (int i = 0; i < verticalList.size(); i++) {
                Filewrite = Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DCIM) + "/" + "Camera_Demo/" + i + ".jpg";
                try {
                    FileOutputStream out = new FileOutputStream(Filewrite);
                    verticalList.get(i).compress(Bitmap.CompressFormat.JPEG, 100, out);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return verticalList.get(0);
        }

        private Bitmap RotateBitmap(Bitmap source, float angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }

        public Bitmap BITMAP_RESIZER(Bitmap bitmap, int newWidth, int newHeight) {
            Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

            float ratioX = newWidth / (float) bitmap.getWidth();
            float ratioY = newHeight / (float) bitmap.getHeight();
            float middleX = newWidth / 2.0f;
            float middleY = newHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setFilterBitmap(true);
            paint.setDither(true);
            canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2, paint);

            return scaledBitmap;

        }

        public Bitmap doInvert(Bitmap src) {
            // create new bitmap with the same settings as source bitmap
            Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
            // color info
            int A, R, G, B;
            int pixelColor;
            // image size
            int height = src.getHeight();
            int width = src.getWidth();
            int imageArray[] = new int[height * width];
            src.getPixels(imageArray, 0, width, 0, 0, width, height);
            // scan through every pixel
            for (int y = 0; y < imageArray.length; y++) {
                // get one pixel
                // saving alpha channel
//                    A = Color.alpha(imageArray[y]);
                // inverting byte for each R/G/B channel
                R = 255 - (imageArray[y] >> 16) & 0xff;
//                    G = 255 - Color.green(imageArray[y]);
//                    B = 255 - Color.blue(imageArray[y]);
                imageArray[y] = Color.argb(255, R, R, R);
            }
            // set newly-inverted pixel to output image
            bmOut.setPixels(imageArray, 0, width, 0, 0, width, height);

            // return final bitmap
            return bmOut;
        }

        private boolean[] rowscan(int[] ImageBuffer, int columns, int rows, int threshold) {
            boolean rowbit[] = new boolean[rows];
            for (int r = 0; r < rows; r++) {
                int count = 0;
                int mtemp = r * columns;
                while (count < columns) {
                    if (Color.red(ImageBuffer[mtemp + count]) < threshold) {
                        rowbit[r] = true;
                        break;
                    }
                    count++;
                }
            }
            return rowbit;
        }

        private boolean[] columnscan(int[] ImageBuffer, int columns, int rows, int threshold) {
            boolean columnbit[] = new boolean[columns];     //true represent black pixel present
            Outer:
            for (int c = 0; c < columns; c++) {
                for (int r = 0; r < rows; r++) {
                    if (c == 0 && Color.red(ImageBuffer[columns * r]) < threshold) {
                        columnbit[c] = true;
                        continue Outer;
                    }
                    if (c != 0 && Color.red(ImageBuffer[(columns * r) + c]) < threshold) {
                        columnbit[c] = true;
                        continue Outer;
                    }
                }
            }
            return columnbit;
        }

        private int[] verticalTrim(int[] ImageBuffer, int start, int end, int rows,
                                   boolean[] columnbit) {
            int rowLength = end - start + 1;
            int temp[] = new int[rowLength * rows];
            int Index = 0;
            for (int i = 1; i <= rows; i++) {
                System.arraycopy(ImageBuffer, start, temp, Index, rowLength);
                Index = Index + rowLength;
                start = start + columnbit.length;
            }
            return temp;
        }

        private int[] horizontalTrim(int[] ImageBuffer, int start, int end, int columns, boolean[] rowbit) {
            int colLength = end - start + 1;
            int startIndex = start * columns;
            int temp[] = new int[colLength * columns];
            System.arraycopy(ImageBuffer, startIndex, temp, 0, colLength * columns);
            return temp;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ivAfter.setImageBitmap(bitmap);
            Long stop = System.currentTimeMillis();
            float len = (stop - start);
            Toast.makeText(GrayscaleThreshold.this, "Task completed in " + len + " milliseconds",
                    Toast.LENGTH_LONG).show();
        }
    }
}

