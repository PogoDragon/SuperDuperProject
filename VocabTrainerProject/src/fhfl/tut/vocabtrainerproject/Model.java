/**
 * Vocabulary Trainer
 */

package fhfl.tut.vocabtrainerproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.util.Log;

public class Model {
	public class DataElement
    {   // values from file
        public String strQuestion = "Error";
        public String strAnswer = "Error";
        public int nBox = 0;
        public DateTime LastAccess = new DateTime();   // maybe use Calendar.getInstance() or new org.joda.time.DateTime()
        public int nAccessCount = 0;
        public String strActive = "a";  // active oder passiv -> aendert die Rolle Frage/Antwort
        public Boolean success = false;	// zum Überprüfen des Zeitformats von Str2
        // temporary values
        public Boolean bLastAnswerCorrect = true;

        public String StringToContent(String Str, int nMaxBoxCount)
        // erwartet einen CSV-String und überführt seinen Inhalt in die jeweiligen Member
        {
        	String defaultString = "Question ; Answer ; 1 ; 01.02.70 10:00:00 ; 0 ; a";
        	String[] defaultSplit = defaultString.split(";");
            String[] split = Str.split(";");
            // Default-Werte nutzen sofern nicht angegeben
            // Frage und Antwort müssen angegeben werden!
        	for(int i = 0; i < 6; i++)
        	{
        		if(i < split.length)
        		{
        			defaultSplit[i] = split[i];
        		}
        	}
           
            int nColumn = 0;
            String Str2;
            Boolean RetVal = true;
            Boolean bComment = false;
            String StrRetMessage = "ok";
            for(String s: defaultSplit)
            {
                Str2 = s.trim();
                switch (nColumn)
                {
                    case 0:
                        strQuestion = Str2;
                        if (strQuestion.startsWith("//"))
                        {
                            bComment = true;
                        }
                        break;
                    case 1:
                        strAnswer = Str2;
                        break;
                    case 2:
                        if (bComment)
                        {
                            nBox = nMaxBoxCount - 1;    // last box is comment box
                        }
                        else
                        {
                            try
                            {
                            	nBox = Integer.valueOf(Str2);
                            }
                            catch (Exception ex)
                            {
                                nBox = 0;       // Box Pool##
                                RetVal = false;
                            }

                            // box range check
                            if ((nBox < 0) || (nBox >= nMaxBoxCount))
                            {
                                nBox = 0;       // Box Pool##
                                RetVal = false;
                            }
                        }
                        break;
                    case 3:
                        if (bComment)
                        {
                            LastAccess = DateTime.now();
                        }
                        else
                        {
                        	//http://stackoverflow.com/questions/7756041/java-dateformat-most-convenient-elegant-way-to-validate-input-date-against-mu
                        	success = false;
                            try
                            {
                            	LastAccess = dtf.parseDateTime(Str2);		//!DateTime.TryParse(Str2, out LastAccess)
                            	Log.d(TAG,"DATUM---------> " + strQuestion + " " + LastAccess.toString());
                            	success = true;
                            }
                        	catch(Exception ex)
                            {
                        		Log.d(TAG, "parse Str2 not working ex: " + Str2);
                        		
                        		//Wenn Datum nicht geparst werden kann, dann default Datum nehmen
                        		try
                        		{
                        			LastAccess = dtf.parseDateTime("01.02.70 10:00:00");		
                        			Log.d(TAG,"DATUM--------->" + strQuestion + LastAccess.toString());
                        			success = true;
                        		}
                        		catch(Exception ex2)
                        		{
                        			Log.d(TAG, "parse Str2 not working ex2: " + Str2);
                        		}
                            }
                            if(!success)
                            {
                            	RetVal = false;
                            }
                        }
                        break;
                    case 4:
                        try
                        {
                            nAccessCount = Integer.valueOf(Str2);
                        }
                        catch (Exception ex)
                        {
                            nAccessCount = 0;
                            RetVal = false;
                            Log.d(TAG,"Exception: " + ex.toString());
                        }

                        // range check
                        if (nAccessCount < 0)
                        {
                            nAccessCount = 0;       // Box Pool##
                            RetVal = false;
                        }

                        break;
                    case 5:
                        if (Str2.equals("a") || Str2.equals("p"))
                        {
                            strActive = Str2;
                        }
                        else
                        {
                            RetVal = false;
                        }

                        break;

                }
                nColumn++;
            }

            if (!RetVal)
            {
                StrRetMessage = "Formatproblems:   " + System.getProperty("line.separator");
                StrRetMessage += "    In:  " + Str + System.getProperty("line.separator");
                StrRetMessage += "    Out:  " + ContentToString() + System.getProperty("line.separator");

                Log.v(TAG,"Debug: StringToContent() RetVal == false  ");
                Log.v(TAG,"    In:  " + Str);
                Log.v(TAG,"    Out:  " + ContentToString());
            }

            return StrRetMessage;
        }

        public String ContentToString()
        {
            String Str;

            Str = strQuestion + " ; " + strAnswer + " ; " + String.valueOf(nBox) + " ; " + String.valueOf(LastAccess.toString(dtf)) + " ; " + String.valueOf(nAccessCount) + " ; " + strActive;
            //Str = ((CDataElement)arData[nIndex]).strEnglish + " ; " +((CDataElement)arData[nIndex]).strGerman + " ; " + ((CDataElement)arData[nIndex]).nBox.ToString();
            return Str;
        }
    }
 
    // ------ public attributes
    public enum AccessMode { All2, Before, FalseSince };  //## public !!!

    // ----- protected attributes (accessible for controller)
    protected int sourceBox;      								// the current box
    protected DateTime TimeProgramStart = new DateTime();		// geändert 
    protected AccessMode ActAccessMode = AccessMode.Before; 	// controlls the search in a box in ElementList
    protected Interval[] TimeSpanForNextAccess;					// Interval from JodaTime  http://stackoverflow.com/questions/6581605/do-we-have-a-timespan-sort-of-class-in-java
    protected int AccessCount = 0;
    protected int AccessCountCorrect = 0;
    protected int AccessCountSinceBoxChange = 0;
    protected int AccessCountCorrectSinceBoxChange = 0;

    // ----- private attributes
    public ArrayList<DataElement> arData;        		// contains the elements of the actuell database   ##use generic collection class !!!
    private int ActuellElementIndex;
    private Random Rand = new Random();     // for random choice of questions, seed = timer        
    private String FileName;   				// without path
    private String Directory;  				// only path to local directory, no filename, include the last backslash "\"
    private String Path;					// zunächst für festen Pfad
    private DateTimeFormatter dtf;			// Zeitformat angepasst an "dd.mm.yy hh:mm:ss"
    private static final String TAG = "fhfl.VocTrModel";
	
 // ----- public methods
    public Model()
    {
    	dtf = DateTimeFormat.forPattern("dd.MM.yy HH:mm:ss");	// Kleingroßschreibung wichtig! große H's für 24-Stunden-Format
        arData = new ArrayList<DataElement>();
        ActuellElementIndex = -1;
        Log.d(TAG, "Model ist da!");
        
        
        //Datumscheck
        DateTime date = DateTime.now();
        String str = date.toString(dtf);
        Log.d("TAG", "Datumscheck: " + str);
        
        // Intervall-Array für Zugriffzeiten aauf Boxen anlegen
        int maxBoxCount = Controller.cBox.values().length;
        TimeSpanForNextAccess = new Interval[maxBoxCount];
        // Intervall-Array für Zugriffszeitpunkte der Boxen initialisieren
        for(int i = 0; i < maxBoxCount; i++)
        {
        	TimeSpanForNextAccess[i] = new Interval(TimeProgramStart, TimeProgramStart.plusHours(Controller.BoxHoursForNextAccess[i]));
        }
    }
    // Gibt die aktuelle Box zurück
    public int GetBox()
    {
    	
    	return sourceBox;
    }

    //Wird im Moment nicht benutzt
    public String GetDiretory()
    {
        return Directory;
    }
    
    //Wird im Moment nicht benutzt
    public String GetFileName()
    {
        return FileName;
    }

    public Boolean SetFileName(String ActualPathFileName)
    {
        //Directory = ActualPathFileName.Remove(ActualPathFileName.lastIndexOf('\\') + 1); // noch kein Plan
//    	String substring = ActualPathFileName.substring(ActualPathFileName.lastIndexOf('\\') + 1, ActualPathFileName.length() - 1);
//    	Directory = ActualPathFileName.replace(substring, "");
//        FileName = ActualPathFileName.substring(ActualPathFileName.lastIndexOf('\\') + 1);
        Path = ActualPathFileName;
        Log.d(TAG, Path);
        if ( !Load() ) return false;
        return true; 
    }

    public int GetBoxCountAll(int nBox)
    {
        return GetBoxCount(nBox, Model.AccessMode.All2);
    }

    public int GetAccessCount()
    {
        return AccessCount;
    }

    public int GetElementCount()
    {
        return arData.size();
    }

    public int GetBoxCountAccessible(int nBox)
    {
        return GetBoxCount(nBox, ActAccessMode);
    }

    public DateTime GetTimeProgramStart()
    {
        return TimeProgramStart;
    }

    public int GetAccessCountCorrect()
    {
        return AccessCountCorrect;
    }

    public int GetAccessCountSinceBoxChange()
    {
        return AccessCountSinceBoxChange;
    }

    public int GetAccessCountCorrectSinceBoxChange()
    {
        return AccessCountCorrectSinceBoxChange;
    }

    public AccessMode GetAccessMode()
    {
        return ActAccessMode;
    }

    // ----- protected and private methods
    protected Boolean Load()
    // 
    // laedt das aktuelle File (aus dem lokalen Verzeichnis) und zerlegt 
    // sie in die Datenelemente
    // Load darf nur einmalig aufgerufen werden 
    // 
    {
        String Str = "";
        String StrReadError = "";
        int nFormatErrorCounter = 0;
        Log.d(TAG,"CDataBase.Load()");
        int nMaxBoxCount = Controller.cBox.values().length;

        try
        {
            // read 
        	BufferedReader br;
            
        	br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(Path))));
        	// String muss vor Schleife leer initialisiert werden
            while ((Str = br.readLine()) != null)  	// in c# sr.peek()
            {
                Log.d(TAG, Str);
                Str.trim();

                if (Str.contains(";") && (Str.length() > 3))
                {
                    String Str2 = Str.replace("<CRLF>", System.getProperty("line.separator"));

                    DataElement Data = new DataElement();
                    String StrRet = Data.StringToContent(Str2, nMaxBoxCount);
                    if (!StrRet.equals("ok"))
                    {
                        nFormatErrorCounter++;
                        StrReadError += FileName + "   " + StrRet;
                    }
                    arData.add(Data);
                }
            }
            br.close();

            if (nFormatErrorCounter != 0)
            {
            	Log.d(TAG, "Es sind " + String.valueOf(nFormatErrorCounter) + " Formatfehler beim Laden aufgetreten !!!"); //besser Toast?
                Log.d(TAG, StrReadError);
            }
            return true;
        }
        catch (Exception ex)
        {
        	Log.d(TAG, ex.getMessage() + " Fehler beim Laden !!!");
            return false;
        }
    }

    protected Boolean Save()
    {
        Log.d(TAG, "CDataBase.Save()");

        String Str = "";
        Boolean RetVal = true;
        DataElement Data = new DataElement();

        try
        {
        	Log.d(TAG, "    wird gespeichert");
        	BufferedWriter bw = new BufferedWriter(new FileWriter(Path));
            bw.newLine();
            for (int k = 0; k < arData.size(); k++)
            {
                Data = (DataElement)arData.get(k);
                Str += Data.ContentToString() + "\r\n";
            }
            bw.write(Str);
            bw.close();
        }
        catch (Exception e1)
        {
        	Log.d(TAG, "Fehler beim Abspeichern: " + e1.getMessage());
            RetVal = false;
        }           

        return RetVal;
    }

    protected Boolean SelectNextElement(int nBox)
    // select next element (randomized or recent) and returns true if possible   
    {
        ArrayList<Integer> ElementsInBox;
        int Index = 0;
        int nCount;

        ElementsInBox = ElementList(nBox, ActAccessMode);  // only elements from fileindex 0

        if (ElementsInBox.size() > 0)
        {
            nCount = ElementsInBox.size();
        }
        else
        {
            return false;
        }

        Index = Rand.nextInt(nCount);
        ActuellElementIndex = (Integer)ElementsInBox.get(Index);    
        return true;
    }

    public String GetQuestion()
    {
        String Str;
        if (ActuellElementIndex != -1)
        {
            if (((DataElement)arData.get(ActuellElementIndex)).strActive.equals("a"))
            {
                Str = ((DataElement)arData.get(ActuellElementIndex)).strQuestion;
            }
            else
            {
                Str = ((DataElement)arData.get(ActuellElementIndex)).strAnswer;
            }
        }
        else
        {
            Str = "Error";
        }
        return Str;
    }

    public String GetAnswer()
    {
        String Str;
        if (ActuellElementIndex != -1)
        {
            if (((DataElement)arData.get(ActuellElementIndex)).strActive.equals("a"))
            {
                Str = ((DataElement)arData.get(ActuellElementIndex)).strAnswer;
            }
            else
            {
                Str = ((DataElement)arData.get(ActuellElementIndex)).strQuestion;
            }
        }
        else
        {
            Str = "Error";
        }
        return Str;
    }

    protected String GetActuellElementString()
    {
        String Str;
        if (ActuellElementIndex != -1)
        {
            Str = ((DataElement)arData.get(ActuellElementIndex)).ContentToString();
        }
        else
        {
            Str = "Error ActuellElementIndex" + ActuellElementIndex;
        }
        return Str;
    }

    protected void SetActuellElementQuestion(String Str)
    {
        if (ActuellElementIndex != -1)
        {
            if (((DataElement)arData.get(ActuellElementIndex)).strActive.equals("a"))
            {
                ((DataElement)arData.get(ActuellElementIndex)).strQuestion = Str;
            }
            else
            {
                ((DataElement)arData.get(ActuellElementIndex)).strAnswer = Str;
            }
        }
    }

    protected void SetActuellElementAnswer(String Str)
    {
        if (ActuellElementIndex != -1)
        {
            if (((DataElement)arData.get(ActuellElementIndex)).strActive.equals("a"))
            {
                ((DataElement)arData.get(ActuellElementIndex)).strAnswer = Str;
            }
            else
            {
                ((DataElement)arData.get(ActuellElementIndex)).strQuestion = Str;
            }                
        }
    }

    protected void SetActuellElementActive(String Str)
    {
        if (ActuellElementIndex != -1)
        {
            ((DataElement)arData.get(ActuellElementIndex)).strActive = Str;
        }
    }

    protected void SetActuellElementBox(int nBox)
    {
        if (ActuellElementIndex != -1)
        {
            ((DataElement)arData.get(ActuellElementIndex)).nBox = nBox;
        }
    }

    protected void SetActuellElementLastAccess()
    {
        if (ActuellElementIndex != -1)
        {
            ((DataElement)arData.get(ActuellElementIndex)).LastAccess = DateTime.now();
        }
    }

    protected void SetActuellElementAccessCount()
    {
        if (ActuellElementIndex != -1)
        {
            ((DataElement)arData.get(ActuellElementIndex)).nAccessCount++;
        }
    }

    protected void SetActuellElementLastAnswerCorrect(Boolean Val)
    {
        ((DataElement)arData.get(ActuellElementIndex)).bLastAnswerCorrect = Val;
    }

    private int GetBoxCount(int nBox, AccessMode Mode)       //##??
    // determine the elementcount of each box
    {
        ArrayList<Integer> ElementsInBox;

        ElementsInBox = ElementList(nBox, Mode);

        return ElementsInBox.size();
    }

    private ArrayList<Integer> ElementList(int nBox, AccessMode Mode)  // ## return value: work around to generic collections ***
    // returns an array with all database indices of the desired box
    {
        ArrayList<Integer> arTemp = new ArrayList<Integer>();

        for (int i = 0; i < arData.size(); i++)
        {
            if ( nBox == ( (DataElement) arData.get(i)).nBox )
            {
                //## a switch statement would be more suitable
                if (Mode == AccessMode.All2)
                {
                    arTemp.add(i);
                }
                if (Mode == AccessMode.Before)
                {
                    DateTime d = ((DataElement) arData.get(i)).LastAccess;
                    //TimeSpan ActSpan = DateTime.Now.Subtract(d);
                    Interval ActSpan = new Interval(d, DateTime.now());
                    
                    long actSpan = ActSpan.toDuration().getMillis();
                    long timeSpanforNext = TimeSpanForNextAccess[nBox].toDuration().getMillis();
                    
                    if (actSpan > timeSpanforNext)
                    {
                        arTemp.add(i);
                    }
                }
                if (Mode == AccessMode.FalseSince)
                {
                    if (((DataElement)arData.get(i)).bLastAnswerCorrect == false)
                    {
                        DateTime d = ((DataElement)arData.get(i)).LastAccess;
                        Interval ActSpan = new Interval(d, DateTime.now());
                        //eigentlich überflüssig, da auf größer 0 geprüft wird und die Zeitspanne immer größer 0 ist
                        if (ActSpan.toDuration().getMillis() > 0)
                        {
                            arTemp.add(i);
                        }
                    }
                }
            }
        }
        return arTemp;
    } 
}
