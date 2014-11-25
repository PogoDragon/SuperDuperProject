/**
 * Vocabulary Trainer
 * 
 * TODO alle Einstellungen z.B. F-Modus (an/aus), Timer (an/aus
 * TODO fragment_settings.xml layout
 */

package fhfl.tut.vocabtrainerproject;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends Fragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Initialisierung
		View view = inflater.inflate(R.layout.fragment_settings, container, false);
		
		return view;
	}
	
	
}
