/*
Presenterar en enskild växt med dess egenskaper och text
*/
package se.baraluftvapen.hansson.flora;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;

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
import java.util.LinkedList;
import java.util.List;

import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;

public class FlowerActivity extends AppCompatActivity {

    private int itemNumber;             //nuvarande växt som visas för användaren
    //Strängar med alla växter som är aktuella med dess egenskaper
    //antalet växter beror på användarens ev. filtrering i browseactiity 
    private String[] flowerID;      
    private String[] flowerName;
    private String[] flowerSpread;
    private String[] flowerBloom;
    private String[] flowerDes;
    private String[] flowerOtherName;
    private String[] flowerLength;
    private String[] flowerColor;
    private String[] flowerArea;
    private String[] flowerLatin;
    private String[] flowerEditname;   
    private String[] flowerCategory;
    private String[] flowerFamily;
    private String[] categorys = new String[3]; //rekomenderat antal max kategorier
    
    //online- och filhantering
    private String selectedFilePath;    //sökväg till txtfil på mobilen
    private String dataupload;          //sträng med vad som ska laddas upp till servern
	private String ServerUrl;
    private boolean uploaddatafile = false;

    //GUI
    private TextView TV_description;
    private TextView TV_blomning;
    private TextView TV_spread;
    private TextView TV_length;
    private TextView TV_names;
    private TextView TV_latin;
    private TextView TV_family;
    private TextView TV_category1;
    private TextView TV_category2;
    private TextView TV_category3;
    private TextView TV_googleLink;
    private ImageView imageView;
    private EditText ET_des;
    private EditText ET_com;
    private TextView TV_com;
    private CheckBox chk_name;
    private LinearLayout lay_com;
    private LinearLayout lay_com2;
    private TextView TV_private;
    private Spinner spinner_petal;
    
    //blandad kompott
    private FabSpeedDial fabSpeedDial1;       //redigera-meny
    private PopupWindow pw;                   //popupp-fönster
    private String editActiveType = "default";
    private SharedPreferences settings;
    private GoogleApiClient mGoogleApiClient; //inloggad
    private Context context;
    private boolean editActive = false;       //om redigering är aktiv
    private Menu menu;
    private String no_petals;                 //antal kronblad en växt har

    //Zoom av bilder
    private Animator mCurrentAnimator;
    private float x1, x2;
    private int mShortAnimationDuration;
    private static final int MIN_DISTANCE = 200;  //hur långt ska man swipa till nästa bild
    private View thumbView;                       //vy för den normala bilden
    private ImageView expandedImageView;          //vy för inzoomade bilder
    private boolean zoomActive = false;           //kontrollerar om inzoomat-läge är aktiverat

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();
        settings = getSharedPreferences("settings", 0);
        ServerUrl = getString ( getResources().getIdentifier("url_up", "string", getPackageName()));

        //GUI
        TV_description = (TextView) findViewById(R.id.textView_description);
        TV_blomning = (TextView) findViewById(R.id.textView_blomning);
        TV_spread = (TextView) findViewById(R.id.textView_spread);
        TV_length = (TextView) findViewById(R.id.textView_length);
        TV_family = (TextView) findViewById(R.id.textView_family);
        TV_googleLink = (TextView) findViewById(R.id.google_link);
        TV_names = (TextView) findViewById(R.id.textView_names);
        TV_latin = (TextView) findViewById(R.id.textView_latin);
        TV_category1 = (TextView) findViewById(R.id.category1);
        TV_category2 = (TextView) findViewById(R.id.category2);
        TV_category3 = (TextView) findViewById(R.id.category3);
        TV_private = (TextView) findViewById(R.id.text_private);
        spinner_petal = (Spinner) findViewById(R.id.spinner_petal);

        //Googlekonto
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //hämtar de blommor som är medskickade från browseactivity
        Intent i = getIntent();
        flowerName = i.getExtras().getStringArray("name");
        flowerSpread = i.getExtras().getStringArray("spread");
        flowerBloom = i.getExtras().getStringArray("bloom");
        flowerDes = i.getExtras().getStringArray("description");
        itemNumber = i.getExtras().getInt("item");
        flowerID = i.getExtras().getStringArray("id");
        flowerLength = i.getExtras().getStringArray("length");
        flowerFamily = i.getExtras().getStringArray("family");
        flowerOtherName = i.getExtras().getStringArray("otherName");
        flowerCategory = i.getExtras().getStringArray("category");
        flowerArea = i.getExtras().getStringArray("area");
        flowerLatin = i.getExtras().getStringArray("latin");
        flowerEditname = i.getExtras().getStringArray("edited");
        flowerColor = i.getExtras().getStringArray("color");

        //init till "redigera antal kronblad"
        List<String> petal_list = new LinkedList<>();
        petal_list.add("Lägg till (inaktiverad tillsvidare)");
        petal_list.add("3 st eller färre");
        petal_list.add("4 st");
        petal_list.add("5 st");
        petal_list.add("6 st eller fler");
        petal_list.add("Varierar");
        petal_list.add("Asymmetrisk/annat");
        petal_list.add("Saknar/syns ej");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, petal_list);
        assert spinner_petal != null;
        spinner_petal.setAdapter(adapter);
        spinner_petal.setEnabled(false);

        //presentera växen på skärmen
        DisplayFlower(); 
        
        //animeringstid vid inzooming av bild
        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        
        //----------------------------------------------------------------------------------------------------
        // den flytande menyn nere i hörnet
        fabSpeedDial1 = (FabSpeedDial) findViewById(R.id.fab_speed_dial);
        //meny alternativ för FAB-menyn
        fabSpeedDial1.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    //Kör "redigera beskrvning" i popuppfönster (kräver inloggning av googlekontot)
                    case R.id.action_text:
                        if (settings.getString("inloggad", "no").equals("no")) {
                            editActiveType = "text";
                            login_confirm();
                        } else {
                            editActiveType = "text";
                            runText();
                        }
                        return false;
                        
                    //privat kommentar ---> öpnna nytt fönster där användaren kan läggatill/redigera sin erfarenhet/kommentar
                    case R.id.action_report:
                        editActive = true;
                        fabSpeedDial1.setVisibility(View.INVISIBLE);
                        editActiveType = "report";
                        sendDataPopUp("Privat kommentar");  //sträng som visas överst i popupp fönstret
                        return false;
                        
                    //för att rappotera fel om växtens egenskaper/beskrivning
                    case R.id.action_report2:
                        ConnectivityManager connMgr = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnected()) {
                            //byter FAB till den andra och byter till "EditText"
                            editActive = true;
                            fabSpeedDial1.setVisibility(View.INVISIBLE);
                            editActiveType = "report2";
                            sendDataPopUp("Rapportera fel");
                        } else {
                            Toast.makeText(context, "Ingen anslutning!", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    default:
                        return false;
                }
            }
        });
    }

    //------------------------------------------------------------------------------------------------------
    //avsluta floweractivity och visa alla i familjen, dvs när användaren trycker på växtens familj
    public void viewFamily(View view) {
        Intent intent = new Intent();
        intent.putExtra("viewFamily", flowerFamily[itemNumber]);
        intent.putExtra("toViewFamily", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    //------------------------------------------------------------------------------------------------------
    //när användaren redigerar antal kronblad
    private void setupSpinners() {
        spinner_petal.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                Object item = arg0.getItemAtPosition(arg2);
                if (item != null) {
                    String val = item.toString();
                    if (val.contains("3") && !no_petals.equals("3"))
                        uploadData("3");
                    else if (val.contains("4")&& !no_petals.equals("4"))
                        uploadData("4");
                    else if (val.contains("5")&& !no_petals.equals("5"))
                        uploadData("5");
                    else if (val.contains("6")&& !no_petals.equals("6"))
                        uploadData("6");
                    else if (val.equals("Asymmetrisk/annat")&& !no_petals.equals("9"))
                        uploadData("9");
                    else if (val.equals("Varierar")&& !no_petals.equals("1"))
                        uploadData("1");
                    else if (val.equals("Saknar/syns ej")&& !no_petals.equals("0"))
                        uploadData("0");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }
    //-----------------------------------------------------------------------------------------
    //öppna webläsare för att visa bildresultat
    public void googleLink(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse("https://www.google.nl/search?tbm=isch&q=" + flowerName[itemNumber]));
        startActivity(intent);
    }
    
    //-----------------------------------------------------------------------------------------
    //Initiering av att visa popup för text redigering
    public void runText() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //byter FAB till den andra och byter till "EditText"
            editActive = true;
            fabSpeedDial1.setVisibility(View.INVISIBLE);
            sendDataPopUp("Redigera text");
        } else {
            Toast.makeText(context, "Ingen anslutning!", Toast.LENGTH_SHORT).show();
        }
    }
    
    //-----------------------------------------------------------------------------------------
    //om man trycker på "OK", när användaren redigerar beskrivning. jepp förvirrande :O
    private final View.OnClickListener ccanel_button = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
            if (settings.getString("inloggad", "no").equals("no")) {
                login_confirm();
            }
            else if (editActiveType.equals("text")) runText();
        }
    };
    
    //------------------------------------------------------------------------------------------------------------------------------
    //hanterar popupfönstert vid tryck av "redigera beskrivning", "privat kommentar" eller "rapporttera fel"
    //skicka textendatan som användaren har gjort
    public void sendDataPopUp(String title) {
        //gemensam kod för alla tre alternativ
        try {
            // We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) FlowerActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.pop_up_send_data,
                    (ViewGroup) findViewById(R.id.popup_1));
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pw.showAtLocation(layout, Gravity.CENTER, 0, -145); //flyttar upp fönstret en aning så softkeyboard inte skymmer popup

            //GUI
            Button popUpCancel = (Button) layout.findViewById(R.id.close_popup);
            popUpCancel.setOnClickListener(cancelButton);
            PopupWindow.OnDismissListener onDismissListener;
            Button popUpSendData = (Button) layout.findViewById(R.id.ok_popup);
            popUpSendData.setOnClickListener(send_data_button);
            TextView popupTV = (TextView) layout.findViewById(R.id.popuptext);
            ImageView popupIV = (ImageView) layout.findViewById(R.id.topicon);
            TextView popuptitle = (TextView) layout.findViewById(R.id.popuptitle);
            ET_des = (EditText) layout.findViewById(R.id.editText_description);
            ET_com = (EditText) layout.findViewById(R.id.editText_commentary);
            chk_name = (CheckBox) layout.findViewById(R.id.checkBox_showname);
            lay_com = (LinearLayout) layout.findViewById(R.id.lay_com);
            lay_com2 = (LinearLayout) layout.findViewById(R.id.lay_com2);
            TV_com = (TextView) layout.findViewById(R.id.popuptext_commentary);
            String editTextMessage = "Detta vertyg låter dig redigera växtens beskrivning. Texten som skickas in kommer att granskas och läggas upp i databasen inom en vecka";
            popupTV.setText(editTextMessage);
            popuptitle.setText(title + ", " + flowerName[itemNumber]);
            fabSpeedDial1.setVisibility(View.INVISIBLE);

            //specifik kod för alternativet redigera beskrivning
            if (editActiveType.equals("text")) {
                ET_des.setText(TV_description.getText());
                if (TV_description.getText().equals("Beskrivning saknas"))
                    TV_description.setHint("Bekrivning saknas....");
                popupIV.setImageResource(R.drawable.ic_edit_white_24dp);
                ET_com.setVisibility(View.VISIBLE);
                chk_name.setVisibility(View.VISIBLE);
                lay_com.setVisibility(View.VISIBLE);
                lay_com2.setVisibility(View.VISIBLE);
                TV_com.setVisibility(View.VISIBLE);
                String[] chkname_text = settings.getString("inloggad", "ej inloggad").split(" ");
                chk_name.setText("Visa mitt förnamn (" + chkname_text[0] + ") under växtens beskrivning");

                //rapportera fel popup
            } else if (editActiveType.equals("report2")) {
                String reportMessage = "Här kan du rapportera fel om växtens information. Korrigeringar publiceras vanligen inom en vecka.";
                popupTV.setText(reportMessage);
                popupIV.setImageResource(R.drawable.ic_report_problem_white_24dp);
                
                //privatkommentar popup
            } else {
                String reportMessage = "Här kan du skriva en privat kommentar/erfarenhet om växten.";
                ET_des.setHint("Kommentar...");
                popUpSendData.setText("Spara"); //byter namn på skickaknappen till spara
                popupTV.setText(reportMessage);
                ET_des.setText(TV_private.getText());
                popupIV.setImageResource(R.drawable.ic_import_contacts_white_24dp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //vad händer när man trycker utanför fönstert --> stäng fönster
        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                pw.dismiss();
                editActive = false;
                fabSpeedDial1.setVisibility(View.VISIBLE);
            }
        });
    }

    //hanterar knappen "skicka" i popupfönstret
    private final View.OnClickListener send_data_button = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
            editActive = false;
            fabSpeedDial1.setVisibility(View.VISIBLE);
            if (editActiveType.equals("text")) {
                if ((!TV_description.getText().toString().equals(ET_des.getText().toString())) || !(ET_com.getText().toString()).trim().equals("")) {
                    if (!settings.getString("inloggad", "no").equals("no")) {
                        StartUploadTextFile();
                        mfab(v);
                    }
                }
            } else if (editActiveType.equals("report2")) {
                if (!ET_des.getText().toString().trim().equals("")) {
                    StartUploadTextFile();
                    mfab(v);
                }

            } else {
                if (!(ET_des.getText().toString()).trim().equals("")) {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString(flowerID[itemNumber], ET_des.getText().toString());
                    editor.apply();
                    TV_private.setText(ET_des.getText().toString());
                }
            }
        }
    };
    //om man trycker på AVBRYT
    private final View.OnClickListener cancelButton = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
            editActive = false;
            fabSpeedDial1.setVisibility(View.VISIBLE);
        }
    };

    /*
        //försök till att kartan skulle visas i en popup istället för i en ny aktivitet??
        public void Popup_map(View view) {
            try {
                // We need to get the instance of the LayoutInflater
                LayoutInflater inflater = (LayoutInflater) FlowerActivity.this
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.pop_up_map,
                        (ViewGroup) findViewById(R.id.popup_1));
                pw = new PopupWindow(layout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    pw.setElevation(10);
                }
                fabSpeedDial1.setVisibility(View.INVISIBLE);
                ImageView popupimage = (ImageView) layout.findViewById(R.id.outline);
                popupimage.setOnClickListener(ExitMap);
            } catch (Exception e) {
                e.printStackTrace();
            }

            pw.setOnDismissListener(new PopupWindow.OnDismissListener() {

                @Override
                public void onDismiss() {
                    pw.dismiss();
                    editActive = false;
                    fabSpeedDial1.setVisibility(View.VISIBLE);
                }
            });
        }

        private final View.OnClickListener ExitMap = new View.OnClickListener() {
            public void onClick(View v) {
                pw.dismiss();
                editActive = false;
                fabSpeedDial1.setVisibility(View.VISIBLE);
            }
        };
    */

    /*
     skapar meny raden där uppe
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.submenu, menu);
        this.menu = menu;
        setFavoIcon(); //kollar om blomman är en favorit och skapar iconen
        return super.onCreateOptionsMenu(menu);
    }

    //de tre metoderna nedan hanterar vad som händer när användaren trycker på en kategori
    //categoty har valts, avsluta --> visa alla blommor som till category
    public void category1(View view) {
        Intent intent = new Intent();
        intent.putExtra("viewFamily", categorys[0].trim());
        intent.putExtra("toViewFamily", false);
        setResult(RESULT_OK, intent);
        finish();
    }

    //categoty2 har valts, avsluta --> visa alla blommor som till category
    public void category2(View view) {
        Intent intent = new Intent();
        intent.putExtra("viewFamily", categorys[1].trim());
        intent.putExtra("toViewFamily", false);
        setResult(RESULT_OK, intent);
        finish();
    }

    //Skicka vidare vald kategori till Browse --> Browse filtrerar och presenterar växterna
    public void category3(View view) {
        Intent intent = new Intent();
        intent.putExtra("viewFamily", categorys[2].trim());
        intent.putExtra("toViewFamily", false);
        setResult(RESULT_OK, intent);
        finish();
    }

    //hanterar init av uppladning av onlinefilen
    private void uploadData(String spinner_value) {

        uploaddatafile = true;
        SharedPreferences flowerdata = getSharedPreferences("flowerdata", 0);
        int nyBlomma_length = flowerdata.getInt("nyBlomma_length", 0);
        dataupload = "dummy";
        boolean foundit = false;
        dataupload = dataupload + "\r\n";   //första raden är alltid en dummy
        int j = 1;
        for (int k = 1; k < nyBlomma_length; k++, j++) {

            String flowerdata_string = flowerdata.getString(Integer.toString(k), "no");    //hämtar vad som finns i minnet
            String flowerdata_element[] = flowerdata_string.split("\t");

            if (flowerID[itemNumber].equals(flowerdata_element[1])) {
                String dataupload_element[];
                foundit = true;
                flowerdata_element[12] = spinner_value;       //vad ska ersättas???

                StringBuilder builder = new StringBuilder();
                for (String s : flowerdata_element) {
                    builder.append(s);
                    builder.append("\t");
                }
                builder.setLength(builder.length() - 1);
                String str = builder.toString();

                SharedPreferences.Editor editor = flowerdata.edit();
                editor.putString(Integer.toString(k), str);
                editor.putBoolean("foundit", true);
                editor.apply();

                dataupload = dataupload + str + "\r\n";
                dataupload = dataupload.trim();

            } else {
                dataupload = dataupload.trim();
                dataupload = dataupload + "\r\n";
                dataupload = dataupload + flowerdata.getString(Integer.toString(k), "no") + "\r\n";
            }
        }
        //för "antal kronblad", då växten inte finns innan i updatefilen
        if (!foundit || j > nyBlomma_length) {
            foundit = false;
            dataupload = dataupload.trim();
            dataupload = dataupload + "\r\n";

            if (!spinner_value.equals("0") && !spinner_value.equals("3") &&!spinner_value.equals("4") &&!spinner_value.equals("5") &&!spinner_value.equals("6") &&!spinner_value.equals("9") &&!spinner_value.equals("1")){
                dataupload = "hej_fel";
            }

            String str = "k" + "\t" + flowerID[itemNumber] + "\t" + "k" + "\t" + "k" + "\t" + "k" +
                    "\t" + "k" + "\t" + "k" + "\t" + "k" + "\t" + "k" + "\t" + "k" + "\t" + "k" + "\t" + "k" + "\t" + spinner_value;
            dataupload = dataupload + str;
            SharedPreferences.Editor editor = flowerdata.edit();
            editor.putString(Integer.toString(j), str);
            editor.putInt("nyBlomma_length", (flowerdata.getInt("nyBlomma_length", 0) + 1));
            editor.apply();
        }
        dataupload = dataupload.trim();
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            StartUploadTextFile();
        }
    }

    //actionbar menyval
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                SharedPreferences settings = getSharedPreferences("favolist", 0);
                boolean isFavorite = settings.getBoolean(flowerID[itemNumber], false);
                if (isFavorite) {
                    Toast.makeText(getApplicationContext(), "Borttagen från favoriter", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(flowerID[itemNumber], false);
                    editor.apply();
                    menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_favorite_border_white_24dp);
                } else {
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(flowerID[itemNumber], true);
                    editor.apply();
                    Toast.makeText(getApplicationContext(), "Tillagd i favoriter", Toast.LENGTH_SHORT).show();
                    menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_favorite_white_24dp);
                }
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    //hanterar tryckningen på defaultbilden --> kommer att zoomas in på bilden
    public void displayImage(View view) {
        int imageRef = getResources().getIdentifier(flowerID[itemNumber], "drawable", getPackageName());
        zoomImageFromThumb(view, imageRef);
    }

    //Hanterar knapptryckning av kartan --> öppna karta_activity
    public void map(View view) {
        Intent i = new Intent(getApplicationContext(), MapActivity.class);
        i.putExtra("maparea", flowerArea[itemNumber]); //skicka med växtens utbredning
        i.putExtra("flowername", flowerName[itemNumber]); //skicka med växtens utbredning
        startActivity(i);
    }

    /*
    Metod som hanterar swipning i normalläge (ej inzoomat läge)
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = event.getX();
                float deltaX = x2 - x1;
                //kontrollerar om redigering är aktiv, så man inte kan swipe när man gör det
                if (!editActive) {
                    //föregående blomma swipning. Startar om från slutet...
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        if (x2 > x1) {
                            if (itemNumber <= 0) {
                                itemNumber = flowerName.length;
                            }
                            itemNumber--;
                            DisplayFlower();
                            setFavoIcon();

                        }
                        //nästa blomma
                        else {
                            if (itemNumber >= (flowerName.length - 1)) {
                                itemNumber = -1;
                            }
                            itemNumber++;
                            DisplayFlower();
                            setFavoIcon();
                        }
                    } else {
                        // consider as something else - a screen tap for example
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    //Metoden som presenterar blomman i vyn
    public void DisplayFlower() {

        //brädgård i texten --> gör enter i texten
        if (flowerDes[itemNumber].contains("#")) {
            String flower_desster = flowerDes[itemNumber].replace('#', '\n');
            TV_description.setText(flower_desster);
        } else
            TV_description.setText(flowerDes[itemNumber]);

        TV_blomning.setText(flowerBloom[itemNumber]);
        TV_spread.setText(flowerSpread[itemNumber]);
        TV_spread.setPaintFlags(TV_spread.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TV_googleLink.setText("Länk till Google bilder");
        TV_googleLink.setPaintFlags(TV_googleLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        if (width < 720) {
            int text_size = 13;
            int offset_pad = 5;
            int offset_pad1 = 10;
            TV_blomning.setTextSize(text_size);
            TV_spread.setTextSize(text_size);
            TV_family.setTextSize(text_size);
            TV_length.setTextSize(text_size);
            TV_names.setTextSize(text_size);
            TV_latin.setTextSize(text_size);
            TV_private.setTextSize(text_size);
            TV_category1.setTextSize(text_size);
            TV_category2.setTextSize(text_size);
            TV_category3.setTextSize(text_size);
            TV_googleLink.setTextSize(text_size);
            TV_description.setTextSize(text_size);
            TextView hc = (TextView) findViewById(R.id.hardcore_also);
            hc.setTextSize(text_size);
            //TV_hardcore_editby.setTextSize(text_size);
            TextView hc1 = (TextView) findViewById(R.id.hardcore_blom);
            hc1.setTextSize(text_size);
            hc1.setPaddingRelative(offset_pad1, offset_pad, 0, 0);
            TextView hc2 = (TextView) findViewById(R.id.hardcore_family);
            hc2.setTextSize(text_size);
            hc2.setPaddingRelative(offset_pad1, offset_pad, 0, 0);
            TextView hc3 = (TextView) findViewById(R.id.hardcore_length);
            hc3.setTextSize(text_size);
            hc3.setPaddingRelative(offset_pad1, offset_pad, 0, 0);
            TextView hc4 = (TextView) findViewById(R.id.hardcore_names);
            hc4.setTextSize(text_size);
            hc4.setPaddingRelative(offset_pad1, offset_pad, 0, 0);
            TextView hc5 = (TextView) findViewById(R.id.hardcore_spread);
            hc5.setTextSize(text_size);
            hc5.setPaddingRelative(offset_pad1, offset_pad, 0, 0);
        }

         no_petals = flowerEditname[itemNumber].trim();
        if (!no_petals.equals("no")) {
            if (no_petals.equals("3"))
                spinner_petal.setSelection(1);
            else if (no_petals.equals("4"))
                spinner_petal.setSelection(2);
            else if (no_petals.equals("5"))
                spinner_petal.setSelection(3);
            else if (no_petals.equals("6"))
                spinner_petal.setSelection(4);
            else if (no_petals.equals("9"))
                spinner_petal.setSelection(6);
            else if (no_petals.equals("1"))
                spinner_petal.setSelection(5);
            else if (no_petals.equals("0"))
                spinner_petal.setSelection(7);
        } else
            spinner_petal.setSelection(0);


        //om category innehåller ALLA kategorier
        if (flowerCategory[itemNumber].contains(",")) {
            categorys = flowerCategory[itemNumber].split(", ");
            TV_category1.setVisibility(View.VISIBLE);
            TV_category2.setVisibility(View.VISIBLE);
            TV_category1.setText(categorys[0].trim());
            TV_category2.setText(categorys[1].trim());
            TV_category1.setPaintFlags(TV_category1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            TV_category2.setPaintFlags(TV_category2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            if (categorys.length >= 3) {
                TV_category3.setVisibility(View.VISIBLE);
                TV_category3.setText(categorys[2].trim());
                TV_category3.setPaintFlags(TV_category3.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            } else {
                TV_category3.setVisibility(View.GONE);
            }
        }
        //är category tom --> göm länkarna
        else if (flowerCategory[itemNumber].equals("")) {
            TV_category1.setVisibility(View.INVISIBLE);
            TV_category2.setVisibility(View.INVISIBLE);
            TV_category3.setVisibility(View.GONE);
        }
        //är den en kategori --> visa en länk och göm en länk
        else {
            categorys[0] = flowerCategory[itemNumber];
            TV_category1.setVisibility(View.VISIBLE);
            TV_category1.setText(categorys[0].trim());
            TV_category1.setPaintFlags(TV_category1.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            TV_category2.setVisibility(View.INVISIBLE);
            TV_category3.setVisibility(View.GONE);
        }

        //är den äver 200cm ska det stå meter istället
        int flowerheight = Integer.parseInt(flowerLength[itemNumber]);
        if (flowerheight < 200)
            TV_length.setText("" + flowerheight + " cm");
        else
            TV_length.setText("" + flowerheight / 100 + " m");

        TV_family.setText(flowerFamily[itemNumber]);
        TV_latin.setText(flowerLatin[itemNumber]);
        TV_family.setPaintFlags(TV_family.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        setTitle(flowerName[itemNumber]);
        //letar upp bilden som heter "Zxx_t"
/*
        if(imageView != null) {
            ((BitmapDrawable)imageView.getDrawable()).getBitmap().recycle();
        }*/
        imageView = (ImageView) findViewById(R.id.full_size_image);
        int imageRef = getResources().getIdentifier(flowerID[itemNumber], "drawable", getPackageName());
        imageView.setImageResource(imageRef);

        TV_private.setText(settings.getString(flowerID[itemNumber], "")); //hämtar kommentar av växt i minnet

        //fixar så det blir korrekt med kommatecknet vid flera olika namn på en blomma
        if (flowerOtherName[itemNumber].equals(""))
            TV_names.setText(flowerName[itemNumber]);
        else
            TV_names.setText(flowerName[itemNumber] + ", " + flowerOtherName[itemNumber]);
    }

    //försök av att spara minnne
    @Override
    public void onDestroy() {
        super.onDestroy();
        imageView.setImageDrawable(null);
    }

    public void onPause() {
        super.onPause();
		//göm tangenbord
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    /*
    Hämtar om blomman är en favorit och byter ut symbolen
     */
    public void setFavoIcon() {
        SharedPreferences settings = getSharedPreferences("favolist", 0);
        boolean isFavorite = settings.getBoolean(flowerID[itemNumber], false);
        if (isFavorite) {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_favorite_white_24dp);
        } else {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_favorite_border_white_24dp);
        }
    }

    /*
    Detta är vad som sker när man är färdig med redigeringen
     */
    public void mfab(View view) {
        if ((!TV_description.getText().toString().equals(ET_des.getText().toString())) || !(ET_com.getText().toString()).trim().equals("")) {
            //En pop-up ruta skapas
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
            dlgAlert.setIcon(R.drawable.ic_cloud_done_black_24dp);
            if (editActiveType.equals("text")) {
                dlgAlert.setMessage("Tack för du bidrar till en bättre växtdatabas!\nTexten kommer granskas och kommer läggas upp inom en vecka");
                dlgAlert.setTitle("Redigera Text");
            } else {

                dlgAlert.setMessage("Tack för ditt bidrag!\nFelet kommer att åtgärdas och kommer läggas upp inom en vecka");
                dlgAlert.setTitle("Rapportera fel");
            }

            //om OK, byt FAB, byt till Textview och ladda upp text
            dlgAlert.setNegativeButton("Okej",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //Gör nada, gå tillbaka bara
                        }
                    });
            dlgAlert.setCancelable(true);
            dlgAlert.create().show();
        }
    }
	//visa tangentbord oavsett var man trycker i textfälteet
    public void msgGetFocus(View view) {
        ET_des.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ET_des, InputMethodManager.SHOW_IMPLICIT);
    }

    //visa login-popup ruta, om användaren inte är inloggad
    public void login_confirm() {
        //En pop-up ruta skapas
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("För att redigera växtens beskrivning behöver du logga in på Google");
        dlgAlert.setTitle("Redigera text");
        dlgAlert.setIcon(R.drawable.ic_report_problem_black_24dp);
        //om OK, byt FAB, byt till Textview och ladda upp text

        dlgAlert.setNegativeButton("Avbryt",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //Gör nada, gå tillbaka bara
                    }
                });

        dlgAlert.setPositiveButton("Logga in",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ConnectivityManager connMgr = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                        if (networkInfo != null && networkInfo.isConnected()) {
                            signIn();
                            if (!settings.getString("inloggad", "no").equals("no")) {
                                if (editActiveType.equals("camera")) {
                                    //runCamera();
                                } else if (editActiveType.equals("text")) {
                                    runText();
                                }
                            }
                        }
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    //hanterar om inloggning lyckades
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == 9001) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    //Lyckad inloggning --> spara användarens data i sharedprtef
    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("inloggad", acct.getDisplayName());
            editor.apply();
        } else {
            // Signed out
        }
    }

    public void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 9001);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        setupSpinners();
    }

    /*
    Vad händer när bakåtknappen trycks
    Håller man på att redigerar skickas man till "färdig med redigering"-menyn
    annars avsluta aktivitet.
     */
    @Override
    public void onBackPressed() {
        if (zoomActive) {
            thumbView.setAlpha(1f);
            expandedImageView.setVisibility(View.GONE);
            mCurrentAnimator = null;
            fabSpeedDial1.setVisibility(View.VISIBLE);
            zoomActive = false;
        } else if (editActive) {
            pw.dismiss();
            editActive = false;
            fabSpeedDial1.setVisibility(View.VISIBLE);
        } else finish();
    }

    /*
    Initerar uppladning av textfilen
    Snarare skapar textfilen och sedan kör metoden uppladdning
     */
    public void StartUploadTextFile() {
        String string;
        String filename;
        //ladda upp för växtuppdatering
        if (uploaddatafile) {
            uploaddatafile = false;
            filename = "blom_update21.txt";
            string = dataupload.trim();
            //ladda upp för rappoertering för växtfel
        } else {
            String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            filename = ("" + flowerID[itemNumber] + "_" + timeStamp + ".txt");
            string = "blomma: " + flowerName[itemNumber] + "--> meddelande --> " + ET_des.getText().toString() + "--> kommentar --> " + ET_com.getText().toString() + "--> inloggad? -->" +
                    settings.getString("inloggad", "ej inloggad") + "--> visa namn --> " + chk_name.isChecked();
        }
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
                    uploadFile(selectedFilePath);
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

    /*
        laddar upp fil till servern
        tar bort den nyligen skapade lokala filen efter uppladdning
         */
    public int uploadFile(String selectedFilePath) {
        int serverResponseCode = 0;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 2048;                    //maxstorlek på fil = 20kB ~~ 500 rader med (ID & "k")
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
                if (serverResponseCode == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(context, "Uppladdning klar!", Toast.LENGTH_LONG).show();
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
            //context.deleteFile(fileName);
            return serverResponseCode;
        }
    }

    //hanterar inzooming av bilden
    private void zoomImageFromThumb(final View thumbView1, int imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        thumbView = thumbView1;
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.
        expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        assert expandedImageView != null;
        expandedImageView.setImageResource(imageResId);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.expanded_image)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();

        mCurrentAnimator = set;
        zoomActive = true;
        fabSpeedDial1.setVisibility(View.INVISIBLE);
        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;

        expandedImageView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        //kontrollerar om redigering är aktiv, så man inte kan swipe när man gör det
                        if (!editActive) {
                            //föregående blomma swipning. Startar om från slutet...
                            if (Math.abs(deltaX) > MIN_DISTANCE) {
                                if (x2 > x1) {
                                    if (itemNumber <= 0) {
                                        itemNumber = flowerName.length;
                                    }
                                    itemNumber--;
                                    DisplayFlower();
                                    setFavoIcon();
                                    int imageRef = getResources().getIdentifier(flowerID[itemNumber], "drawable", getPackageName());
                                    setExpandedImage(imageRef);
                                }
                                //nästa blomma
                                else {
                                    if (itemNumber >= (flowerName.length - 1)) {
                                        itemNumber = -1;
                                    }
                                    itemNumber++;
                                    DisplayFlower();
                                    setFavoIcon();
                                    int imageRef = getResources().getIdentifier(flowerID[itemNumber], "drawable", getPackageName());
                                    setExpandedImage(imageRef);
                                }
                            } else {
                                thumbView.setAlpha(1f);
                                expandedImageView.setVisibility(View.GONE);
                                mCurrentAnimator = null;
                                fabSpeedDial1.setVisibility(View.VISIBLE);
                                zoomActive = false;
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void setExpandedImage(int imageResId) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        // Load the high-resolution "zoomed-in" image.
        expandedImageView = (ImageView) findViewById(
                R.id.expanded_image);
        assert expandedImageView != null;
        expandedImageView.setImageResource(imageResId);
        expandedImageView.setVisibility(View.VISIBLE);
        zoomActive = true;
    }
}