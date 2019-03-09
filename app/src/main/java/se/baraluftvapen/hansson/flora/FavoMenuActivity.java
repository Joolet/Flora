/*
    Hanterar bläddrings-/favorit-vyn med presentation, filtrering, sortering och sökning av växter
*/
package se.baraluftvapen.hansson.flora;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Map;

import static se.baraluftvapen.hansson.flora.R.id.action_sort;
import static se.baraluftvapen.hansson.flora.R.id.all_color;
import static se.baraluftvapen.hansson.flora.R.id.main_filter;

public class FavoMenuActivity extends AppCompatActivity {

    private GridView gridView;
    private String[] nodata = new String[]{"nodata", "", "", ""};     //Tom sträng, när inget fanns att hämta
    private SharedPreferences settings;                                       //Hämtar data på valda inställningar och in/ut data från olika Activitys
    private int noFavoLists;
    private SharedPreferences favoInfo;
    private SharedPreferences favoData;
    private  Context context;

    private TextView     ET_des;
    private TextView     TV_private;
    private TextView     TV_privateHC;
    private LinearLayout lay_com2;
    private PopupWindow  pw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getWindow().setBackgroundDrawableResource(R.drawable.wood4);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        context = getApplicationContext();

        //Tar bort blommor i flowerlist, om vi är i favoritvyn

        favoInfo = getSharedPreferences("favoinfo", 0);
        favoData = getSharedPreferences("favoList", 0);
        settings = getSharedPreferences("settings", 0);

        noFavoLists = favoInfo.getInt("NoLists", 0);
        DisplayFavoList();


/*
Denna "metod" skapar en ny aktivitet (FlowerActivity) som visar mer info om blomman som trycktes på.
Går igenom blomlistan för att matcha den som trycktes på, sedan skickas all data om blomman vidare till den nya aktiviteten.
 */
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                if ( position == noFavoLists){
                    sendDataPopUp();
                } else {
                    Intent i = new Intent(getApplicationContext(), FlowerActivity.class);
                    i.putExtra("favo", true);
                    i.putExtra("update", nodata);
                    i.putExtra(v.findViewById(R.id.grid_item_label).toString(), "Skapa ny lista");

                    String FavoListName = "favoList" + position;
                    SharedPreferences favoList = getSharedPreferences(FavoListName, 0);
                    SharedPreferences.Editor ed = favoData.edit();
                    favoData = favoList; //The shared preferences to copy from
                    ed.clear();

                    for (Map.Entry<String, ?> entry : favoList.getAll().entrySet()) {
                        Object vobj = entry.getValue();
                        String key = entry.getKey();
                        if (vobj instanceof Boolean)
                            ed.putBoolean(key, ((Boolean) vobj).booleanValue());
                    }
                    ed.apply(); //save it.

                    startActivity(i);
                }
            }
        });
    }


    //-------------------------------------------------------------------------------------------------
//Metod som presenterar den listan över favoritlistor
//-------------------------------------------------------------------------------------------------
    private void DisplayFavoList() {

        String[] imageID = new String[noFavoLists + 1];
        String[] nameOfList = new String[noFavoLists + 1];

        //läser av alla namn och bildIDm som sedan behöves för att inflatea gridview

        for (int i = 0; i < noFavoLists; i++) {
            imageID[i] = "favo";
            nameOfList[i] = favoInfo.getString("favoName" + i, "Null");
        }

        imageID[noFavoLists] = "favo";
        nameOfList[noFavoLists] = "Skapa ny lista";

        //letar upp hur många kolumner som användaren har angett
        //"settings" är listan som nyckelorden finns. "0" är Private_mode(ej läsbar för andra)
        //"antalkolumner" är nycketordet och "3" är default value om nyckelordet inte finns
        int noKol = settings.getInt("antalkolumner", 4);

        gridView = (GridView) findViewById(R.id.gridView1);
        assert gridView != null;
        gridView.setNumColumns(noKol);
        gridView.setAdapter(new ImageAdapter(this, imageID, nameOfList));

    }

    //skicka textendatan som användaren har gjort
    public void sendDataPopUp() {
        //gemensam kod för alla tre alternativ
        try {
            // We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) FavoMenuActivity.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.pop_up_favolist,
                    (ViewGroup) findViewById(R.id.popup_favo));
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pw.showAtLocation(layout, Gravity.CENTER, 0, -145); //flyttar upp fönstret en aning så softkeyboard inte skymmer popup

            //GUI
            Button popUpCancel = (Button) layout.findViewById(R.id.close_popup_favo);
            popUpCancel.setOnClickListener(cancelButton);
            Button popUpSendData = (Button) layout.findViewById(R.id.ok_popup_favo);
            popUpSendData.setOnClickListener(okej_button);

            ET_des = (EditText) layout.findViewById(R.id.editText_favo);
            TextView popupTV = (TextView) layout.findViewById(R.id.popuptext_favo);
            String reportMessage = "Namn på den ny listan:";
            ET_des.setHint("");
            popupTV.setText(reportMessage);
            ET_des.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(ET_des, InputMethodManager.SHOW_IMPLICIT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //vad händer när man trycker utanför fönstert --> stäng fönster
        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                pw.dismiss();
            }
        });
    }
    //hanterar knappen "skicka" i popupfönstret
    private final View.OnClickListener okej_button = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
            String FavoListName = "favoName" + noFavoLists;
            if (!(ET_des.getText().toString()).trim().equals("")) {
                    SharedPreferences.Editor editor = favoInfo.edit();
                    editor.putString(FavoListName, ET_des.getText().toString());
                    noFavoLists = noFavoLists + 1;
                    editor.putInt("NoLists", noFavoLists);
                    editor.apply();
                    DisplayFavoList();
            } else {
                Toast.makeText(getApplicationContext(), "Nya listan måste ha ett namn!", Toast.LENGTH_LONG).show();
            }
        }
    };
    //om man trycker på AVBRYT
    private final View.OnClickListener cancelButton = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
        }
    };

    //visa tangentbord oavsett var man trycker i textfälteet
    public void msgGetFocusFavo(View view) {
        ET_des.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(ET_des, InputMethodManager.SHOW_IMPLICIT);
    }
}