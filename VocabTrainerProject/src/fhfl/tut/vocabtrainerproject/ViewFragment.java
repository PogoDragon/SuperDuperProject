/**
 * Vocabulary Trainer
 */

package fhfl.tut.vocabtrainerproject;

import fhfl.tut.vocabtrainerproject.Model.AccessMode;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ViewFragment extends Fragment {

	// ----- private
	private TextView tvBoxNr;
	private TextView tvBoxCount;
	private TextView tvQuestion;
	private TextView tvAnswer;
	private Button btCorrect;
	private Button btWrong;
	private Controller controller;
	private static final String TAG = "fhfl.VocTrMainFrag"; // Identifier For Log-Outputs
	
	// ----- public
	public enum ShowEvent { NewQuestion, Answer, BoxChanged, BoxEmpty };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//Initialisierung
		MainActivity activity = (MainActivity) getActivity();
		controller = activity.controller;
		
		View view = inflater.inflate(R.layout.fragment_main, container, false);
		
		// GUI Elemente
		tvBoxNr = (TextView)view.findViewById(R.id.tvBoxNr);
    	tvBoxCount = (TextView)view.findViewById(R.id.tvBoxCount);
    	tvQuestion = (TextView)view.findViewById(R.id.tvQuestion);
    	tvAnswer = (TextView)view.findViewById(R.id.tvAnswer);
    	btCorrect = (Button)view.findViewById(R.id.btCorrect);
    	btWrong = (Button)view.findViewById(R.id.btWrong);
    	
    	// Controller StateMachine auf Start
    	controller.Sm(Controller.Event.Start, 0, 0 );
    	
    	// ClickListener für die Buttons
        btCorrect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				controller.Sm(Controller.Event.Correct, 0, 0);
			}
		});
        
        btWrong.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				controller.Sm(Controller.Event.Wrong, 0, 0);
			}
		});
        
		return view;
	}
	
	// Methode für die Anzeige - Wird vom Controller aufgerufen
    public void showIt(ShowEvent para)
    {
    	Log.d(TAG,para.toString());
        switch (para)
        {
            case NewQuestion:
            	// Was geschehen soll, wenn einen neue Frage kommt
                tvQuestion.setText(controller.model.GetQuestion());
                tvAnswer.setText("");
                showStats();
                btCorrect.setEnabled(true);
                btCorrect.setText("Zeige Antwort");
                btWrong.setText("Nächste Box");
                Log.d(TAG,"Show new Question");
                break;
            case Answer:
            	// Was geschehen soll, wenn eine Antwort kommt
                tvAnswer.setText(controller.model.GetAnswer());
                showStats();
                btCorrect.setText("Richtig");
                btWrong.setText("Falsch");
                Log.d(TAG,"Show Answer");
                break;
            case BoxChanged:
            	// Was geschehen soll, wenn die Box gewechselt wird
            	showStats();
                Log.d(TAG,"Change Box");
                break;
            case BoxEmpty:
            	// Was geschehen soll, wenn die derzeitige Box leer ist
                tvQuestion.setText("Keine Fragen in aktueller Box verfügbar, bitte andere Box waehlen !!!");
                tvAnswer.setText("");
                showStats();
                btCorrect.setEnabled(false);
                btWrong.setText("Nächste Box");
                Log.d(TAG,"Box Empty");
                break;
        }
    } 
    
    // Methode für die Statistik - Wird von showIt() aufgerufen
    private void showStats()
    {
    	// Derzeitige Box bekommen
    	int currentBox = controller.model.GetBox();
    	tvBoxNr.setText("Box " + Integer.toString(currentBox));
    	// Den AccessMode überprüfen, falls er auf "Before" steht die normale Anzeige verwenden
        if(controller.model.GetAccessMode() == AccessMode.Before)
        	tvBoxCount.setText(controller.model.GetBoxCountAccessible(currentBox) + "/" + controller.model.GetBoxCountAll(currentBox));
        else
        	tvBoxCount.setText("F " + controller.model.GetBoxCountAccessible(currentBox) + "/" + controller.model.GetBoxCountAll(currentBox));
    }
}
