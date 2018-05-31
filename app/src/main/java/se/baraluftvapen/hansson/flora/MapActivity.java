/*
    Visar karta på utbredningsområde i Sverige
*/
package se.baraluftvapen.hansson.flora;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        String area = i.getExtras().getString("maparea");
        getSupportActionBar().setTitle("Utbredning, " + i.getExtras().getString("flowername"));

        //varje län är motsvarar en bild
        ImageView iva12 = (ImageView) findViewById(R.id.a12);
        ImageView iva = (ImageView) findViewById(R.id.a);
        ImageView ivb = (ImageView) findViewById(R.id.b);
        ImageView ivc = (ImageView) findViewById(R.id.c);
        ImageView ivd = (ImageView) findViewById(R.id.d);
        ImageView ive = (ImageView) findViewById(R.id.e);
        ImageView ivf = (ImageView) findViewById(R.id.f);
        ImageView ivg = (ImageView) findViewById(R.id.g);
        ImageView ivh = (ImageView) findViewById(R.id.h);
        ImageView ivi = (ImageView) findViewById(R.id.i);
        ImageView ivj = (ImageView) findViewById(R.id.j);
        ImageView ivk = (ImageView) findViewById(R.id.k);
        ImageView ivl = (ImageView) findViewById(R.id.l);
        ImageView ivm = (ImageView) findViewById(R.id.m);
        ImageView ivn = (ImageView) findViewById(R.id.n);
        ImageView ivo = (ImageView) findViewById(R.id.o);
        ImageView ivp = (ImageView) findViewById(R.id.p);
        ImageView ivq = (ImageView) findViewById(R.id.q);
        ImageView ivr = (ImageView) findViewById(R.id.r);
        ImageView ivs = (ImageView) findViewById(R.id.s);
        ImageView ivt = (ImageView) findViewById(R.id.t);
        ImageView outline = (ImageView) findViewById(R.id.outline);

        //hämta användarens upplösning
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        //såg konstigt ut i 1440p, så lite redigering
        if (width>=1080){
           outline.setPadding(0,0,0,70);
            iva.setPadding(0,0,0,70);
        }

        //grön färg är default sen innan. Byt här till röd eller gul beronde om växt är sällsynt
        if (area.contains("2")) {
            if ((area.charAt(area.indexOf("1") + 1)) == '.') {
                iva12.setImageResource(R.drawable.a12s);
            }
        } else iva12.setImageResource(R.drawable.a12r);

        if (area.contains("a")) {
            if ((area.charAt(area.indexOf("a") + 1)) == '.') {
                iva.setImageResource(R.drawable.as);
            }
        } else iva.setImageResource(R.drawable.ar);

        if (area.contains("b")) {
            if ((area.charAt(area.indexOf("b") + 1)) == '.') {
                ivb.setImageResource(R.drawable.bs);
            }
        } else ivb.setImageResource(R.drawable.br);

        if (area.contains("c")) {
            if ((area.charAt(area.indexOf("c") + 1)) == '.') {
                ivc.setImageResource(R.drawable.cs);
            }
        } else ivc.setImageResource(R.drawable.cr);

        if (area.contains("d")) {
            if ((area.charAt(area.indexOf("d") + 1)) == '.') {
                ivd.setImageResource(R.drawable.ds);
            }
        } else ivd.setImageResource(R.drawable.dr);

        if (area.contains("e")) {
            if ((area.charAt(area.indexOf("e") + 1)) == '.') {
                ive.setImageResource(R.drawable.es);
            }
        } else ive.setImageResource(R.drawable.er);

        if (area.contains("f")) {
            if ((area.charAt(area.indexOf("f") + 1)) == '.') {
                ivf.setImageResource(R.drawable.fs);
            }
        } else ivf.setImageResource(R.drawable.fr);

        if (area.contains("g")) {
            if ((area.charAt(area.indexOf("g") + 1)) == '.') {
                ivg.setImageResource(R.drawable.gs);
            }
        } else ivg.setImageResource(R.drawable.gr);

        if (area.contains("h")) {
            if ((area.charAt(area.indexOf("h") + 1)) == '.') {
                ivh.setImageResource(R.drawable.hs);
            }
        } else ivh.setImageResource(R.drawable.hr);

        if (area.contains("i")) {
            if ((area.charAt(area.indexOf("i") + 1)) == '.') {
                ivi.setImageResource(R.drawable.is);
            }
        } else ivi.setImageResource(R.drawable.ir);

        if (area.contains("j")) {
            if ((area.charAt(area.indexOf("j") + 1)) == '.') {
                ivj.setImageResource(R.drawable.js);
            }
        } else ivj.setImageResource(R.drawable.jr);

        if (area.contains("k")) {
            if ((area.charAt(area.indexOf("k") + 1)) == '.') {
                ivk.setImageResource(R.drawable.ks);
            }
        } else ivk.setImageResource(R.drawable.kr);

        if (area.contains("l")) {
            if ((area.charAt(area.indexOf("l") + 1)) == '.') {
                ivl.setImageResource(R.drawable.ls);
            }
        } else ivl.setImageResource(R.drawable.lr);

        if (area.contains("m")) {
            if ((area.charAt(area.indexOf("m") + 1)) == '.') {
                ivm.setImageResource(R.drawable.ms);
            }
        } else ivm.setImageResource(R.drawable.mr);

        if (area.contains("n")) {
            if ((area.charAt(area.indexOf("n") + 1)) == '.') {
                ivn.setImageResource(R.drawable.ns);
            }
        } else ivn.setImageResource(R.drawable.nr);

        if (area.contains("o")) {
            if ((area.charAt(area.indexOf("o") + 1)) == '.') {
                ivo.setImageResource(R.drawable.os);
            }
        } else ivo.setImageResource(R.drawable.or);

        if (area.contains("p")) {
            if ((area.charAt(area.indexOf("p") + 1)) == '.') {
                ivp.setImageResource(R.drawable.ps);
            }
        } else ivp.setImageResource(R.drawable.pr);

        if (area.contains("q")) {
            if ((area.charAt(area.indexOf("q") + 1)) == '.') {
                ivq.setImageResource(R.drawable.qs);
            }
        } else ivq.setImageResource(R.drawable.qr);

        if (area.contains("r")) {
            if ((area.charAt(area.indexOf("r") + 1)) == '.') {
                ivr.setImageResource(R.drawable.rs);
            }
        } else ivr.setImageResource(R.drawable.rr);

        if (area.contains("s")) {
            if ((area.charAt(area.indexOf("s") + 1)) == '.') {
                ivs.setImageResource(R.drawable.ss);
            }
        } else ivs.setImageResource(R.drawable.sr);

        if (area.contains("t")) {
            if ((area.charAt(area.indexOf("t") + 1)) == '.') {
                ivt.setImageResource(R.drawable.ts);
            }
        } else ivt.setImageResource(R.drawable.tr);

        //Stäng fönster
        RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.screen);
        rlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    //Stäng fönster
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}