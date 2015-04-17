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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity {

    public final static String apiURL = "http://ws.strikeiron.com/StrikeIron/EMV6Hygiene/VerifyEmail?";
    public final static String EXTRA_MESSAGE = "com.example.webapitutorial.MESSAGE";

    private Accident accident = new Accident();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        accident.setLatitude("123");
        accident.setLongitude("456");

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
            String resultToDisplay;
            EmailVerificationResult result = null;
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

            // Parse XML
            XmlPullParserFactory pullParserFactory;

            try {
                pullParserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = pullParserFactory.newPullParser();

                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(in, null);
                result = parseXML(parser);
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Simple logic to determine if the email is dangerous, invalid, or valid
            if (result != null ) {
                if( result.hygieneResult.equals("Spam Trap")) {
                    resultToDisplay = "Dangerous email, please correct";
                }
                else if( Integer.parseInt(result.statusNbr) >= 300) {
                    resultToDisplay = "Invalid email, please re-enter";
                }
                else {
                    resultToDisplay = "Thank you for your submission";
                }
            }
            else {
                resultToDisplay = "Exception Occured";
            }

            return resultToDisplay;
        }

        protected void onPostExecute(String result) {

            Intent intent = new Intent(getApplicationContext(), ResultActivity.class);

            intent.putExtra(EXTRA_MESSAGE, result);

            startActivity(intent);
        }

    } // end CallAPI

    private EmailVerificationResult parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {

        int eventType = parser.getEventType();
        EmailVerificationResult result = new EmailVerificationResult();

        while( eventType!= XmlPullParser.END_DOCUMENT) {
            String name = null;

            switch(eventType)
            {
                case XmlPullParser.START_TAG:
                    name = parser.getName();

                    if( name.equals("Error")) {
                        System.out.println("Web API Error!");
                    }
                    else if ( name.equals("StatusNbr")) {
                        result.statusNbr = parser.nextText();
                    }
                    else if (name.equals("HygieneResult")) {
                        result.hygieneResult = parser.nextText();
                    }

                    break;

                case XmlPullParser.END_TAG:
                    break;
            } // end switch

            eventType = parser.next();
        } // end while

        return result;
    }

    private class EmailVerificationResult {
        public String statusNbr;
        public String hygieneResult;
    }
}
