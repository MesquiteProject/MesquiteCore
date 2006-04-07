package mesquite.chromaseq.lib;

import java.awt.*;
import java.awt.event.*;

import mesquite.lib.*;

public class ChromFileNameParsing implements Listable, Explainable {
	
	public String dnaCodeStartToken = "DNA";   
	public String dnaCodeEndToken = "_";  
	public String dnaCodeSuffixToken = ".";  
	String dnaCodeRemovalToken= "-";
	
	public String primerStartToken="_";
	public String primerEndToken = "_";
	
	public String path;
	
	boolean sampleCodeFirst = true;
	
	public String name = "default";
	public String explanation;
	
	public ChromFileNameParsing() {
	}

	public ChromFileNameParsing(ChromFileNameParsing rule) {
		dnaCodeStartToken = rule.dnaCodeStartToken;
		dnaCodeEndToken = rule.dnaCodeEndToken;
		dnaCodeSuffixToken = rule.dnaCodeSuffixToken;
		dnaCodeRemovalToken = rule.dnaCodeRemovalToken;
		primerStartToken = rule.primerStartToken;
		primerEndToken = rule.primerEndToken;
		name = rule.name;
		sampleCodeFirst = rule.sampleCodeFirst;
	}

	public void setPath(String path){
		this.path = path;
	}
	public void setName(String name){
		this.name = name;
	}
	public String getName(){
		return name;
	}
	public String getStartToken(){
		return dnaCodeStartToken;
	}
	public String getExplanation(){
		return explanation;
	}
	String getProcessedTokenForWrite(String s) {
		if (" ".equals(s))
		return "\\ ";
	else if (StringUtil.blank(s))
		return " ";
	else
		return s;
	}
	public String getXML(){
		StringBuffer buffer = new StringBuffer(1000);
		buffer.append("<?xml version=\"1.0\"?>\n");
		buffer.append("<mesquite>\n");
		buffer.append("\t<chromFileNameParsingRules>\n");
		buffer.append("\t\t<version>1</version>\n");
		buffer.append("\t\t<boundedByTokens>\n");
		StringUtil.appendXMLTag(buffer, 3, "name", name);  
		StringUtil.appendXMLTag(buffer, 3, "sampleCodeFirst", sampleCodeFirst);  
		StringUtil.appendXMLTag(buffer, 3, "dnaCodeStartToken", getProcessedTokenForWrite(dnaCodeStartToken));  
		StringUtil.appendXMLTag(buffer, 3, "dnaCodeEndToken", getProcessedTokenForWrite(dnaCodeEndToken));  
		StringUtil.appendXMLTag(buffer, 3, "dnaCodeSuffixToken", getProcessedTokenForWrite(dnaCodeSuffixToken));  
		StringUtil.appendXMLTag(buffer, 3, "dnaCodeRemovalToken", getProcessedTokenForWrite(dnaCodeRemovalToken));  
		StringUtil.appendXMLTag(buffer, 3, "primerStartToken", getProcessedTokenForWrite(primerStartToken));  
		StringUtil.appendXMLTag(buffer, 3, "primerEndToken", getProcessedTokenForWrite(primerEndToken));  
/*
 * 		StringUtil.appendXMLTag(buffer, 3, "translateSampleCodes", translateSampleCodes);  
		StringUtil.appendXMLTag(buffer, 3, "primerListPath", primerListPath);  
		StringUtil.appendXMLTag(buffer, 3, "requiresExtension", requiresExtension);  
		StringUtil.appendXMLTag(buffer, 3, "dnaNumberListPath", dnaNumberListPath);  
*/
		buffer.append("\t\t</boundedByTokens>\n");
		buffer.append("\t</chromFileNameParsingRules>\n");
		buffer.append("</mesquite>\n");
		return buffer.toString();
	}
	public void save(String path, String name){
		this.name = name;
		this.path = path;
		MesquiteFile.putFileContents(path, getXML(), true); 	
	}

	public void save(){
		if (path!=null)
			MesquiteFile.putFileContents(path, getXML(), true); 	
	}

	/*.................................................................................................................*/
	public boolean readXML(String contents, CommandRecord commandRec) {
		Parser parser = new Parser();
		Parser subParser = new Parser();
		parser.setString(contents);
		boolean acceptableVersion = false;
		if (!parser.isXMLDocument(false))   // check if XML
			return false;
		if (!parser.resetToMesquiteTagContents())   // check if has mesquite tag
			return false;
		MesquiteString nextTag = new MesquiteString();
		String tagContent = parser.getNextXMLTaggedContent(nextTag);
		if ("chromFileNameParsingRules".equalsIgnoreCase(nextTag.getValue())) {  //make sure it has the right root tag
			parser.setString(tagContent);
			tagContent = parser.getNextXMLTaggedContent(nextTag);
			String subTagContent;
			while (!StringUtil.blank(tagContent)) {
				if ("version".equalsIgnoreCase(nextTag.getValue())) {
					if ("1".equalsIgnoreCase(tagContent))
						acceptableVersion = true;
					else
						return false;
				}
				else if ("boundedByTokens".equalsIgnoreCase(nextTag.getValue()) && acceptableVersion) {
					subParser.setString(tagContent);
					subTagContent = subParser.getNextXMLTaggedContent(nextTag);
					while (!StringUtil.blank(nextTag.getValue())) {
						if ("name".equalsIgnoreCase(nextTag.getValue()))
							name = StringUtil.cleanXMLEscapeCharacters(subTagContent);
						else if ("dnaCodeStartToken".equalsIgnoreCase(nextTag.getValue()))
							dnaCodeStartToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("dnaCodeEndToken".equalsIgnoreCase(nextTag.getValue()))
							dnaCodeEndToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("dnaCodeSuffixToken".equalsIgnoreCase(nextTag.getValue()))
							dnaCodeSuffixToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("dnaCodeRemovalToken".equalsIgnoreCase(nextTag.getValue()))
							dnaCodeRemovalToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("primerStartToken".equalsIgnoreCase(nextTag.getValue()))
							primerStartToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("primerEndToken".equalsIgnoreCase(nextTag.getValue()))
							primerEndToken = processTokenAfterRead(StringUtil.cleanXMLEscapeCharacters(subTagContent));
						else if ("sampleCodeFirst".equalsIgnoreCase(nextTag.getValue()))
							sampleCodeFirst = MesquiteBoolean.fromTrueFalseString(subTagContent);
					/*		else if ("primerListPath".equalsIgnoreCase(nextTag.getValue()))
							primerListPath = StringUtil.cleanXMLEscapeCharacters(subTagContent);
						else if ("dnaNumberListPath".equalsIgnoreCase(nextTag.getValue()))
							dnaNumberListPath = StringUtil.cleanXMLEscapeCharacters(subTagContent);
						else if ("translateSampleCodes".equalsIgnoreCase(nextTag.getValue()))
							translateSampleCodes = MesquiteBoolean.fromTrueFalseString(subTagContent);
						*/
						subTagContent = subParser.getNextXMLTaggedContent(nextTag);
					}
				}
				tagContent = parser.getNextXMLTaggedContent(nextTag);
			}
		} else
			return false;
		return true;
	}
	
	/*.................................................................................................................*/
	public String processTokenAfterRead(String s) {
		if ("\\ ".equals(s))
			return " ";
		else if (StringUtil.blank(s))
			return "";
		else
			return s;
	}

	/*.................................................................................................................*/
	public boolean queryOptions(String name) {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "File Naming Rule",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		if (!StringUtil.blank(name))
			dialog.addLabel("Rules for Chromatogram File Names: "+name);
		else
			dialog.addLabel("Rules for Chromatogram File Names");
		Checkbox sampleCodeFirstBox = dialog.addCheckBox("sample code precedes primer name", sampleCodeFirst);
		SingleLineTextField dnaCodeStartField = dialog.addTextField("String before sample code:", dnaCodeStartToken, 8, true);
		SingleLineTextField dnaCodeEndField = dialog.addTextField("String after sample code:", dnaCodeEndToken, 8, true);
		SingleLineTextField dnaCodeSuffixField = dialog.addTextField("String before sample code suffix:", dnaCodeSuffixToken, 4, true);
		SingleLineTextField dnaCodeRemovalField = dialog.addTextField("Characters to be removed from sample code:", dnaCodeRemovalToken, 4, true);
		dialog.addHorizontalLine(2);
		SingleLineTextField primerStartField = dialog.addTextField("String before primer name:", primerStartToken, 8, true);
		SingleLineTextField primerEndField = dialog.addTextField("String after primer name:", primerEndToken, 8, true);
		
		
//		Checkbox requiresExtensionBox = dialog.addCheckBox("only process files with standard extensions (ab1,abi,ab,CRO,scf)", requiresExtension);
		
		
		String s = "Mesquite searches within the name of each chromatogram file for both a code indicating the sample (e.g., a voucher number) and the primer name. ";
		s+= "To allow this, you must indicate the string of characters that appears immediately before the sample code, and immediately after, as well as the strings before and after the primer name. ";
		s+= "Those strings cannot also appear within the sample code and primer name.\n";
		dialog.appendToHelpString(s);
		
		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			dnaCodeStartToken = dnaCodeStartField.getText();
			dnaCodeEndToken = dnaCodeEndField.getText();
			dnaCodeSuffixToken = dnaCodeSuffixField.getText();
			dnaCodeRemovalToken = dnaCodeRemovalField.getText();
			primerStartToken = primerStartField.getText();
			primerEndToken = primerEndField.getText();
			sampleCodeFirst = sampleCodeFirstBox.getState();
//			translateSampleCodes = translateCodesBox.getState();
		}
		//storePreferences();  // do this here even if Cancel pressed as the File Locations subdialog box might have been used
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}
	
	/*.................................................................................................................*/
	public String getStringPiece(MesquiteModule ownerModule, String s, String startToken, String endToken, MesquiteString remainder, StringBuffer logBuffer, String message){
		String piece="";
		if (remainder!=null)
			remainder.setValue(s);
		
		if (!StringUtil.blank(startToken) || " ".equals(startToken)) {
			if (s.indexOf(startToken)>-1) {
				piece = s.substring(s.indexOf(startToken)+startToken.length(), s.length());  // getting substring that starts with DNA number
			}
			else {
				if (ownerModule!=null)
					ownerModule.echoStringToFile(" ** Can't extract " + message + " from file name: " + s, logBuffer);
				return null;
			}
		} 
		else
			piece = s;
		remainder.setValue(piece);
		if (!StringUtil.blank(endToken) || " ".equals(endToken)) {
			if (remainder!=null)
				if (piece.indexOf(endToken)>=0)
					remainder.setValue(piece.substring(piece.indexOf(endToken), piece.length())); // now wiping out rest, but NOT endToken   +endToken.length()
			if (piece.indexOf(endToken)>=0)
				return piece.substring(0,piece.indexOf(endToken));
			else
				return piece;
		} 
		
		return null;
	}
	/*.................................................................................................................*/
	public boolean parseFileName(MesquiteModule ownerModule, String fileName, MesquiteString sampleCode, MesquiteString sampleCodeSuffix, MesquiteString primerName, StringBuffer logBuffer){
		String primerNamePiece="";
		String sampleCodePiece = "";
		MesquiteString remainder = new MesquiteString();
		
		//	Finding and processing the sample code 
		if (sampleCodeFirst) {
			sampleCodePiece = getStringPiece(ownerModule, fileName, dnaCodeStartToken, dnaCodeEndToken, remainder, logBuffer, "sample code");
			if (sampleCodePiece==null)
				return false;
			primerNamePiece = getStringPiece(ownerModule, remainder.getValue(), primerStartToken, primerEndToken, remainder, logBuffer, "primer name");
		}		
		else	if (sampleCodeFirst) {
			primerNamePiece = getStringPiece(ownerModule, fileName, primerStartToken, primerEndToken, remainder, logBuffer, "primer name");
			if (primerNamePiece==null)
				return false;
			sampleCodePiece = getStringPiece(ownerModule, remainder.getValue(), dnaCodeStartToken, dnaCodeEndToken, remainder, logBuffer, "sample code");
		}		
		if (sampleCodePiece==null || primerNamePiece==null)
			return false;
		sampleCodePiece = StringUtil.removeCharacters(sampleCodePiece,dnaCodeRemovalToken); //TODO: give user control of this

		String suffix = "";
		if (!StringUtil.blank(dnaCodeSuffixToken)) {
			if (sampleCodePiece.indexOf(dnaCodeSuffixToken)>-1) {
				suffix = " " + sampleCodePiece.substring(sampleCodePiece.indexOf(dnaCodeSuffixToken)+1,sampleCodePiece.length());
				sampleCodePiece = sampleCodePiece.substring(0,sampleCodePiece.indexOf(dnaCodeSuffixToken));
			}
		}
		sampleCode.setValue(sampleCodePiece);
		sampleCodeSuffix.setValue(suffix);
		primerName.setValue(primerNamePiece);
		
		return true;
	}

	
}
