/*
	Vertyg som visar bild+namn i Gridview-layouten under browse
*/
package se.baraluftvapen.hansson.flora;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import se.baraluftvapen.hansson.flora.R;

public class ImageAdapter extends BaseAdapter {
    private final Context context;
    private final String[] idValues;
    private final String[] nameValues;
    private TextView textView;
    private ImageView imageView;

    public ImageAdapter(Context context, String[] idValues, String[] nameValues) {
        this.context = context;
        this.idValues = idValues;
        this.nameValues = nameValues;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.inflate_gridview, parent, false);
        }
        convertView.setBackgroundResource(R.drawable.bggg);
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings", 0);
        // hämtar antal kolumner som ska visas beroende på användaren har tagit för inställning
        // inställningarna ligger sparade i sharedpreferenses
        String PREF_NAME = "antalkolumner";
        int kol = sharedPreferences.getInt(PREF_NAME, 4);

        //beroende på skämupplösningen och antal kolumner, samt växtnamn längd, måste kapning av namn ske
        //för att namnet ska hamna på en rad i layouten
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        textView = (TextView) convertView.findViewById(R.id.grid_item_label);
        //om skärmupplösningen är mindre än 1080p
        //är bara två kolumner som ska visas så behövs ingen kapning av namnet 
        //när kapning sker ska det vara snyggt, inte bara kapa rakt av
        if (nameValues[position].length()>15 && kol>2 && width<1080){
            textView.setTextSize(10);
            if (nameValues[position].contains(" ")) {
                String[] tooLong = nameValues[position].split(" ");
                if ((tooLong[0]+tooLong[1]).length() > 15){
                    textView.setText(tooLong[0]);
                }
                else{
                    if (tooLong[1].equals("&")){
                        textView.setText(tooLong[0]);
                    }
                    else
                        textView.setText(tooLong[0]+" "+tooLong[1]);
                }
            }
            else{
                textView.setText(nameValues[position].substring(0, Math.min(nameValues[position].length(), 13)));
            }
        }
        //om skärmupplösningen är 1080p
        else if (nameValues[position].length()>15 && kol>2 && width==1080){
            textView.setTextSize(11);
            if (nameValues[position].contains(" ")) {
                String[] tooLong = nameValues[position].split(" ");
                if ((tooLong[0]+tooLong[1]).length() > 15){
                    textView.setText(tooLong[0]);
                }
                else{
                    if (tooLong[1].equals("&")){
                        textView.setText(tooLong[0]);
                    }
                    else
                        textView.setText(tooLong[0]+" "+tooLong[1]);
                }
            }
            else{
                textView.setText(nameValues[position].substring(0, Math.min(nameValues[position].length(), 13)));
            }
        }
        //om skärmupplösningen är större än 1080p
        else if (nameValues[position].length()>15 && kol>2 && width>1080){
            textView.setTextSize(12);
            if (nameValues[position].contains(" ")) {
                String[] tooLong = nameValues[position].split(" ");
                if ((tooLong[0]+tooLong[1]).length() > 15){
                    textView.setText(tooLong[0]);
                }
                else{
                    if (tooLong[1].equals("&")){
                        textView.setText(tooLong[0]);
                    }
                    else
                        textView.setText(tooLong[0]+" "+tooLong[1]);
                }
            }
            else{
                textView.setText(nameValues[position].substring(0, Math.min(nameValues[position].length(), 13)));
            }
        }
        //ingen kapning --> redigera bara textsize
        else {
            if (width<1080)
                textView.setTextSize(10);
            if (width==1080)
                textView.setTextSize(11);
            if (width>1080)
                textView.setTextSize(12);
            textView.setText(nameValues[position]);
        }

        // set image based on selected text
        imageView = (ImageView) convertView.findViewById(R.id.grid_item_image);
        String image_id;
        
        //beroende på skärmupplösning & hur många kolumner som ska visas, behövs det olika inställningar för
        //bl.a. bildkvaliten och upplösningen på bilden. Detta ger 12 olika scenarion
        //om skämupplösningen är 1080p
        if (width==1080) {
            if (kol == 3) {
              //ladda iamgae_id med tumnagel bilden, tumnagel laggar mindre än original storlek vid fler än 2 kolumner
                image_id = idValues[position]+ "_t";
                //änndra bildstorleken
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 800, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 650;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.setPadding(0, 10, 0, 0);
                imageView.requestLayout();
                textView.setVisibility(View.VISIBLE);
                textView.setTextSize(13);
            } else if (kol == 4) {
                image_id = idValues[position]+ "_t";
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 288, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 500;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.setPadding(0, 10, 0, 0);
                imageView.requestLayout();
                textView.setVisibility(View.VISIBLE);
            } else {
                //här används fulla bilden för att bilden blir såpass stor att det går se om det är tumnagel eller inte
                image_id = idValues[position];
                textView.setVisibility(View.VISIBLE);
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 288, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 900;
                imageView.setPadding(0, 10, 0, 0);
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.requestLayout();
            }
        }
        
        //om upplösningen är mellan 720p och 1080p
        else if (width<1080 && width>=720) {
            if (kol == 3) {
                image_id = idValues[position]+ "_t";
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 410;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.setPadding(0, 10, 0, 0);
                imageView.requestLayout();
                textView.setVisibility(View.VISIBLE);
                textView.setTextSize(13);
            } else if (kol == 4) {
                image_id = idValues[position] + "_t";
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 288, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 330;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.requestLayout();
                imageView.setPadding(0, 10, 0, 0);
                textView.setVisibility(View.VISIBLE);
            } else {
                image_id = idValues[position];
                textView.setVisibility(View.VISIBLE);
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 600;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.requestLayout();
                imageView.setPadding(0, 10, 0, 0);
            }
        }

        //skämupplösning på mindre än 720p
        else if (width<720) {
            if (kol == 3) {
                image_id = idValues[position]+ "_t";
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 288;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.setPadding(0, 10, 0, 0);
                imageView.requestLayout();
                textView.setVisibility(View.VISIBLE);
                textView.setTextSize(13);
            } else if (kol == 4) {
                image_id = idValues[position] + "_t";
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 288, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 230;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.requestLayout();
                imageView.setPadding(0, 10, 0, 0);
                textView.setVisibility(View.VISIBLE);
            } else {
                image_id = idValues[position];
                textView.setVisibility(View.VISIBLE);
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 288, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 400;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.requestLayout();
                imageView.setPadding(0, 10, 0, 0);
            }
        }

        //annars är skämupplösningen större än 1080p
        else {
            if (kol == 3) {
                image_id = idValues[position]+ "_t";
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 288, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 790;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.setPadding(0, 10, 0, 0);
                imageView.requestLayout();
                textView.setVisibility(View.VISIBLE);
                textView.setTextSize(13);
            } else if (kol == 4) {
                image_id = idValues[position] + "_t";
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 610;
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.requestLayout();
                imageView.setPadding(0, 10, 0, 0);
                textView.setVisibility(View.VISIBLE);
            } else {
                image_id = idValues[position];
                textView.setVisibility(View.VISIBLE);
                int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, context.getResources().getDisplayMetrics());
                imageView.getLayoutParams().height = 1200;
                imageView.setPadding(0, 10, 0, 0);
                imageView.getLayoutParams().width = dimensionInDp;
                imageView.requestLayout();
            }
        }

        //ändrar bildkvaliten. kol==3 är det optimala gällande kvalite och hackningar under bläddring
        int id = context.getResources().getIdentifier(image_id, "drawable", context.getPackageName());
        if (kol !=3) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;
            options.inPurgeable = true;
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
                    id, options);
            imageView.setImageBitmap(icon);
        }
        else
          imageView.setImageResource(id);

        return convertView;
    }

    @Override
    public int getCount() {
        return idValues.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


}