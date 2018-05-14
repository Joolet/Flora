package se.baraluftvapen.hansson.flora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static se.baraluftvapen.hansson.flora.R.array.region_array;

public class QuizMenu extends AppCompatActivity {

    private String category_val = "";
    private String language = "";
    private LinkedList<Flower> flowerList;
    private HashMap<String, Integer> nr_fam = new HashMap<String, Integer>();
    private HashMap<String, Integer> nr_cat = new HashMap<String, Integer>();
    List<String> familys = new LinkedList<>();
    List<String> superfamilys = new LinkedList<>();
    List<String> trashfamily = new LinkedList<>();
    List<String> categorys = new ArrayList<String>();
    List<String> all_no_flowers = new ArrayList<String>();
    private String selected_region;
    private Spinner spinner_cat;
    private Spinner spinner_fam;
    private Spinner spinner_all;
    private Spinner spinner_region;
    private RadioButton radiofam;
    private RadioButton radiocat;
    private RadioButton radioall;
    private RadioButton radiofav;
    private RadioButton radioland;
    private TextView favor;
    private TextView landor;
    int no_favos = 0;
    private String type = "list";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_menu);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        radiocat = (RadioButton) findViewById(R.id.radio_cat);
        radiofam = (RadioButton) findViewById(R.id.radio_fam);
        radiofav = (RadioButton) findViewById(R.id.radio_fav);
        radioland = (RadioButton) findViewById(R.id.radio_land);
        radioall = (RadioButton) findViewById(R.id.radio_all);
        favor = (TextView) findViewById(R.id.textview_favo);
        landor = (TextView) findViewById(R.id.textview_land);

        CreateList();
        SetUpLists();

        spinner_cat = (Spinner) findViewById(R.id.spinner_cat);
        spinner_fam = (Spinner) findViewById(R.id.spinner_fam);
        spinner_all = (Spinner) findViewById(R.id.spinner_all);
        spinner_region = (Spinner) findViewById(R.id.spinner_region);
        spinner_fam.setEnabled(false);
        spinner_fam.setClickable(false);
        spinner_all.setEnabled(false);
        spinner_all.setClickable(false);

        setupSpinners();

    }

    //när man trycker på starta quiz --> startar nästa aktivitet
    public void goNext(View view) {
        if (type.equals("list")) {
            if (category_val.equals("Blanda alla, välj antal:") || category_val.equals("Välj familj:") || category_val.equals("Välj kategori:") || category_val.equals("favo_err")) {
                if (category_val.equals("favo_err"))
                    Toast.makeText(getApplicationContext(), "Du måste ha minst fyra växter i favoriter för att starta", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Välj vad du vill träna på", Toast.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent(getApplicationContext(), QuizActivity.class);

                if (radiocat.isChecked() || radiofam.isChecked()) {
                    //är det familjen som ska visas behövs det ta bort det sista i strängen som visar hur många blommor det finns
                    i.putExtra("choice", category_val.substring(0, category_val.length() - 4).trim());
                }
                //skickar vidare bara talet
                else if (radioall.isChecked()) {
                    i.putExtra("choice", category_val.substring(0, category_val.length() - 2).trim());
                } else
                    i.putExtra("choice", category_val);

                i.putExtra("id", trashfamily.toString()); //skickar alltid in trashfamily, behövs egentligen inte
                i.putExtra("show_cat", radiofam.isChecked());
                i.putExtra("show_fam", radiocat.isChecked());
                i.putExtra("show_all", radioall.isChecked());
                i.putExtra("show_fav", radiofav.isChecked());
                i.putExtra("show_land", radioland.isChecked());
                i.putExtra("region", selected_region);
                i.putExtra("language", language);
                startActivity(i);
            }
        } else {
            if (category_val.equals("Blanda alla, välj antal:") || category_val.equals("Välj familj:") || category_val.equals("Välj kategori:") || category_val.equals("favo_err")) {
                if (category_val.equals("favo_err"))
                    Toast.makeText(getApplicationContext(), "Du måste ha minst fyra växter i favoriter för att starta", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getApplicationContext(), "Välj vad du vill träna på", Toast.LENGTH_SHORT).show();
            } else {
                Intent i = new Intent(getApplicationContext(), QuizPic.class);

                if (radiocat.isChecked() || radiofam.isChecked()) {
                    //är det familjen som ska visas behövs det ta bort det sista i strängen som visar hur många blommor det finns
                    i.putExtra("choice", category_val.substring(0, category_val.length() - 4).trim());
                }
                //skickar vidare bara talet
                else if (radioall.isChecked()) {
                    i.putExtra("choice", category_val.substring(0, category_val.length() - 2).trim());
                } else
                    i.putExtra("choice", category_val);

                i.putExtra("id", trashfamily.toString()); //skickar alltid in trashfamily, behövs egentligen inte
                i.putExtra("show_cat", radiofam.isChecked());
                i.putExtra("show_fam", radiocat.isChecked());
                i.putExtra("show_all", radioall.isChecked());
                i.putExtra("show_fav", radiofav.isChecked());
                i.putExtra("show_land", radioland.isChecked());
                i.putExtra("region", selected_region);
                i.putExtra("language", language);
                startActivity(i);
            }
        }
    }

    private void CreateList() {
        flowerList = new LinkedList<Flower>();
        try {
            InputStream iS = this.getAssets().open("blommor.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(iS));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] flowerElement = line.split("\t");
                flowerList.add(new Flower(flowerElement[0], flowerElement[1], flowerElement[2], flowerElement[3],
                        flowerElement[5], flowerElement[6], flowerElement[7], flowerElement[8], flowerElement[9], flowerElement[10], flowerElement[11], flowerElement[4], flowerElement[12]));
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(flowerList, new QuizMenu.SortByName());
        //Har gjort en "dummy", som alltid kommer sist när man sorterar efter namn. Därför tas den blomman bort
        //varför det är såhär, vet jag inte.
        flowerList.removeLast();
    }

    private void SetUpLists() {
        categorys.add("Välj kategori:");
        categorys.add("Buskar (35)");
        categorys.add("Fibblor (12)");
        categorys.add("Fjällväxter (28)");
        categorys.add("Klängväxter (7)");
        categorys.add("Prydnadsväxter (43)");
        categorys.add("Tistlar (12)");
        categorys.add("Träd (31)");
        categorys.add("Vattenväxter (18)");

        all_no_flowers.add("Blanda alla, välj antal:");
        all_no_flowers.add("10 st");
        all_no_flowers.add("25 st");
        all_no_flowers.add("50 st");
        all_no_flowers.add("100 st");
        all_no_flowers.add("< 506 st");

        SharedPreferences favoData = getSharedPreferences("favolist", 0);


        for (Flower thisFlower : flowerList
                ) {
            //tar fram alla familjer som finns
            if ((!familys.contains(thisFlower.getFamily())) && !categorys.contains(thisFlower.getFamily())) {
                familys.add(thisFlower.getFamily());
            }
            //räknar antal favoriter som finns med i flowerlist
            if (favoData.getBoolean(thisFlower.getID(), false)) {
                no_favos++;
            }
        }

        Collections.sort(familys.subList(0, familys.size()));

        for (String thisString : familys) {
            nr_fam.put(thisString, 0);
        }

        //slår samman familj + anatal växter i den familj?
        for (Flower thisFlower : flowerList) {
            if (familys.contains(thisFlower.getFamily())) {
                nr_fam.put(thisFlower.getFamily(), nr_fam.get(thisFlower.getFamily()) + 1);
            }
        }
        // eller är det denna dubbelloop som slår samman kanske?
        for (String thisFamily : familys) {
            for (String key : nr_fam.keySet()) {
                if (thisFamily.equals(key)) {
                    superfamilys.add(thisFamily + " (" + nr_fam.get(key) + ")");
                }

            }
        }

        //denna tar bort alla familjer som bara har 3 eller mindre --> lägg till i trashfamily
        Iterator<String> it = superfamilys.iterator();
        while (it.hasNext()) {
            String s = it.next(); // must be called before you can call i.remove()
            String lasttwo = s.substring(s.length() - 3);
            if (lasttwo.startsWith("(")) {
                lasttwo = lasttwo.substring(lasttwo.length() - 2);
            }
            int string_int = Integer.parseInt(lasttwo.substring(0, lasttwo.length() - 1));
            if (string_int < 4) {
                trashfamily.add(s);
                it.remove();
            }
        }

        //två statiska val som ligger överst
        superfamilys.add(0, "Välj familj:");
        superfamilys.add(1, "Övriga familjer (87)");
        favor.setText("Mina favoriter (" + no_favos + ")");
        landor.setTextColor(Color.parseColor("#c6c6c6"));
        favor.setTextColor(Color.parseColor("#c6c6c6"));

    }

    //sortera listan efter namn
    private class SortByName implements Comparator<Flower> {
        @Override
        public int compare(Flower o1, Flower o2) {
            return o1.getName().toLowerCase().trim().compareTo(o2.getName().toLowerCase().trim());
        }
    }

    private void setupSpinners() {
        SharedPreferences settingsData = getSharedPreferences("settings", 0);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categorys);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinner_cat != null;
        spinner_cat.setAdapter(adapter);
        spinner_cat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if (item != null) {
                    category_val = item.toString();
                } else {
                    category_val = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });


        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, superfamilys);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinner_fam != null;
        spinner_fam.setAdapter(adapter2);
        //spinner.setSelection(settingsData.getInt("jump_to_region_position", 0));
        spinner_fam.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if (item != null) {
                    category_val = item.toString();
                } else {
                    category_val = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, all_no_flowers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinner_all != null;
        spinner_all.setAdapter(adapter3);
        //spinner.setSelection(settingsData.getInt("jump_to_region_position", 0));
        spinner_all.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if (item != null) {
                    category_val = item.toString();
                } else {
                    category_val = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this,
                R.array.region_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinner_region != null;
        spinner_region.setAdapter(adapter4);
        //spinner.setSelection(settingsData.getInt("jump_to_region_position", 0));
        spinner_region.setSelection(settingsData.getInt("jump_to_region_position", 0));
        spinner_region.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                Object item = arg0.getItemAtPosition(arg2);
                if (item != null) {
                    selected_region = item.toString();
                } else {
                    selected_region = "Hela Sverige";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

    }

    private void disableAll() {
        radiocat.setChecked(false);
        radiofam.setChecked(false);
        radioall.setChecked(false);
        radiofav.setChecked(false);
        radioland.setChecked(false);
        spinner_fam.setEnabled(false);
        spinner_fam.setClickable(false);
        spinner_cat.setEnabled(false);
        spinner_cat.setClickable(false);
        spinner_all.setEnabled(false);
        spinner_all.setClickable(false);
        favor.setTextColor(Color.parseColor("#c6c6c6"));
        landor.setTextColor(Color.parseColor("#c6c6c6"));
        CheckBox chbox = (CheckBox) findViewById(R.id.checkBox);
        chbox.setEnabled(true);
        spinner_region.setEnabled(true);
        selected_region = spinner_region.getSelectedItem().toString();
    }

    public void radio_fam(View view) {
        disableAll();
        radiofam.setChecked(true);
        spinner_cat.setEnabled(true);
        spinner_cat.setClickable(true);
        category_val = spinner_cat.getSelectedItem().toString();
    }

    public void radio_cat(View view) {
        disableAll();
        radiocat.setChecked(true);
        spinner_fam.setEnabled(true);
        spinner_fam.setClickable(true);
        category_val = spinner_fam.getSelectedItem().toString();
    }

    public void radio_all(View view) {
        disableAll();
        radioall.setChecked(true);
        spinner_all.setEnabled(true);
        spinner_all.setClickable(true);
        category_val = spinner_fam.getSelectedItem().toString();
    }

    public void radio_fav(View view) {
        disableAll();
        radiofav.setChecked(true);
        if (no_favos < 4)
            category_val = "favo_err";
        else
            category_val = favor.getText().toString();

        favor.setTextColor(Color.parseColor("#212121"));
    }

    public void radio_land(View view) {
        disableAll();
        radioland.setChecked(true);
        category_val = "Landskapsblommor";
        landor.setTextColor(Color.parseColor("#212121"));
        CheckBox chbox = (CheckBox) findViewById(R.id.checkBox);
        chbox.setEnabled(false);
        spinner_region.setEnabled(false);
        selected_region = "Hela Sverige";
    }

    public void region_check(View view) {
        CheckBox chbox = (CheckBox) findViewById(R.id.checkBox);
        if (chbox.isChecked()) {
            chbox.setChecked(true);
            spinner_region.setEnabled(true);
            spinner_region.setClickable(true);
            selected_region = spinner_region.getSelectedItem().toString();
        } else {
            chbox.setChecked(false);
            spinner_region.setEnabled(false);
            spinner_region.setClickable(false);
            selected_region = "Hela Sverige";

        }

    }

    public void goToListQuiz(View view) {
        ImageView listimage = (ImageView) findViewById(R.id.goToListQuiz);
        ImageView picimage = (ImageView) findViewById(R.id.goToPicQuiz);
        listimage.setImageResource(R.drawable.example_quiz_s);
        picimage.setImageResource(R.drawable.example_quiz_pic);
        type = "list";

    }

    public void goToPicQuiz(View view) {
        ImageView listimage = (ImageView) findViewById(R.id.goToListQuiz);
        ImageView picimage = (ImageView) findViewById(R.id.goToPicQuiz);
        picimage.setImageResource(R.drawable.example_quiz_pic_s);
        listimage.setImageResource(R.drawable.example_quiz);
        type = "pic";
    }

    public void radio_lan(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.radio_swedish:
                if (checked)
                    language = "";
                break;
            case R.id.radio_latin:
                if (checked)
                    language = "latin";
                    break;
        }
    }
}
