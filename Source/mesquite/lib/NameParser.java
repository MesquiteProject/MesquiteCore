package mesquite.lib;

import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import javax.swing.JLabel;

import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.RadioButtons;
import mesquite.lib.ui.SingleLineTextField;


public class NameParser implements XMLPreferencesProcessor, ItemListener, TextListener{

	String objectName;
	MesquiteModule ownerModule;
	boolean keepPieces = false;

	String startBoundary = "_";
	boolean considerStart = false;
	int numFromStart = 0;
	boolean includeStartBoundaryInName = false;

	String endBoundary = "_";
	boolean considerEnd = true;
	int numFromEnd = 0;	
	boolean includeEndBoundaryInName = false;
	String[] examples;

	public NameParser (MesquiteModule ownerModule, String objectName){
		this.objectName=objectName;
		this.ownerModule = ownerModule;
	}

	public void setExamples(String[] examples){
		this.examples = examples;
	}

	RadioButtons includeExcludeButtons;
	IntegerField numStartField, numEndField;
	SingleLineTextField startBoundaryField, endBoundaryField;
	Checkbox includeStartBoundaryInNameField, includeEndBoundaryInNameField, considerStartField, considerEndField;
	JLabel[] exampleLabels;
	String iEPhrase(boolean upperCase){
		if (upperCase){
			if (includeExcludeButtons.getValue() == 0)
				return "Include";
			else
				return "Exclude";
		}
		else {
			if (includeExcludeButtons.getValue() == 0)
				return "include";
			else
				return "exclude";
		}
	}
	public void textValueChanged(TextEvent e) {
		resetExamplesLabels();
	}	
	public void itemStateChanged(ItemEvent e) {
		if (includeExcludeButtons.isAButton(e.getItemSelectable())){
			numStartField.setLabelText("Number of pieces to " + iEPhrase(false) + " from the start:");
			numEndField.setLabelText("Number of pieces to " + iEPhrase(false) + " from the end:");
			considerStartField.setLabel(iEPhrase(true) + " pieces at start of "+ objectName + " name");
			considerEndField.setLabel(iEPhrase(true) + " pieces from end of "+ objectName + " name");
		}
		resetExamplesLabels();
	}
	void resetExamplesLabels(){
		numStartField.setEnabled(considerStartField.getState());
		startBoundaryField.setEnabled(considerStartField.getState());
		includeStartBoundaryInNameField.setEnabled(considerStartField.getState());
		numEndField.setEnabled(considerEndField.getState());
		endBoundaryField.setEnabled(considerEndField.getState());
		includeEndBoundaryInNameField.setEnabled(considerEndField.getState());
		for (int i = 0; i<exampleLabels.length; i++)
			exampleLabels[i].setText("\"" + examples[i] + "\" —> \"" + exampleExtraction(examples[i]) + "\"");
	}
	public boolean queryOptions(String title, String label, String constructingIntro, String helpString){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(ownerModule.containerOfModule(), title, buttonPressed);
		dialog.appendToHelpString(helpString);
		dialog.addLabel(label);
		dialog.addHorizontalLine(2);

		dialog.addLabel(constructingIntro);
		int keepChoice = 1;
		if (keepPieces)
			keepChoice = 0;
		includeExcludeButtons = dialog.addRadioButtons(new String[]{"Include pieces of " + objectName + " name", "Exclude pieces from " + objectName + " name"}, keepChoice);
		includeExcludeButtons.addItemListener(this);
		dialog.addHorizontalLine(1);
		considerStartField = dialog.addCheckBox(iEPhrase(true) + " pieces at start of "+ objectName + " name", considerStart);
		numStartField = dialog.addIntegerField("Number of pieces to " + iEPhrase(false) + " from the start:", numFromStart, 3);
		startBoundaryField = dialog.addTextField("String separating pieces at start:", startBoundary, 8, true);
		includeStartBoundaryInNameField = dialog.addCheckBox("Include last delimiter", includeStartBoundaryInName);

		dialog.addHorizontalLine(1);

		considerEndField = dialog.addCheckBox(iEPhrase(true) + " pieces from end of "+ objectName + " name", considerEnd);
		numEndField = dialog.addIntegerField("Number of pieces to " + iEPhrase(false) + " from the end:", numFromStart, 3);
		endBoundaryField = dialog.addTextField("String separating pieces at end:", endBoundary, 8, true);
		includeEndBoundaryInNameField = dialog.addCheckBox("Include first delimiter", includeEndBoundaryInName);

		considerStartField.addItemListener(this);
		considerEndField.addItemListener(this);
		includeStartBoundaryInNameField.addItemListener(this);
		includeEndBoundaryInNameField.addItemListener(this);
		numStartField.getTextField().addTextListener(this);
		numEndField.getTextField().addTextListener(this);
		startBoundaryField.addTextListener(this);
		endBoundaryField.addTextListener(this);
		//		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);


		/*	String s = "Mesquite searches within the name of each chromatogram file for both a code indicating the sample (e.g., a voucher number) and the primer name. ";
		s+= "To allow this, you must indicate the string of characters that appears immediately before the sample code, and immediately after, as well as the strings before and after the primer name. ";
		s+= "Those strings cannot also appear within the sample code and primer name.\n";
		dialog.appendToHelpString(s);
		 */
		if (examples != null && examples.length>0){
			dialog.addHorizontalLine(1);
			dialog.addLabel("Examples:");
			exampleLabels = new JLabel[examples.length];
			for (int i = 0; i<exampleLabels.length; i++){
				exampleLabels[i] = dialog.addLabel(examples[i]);
			}
			resetExamplesLabels();
			dialog.addHorizontalLine(1);
		}

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			considerStart = considerStartField.getState();
			startBoundary = startBoundaryField.getText();
			numFromStart = numStartField.getValue();
			includeStartBoundaryInName = includeStartBoundaryInNameField.getState();

			considerEnd = considerEndField.getState();
			endBoundary = endBoundaryField.getText();
			numFromEnd = numEndField.getValue();
			includeEndBoundaryInName = includeEndBoundaryInNameField.getState();

		}
		//storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}	
	/*.................................................................................................................*/
	public void processSingleXMLPreference(String tag, String flavor, String content) {
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("considerStart".equalsIgnoreCase(tag))
			considerStart = MesquiteBoolean.fromTrueFalseString(content);
		else if ("includeStartBoundaryInName".equalsIgnoreCase(tag))
			includeStartBoundaryInName = MesquiteBoolean.fromTrueFalseString(content);
		else if ("considerEnd".equalsIgnoreCase(tag))
			considerEnd = MesquiteBoolean.fromTrueFalseString(content);
		else if ("includeEndBoundaryInName".equalsIgnoreCase(tag))
			includeEndBoundaryInName = MesquiteBoolean.fromTrueFalseString(content);

		else if ("startBoundary".equalsIgnoreCase(tag))
			startBoundary = StringUtil.cleanXMLEscapeCharacters(content);
		else if ("endBoundary".equalsIgnoreCase(tag))
			endBoundary = StringUtil.cleanXMLEscapeCharacters(content);

		else if ("numFromStart".equalsIgnoreCase(tag))
			numFromStart = MesquiteInteger.fromString(content);
		else if ("numFromEnd".equalsIgnoreCase(tag))
			numFromEnd = MesquiteInteger.fromString(content);
		else if ("keepPieces".equalsIgnoreCase(tag))
			keepPieces = MesquiteBoolean.fromTrueFalseString(content);

	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "keepPieces", keepPieces);  
		StringUtil.appendXMLTag(buffer, 2, "considerStart", considerStart);  
		StringUtil.appendXMLTag(buffer, 2, "startBoundary", startBoundary);  
		StringUtil.appendXMLTag(buffer, 2, "numFromStart", numFromStart);  
		StringUtil.appendXMLTag(buffer, 2, "includeStartBoundaryInName", includeStartBoundaryInName);  
		StringUtil.appendXMLTag(buffer, 2, "considerEnd", considerEnd);  
		StringUtil.appendXMLTag(buffer, 2, "endBoundary", endBoundary);  
		StringUtil.appendXMLTag(buffer, 2, "numFromEnd", numFromEnd);  
		StringUtil.appendXMLTag(buffer, 2, "includeEndBoundaryInName", includeEndBoundaryInName);  
		return buffer.toString();
	}
	/*.................................................................................................................*/

	/*.................................................................................................................*/
	public String exampleExtraction(String name){
		if (StringUtil.blank(name))
			return name;
		boolean keepPiecesSET = keepPieces;
		boolean considerStartSET = considerStart;
		boolean considerEndSET = considerEnd;
		String startBoundarySET = startBoundary;
		String endBoundarySET = endBoundary;
		int numFromStartSET = numFromStart;
		int numFromEndSET = numFromEnd;
		boolean includeStartBoundaryInNameSET = includeStartBoundaryInName;
		boolean includeEndBoundaryInNameSET = includeEndBoundaryInName;
		keepPieces=includeExcludeButtons.getValue()==0;
		considerStart = considerStartField.getState();
		considerEnd = considerEndField.getState();
		startBoundary = startBoundaryField.getText();
		endBoundary = endBoundaryField.getText();
		numFromStart = numStartField.getValue();
		numFromEnd = numEndField.getValue();
		includeStartBoundaryInName = includeStartBoundaryInNameField.getState();
		includeEndBoundaryInName = includeEndBoundaryInNameField.getState();

		String part = extractPart(name);

		keepPieces=keepPiecesSET;
		considerStart = considerStartSET;
		considerEnd = considerEndSET;
		startBoundary = startBoundarySET;
		endBoundary = endBoundarySET;
		numFromStart = numFromStartSET;
		numFromEnd = numFromEndSET;
		includeStartBoundaryInName = includeStartBoundaryInNameSET;
		includeEndBoundaryInName = includeEndBoundaryInNameSET;

		return part;
	}	

	static final String NULLRETURN  = "";
	/*.................................................................................................................*/
	public String extractPart(String name){
		if (StringUtil.blank(name))
			return name;
		int startBoundaryIndex =-1;
		int endBoundaryIndex = -1;
		boolean startBoundaryPresent = name.indexOf(startBoundary)>=0;
		boolean endBoundaryPresent = name.indexOf(endBoundary)>=0;

		if (considerStart && numFromStart>0) {
			startBoundaryIndex = StringUtil.getIndexOfMatchingString(name, startBoundary, numFromStart, true);
			if (startBoundaryIndex>=0){ //was found
				if (keepPieces){
					if (includeStartBoundaryInName)
						startBoundaryIndex += startBoundary.length();
				}
				else { //excluding
					if (!includeStartBoundaryInName)
						startBoundaryIndex += startBoundary.length();
				}
			}
		}
		if (considerEnd && numFromEnd>0) {
			endBoundaryIndex = StringUtil.getIndexOfMatchingString(name, endBoundary, numFromEnd, false);
			if (endBoundaryIndex>=0){ //was found
				if (keepPieces){
					if (!includeEndBoundaryInName)
						endBoundaryIndex += startBoundary.length();
				}
				else { //excluding
					if (includeEndBoundaryInName)
						endBoundaryIndex += startBoundary.length();
				}
			}
		}

		if (keepPieces){ //INCLUDE PIECES -- if no delimiter, include all.
			if (considerStart && considerEnd){
				if (numFromStart> 0 && numFromEnd> 0){ //including from both sides
					if (startBoundaryIndex <0) //delimiter not found, therefore include everything
						return name;
					if (endBoundaryIndex <0) //delimiter not found, therefore include everything
						return name;
					if (startBoundaryIndex>=endBoundaryIndex) //overlap
						return name;
					String part=name.substring(0,startBoundaryIndex);   
					part += name.substring(endBoundaryIndex, name.length());
					return part;
				}
				else if (numFromStart> 0){ //including from start only
					if (startBoundaryIndex <0) //delimiter not found, therefore include everything
						return name;
					return name.substring(0,startBoundaryIndex);   
				}
				else if (numFromEnd> 0){ //including from end only
					if (endBoundaryIndex <0) //delimiter not found, therefore include everything
						return name;
					return name.substring(startBoundaryIndex, name.length());   
				}
				else //not including from either end
					return NULLRETURN;
			}
			else if (considerStart){
				if (numFromStart> 0){ //including from start only
					if (startBoundaryIndex <0) //delimiter not found, therefore include everything
						return name;
					return name.substring(0,startBoundaryIndex);   
				}
				else
					return NULLRETURN;
			}
			else if (considerEnd){
				if (numFromEnd> 0){ //including from end only
					if (endBoundaryIndex <0) //delimiter not found, therefore include everything
						return name;
					return name.substring(endBoundaryIndex, name.length());   
				}
				else
					return NULLRETURN;
			}
			else
				return NULLRETURN;
		}
		else { //EXCLUDE PIECES  xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
			if (considerStart && considerEnd){//excluding from both sides
				if (!startBoundaryPresent){  //from start there are no delimiters
					if (!endBoundaryPresent) //nor from end
						return name;
					if (numFromEnd == 0) //no requested deletions; return all
						return name;
					if (endBoundaryIndex<0) //end boundary is present, but there aren't enough; exclude all
						return NULLRETURN;
					return name.substring(0, endBoundaryIndex);   //end boundary is present and there are enough pieces; exclude those requested
				}
				else if (!endBoundaryPresent){ //from end there are no delimiters, though there are from start
					if (numFromStart == 0) //no requested deletions; return all
						return name;
					if (startBoundaryIndex<0) //start boundary is present, but there aren't enough; exclude all
						return NULLRETURN;
					return name.substring(startBoundaryIndex,name.length());   //start boundary is present and there are enough pieces; exclude those requested
				}
				else	if (startBoundaryIndex<0 || endBoundaryIndex<0){ // numFrom are zero or not enough delimiters on one side or the other, but delimiters present in both directions
					if (startBoundaryIndex<0 && endBoundaryIndex<0){  //both ends
						if (numFromStart == 0 && numFromEnd == 0) //both ask for zero; forgiven; return all
							return name;
						return NULLRETURN;
					}
					else if (startBoundaryIndex<0){ //start not enough or numFromStart 0, but end found
						if (numFromStart == 0)//forgiven, because not asking for anything
							return name.substring(0, endBoundaryIndex);   
						return NULLRETURN;
					}
					else if (endBoundaryIndex<0){
						if (numFromEnd == 0) //forgiven, because not asking for anything
							return name.substring(startBoundaryIndex,name.length());   
						return NULLRETURN;
					}
					return name;
				}
				else if (startBoundaryIndex>endBoundaryIndex) //overlap; exclude everything
					return NULLRETURN;
				else
					return name.substring(startBoundaryIndex,endBoundaryIndex);   
			}
			else if (considerStart){//excluding from start only
				if (numFromStart== 0)
					return name;
				else if (startBoundaryIndex <0){
					if (startBoundaryPresent)  //present but not enough
						return NULLRETURN;
					else
						return name;
				}
				else 
					return name.substring(startBoundaryIndex,name.length());   
			}
			else if (considerEnd){//excluding from end only
				if (numFromEnd== 0)
					return name;
				else if (endBoundaryIndex <0){
					if (endBoundaryPresent)  //present but not enough
						return NULLRETURN;
					else
						return name;
				}
				else 
					return name.substring(0, endBoundaryIndex);   

			}
			else //not excluding anything
				return name;
		}

	}




}
