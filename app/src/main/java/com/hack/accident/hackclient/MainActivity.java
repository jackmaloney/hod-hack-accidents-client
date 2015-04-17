package com.hack.accident.hackclient;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    public final static String apiURL = "http://ws.strikeiron.com/StrikeIron/EMV6Hygiene/VerifyEmail?";
    public final static String EXTRA_MESSAGE = "com.example.webapitutorial.MESSAGE";
    private final static int TAKE_PHOTO_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public final static String DEBUG_TAG = "MakePhotoActivity";

    private String imageFile = null;
    private Accident accident = new Accident();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accident.setLatitude("123");
        accident.setLongitude("456");

        Button capture = (Button) findViewById(R.id.btnCamera);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String directory = getDirectory();
                Uri outputFileUri = getOutputFileUri();
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                imageFile = outputFileUri.toString();
                startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_PHOTO_CODE && resultCode == RESULT_OK) {
            Log.d("CameraDemo", "Pic saved in " + imageFile);
            ExifClient exifClient = new ExifClient(imageFile);
            exifClient.loadExifData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // This is the method that is called when the submit button is clicked
    public void submitMetadata(View view) {
            String urlParams = "&latitude=%s&longitude=%s".format(accident.getLatitude(), accident.getLongitude());
            String urlString = apiURL + urlParams;
            new CallAPI().execute(urlString);
    }

    private class CallAPI extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString=params[0];
            String result = null;
            InputStream in = null;

            // HTTP Get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = new BufferedInputStream(urlConnection.getInputStream());
            } catch (Exception e ) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }

            result = parseJson(in);

            return result;
        }

        protected void onPostExecute(String result) {

            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);

            intent.putExtra(EXTRA_MESSAGE, result);

            startActivity(intent);
        }

    } // end CallAPI

//    {"status":"OK","timestamp":"2015-04-17 11:44:16 +0100","latitude":"51.494883","longitude":"-0.129057","address_data":{"number":"78-96","street":"Marsham Street","post_code":"SW1P 4LY"}}


    private String parseJson(InputStream inputStream) {
        String result = null;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            // Oops
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
        }

        return result;
    }

    private String getDirectory() {
        final String dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + "/picFolder/";
        File newDirectory = new File(dir);
        newDirectory.mkdirs();
        return dir;
    }

    private Uri getOutputFileUri() {
        Long tsLong = System.currentTimeMillis()/1000;Log.d("CameraDemo", "Pic saved");
        String timestamp = tsLong.toString();
        String file = getDirectory() + timestamp + ".jpg";
        File newFile = new File(file);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(newFile);
    }
}