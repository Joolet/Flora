package se.baraluftvapen.hansson.flora;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class QuizPic extends AppCompatActivity {
    private LinkedList<Flower> flowerList;
    private LinkedList<Flower> refinedList = new LinkedList<Flower>();
    private TextView question_text;
    private TextView goto_next_q;
    private LinearLayout hidebar;
    private ImageView image1;
    private ImageView image2;
    private ImageView image3;
    private ImageView image4;
    private TextView score_text;
    private Context context;
    private Random rand;
    private String language = "";
    private String categoty_choice = "";
    private PopupWindow pw;
    private int noClicks = 4;
    private int score = 0;
    private int imagePosition;
    private int rightAnswer;

    String f1="Ek";
    String f2="Vildkaprifol";
    String f3="Ängsklocka";
    String f4="Förgätmigej";
    String f5="Murgröna";
    String f6="Liljekonvalj";
    String f7="Hårginst";
    String f8="Lin";
    String f9="Mosippa";
    String f10="Brunkulla";
    String f11="Fjällsippa";
    String f12="Gran";
    String f13="Åkerbär";
    String f14="Gullviva";
    String f15="Prästkrage";
    String f16="Linnea";
    String f17="Vit näckros";
    String f18="Kungsängslilja";
    String f19="Skogsstjärna";
    String f20="Kung Karls spira";
    String f21="Ljung";
    String f22="Mistel";
    String f23="Styvmorsviol";
    String f24="Ölandssolvända";
    String f25="Blåklint";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_pic);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        question_text = (TextView) findViewById(R.id.question);
        goto_next_q = (TextView) findViewById(R.id.GoToNextQ);
        hidebar = (LinearLayout) findViewById(R.id.hidebar);
        CreateList();
        image1 = (ImageView) findViewById(R.id.image1);
        image2 = (ImageView) findViewById(R.id.image2);
        image3 = (ImageView) findViewById(R.id.image3);
        image4 = (ImageView) findViewById(R.id.image4);
        score_text = (TextView) findViewById(R.id.score);
        Intent i = getIntent();
        categoty_choice = i.getExtras().getString("choice");
        language = i.getExtras().getString("language");
        context = getApplicationContext();

        //refinedList.clear();
        //om det är en kategori som ska visas, lägg till alla med matchande kategori
        if (i.getExtras().getBoolean("show_cat")) {
            for (Flower thisFlower : flowerList
                    ) {
                if (thisFlower.getCategory().contains(categoty_choice)) {
                    refinedList.add(thisFlower);
                }
            }
            //om "blanda alla" ska köras, lägg till no_flowers till refinedlist
        } else if (i.getExtras().getBoolean("show_all")) {
            if (!i.getExtras().getString("region").contains("Hela")) {
                String[] styckad = i.getExtras().getString("region").split(" ");
                styckad[0] = styckad[0].replace('ö', 'o').replace('å', 'a').replace('ä', 'a').replace('é', 'e').replace('Ä', 'A').replace('Ö', 'O').replace('Å', 'A');
                String region_char = getString(getResources().getIdentifier((styckad[0]), "string", getPackageName()));

                Iterator<Flower> it = flowerList.iterator();
                while (it.hasNext()) {
                    Flower s = it.next(); // must be called before you can call i.remove()
                    if (!s.getAreacode().contains(region_char)) {
                        it.remove();
                    }
                }

            }
            if (categoty_choice.contains("<")) {
                refinedList.addAll(flowerList);
            } else {
                int no_flowers = Integer.parseInt(categoty_choice);
                for (int j = 0; j < no_flowers; j++) {
                    refinedList.add(flowerList.get(j));
                }
            }

            //visa favoriter
        } else if (i.getExtras().getBoolean("show_fav")) {
            SharedPreferences favoData = getSharedPreferences("favolist", 0);
            for (Flower thisFlower : flowerList
                    ) {
                if (favoData.getBoolean(thisFlower.getID(), false)) {
                    refinedList.add(thisFlower);
                }
            }

            //visa landskapblommor
        } else if (i.getExtras().getBoolean("show_land")) {

            for (Flower thisFlower : flowerList
                    ) {
                String a = thisFlower.getName();
                if (a.equals(f1)||a.equals(f2)||a.equals(f3)||a.equals(f4)||a.equals(f5)||a.equals(f6)||a.equals(f7)||a.equals(f8)||a.equals(f9)||a.equals(f10)||a.equals(f11)||a.equals(f12)||a.equals(f13)||a.equals(f14)||a.equals(f15)||a.equals(f16)||a.equals(f17)
                        ||a.equals(f18)||a.equals(f19)||a.equals(f20)||a.equals(f21)||a.equals(f22)||a.equals(f23)||a.equals(f24)||a.equals(f25)){
                    refinedList.add(thisFlower);
                }
            }
            //visa familj
        }else if (i.getExtras().
                getBoolean("show_fam")
                )
        {
            //om trashfamilj ska visas
            if (categoty_choice.contains("Övriga")) {
                String trashString[] = new String[100];
                //trashfamiljen inkommer som en lång sträng sepererat med kommatecken
                trashString = i.getExtras().getString("id").split(", ");
                for (Flower thisFlower : flowerList
                        ) {
                    //statisk, finns 54 övriga familjer som inte uppnår 4 växter
                    for (int ij = 0; ij < 54; ij++) {
                        //tar bort 4 sista tecken, som är (xx)
                        if (trashString[ij].substring(0, trashString[ij].length() - 4).trim().equals(thisFlower.getFamily())) {
                            refinedList.add(thisFlower);
                        }
                    }
                }
            } else {
                //är det inte trashfamiljen är det bara tuta och köra
                for (Flower thisFlower : flowerList
                        ) {
                    if (thisFlower.getFamily().equals(categoty_choice)) {
                        refinedList.add(thisFlower);
                    }
                }
            }
        }

        if (!i.getExtras().getString("region").contains("Hela")) {
            String[] styckad = i.getExtras().getString("region").split(" ");
            styckad[0] = styckad[0].replace('ö', 'o').replace('å', 'a').replace('ä', 'a').replace('é', 'e').replace('Ä', 'A').replace('Ö', 'O').replace('Å', 'A');
            String region_char = getString(getResources().getIdentifier((styckad[0]), "string", getPackageName()));

            Iterator<Flower> it = refinedList.iterator();
            while (it.hasNext()) {
                Flower s = it.next(); // must be called before you can call i.remove()
                if (!s.getAreacode().contains(region_char)) {
                    it.remove();
                }
            }

        }

        //uppenbarligen måste refinedlist vara större än noll, men tar man bort det krashar det
        if (refinedList.size() > 3)

        {
            rand = new Random();
            //imagePosition = rand.nextInt(refinedList.size()-1);
            imagePosition = 0;
            int question = imagePosition + 1;
            //sätter titeln i appbar
            if ((categoty_choice.contains("0") || categoty_choice.contains("5")) && !categoty_choice.contains("Mina")) {
                getSupportActionBar().setTitle("Blanda alla" + " (" + question + "/" + refinedList.size() + ")");
            } else {
                getSupportActionBar().setTitle(categoty_choice + " (" + question + "/" + refinedList.size() + ")");
            }

            if (language.equals("latin")&& !categoty_choice.equals("Landskapsblommor")){
                question_text.setText(refinedList.get(imagePosition).getLatin());
                question_text.setTextSize(20);
            }
            else
                setquestion_text();

            initialImages();
        } else {
            Toast.makeText(getApplicationContext(), "Denna kombinationen mellan kategori och filter ger mindre än fyra växter", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /*
Läser in datafilen med blommor och skapar ett nytt objekt för varje blomma
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
        flowerList.removeFirst();  //den första är en dummy
        Collections.shuffle(flowerList);    //blanda ordningen, så varje frågeomgång blir unik
    }

    //en metod som slumpmässigt väljer ut texten som ska visas på knapparna
    private void initialImages() {
        int which_button = rand.nextInt(4); //denna slumpar var det rätta svaret ska vara (vilklen knapp asså)
        //beronde på vilken knapp som ska visa svaret behöves olika scenarion
        if (which_button == 0) {
            rightAnswer = 1;
            String image_id = refinedList.get(imagePosition).getID();
            int id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image1.setImageResource(id);
            //b1.setText(refinedList.get(imagePosition).getName()); //rätta svaret till knapp ett
            //en initiering till knapparna, så de är tvingade framöver att byta värde
            int i2 = imagePosition;
            int i3 = imagePosition;
            int i4 = imagePosition;
            //fixar unik text till knapp två
            while (imagePosition == i2) {
                i2 = rand.nextInt(refinedList.size());
            }
            //fixar unik text till knapp tre
            while (i3 == imagePosition || i3 == i2) {
                i3 = rand.nextInt(refinedList.size());
            }
            //fixar unik text till knapp fyra
            while (i4 == imagePosition || i4 == i2 || i4 == i3) {
                i4 = rand.nextInt(refinedList.size());
            }
            image_id = refinedList.get(i2).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image2.setImageResource(id);
            image_id = refinedList.get(i3).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image3.setImageResource(id);
            image_id = refinedList.get(i4).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image4.setImageResource(id);

        } else if (which_button == 1) {
            rightAnswer = 2;
            String image_id = refinedList.get(imagePosition).getID();
            int id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image2.setImageResource(id);
            //b2.setText(refinedList.get(imagePosition).getName());
            int i1 = imagePosition;
            int i3 = imagePosition;
            int i4 = imagePosition;
            while (imagePosition == i1) {
                i1 = rand.nextInt(refinedList.size());
            }
            while (i3 == imagePosition || i3 == i1) {
                i3 = rand.nextInt(refinedList.size());
            }
            while (i4 == imagePosition || i4 == i1 || i4 == i3) {
                i4 = rand.nextInt(refinedList.size());
            }
            image_id = refinedList.get(i1).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image1.setImageResource(id);
            image_id = refinedList.get(i3).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image3.setImageResource(id);
            image_id = refinedList.get(i4).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image4.setImageResource(id);
        } else if (which_button == 2) {
            rightAnswer = 3;
            String image_id = refinedList.get(imagePosition).getID();
            int id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image3.setImageResource(id);
            int i1 = imagePosition;
            int i2 = imagePosition;
            int i4 = imagePosition;
            while (imagePosition == i1) {
                i1 = rand.nextInt(refinedList.size());
            }
            while (i2 == imagePosition || i2 == i1) {
                i2 = rand.nextInt(refinedList.size());
            }
            while (i4 == imagePosition || i4 == i1 || i4 == i2) {
                i4 = rand.nextInt(refinedList.size());
            }
            image_id = refinedList.get(i2).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image2.setImageResource(id);
            image_id = refinedList.get(i1).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image1.setImageResource(id);
            image_id = refinedList.get(i4).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image4.setImageResource(id);
        } else if (which_button == 3) {
            rightAnswer = 4;
            String image_id = refinedList.get(imagePosition).getID();
            int id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image4.setImageResource(id);
            int i1 = imagePosition;
            int i3 = imagePosition;
            int i2 = imagePosition;
            while (imagePosition == i1) {
                i1 = rand.nextInt(refinedList.size());
            }
            while (i3 == imagePosition || i3 == i1) {
                i3 = rand.nextInt(refinedList.size());
            }
            while (i2 == imagePosition || i2 == i1 || i2 == i3) {
                i2 = rand.nextInt(refinedList.size());
            }
            image_id = refinedList.get(i2).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image2.setImageResource(id);
            image_id = refinedList.get(i3).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image3.setImageResource(id);
            image_id = refinedList.get(i1).getID();
            id = context.getResources().getIdentifier(image_id+"_t", "drawable", context.getPackageName());
            image1.setImageResource(id);
        }
    }
    //hanterar svarsknapparna
    public void image1Click(View view) {
        //minska antalet klick, från början har man fyra
        //används för att hålla reda på hur mycket poäng man ska få
        noClicks--;
        if (rightAnswer == 1) {
            image1.setBackground(getResources().getDrawable(R.drawable.pic_border));
            question_text.setText(refinedList.get(imagePosition).getName());
            goNext(); //var svaret rätt --> visa "nästa"-knapp
        } else {
            image1.setBackground(getResources().getDrawable(R.drawable.pic_border_wrong));
        }
    }
    public void image2Click(View view) {
        noClicks--;
        if (rightAnswer == 2) {
            image2.setBackground(getResources().getDrawable(R.drawable.pic_border));
            question_text.setText(refinedList.get(imagePosition).getName());
            goNext(); //var svaret rätt --> visa "nästa"-knapp
        } else {
            image2.setBackground(getResources().getDrawable(R.drawable.pic_border_wrong));
        }
    }
    public void image3Click(View view) {
        noClicks--;
        if (rightAnswer == 3) {
            image3.setBackground(getResources().getDrawable(R.drawable.pic_border));
            question_text.setText(refinedList.get(imagePosition).getName());
            goNext(); //var svaret rätt --> visa "nästa"-knapp
        } else {
            image3.setBackground(getResources().getDrawable(R.drawable.pic_border_wrong));
        }
    }
    public void image4Click(View view) {
        noClicks--;
        if (rightAnswer == 4) {
            image4.setBackground(getResources().getDrawable(R.drawable.pic_border));
            question_text.setText(refinedList.get(imagePosition).getName());
            goNext(); //var svaret rätt --> visa "nästa"-knapp
        } else {
            image4.setBackground(getResources().getDrawable(R.drawable.pic_border_wrong));
        }
    }

    //detta händer när man svarar rätt
    public void goNext() {
        score = score + noClicks * 10;
        score_text.setText("Poäng: " + score);
        //kontrollerar om man har kört alla frågorna
        if (imagePosition != refinedList.size() - 1) {
            goto_next_q.setVisibility(View.VISIBLE); //visar knappen "nästa"
            image1.setClickable(false);
            image2.setClickable(false);
            image3.setClickable(false);
            image4.setClickable(false);
            noClicks = 4;
        } else {
            finishQuiz();
        }
    }

    //sätter texten
    private void setquestion_text() {
        if (categoty_choice.equals("Landskapsblommor")) {
            String packagename = getPackageName();
            String[] family = new String[30];
            family[0] = refinedList.get(imagePosition).getName();
            if (family[0].contains(" ")) {
                family = family[0].split(" ");
                family[0] = family[0].trim();
            }
            family[0] = family[0].replace('ö', 'o').replace('å', 'a').replace('ä', 'a').replace('é', 'e').replace('Ä', 'A').replace('Ö', 'O').replace('Å', 'A');
            int resId = getResources().getIdentifier(family[0], "string", packagename);
            question_text.setText(getString(resId));
        }
        else
            question_text.setText(refinedList.get(imagePosition).getName());

    }

    //metod för knappen "nästa"
    //detta laddar nästa fråga
    public void GoToNextQ(View view) {
        goto_next_q.setVisibility(View.INVISIBLE); //göm knappen
        //reseta knapparna med färg osv.
        image1.setBackground(getResources().getDrawable(android.R.color.transparent));
        image2.setBackground(getResources().getDrawable(android.R.color.transparent));
        image3.setBackground(getResources().getDrawable(android.R.color.transparent));
        image4.setBackground(getResources().getDrawable(android.R.color.transparent));
        image1.setClickable(true);
        image2.setClickable(true);
        image3.setClickable(true);
        image4.setClickable(true);
        imagePosition++;
        initialImages();
        //titel i appbar behöver uppdateras så man kan se hur lång man har kommit i quizet
        int question = imagePosition + 1;
        if ((categoty_choice.contains("0") || categoty_choice.contains("5")) && !categoty_choice.contains("Mina")) {
            getSupportActionBar().setTitle("Blanda alla" + " (" + question + "/" + refinedList.size() + ")");
        } else {
            getSupportActionBar().setTitle(categoty_choice + " (" + question + "/" + refinedList.size() + ")");
        }
        if (language.equals("latin")&& !categoty_choice.equals("Landskapsblommor"))
            question_text.setText(refinedList.get(imagePosition).getLatin());
        else
            setquestion_text();    }

    //metod för popup, när quizet är över
    public void finishQuiz() {

        try {
            // We need to get the instance of the LayoutInflater
            LayoutInflater inflater = (LayoutInflater) QuizPic.this
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.quiz_popup,
                    (ViewGroup) findViewById(R.id.popup_1));
            pw = new PopupWindow(layout, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pw.showAtLocation(layout, Gravity.CENTER, 0, 0);
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                pw.setElevation(10);
            }
            //pw.setOutsideTouchable(true);
            //pw.setBackgroundDrawable(new BitmapDrawable());
            Button popUpCancel = (Button) layout.findViewById(R.id.close_popup);
            popUpCancel.setText("Avsluta");
            popUpCancel.setOnClickListener(cancelButton);
            PopupWindow.OnDismissListener onDismissListener;
            Button popUpSendData = (Button) layout.findViewById(R.id.ok_popup);
            popUpSendData.setText("Spela igen");
            popUpSendData.setOnClickListener(restart_quiz);
            TextView popupTV = (TextView) layout.findViewById(R.id.popuptext);
            TextView popupTV1 = (TextView) layout.findViewById(R.id.popuptext1);
            TextView popuptitle = (TextView) layout.findViewById(R.id.popuptitle);
            int maxscore = refinedList.size() * 30;
            popupTV1.setText("Du fick " + score + "p");
            popupTV.setText("Max poäng för denna kategori är " + maxscore + "p");

            if ((categoty_choice.contains("0") || categoty_choice.contains("5")) && !categoty_choice.contains("Mina")) {
                if (language.equals("latin")&& !categoty_choice.equals("Landskapsblommor"))
                    popuptitle.setText("Resultat för Blanda " + categoty_choice + "st, latin");
                else
                    popuptitle.setText("Resultat för Blanda " + categoty_choice + "st");
            } else {
                if (language.equals("latin")&& !categoty_choice.equals("Landskapsblommor"))
                    popuptitle.setText("Resultat för " + categoty_choice + ", latin");
                else
                    popuptitle.setText("Resultat för " + categoty_choice);
            }

            TextView topscore1 = (TextView) layout.findViewById(R.id.topscore1);
            TextView topscore2 = (TextView) layout.findViewById(R.id.topscore2);
            TextView topscore3 = (TextView) layout.findViewById(R.id.topscore3);

            //top tre bästa resultaten sparas/är sparande i minnet och varje kategori har sin unika nyckel (kategorins namn)
            SharedPreferences TopScore = getSharedPreferences(categoty_choice+"pic", 0);
            SharedPreferences.Editor editor = TopScore.edit();
            //hämta topplistan
            int mem_top1 = TopScore.getInt("1", 0);
            int mem_top2 = TopScore.getInt("2", 0);
            int mem_top3 = TopScore.getInt("3", 0);
            //beronde på var man hamnar på topplistan behöver man ev. justera topplistan
            if (score > mem_top1) {
                topscore1.setText("1. \t" + score);
                topscore2.setText("2. \t" + mem_top1);
                topscore3.setText("3. \t" + mem_top2);
                editor.putInt("1", score);
                editor.putInt("2", mem_top1);
                editor.putInt("3", mem_top2);
                editor.apply();
            } else if (score > mem_top2) {
                topscore1.setText("1. \t" + mem_top1);
                topscore2.setText("2. \t" + score);
                topscore3.setText("3. \t" + mem_top2);
                editor.putInt("2", score);
                editor.putInt("3", mem_top2);
                editor.apply();
            } else if (score > mem_top3) {
                topscore1.setText("1. \t" + mem_top1);
                topscore2.setText("2. \t" + mem_top2);
                topscore3.setText("3. \t" + score);
                editor.putInt("3", score);
                editor.apply();
            } else {
                topscore1.setText("1. \t" + mem_top1);
                topscore2.setText("2. \t" + mem_top2);
                topscore3.setText("3. \t" + mem_top3);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                pw.dismiss();
                //man ska inte kunna terycka utanför!
            }
        });

    }

    //trycker man på "spela igen" på popuppen
    //nollställer allt förutom refinedlist som är detsamma
    private final View.OnClickListener restart_quiz = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
            Collections.shuffle(refinedList);
            if (refinedList.size() > 0) {
                rand = new Random();
                //imagePosition = rand.nextInt(refinedList.size()-1);
                imagePosition = 0;
                initialImages();

                if (language.equals("latin")&& !categoty_choice.equals("Landskapsblommor"))
                    question_text.setText(refinedList.get(imagePosition).getLatin());
                else
                    setquestion_text();

                image1.setBackground(getResources().getDrawable(android.R.color.transparent));
                image2.setBackground(getResources().getDrawable(android.R.color.transparent));
                image3.setBackground(getResources().getDrawable(android.R.color.transparent));
                image4.setBackground(getResources().getDrawable(android.R.color.transparent));
                image1.setClickable(true);
                image2.setClickable(true);
                image3.setClickable(true);
                image4.setClickable(true);
                noClicks = 4;
                score = 0;
                score_text.setText("Poäng: 0");
                int question = imagePosition + 1;
                // <488 ja, ändra
                if ((categoty_choice.contains("0") || categoty_choice.contains("5")) && !categoty_choice.contains("Mina")) {
                    getSupportActionBar().setTitle("Blanda alla" + " (" + question + "/" + refinedList.size() + ")");
                } else {
                    getSupportActionBar().setTitle(categoty_choice + " (" + question + "/" + refinedList.size() + ")");
                }
            }
        }
    };
    //om man trycker på "avsluta"
    private final View.OnClickListener cancelButton = new View.OnClickListener() {
        public void onClick(View v) {
            pw.dismiss();
            finish();
        }
    };
}
