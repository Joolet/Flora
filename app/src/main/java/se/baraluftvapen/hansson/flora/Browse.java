/*
    Hanterar bläddrings-/favorit-vyn med presentation, filtrering, sortering och sökning av växter
*/
package se.baraluftvapen.hansson.flora;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Point;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import static se.baraluftvapen.hansson.flora.R.id.action_sort;
import static se.baraluftvapen.hansson.flora.R.id.all_color;
import static se.baraluftvapen.hansson.flora.R.id.main_filter;

public class Browse extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private GridView            gridView;
    private LinkedList<Flower>  flowerList;                                              //Lista med alla blommor. Denna lista kommer förbli orörd
    private LinkedList<Flower>  refinedList = new LinkedList<Flower>();                  //Lista med filtrerade blommor. Listan skapas och raderas på nytt för varje filtreringsval
    private Menu                menu;                                           //Ha menyn som instansvariablen eftersom menyn kommer att uppdateras
    private Boolean             favoriteActive;                                 //Har kolla på om vi är i bläddra-vyn eller i favorit-vyn
    private int                 currentMonthInt = 0;                            //Håller årets månad, 1 = januari, 12 = decmber
    private String              currentRegionChar = "no";                       //Hoppa till vald landskap/region? Deafult läget är "nej hoppa inte till region"
    private int                 currentMonthId;                                 //Identifier för månaden i menyn, så den vet vilket id det har i Layouten-filen
    private int                 currentRegionId;                                //Identifier för Region i menyn, så den vet vilket id det har i Layouten-filen
    private SharedPreferences   settings;                                       //Hämtar data på valda inställningar och in/ut data från olika Activitys
    private String              latestSearch;                                   //Senaste sökning, trycker man på en blomma utan sök knappen, och sedan backar-> så finns sökning kvar här
    private String[]            filterSettings = new String[]{"", "", "", ""};  //Filterval, [0] Blomfärg, [1] Blomningstid, [2]Region, [3]Familj
    private MenuItem            searchItem;                                     
    private SearchView          searchView;
    private boolean             searchListActive = false;                       //Håller reda på om sökning har gjorts, så RefinedList visas istället för FlowerList
    private String              packageName;
    private int                 resId;                                          //Resource ID
    private int                 org_ny_blom_len = 0;                            //Sparar antal växter som har blivit uppdaterade via onlinefilen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getWindow().setBackgroundDrawableResource(R.drawable.wood4);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        registerForContextMenu(navigationView);
        packageName = getPackageName();
        
        CreateList(); //skapar listan

        //Tar bort blommor i flowerlist, om vi är i favoritvyn
        Intent i = getIntent();
        favoriteActive = i.getBooleanExtra("favo", false);
        if (i.getBooleanExtra("favo", false)) {
            SharedPreferences favoData = getSharedPreferences("favolist", 0);
            Iterator<Flower> it = flowerList.iterator();
            while (it.hasNext()) {
                Flower thisflower = it.next();
                if (!favoData.getBoolean(thisflower.getID(), false)) {
                    it.remove();
                }
            }
        }
        SharedPreferences settingsData = getSharedPreferences("settings", 0);
        if (!settingsData.getBoolean("offlinemode", false)) {
            //Uppdaterar blomdatabasen ifrån internetfilen
            if (!i.getExtras().getStringArray("update")[0].equals("nodata")) {
                String[] nyBlomma = i.getExtras().getStringArray("update");
                assert nyBlomma != null;
                String[][] nyBlommaElement = new String[nyBlomma.length][12]; //två-dimenstionel array
                
                //styckar upp nyBlomma så varje elment koms åt
                settings = getSharedPreferences("flowerdata", 0);
                SharedPreferences.Editor editor = settings.edit();
                //int errors, behövdes när användaren kunde lägga till antal kronblad, eftrsom det skapades tomma rader ibland
                //int errors = 0;     //räknar antalet tomma rader
                for (int j = 1; j < nyBlomma.length; j++) {

                    nyBlommaElement[j] = nyBlomma[j].split("\t");
                    //i Sharedpref. "flowerdata" finns rader för varje växt som finns med i uppdateringsfilen, dessa lagras med nyckeln "j" alltså bara 0, 1 2 3 osv
                    //för att sedan komma åt tex flowerid så behöver man skapa en två-demisionel stringarray
                        editor.putString(Integer.toString(j), nyBlomma[j]);
                        editor.putInt("nyBlomma_length", nyBlomma.length);      //längeden på hur många blommor som är med uppdateringsfilen (används sedan i floweractivity)
                        editor.apply();
                /*
                for (int jk = 1; jk < nyBlomma.length; jk++) {
                    settings = getSharedPreferences("settings", 0);
                    editor.putString(nyBlommaElement[j][jk], nyBlommaElement[j][jk]);
                    editor.apply();
                }
                */
                }
                //spara hur många växter som blivit uppdaterade. Så upptäcks det om användaren har redigerat "anatl kronblad" i Floweractivity
                org_ny_blom_len = nyBlomma.length;
                editor.putInt("nyBlomma_length", nyBlomma.length);      //längeden på hur många blommor som är med uppdateringsfilen (används sedan i floweractivity)
                editor.apply();
                //dubbel forloop, som letar upp om IDnamnet passar. Gör det det så kontrolleras det om det finns data som ska ersättas
                // "k" indikerar att fältet inte ska uppdateras
                for (int j = 0; j < nyBlomma.length - 1; j++) {
                    for (Flower newList : flowerList) {
                        if (newList.getID().equals(nyBlommaElement[j][1])) {
                            if (!nyBlommaElement[j][0].equals("k"))
                                newList.setName(nyBlommaElement[j][0]);
                            if (!nyBlommaElement[j][2].equals("k"))
                                newList.setColor(nyBlommaElement[j][2]);
                            if (!nyBlommaElement[j][3].equals("k"))
                                newList.setOtherName(nyBlommaElement[j][3]);
                            if (!nyBlommaElement[j][4].equals("k"))
                                newList.setLatin(nyBlommaElement[j][4]);
                            if (!nyBlommaElement[j][5].equals("k"))
                                newList.setAreacode(nyBlommaElement[j][5]);
                            if (!nyBlommaElement[j][6].equals("k"))
                                newList.setLength(nyBlommaElement[j][6]);
                            if (!nyBlommaElement[j][7].equals("k"))
                                newList.setFamily(nyBlommaElement[j][7]);
                            if (!nyBlommaElement[j][8].equals("k"))
                                newList.setSpread(nyBlommaElement[j][8]);
                            if (!nyBlommaElement[j][9].equals("k"))
                                newList.setBloomID(nyBlommaElement[j][9]);
                            if (!nyBlommaElement[j][10].equals("k"))
                                newList.setCategory(nyBlommaElement[j][10]);
                            if (!nyBlommaElement[j][11].equals("k"))
                                newList.setDescription(nyBlommaElement[j][11]);
                            if (!nyBlommaElement[j][12].equals("k"))
                                newList.setEdited(nyBlommaElement[j][12]);
                        }
                    }
                }
            }
        }
        //sätt sortering AZ till standard, oavsett vad sortering innan var
        settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("sort", "az");
        editor.apply();
        jumpToRegionMonth();

        //presentera blommorna beronde vilken lista som är aktiv
        if (isFlowerListActive()) {
            DisplayList(flowerList);
        } else {
            RefineList();
            DisplayList(refinedList);
        }

/*
Denna "metod" skapar en ny aktivitet (FlowerActivity) som visar mer info om blomman som trycktes på.
Går igenom blomlistan för att matcha den som trycktes på, sedan skickas all data om blomman vidare till den nya aktiviteten.
 */
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), FlowerActivity.class);
                StartNewActivity(i, v); //denna metod förbreder och öppnar floweractivity
            }
        });
    }

////sorterar listan efter namn
    private class SortByName implements Comparator<Flower> {
        @Override
        public int compare(Flower o1, Flower o2) {
            return o1.getName().toLowerCase().trim().compareTo(o2.getName().toLowerCase().trim());
        }
    }


////sorterar listan efter blommans höjd
    private class SortByLength implements Comparator<Flower> {
        @Override
        public int compare(Flower o1, Flower o2) {
            int first = Integer.parseInt(o1.getLength());
            int second = Integer.parseInt(o2.getLength());
            if (first < second) return -10;
            else return 10;
        }
    }

////kontrollerar om flowerlist är aktiv
    private boolean isFlowerListActive() {
        SharedPreferences settingsData = getSharedPreferences("settings", 0);
        return filterSettings[0].equals("") && filterSettings[1].equals("") && filterSettings[2].equals("") && filterSettings[3].equals("")
                && !searchListActive && settingsData.getString("sort", "az").equals("az");
    }

/*------------------------------------------------------------------------------------------------------
  Lägger de växter som matchar kriteriet i refinedList
  4 st filter alternativ ger 16 kombinationer. 5 st är därför inte praktiskt möjligt
  Under dessa 16 ifsatser finns en bättre lösning
  Sedan sorteras listan efter användarens menyval
  -----------------------------------------------------------------------------------------------------
*/
    private void RefineList() {

        refinedList.clear(); //rensar listan först och sedan gör om hela listan

//1000
        if (!filterSettings[0].equals("") && filterSettings[1].equals("") && filterSettings[2].equals("") && filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0]))
                    refinedList.add(thisFlower);
            }
//1110
        } else if (!filterSettings[0].equals("") && !filterSettings[1].equals("") && !filterSettings[2].equals("") && filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0]) && thisFlower.getBloomID().contains(filterSettings[1]) && thisFlower.getAreacode().contains(filterSettings[2]))
                    refinedList.add(thisFlower);
            }
//0100
        } else if (filterSettings[0].equals("") && !filterSettings[1].equals("") && filterSettings[2].equals("") && filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getBloomID().contains(filterSettings[1]))
                    refinedList.add(thisFlower);
            }
//0000
        } else if (filterSettings[0].equals("") && filterSettings[1].equals("") && filterSettings[2].equals("") && filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                refinedList.add(thisFlower);
            }
//0110
        } else if (filterSettings[0].equals("") && !filterSettings[1].equals("") && !filterSettings[2].equals("") && filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getBloomID().contains(filterSettings[1]) && thisFlower.getAreacode().contains(filterSettings[2]))
                    refinedList.add(thisFlower);
            }
//0010
        } else if (filterSettings[0].equals("") && filterSettings[1].equals("") && !filterSettings[2].equals("") && filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getAreacode().contains(filterSettings[2]))
                    refinedList.add(thisFlower);
            }
//1010
        } else if (!filterSettings[0].equals("") && filterSettings[1].equals("") && !filterSettings[2].equals("") && filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0]) && thisFlower.getAreacode().contains(filterSettings[2]))
                    refinedList.add(thisFlower);
            }
//1100
        } else if (!filterSettings[0].equals("") && !filterSettings[1].equals("") && filterSettings[2].equals("") && filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0]) && thisFlower.getBloomID().contains(filterSettings[1]))
                    refinedList.add(thisFlower);
            }
        }
//1001
        if (!filterSettings[0].equals("") && filterSettings[1].equals("") && filterSettings[2].equals("") && !filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0]) && (thisFlower.getFamily().contains(filterSettings[3]) || thisFlower.getCategory().contains(filterSettings[3])))
                    refinedList.add(thisFlower);
            }
//1111
        } else if (!filterSettings[0].equals("") && !filterSettings[1].equals("") && !filterSettings[2].equals("") && !filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0]) && thisFlower.getBloomID().contains(filterSettings[1]) && thisFlower.getAreacode().contains(filterSettings[2]) && (thisFlower.getFamily().contains(filterSettings[3]) || thisFlower.getCategory().contains(filterSettings[3])))
                    refinedList.add(thisFlower);
            }
//0101
        } else if (filterSettings[0].equals("") && !filterSettings[1].equals("") && filterSettings[2].equals("") && !filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getBloomID().contains(filterSettings[1]) && (thisFlower.getFamily().contains(filterSettings[3]) || thisFlower.getCategory().contains(filterSettings[3])))
                    refinedList.add(thisFlower);
            }
//0001
        } else if (filterSettings[0].equals("") && filterSettings[1].equals("") && filterSettings[2].equals("") && !filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getFamily().contains(filterSettings[3]) || thisFlower.getCategory().contains(filterSettings[3]))
                    refinedList.add(thisFlower);
            }
//0111
        } else if (filterSettings[0].equals("") && !filterSettings[1].equals("") && !filterSettings[2].equals("") && !filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getBloomID().contains(filterSettings[1]) && thisFlower.getAreacode().contains(filterSettings[2]) && (thisFlower.getFamily().contains(filterSettings[3]) || thisFlower.getCategory().contains(filterSettings[3])))
                    refinedList.add(thisFlower);
            }
//0011
        } else if (filterSettings[0].equals("") && filterSettings[1].equals("") && !filterSettings[2].equals("") && !filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getAreacode().contains(filterSettings[2]) && (thisFlower.getFamily().contains(filterSettings[3]) || thisFlower.getCategory().contains(filterSettings[3])))
                    refinedList.add(thisFlower);
            }
//1011
        } else if (!filterSettings[0].equals("") && filterSettings[1].equals("") && !filterSettings[2].equals("") && !filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0]) && thisFlower.getAreacode().contains(filterSettings[2]) && (thisFlower.getFamily().contains(filterSettings[3]) || thisFlower.getCategory().contains(filterSettings[3])))
                    refinedList.add(thisFlower);
            }
//1101
        } else if (!filterSettings[0].equals("") && !filterSettings[1].equals("") && filterSettings[2].equals("") && !filterSettings[3].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0]) && thisFlower.getBloomID().contains(filterSettings[1]) && (thisFlower.getFamily().contains(filterSettings[3]) || thisFlower.getCategory().contains(filterSettings[3])))
                    refinedList.add(thisFlower);
            }
        }

//Funkar!
//betydligt mer dynamisk, ett filteralternativ till --> en if-sats
        //avvaktar tills jag lägger till nytt filter
        //eftersom den ovan är failsafe!
        /*
        if (!filterSettings[0].equals("")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getColor().contains(filterSettings[0])) {
                    refinedList.add(thisFlower);
                }
            }
        }
        if (!filterSettings[1].equals("")) {
            if (refinedList.size() > 0) {
                Iterator<Flower> it = refinedList.iterator();
                while (it.hasNext()) {
                    Flower s = it.next();
                    if (!s.getBloomID().contains(filterSettings[1])) {
                        it.remove();
                    }
                }
            }
            else{
                for (Flower thisFlower : flowerList
                        ) {
                    if (thisFlower.getBloomID().contains(filterSettings[1])) {
                        refinedList.add(thisFlower);
                    }
                }
            }
        }
        if (!filterSettings[2].equals("")) {
            if (refinedList.size() > 0) {
                Iterator<Flower> it = refinedList.iterator();
                while (it.hasNext()) {
                    Flower s = it.next();
                    if (!s.getAreacode().contains(filterSettings[2])) {
                        it.remove();
                    }
                }
            }
            else{
                for (Flower thisFlower : flowerList
                        ) {
                    if (thisFlower.getAreacode().contains(filterSettings[2])) {
                        refinedList.add(thisFlower);
                    }
                }
            }
        }
        if (!filterSettings[3].equals("")) {
            if (refinedList.size() > 0) {
                Iterator<Flower> it = refinedList.iterator();
                while (it.hasNext()) {
                    Flower s = it.next();
                    if (!(s.getCategory().contains(filterSettings[3]) || s.getFamily().contains(filterSettings[3]))) {
                        it.remove();
                    }
                }
            }
            else{
                for (Flower thisFlower : flowerList
                        ) {
                    if (thisFlower.getCategory().contains(filterSettings[3]) || thisFlower.getFamily().contains(filterSettings[3])) {
                        refinedList.add(thisFlower);
                    }
                }
            }
        }
*/
        //sortera listan efter vad som önskas
        SharedPreferences settingsData = getSharedPreferences("settings", 0);
        if (settingsData.getString("sort", "az").equals("za")) {
            //Collections.sort(refinedList, new SortByName());
            Collections.reverse(refinedList);
        } else if (settingsData.getString("sort", "az").equals("length_down")) {
            Collections.sort(refinedList, new SortByLength());
        } else if (settingsData.getString("sort", "az").equals("length_up")) {
            Collections.sort(refinedList, new SortByLength());
            Collections.reverse(refinedList);
        }

        //undantag då det finns trädgårdsveronika, som innehåller ordet träd
        if (filterSettings[3].equals("Träd")) {
            Iterator<Flower> it = refinedList.iterator();
            while (it.hasNext()) {
                Flower thisflower = it.next();
                if (thisflower.getName().contains("veronika")) {
                    it.remove();
                }
            }
        }

    }

    /*----------------------------------------------------------------------------------
    Läser in datafilen med blommor och skapar ett nytt objekt för varje blomma
    ------------------------------------------------------------------------------------
     */
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
        //sortera växterna i bokstavsordning
        Collections.sort(flowerList, new SortByName());
        //Har gjort en "dummy", som alltid kommer sist när man sorterar efter namn. Därför tas den blomman bort
        //varför det är såhär, vet jag inte.
        flowerList.removeLast();
    }
//-------------------------------------------------------------------------------------------------
//Metod som presenterar den aktiva listan
//-------------------------------------------------------------------------------------------------
    private void DisplayList(LinkedList<Flower> thisList) {
        int listasize = thisList.size();
        String[] imageID = new String[listasize];
        String[] nameOfFlower = new String[listasize];

        //läser av alla namn och bildIDm som sedan behöves för att inflatea gridview
        for (int i = 0; i < listasize; i++) {
            imageID[i] = thisList.get(i).getID();
            nameOfFlower[i] = thisList.get(i).getName();
        }
        //letar upp hur många kolumner som användaren har angett
        //"settings" är listan som nyckelorden finns. "0" är Private_mode(ej läsbar för andra)
        //"antalkolumner" är nycketordet och "3" är default value om nyckelordet inte finns
        SharedPreferences settingsData = getSharedPreferences("settings", 0);
        int noKol = settingsData.getInt("antalkolumner", 4);

        gridView = (GridView) findViewById(R.id.gridView1);
        assert gridView != null;
        gridView.setNumColumns(noKol);
        gridView.setAdapter(new ImageAdapter(this, imageID, nameOfFlower));

        if (flowerList.size() <= 0) {
            Toast.makeText(getApplicationContext(), "Inga favoriter tillagda", Toast.LENGTH_LONG).show();
        } else if (listasize <= 0 && !searchListActive) {
            Toast.makeText(getApplicationContext(), "Filtreringen gav inget resultat!", Toast.LENGTH_SHORT).show();
        }

        //aktivera fastscrollbaren?
        if (settingsData.getBoolean("scroll", false)) {
            gridView.setFastScrollEnabled(true);
            gridView.setFastScrollAlwaysVisible(true);
        } else {
            gridView.setFastScrollEnabled(false);
            gridView.setFastScrollAlwaysVisible(false);
        }
    }
//-------------------------------------------------------------------------------------------------
    //kommer man tillbaka från inställningar, så får browse info om vyn behöver ändras
    //Eller om man kommer från Floweractivity och vill visa en familj/kategori
//-------------------------------------------------------------------------------------------------
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //kommer tallbaka från inställningarna
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra("changed", false)) {
                    if (isFlowerListActive()) {
                        DisplayList(flowerList);
                    } else {
                        DisplayList(refinedList);
                    }
                }
            }
            //kommer tillbaka från Floweractivity --> visa vald familj/kategori
        } else if (requestCode == 2) {
            if (resultCode == RESULT_OK || resultCode == -1) {
                String dataBack = data.getStringExtra("viewFamily");
                if (!(dataBack == null || dataBack.equals(""))) {
                    if (dataBack.equals("Fjällväxter") || dataBack.equals("Prydnadsväxter") || dataBack.equals("Träd") || dataBack.equals("Landskapsblommor") || dataBack.equals("Buskar") || dataBack.equals("Vattenväxter") || dataBack.equals("Klängväxter") || dataBack.equals("Fibblor") || dataBack.equals("Tistlar") || data.getBooleanExtra("toViewFamily", false)) {
                        searchListActive = false;
                        resetFilter();
                        String[] family = new String[30];
                        //hmm tror inte komma existerar när man kommer tillbaka
                        if (dataBack.contains(",")) {
                            family = dataBack.split(",");
                            //splitar namanet så man bara får det första ordet tex "flockblommiga växter", så skippar "växter"
                        } else if (dataBack.contains(" ")) {
                            family = dataBack.split(" ");
                            family[0] = family[0].trim();
                        } else {
                            family[0] = dataBack.trim();
                        }
                        filterSettings[3] = family[0];
                        family[0] = family[0].replace('ö', 'o').replace('å', 'a').replace('ä', 'a').replace('é', 'e').replace('Ä', 'A').replace('Ö', 'O').replace('Å', 'A');
                        //hämta först strängen i stringsxml filen
                        resId = getResources().getIdentifier(family[0], "string", packageName);

                        //uppdaterar menyn
                        if (!(dataBack.equals("Fibblor") || dataBack.equals("Tistlar") || dataBack.equals("Klängväxter"))) //tre småkategorier som jag inte tar med i menyn
                        {
                            if (resId != 0) {
                                String qfamily = getString(resId);
                                //hämta sedan id, utifrån strängen
                                int id = getResources().getIdentifier("q" + qfamily, "id", packageName);
                                onOptionsItemSelected(menu.findItem(id));
                            }
                        }
                        //filtrera och presentera
                        RefineList();
                        DisplayList(refinedList);
                    }

                    //Är det inte en av kategorierna, så är det en växt som ska visas istället
                    //leta upp växten, samla växtdata, skicka in det och strta activity
                    else {
                        for (Flower thisflower : flowerList
                                ) {
                            if (dataBack.equals(thisflower.getName())) {
                                String[] id1 = {thisflower.getID(), thisflower.getID()};
                                String[] bloom1 = {thisflower.getBloom(), thisflower.getBloom()};
                                String[] name1 = {thisflower.getName(), thisflower.getName()};
                                String[] othername1 = {thisflower.getToxic(), thisflower.getToxic()};
                                String[] des1 = {thisflower.getDescription(), thisflower.getDescription()};
                                String[] fam1 = {thisflower.getFamily(), thisflower.getFamily()};
                                String[] spread1 = {thisflower.getSpread(), thisflower.getSpread()};
                                String[] len1 = {thisflower.getLength(), thisflower.getLength()};
                                String[] area1 = {thisflower.getAreacode(), thisflower.getAreacode()};
                                String[] latin1 = {thisflower.getLatin(), thisflower.getLatin()};
                                String[] edited1 = {thisflower.getEdited(), thisflower.getEdited()};
                                String[] color1 = {thisflower.getColor(), thisflower.getColor()};
                                String[] cate1 = {thisflower.getCategory(), thisflower.getCategory()};
                                Intent i = new Intent(getApplicationContext(), FlowerActivity.class);
                                i.putExtra("item", 1);
                                i.putExtra("id", id1);
                                i.putExtra("bloom", bloom1);
                                i.putExtra("name", name1);
                                i.putExtra("latin", latin1);
                                i.putExtra("otherName", othername1);
                                i.putExtra("description", des1);
                                i.putExtra("family", fam1);
                                i.putExtra("spread", spread1);
                                i.putExtra("length", len1);
                                i.putExtra("area", area1);
                                i.putExtra("category", cate1);
                                i.putExtra("edited", edited1);
                                i.putExtra("color", edited1);
                                startActivityForResult(i, 2);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

//-------------------------------------------------------------------------------------------------
    @Override
    public void onResume() {
        View view = this.getCurrentFocus();
        //släng ner tangentbord
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        //skulle det vara så att användaren redigerat "antal kronblad" i floweractivity,
        //så måste det kontrolleras så ändringen skall kunna ske lokalt
        String flowerdata_string;
        settings = getSharedPreferences("flowerdata", 0);
        int resume_length = settings.getInt("nyBlomma_length", 0);
        if ((org_ny_blom_len < resume_length) || settings.getBoolean("foundit", false)) {
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("foundit", false);
            editor.apply();

            for (int j = 1; j < resume_length; j++) {
                flowerdata_string = settings.getString(Integer.toString(j), "no");    //hämtar vad som finns i minnet
                String nyBlommaElement[] = flowerdata_string.split("\t");

                for (Flower newList : flowerList) {
                    if (newList.getID().equals(nyBlommaElement[1])) {
                        if (!nyBlommaElement[0].equals("k"))
                            newList.setName(nyBlommaElement[0]);
                        if (!nyBlommaElement[2].equals("k"))
                            newList.setColor(nyBlommaElement[2]);
                        if (!nyBlommaElement[3].equals("k"))
                            newList.setOtherName(nyBlommaElement[3]);
                        if (!nyBlommaElement[4].equals("k"))
                            newList.setLatin(nyBlommaElement[4]);
                        if (!nyBlommaElement[5].equals("k"))
                            newList.setAreacode(nyBlommaElement[5]);
                        if (!nyBlommaElement[6].equals("k"))
                            newList.setLength(nyBlommaElement[6]);
                        if (!nyBlommaElement[7].equals("k"))
                            newList.setFamily(nyBlommaElement[7]);
                        if (!nyBlommaElement[8].equals("k"))
                            newList.setSpread(nyBlommaElement[8]);
                        if (!nyBlommaElement[9].equals("k"))
                            newList.setBloomID(nyBlommaElement[9]);
                        if (!nyBlommaElement[10].equals("k"))
                            newList.setCategory(nyBlommaElement[10]);
                        if (!nyBlommaElement[11].equals("k"))
                            newList.setDescription(nyBlommaElement[11]);
                        if (!nyBlommaElement[12].equals("k"))
                            newList.setEdited(nyBlommaElement[12]);
                    }
                }
            }
        }
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void onStop() {
        super.onStop();
    }

    /*-------------------------------------------------------------------------------------------------------------
    Denna metod startar FlowerActivity för att vissa information om vald blomma.
    EFtersom man inte får skicka in blomlistan i kommande aktivitet så skickar jag flera strängearrayer som innehåller alla information.
    Detta görs så att man kan swipa i Floweractivity, så den håller reda på nästkommande/föregående blomma.
    -------------------------------------------------------------------------------------------------------------
     */
    private void StartNewActivity(Intent i, View v) {
        int sizeOfList;

        //stäng sök skiten --> listan visar Flowerlist --> tvinga den att visa refinedlist istället
        if (searchItem.isActionViewExpanded()) {
            searchItem.collapseActionView();
            DisplayList(refinedList);
        }
        //Kollar vilken av flowerlist eller refinedlist som är aktiv
        if (isFlowerListActive()) {
            sizeOfList = flowerList.size();
        } else {
            sizeOfList = refinedList.size();
        }

        //Sätter längden på alla arrayer
        String[] ids = new String[sizeOfList];
        String[] blooms = new String[sizeOfList];
        String[] names = new String[sizeOfList];
        String[] colors = new String[sizeOfList];
        String[] otherName = new String[sizeOfList];
        String[] dess = new String[sizeOfList];
        String[] family = new String[sizeOfList];
        String[] spreads = new String[sizeOfList];
        String[] length = new String[sizeOfList];
        String[] latin = new String[sizeOfList];
        String[] edited = new String[sizeOfList];
        String[] category = new String[sizeOfList];
        String[] area = new String[sizeOfList];

        //beroende på vilken blomlista som är aktiv, laddas strängarrayerna olika.
        int dubblename = -1;
        for (int j = 0; j < sizeOfList; j++) {
            if (isFlowerListActive()) {
                String flowerLabel = flowerList.get(j).getName().trim(); //hämtar namn
                //innehåller label en del av blommans namn ska det skicka in positionen till floweractivity
                if (flowerLabel.contains(((TextView) v.findViewById(R.id.grid_item_label)).getText())) {
                    //undantag som gör att flera har samma namn
                    if (flowerLabel.equals("Humle") || flowerLabel.equals("Björk") || flowerLabel.equals("Lin") || flowerLabel.equals("Ek") || flowerLabel.equals("Pil") || flowerLabel.equals("Tall")) {
                        dubblename = j;
                    } else {
                        i.putExtra("item", j);
                    }
                }

                ids[j] = flowerList.get(j).getID();
                blooms[j] = flowerList.get(j).getBloom();
                names[j] = flowerList.get(j).getName();
                colors[j] = flowerList.get(j).getColor();
                otherName[j] = flowerList.get(j).getToxic();
                dess[j] = flowerList.get(j).getDescription();
                family[j] = flowerList.get(j).getFamily();
                spreads[j] = flowerList.get(j).getSpread();
                latin[j] = flowerList.get(j).getLatin();
                edited[j] = flowerList.get(j).getEdited();
                length[j] = flowerList.get(j).getLength();
                category[j] = flowerList.get(j).getCategory();
                area[j] = flowerList.get(j).getAreacode();

            //om refined är aktiv
            } else {
                String flowerLabel = refinedList.get(j).getName().trim(); //hämtar namn
                if (flowerLabel.contains(((TextView) v.findViewById(R.id.grid_item_label)).getText())) {
                    //undantag som gör att flera har samma namn
                    if (flowerLabel.equals("Humle") || flowerLabel.equals("Björk") || flowerLabel.equals("Lin") || flowerLabel.equals("Ek") || flowerLabel.equals("Pil") || flowerLabel.equals("Tall")) {
                        dubblename = j;
                    } else {
                        i.putExtra("item", j);
                    }
                }
                ids[j] = refinedList.get(j).getID();
                blooms[j] = refinedList.get(j).getBloom();
                names[j] = refinedList.get(j).getName();
                colors[j] = refinedList.get(j).getColor();
                otherName[j] = refinedList.get(j).getToxic();
                dess[j] = refinedList.get(j).getDescription();
                family[j] = refinedList.get(j).getFamily();
                latin[j] = refinedList.get(j).getLatin();
                edited[j] = refinedList.get(j).getEdited();
                spreads[j] = refinedList.get(j).getSpread();
                length[j] = refinedList.get(j).getLength();
                category[j] = refinedList.get(j).getCategory();
                area[j] = refinedList.get(j).getAreacode();
            }
        }

        //kontrollerar om det var flera som har samma namn, skickar i så fall med den som var först förekommande
        if (dubblename != -1) {
            i.putExtra("item", dubblename);
        }

        //Skickar strängarrayerna och startar aktiviteten
        i.putExtra("id", ids);
        i.putExtra("bloom", blooms);
        i.putExtra("name", names);
        i.putExtra("color", colors);
        i.putExtra("otherName", otherName);
        i.putExtra("description", dess);
        i.putExtra("family", family);
        i.putExtra("spread", spreads);
        i.putExtra("latin", latin);
        i.putExtra("edited", edited);
        i.putExtra("length", length);
        i.putExtra("area", area);
        i.putExtra("category", category);
        startActivityForResult(i, 2);
    }

    /*-------------------------------------------------------------------------------------------------------------
    Denna metod definerar vad som ska hända när en menyknapp trycks/väljs i actionbaren
    -------------------------------------------------------------------------------------------------------------
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        switch (item.getItemId()) {
            case all_color:
                //knapparna fungerar som en spinner.
                filterSettings[0] = "";
                menu.findItem(R.id.filter_color).setTitle(getResources().getString(R.string.pick_color));
                menu.findItem(R.id.filter_color).setIcon(null);
                break;
            case R.id.yellow:
                filterSettings[0] = "Gul";
                menu.findItem(R.id.filter_color).setTitle("Gula");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.orange:
                filterSettings[0] = "Orange";
                menu.findItem(R.id.filter_color).setTitle("Orange");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.blue:
                filterSettings[0] = "Blå";
                menu.findItem(R.id.filter_color).setTitle("Blåa");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.red:
                filterSettings[0] = "Röd";
                menu.findItem(R.id.filter_color).setTitle("Röda");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.green:
                filterSettings[0] = "Grön";
                menu.findItem(R.id.filter_color).setTitle("Gröna");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.violet:
                filterSettings[0] = "Violett";
                menu.findItem(R.id.filter_color).setTitle("Violetta");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.brown:
                filterSettings[0] = "Brun";
                menu.findItem(R.id.filter_color).setTitle("Bruna");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.purple:
                filterSettings[0] = "Lila";
                menu.findItem(R.id.filter_color).setTitle("Purpur");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.white:
                filterSettings[0] = "Vit";
                menu.findItem(R.id.filter_color).setTitle("Vita");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.skar:
                filterSettings[0] = "Skär";
                menu.findItem(R.id.filter_color).setTitle("Skära");
                menu.findItem(R.id.filter_color).setIcon(R.drawable.ic_check_black_24dp);
                break;
//Regioner-----------------------------------------------------------------------------------------------------------
            case R.id.all_regions:
                filterSettings[2] = "";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.pick_region));
                menu.findItem(R.id.filter_region).setIcon(null);
                break;
            case R.id.r1:
                filterSettings[2] = "1";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.r1));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.ra:
                filterSettings[2] = "a";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.ra));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rb:
                filterSettings[2] = "b";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rb));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rc:
                filterSettings[2] = "c";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rc));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rd:
                filterSettings[2] = "d";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rd));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.re:
                filterSettings[2] = "e";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.re));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rf:
                filterSettings[2] = "f";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rf));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rg:
                filterSettings[2] = "g";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rg));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rh:
                filterSettings[2] = "h";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rh));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.ri:
                filterSettings[2] = "i";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.ri));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rj:
                filterSettings[2] = "j";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rj));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rk:
                filterSettings[2] = "k";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rk));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rl:
                filterSettings[2] = "l";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rl));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rm:
                filterSettings[2] = "m";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rm));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rn:
                filterSettings[2] = "n";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rn));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.ro:
                filterSettings[2] = "o";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.ro));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rp:
                filterSettings[2] = "p";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rp));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rq:
                filterSettings[2] = "q";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rq));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rr:
                filterSettings[2] = "r";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rr));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rs:
                filterSettings[2] = "s";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rs));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.rt:
                filterSettings[2] = "t";
                menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.rt));
                menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
                break;
//Månader-----------------------------------------------------------------------------------------------------------
            case R.id.all_month:
                filterSettings[1] = "";
                menu.findItem(R.id.filter_bloom).setTitle(getResources().getString(R.string.pick_month));
                menu.findItem(R.id.filter_bloom).setIcon(null);
                break;
            case R.id.jan:
                filterSettings[1] = "1";
                menu.findItem(R.id.filter_bloom).setTitle("Januari");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.feb:
                filterSettings[1] = "2";
                menu.findItem(R.id.filter_bloom).setTitle("Februari");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.mar:
                filterSettings[1] = "3";
                menu.findItem(R.id.filter_bloom).setTitle("Mars");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.apr:
                filterSettings[1] = "4";
                menu.findItem(R.id.filter_bloom).setTitle("April");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.may:
                filterSettings[1] = "5";
                menu.findItem(R.id.filter_bloom).setTitle("Maj");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.jun:
                filterSettings[1] = "6";
                menu.findItem(R.id.filter_bloom).setTitle("Juni");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.jul:
                filterSettings[1] = "7";
                menu.findItem(R.id.filter_bloom).setTitle("Juli");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.sep:
                filterSettings[1] = "9";
                menu.findItem(R.id.filter_bloom).setTitle("September");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.oct:
                filterSettings[1] = "A";
                menu.findItem(R.id.filter_bloom).setTitle("Oktober");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.nov:
                filterSettings[1] = "B";
                menu.findItem(R.id.filter_bloom).setTitle("November");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.dec:
                filterSettings[1] = "C";
                menu.findItem(R.id.filter_bloom).setTitle("December");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.aug:
                filterSettings[1] = "8";
                menu.findItem(R.id.filter_bloom).setTitle("Augusti");
                menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                break;
//Familjer/kategori-----------------------------------------------------------------------------------------------------------
            case R.id.all_group:
                filterSettings[3] = "";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.pick_family));
                menu.findItem(R.id.filter_group).setIcon(null);
                break;
            case R.id.qart:
                filterSettings[3] = "Ärtväxter";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.art));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qprydnad:
                filterSettings[3] = "Prydnadsväxter";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.prydnad));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qland:
                filterSettings[3] = "Landskapsblommor";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.land));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qbla:
                filterSettings[3] = "Blågul";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.bla));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qbusk:
                filterSettings[3] = "Buskar";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.busk));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qdes:
                filterSettings[3] = "Desmeknopp";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.des));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qfack:
                filterSettings[3] = "Fackels";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.fack));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qfet:
                filterSettings[3] = "Fetblad";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.fet));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qgen:
                filterSettings[3] = "Gentiana";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.gen));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qgro:
                filterSettings[3] = "Groblad";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.gro));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qham:
                filterSettings[3] = "Hamp";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ham));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qlju:
                filterSettings[3] = "Ljung";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.lju));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qara:
                filterSettings[3] = "Aralia";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ara));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qflo:
                filterSettings[3] = "Flockb";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.flo));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qdun:
                filterSettings[3] = "Dunört";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.dun));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qork:
                filterSettings[3] = "Orkide";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ork));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qlok:
                filterSettings[3] = "Lökv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.lok));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qtre:
                filterSettings[3] = "Treblad";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.tre));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qiris:
                filterSettings[3] = "Irisv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.iris));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qsva:
                filterSettings[3] = "Svalting";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.sva));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qhar:
                filterSettings[3] = "Harsyr";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.har));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qjoh:
                filterSettings[3] = "Johannes";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.joh));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qjord:
                filterSettings[3] = "Jordrök";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.jord));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qjung:
                filterSettings[3] = "Jungfru";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.jung));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkap:
                filterSettings[3] = "Kaprifol";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.kap));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qklock:
                filterSettings[3] = "Klock";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.klock));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkon:
                filterSettings[3] = "Konvalj";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.kon));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkor:
                filterSettings[3] = "Kornell";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.kon));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkorg:
                filterSettings[3] = "Korgblommiga";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.korg));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkors:
                filterSettings[3] = "Korsblommiga";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.kors));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkrans:
                filterSettings[3] = "Kransblommiga";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.krans));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qlej:
                filterSettings[3] = "Lejon";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.lej));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qlin:
                filterSettings[3] = "Linväxter";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.lin));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qlinn:
                filterSettings[3] = "Linnea";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.linn));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qmal:
                filterSettings[3] = "Malväxter";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.mal));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qmall:
                filterSettings[3] = "Mållv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.mall));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qmar:
                filterSettings[3] = "Måre";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.mar));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qnak:
                filterSettings[3] = "Näck";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.nak));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qnas:
                filterSettings[3] = "Nässel";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.nas));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qnav:
                filterSettings[3] = "Näve";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.nav));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qnej:
                filterSettings[3] = "Nejlik";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.nej));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qole:
                filterSettings[3] = "Oleander";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ole));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qpot:
                filterSettings[3] = "Potatis";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.pot));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qran:
                filterSettings[3] = "Ranunkel";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ran));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qres:
                filterSettings[3] = "Reseda";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ran));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qros:
                filterSettings[3] = "Rosväxter";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ros));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qsand:
                filterSettings[3] = "Sand";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.sand));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qsil:
                filterSettings[3] = "Sileh";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.sil));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qspa:
                filterSettings[3] = "Sparris";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.spa));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qsli:
                filterSettings[3] = "Slide";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.sli));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qsny:
                filterSettings[3] = "Snylt";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.sny));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qsten:
                filterSettings[3] = "Stenbr";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.sten));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qstra:
                filterSettings[3] = "Strävb";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.stra));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qsyr:
                filterSettings[3] = "Syren";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.syr));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qtat:
                filterSettings[3] = "Tätört";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.tat));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qtor:
                filterSettings[3] = "Törel";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.tor));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qtrad:
                filterSettings[3] = "Träd";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.trad));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qtri:
                filterSettings[3] = "Trift";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.tri));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qvadd:
                filterSettings[3] = "Vädd";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.vadd));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qvall:
                filterSettings[3] = "Vallmo";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.vall));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qvand:
                filterSettings[3] = "Vände";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.vand));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qvat:
                filterSettings[3] = "Vattenväxt";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.vat));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qvatt:
                filterSettings[3] = "Vattenkl";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.vatt));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qvin:
                filterSettings[3] = "Vinde";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.vin));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qviol:
                filterSettings[3] = "Viol";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.viol));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qviv:
                filterSettings[3] = "Vivev";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.viv));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qtall:
                filterSettings[3] = "Tallv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.tall));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qvide:
                filterSettings[3] = "Videv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.vide));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkines:
                filterSettings[3] = "Kines";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.kines));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qmalva:
                filterSettings[3] = "Malva";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.malva));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qber:
                filterSettings[3] = "Berber";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ber));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qben:
                filterSettings[3] = "Benved";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ben));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qbra:
                filterSettings[3] = "Brakved";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.bra));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qhav:
                filterSettings[3] = "Havtorn";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.hav));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qrip:
                filterSettings[3] = "Rips";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.rip));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qalm:
                filterSettings[3] = "Almv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.alm));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qbok:
                filterSettings[3] = "Bokv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.bok));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qbjo:
                filterSettings[3] = "Bj";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.bjo));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qpors:
                filterSettings[3] = "Porsv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.pors));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qide:
                filterSettings[3] = "Ideg";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.ide));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qcyp:
                filterSettings[3] = "Cypr";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.cyp));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qfjall:
                filterSettings[3] = "Fjällväxter";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.fjall));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qbal:
                filterSettings[3] = "Balsa";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.bal));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qsol:
                filterSettings[3] = "Solv";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.sol));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qtib:
                filterSettings[3] = "Tibas";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.tib));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qlil:
                filterSettings[3] = "Liljev";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.lil));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qhas:
                filterSettings[3] = "Hassel";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.has));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qmyr:
                filterSettings[3] = "Myrl";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.myr));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkall:
                filterSettings[3] = "Kalla";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.kall));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qkavel:
                filterSettings[3] = "Kavel";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.kavel));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qorm:
                filterSettings[3] = "Ormb";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.orm));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qlas:
                filterSettings[3] = "Lås";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.las));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qfrak:
                filterSettings[3] = "Fräken";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.frak));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.qlumm:
                filterSettings[3] = "Lummer";
                menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.lumm));
                menu.findItem(R.id.filter_group).setIcon(R.drawable.ic_check_black_24dp);
                break;
            case R.id.filter_reset:
                resetFilter();
                break;
            case R.id.menu_sort_az:
                editor.putString("sort", "az");
                editor.apply();
                break;
            case R.id.menu_sort_za:
                editor.putString("sort", "za");
                editor.apply();
                break;
            case R.id.menu_sort_length:
                editor.putString("sort", "length_down");
                editor.apply();
                break;
            case R.id.menu_sort_length_rev:
                editor.putString("sort", "length_up");
                editor.apply();
                break;
            default:
                return true;
        }
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);
        RefineList();
        DisplayList(refinedList);

        //Visa "återställ filter" om filtret är aktiverat
        if (!filterSettings[0].equals("") || !filterSettings[1].equals("") || !filterSettings[2].equals("") || !filterSettings[3].equals(""))
            menu.findItem(R.id.filter_reset).setVisible(true);
        else
            menu.findItem(R.id.filter_reset).setVisible(false);

        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        }

//nollställ sökningen
        latestSearch = "";
        if (searchItem.isActionViewExpanded()) {
            searchItem.collapseActionView();
            Toast.makeText(getApplicationContext(), "Filter/sortera fungerar ej på sökresultat!", Toast.LENGTH_LONG).show();
        }
        return true;
    }
//-------------------------------------------------------------------------------------------------------------
    //metod för hanteringen av att automatisk hoppa till vald månad och/eller region
//-------------------------------------------------------------------------------------------------------------
    private void jumpToRegionMonth() {
        //Ska vyn filtrera automatiskt till vald månad?
        boolean jumpToActive = false;
        if (settings.getBoolean("jump_to_month", false) && !favoriteActive) {
            Calendar c = Calendar.getInstance();
            currentMonthInt = c.get(Calendar.MONTH) + 1;

            //letar upp "id" på layouten, så rätt månad blir markerad i menyn framöver
            resId = getResources().getIdentifier("m" + currentMonthInt, "string", packageName);
            String qfamily = getString(resId);
            currentMonthId = getResources().getIdentifier(qfamily, "id", packageName);
            //och den viktigaste tilldelningen
            if (currentMonthInt < 10)
                filterSettings[1] = "" + currentMonthInt;
            else if (currentMonthInt == 10)
                filterSettings[1] = "A";
            else if (currentMonthInt == 11)
                filterSettings[1] = "B";
            else
                filterSettings[1] = "C";

            //menyn finns ännu inte och när menyn skapas kommer denna månad bli vald automatiskt
            if (menu != null) {
                if (settings.getBoolean("jump_to_month", false) && !favoriteActive) {
                    menu.findItem(currentMonthId).setChecked(false);
                    resId = getResources().getIdentifier("n" + currentMonthInt, "string", packageName);
                    menu.findItem(R.id.filter_bloom).setTitle(getString(resId));
                    menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
                }
            }
        }

        //Ska vyn filtrera automatiskt till vald region?
        if (!(settings.getString("jump_to_region", "no").equals("no")) && !favoriteActive) {
            resId = getResources().getIdentifier(settings.getString("jump_to_region", "no"), "string", packageName);
            currentRegionChar = getString(resId);
            currentRegionId = getResources().getIdentifier("r" + currentRegionChar, "id", packageName);
            filterSettings[2] = currentRegionChar;
        }
    }
//---------------------------------------------------------------------------------------------------------
//Hanterar vad som sker vid bakåttryckning
//---------------------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        //kasta ner tangentbord
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        //är sidomeny öppen, så stäng bara den och gör inget mer
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        //nollställ filtrering om bakåttrycks eller avsluta activiteten
        else {
            // är vi i defaultläge avslutas aktiviteten. Detta villkor är definitionen på dafualtläge i bläddringsvyn
            // observera att region inte är ett kritere eller om månaden stämmer övrens med mobilen
            // detta pga av användarens val i settings
            if (isFlowerListActive() || (((filterSettings[1].equals("" + currentMonthInt)) || (currentMonthInt <= 12) || filterSettings[1].equals("")) && filterSettings[0].equals("") && filterSettings[3].equals("")
                    && !searchListActive && settings.getString("sort", "az").equals("az"))) {
                super.onBackPressed();
            }
            // om listan är filtrerad på något sätt --> nollställ allt
            else {
                searchListActive = false;
                resetFilter();

                //inget filter är aktivt
                if (isFlowerListActive()) {
                    DisplayList(flowerList);    //presentera blommorna
                }
                //annars har användaren gjort inställningar i settingsactivity
                else {
                    RefineList();
                    DisplayList(refinedList);
                }
            }
        }
    }

/*---------------------------------------------------------------------------------------------------------
Skapar den översta menyn
---------------------------------------------------------------------------------------------------------
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        //beroende på vilken skämupplösningen användaren har, så måste texten justeras
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        if (width < 720) {
            menu.findItem(main_filter).setIcon(R.drawable.ic_filter_list_white_24dp);
            menu.findItem(action_sort).setIcon(R.drawable.ic_sort_white_24dp);
        }
/*---------------------------------------------------------------------------------------------------------
Hantering sökning på växt
*/
//---------------------------------------------------------------------------------------------------------
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Define the listener
        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            //När användaren har trycket på sök eller stängt ner sökfunktionen
            //presentera slutresulatatet. strängen "latestSearch" innehåller alla inmatade tecken
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                if (latestSearch != null) {
                    //Söker by default i alla fyra sökområden
                    SharedPreferences settingsData = getSharedPreferences("settings", 0);
                    Boolean show_swe = settingsData.getBoolean(("swe"), true);
                    Boolean show_alt = settingsData.getBoolean(("alt"), true);
                    Boolean show_lat = settingsData.getBoolean(("lat"), true);
                    Boolean show_fam = settingsData.getBoolean(("fam"), true);
                    refinedList.clear(); //rensar listan först och så den inte bara adderar

                    //söker efter växer
                    for (Flower thisFlower : flowerList
                            ) {
                        if (show_swe) {
                            if (thisFlower.getName().toLowerCase().contains(latestSearch.toLowerCase().trim())) {
                                refinedList.add(thisFlower);
                            }
                        }
                        if (show_lat) {
                            if (thisFlower.getLatin().toLowerCase().contains(latestSearch.toLowerCase().trim())) {
                                if (!refinedList.contains(thisFlower)) {
                                    refinedList.add(thisFlower);
                                }
                            }
                        }
                        if (show_alt) {
                            if (thisFlower.getToxic().toLowerCase().contains(latestSearch.toLowerCase().trim())) {
                                if (!refinedList.contains(thisFlower)) {
                                    refinedList.add(thisFlower);
                                }
                            }
                        }
                        if (show_fam) {
                            if (thisFlower.getFamily().toLowerCase().contains(latestSearch.toLowerCase().trim())) {
                                if (!refinedList.contains(thisFlower)) {
                                    refinedList.add(thisFlower);
                                }
                            }
                        }
                    }
                    searchListActive = true;
                    resetFilter(); //eftersom refinedlist har ändrats i grunden, behövs alla filter nollställas
                    DisplayList(refinedList); //presentera slut resultatet
                }
                return true;  //stänger ner sökfunktionen
            }
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchListActive = true;
                return true;  // öppnar upp sökfunktionen
            }
        };

        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) { //va?
                searchListActive = true;
                return false;
            }
//---------------------------------------------------------------------------------------------------------
//är bara en temporär visning av listan medans man håller på att fylla i sökfältet
//---------------------------------------------------------------------------------------------------------
            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.equals("")) {
                    SharedPreferences settingsData = getSharedPreferences("settings", 0);
                    Boolean show_swe = settingsData.getBoolean(("swe"), true);
                    Boolean show_alt = settingsData.getBoolean(("alt"), true);
                    Boolean show_lat = settingsData.getBoolean(("lat"), true);
                    Boolean show_fam = settingsData.getBoolean(("fam"), true);
                    LinkedList<Flower> searchList = new LinkedList<Flower>();
                    for (Flower thisFlower : flowerList
                            ) {

                        if (show_swe) {
                            if (thisFlower.getName().toLowerCase().contains(s.toLowerCase().trim())) {
                                searchList.add(thisFlower);
                            }
                        }
                        if (show_lat) {
                            if (thisFlower.getLatin().toLowerCase().contains(s.toLowerCase().trim())) {
                                if (!searchList.contains(thisFlower)) {
                                    searchList.add(thisFlower);
                                }
                            }
                        }
                        if (show_alt) {
                            if (thisFlower.getToxic().toLowerCase().contains(s.toLowerCase().trim())) {

                                if (!searchList.contains(thisFlower)) {
                                    searchList.add(thisFlower);
                                }
                            }
                        }
                        if (show_fam) {
                            if (thisFlower.getFamily().toLowerCase().contains(s.toLowerCase().trim())) {
                                if (!searchList.contains(thisFlower)) {
                                    searchList.add(thisFlower);
                                }
                            }
                        }
                    }
                    //eftersom refinedlist har ändrats i grunden, behövs alla filter nollställas
                    resetFilter();

                    //presentera sökreulatatet för varje ifyllt tecken
                    DisplayList(searchList);

                    //spara strängen som användaren har skrivit in,
                    //så om sökningen är klar lågger strängen i latestSearch annars så kommer strängen s ersättas med ny tycken
                    latestSearch = s;
                }
                searchListActive = true; //är fortfarnde active, om användaren vill filtrerar/sortera sökresultatet
                return false; //stäng ner sökfunktionen
            }
        });
        this.menu = menu;

//----------------------------------------------------------------------------------------------
//om "hoppa till månad" är aktiv behöver den månaden bli ikryssat i menyn
//----------------------------------------------------------------------------------------------

        if (settings.getBoolean("jump_to_month", false) && !favoriteActive) {
            menu.findItem(currentMonthId).setChecked(false);
            resId = getResources().getIdentifier("n" + currentMonthInt, "string", packageName);
            menu.findItem(R.id.filter_bloom).setTitle(getString(resId));
            menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
        }

        //om "hoppa till region" är aktiv behöver den regionen bli vald i menyn
        if (!currentRegionChar.equals("no") && !favoriteActive) {
            menu.findItem(currentRegionId).setChecked(false);
            resId = getResources().getIdentifier("r" + currentRegionChar, "string", packageName);
            menu.findItem(R.id.filter_region).setTitle(getString(resId));
            menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
        }
        return true;
    }
//----------------------------------------------------------------------------------------------
//Hanterar kanpptryckningarna på navigationdrawer
//----------------------------------------------------------------------------------------------
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
//hem
        if (id == R.id.nav_home) {
            finish();
        }
//gå till browse --> gör inget om vi redan är i browse annars deaktivera favorite-vyn och starta om bbrowseactivity
        else if (id == R.id.nav_browse) {
            if (favoriteActive) {
                finish();
                Intent i = getIntent();
                i.putExtra("favo", false);
                startActivity(i);
            }
        }
//settings
        else if (id == R.id.nav_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            SharedPreferences settingsData = getSharedPreferences("settings", 0);
            //hmm, kan deta vara defaultläge med 4 kolum? minnet är bra men kort :)
            int starting_kol = settingsData.getInt("antalkolumner", 4);
            i.putExtra("start", starting_kol);
            //går man till Settingsactivity från browse kommer användaren inte kunna redigera region eller hoppa till aktuell månad
            i.putExtra("fromBrowse", true);
            startActivityForResult(i, 1);
        }
//gå till favoriter --> aktivera favorit-vyn och avsluta browseactivity för att sedan starta den igen
        else if (id == R.id.nav_favo) {
            if (!favoriteActive) {
                finish();
                Intent i = getIntent();
                i.putExtra("favo", true);
                startActivity(i);
            }
        }
//starta Quiz
        else if (id == R.id.nav_quiz) {
            Intent i = new Intent(getApplicationContext(), QuizMenu.class);
            startActivity(i);
        }
//stäng drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//------------------------------------------------------------------------------------------------------
//rensar alla inställningar som har gjorts i menyn
//------------------------------------------------------------------------------------------------------
    private void resetFilter() {
        filterSettings[1] = "";
        filterSettings[0] = "";
        filterSettings[2] = "";
        filterSettings[3] = "";
        menu.findItem(all_color).setChecked(false);
        menu.findItem(R.id.menu_sort_az).setChecked(false);
        menu.findItem(R.id.all_group).setChecked(false);
        menu.findItem(R.id.all_month).setChecked(false);
        menu.findItem(R.id.all_regions).setChecked(false);
        menu.findItem(R.id.filter_color).setTitle(getResources().getString(R.string.pick_color));
        menu.findItem(R.id.filter_color).setIcon(null);
        menu.findItem(R.id.filter_group).setTitle(getResources().getString(R.string.pick_family));
        menu.findItem(R.id.filter_group).setIcon(null);
        menu.findItem(R.id.filter_region).setTitle(getResources().getString(R.string.pick_region));
        menu.findItem(R.id.filter_region).setIcon(null);
        menu.findItem(R.id.filter_bloom).setTitle(getResources().getString(R.string.pick_month));
        menu.findItem(R.id.filter_bloom).setIcon(null);
        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("sort", "az");
        editor.apply();

        //om hoppa till aktuell månad är aktiv vid reset
        if (settings.getBoolean("jump_to_month", false) && !searchListActive && !favoriteActive) {

            if (currentMonthInt < 10)
                filterSettings[1] = "" + currentMonthInt;
            else if (currentMonthInt == 10)
                filterSettings[1] = "A";
            else if (currentMonthInt == 11)
                filterSettings[1] = "B";
            else
                filterSettings[1] = "C";

            menu.findItem(currentMonthId).setChecked(false);
            resId = getResources().getIdentifier("n" + currentMonthInt, "string", packageName);
            menu.findItem(R.id.filter_bloom).setTitle(getString(resId));
            menu.findItem(R.id.filter_bloom).setIcon(R.drawable.ic_check_black_24dp);
        }

        //om region har valts, när resetsmetoden ska exekuveras
        if (!currentRegionChar.equals("no") && !searchListActive && !favoriteActive) {
            filterSettings[2] = currentRegionChar;
            menu.findItem(currentRegionId).setChecked(false);
            resId = getResources().getIdentifier("r" + currentRegionChar, "string", packageName);
            menu.findItem(R.id.filter_region).setTitle(getString(resId));
            menu.findItem(R.id.filter_region).setIcon(R.drawable.ic_check_black_24dp);
        }

    }
}
