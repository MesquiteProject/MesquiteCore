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
import mesquite.lib.ObjectSpecsSet;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterModel;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.characters.ModelSet;
import mesquite.lib.ui.ListDialog;
import mesquite.lib.ui.MesquiteWindow;


/* ======================================================================== */
/** Manages spec sets.*/

/* ======================================================================== */
public abstract class MatrixSpecsSetManager extends SpecsSetManager {
	//"@MATRIXGROUP
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
		return null;
	}
	public void elementDisposed(FileElement e){
	}

	public abstract String nexusToken();
	public abstract SpecsSet getNewSpecsSet(String name);
	public boolean appropriateBlockForWriting(String blockName){
		return blockName.equalsIgnoreCase("SETS"); //only sets block for now
	}
	public boolean appropriateBlockForReading(String blockName){
		return blockName.equalsIgnoreCase("SETS"); //only sets block for now
	}
	public abstract Object getSpecification(String token);
	public abstract void setSpecification(SpecsSet specsSet, Object specification, int ic);

	/** Returns whether or not the NEXUS command has category tokens before character lists, e.g. the weight, or the partition name */
	public boolean hasSpecificationTokens(){
		return true;
	}

	/*.................................................................................................................*
	/** A method called immediately after the project has been established.*
	public void projectEstablished() {
		CharactersManager manager = (CharactersManager)findElementManager(mesquite.lib.characters.CharacterData.class);
		if (manager == null)
			return;
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.charactersMenu, manager.getListsSubmenu(), upperCaseTypeName() + "s", makeCommand("showList",  this));

		super.projectEstablished();
	}
	/*.................................................................................................................*
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
	/*.................................................................................................................*
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
	/*.................................................................................................................*/

	public boolean isSubstantive(){
		return false;  
	}
	/*.................................................................................................................*/
	public static String nexusCoreStringForSpecsSet(ObjectSpecsSet specsSet, CharacterData data){
		return "";
	}

	/*.................................................................................................................*/
	public abstract String nexusStringForSpecsSet(ObjectSpecsSet specsSet, MesquiteFile file, boolean isCurrent);
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (appropriateBlockForWriting(blockName)) { 
			String s= "";
			ListableVector datas = getProject().getCharacterMatrices();
			int numSets = datas.getNumSpecsSets(getElementClass());
			SpecsSetVector ssv = datas.getSpecSetsVector(getElementClass());
			if (ssv!=null){
				SpecsSet ms = (SpecsSet)datas.getCurrentSpecsSet(getElementClass());
				if (ms!=null && (ms.getNexusBlockStored()==null || blockName.equalsIgnoreCase(ms.getNexusBlockStored()))) {
					if (!ms.allDefault()) {
						ms.setNexusBlockStored(blockName);
						ms.setName("UNTITLED");
						s += nexusStringForSpecsSet( (ObjectSpecsSet)ms, file, true);
					}
				}


				for (int ims = 0; ims<numSets; ims++) {
					ObjectSpecsSet cs = (ObjectSpecsSet)datas.getSpecsSet(ims, getElementClass());
					if (!cs.allDefault())
						s += nexusStringForSpecsSet(cs, file, false);
				}
			}

			return s;
		}
		return null;
	}
	/*
	public  CharSelectionSet getSpecSetFromName(CharacterData data, String name){
		SpecsSetVector ssv = data.getSpecSetsVector(CharSelectionSet.class);
		if (ssv==null)
			return null;
		return (CharSelectionSet)ssv.getElement(name);
	}


	public  static Bits getCharBitsFromName(String name){
		return null;
	}
	 */

	int referentWarnings = 0;
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment, String fileReadingArguments){ 
		if (!appropriateBlockForReading(blockName))
			return false; 
		MesquiteInteger startPos = new MesquiteInteger(0);

		String commandName = ParseUtil.getToken(command, startPos);
		if (commandName == null || !commandName.equalsIgnoreCase(nexusToken()))
			return false;
		String token = ParseUtil.getToken(command, startPos);
		boolean isDefault = false;
		if ("*".equals(token)) {
			isDefault = true;
			token = ParseUtil.getToken(command, startPos);
		}
		String nameOfSpecsSet = StringUtil.deTokenize(token); // name of specs set 
		token = ParseUtil.getToken(command, startPos);
		if (token == null)
			return false;
		//String paradigmString = null;
		MesquiteProject project = file.getProject();
		ListableVector datas=project.getCharacterMatrices();
		int numMatrices = datas.size();

		if ("=".equals(token))
			token = ParseUtil.getToken(command, startPos);  //getting name of first model

		//CharactersGroup defaultProperty =  new CharactersGroup();

		Object specification = getSpecification(token);

		SpecsSet specsSet= getNewSpecsSet(nameOfSpecsSet);
		specsSet.setNexusBlockStored(blockName);

		//=======================
		int lastMatrix = -1;
		boolean join = false;
		boolean nextIsMatrixList = !hasSpecificationTokens();
		while (token !=null && !token.equals(";") && token.length()>0) {
			if (token.equals("-")) {
				if (lastMatrix!=-1)
					join = true;
			}
			else {
				if (token != null && token.equals("."))
					token = Integer.toString(numMatrices);
				if (token.startsWith("-")) {
					if (lastMatrix!=-1)
						join = true;
					token = token.substring(1, token.length());
				}
				if (token.equals(":")) {
					nextIsMatrixList = true;
				}
				else if (token.equals(","))
					nextIsMatrixList=false;
				else if (nextIsMatrixList) {
					int whichMatrix = CharacterStates.toInternal(MesquiteInteger.fromString(token, false));
					if (MesquiteInteger.isCombinable(whichMatrix) && whichMatrix>=0) {
						if (whichMatrix>= numMatrices)
							whichMatrix = numMatrices-1;

						if (join) {
							int skip = 1;
							//check here if next char is "\"; if so then need to skip
							int temp = startPos.getValue();
							token = ParseUtil.getToken(command, startPos); 
							if (token.equals("\\")){
								token = ParseUtil.getToken(command, startPos); 
								int tSkip = MesquiteInteger.fromString(token, false);
								if (MesquiteInteger.isCombinable(tSkip))
									skip = tSkip;
							}
							else
								startPos.setValue(temp);
							for (int j = lastMatrix; j<=whichMatrix; j += skip) {
								setSpecification(specsSet, specification,j);
							}
							join = false;
							lastMatrix = -1;
						}
						else {
							lastMatrix = whichMatrix;
							setSpecification(specsSet, specification,whichMatrix);
						}
					} 
				}
				else {
					specification =  getSpecification(token);
					nextIsMatrixList = true;
				}
			}
			token = ParseUtil.getToken(command, startPos); 
		}

		//=======================

		if (isDefault) {
			if (!"UNTITLED".equals(specsSet.getName())) {
				datas.storeSpecsSet(specsSet, getElementClass());
			}
			specsSet.addToFile(file, getProject(), this);
			SpecsSet ss = specsSet.cloneSpecsSet();
			datas.setCurrentSpecsSet(ss, getElementClass());
		}
		else {
			datas.storeSpecsSet(specsSet, getElementClass());
			specsSet.addToFile(file, getProject(), this);
		}
		return true;
	}


}


