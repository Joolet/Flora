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
    private static final String DEBUG_TAG = "http://baraluftvapen.se/tmp/uploads/blom_update21.txt";
    private static final String DEBUG_SHOULD = "http://baraluftvapen.se/tmp/should_update.txt";

    //nyBlomma innehåller alla uppdaterade blommor. varje position i denna sträng är en blomma innehållande alla egenskaper
    private String[] nyBlomma;
    private String[] shoulda;
    private String[] nodata = new String[]{"nodata", "", "", ""};
    private static final int TIME_DELAY = 2000;
    private static long back_pressed;
    private static final int REQUEST_CODE = 0x11;
    private SharedPreferences settingsData;

    private LinkedList<Flower> flowerListi;                  //Lista med alla blommor
    private BufferedReader reader;                          //inläsare av datafil
    private String[] flowerElement = new String[13];        //Innehåller varje del-information av en blomma
    private String line;                                    //inläsning av en rad i datafil

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        settingsData = getSharedPreferences("settings", 0);

        /*
        String[] permissions = {"android.permission.WRITE_EXTERNAL_STORAGE"};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        */
        if (!settingsData.getBoolean("offlinemode", false)) {
            //kontrollerar om internetanslutning finns
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                new DownloadWebpageTask().execute(DEBUG_TAG);   //hämta och uppdatera blomlistan
                //new DownloadWebpageTask2().execute(DEBUG_SHOULD);   //hämta och uppdatera blomlistan

            } else {
                //inget internet,hämta inte filen
            }
        }
        //Detta är ett test som tillåter snabbare laddning av blommor.
        //resultat är bra och borde implementera detta någon gång framöver
        //tyvärr är detta  ett större arbete
        //CreateList();

    }

    private class SortByName implements Comparator<Flower> {
        @Override
        public int compare(Flower o1, Flower o2) {
            return o1.getName().toLowerCase().trim().compareTo(o2.getName().toLowerCase().trim());
        }
    }

    @Override
    public void onResume() {
        if (!settingsData.getBoolean("offlinemode", false))
            new DownloadWebpageTask().execute(DEBUG_TAG);
        super.onResume();
    }

    //dessa fyra metoderna nedan, startar en ny aktivitet, beronde på användarens val på huvudmenyn
    public void goBrowse(View view) {
        Intent i = new Intent(getApplicationContext(), Browse.class);
        /*
        boolean shoulda_update = false;

        SharedPreferences explanation = getSharedPreferences("explanation", 0);

        if (Integer.parseInt(shoulda[0].replace("\r", "")) > explanation.getInt("comment_update", 1) ) {
            shoulda_update = true;
            SharedPreferences.Editor editor = explanation.edit();
            editor.putInt("comment_update", Integer.parseInt(shoulda[0].replace("\r", "")));
            editor.apply();
        }
        */
            if (nyBlomma != null) {
                if (!settingsData.getBoolean("offlinemode", false))
            i.putExtra("update", nyBlomma); //skicka med de nya uppdaterade blommorna
            i.putExtra("favo", false);
                //i.putExtra("shoulda_update", shoulda_update);

            } else {
            i.putExtra("update", nodata); //skicka med de nya uppdaterade blommorna
            i.putExtra("favo", false);
                //i.putExtra("shoulda_update", shoulda_update);
        }
        startActivity(i);
    }

    public void goFavo(View view) {
        Intent i = new Intent(getApplicationContext(), Browse.class);
        if (nyBlomma != null) {
            if (!settingsData.getBoolean("offlinemode", false))
            i.putExtra("update", nyBlomma); //skicka med de nya uppdaterade blommorna
            i.putExtra("favo", true);
        } else {
            i.putExtra("update", nodata); //skicka med de nya uppdaterade blommorna
            i.putExtra("favo", true);
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

    /*
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == REQUEST_CODE) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // save file
                } else {
                    Toast.makeText(getApplicationContext(), "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
                }
            }
        }
    */

    //tryck två gånger bak för att avslsuta appen
   /*
    @Override
    public void onBackPressed() {
        if (back_pressed + TIME_DELAY > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(getBaseContext(), "Tryck en gång till för att avsluta",
                    Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }
*/
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
