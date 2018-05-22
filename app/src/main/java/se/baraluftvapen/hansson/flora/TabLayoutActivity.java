//Info är egentligen TabLayoutActivity eller tvärt om, med flik- och flikinnehållsLayout, samt knapptryckshantering
package se.baraluftvapen.hansson.flora;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TabLayoutActivity extends AppCompatActivity {
    private EditText ET_name;
    private EditText ET_email;
    private EditText ET_mess;
    private Context context;
    private String vote = "";				//Användarens röstning
    private String selectedFilePath;        //sökväg till textfilen som ska laddas upp
	private String ServerUrl;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_layout);
        ServerUrl = getString ( getResources().getIdentifier("url_up", "string", getPackageName()));
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        TabsPagerAdapter adapter = new TabsPagerAdapter(getSupportFragmentManager());	
        assert pager != null;
        pager.setAdapter(adapter);
        assert tabs != null;
        tabs.setupWithViewPager(pager);
    }
	
//-------------------------------------------------------------------
    //metoder som hanterar länkar vid tryck av källa
    public void kalla1_m (View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://plantcurator.com/?s=Franz"));
        startActivity(intent);
    }
    public void kalla2_m (View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://biolib.mpipz.mpg.de/library/authors/author_00241_de.html"));
        startActivity(intent);
    }
    public void kalla3_m (View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://runeberg.org/nordflor/"));
        startActivity(intent);
    }
    public void kalla4_m (View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://www.artdatabanken.se/"));
        startActivity(intent);
    }
    public void kalla5_m (View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://linnaeus.nrm.se/flora/welcome.html"));
        startActivity(intent);
    }
    public void kalla6_m (View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://biolib.mpipz.mpg.de/library/authors/author_00140_en.html"));
        startActivity(intent);
    }
    public void kalla13_m (View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("http://biolib.mpipz.mpg.de/library/authors/author_00146_de.html"));
        startActivity(intent);
    }
	
//-------------------------------------------------------------------
	//Oavsett var man trycker i namnfältet i kontakttabb, ska redigering av text alltid ske
    public void nameGetFocus (View view) {
        ET_name = (EditText) findViewById(R.id.editText_name);
        ET_name.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ET_name, InputMethodManager.SHOW_IMPLICIT);
    }

    public void emailGetFocus (View view) {
        ET_email = (EditText) findViewById(R.id.editText_email);
        ET_email.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ET_email, InputMethodManager.SHOW_IMPLICIT);
    }

    public void msgGetFocus (View view) {
        ET_mess = (EditText) findViewById(R.id.editText_mess);
        ET_mess.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ET_mess, InputMethodManager.SHOW_IMPLICIT);
    }
	
//-------------------------------------------------------------------
	//Hantering av röstningsval i röstningstabben
    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_category:
                if (checked)
                    vote = "category";
                    break;
            case R.id.radio_edit:
                if (checked)
                    vote = "edit";
                    break;
            case R.id.radio_filter:
                if (checked)
                    vote = "filter";
                    break;
            case R.id.radio_more:
                if (checked)
                    vote = "more";
                    break;
            case R.id.radio_other:
                if (checked)
                    vote = "other";
                    break;
            case R.id.radio_pics:
                if (checked)
                    vote = "pics";
                    break;
            case R.id.radio_quiz:
                if (checked)
                    vote = "quiz";
                    break;
        }
    }

//-------------------------------------------------------------------
    //skicka-knappen i kontakt-tabben
    public void vote_send (View view) {

        context = getApplicationContext();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("settings", 0);
            if (sharedPreferences.getBoolean("vote", true) && !vote.equals("")){
                StartUploadTextFile(vote, "poll_");											//lagrar valet i txt som skickas till server
                Toast.makeText(context, "Tack för din röst!", Toast.LENGTH_LONG).show();
                SharedPreferences.Editor editor = sharedPreferences.edit();					//lagrar lokalt att röstning har skett
                editor.putBoolean("vote", false);
                editor.apply();
            }
            else {
                if (vote.equals(""))
                    Toast.makeText(context, "Du måste välja ett alternativ", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(context, "Du har redan röstat!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Internet saknas!", Toast.LENGTH_SHORT).show();
        }
    }

//-------------------------------------------------------------------
    //skicka-knappen i kontakt-tabben
    public void kontakt_send (View view) {
        context = getApplicationContext();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            ET_name = (EditText) findViewById(R.id.editText_name);
            ET_email = (EditText) findViewById(R.id.editText_email);
            ET_mess = (EditText) findViewById(R.id.editText_mess);

            if (!(ET_name.getText().toString().equals("") || ET_mess.getText().toString().equals("")))		//namn och meddelande är obligatoriskt
			{	
				//starta uppladdning av text
                StartUploadTextFile(ET_name.getText().toString()+"||"+ET_email.getText().toString()+"||"+ET_mess.getText().toString(), "kontakt_");
                ET_name.setText("");
                ET_email.setText("");
                ET_mess.clearFocus();
                Toast.makeText(context, "Meddelande skickat", Toast.LENGTH_LONG).show();
                ET_mess.setText("");
				}
            else {
                if (ET_name.getText().toString().equals(""))
                    Toast.makeText(context, "Namn måste fyllas i", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(context, "Meddelande saknas", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, "Internet saknas!", Toast.LENGTH_SHORT).show();
        }
    }

//-------------------------------------------------------------------
	//förbereder sträng i textfil som kommer laddas upp
    public void StartUploadTextFile(String string, String type) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String filename = (type + timeStamp + ".txt");	//type är prefix, timestamp är för att skapa unikt namn på server
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        selectedFilePath = context.getFilesDir() + "/" + filename;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //creating new thread to handle Http Operations
                    uploadFile();
                } catch (OutOfMemoryError e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            }
        }).start();
    }

//-------------------------------------------------------------------
	//Hanterar tryckning när användaren vill "dela app"
    public void share(View view){
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "Svenska Växter");
            String sAux = "";
            sAux = sAux + "https://play.google.com/store/apps/details?id=se.baraluftvapen.hansson.flora\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, "Dela Svenska Växter"));
        } catch(Exception e) {
            //e.toString();
        }
    }

//-------------------------------------------------------------------
	//Laddar upp txt-fil på server
    public int uploadFile() {
        int serverResponseCode = 0;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024; //10kB
        File selectedFile = new File(selectedFilePath);
        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];
        if (!selectedFile.isFile()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Filen existerar inte!", Toast.LENGTH_SHORT).show();
                }
            });
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(ServerUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty(
                        "Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file", selectedFilePath);
                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());
                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);
                dataOutputStream.writeBytes(lineEnd);
                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];
                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0) {
                    try {
                        //write the bytes read from inputstream
                        dataOutputStream.write(buffer, 0, bufferSize);
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(context, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                    }
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }
                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                try {
                    serverResponseCode = connection.getResponseCode();
                } catch (OutOfMemoryError e) {
                    Toast.makeText(context, "Memory Insufficient!", Toast.LENGTH_SHORT).show();
                }
                String serverResponseMessage = connection.getResponseMessage();
                //Log.i(TAG, "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);
                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(context, "Meddelande skickat", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "File Not Found", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "URL Error!", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Ingen anslutning!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            context.deleteFile(fileName);		//delete temporära txtfil
			
            return serverResponseCode;
        }
    }
}
