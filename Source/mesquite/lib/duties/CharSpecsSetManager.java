/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.duties;

import java.util.Enumeration;

import mesquite.lib.Bits;
import mesquite.lib.CommandChecker;
import mesquite.lib.FileElement;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NexusBlock;
import mesquite.lib.ParseUtil;
import mesquite.lib.Snapshot;
import mesquite.lib.SpecsSet;
import mesquite.lib.SpecsSetVector;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.CharSelectionSet;
import mesquite.lib.characters.CharSpecsSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.ModelSet;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteWindow;


/* ======================================================================== */
/** Manages spec sets.*/

/* ======================================================================== */
public abstract class CharSpecsSetManager extends SpecsSetManager {
	public boolean getSearchableAsModule(){
		return false;
	}
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		//TODO:
		if (e != null)
			alert("Sorry, the " + e.getTypeName() + "  cannot be shown by this means yet.");
		return null;
	}
	public NexusBlock elementAdded(FileElement e){
		if (e!=null && e instanceof mesquite.lib.characters.CharacterData){
			e.prepareSpecsSetVector(getElementClass(), upperCaseTypeName() + "s");
		}
		return null;
	}
	public void elementDisposed(FileElement e){
		//nothing needs doing since separate reference not stored locally
	}
	public abstract String upperCaseTypeName();
	public abstract String lowerCaseTypeName();
	public abstract String nexusToken();
	public String alternativeNexusToken(){
		return null;
	}
	public abstract SpecsSet getNewSpecsSet(String name, mesquite.lib.characters.CharacterData data);
	public abstract boolean appropriateBlockForWriting(String blockName);
	public abstract boolean appropriateBlockForReading(String blockName);
	public abstract Object getSpecification(String token);
	public abstract void setSpecification(SpecsSet specsSet, Object specification, int ic);

	/** Returns whether or not the NEXUS command has category tokens before character lists, e.g. the weight, or the partition name */
	public boolean hasSpecificationTokens(){
		return true;
	}

	/*.................................................................................................................*/
	public static boolean writeLinkWithCharacterMatrixName(MesquiteFile file, CharacterData data){
		return (file.getProject().getNumberCharMatrices()>1 && MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data));
	}

	/*.................................................................................................................*/
	/** A method called immediately after the project has been established.*/
	public void projectEstablished() {
		CharactersManager manager = (CharactersManager)findElementManager(mesquite.lib.characters.CharacterData.class);
		if (manager == null)
			return;
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.charactersMenu, manager.getListsSubmenu(), upperCaseTypeName() + "s", makeCommand("showList",  this));
		/**
		MesquiteSubmenuSpec mmis = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, upperCaseTypeName() + "s", makeCommand("showList",  this),  (ListableVector)getProject().datas);
		mmis.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		mmis.setOwnerModuleID(getID());
		/**/
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant) {
				Object obj = e.doCommand("getData", null, CommandChecker.defaultChecker);
				if (obj instanceof CharacterData) {
					mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)obj;
					MesquiteWindow w = e.getModuleWindow();
					if (w != null && w.isVisible())
						temp.addLine("showList " + getProject().getMatrixNumber(data), e); 
				}
			}
		}
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger(0);
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows the " + lowerCaseTypeName(), "[optional: number of data matrix for which to show " + lowerCaseTypeName() + "]", commandName, "showList")) {
			if (StringUtil.blank(arguments)) {
				ListableVector v = new ListableVector();

				for (int i = 0; i< getProject().getNumberCharMatrices(checker.getFile()); i++) {
					v.addElement(getProject().getCharacterMatrix(checker.getFile(), i), false);
				}
				CharacterData data = null;
				if (v.size() == 1)
					data = (CharacterData)v.elementAt(0);
				else if (v.size() == 01)
					alert("Sorry, there is no matrix for which to show the list.");
				else
					data = (CharacterData)ListDialog.queryList(containerOfModule(), "Show list for which matrix?", "", null, v, 0);
				if (data != null)
					showSpecsSets(data, "List of " + upperCaseTypeName() + "s");
			}
			else {
				int t = MesquiteInteger.fromFirstToken(arguments, pos);
				if (MesquiteInteger.isCombinable(t) && t<getProject().getNumberCharMatrices(checker.getFile())) {
					return showSpecsSets(getProject().getCharacterMatrix(checker.getFile(), t), "List of " + upperCaseTypeName() + "s");
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public boolean isSubstantive(){
		return false;  
	}
	/*.................................................................................................................*/
	public String nexusStringForSpecsSetStandard(CharSpecsSet specsSet, mesquite.lib.characters.CharacterData data, MesquiteFile file, boolean isCurrent){
		if (specsSet ==null || !(getElementClass().isAssignableFrom(specsSet.getClass())))
			return null;
		ModelSet modelSet = (ModelSet)specsSet;
		String s= "";
		if (modelSet !=null  && (modelSet.getFile()==file || (modelSet.getFile()==null && data.getFile()==file))) {
			String sT = " ";
			boolean firstTime = true;
			Enumeration enumeration = file.getProject().getCharacterModels().elements();
			while (enumeration.hasMoreElements()){
				Object obj = enumeration.nextElement();
				CharacterModel cm = (CharacterModel)obj;
				String q = ListableVector.getListOfMatches(modelSet.getModels(), cm, CharacterStates.toExternal(0));
				if (q != null) {
					if (!firstTime) {
						sT += ", ";
					}
					sT += StringUtil.tokenize(cm.getNEXUSName()) + ": " + q;
					firstTime = false;
				}
			}
			if (!StringUtil.blank(sT)) {
				s+= nexusToken()+ " " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(modelSet.getName()) + " ";
				if (writeLinkWithCharacterMatrixName(file, data))
					s+= " (CHARACTERS = " +  StringUtil.tokenize(data.getName()) + ")";
				s+= "  = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	public static String nexusCoreStringForSpecsSet(CharSpecsSet specsSet, CharacterData data){
		return "";
	}

	/*.................................................................................................................*/
	public abstract String nexusStringForSpecsSet(CharSpecsSet specsSet, mesquite.lib.characters.CharacterData data, MesquiteFile file, boolean isCurrent);
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (appropriateBlockForWriting(blockName)) { 
			String s= "";
			for (int ids = 0; ids<file.getProject().getNumberCharMatrices(file); ids++) {
				mesquite.lib.characters.CharacterData data =  file.getProject().getCharacterMatrix(file, ids);
				if (data.getFile() == file && data.getWritable()){
					int numSets = data.getNumSpecsSets(getElementClass());
					SpecsSetVector ssv = data.getSpecSetsVector(getElementClass());
					if (ssv!=null){
						SpecsSet ms = (SpecsSet)data.getCurrentSpecsSet(getElementClass());
						if (ms!=null && (ms.getNexusBlockStored()==null || blockName.equalsIgnoreCase(ms.getNexusBlockStored()))) {
							if (!ms.allDefault()) {
								ms.setNexusBlockStored(blockName);
								ms.setName("UNTITLED");
								s += nexusStringForSpecsSet( (CharSpecsSet)ms, data, file, true);
							}
						}


						for (int ims = 0; ims<numSets; ims++) {
							CharSpecsSet cs = (CharSpecsSet)data.getSpecsSet(ims, getElementClass());
							if (!cs.allDefault())
								s += nexusStringForSpecsSet(cs, data, file, false);
						}
					}
				}
			}
			return s;
		}
		return null;
	}

	public  CharSelectionSet getSpecSetFromName(CharacterData data, String name){
		SpecsSetVector ssv = data.getSpecSetsVector(CharSelectionSet.class);
		if (ssv==null)
			return null;
		return (CharSelectionSet)ssv.getElement(name);
	}


	public  static Bits getCharBitsFromName(String name){
		return null;
	}


int referentWarnings = 0;
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment, String fileReadingArguments){ 
		if (appropriateBlockForReading(blockName)) { 
			MesquiteInteger startCharT = new MesquiteInteger(0);

			boolean noWarnMissingReferent = parser.hasFileReadingArgument(fileReadingArguments, "noWarnMissingReferent");
			String commandName = ParseUtil.getToken(command, startCharT);
			if (commandName == null)
				return false;
			if (commandName.equalsIgnoreCase(nexusToken()) || (alternativeNexusToken()!=null && commandName.equalsIgnoreCase(alternativeNexusToken()))) {
				String token = ParseUtil.getToken(command, startCharT);
				boolean isDefault = false;
				if ("*".equals(token)) {
					isDefault = true;
					token = ParseUtil.getToken(command, startCharT);
				}
				String nameOfSpecsSet = StringUtil.deTokenize(token); // name of specs set 
				token = ParseUtil.getToken(command, startCharT);
				if (token == null)
					return false;
				//String paradigmString = null;
				MesquiteProject project = file.getProject();
				mesquite.lib.characters.CharacterData data=null;
				String dataName = null;

				if (token.equalsIgnoreCase("(")) { //VVECTOR
					token = ParseUtil.getToken(command, startCharT); //CHARACTERS  //TODO: check to see what parameter is being set!
					if (token.equalsIgnoreCase("VECTOR")) {
						token = ParseUtil.getToken(command, startCharT); //)
						MesquiteMessage.discreetNotifyUser("Sorry, a " + lowerCaseTypeName() + " could not be read because Mesquite does not support the VECTOR subcommand.");
						return false;
					}
					else if (token.equalsIgnoreCase("STANDARD")) {
						token = ParseUtil.getToken(command, startCharT); //)
					}
					else {
						token = ParseUtil.getToken(command, startCharT); //=
						token = (ParseUtil.getToken(command, startCharT)); // name of data

						dataName = token;
						data = project.getCharacterMatrixByReference(file, dataName);
						token = (ParseUtil.getToken(command, startCharT)); // )
					}
					token = ParseUtil.getToken(command, startCharT);  // =
				}
				else if (project.getNumberCharMatrices(file)>0) //should use first in this file
					data= project.getCharacterMatrix(file,0);
				if (data == null) {
					if (dataName!=null)
						data = project.getCharacterMatrixByReference(null, dataName);
					else if (project.getNumberCharMatrices()>0)
						data= project.getCharacterMatrix(0);
				}

				if (data==null) {
					if (!noWarnMissingReferent){
					referentWarnings++;
					if (referentWarnings<2)
						MesquiteMessage.discreetNotifyUser("Sorry, a " + lowerCaseTypeName() + " could not be read because its associated data set was not found.  This can occur if you are fusing files, or if you have edited files by hand or with another program.  Another possible cause is that your current Mesquite configuration doesn't include packages to read matrices of that type.  Try restarting Mesquite after selecting \"Use all installed packages\" in the Activate/Deactivate submenu of the File menu.\n\nCommand: " + command);
					else if (referentWarnings==2)
						MesquiteMessage.discreetNotifyUser("Another " + lowerCaseTypeName() + " could not be read because its associated data set was not found.  "
						+"With this one, warnings will cease, but there may be more such cases in the file.");
					}
					return false;
				}
				if (data.getSuppressSpecssetReading())
					return true;  //acting as everything is fine, but in fact specset was ignored
				if ("=".equals(token))
					token = ParseUtil.getToken(command, startCharT);  //getting name of first model

				//CharactersGroup defaultProperty =  new CharactersGroup();

				Object specification = getSpecification(token);

				SpecsSet specsSet= getNewSpecsSet(nameOfSpecsSet, data);
				specsSet.setNexusBlockStored(blockName);

				//=======================
				int lastChar = -1;
				boolean join = false;
				boolean nextIsCharList = !hasSpecificationTokens();
				while (token !=null && !token.equals(";") && token.length()>0) {
					if (token.equals("-")) {
						if (lastChar!=-1)
							join = true;
					}
					else {
						if (token != null && token.equals("."))
							token = Integer.toString(data.getNumChars());
						if (token.startsWith("-")) {
							if (lastChar!=-1)
								join = true;
							token = token.substring(1, token.length());
						}
						if (token.equals(":")) {
							nextIsCharList = true;
						}
						else if (token.equals(","))
							nextIsCharList=false;
						else if (nextIsCharList) {
							int whichChar = CharacterStates.toInternal(MesquiteInteger.fromString(token, false));
							if (MesquiteInteger.isCombinable(whichChar) && whichChar>=0) {
								if (whichChar>= data.getNumChars())
									whichChar = data.getNumChars()-1;

								if (join) {
									int skip = 1;
									//check here if next char is "\"; if so then need to skip
									int temp = startCharT.getValue();
									token = ParseUtil.getToken(command, startCharT); 
									if (token.equals("\\")){
										token = ParseUtil.getToken(command, startCharT); 
										int tSkip = MesquiteInteger.fromString(token, false);
										if (MesquiteInteger.isCombinable(tSkip))
											skip = tSkip;
									}
									else
										startCharT.setValue(temp);
									for (int j = lastChar; j<=whichChar; j += skip) {
										setSpecification(specsSet, specification,j);
									}
									join = false;
									lastChar = -1;
								}
								else {
									lastChar = whichChar;
									setSpecification(specsSet, specification,whichChar);
								}
							} else { // it might be a character set.  Added April 2018 DRM
								CharSelectionSet charSet = getSpecSetFromName(data,token);
								if (charSet!=null) {
									for (whichChar = 0; whichChar<data.getNumChars(); whichChar++) {
										if (charSet.isBitOn(whichChar))
											setSpecification(specsSet, specification,whichChar);
									}
								}
							}
						}
						else {
							specification =  getSpecification(token);
							nextIsCharList = true;
						}
					}
					token = ParseUtil.getToken(command, startCharT); 
				}

				//=======================

				if (isDefault) {
					if (!"UNTITLED".equals(specsSet.getName())) {
						data.storeSpecsSet(specsSet, getElementClass());
					}
					specsSet.addToFile(file, getProject(), this);
					SpecsSet ss = specsSet.cloneSpecsSet();
					data.setCurrentSpecsSet(ss, getElementClass());
				}
				else {
					data.storeSpecsSet(specsSet, getElementClass());
					specsSet.addToFile(file, getProject(), this);
				}
				return true;
			}
		}
		return false;
	}
}


