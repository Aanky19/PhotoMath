package com.example.ankur.photomath;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import static com.example.ankur.photomath.CameraTab.av;

/**
 * Created by ankur on 5/4/17.
 */

public class WeightImportTask extends AsyncTask<String, Void, String> {
    Context context;

    WeightImportTask(Context context) {
        this.context = context;
    }
    @Override
    protected String doInBackground(String[] jsonFiles) {
        String weightJSON = "";
        try {
            InputStream jsonIS = context.getAssets().open(jsonFiles[0]);
            int size = jsonIS.available();
            byte[] buffer = new byte[size];
            jsonIS.read(buffer);
            jsonIS.close();
            weightJSON = new String(buffer, "UTF-8");
            File tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),"jsonOutput.txt");
            tempFile.createNewFile();
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(tempFile));
            out.write(weightJSON);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject weightData = null;
        try {
            weightData = new JSONObject(weightJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return weightJSON;
    }

    @Override
    protected void onPostExecute(String jsonObject) {
        super.onPostExecute(jsonObject);
        System.out.println(jsonObject);
    }
}
