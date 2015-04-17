package com.hack.accident.hackclient;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;


public class MainActivity extends ActionBarActivity {

    public final static String apiURL = "http://ws.strikeiron.com/StrikeIron/EMV6Hygiene/VerifyEmail?";
    public final static String EXTRA_MESSAGE = "com.example.webapitutorial.MESSAGE";

    private Accident accident = new Accident();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        accident.setLatitude("51.494883");
        accident.setLongitude("-0.129057");

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

                byte[] contents = new byte[1024];

                int bytesRead=0;
                String strFileContents = null;
                while( (bytesRead = in.read(contents)) != -1){
                    strFileContents = new String(contents, 0, bytesRead);
                }
                result = parseJson(strFileContents);


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
}
