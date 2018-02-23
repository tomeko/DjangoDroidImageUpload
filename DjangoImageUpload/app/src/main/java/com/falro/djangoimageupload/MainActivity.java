package com.falro.djangoimageupload;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    public static final String FILE_UPLOAD_URL = "http://192.168.1.12:8000/upload_file";    // path to django/flask upload endpoint
    public static String opFilePath = "";           // global filepath to temporary stored bitmap
    public static long totalSize = 0;               // global size of temporary stored bitmap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Button that triggers upload */
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                /* File that points to bitmap */
                File img_file = null;

                /* Set bitmap from local resource for this demo */
                Bitmap img = getBitmapFromAsset("jimmy.jpg");
                try{
                    img_file = saveBitmap(img,"jimmy.jpg");
                } catch (IOException e) {
                    Log.d("Log", "File not found");
                }

                /* Set path and size global vars */
                opFilePath =  img_file.getAbsolutePath();
                totalSize = BitmapCompat.getAllocationByteCount(img);

                /* Trigger upload function asynctask */
                UploadFileToServer uploadFileToServer = new UploadFileToServer();
                uploadFileToServer.execute();
            }
        });
    }

    public class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /* show result of upload in a toast */
        @Override
        protected String doInBackground(Void... params) {
            final String ret = uploadFile();
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(),ret, Toast.LENGTH_LONG).show();
                }
            });
            return ret;
        }

        private String uploadFile() {
            String responseString = null;
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(FILE_UPLOAD_URL);
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {
                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });
                File file = new File(opFilePath);
                entity.addPart("pic", new FileBody(file));      // the name here needs to match on python side
                totalSize = entity.getContentLength();
                httppost.setEntity(entity);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode + " -> " + response.getStatusLine().getReasonPhrase();
                    Log.d("Log", responseString);
                }
            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            return responseString;
        }
    }

    /* saves bitmap to local cache dir */
    public File saveBitmap(Bitmap bmp, String fname) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(getCacheDir() + File.separator + fname);
        if (f.exists())
            return f;
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }

    /* returns bitmap from local asset folder (only for demo) */
    private Bitmap getBitmapFromAsset(String strName)
    {
        AssetManager assetManager = getAssets();
        InputStream istr = null;
        try {
            istr = assetManager.open(strName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
    }

}
