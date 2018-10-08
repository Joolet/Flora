/*
	Hanterar inställningar
*/
package se.baraluftvapen.hansson.flora;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;

public class SettingsActivity extends AppCompatActivity {

    private TextView noKol_textview;						//visar aktuella värdet på antal kolumner
    private int noKol;										//antal kolumner som ska visas
    private boolean kol_change = false;						//skickas vidare till browse, för att veta om vyn ska ändras
    private SharedPreferences settingsData;					//inställningarnas lagringsplats
	private boolean fromBrowse;								//ser om man kom från browse eller huvudmenyn
	private Switch toMonth_switch;		//switch för "hoppa till aktuell månad"
    private Switch scrollSwitch;		//switch för "visa scrollbar"
    private CheckBox checkBox_swe;		//sökalternativ
    private CheckBox checkBox_lat;		//sökalternativ
    private CheckBox checkBox_fam;		//sökalternativ
    private CheckBox checkBox_alt;		//sökalternativ
	//googlekonto login
    private static final String TAG = "SignInActivity";		
    private static final int RC_SIGN_IN = 9001;
    private SignInButton signInButton;
    private GoogleApiClient mGoogleApiClient;
    private TextView mStatusTextView;
    private Button signOutButton;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        fromBrowse = i.getExtras().getBoolean("fromBrowse");
        settingsData = getSharedPreferences("settings", 0);
		
		//offlineSwitch = (Switch) findViewById(R.id.offlinemode);
        toMonth_switch = (Switch) findViewById(R.id.jumptomonth);
        scrollSwitch = (Switch) findViewById(R.id.ActivateScroll);
		SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        noKol_textview = (TextView) findViewById(R.id.textView2);
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signOutButton = (Button) findViewById(R.id.sign_out_button);
        mStatusTextView = (TextView) findViewById(R.id.mStatusTextView);
		
		//setup för sökalternativ spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        checkBox_fam = (CheckBox) findViewById(R.id.box_fam);
        checkBox_swe = (CheckBox) findViewById(R.id.box_swe);
        checkBox_lat = (CheckBox) findViewById(R.id.box_lat);
        checkBox_alt = (CheckBox) findViewById(R.id.box_alt);
        checkBox_alt.setChecked(settingsData.getBoolean(("alt"), true));
        checkBox_fam.setChecked(settingsData.getBoolean(("fam"), true));
        checkBox_lat.setChecked(settingsData.getBoolean(("lat"), true));
        checkBox_swe.setChecked(settingsData.getBoolean(("swe"), true));

		//från browse --> val av region är inte tillgänglig
		//pga när man kommer tillbaka till browse, uppstår problem med att uppdatera och visa aktuell lista
        if (fromBrowse) {
            spinner.setEnabled(false);
            TextView tv_display = (TextView) findViewById(R.id.disable_settings);
            tv_display.setVisibility(View.VISIBLE);
        }
		
		//setup för "hoppa till region" spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.region_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
        assert spinner != null;
        spinner.setAdapter(adapter);
        spinner.setSelection(settingsData.getInt("jump_to_region_position", 0));	//hämta och kryssa redan vald region sen tidigare
		
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                Object item = arg0.getItemAtPosition(arg2);
                if (item != null) {
                    SharedPreferences.Editor editor = settingsData.edit();
                    editor.putInt("jump_to_region_position", arg2);	//lagra vilken som är vald
					//hela sverige ska visas
                    if (item.toString().contains("Hela")) {
                        editor.putString("jump_to_region", "no");
                        editor.apply();
                    }
					// annars räcker det att lagra första namnet i region
					else {
                        String[] styckad = item.toString().split(" ");
                        styckad[0] = styckad[0].replace('ö', 'o').replace('å', 'a').replace('ä', 'a').replace('é', 'e').replace('Ä', 'A').replace('Ö', 'O').replace('Å', 'A');
                        editor.putString("jump_to_region", styckad[0].trim());
                    }
                    editor.apply();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

		//från browse --> val att hoppa till aktuell månad är inte tillgänglig
        if (fromBrowse) {
            toMonth_switch.setEnabled(false);
            TextView tv_display = (TextView) findViewById(R.id.disable_settings);
            tv_display.setVisibility(View.VISIBLE);
        }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

		//initiering av switch
        if (settingsData.getBoolean("jump_to_month", false)) {
            toMonth_switch.setChecked(true);
        } else {
            toMonth_switch.setChecked(false);
        }

        //initiering av switch
        if (settingsData.getBoolean("scroll", false)) {
            scrollSwitch.setChecked(true);
        } else {
            scrollSwitch.setChecked(false);
        }
/*
        //initiering av switchen, ska den vara på eller av
        if (settingsData.getBoolean("offlinemode", false)) {
            offlineSwitch.setChecked(true);
        } else {
            offlineSwitch.setChecked(false);
        }
*/
        //hoppa till månad switch
        toMonth_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    SharedPreferences.Editor editor = settingsData.edit();
                    editor.putBoolean("jump_to_month", true);
                    editor.apply();

                } else {
                    SharedPreferences.Editor editor = settingsData.edit();
                    editor.putBoolean("jump_to_month", false);
                    editor.apply();
                }
            }
        });

        //Scroll Switch
        scrollSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                kol_change = true;
                if (isChecked) {
                    SharedPreferences.Editor editor = settingsData.edit();
                    editor.putBoolean("scroll", true);
                    editor.apply();
                } else {
                    SharedPreferences.Editor editor = settingsData.edit();
                    editor.putBoolean("scroll", false);
                    editor.apply();
                }
            }
        });
/*
        offlineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    SharedPreferences.Editor editor = settingsData.edit();
                    editor.putBoolean("offlinemode", true);
                    editor.apply();

                } else {
                    SharedPreferences.Editor editor = settingsData.edit();
                    editor.putBoolean("offlinemode", false);
                    editor.apply();
                }
            }
        });
*/
        //visa antal kolumener
		settingsData = getSharedPreferences("settings", 0);
        noKol = settingsData.getInt("antalkolumner", 4);
        assert seekBar != null;
        seekBar.setProgress(noKol - 2);
        noKol_textview.setText("Antal kolumner: " + noKol);

		//google signin button
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

		//inloggad eller
        if (settingsData.getString("inloggad", "no").equals("no")) {
            mStatusTextView.setText("Utloggad");
            signInButton.setVisibility(View.VISIBLE);
            mStatusTextView.setVisibility(View.GONE);
            signOutButton.setVisibility(View.GONE);
        } else {
            mStatusTextView.setText("Du är inloggad som: " + settingsData.getString("inloggad", "no"));
            signInButton.setVisibility(View.GONE);
            mStatusTextView.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
        }

		//spara antal kolumner som ska visas o browse
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = noKol;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue + 2;
                noKol_textview.setText("Antal kolumner: " + progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), "Started tracking seekbar", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                noKol_textview.setText("Antal kolumner: " + progress);
                SharedPreferences.Editor editor = settingsData.edit();
                editor.putInt("antalkolumner", progress);
                editor.apply();
                kol_change = true;	
            }
        });
    }

	//avsluta settingsactivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (kol_change) {
                    Intent intent = new Intent();
                    intent.putExtra("changed", true);
                    setResult(RESULT_OK, intent);
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	//avsluta settingsactivity
    public void onBackPressed() {
        if (kol_change) {						//kolla om antalet kolumner/scrollbar har ändrats 
            Intent intent = new Intent();
            intent.putExtra("changed", true);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

	//när inloggning har skett
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            SharedPreferences.Editor editor = settingsData.edit();
            editor.putString("inloggad", acct.getDisplayName());		//lagra användarens namn
            editor.putString("inloggad_id", acct.getId());				//lagra användarens unika id
            editor.apply();
            signInButton.setVisibility(View.GONE);
            mStatusTextView.setVisibility(View.VISIBLE);
            signOutButton.setVisibility(View.VISIBLE);
            mStatusTextView.setText("Du är inloggad som: " + settingsData.getString("inloggad", "no"));
            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }

    public void signOutClick(View v) {
        signOut();
    }

	//logga ut från googlekonto
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        mStatusTextView.setText("Utloggad");
                        SharedPreferences.Editor editor = settingsData.edit();
                        editor.putString("inloggad", "no");				//logga ut användaren från minnet
                        editor.putString("inloggad_id", "no");			//logga ut användaren från minnet
                        editor.apply();
                        signInButton.setVisibility(View.VISIBLE);
                        mStatusTextView.setVisibility(View.GONE);
                        signOutButton.setVisibility(View.GONE);
                    }
                });
    }

	//fyra checkboxar för fyra val av sökalternativ
    public void box_swe(View view) {
        boolean value;
        value = checkBox_swe.isChecked();
        SharedPreferences.Editor editor = settingsData.edit();
        editor.putBoolean("swe", value);
        editor.apply();
    }

    public void box_lat(View view) {
        boolean value;
        value = checkBox_lat.isChecked();
        SharedPreferences.Editor editor = settingsData.edit();
        editor.putBoolean("lat", value);
        editor.apply();
    }
    public void box_alt(View view) {
        boolean value;
        value = checkBox_alt.isChecked();
        SharedPreferences.Editor editor = settingsData.edit();
        editor.putBoolean("alt", value);
        editor.apply();
    }
    public void box_fam(View view) {
        boolean value;
        value = checkBox_fam.isChecked();
        SharedPreferences.Editor editor = settingsData.edit();
        editor.putBoolean("fam", value);
        editor.apply();
    }

}