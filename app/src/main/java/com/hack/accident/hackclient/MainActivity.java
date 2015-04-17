package com.hack.accident.hackclient;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationManager;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    public final static String apiURL = "http://ws.strikeiron.com/StrikeIron/EMV6Hygiene/VerifyEmail?";
    public final static String EXTRA_MESSAGE = "com.example.webapitutorial.MESSAGE";
    private final static int TAKE_PHOTO_CODE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    public final static String DEBUG_TAG = "MakePhotoActivity";

    private String imageFile = null;
    private Double longitude = null;
    private Double latitude = null;
    private Accident accident = new Accident();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accident.setLatitude("51.494883");
        accident.setLongitude("-0.129057");

        Button capture = (Button) findViewById(R.id.btnCamera);
        capture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
            Log.i("CameraDemo", "Pic saved in " + imageFile);
            ExifClient exifClient = new ExifClient(imageFile);
            exifClient.loadExifData();

            String locationProvider = LocationManager.GPS_PROVIDER;
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                longitude = lastKnownLocation.getLongitude();
                latitude = lastKnownLocation.getLatitude();
                if (longitude != null && latitude != null) {
                    accident.setLongitude(longitude + "");
                    accident.setLatitude(latitude + "");
                    Log.i("CameraDemo", "Last known location: lat %s, long %s".format(latitude + "", longitude + ""));
                } else {
                    Log.i("CameraDemo", "No latitude and longitude");
                }
            } else {
                Log.i("CameraDemo", "No location");
            }
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
            String urlString = params[0];
            String result = null;
            InputStream in = null;

            // HTTP Get
            try {
                String fileContents = getFileContents();
                result = parseJson(fileContents);
            } catch (Exception e ) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }

            return result;
        }

        protected void onPostExecute(String result) {

            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);

            intent.putExtra(EXTRA_MESSAGE, result);

            startActivity(intent);
        }

    } // end CallAPI

    private String getFileContents() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(new Date());
         return "{\"status\":\"OK\"," +
            "\"timestamp\":\"" + date + "\"," +
            "\"latitude\":\"51.494883\"," +
            "\"longitude\":\"-0.129057\"," +
            "\"address_data\":{\"number\":\"78-96\"," +
            "\"street\":\"Marsham Street\"," +
            "\"post_code\":\"SW1P 4LY\"}" +
            "}";
    }


    public String parseJson(String jsonString) {
        String result = null;

        try {
            StringBuilder sb = new StringBuilder();

            JSONObject root = new JSONObject(jsonString);

            if (root.getString("status").equals("OK")){
                sb.append("Your incident taken at " + root.getString("timestamp") + " has been succesfully recorded. \n");

                JSONObject addressData = root.getJSONObject("address_data");

                sb.append("The address is:  " + addressData.getString("number") + ", "
                        + addressData.getString("street") + ", "
                        + addressData.getString("post_code"));
                result = sb.toString();
            } else {
                result = "Your submission was unsuccessful";
            }
        } catch (Exception e) {
            System.out.print("There was an error with your submission");
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
        Long tsLong = System.currentTimeMillis()/1000;
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