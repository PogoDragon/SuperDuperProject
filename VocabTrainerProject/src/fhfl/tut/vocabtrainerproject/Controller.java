/**
 * Vocabulary Trainer
 * 
 * TODO SM ordentlich machen (ruft sich selber auf <- unschön)
 * TODO SM hat keinen Que (android Message Handler <- Warteschlange mit drin), vll post-Message
 * TODO Timer in SM, der z.B. nach 5 Sekunden los schickt (z.B. nächste Frage)
 */

package fhfl.tut.vocabtrainerproject;

import fhfl.tut.vocabtrainerproject.ViewFragment.ShowEvent;

import android.util.Log;

public class Controller {
	 // ----- public
    public enum Event { Start, BoxChanged, BoxNext, ShowAnswer, Correct, Wrong, MarkQuestion, Time, AccessMode, ShutDown};
    public enum cBox { POOL, BOX1, BOX2, BOX3, BOX4, BOX5, EASY, PROOF, INTERNAL_COMMENT };   // last box must always be the comment box, comment box is not active
    public Model model;

    // ----- private
    private enum States { Start, WaitForBoxChange, QuestionShown, AnswerShown };
    private States state;   // the state variable
    private int runCount;  // 
    private static final String TAG = "fhfl.VocTrController"; // Identifier For Log-Outputs
    private ViewFragment viewFragment;

    // ## Arrays with classes for Box dependent information (exclude grafic)  ## transfer to CBox
    int[] NextBoxIfAnswerIsCorrect = { cBox.POOL.ordinal(),  cBox.BOX2.ordinal(), cBox.BOX3.ordinal(), cBox.BOX4.ordinal(), cBox.BOX5.ordinal(), cBox.BOX5.ordinal(), cBox.EASY.ordinal(), cBox.PROOF.ordinal()};
    int[] NextBoxIfAnswerIsWrong =   { cBox.BOX1.ordinal(),  cBox.BOX1.ordinal(), cBox.BOX1.ordinal(), cBox.BOX2.ordinal(), cBox.BOX3.ordinal(), cBox.BOX4.ordinal(), cBox.EASY.ordinal(), cBox.PROOF.ordinal()};
                                      //      POOL                    BOX1                2                   3                   4                   Box5               EASY               PROOF    
    int[] NextBoxIfBoxChange = {       cBox.BOX1.ordinal(),  cBox.BOX2.ordinal(), cBox.BOX3.ordinal(), cBox.BOX4.ordinal(), cBox.BOX5.ordinal(), cBox.BOX1.ordinal(), cBox.BOX1.ordinal(), cBox.BOX1.ordinal()};
    static int[] BoxHoursForNextAccess =    {          0,                      24,                170,             340,                780,                    1560,               0,                  0,                      1,              170, 0 }; // ## wozu die letzten deri Einträge ?
    
    // ----- public methods
    public Controller(ViewFragment viewFragment)
    {
    	this.viewFragment = viewFragment;
        state = States.Start;
        runCount = 0;
        model = new Model();
        Log.d(TAG, "Controller ist da!");
    }

    // the Statemachine
	public void Sm(Event Input, int Para1, int Para2)
    {
        Log.d(TAG,"CBrain.Sm() Start: State: " + state.toString() + "  Message: " + Input.toString() + "  Para1: " + Integer.toString(Para1) + "  Para2: " + Integer.toString(Para2));
        Log.d(TAG,"ActAccessMode: " + model.GetAccessMode() + "  TimeSpanForNextAccess: " + model.TimeSpanForNextAccess);

        String Str;

        // first the any states 
        switch (Input)
        {
            case ShutDown:
            	model.Save();
                break;

            case AccessMode:
                if (Para1 == 1)
                {
                    model.ActAccessMode = Model.AccessMode.FalseSince;
                }
                else
                {
                    model.ActAccessMode = Model.AccessMode.Before;
                }
                SmNextQuestion();
                break;
            default:
            	break;
        }

        // all other states
        switch (state)
        {
            case Start:
                if (Input == Event.Start)
                {
                    model.sourceBox = cBox.BOX1.ordinal(); 
                    state = States.WaitForBoxChange;
                    
//                    for (DataElement data : model.arData) {
//						Log.d(TAG, "Element: " + data.ContentToString());
//					}
                    
                    Sm(Event.BoxChanged, 1, 0);             // recursive call
                }
                break;

            case WaitForBoxChange:
                switch (Input)
                {
                    case Wrong:  
                        Sm(Event.BoxChanged, NextBoxIfBoxChange[model.sourceBox], 0);
                        break;

                    case BoxChanged:
                        model.sourceBox = Para1;             
                          
                        if (model.sourceBox == cBox.BOX1.ordinal())
                        {
                            runCount++;
                            Log.d(TAG,"    runCount: " + runCount + "   Box: " + model.sourceBox);
                            if ((runCount % 2) == 0)
                            {
                                Sm(Event.AccessMode, 1, 0);  // switch to FalseSince  ##direkt eintragen, keine Message mehr notwendig
                                break;
                            }
                            else
                            {
                                Sm(Event.AccessMode, 0, 0);  // switch to Before
                                break;
                            }
                        }

                        model.AccessCountSinceBoxChange = 0;
                        model.AccessCountCorrectSinceBoxChange = 0;
                        //model.TimeSpanForNextAccess = new Interval(model.TimeProgramStart, model.TimeProgramStart.plusHours(BoxHoursForNextAccess[model.sourceBox]));
                        SmNextQuestion();                      
                        break;
                     default:
                    	 break;
                }
                break;

            case QuestionShown:
                switch (Input)
                {
                    case Correct:
                    	viewFragment.showIt(ShowEvent.Answer);
                        
                        state = States.AnswerShown;
                        break;

                    case Wrong:    // -> box change
                        state = States.WaitForBoxChange;  // attention: only SendMessage allowed 
                        Sm(Event.BoxChanged, NextBoxIfBoxChange[model.sourceBox], 0);
                        break;

                    case BoxChanged:
                        state = States.WaitForBoxChange;  // attention: only SendMessage allowed 
                        Sm(Event.BoxChanged, Para1, 0);
                        break;
                    default:
                    	break;
                }
                break;

            case AnswerShown:
                switch (Input)
                {
                    case Correct:
                        int n;
                        if ( model.ActAccessMode == Model.AccessMode.FalseSince ) 
                        {  // no box change in this mode
                            n = model.sourceBox;
                        }
                        else
                        {
                            n = NextBoxIfAnswerIsCorrect[model.sourceBox];
                        }
                        Log.d(TAG, "    Antwort korrekt, Voc wird einsortiert in Box: " + cBox.values()[n]);
                        model.SetActuellElementBox(n);
                        model.SetActuellElementLastAnswerCorrect(true);
                        model.SetActuellElementLastAccess();
                        model.SetActuellElementAccessCount();
                        model.AccessCount++;
                        model.AccessCountCorrect++;
                        model.AccessCountSinceBoxChange++;
                        model.AccessCountCorrectSinceBoxChange++;
                        SmNextQuestion();
                        break;

                    case Wrong:
                        if (model.ActAccessMode == Model.AccessMode.FalseSince)
                        {  // no box change in this mode
                            n = model.sourceBox;
                        }
                        else
                        {
                            n = NextBoxIfAnswerIsWrong[model.sourceBox];
                        }
                        Log.d(TAG,"    Antwort nicht korrekt, Voc wird einsortiert in Box: " + cBox.values()[n]);
                        model.SetActuellElementBox(n);
                        model.SetActuellElementLastAnswerCorrect(false);
                        model.SetActuellElementLastAccess();
                        model.SetActuellElementAccessCount();
                        model.AccessCount++;
                        model.AccessCountSinceBoxChange++;
                        SmNextQuestion();
                        break;

                    case BoxChanged:
                        state = States.WaitForBoxChange;  // attention: only SendMessage allowed 
                        Sm(Event.BoxChanged, Para1, 0);
                        break;

                    case MarkQuestion:
                        Str = model.GetQuestion();
                        Str = Str + " # ";
                        model.SetActuellElementQuestion(Str);
                        viewFragment.showIt(ShowEvent.NewQuestion);
                        break;
                    default:
                    	break;
                 }
                break;

            default:
                break;
        }
        Log.d(TAG,"CBrain.Sm() End: new State: " + state.toString());
        Log.d(TAG,"    ActAccessMode: " + model.ActAccessMode + "  TimeSpanForNextAccess: " + model.TimeSpanForNextAccess);
        Log.d(TAG,"");
    }
	
	// Zum direkten Wechsel in eine bestimmte Box aus dem View heraus
    public void ChangeBox(int nBox)
    {
    	Sm(Event.BoxChanged, nBox, 0);
    }

    // ----- private methods
    private void SmNextQuestion()
    {
        Log.d(TAG,"    SmNextQuestion()");
        Log.d(TAG,"    Element (Aktualisiertes): " + model.GetActuellElementString());
        if (!model.SelectNextElement(model.sourceBox))
        {
        	viewFragment.showIt(ShowEvent.BoxEmpty);
            state = States.WaitForBoxChange;
        }
        else
        {
        	viewFragment.showIt(ShowEvent.NewQuestion);
            Log.d(TAG,"    Element: " + model.GetActuellElementString());
            state = States.QuestionShown;
        }
    }
}
