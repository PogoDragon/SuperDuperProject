/**
 * Vocabulary Trainer
 * Mobile Computing WS14/15
 * Author: Prof. Dr. rer. nat. Tim Aschmoneit, Raul Böttcher, Philip Hermes
 * Last Update: 2014-11-13
 * History: 
 * 	2014-11-13: 	Button-Beschriftungen je nach Zustand der App wird angepasst;
 * 					TAGs angepasst (fhfl)
 * 					Verschachtelung bei showStats vereinfacht (currentBox)
 * 					Boxwechsel über OptionsMenu eingefügt
 *  2014-11-25:		gesamtes Projekt auf Fragment-Struktur umgebaut
 *  					das ViewFragment ist das neue View (vorher MainActivity)
 *  					das SettingsFragment ist für die Einstellungen
 *  				neues OptionMenu (mit Gruppen für das Ausblenden bestimmter Items)
 * 
 * TODO Dateiauswahl, überlegen ob extra Fragment oder mit in Settings, Speichern und Listen-clear beachten
 * TODO SettingsFragment (siehe SettingsFragment)
 * TODO Controller aufräumen (siehe Contoller)
 * TODO Userinterface (z.B. Wischen, Sprach-Eingabe/-Augabe, Lautstärkeknöpfe, Neigung, Taster(Bluetooth?), Blinzel(Gesichtserkennung))
 * TODO weiteres Fragment (Motivations-Statistik/ Prozent: richtig/falsch)
 * TODO Präsentation (FH-Rundgang Herbst 2015): Plakatt, einpflegen in Website für Rundgang	
 */

package fhfl.tut.vocabtrainerproject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	Controller controller;
	private static final String TAG = "fhfl.VocTrMain"; // Identifier For Log-Outputs
	private ViewFragment viewFragment;
	private SettingsFragment settingsFragment;
	private Menu menu;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	// Fragments initialisieren
    	viewFragment = new ViewFragment();
    	settingsFragment = new SettingsFragment();
    	
        // Controller initialisieren
    	controller = new Controller(viewFragment);

    	// File laden
    	controller.model.SetFileName(Environment.getExternalStorageDirectory().toString() + "/vocab/" + "SkinEngHochschule_ok_v2.txt");
    	Log.d(TAG, Environment.getExternalStorageDirectory().toString() + "/vocab/" + "SkinEngHochschule_kurz.txt");
    	
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, viewFragment)
                    .add(R.id.container, settingsFragment)
                    .hide(settingsFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu.setGroupVisible(R.id.menu_group_settings, false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id)
        {
        	case R.id.menu_save:
        		Log.d(TAG, "Save!");
            	controller.model.Save();
                break;
        	case R.id.menu_view:
        		Log.d(TAG, "show viewFragment");
        		getFragmentManager().beginTransaction()
        			.hide(settingsFragment)
    				.show(viewFragment)
    				.commit();
        		menu.setGroupVisible(R.id.menu_group_vocab, true);
        		menu.setGroupVisible(R.id.menu_group_settings, false);
        		break;
        	case R.id.menu_settings:
        		Log.d(TAG, "show settingsFragment");
        		getFragmentManager().beginTransaction()
    			.hide(viewFragment)
				.show(settingsFragment)
				.commit();
        		menu.setGroupVisible(R.id.menu_group_vocab, false);
        		menu.setGroupVisible(R.id.menu_group_settings, true);
            	break;
        	case R.id.box_1:
        		Log.d(TAG, "In Box 1 gehen");
            	controller.ChangeBox(1);
            	break;
        	case R.id.box_2:
        		Log.d(TAG, "In Box 2 gehen");
            	controller.ChangeBox(2);
            	break;
        	case R.id.box_3:
        		Log.d(TAG, "In Box 3 gehen");
            	controller.ChangeBox(3);
            	break;
        	case R.id.box_4:
        		Log.d(TAG, "In Box 4 gehen");
            	controller.ChangeBox(4);
            	break;
        	case R.id.box_5:
        		Log.d(TAG, "In Box 5 gehen");
            	controller.ChangeBox(5);
            	break;
        	default:
        		Log.d(TAG, "irgendwas im Menu kaputt");
        		break;
        }
		
        //return super.onOptionsItemSelected(item);
		return true;
    }
    
}
