package se.baraluftvapen.hansson.flora;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    //address till onlinefilen
    private static final String DEBUG_TAG = "http://baraluftvapen.se/tmp/uploads/blom_update21.txt";

    //nyBlomma innehåller alla uppdaterade blommor. varje position i denna sträng är en blomma innehållande alla egenskaper
    private String[] nyBlomma;
    private String[] nodata = new String[]{"nodata", "", "", ""};
    private LinkedList<Flower> flowerListi;                  //Lista med alla blommor
    private BufferedReader reader;                          //inläsare av datafil
    private String[] flowerElement = new String[13];        //Innehåller varje del-information av en blomma
    private String line;                                    //inläsning av en rad i datafil

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
       
        //Hämtar ner updatefilen från servern
        ConnectivityManager connMgr = (ConnectivityManager)
        getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
          if (networkInfo != null && networkInfo.isConnected()) {
             new DownloadWebpageTask().execute(DEBUG_TAG);   //hämta uppdateringsfilen
          } else {
                //inget internet,hämta inte filen
          }
    }

    //hämta ner onlinefil igen, körs varje gång huvudmeny körs
    @Override
    public void onResume() {
        new DownloadWebpageTask().execute(DEBUG_TAG);
        super.onResume();
    }

    //---------------------------------------------------------------------------------------------------------------
    //Fem stycken metoder för varje menyval
    //---------------------------------------------------------------------------------------------------------------
    
    public void goBrowse(View view) {
        Intent i = new Intent(getApplicationContext(), Browse.class);
        i.putExtra("favo", false);
        //om ny data finns om växter
        if (nyBlomma != null) {
            if (!settingsData.getBoolean("offlinemode", false))
            i.putExtra("update", nyBlomma); //skicka med de nya uppdaterade blommorna
        } else {
            i.putExtra("update", nodata); //skicka med de nya uppdaterade blommorna
        }
        startActivity(i);
    }

    public void goFavo(View view) {
        Intent i = new Intent(getApplicationContext(), Browse.class);
        i.putExtra("favo", true);
        if (nyBlomma != null) {
            if (!settingsData.getBoolean("offlinemode", false))
            i.putExtra("update", nyBlomma); //skicka med de nya uppdaterade blommorna
        } else {
            i.putExtra("update", nodata); //skicka med de nya uppdaterade blommorna
        }
        startActivity(i);
    }

    public void goQuiz(View view) {
        Intent i = new Intent(getApplicationContext(), QuizMenu.class);
        startActivity(i);
    }

    public void goAbout(View view) {
        Intent intent = new Intent(this, TabLayoutActivity.class);
        if (nyBlomma != null) {
            SharedPreferences totalList = getSharedPreferences("superlist", 0);
            SharedPreferences.Editor editor = totalList.edit();
                editor.putString("appv", nyBlomma[0]);
                editor.apply();
        }
        startActivity(intent);
    }

    public void goSettings(View view) {
        Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
        i.putExtra("fromBrowse", false);
        startActivity(i);
    }

    // Reads an InputStream and converts it to a String.
    private String readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
                nyBlomma = result.split("\n");
        }
    }

    private class DownloadWebpageTask2 extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl2(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            shoulda = result.split("\n");
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 10240;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is, len);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    private String downloadUrl2(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 10240;

        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();

            // Convert the InputStream into a string
            return readIt(is, len);

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}
