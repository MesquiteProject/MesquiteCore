package mesquite.lib;

import java.awt.Checkbox;


public class NameParser implements XMLPreferencesProcessor{
	
	String objectName;
	MesquiteModule ownerModule;
	
	String startBoundary = "_";
	boolean considerStartBoundary = true;
	int numFromStart = 1;
	boolean includeStartBoundaryInName = false;

	String endBoundary = "_";
	boolean considerEndBoundary = true;
	int numFromEnd = 1;	
	boolean includeEndBoundaryInName = false;

	
	public NameParser (MesquiteModule ownerModule, String objectName){
		this.objectName=objectName;
		this.ownerModule = ownerModule;
	}
	
	public boolean queryOptions(String title, String label, String helpString){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(ownerModule.containerOfModule(), title, buttonPressed);
		dialog.appendToHelpString(helpString);
		dialog.addLabel(label);
		dialog.addHorizontalLine(2);

		dialog.addLabel("Options for portion to be removed at start of name");
		Checkbox considerStartBoundaryField = dialog.addCheckBox("remove start of name", considerStartBoundary);
		SingleLineTextField startBoundaryField = dialog.addTextField("String delimiting portion at start to be removed:", startBoundary, 8, true);
		IntegerField numStartBoundaryField = dialog.addIntegerField("Number of delimiting string that marks to end of the section to be removed :", numFromStart, 3);
		Checkbox includeStartBoundaryInNameField = dialog.addCheckBox("include delimiting string in name", includeStartBoundaryInName);
		
		dialog.addHorizontalLine(2);

		dialog.addLabel("Options for portion to be removed at end of name");
		Checkbox considerEndBoundaryField = dialog.addCheckBox("remove end of name", considerEndBoundary);
		SingleLineTextField endBoundaryField = dialog.addTextField("String delimiting portion at end to be removed:", endBoundary, 8, true);
		IntegerField numEndBoundaryField = dialog.addIntegerField("Number of delimiting string that marks to start of the section to be removed :", numFromEnd, 3);
		Checkbox includeEndBoundaryInNameField = dialog.addCheckBox("include delimiting string in name", includeEndBoundaryInName);


		//		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);


	/*	String s = "Mesquite searches within the name of each chromatogram file for both a code indicating the sample (e.g., a voucher number) and the primer name. ";
		s+= "To allow this, you must indicate the string of characters that appears immediately before the sample code, and immediately after, as well as the strings before and after the primer name. ";
		s+= "Those strings cannot also appear within the sample code and primer name.\n";
		dialog.appendToHelpString(s);
		*/

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			considerStartBoundary = considerStartBoundaryField.getState();
			startBoundary = startBoundaryField.getText();
			numFromStart = numStartBoundaryField.getValue();
			includeStartBoundaryInName = includeStartBoundaryInNameField.getState();
			
			considerEndBoundary = considerEndBoundaryField.getState();
			endBoundary = endBoundaryField.getText();
			numFromEnd = numEndBoundaryField.getValue();
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
		if ("considerStartBoundary".equalsIgnoreCase(tag))
			considerStartBoundary = MesquiteBoolean.fromTrueFalseString(content);
		else if ("includeStartBoundaryInName".equalsIgnoreCase(tag))
			includeStartBoundaryInName = MesquiteBoolean.fromTrueFalseString(content);
		else if ("considerEndBoundary".equalsIgnoreCase(tag))
			considerEndBoundary = MesquiteBoolean.fromTrueFalseString(content);
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

	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "considerStartBoundary", considerStartBoundary);  
		StringUtil.appendXMLTag(buffer, 2, "startBoundary", startBoundary);  
		StringUtil.appendXMLTag(buffer, 2, "numFromStart", numFromStart);  
		StringUtil.appendXMLTag(buffer, 2, "includeStartBoundaryInName", includeStartBoundaryInName);  
		StringUtil.appendXMLTag(buffer, 2, "considerEndBoundary", considerEndBoundary);  
		StringUtil.appendXMLTag(buffer, 2, "endBoundary", endBoundary);  
		StringUtil.appendXMLTag(buffer, 2, "numFromEnd", numFromEnd);  
		StringUtil.appendXMLTag(buffer, 2, "includeEndBoundaryInName", includeEndBoundaryInName);  
		return buffer.toString();
	}
	/*.................................................................................................................*/
	

	/*.................................................................................................................*/
	public String extractPart(String name){
		if (StringUtil.blank(name))
			return name;
		int startBoundaryIndex =-1;
		int endBoundaryIndex = -1;
		boolean failedMatch=false;

		// finding starting boundary
		if (considerStartBoundary) {
			startBoundaryIndex = StringUtil.getIndexOfMatchingString(name, startBoundary, numFromStart, true, includeStartBoundaryInName);
			if (startBoundaryIndex<0)
				failedMatch=true;
		}
		if (considerEndBoundary) {
			endBoundaryIndex = StringUtil.getIndexOfMatchingString(name, endBoundary, numFromEnd, false, includeEndBoundaryInName);
			if (endBoundaryIndex<0)
				failedMatch=true;
		}

		if (startBoundaryIndex<0)
			startBoundaryIndex=0;
		if (endBoundaryIndex<0)
			endBoundaryIndex = name.length();
		
		String part = name;
		
		if (!failedMatch)
			part=name.substring(startBoundaryIndex,endBoundaryIndex);   

		return part;
	}	



}
