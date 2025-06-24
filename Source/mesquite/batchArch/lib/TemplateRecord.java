/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.batchArch.lib; 


import mesquite.lib.Arguments;
import mesquite.lib.Explainable;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteString;
import mesquite.lib.NumberArray;
import mesquite.lib.Parser;
import mesquite.lib.RandomBetween;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.FileInterpreterI;
import mesquite.lib.tree.Tree;


public class TemplateRecord  implements Listable, Explainable {
	public boolean userDefined = false;
	public static String defaultExportFormat = "NEXUS file interpeter";
	public String name = "", explanation, matrixExportFormat=defaultExportFormat, path;
	private String[] startText, textForEachFile, endText;
	private String[] batchFileName;
	TemplateManager manager;
	RandomBetween randomBetween = new RandomBetween();
	
	StringBuffer[] outputBuffer;
	int defaultNumFiles = 5;
	int numFiles = defaultNumFiles;
	public TemplateRecord(boolean userDefined, TemplateManager manager){
		this.userDefined = userDefined;
		this.manager = manager;
		setNumFiles(defaultNumFiles);
	}
	public boolean batchFileHasContent(int fileNumber){
		if (!StringUtil.blank(getStartText(fileNumber)))
			return true;
		if (!StringUtil.blank(getTextEach(fileNumber)))
			return true;
		if (!StringUtil.blank(getEndText(fileNumber)))
			return true;
		return false;
	}
	public String batchFileToString(int fileNumber, boolean isNEXUS){
		String contents = "";
		if (isNEXUS) {
			if (!StringUtil.blank(getStartText(fileNumber)))
				contents += " start" + (fileNumber+1) + " = " + StringUtil.tokenize(getStartText(fileNumber)) + StringUtil.lineEnding();
			if (!StringUtil.blank(getTextEach(fileNumber)))
				contents += " repeat" + (fileNumber+1) + " = " + StringUtil.tokenize(getTextEach(fileNumber)) + StringUtil.lineEnding();
			if (!StringUtil.blank(getEndText(fileNumber)))
				contents += " end" + (fileNumber+1) + " = " + StringUtil.tokenize(getEndText(fileNumber)) + StringUtil.lineEnding();
			if (batchFileName!=null && fileNumber<batchFileName.length && !StringUtil.blank(batchFileName[fileNumber])) //Changed here to allow replacement of text in file names
				contents += " batchFileName" + (fileNumber+1) + " = " + StringUtil.tokenize(batchFileName[fileNumber]) + StringUtil.lineEnding();
		}
		return contents;
	}
	public String toNEXUSString(){
		String contents = "#MESQUITEINIT BATCHFILE 1" + StringUtil.lineEnding();
		if (!StringUtil.blank(name))
			contents += " name = " + StringUtil.tokenize(name) + StringUtil.lineEnding();
		if (!StringUtil.blank(explanation))
			contents += " explanation = " + StringUtil.tokenize(explanation) + StringUtil.lineEnding();
			
		for (int i=0; i<numFiles; i++)
			contents += batchFileToString(i,true);
						
		if (!StringUtil.blank(matrixExportFormat))
			contents += " matrixExportFormat =  " + StringUtil.tokenize(matrixExportFormat) + StringUtil.lineEnding();
		return contents;
	}
	public boolean fillFromString(String contents, Parser parser){
		parser.setString(contents);
		String token = parser.getNextToken(); 
		if (!StringUtil.blank(token))
			if (token.equalsIgnoreCase("#MESQUITEINIT")) {
				token = parser.getNextToken(); 
				if (token.equalsIgnoreCase("BATCHFILE")) {
					token = parser.getNextToken(); 
					if (token.equalsIgnoreCase("1")) {
						Arguments arguments = new Arguments(parser, true);
						name = arguments.getParameterValue("name");
						if (StringUtil.blank(name))
							name = "untitled template";
						explanation= arguments.getParameterValue("explanation");
						if (StringUtil.blank(explanation))
							explanation = "";
						
						for (int i=0; i<numFiles; i++) {
							setStartText(arguments.getParameterValue("start"+(i+1)), i);
							setTextEach(arguments.getParameterValue("repeat"+(i+1)), i);
							setEndText(arguments.getParameterValue("end"+(i+1)), i);
							setBatchFileName(arguments.getParameterValue("batchFileName"+(i+1)), i);
						}
						matrixExportFormat = arguments.getParameterValue("matrixExportFormat");
						if (StringUtil.blank(matrixExportFormat))
							matrixExportFormat = defaultExportFormat;
						return true; 
					}
					else 
						MesquiteMessage.warnProgrammer("Mesquite does not understand templates of this version.");
				}
				else 
					MesquiteMessage.warnProgrammer("The second token in templates must be \"BATCHFILE\".");
			}
			else 
				MesquiteMessage.warnProgrammer("Templates must begin with \"#MESQUITEINIT\".");
		return false;
	}
	public int getNumFiles(){
		return numFiles;
	}
	public String getName(){
		return name;
	}
	public boolean getUserDefined(){
		return userDefined;
	}
	public String getExplanation(){
		return explanation;
	}
	public void setStartText(String s, int file){
		if (startText == null || file>=startText.length)
			return;
		startText[file] = s;
	}
	public String getStartText(int file){
		if (startText == null || file>=startText.length)
			return null;
		return startText[file];
	}
	public void setTextEach(String s, int file){
		if (textForEachFile == null || file>=textForEachFile.length)
			return;
		textForEachFile[file] = s;
	}
	public String getTextEach(int file){
		if (textForEachFile == null || file>=textForEachFile.length)
			return null;
		return textForEachFile[file];
	}
	public void setEndText(String s, int file){
		if (endText == null || file>=endText.length)
			return;
		endText[file] = s;
	}
	public String getEndText(int file){
		if (endText == null || file>=endText.length)
			return null;
		return endText[file];
	}
	public void setBatchFileName(String s, int file){
		if (batchFileName == null || file>=batchFileName.length)
			return;
		batchFileName[file] = s;
	}
	public String getBatchFileName(int file, String baseName, boolean replace){
		if (batchFileName == null || file>=batchFileName.length)
			return null;
		if (!replace)
			return batchFileName[file];
		return replaceUniversalCode(batchFileName[file], baseName, null);
	}
	/*private boolean treeInBuffer(StringBuffer sb){
		if (sb == null)
			return false;
		String s = sb.toString();
		if (s == null)
			return false;
		return (s.indexOf("<tree>")>=0);
	}*/
	private boolean treeInString(String sb){
		if (sb == null)
			return false;
		return (sb.indexOf("<tree>")>=0);
	}
	public boolean needsTree(){
		for (int i=0; i<numFiles; i++) {
			if (treeInString(startText[i]))
				return true;
			if (treeInString(textForEachFile[i]))
				return true;
			if (treeInString(endText[i]))
				return true;
		}
		return false;
	}
	private int randomIndex(String sb, MesquiteInteger from){
		if (sb == null)
			return -1;
		return (sb.indexOf("<random>", from.getValue()));
	}
	private int randomCloseIndex(String sb, MesquiteInteger from){
		if (sb == null)
			return -1;
		return (sb.indexOf("</random>", from.getValue()));
	}
	private int snippetIndex(String sb, MesquiteInteger from){
		if (sb == null)
			return -1;
		return (sb.indexOf("<snippet>", from.getValue()));
	}
	private int snippetCloseIndex(String sb, MesquiteInteger from){
		if (sb == null)
			return -1;
		return (sb.indexOf("</snippet>", from.getValue()));
	}
	private String nextSnippet(String sb, MesquiteInteger from){
		int s = snippetIndex(sb, from);
		if (s <0)
			return null;
		from.setValue(from.getValue()+6);
		int sc = snippetCloseIndex(sb, from);
		if (sc <0 || sc<s)
			return null;
		String snippet = sb.substring(s+9, sc);
		from.setValue(sc+7);
		return snippet;
	}
	private void collectSnippets(ListableVector v, String sb){
		MesquiteInteger from = new MesquiteInteger(-1);
		String snippet;
		while ((snippet = nextSnippet(sb, from))!=null) {
			if (v.indexOfByName(snippet)<0) {
				MesquiteString ms = new MesquiteString();
				ms.setName(snippet);
				v.addElement(ms, false);
			}
		}
	}
	public ListableVector snippetsNeeded(){
		ListableVector v = new ListableVector();
		for (int i=0; i<numFiles; i++) {
			collectSnippets(v, startText[i]);
			collectSnippets(v, textForEachFile[i]);
			collectSnippets(v, endText[i]);
		}
		return v;
	}
	public void setNumFiles(int num){
		numFiles = num;
		batchFileName = new String[numFiles];
		startText = new String[numFiles];
		textForEachFile = new String[numFiles];
		endText = new String[numFiles];
		for (int i=0; i<numFiles; i++) {
			batchFileName[i] = "Batch File " + i;
		}
	}
	String replaceSnippets(String s, ListableVector v){
		if (v !=null && v.size()!=0) {
			for (int i=0; i<v.size(); i++){
				MesquiteString ms = (MesquiteString)v.elementAt(i);
				MesquiteString snippet = manager.getFileSpecific(ms.getName());
				String replaceString = "";
				if (snippet !=null)
					replaceString = snippet.getValue();
				
				s = StringUtil.replace(s, "<snippet>" + ms.getName() + "</snippet>", replaceString);
			}
			return s;
		}
		else
			return s;
	}
	/*.................................................................................................................*/
	String replaceTree(String s, Tree tree){
		String replaceString = "";
		if (tree !=null)
			replaceString = tree.writeTree(Tree.BY_NAMES, true);
		return StringUtil.replace(s, "<tree>", replaceString);
	}
	/*.................................................................................................................*/
	public String fileExtension(String matrixExportFormatName, FileCoordinator coord, boolean includeDot){
		String s = "";
		if (includeDot) s = ".";
		if (matrixExportFormatName.equals("NEXUS file interpreter")|| matrixExportFormatName.equals("NEXUS file")) {
			return s + "nex";
		}
		else {
			//arguments = "#mesquite.io.InterpretNonaHennig.InterpretNonaHennig 1 file= 'exported.ss' directory = '/Users/wmaddisn/Desktop/'";
			FileInterpreterI matrixExportFormat = (FileInterpreterI)coord.findEmployeeWithName(matrixExportFormatName);
			if (matrixExportFormat!=null) {
				String ext = matrixExportFormat.preferredDataFileExtension();
				if (!StringUtil.blank(ext))
					return s + ext;
			}
		}
		return "";
	}
	/*.................................................................................................................*/
	String replaceExtension(String s){
		FileCoordinator coord = manager.getFileCoordinator();
		String replaceString = fileExtension(matrixExportFormat,coord,true);
		return StringUtil.replace(s, "<fileExtension>", replaceString);
	}
	/*.................................................................................................................*/
	String getRandomValues(int numValues, int type, MesquiteNumber lowerBound, MesquiteNumber upperBound, boolean withReplacement){
		String s = "";
		NumberArray numArray = new NumberArray(numValues);
		int intValue;
		double doubleValue;
		long longValue;
		MesquiteNumber n =new MesquiteNumber();
		if ((type==MesquiteNumber.INT) && (upperBound.getIntValue()-lowerBound.getIntValue()<numValues-1))
			numValues = upperBound.getIntValue()-lowerBound.getIntValue() +1;
		else if ((type==MesquiteNumber.LONG) && (upperBound.getLongValue()-lowerBound.getLongValue()<numValues-1))
			numValues = (int)(upperBound.getLongValue()-lowerBound.getLongValue() +1);
		for (int i=0;i<numValues; i++){
			if (type==MesquiteNumber.INT) {
				intValue= randomBetween.randomIntBetween(lowerBound.getIntValue(), upperBound.getIntValue());
				n.setValue(intValue);
				if (!withReplacement)
					while (numArray.findValue(n)>=0) {
						intValue = randomBetween.randomIntBetween(lowerBound.getIntValue(), upperBound.getIntValue());
						n.setValue(intValue);
					}
				s += " " + intValue;
				numArray.setValue(i,intValue);
			}
			else if (type==MesquiteNumber.DOUBLE) {
				doubleValue = randomBetween.randomDoubleBetween(lowerBound.getDoubleValue(), upperBound.getDoubleValue());
				n.setValue(doubleValue);
				if (!withReplacement)
					while (numArray.findValue(n)>=0) {
						doubleValue = randomBetween.randomDoubleBetween(lowerBound.getDoubleValue(), upperBound.getDoubleValue());
						n.setValue(doubleValue);
					}
				s += " " + doubleValue;
				numArray.setValue(i,doubleValue);
			}
			else if (type==MesquiteNumber.LONG) {
				longValue = randomBetween.randomLongBetween(lowerBound.getLongValue(), upperBound.getLongValue());
				n.setValue(longValue);
				if (!withReplacement)
					while (numArray.findValue(n)>=0) {
						longValue = randomBetween.randomLongBetween(lowerBound.getLongValue(), upperBound.getLongValue());
						n.setValue(longValue);
					}
				s += " " + longValue;
				numArray.setValue(i,longValue);
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	String replaceRandom(String s){
		String replaceString = "";
		String randomString = "";
		Parser parser = new Parser();
		Arguments arguments;
		int lastIndex = s.indexOf("<random>");
		while (s.indexOf("</random>")>=0 && s.indexOf("<random>")>=0) {
			randomString = StringUtil.getFirstSubString(s,"<random>", "</random>");
			parser.setString(randomString);
			arguments = new Arguments(parser, true);
			int numValues = arguments.getParameterValueAsInt("numValues");
			MesquiteNumber lowerBound = new MesquiteNumber();
			MesquiteNumber upperBound = new MesquiteNumber();
			String arg = arguments.getParameterValue("lowerBound");
			if (!StringUtil.blank(arg))
				lowerBound.setValue(arg);
			arg = arguments.getParameterValue("upperBound");
			if (!StringUtil.blank(arg))
				upperBound.setValue(arg);
			boolean withReplacement = false;
			arg = arguments.getParameterValue("withReplacement");
			if (!StringUtil.blank(arg))
				if (arg.equalsIgnoreCase("yes"))
					withReplacement = true;
				else if (arg.equalsIgnoreCase("no"))
					withReplacement = false;
			String numberType = arguments.getParameterValue("type");
			int type = MesquiteNumber.INT;
			if (!StringUtil.blank(numberType)) {
				if (numberType.equalsIgnoreCase("double"))
					type = MesquiteNumber.DOUBLE;
				else if (numberType.equalsIgnoreCase("long"))
					type = MesquiteNumber.LONG;
			}
			s = StringUtil.replaceFirst(s,"<random>", "</random>", getRandomValues(numValues,type,lowerBound,upperBound, withReplacement).toString());
			if (lastIndex== s.indexOf("<random>"))
				return s;
			 lastIndex = s.indexOf("<random>");
		}
		return s;
	}
	/*.................................................................................................................*/
	public String replaceUniversalCode(String originalString, String baseName,ListableVector v ){
		String s = "";
		if (!StringUtil.blank(originalString)) {
			s = replaceSnippets(originalString, v);
			s = StringUtil.replace(s,"<name>",baseName);
			s = replaceExtension(s);
			s = replaceRandom(s);
			s = StringUtil.replace(s,"<matrixFormat>",matrixExportFormat);
			s = replaceTree(s, manager.getTree());
		}
		return s;
	}
	/*.................................................................................................................*/
	public void composeAccessoryFilesStart(int numReplicates, String baseName, String basePath){
		ListableVector v = snippetsNeeded();
		outputBuffer = new StringBuffer[numFiles];
		for (int i=0; i<numFiles; i++) {
			outputBuffer[i] = new StringBuffer(numReplicates*(200));//���
			if (!StringUtil.blank(startText[i])) 
				outputBuffer[i].append(replaceUniversalCode(startText[i].toString(), baseName,v));
		}
	}
	/*.................................................................................................................*/
	public void composeAccessoryFilesReplicate(int iMatrix, String matrixName, int section, String baseName, String basePath){
		if (outputBuffer==null)
			return;
		ListableVector v = snippetsNeeded();
		for (int i=0; i<numFiles; i++) {
			if (outputBuffer[i] !=null){//���
				if (!StringUtil.blank(textForEachFile[i])){
					String s = StringUtil.replace(textForEachFile[i].toString(),"<number>",""+iMatrix);
					if (matrixName != null)
						s = StringUtil.replace(s,"<matrixName>",matrixName);
					s = replaceUniversalCode(s,baseName,v);
					if (outputBuffer[i].length()>0 && !StringUtil.blank(s))
						outputBuffer[i].append(StringUtil.lineEnding()+StringUtil.lineEnding());
					outputBuffer[i].append(s);
				}
			}
		}
	}
	/*.................................................................................................................*/
	public void composeAccessoryFilesEnd(String baseName, String directoryPath){
		if (outputBuffer==null)
			return;
		ListableVector v = snippetsNeeded();
		for (int i=0; i<numFiles; i++) {
			if (outputBuffer[i] != null) {//���
				if (!StringUtil.blank(endText[i])){
					String s = replaceUniversalCode(endText[i].toString(), baseName,v);
					if (outputBuffer[i].length()>0 && !StringUtil.blank(s))
						outputBuffer[i].append(StringUtil.lineEnding()+StringUtil.lineEnding());
					outputBuffer[i].append(s);
				}
				if (outputBuffer[i].length()>0){
					String path; 
					if (!StringUtil.blank(getBatchFileName(i, baseName, true)))
						path = directoryPath + getBatchFileName(i, baseName, true);
					else
						path = directoryPath + "Batch File " + i;
					MesquiteFile.putFileContents(path, outputBuffer[i].toString(), true);
				}
			}
		}
	}
}

