/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ManageCharacters;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;

import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.MolecularData;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/**  Manages character matrices, including reading and writing from files (for which it relies on managers of the particular data types).
This has some methods and classes for recording the history of changes of the cells of the matrix.  These functions are not yet publicly available. */
public class ManageCharacters extends CharactersManager {
	/*.................................................................................................................*/
	public String getName() {
		return "Character Matrix manager";//name must be updated in BasicFileCoord
	}
	public String getExplanation() {
		return "Coordinates the management of character data matrices." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(CharMatrixFiller.class, "New Character matrices can be created from various sources.",
		"You can create a new matrix from various sources by selecting items from the Make New Matrix From... submenu of the Characters menu.");
		e.setAlternativeEmployerLabel("Create and Add Matrix to File/Project");
		EmployeeNeed e2 = registerEmployeeNeed(CharMatrixManager.class, "Character matrices of different types (continuous, DNA, etc.) have different managing modules",
		"These are activated automatically.");
		EmployeeNeed e3 = registerEmployeeNeed(mesquite.lists.CharacterList.CharacterList.class, "The List of Characters window permits viewing and modifying of character properties",
		"The List of Characters window is available in the Characters menu.");
	}
	public void elementsReordered(ListableVector v){
		if (v == getProject().datas){
			NexusBlock.equalizeOrdering(v, getProject().getNexusBlocks());
		}
	}
	MesquiteMenuItemSpec calw = null;
	/*.................................................................................................................*/
	ListableVector taxas; //local reference to Project's vector of taxa blocks
	MesquiteSubmenuSpec  listsSubMenu; //submenu to show list windows for character specssets
	static boolean warnChecksum = true;
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		hireAllEmployees(CharMatrixManager.class);
		setMenuToUse(MesquiteTrunk.charactersMenu);
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}
	/* ................................................................................................................. */
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (employee instanceof DataWindowMaker){
			if (calw != null) calw.setEnabled(getNumListWindows()>0);
			resetAllMenuBars();
		}
	}
	int getNumListWindows(){
		int count = 0;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant)
				if (((ManagerAssistant)e).getName().equals("Character List"))
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		if (e instanceof CharacterData){
			CharacterData t = (CharacterData)e;
			return showMatrixEditor(t);
		}
		return null;
	}
	/*.................................................................................................................*/
	public void deleteElement(FileElement e){
		if (e instanceof CharacterData){
			CharacterData t = (CharacterData)e;
			t.doom();
			getProject().removeFileElement(t);//must remove first, before disposing
			t.dispose();
		}
	}
	private String allValid(CharacterData data){
		for (int ic = 0; ic < data.getNumChars(); ic++)
			for (int it = 0; it < data.getNumTaxa(); it++){
				if (!data.isValid(ic, it)){
					return " char " + (ic+1) + " taxon " + (it+1);
				}
			}
		return null;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
		MesquiteProject proj = getProject();
		for (int i= 0; i<proj.getNumberCharMatrices(); i++){
			CharacterData data = proj.getCharacterMatrix(i);
			if (data.getFile() == f){
				String s = data.checkIntegrity();
				if (s != null){
					discreetAlert("Warning: Matrix \"" + data.getName() + "\" has a problem: " + s);
					MesquiteTrunk.errorReportedDuringRun = true;
				}
				else {
					String result = null;
					if ((result = allValid(data))!= null){
						discreetAlert("Warning: Matrix \"" + data.getName() + "\" has a problem: Some character states are invalid.  The file being read may have been corrupted or incorrectly formatted. First problem found:" + result);
						MesquiteTrunk.errorReportedDuringRun = true;
					}
				}
			}
		}
	}
	/*.................................................................................................................*/
	MesquiteModule showMatrixEditor(CharacterData data){
		if (isDoomed() || getProject() == null)
			return null;
		Object obj = doCommand("showDataWindow", getProject().getCharMatrixReferenceInternal(data), CommandChecker.defaultChecker);
		if (obj instanceof MesquiteModule)
			return (MesquiteModule)obj;
		else
			return null;
	}
	public MesquiteModule getListOfCharactersModule(CharacterData data, boolean showIfClosed){
		boolean found = false;
		if (data == null)
			return null;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant)
				if (((ManagerAssistant)e).showing(data)&& ((ManagerAssistant)e).getName().equals("Character List")) {
					((ManagerAssistant)e).getModuleWindow().setVisible(true);
					if (calw != null) calw.setEnabled(getNumListWindows()>0);
					resetAllMenuBars();
					return ((ManagerAssistant)e);
				}
		}
		if (!showIfClosed)
			return null;
		ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("Character List"));
		if (lister!=null) {
			lister.showListWindow(data);
			if (calw != null) calw.setEnabled(true);
			resetAllMenuBars();
			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
				lister.getModuleWindow().setVisible(true);
		}
		return lister;
	}
	/*.................................................................................................................*/
	MesquiteModule showCharactersList(CharacterData data){
		boolean found = false;
		if (data == null)
			MesquiteMessage.warnProgrammer("Data null in showCharactersList in ManageCharacters");
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant)
				if (((ManagerAssistant)e).showing(data)&& ((ManagerAssistant)e).getName().equals("Character List")) {
					((ManagerAssistant)e).getModuleWindow().setVisible(true);
					if (calw != null) calw.setEnabled(getNumListWindows()>0);
					resetAllMenuBars();
					return ((ManagerAssistant)e);
				}
		}
		ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("Character List"));
		if (lister!=null) {
			lister.showListWindow(data);
			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null){
				lister.getModuleWindow().setVisible(true);
			}
				if (calw != null) calw.setEnabled(getNumListWindows()>0);
				resetAllMenuBars();
		}
		return lister;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "-", null);
		MesquiteSubmenuSpec mmis = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "List of Characters", makeCommand("showCharacters",  this),  (ListableVector)getProject().datas);
		calw = getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "Close All Lists of Characters", makeCommand("closeAllListWindows",  this));
		calw.setEnabled(false);
		mmis.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "-", null);
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "List of Character Matrices", makeCommand("showDatasList",  this));
		MesquiteCommand ccm= makeCommand("showDatasList" ,  this);
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "Concatenate Character Matrices...", ccm);
		ccm.setDefaultArguments("concat");
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "Delete Character Matrices...", makeCommand("deleteMatrices",  this));
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "New Empty Matrix...", makeCommand("newMatrix",  this));
		getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "Make New Matrix from", makeCommand("newFilledMatrix",  this), CharMatrixFiller.class);
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "New Linked Matrix...", makeCommand("newLinkedMatrix",  this));
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "-", null);
		getFileCoordinator().addModuleMenuItems( MesquiteTrunk.charactersMenu, makeCommand("newAssistant",  getFileCoordinator()), FileAssistantM.class);
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, "-", null);
		taxas = getProject().getTaxas();
		taxas.addListener(this);
		listsSubMenu = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu, "Lists");
		listsSubMenu.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		super.projectEstablished();
	}
	public MesquiteSubmenuSpec getListsSubmenu(){
		return listsSubMenu;
	}
	/*.................................................................................................................*/
	public void endJob(){
		if (taxas!=null)
			taxas.removeListener(this);
		super.endJob();
	}
	/*.................................................................................................................*/
	public void exportMatrix(CharacterData data, String path) {
		if (data==null)
			return;
		incrementMenuResetSuppression();
		Taxa taxa = data.getTaxa();

		FileCoordinator coord = getFileCoordinator();
		MesquiteFile tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(path), CommandChecker.defaultChecker); //TODO: never scripting???
		TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
		Taxa newTaxa =taxa.cloneTaxa();
		newTaxa.addToFile(tempDataFile, null, taxaManager);

		CharacterData newData = data.cloneData();
		newData.setName(data.getName());
		newData.addToFile(tempDataFile, getProject(), null);
		coord.writeFile(tempDataFile);
		tempDataFile.close();
		decrementMenuResetSuppression();
	}
	/*.................................................................................................................*/
	/** check if any adjustments are needed before writing, .e.g. resolve name conflicts.  Format is file type, e.g. NEXUS, NEXML. */
	public void preWritingCheck(MesquiteFile file, String format){	
		if (format.equalsIgnoreCase("NEXUS")){
			int numSets = getProject().getNumberCharMatrices();
			ListableVector datas = getProject().getCharacterMatrices();
			String[] names = new String[numSets];
			boolean changed = false;
			String changes = "";
			for (int i=0; i<numSets; i++) {
				CharacterData data = getProject().getCharacterMatrix(i);
				if (data.getFile() == file && data.getWritable()){
					String cName = data.getName();
					if (StringArray.indexOf(names, cName)>=0){
						data.setName(datas.getUniqueName(cName));
						changed = true;
						changes += " " + cName + " renamed to " + data.getName();
					}
					else
						names[i] = data.getName();
				}
			}
			if (changed)
				discreetAlert("Some character matrices had the same name (i.e., the same title).  This is not permitted in NEXUS files; some names were changed (" + changes + ")");
		}
	}
	/*.................................................................................................................*/
	void deleteTaxa(Taxa taxa){
		if (taxa==null)
			return;
		boolean someDeleted = true;
		while (someDeleted){
			someDeleted = false;
			int numSets = getProject().getNumberCharMatrices(taxa);
			for (int i=numSets; i>=0 && !someDeleted ; i--) {
				CharacterData data = getProject().getCharacterMatrix(taxa, i);
				if (data!=null && data.getTaxa()==taxa) {
					getProject().removeFileElement(data);//must remove first, before disposing
					someDeleted = true;
					data.dispose();
				}
			}
		}
	}
	/*.................................................................................................................*/
	void deleteMatricesWithoutTaxa(){
		if (taxas==null)
			return;
		Listable[] datas = getProject().getCharacterMatrices().getListables();
		for (int i = 0; i<datas.length; i++) {
			CharacterData data = (CharacterData)datas[i];
			if (data!=null && taxas.indexOf(data.getTaxa())<0) {
				getProject().removeFileElement(data);//must remove first, before disposing
			}
		}
		for (int i = 0; i<datas.length; i++) {
			CharacterData data = (CharacterData)datas[i];
			if (data!=null && taxas.indexOf(data.getTaxa())<0) {
				data.dispose();
			}
		}

	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj instanceof Taxa) {
			deleteTaxa((Taxa)obj);

		}
	}
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj == taxas && (code == MesquiteListener.PARTS_DELETED || code == MesquiteListener.PARTS_CHANGED)) {
			deleteMatricesWithoutTaxa();

		}
	}
	//
	public String[] dataClassesAvailable() {
		int count =0;
		for (int i = 0; i<getNumberOfEmployees() ; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof CharMatrixManager) 
				count++;
		}
		String[] names = new String[count];
		count =0;
		for (int i = 0; i<getNumberOfEmployees() ; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof CharMatrixManager) {
				names[count] = ((CharMatrixManager)e).getDataClassName();
				count++;
			}
		}
		return names;
	}
	/*.................................................................................................................*/
	public CharacterData newCharacterData(Taxa taxa, int numChars, String dataType) {
		CharMatrixManager manager = findCharacterTypeManager(dataType);
		if (manager == null )
			return null;
		return manager.getNewData(taxa, numChars);
	}
	/*.................................................................................................................*/
	public CharMatrixManager findCharacterTypeManager(String dataType) {
		if (dataType == null)
			return null;
		for (int i = 0; i<getNumberOfEmployees() ; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof CharMatrixManager) {
				if (dataType.equalsIgnoreCase(((CharMatrixManager)e).getDataClassName())) {
					return ((CharMatrixManager)e);
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public CharMatrixManager findCharacterTypeManager(Class dataClass) {
		if (dataClass == null)
			return null;
		for (int i = 0; i<getNumberOfEmployees() ; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof CharMatrixManager) {
				if (dataClass == ((CharMatrixManager)e).getDataClass()) {
					return ((CharMatrixManager)e);
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	public void elementDisposed(FileElement e){
		NexusBlock nb = findNEXUSBlock(e);
		if (nb!=null)
			removeNEXUSBlock(nb);
	}
	/*.................................................................................................................*/
	public NexusBlock elementAdded(FileElement data){
		if (data == null || !(data instanceof CharacterData))
			return null;
		resetAllMenuBars();
		NexusBlock nb = findNEXUSBlock(data);
		if (nb==null) {
			CharactersBlock cb = new CharactersBlock(data.getFile(), this);
			cb.setData((CharacterData)data);
			addNEXUSBlock(cb);
			return cb;
		}
		else return nb;
	}
	/*.................................................................................................................*/
	public Snapshot getIDSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (file == null || file == getProject().getHomeFile()){
			int count = 0;
			String addendum = "";
			if (MesquiteTrunk.errorReportedDuringRun)
				addendum = " errorReportedDuringRun";
			if (MainThread.emergencyCancelled)
				addendum += " emergencyCancelDuringRun";

			for (int i = 0; i< getProject().getNumberCharMatrices(); i++) {
				CharacterData data = getProject().getCharacterMatrix(i);
				if (data.getWritable()){
					String dAnd = "";
					if (data.badImport)
						dAnd = " errorDuringImport";
					if (StringUtil.blank(data.getUniqueID()))
						temp.addLine("setID " + count + " " + data.getAssignedIDNumber());
					else
						temp.addLine("setID " + count + " " + data.getAssignedIDNumber() + " " + data.getUniqueID());
					Snapshot fromMatrix = data.getSnapshot(file);
					if (fromMatrix != null && fromMatrix.getNumLines() > 0) {
						temp.addLine("tell It");
						temp.incorporate(fromMatrix, true);
						temp.addLine("endTell");
					}

					temp.addLine("mqVersion " + MesquiteTrunk.mesquiteTrunk.getVersionInt());
					temp.addLine("checksumv " + count + " " + CharacterData.getCurrentChecksumVersion() + " " + data.getChecksumForFileRecord(CharacterData.getCurrentChecksumVersion()) + " " + data.getUniqueID() + "  " + data.getChecksumSummaryString() + "  " + getProjectStatus(data) + addendum + dAnd);
					temp.addLine("mqVersion");
					count++;
				}
			}
		}
		return temp;
	}
	int lastWriterVersion = MesquiteInteger.unassigned;
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		/*
 		if (file == null || file == getProject().getHomeFile()){
	 		for (int i = 0; i< getProject().getNumberCharMatrices(); i++) {
	 			temp.addLine("setID " + i + " " + getProject().getCharacterMatrix(i).getAssignedID());
  	 			Snapshot fromMatrix = getProject().getCharacterMatrix(i).getSnapshot(file);
		  	 	if (fromMatrix != null && fromMatrix.getNumLines() > 0) {
					temp.addLine("tell It");
					temp.incorporate(fromMatrix, true);
					temp.addLine("endTell");
	 			}
	 			temp.addLine("checksum " + i + " " + getProject().getCharacterMatrix(i).getChecksum());
	 		}
 		}
		 */
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals("Character Matrices List")) {
				temp.addLine("showDatasList ", e); 
			}
		}
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals("Character List")) {
				CharacterData data = (CharacterData)e.doCommand("getData", null, CommandChecker.defaultChecker);
				if (data != null)
					temp.addLine("showCharacters " + getProject().getCharMatrixReferenceExternal(data), e); //getProject().getMatrixNumber(data), e); 
			}
		}
		return temp;
	}
	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger(0);
	public MesquiteFile chooseFile( CharacterData data){  //changed 13 Dec 01 to specify data so as to do check on which file can be
		MesquiteFile file=null;
		Taxa taxa = data.getTaxa();
		if (getProject().getNumberLinkedFiles()==1)
			file = getProject().getHomeFile();
		else if (MesquiteThread.isScripting())
			file = taxa.getFile();
		else {
			Listable[] files = getProject().getFiles().getElementArray();
			if (files.length >1) {
				int count = 0;
				boolean taxaFound = false;
				for (int i=0; i<files.length; i++) {
					if (!taxaFound && files[i] == taxa.getFile())
						taxaFound = true;
					if (taxaFound)
						count++;
				}
				if (count!=files.length){
					Listable[] legalFiles = new Listable[count];
					count = 0;
					taxaFound = false;
					for (int i=0; i<files.length; i++) {
						if (!taxaFound && files[i] == taxa.getFile())
							taxaFound = true;

						if (taxaFound) {
							legalFiles[count] = files[i];
							count++;
						}
					}
					files = legalFiles;
				}

			}
			if (files.length == 1)
				return (MesquiteFile)files[0];
			file = (MesquiteFile)ListDialog.queryList(containerOfModule(), "Select file", "Select file to which to add the new character matrix",MesquiteString.helpString, files, 0);
		}
		return file;
	}
	String getProjectStatus(CharacterData data){
		//indicate whether file is linked, number of linked files, number of matrices total in project
		String s = "";
		MesquiteFile f = data.getFile();
		if (f != getProject().getHomeFile())
			s += " InLinked ";
		s += " NumFiles " + getProject().getNumberLinkedFiles();
		s += " NumMatrices " + getProject().getNumberCharMatrices();
		return s;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Creates a new empty character data matrix", "[number of characters] [title] [data type name]", commandName, "newMatrix")) {
			//ask user how which taxa, how many characters
			//create chars block and add to file
			//return chars
			incrementMenuResetSuppression();
			CharacterData newMatrix=null;
			Taxa taxa = null;
			if (getProject().getNumberTaxas()==0) {
				alert("Data matrix cannot be created until taxa exist in file.");
				decrementMenuResetSuppression();
				return null;
			}
			else {
				String tx = parser.getFirstToken(arguments);
				if (!StringUtil.blank(tx) && tx.equalsIgnoreCase("taxa")){
					parser.getNextToken(); //=
					long id = MesquiteLong.fromString(parser.getNextToken());
					if (MesquiteLong.isCombinable(id))
						taxa = getProject().getTaxaByID(id);
					arguments = arguments.substring(parser.getPosition(), arguments.length());
				}
				if (taxa == null)
					taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to create a new character matrix?");
			}
			if (taxa==null){
				decrementMenuResetSuppression();
				return null;
			}
			int numChars= 1;
			String title= getProject().getCharacterMatrices().getUniqueName("Character Matrix");
			String dataTypeName = CategoricalData.DATATYPENAME;
			MesquiteBoolean answer = new MesquiteBoolean(true);
			if (StringUtil.blank(arguments)) {
				MesquiteInteger buttonPressed = new MesquiteInteger(1);
				MesquiteInteger ion = new MesquiteInteger(2);
				MesquiteString iod = new MesquiteString(dataTypeName);
				MesquiteString ios = new MesquiteString(title);
				String[] names = dataClassesAvailable();

				String s = "Use this dialog box to create a new, named matrix of characters.  You must choose the type of data from the list.";
				StringIntegerListDlog dialog = new StringIntegerListDlog(containerOfModule(), "New Character Matrix", "Name of character matrix", "Number of characters", "Type of Data", names,true,ios,ion, iod,1, MesquiteInteger.unassigned, buttonPressed,s);

				answer.setValue((buttonPressed.getValue()==0));

				numChars= ion.getValue();
				title= ios.getValue();
				dataTypeName = iod.getValue();
			}
			else {
				MesquiteInteger io = new MesquiteInteger(0);
				numChars= MesquiteInteger.fromString(arguments, io);
				title= ParseUtil.getToken(arguments, io);
				dataTypeName= ParseUtil.getToken(arguments, io);
			}
			if (answer.getValue() && numChars>0 && numChars< 10000000) { //TODO: put CONSTANTS here for limits???
				CharMatrixManager manager = findCharacterTypeManager(dataTypeName);
				if (manager == null) {
					decrementMenuResetSuppression();
					return null;
				}
				newMatrix = manager.getNewData(taxa, numChars);
				if (newMatrix == null) {
					alert("Sorry, a character data matrix could not be made.  There may be no data matrix types defined.");
					decrementMenuResetSuppression();
					return null;
				}
				if (title!=null)
					newMatrix.setName(title);

				MesquiteFile file=chooseFile( newMatrix);
				newMatrix.addToFile(file, getProject(), this);  

				MesquiteModule mb = findNearestColleagueWithName("Data Window Coordinator");
				if (mb != null)
					mb.doCommand("showDataWindow", MesquiteInteger.toString(getProject().getNumberCharMatrices()-1), checker);
			}
			resetAllMenuBars();
			decrementMenuResetSuppression();
			return newMatrix;
		}
		else if (checker.compare(this.getClass(), "Shows window for last data matrix", "", commandName, "showLastMatrixWindow")) {
			MesquiteModule mb = findNearestColleagueWithName("Data Window Coordinator");
			if (mb != null)
				mb.doCommand("showDataWindow", MesquiteInteger.toString(getProject().getNumberCharMatrices()-1), checker);
		}
		else if (checker.compare(this.getClass(), "Saves a copy of the character data matrix to a separate file", "[id number of data matrix]", commandName, "exportMatrix")) {
			CharacterData d =  getProject().getCharacterMatrixByReference(checker.getFile(), parser.getFirstToken(arguments), true);
			if (d == null)
				d =  getProject().getCharacterMatrixByReference(checker.getFile(), parser.getFirstToken(arguments));
			if (d == null){ //probably not needed but this used to be here
				int t = MesquiteInteger.fromString(parser.getFirstToken(arguments));
				if (MesquiteInteger.isCombinable(t) && t< getProject().getNumberCharMatrices()) {
					long id  = MesquiteLong.fromString(parser.getNextToken());
					d = getProject().getCharacterMatrix(t);
				}
			}
			if (d!=null) {
				String path = MesquiteFile.saveFileAsDialog("Save copy of matrix to file");
				if (!StringUtil.blank(path))
					exportMatrix(d, path);
			}


		}
		else if (checker.compare(this.getClass(), "Sets the ID number of a character data matrix", "[number of matrix][id number of data matrix]", commandName, "setID")) {
			int t = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			long id  = MesquiteLong.fromString(parser.getNextToken());
			String blockID  = parser.getNextToken();
			if (MesquiteInteger.isCombinable(t) || !StringUtil.blank(blockID)) {
				if (!StringUtil.blank(blockID) && getNumberCharMatricesWithUniqueID(blockID)==1){
					CharacterData d = getProject().getCharacterMatrixByUniqueID(blockID, 0);
					if (d!=null) {
						d.setAssignedIDNumber(id);
						return d;
					}
				}
				if (t< getProject().getNumberCharMatrices(CommandRecord.getScriptingFileS())) {
					CharacterData d = getProject().getCharacterMatrix(CommandRecord.getScriptingFileS(), t);
					if (d!=null) {
						d.setAssignedIDNumber(id);

						return d;
					}
				}
				else if (t< getProject().getNumberCharMatrices()) {
					CharacterData d = getProject().getCharacterMatrix( t);
					if (d!=null) {
						d.setAssignedIDNumber(id);

						return d;
					}
				}
			}

		}
		else if (checker.compare(this.getClass(), "Indicates version of Mesquite that had saved the file (for ID snapshot)", "[version of Mesquite saving matrix]", commandName, "mqVersion")) {
			lastWriterVersion = MesquiteInteger.fromString(parser.getFirstToken(arguments));

		}
		else if (checker.compare(this.getClass(), "Indicates the checksum of a matrix; new version", "[number of matrix][id number of data matrix]", commandName, "checksumv")) {
			int t = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			int version  = MesquiteInteger.fromString(parser.getNextToken());
			long checksumRecorded  = MesquiteLong.fromString(parser.getNextToken());
			boolean errorReported = (arguments != null && StringUtil.indexOfIgnoreCase(arguments, "errorReportedDuringRun")>=0 || StringUtil.indexOfIgnoreCase(arguments, "emergencyCancelDuringRun")>=0);
			boolean badImport = (arguments != null && StringUtil.indexOfIgnoreCase(arguments, "errorDuringImport")>=0);
			String blockID  = parser.getNextToken();
			CharacterData d = null;
			boolean usedID = false;
			if (!StringUtil.blank(blockID) && getNumberCharMatricesWithUniqueID(blockID)==1){
				d = getProject().getCharacterMatrixByUniqueID(blockID, 0);
				usedID = true;
			}
			else {
				if (MesquiteInteger.isCombinable(t) && t< getProject().getNumberCharMatrices(CommandRecord.getScriptingFileS())) 
					d = getProject().getCharacterMatrix(CommandRecord.getScriptingFileS(), t);
			}
			if (d!=null) {
				long checksumAsRead = d.getChecksumForFileRecord(version);
				if (!d.suppressChecksum && checksumAsRead != checksumRecorded) {
					//there appears to be a problem, but first check that the matrix wasn't misidentified because a unique ID wasn't used
					if (!usedID){
						for (int i = 0; i<getProject().getNumberCharMatrices(); i++){
							CharacterData cd = getProject().getCharacterMatrix(i);
							long checksumI = cd.getChecksumForFileRecord(version);
							if (checksumI == checksumRecorded){  //OK, other matrix found with this checksum; assumed to be OK
								cd.suppressChecksum = false;  //other matrix checksum found, so suppress its warnings
								return null;
							}
						}
					}
					String warning = "Error: checksum on data matrix \"" + d.getName() + "\" (" + d.getDataTypeName() + ") does not match that expected and stored in file.  Either the matrix has been modified with a program other than Mesquite, or the file had another issue already reported to you, or there is a bug in Mesquite.  If you are unaware of an intentional change, it is recommended that you use Save As to leave the previous copy of the file intact.";


					String diffFileSave = "";
					double diff = 0;
					if (getProject().timePreviouslySavedAsRecorded != 0 && getProject().timePreviouslySavedByFile != 0)
						diff = Math.abs(getProject().timePreviouslySavedAsRecorded - getProject().timePreviouslySavedByFile)/1000.0;
					if (diff > 0) 
						diffFileSave = "\nFile save time difference from recorded: " + diff;
					String remainingDetails = parser.getRemaining();
					String dataDetails = "\nMatrix details:\nChecksumAsRead " + checksumAsRead + "   " + d.getChecksumSummaryString() + getProjectStatus(d) + "\nRecorded details:\nChecksumRecorded " + checksumRecorded + "  " + remainingDetails;
					String details = "\n[DETAILS for data matrix " + d.getName() + " (" + d.getDataTypeName() + ")" + dataDetails + diffFileSave + "]";
					details += "\n[recorded block id: " + blockID + "; and of compared matrix: " + d.getUniqueID() + " usedID? " + usedID + "]";
					if (MesquiteInteger.isCombinable(lastWriterVersion))
						details += "\n[Written by version " + MesquiteInteger.toString(lastWriterVersion) + "]";
					else 
						details += "\n[Written by version: Not Recorded]";
					if (MesquiteTrunk.errorReportedDuringRun)
						details += " [NOTE: a crash or other error occurred previously during this current run of Mesquite.  Thus, the checksum error may be a result of that crash or error.]";
					if (errorReported)
						details += " [NOTE: a crash or other error had been reported in the run of Mesquite in which the file had been saved.  Thus, the checksum error may be a result of that crash or error.]";
					if (badImport)
						details += " [NOTE: This matrix was not read successfully when originally imported.]";

					String integrity = d.checkIntegrity();
					if (integrity != null) 
						details += "\n[NOTE: the data matrix appears corrupt; it is possible that it had been saved incorrectly in the file [" + integrity + "]]";
					ListableVector datas = getProject().getCharacterMatrices();
					if (datas != null && datas.size() > 1){
						details += "\nSummary of matrices in file:";
						for (int i=0; i<datas.size(); i++){
							CharacterData dd = (CharacterData)datas.elementAt(i);
							details += "\n    matrix " + dd.getName() + "  ID  " + dd.getUniqueID() + "  for taxa " + dd.getTaxa().getName();
						}
					}
					if (diff>20){//assume file writing is at most 20 seconds off; anything more assumes user fiddling so don't report!!!!
						logln("Note: checksum on data matrix \"" + d.getName() + "\" (" + d.getDataTypeName() + ") does not match that expected and stored in file.  This appears to be caused by the file having been modified by a program other than Mesquite.");
						logln(details);
					}
					else if (getProject().timePreviouslySavedAsRecorded == 0){//no record of time saved; may be user fiddling so don't report!!!!
						logln("Note: checksum on data matrix \"" + d.getName() + "\" (" + d.getDataTypeName() + ") does not match that expected and stored in file.  This may have been caused by the file having been modified by a program other than Mesquite.");
						logln(details);
					}
					else if (errorReported){//assume file writing had had a problem
						logln("Note: checksum on data matrix \"" + d.getName() + "\" (" + d.getDataTypeName() + ") does not match that expected and stored in file.  A crash or other error had been reported in the run of Mesquite in which the file had been saved.  Thus, the checksum error is probably a result of that crash or error..");
						logln(details);
					}
					else if (badImport){//assume file import had had a problem
						logln("Note: checksum on data matrix \"" + d.getName() + "\" (" + d.getDataTypeName() + ") does not match that expected and stored in file.  The matrix had not been read successfully when originally imported.  Thus, the checksum error is probably a result of that crash or error..");
						logln(details);
					}
					else if (MesquiteTrunk.errorReportedDuringRun){//assume crash caused the problem
						logln("Note: checksum on data matrix \"" + d.getName() + "\" (" + d.getDataTypeName() + ") does not match that expected and stored in file.  A crash or other error occurred previously during this current run of Mesquite.  Thus, the checksum error may be a result of that crash or error.");
						logln(details);
					}
					else if (!MesquiteInteger.isCombinable(lastWriterVersion) && StringUtil.blank(remainingDetails)){
						logln("Note: checksum on data matrix \"" + d.getName() + "\" (" + d.getDataTypeName() + ") does not match that expected and stored in file.  This file appears to be written by an old version of Mesquite, and no details to help diagnose the issue were written.  The checksum error may be innocent.");
						logln(details);
					}
					else if (!warnChecksum)
						logln(warning + " If this appears to be due to a bug in Mesquite, please report it to info@mesquiteproject.org." + details);
					else {
						if (d.problemReading != null){
							details += "\n[PROBLEM READING " + d.problemReading + "]";
							d.problemReading = null;
						}
						logln(warning +  details);
						logln("checksumv arguments as written: " + arguments);
						reportableAlert(warning, details);
						if (!errorReported) { // && getProject().timePreviouslySavedAsRecorded != 0){
							alert("Please send us a copy of your data file.\n\nThe checksum error that was just reported may represent a bug in Mesquite.  It would help us to diagnose this to have a copy of your data file. " +
							"If possible, please send us a copy of this data file to info@mesquiteproject.org.  We will not release or make any use of your data file except to diagnose possible bugs in Mesquite.");
						}
					}
				}
				d.suppressChecksum = false;
			}
		}
		else if (checker.compare(this.getClass(), "Indicates the checksum of a matrix.  Old style; for reading old matrices", "[number of matrix][id number of data matrix]", commandName, "checksum")) {
			int t = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			long cs  = MesquiteLong.fromString(parser.getNextToken());
			String blockID  = parser.getNextToken();
			CharacterData d = null;
			if (!StringUtil.blank(blockID) && getNumberCharMatricesWithUniqueID(blockID)==1)
				d = getProject().getCharacterMatrixByUniqueID(blockID, 0);
			else if (MesquiteInteger.isCombinable(t) && t< getProject().getNumberCharMatrices(CommandRecord.getScriptingFileS())) 
				d = getProject().getCharacterMatrix(CommandRecord.getScriptingFileS(), t);
			if (d!=null) {
				if (!d.suppressChecksum && d.getChecksum() != cs) {
					String warning = "Error: checksum on data matrix \"" + d.getName() + "\" does not match that expected and stored in file.  Either the matrix has been modified with a program other than Mesquite, or there is a bug in Mesquite.  If you are unaware of an intentional change, it is recommended that you use Save As to leave the previous copy of the file intact.  If this appears to be due to a bug in Mesquite, please report it to info@mesquiteproject.org";
					if (!warnChecksum)
						logln(warning);
					else if (!AlertDialog.query(containerOfModule(), "Checksum doesn't match",warning + "\n\nYou may suppress warnings of this type within this run of Mesquite.", "Continue", "Suppress warnings"))
						warnChecksum = false;
				}
				d.suppressChecksum = false;
			}
		}
		else if (checker.compare(this.getClass(),  "Creates a new character data matrix linked to an existing one (i.e. characters in the two matrixes are associated 1:1, forcing the two matrices to maintain the same number of characters).", "[number of data matrix with which to be linked] [title] [data type name]", commandName, "newLinkedMatrix")) {
			//ask user how which taxa, how many characters
			//create chars block and add to file
			//return chars
			incrementMenuResetSuppression();
			CharacterData newMatrix=null;
			Taxa taxa = null;
			if (getProject().getNumberTaxas()==0) {
				alert("Data matrix cannot be created until taxa exist in file.");
				decrementMenuResetSuppression();
				return null;
			}

			CharacterData other=null;
			String title= getProject().getCharacterMatrices().getUniqueName("Character Matrix");
			String dataTypeName = CategoricalData.DATATYPENAME;
			MesquiteBoolean answer = new MesquiteBoolean(true);
			if (StringUtil.blank(arguments)) {

				MesquiteInteger buttonPressed = new MesquiteInteger(1);
				ExtensibleExplDialog makeLinkedDialog = new ExtensibleExplDialog(containerOfModule(), "Make Linked Matrix",buttonPressed);
				makeLinkedDialog.setExplainable(new MesquiteString("With this you create a new character matrix that is linked to an existing one.  By being linked, the two matrices are constrained to have the same number of characters.  If characters are deleted from or added to one matrix, the corresponding characters will be deleted from or added to the other.")); 
				java.awt.List types = makeLinkedDialog.addList(dataClassesAvailable(), new MesquiteInteger(0), "Make new character matrix of what type?");
				SingleLineTextField nameTaxa = makeLinkedDialog.addTextField("Name of new matrix", "Character Matrix", 30);
				java.awt.List linkTo = makeLinkedDialog.addList(getProject().getCharacterMatrices(), new MesquiteInteger(0), "Link new matrix to which matrix?");

				makeLinkedDialog.completeAndShowDialog(true);

				if (buttonPressed.getValue()==0) {
					other = getProject().getCharacterMatrix(linkTo.getSelectedIndex());
					dataTypeName = types.getSelectedItem();
					title = nameTaxa.getText();
				}
				if (other==null || dataTypeName == null){
					decrementMenuResetSuppression();
					return null;
				}
			}
			else {
				MesquiteInteger io = new MesquiteInteger(0);
				int otherNum= MesquiteInteger.fromString(arguments, io);
				if (!MesquiteInteger.isCombinable(otherNum)){
					decrementMenuResetSuppression();
					return null;
				}
				other = getProject().getCharacterMatrix(otherNum);
				if (other==null) {
					decrementMenuResetSuppression();
					return null;
				}
				title= ParseUtil.getToken(arguments, io);
				dataTypeName= ParseUtil.getToken(arguments, io);
			}

			newMatrix = newCharacterData(other.getTaxa(), other.getNumChars(), dataTypeName);
			if (newMatrix == null) {
				alert("Sorry, a character data matrix could not be made.  There may be no data matrix types defined.");
				decrementMenuResetSuppression();
				return null;
			}
			MesquiteFile file=chooseFile( newMatrix);
			if (title!=null)
				newMatrix.setName(title);
			other.addToLinkageGroup(newMatrix);

			newMatrix.addToFile(file, getProject(), this);  //TODO: which file to add to??  how to choose?

			MesquiteModule mb = findNearestColleagueWithName("Data Window Coordinator");
			if (mb != null)
				mb.doCommand("showDataWindow", MesquiteInteger.toString(getProject().getNumberCharMatrices()-1), checker);
			newMatrix.resetCellMetadata();

			resetAllMenuBars();
			decrementMenuResetSuppression();
			return newMatrix;
		}
		else if (checker.compare(this.getClass(), "Creates a new character data matrix filled as requested", "[name of module to fill the matrix]", commandName, "newFilledMatrix")) {
			//ask user how which taxa, how many characters
			//create chars block and add to file
			//return chars
			CharacterData newMatrix=null;
			Taxa taxa = null;
			if (getProject().getNumberTaxas()==0) {
				alert("Data matrix cannot be created until taxa exist in file.");
				return null;
			}
			else 
				taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to create a new character matrix?");
			if (taxa == null)
				return null;

			CharMatrixFiller characterSourceTask;
			if (StringUtil.blank(arguments))
				characterSourceTask = (CharMatrixFiller)hireEmployee(CharMatrixFiller.class, "Fill matrix with characters from:");
			else
				characterSourceTask = (CharMatrixFiller)hireNamedEmployee(CharMatrixFiller.class, arguments);
			if (characterSourceTask != null) {
				getProject().incrementProjectWindowSuppression();
				int num = characterSourceTask.getNumberOfMatrices(taxa);
				int whichMatrix;
				if (num != 1 && MesquiteInteger.isCombinable(num)) // num != 1 added 8 Dec 01
					whichMatrix = characterSourceTask.queryUserChoose(taxa, " to use as new stored matrix");
				else
					whichMatrix = 0;
				MCharactersDistribution matrix = characterSourceTask.getMatrix(taxa, whichMatrix);
				if (matrix==null) {
					fireEmployee(characterSourceTask);
					getProject().decrementProjectWindowSuppression();
					return null;
				}
				CharMatrixManager manager = findCharacterTypeManager(matrix.getDataTypeName());
				if (manager == null) {
					fireEmployee(characterSourceTask);
					getProject().decrementProjectWindowSuppression();
					return null;
				}
				newMatrix = matrix.makeCharacterData(manager, taxa);
				if (matrix.getParentData()!=null){
					CharacterData parent = matrix.getParentData();
					if (parent.characterNamesExist()){
						for (int ic=0; ic<parent.getNumChars() && ic<matrix.getNumChars(); ic++)
							newMatrix.setCharacterName(ic, parent.getCharacterName(ic));
					}
					CharWeightSet weightSet= (CharWeightSet)parent.getCurrentSpecsSet(CharWeightSet.class);  //DRM added 1 May 14
					if (weightSet!=null) {
						newMatrix.setCurrentSpecsSet(weightSet, CharWeightSet.class); 
					}
				}
				String name = MesquiteString.queryShortString(containerOfModule(), "Name Matrix", "Name of New Matrix", getProject().getCharacterMatrices().getUniqueName(matrix.getName()));
				if (name == null){
					fireEmployee(characterSourceTask);
					getProject().decrementProjectWindowSuppression();
					return null;
				}
				newMatrix.setName(name);
				MesquiteFile file=chooseFile( newMatrix);
				newMatrix.addToFile(file, getProject(), this);  

				MesquiteModule mb = findNearestColleagueWithName("Data Window Coordinator");
				if (mb != null)
					mb.doCommand("showDataWindow", MesquiteInteger.toString(getProject().getNumberCharMatrices()-1), checker);
				else
					MesquiteMessage.warnProgrammer("NO DATA WINDOW COORDINATOR");
				fireEmployee(characterSourceTask);
				getProject().decrementProjectWindowSuppression();
				resetAllMenuBars();
				newMatrix.resetCellMetadata();
				return newMatrix;
			}
		}
		else if (checker.compare(this.getClass(), "Shows list of data matrices", null, commandName, "showDatasList")) {
			String concatMessage = "To concatenate matrices, select their rows in the List of Character Matrices window, and choose List>Utilities>Concatenate Selected Matrices.";
			//Check to see if already has lister for this
			for (int i = 0; i<getNumberOfEmployees(); i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ManagerAssistant)
					if (((ManagerAssistant)e).getName().equals("Character Matrices List")) {
						((ManagerAssistant)e).getModuleWindow().setVisible(true);
						if ("concat".equalsIgnoreCase(arguments))
							discreetAlert(concatMessage);
						return e;
					}
			}
			ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("Character Matrices List"));
			if (lister==null){
				alert("Sorry, no module was found to list the character data matrices");
				return null;
			}
			lister.showListWindow(null);
			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
				lister.getModuleWindow().setVisible(true);
			if ("concat".equalsIgnoreCase(arguments))
				discreetAlert(concatMessage);
			return lister;
		}
		else if (checker.compare(this.getClass(), "Deletes matrices from the project", null, commandName, "deleteMatrices")) {
			Listable[] chosen = ListDialog.queryListMultiple(containerOfModule(), "Select Matrices to Delete", "Select one or more character matrices to be deleted", (String)null, "Delete", false, getProject().getCharacterMatrices(), (boolean[])null);
			if (chosen != null){
				for (int i = chosen.length-1; i>=0; i--) {  
					((FileElement)chosen[i]).doom();
				}
				getProject().incrementProjectWindowSuppression();
				for (int i = chosen.length-1; i>=0; i--) {  
					logln("Deleting " + chosen[i].getName());
					deleteElement((FileElement)chosen[i]);
				}
				getProject().decrementProjectWindowSuppression();
			}
		}
		else if (checker.compare(this.getClass(), "Deletes all matrices from the project", null, commandName, "deleteAllMatrices")) {
			for (int i = getProject().getNumberCharMatrices(); i>=0; i--) {  
				CharacterData data = getProject().getCharacterMatrixDoomedOrNot(i);
				data.doom();
			}
			for (int i = getProject().getNumberCharMatrices(); i>=0; i--) {  
				CharacterData data = getProject().getCharacterMatrixDoomedOrNot(i);
				deleteElement(data);
			}
		}
		else if (checker.compare(this.getClass(), "Concatenates all matrices of a given type into the first matrix of that type", "[file id to move all matrices to]", commandName, "concatenateAllMatrices")) {
			int fileID = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			MesquiteFile file = null;
			if (MesquiteInteger.isCombinable(fileID))
				file = getProject().getFileByID(fileID);

			for (int i = getProject().getNumberCharMatrices()-1; i>=1; i--) {  
				if (getProject().getNumberCharMatrices() == 1)
					return null;
				CharacterData data = getProject().getCharacterMatrix(i);
				//now look to find previous matrix of same type and taxa, concatenate
				for (int j = i-1; j>=0; j--) {  
					CharacterData previous = getProject().getCharacterMatrix(j);

					if (previous.getTaxa() == data.getTaxa() && data.getClass() == previous.getClass()) {
						//concatenate matrices
						int origNumChars = previous.getNumChars();
						previous.addParts(previous.getNumChars()+1, data.getNumChars());
						previous.addInLinked(previous.getNumChars()+1, data.getNumChars(), true);
						CharacterState cs = null;
						for (int it = 0; it<previous.getNumTaxa() && it<data.getNumTaxa(); it++){
							for (int ic = 0; ic<data.getNumChars(); ic++){
								cs= data.getCharacterState(cs, ic, it);
								previous.setState(ic+origNumChars, it, cs);
								if (data.characterHasName(ic))
									previous.setCharacterName(ic+origNumChars, data.getCharacterName(ic));
								//note that does not set other stuff (state names, weights, footnotes)
							}
						}
						previous.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {origNumChars, data.getNumChars()}));
						//delete second
						deleteElement(data);
						j = -1; //do this to pop out of the loop
					}
				}
			}
			if (file != null){
				ElementManager em = findElementManager(Taxa.class);

				int numM = getProject().getNumberCharMatrices();
				for (int i = 0; i<numM; i++) {
					CharacterData data = getProject().getCharacterMatrix(i);
					CharacterData clone = data.cloneData();
					Taxa taxaClone = data.getTaxa().cloneTaxa();
					taxaClone.addToFile(file, getProject(), em);
					clone.addToFile(file, getProject(), this);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Shows the character list window for a specified data matrix; if a list window already exists, show it", "[optional: number of matrix to show]", commandName, "showCharacters")) {
			if (StringUtil.blank(arguments)) {
				for (int i = 0; i< getProject().getNumberCharMatrices(); i++) {  //restriction to checker.getFile() deleted 13 Dec 01
					CharacterData data = getProject().getCharacterMatrix(i);
					if (data!=null && data.isUserVisible())
						showCharactersList(data);//restriction to checker.getFile() deleted 13 Dec 01
				}
				return null;
			}
			else {
				// in general, will only show user visible matrices.  However, if arguments starts with #, then assume a direct, non-numbered request that will be obeyed even if not user visible
				//Check to see if already has lister for this
				CharacterData data =  getProject().getCharacterMatrixByReference(checker.getFile(), parser.getFirstToken(arguments), !arguments.startsWith("#"));  
				if (data != null){
					return showCharactersList(data);
				}
				int t = MesquiteInteger.fromFirstToken(arguments, pos);
				if (MesquiteInteger.isCombinable(t) && t<getProject().getNumberCharMatrices()) {//restriction to checker.getFile() deleted 13 Dec 01
					data = getProject().getCharacterMatrix(t, true);//restriction to checker.getFile() deleted 13 Dec 01
					return showCharactersList(data);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Closes all list of characters windows", null, commandName, "closeAllListWindows")) {
			for (int i = getNumberOfEmployees()-1; i>=0; i--) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ManagerAssistant &&  ((ManagerAssistant)e).getName().equals("Character List")){
					((ManagerAssistant)e).windowGoAway(((ManagerAssistant)e).getModuleWindow());
				}
			}
			if (calw != null) calw.setEnabled(false);
			resetAllMenuBars();
			return null;
		}
		else if (checker.compare(this.getClass(), "Shows the data editor window for a specified data matrix; if a data window already exists, show it", "[number of matrix to show]", commandName, "showDataWindow")) {
			//Check to see if already has lister for this
			MesquiteModule mb = findNearestColleagueWithName("Data Window Coordinator");
			if (mb != null)
				return mb.doCommand("showDataWindow", arguments, checker);
			/*	int t = MesquiteInteger.fromFirstToken(arguments, pos);
			if (MesquiteInteger.isCombinable(t) && t<getProject().getNumberCharMatrices()) {
				MesquiteModule mb = findNearestColleagueWithName("Data Window Coordinator");
				if (mb != null)
					return mb.doCommand("showDataWindow", MesquiteInteger.toString(t), checker);
			}*/
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** returns number of data sets of given reference string (ref. number, name, number) from the project */
	public int  getNumberCharMatricesWithUniqueID(String uniqueID) {  //ManageCharacters Only
		ListableVector datasVector = getProject().getCharacterMatrices();
		int count = 0;
		for (int i=0; i<datasVector.size(); i++) {
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datasVector.elementAt(i);
			if (!data.isDoomed() && data.getUniqueID() != null && uniqueID != null && uniqueID.equals(data.getUniqueID())) {
				count++;
			}
		}

		return count;  
	}

	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new CharactersBlockTest();}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new CHARNexusCommandTest();
	}
	/*.................................................................................................................*/
	public CharMatrixManager getMatrixManager(Class dataClass) {
		CharMatrixManager readerTask=null;
		for (int i = 0; i<getNumberOfEmployees() && readerTask==null; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof CharMatrixManager)
				if (((CharMatrixManager)e).readsWritesDataType(dataClass)) {
					readerTask=(CharMatrixManager)e;
				}
		}
		return readerTask;
	}
	/*.................................................................................................................*/
	public CharMatrixManager findReader(String dataType) {
		CharMatrixManager readerTask=null;
		for (int i = 0; i<getNumberOfEmployees() && readerTask==null; i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof CharMatrixManager)
				if (((CharMatrixManager)e).readsWritesDataType(dataType)) {
					readerTask=(CharMatrixManager)e;
				}
		}
		return readerTask;
	}

	/*.................................................................................................................*/
	public CharacterData processFormat(MesquiteFile file, Taxa taxa, String formatCommand, int numChars, String title, String fileReadingArguments) {
		CharacterData data=null;
		MesquiteInteger startCharF = new MesquiteInteger(0);

		String tok = ParseUtil.getToken(formatCommand, startCharF);
		String dataType = "Standard";
		//Finding datatype (done first in case datatype subcommand is illegally written other than first, as by Clustal
		while (tok != null && !tok.equals(";")) {
			if (tok.equalsIgnoreCase("DATATYPE")) {
				tok = ParseUtil.getToken(formatCommand, startCharF); //eat up "="
				dataType = ParseUtil.getToken(formatCommand, startCharF); //find datatype
			}
			tok = ParseUtil.getToken(formatCommand, startCharF);
		}
		//processing format
		startCharF.setValue(0);
		CharMatrixManager readerTask= findReader(dataType);
		if (readerTask !=null)
			data = readerTask.processFormat(file, taxa, dataType, formatCommand, startCharF, numChars, title, fileReadingArguments);
		else 
			return null;

		/*int previousPos =0;
		startCharF.setValue(0);
		while (tok != null && !tok.equals(";")) {
			if (tok.equalsIgnoreCase("DATATYPE")) {
				tok = ParseUtil.getToken(formatCommand, startCharF); //eat up "="
				tok = ParseUtil.getToken(formatCommand, startCharF); //find datatype

				CharMatrixManager readerTask= findReader(tok);
				if (readerTask !=null)
					data = readerTask.processFormat(file, taxa, tok, formatCommand, startCharF, numChars, title, fileReadingArguments);
				else 
					return null;
			}
			else if (!(tok.equalsIgnoreCase("FORMAT"))) {
				if (data==null) {

					CharMatrixManager readerTask= findReader("Standard");
					if (readerTask !=null) {
						startCharF.setValue(previousPos);
						data = readerTask.processFormat(file, taxa, tok, formatCommand, startCharF, numChars, title, fileReadingArguments);
						return data;
					}
					else 
						return null;
				}

			}
			previousPos = startCharF.getValue();
			tok = ParseUtil.getToken(formatCommand, startCharF);
		}
		 */
		return data;
	}

	/*ABBREVIATIONS NEW to 1. 06:
	SU Supplemental
	C Character
	T Taxon
	N Name
	I Integer
	R Real
	S String
	 */					
	static final String suppTokenAbbrev = "\tSU ";
	static final String suppTMTokenAbbrev = "\tSUTM ";
	static final String characterTokenAbbrev = " C = ";
	static final String taxonTokenAbbrev = " T = ";
	static final String nameTokenAbbrev = " N = ";
	static final String intTokenAbbrev = " I = ";
	static final String realTokenAbbrev = " R = ";
	static final String booleanTokenAbbrev = " B = ";
	static final String stringTokenAbbrev = " S = ";
	static final String stringArrayTokenAbbrev = " SE = ";
	static final String characterToken = " CHARACTER = ";
	static final String taxonToken = " TAXON = ";
	static final String textToken = " TEXT = ";
	NameReference commentsRef = NameReference.getNameReference("comments");
	/*.................................................................................................................*/
	public boolean writeNexusCommands(MesquiteFile file, String blockName, MesquiteString pending){ 
		boolean found = false;
		if (blockName.equalsIgnoreCase("NOTES")) {
			StringBuffer s = new StringBuffer(100);
			StringBuffer tokSB = new StringBuffer(100);
			MesquiteProject project = file.getProject();
			for (int i=0; i<project.getNumberCharMatrices(); i++){
				CharacterData data = getProject().getCharacterMatrix(i);
				if (data.getFile()==file && data.getWritable()){
					String eL =";" + StringUtil.lineEnding();
					if ((project.getNumberCharMatrices()>1 && MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data)) || (project.getNumberTaxas()>1&& MesquiteFile.okToWriteTitleOfNEXUSBlock(file, data.getTaxa()))) //note shift in 1. 06 to "current matrix and taxa" to avoid having to repeat in each note
						s.append("\tCHARACTERS = " +  StringUtil.tokenize(data.getName(), null, tokSB) +" TAXA = " +  StringUtil.tokenize(data.getTaxa().getName(), null, tokSB) + eL);
					String textDataSpec = "\tTEXT  ";
					Associable as = data.getTaxaInfo(false);

					if (as != null){
						for (int it = 0; it<data.getNumTaxa(); it++){
							//look through all attached longs
							int numLongs = as.getNumberAssociatedLongs();

							for (int v = 0; v<numLongs; v++){  
								LongArray array = as.getAssociatedLongs(v);
								long c = array.getValue(it);
								if (MesquiteLong.isCombinable(c)){
									s.append(suppTMTokenAbbrev);
									s.append(taxonTokenAbbrev);
									s.append(Integer.toString(CharacterStates.toExternal(it)));
									s.append(nameTokenAbbrev);
									s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
									s.append(intTokenAbbrev);
									s.append(Long.toString(c));
									s.append(eL);
									found = true;
								}
							}
							//look through all attached doubles
							int numDoubs = as.getNumberAssociatedDoubles();

							for (int v = 0; v<numDoubs; v++){  
								DoubleArray array = as.getAssociatedDoubles(v);
								double c = array.getValue(it);

								if (MesquiteDouble.isCombinable(c)){
									s.append(suppTMTokenAbbrev);
									s.append(taxonTokenAbbrev);
									s.append(Integer.toString(CharacterStates.toExternal(it)));
									s.append(nameTokenAbbrev);
									s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
									s.append(realTokenAbbrev);
									s.append(Double.toString(c));
									s.append(eL);
									found = true;
								}
							}

							//look through all attached booleans
							int numBools = as.getNumberAssociatedBits();

							for (int v = 0; v<numBools; v++){  
								Bits array = as.getAssociatedBits(v);
								boolean c = array.isBitOn(it);

								if (c){
									s.append(suppTMTokenAbbrev);
									s.append(taxonTokenAbbrev);
									s.append(Integer.toString(CharacterStates.toExternal(it)));
									s.append(nameTokenAbbrev);
									s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
									s.append(booleanTokenAbbrev);
									s.append("TRUE");
									s.append(eL);
									found = true;
								}
							}
							//look through all attached objects
							int numObs = as.getNumberAssociatedObjects();

							for (int v = 0; v<numObs; v++){  
								ObjectArray array = as.getAssociatedObjects(v);
								Object c = array.getValue(it);

								if (c != null && c instanceof String){
									s.append(suppTMTokenAbbrev);
									s.append(taxonTokenAbbrev);
									s.append(Integer.toString(CharacterStates.toExternal(it)));
									s.append(nameTokenAbbrev);
									s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
									s.append(stringTokenAbbrev);
									s.append(StringUtil.tokenize((String)c, null, tokSB));
									s.append(eL);
									found = true;
								}
								else if (c != null && c instanceof String[] && ((String[])c).length>0){
									s.append(suppTMTokenAbbrev);
									s.append(taxonTokenAbbrev);
									s.append(Integer.toString(CharacterStates.toExternal(it)));
									s.append(nameTokenAbbrev);
									s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
									String[] strings = (String[])c;
									for (int k = 0; k<strings.length; k++){
										s.append(stringArrayTokenAbbrev);
										s.append(StringUtil.tokenize(strings[k], null, tokSB));
										s.append(' ');
									}
									s.append(eL);
									found = true;
								}

							}



						}
					}
					for (int ic = 0; ic<data.getNumChars(); ic++){
						boolean foundForChar = false;
						String obj = data.getAnnotation(ic);
						if (!StringUtil.blank(obj)){ //OLD footnotes; maintain non-abbreviated
							s.append(textDataSpec);
							s.append(" CHARACTER = ");
							s.append(Integer.toString(CharacterStates.toExternal(ic)));
							s.append(" TEXT = ");
							s.append(StringUtil.tokenize(obj, null, tokSB));
							s.append(eL);
							foundForChar = true;
						}

						//look through all attached longs
						int numLongs = data.getNumberAssociatedLongs();

						for (int v = 0; v<numLongs; v++){  
							LongArray array = data.getAssociatedLongs(v);
							long c = array.getValue(ic);
							if (MesquiteLong.isCombinable(c)){
								s.append(suppTokenAbbrev);
								s.append(characterTokenAbbrev);
								s.append(Integer.toString(CharacterStates.toExternal(ic)));
								s.append(nameTokenAbbrev);
								s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
								s.append(intTokenAbbrev);
								s.append(Long.toString(c));
								s.append(eL);
								foundForChar = true;
							}
						}
						//look through all attached doubles
						int numDoubs = data.getNumberAssociatedDoubles();

						for (int v = 0; v<numDoubs; v++){  
							DoubleArray array = data.getAssociatedDoubles(v);
							double c = array.getValue(ic);

							if (MesquiteDouble.isCombinable(c)){
								s.append(suppTokenAbbrev);
								s.append(characterTokenAbbrev);
								s.append(Integer.toString(CharacterStates.toExternal(ic)));
								s.append(nameTokenAbbrev);
								s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
								s.append(realTokenAbbrev);
								s.append(Double.toString(c));
								s.append(eL);
								foundForChar = true;
							}
						}

						//look through all attached booleans
						int numBools = data.getNumberAssociatedBits();

						for (int v = 0; v<numBools; v++){  
							Bits array = data.getAssociatedBits(v);
							boolean c = array.isBitOn(ic);

							if (c){
								s.append(suppTokenAbbrev);
								s.append(characterTokenAbbrev);
								s.append(Integer.toString(CharacterStates.toExternal(ic)));
								s.append(nameTokenAbbrev);
								s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
								s.append(booleanTokenAbbrev);
								s.append("TRUE");
								s.append(eL);
								found = true;
							}
						}
						//look through all attached objects
						int numObs = data.getNumberAssociatedObjects();

						for (int v = 0; v<numObs; v++){  
							ObjectArray array = data.getAssociatedObjects(v);
							if (!commentsRef.equals(array.getNameReference())){
								Object c = array.getValue(ic);
								if (c != null && c instanceof String){
									s.append(suppTokenAbbrev);
									s.append(characterTokenAbbrev);
									s.append(Integer.toString(CharacterStates.toExternal(ic)));
									s.append(nameTokenAbbrev);
									s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
									s.append(stringTokenAbbrev);
									s.append(StringUtil.tokenize((String)c, null, tokSB));
									s.append(eL);
									foundForChar = true;
								}
								else if (c != null && c instanceof String[] && ((String[])c).length>0){
									s.append(suppTMTokenAbbrev);
									s.append(characterTokenAbbrev);
									s.append(Integer.toString(CharacterStates.toExternal(ic)));
									s.append(nameTokenAbbrev);
									s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
									String[] strings = (String[])c;
									for (int k = 0; k<strings.length; k++){
										s.append(stringArrayTokenAbbrev);
										s.append(StringUtil.tokenize(strings[k], null, tokSB));
										s.append(' ');
									}
									s.append(eL);
									foundForChar = true;
								}
							}
						}



						for (int it = 0; it< data.getNumTaxa(); it++){ //OLD footnotes; maintain non-abbreviated
							String e = data.getAnnotation(ic, it);
							if (!StringUtil.blank(e)){
								s.append(textDataSpec);
								s.append(taxonToken);
								s.append(Integer.toString(Taxon.toExternal(it)));
								s.append(characterToken);
								s.append(Integer.toString(CharacterStates.toExternal(ic)));
								s.append(textToken);
								s.append(StringUtil.tokenize(e, null, tokSB));
								s.append(eL);
								foundForChar = true;
							}
							Vector vector = data.getCellObjectsVector(); 
							if (vector !=null){
								for (int v = 0; v<vector.size(); v++){
									Object2DArray array = (Object2DArray)vector.elementAt(v);
									Object c = array.getValue(ic, it);

									if (c !=null){
										if (c instanceof MesquiteInteger){
											s.append(suppTokenAbbrev);
											s.append(taxonTokenAbbrev);
											s.append(Integer.toString(Taxon.toExternal(it)));
											s.append(characterTokenAbbrev);
											s.append(Integer.toString(CharacterStates.toExternal(ic)));
											s.append(nameTokenAbbrev);
											s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
											s.append(intTokenAbbrev);
											s.append(Integer.toString(((MesquiteInteger)c).getValue()));
											s.append(eL);
											foundForChar = true;
										}
										else if ( c instanceof MesquiteDouble){
											s.append(suppTokenAbbrev);
											s.append(taxonTokenAbbrev);
											s.append(Integer.toString(Taxon.toExternal(it)));
											s.append(characterTokenAbbrev);
											s.append(Integer.toString(CharacterStates.toExternal(ic)));
											s.append(nameTokenAbbrev);
											s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
											s.append(realTokenAbbrev);
											s.append(Double.toString(((MesquiteDouble)c).getValue()));
											s.append(eL);
											foundForChar = true;
										}
										else if (c instanceof String){
											s.append(suppTokenAbbrev);
											s.append(taxonTokenAbbrev);
											s.append(Integer.toString(Taxon.toExternal(it)));
											s.append(characterTokenAbbrev);
											s.append(Integer.toString(CharacterStates.toExternal(ic)));
											s.append(nameTokenAbbrev);
											s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
											s.append(stringTokenAbbrev);
											s.append(StringUtil.tokenize((String)c, null, tokSB));
											s.append(eL);
											foundForChar = true;
										}
										else if (c != null && c instanceof String[] && ((String[])c).length>0){
											s.append(suppTMTokenAbbrev);
											s.append(taxonTokenAbbrev); 
											s.append(Integer.toString(Taxon.toExternal(it)));
											s.append(characterTokenAbbrev);
											s.append(Integer.toString(CharacterStates.toExternal(ic)));
											s.append(nameTokenAbbrev);
											s.append( ParseUtil.tokenize(array.getNameReference().getValue(), null, tokSB));
											String[] strings = (String[])c;
											for (int k = 0; k<strings.length; k++){
												s.append(stringArrayTokenAbbrev);
												s.append(StringUtil.tokenize(strings[k], null, tokSB));
												s.append(' ');
											}
											s.append(eL);
											foundForChar = true;
										}
									}
								}
							}
						}
						if (foundForChar && s.length()>0){ //dump to file
							if (pending != null && !pending.isBlank()){
								file.writeLine(pending.toString());
								pending.setValue("");
							}
							file.writeLine(s.toString());
							s.setLength(0);
						}
						found = found || foundForChar;

					}
					Vector a = data.getAttachments();
					if (a != null)
						for (int ia = 0; ia<a.size(); ia++){
							Object o = a.elementAt(ia);
							if (o instanceof MesquiteString){
								MesquiteString ms = (MesquiteString)o;
								if (ms.getName() != null){
									s.append("\tSUCM ");
									s.append(nameTokenAbbrev);
									s.append( ParseUtil.tokenize(ms.getName(), null, tokSB));
									s.append(stringTokenAbbrev);
									s.append(StringUtil.tokenize(ms.getValue(), null, tokSB));
									s.append(eL);
									found = true;
								}
							}
						}
					if (found == true && s.length()>0){ //dump to file
						if (pending != null && !pending.isBlank()){
							file.writeLine(pending.toString());
							pending.setValue("");
						}
						file.writeLine(s.toString());
						s.setLength(0);
					}

				}
			}
		}
		return found;
	}
	/*...................................................................................................................*
	Taxa defaultTaxa = null;
	MesquiteFile lastFileTaxa = null;
	Taxa getDefaultTaxa(MesquiteFile file){
		if (lastFileTaxa == file && defaultTaxa != null){
			return defaultTaxa;
		}
		if (lastFileTaxa != file)
			lastFileTaxa = file;
		defaultTaxa = getProject().getTaxa(0);
		return defaultTaxa;
	}
	/*...................................................................................................................*
	CharacterData defaultData = null;
	MesquiteFile lastFileData = null;
	CharacterData getDefaultData(MesquiteFile file){
		if (lastFileData == file && defaultData != null){
			return defaultData;
		}
		if (lastFileData != file)
			lastFileData = file;
		defaultData = getProject().getCharacterMatrix(defaultTaxa, 0);
		return defaultData;
	}
	
	
	 */
	/** This method cleans up the confounded GenBank annotations of version 3.1 and earlier.  In those versions, the GenBank 
	 * associated string included GenBank accession numbers and/or other details of sequence length, etc., appended in the 
	 * TaxonListHadData module. After version 3.1, these were separated into different objects, in order to maintain the veracity
	 * of the GenBank data.
	 /*...................................................................................................................*/
	void cleanUpGenBankAssociatedObject (Associable as, int whichTaxon, String genBankNote){
		String newNote="";
		while (!StringUtil.blank(genBankNote) && genBankNote.indexOf("(")>=0){
			int start = genBankNote.indexOf("(");
			int end = genBankNote.indexOf(")");
			String firstBit = "";
			if (start>0)
				firstBit = genBankNote.substring(0, start);
			newNote=genBankNote.substring(start,end+1);
			genBankNote = firstBit + genBankNote.substring(end+1, genBankNote.length());
		}
		 as.setAssociatedObject(MolecularData.genBankNumberRef, whichTaxon, genBankNote);
		 as.setAssociatedObject(CharacterData.taxonMatrixNotesRef, whichTaxon, newNote);
	}

	
	NameReference origIndexRef = NameReference.getNameReference("OrigIndex");
	 /*...................................................................................................................*/
	 public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		 if (blockName.equalsIgnoreCase("NOTES")) {
			 boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
			 MesquiteProject project = file.getProject();
			 String commandName = parser.getFirstToken(command);
			 nBlock.setDefaultTaxa(project.getTaxa(project.getNumberTaxas()-1));
			 int code = 0;
			 // if TAXA, CHARACTERS, SUTM then accept even if fuse
			 if (fuse && !((commandName.equalsIgnoreCase("SUTM")) || commandName.equalsIgnoreCase("CHARACTERS")))
				 return true;
			 if  (commandName.equalsIgnoreCase("TEXT")) 
				 code = 1;
			 else if  (commandName.equalsIgnoreCase("I") || commandName.equalsIgnoreCase("INTEGER")) 
				 code = 2;
			 else if  (commandName.equalsIgnoreCase("SU") || commandName.equalsIgnoreCase("SUPPLEMENTAL")) 
				 code = 3;
			 else if  (commandName.equalsIgnoreCase("SUTM")) 
				 code = 4;
			 else if  (commandName.equalsIgnoreCase("SUCM")) {
				 code = 5;
			 }
			 else if  (commandName.equalsIgnoreCase("B")) 
				 code = 6;
			 else if  (commandName.equalsIgnoreCase("CHARACTERS")) {
				 String ctoken  = parser.getNextToken(); //=
				 ctoken  = parser.getNextToken();
				 String ttoken  = parser.getNextToken(); //TAXA
				 if ("Taxa".equalsIgnoreCase(ttoken)){
					 parser.getNextToken(); //=
					 ttoken  = parser.getNextToken(); //TAXA block (optional)
					 if (!StringUtil.blank(ttoken)){
						 Taxa t = getProject().findTaxa(file, ttoken);
/*
						 if (t==null){
							 int wt = MesquiteInteger.fromString(ttoken);
							 if (MesquiteInteger.isCombinable(wt))
								 t = getProject().getTaxa(wt-1);
						 }
						 if (t == null && getProject().getNumberTaxas(file)==1){
							 t = getProject().getTaxa(file, 0);
						 }
						 if (t == null && getProject().getNumberTaxas()==1){
							 t = getProject().getTaxa(0);
						 }
						 */
						 if (t!=null) {
							 nBlock.setDefaultTaxa(t);
						 }
						 else
							 return false;
					 }
				 }
				 if (fuse && getProject().getNumberCharacterMatricesWithName(null, null, null, ctoken) != 1){
					 CharacterData d = getProject().chooseData(containerOfModule(), null, null, "A NOTES command refers to matrix \"" + ctoken + "\", but this is ambiguous.  Please choose a matrix to which it applies. (command: " + command  + ")");
					 if (d!=null) {
						 nBlock.setDefaultCharacters(d);
						 return true;
					 }
				 }
				 CharacterData d = getProject().findCharacterMatrix(file, nBlock.getDefaultTaxa(), ctoken);

				 if (d!=null) {
					 nBlock.setDefaultCharacters(d);
					 return true;
				 }
				 else
					 return false;
			 }

			 if (code >0 ) {
				 int integer = MesquiteInteger.unassigned;
				 double doub = MesquiteDouble.unassigned;
				 boolean bool = false;
				 boolean boolSet = false;
				 String string = null;
				 Vector strings = new Vector(); //to store string array
				 String name = null;
				 stringPos.setValue(parser.getPosition());
				 String[][] subcommands  = ParseUtil.getSubcommands(command, stringPos);
				 if (subcommands == null || subcommands.length == 0 || subcommands[0] == null || subcommands[0].length == 0)
					 return false;
				 int whichTaxon = MesquiteInteger.unassigned;
				 int whichCharacter = MesquiteInteger.unassigned;
				 String text = null;
				 Taxa taxa = nBlock.getDefaultTaxa();
				 CharacterData data = nBlock.getDefaultCharacters();
				// IntegerArray translationTable = (IntegerArray)taxa.getAttachment("originalIndicesDupRead");
				 IntegerArray translationTable = null;
				 if (taxa != null && file != null)
					 translationTable = (IntegerArray)taxa.getAttachment("OrigIndex" + file.getFileName());
				 if (fuse && translationTable == null)
					 return false;
				 for (int i=0; i<subcommands[0].length; i++){
					 String subC = subcommands[0][i];
					 if ("T".equalsIgnoreCase(subC) || "TAXON".equalsIgnoreCase(subC)) {
						 String token = subcommands[1][i];
						 whichTaxon = MesquiteInteger.fromString(token);


						 if (!MesquiteInteger.isCombinable(whichTaxon))
							 return false;
						 whichTaxon = Taxon.toInternal(whichTaxon);

						 if (fuse){
							 if (translationTable != null)
								 whichTaxon = translationTable.getValue(whichTaxon);
							 
							 /*
							 MesquiteInteger oldNumTaxa = (MesquiteInteger)taxa.getAttachment("OLDNUMTAXA" + file.getFileName());
							 IntegerArray translationTable = (IntegerArray)taxa.getAttachment("OrigIndex" + file.getFileName());
							 if (oldNumTaxa != null && translationTable != null){
								 whichTaxon += oldNumTaxa.getValue();
								 whichTaxon = translationTable.getValue(whichTaxon);
							 }
							 */
						 }

					 }
					 else if ( "C".equalsIgnoreCase(subC) || "CHARACTER".equalsIgnoreCase(subC)) {
						 String token = subcommands[1][i];
						 whichCharacter = MesquiteInteger.fromString(token);
						 if (!MesquiteInteger.isCombinable(whichCharacter))
							 return false;
						 whichCharacter = CharacterStates.toInternal(whichCharacter);
					 }
					 else if ("N".equalsIgnoreCase(subC) || "NAME".equalsIgnoreCase(subC)) {
						 name = subcommands[1][i];
					 }
					 else if ("B".equalsIgnoreCase(subC) || "BOOLEAN".equalsIgnoreCase(subC)) {
						 if ("true".equalsIgnoreCase(subcommands[1][i]))
							 bool = true;
						 boolSet = true;
					 }
					 else if ("I".equalsIgnoreCase(subC) || "INTEGER".equalsIgnoreCase(subC)) {
						 integer = MesquiteInteger.fromString(subcommands[1][i]);
					 }
					 else if ("R".equalsIgnoreCase(subC) || "REAL".equalsIgnoreCase(subC)) {
						 doub = MesquiteDouble.fromString(subcommands[1][i]);
					 }
					 else if ("S".equalsIgnoreCase(subC) || "STRING".equalsIgnoreCase(subC)) {
						 string = subcommands[1][i];
					 }
					 else if ("SE".equalsIgnoreCase(subC)) {
						 strings.addElement(subcommands[1][i]);
					 }
					 else if ("TEXT".equalsIgnoreCase(subC)) {
						 text = subcommands[1][i];
					 }
					 else if ("TAXA".equalsIgnoreCase(subC)) {
						 String token = subcommands[1][i];
						 Taxa t = getProject().findTaxa(file, token);
						 /*getTaxaLastFirst(token);
						 if (t==null){
							 int wt = MesquiteInteger.fromString(token);
							 if (MesquiteInteger.isCombinable(wt))
								 t = getProject().getTaxa(wt-1);
						 }
						 if (t == null && getProject().getNumberTaxas(file)==1)
							 t = getProject().getTaxa(file, 0);
							if (t == null && getProject().getNumberTaxas()==1)
								t = getProject().getTaxa(0);
								*/
						 if (t!=null) {
							 taxa = t;
						 }
						 else
							 return false;
					 }
					 else if ("CHARACTERS".equalsIgnoreCase(subC)) {
						 String token = subcommands[1][i];
						 CharacterData t = getProject().findCharacterMatrix(file, taxa, token);
						 if (t!=null) {
							 data = t;
						 }
						 else
							 return false;
					 }
				 }

				 if (!MesquiteInteger.isCombinable(whichCharacter) && !(code == 4 && MesquiteInteger.isCombinable(whichTaxon)) && code !=5){
					 return false;
				 }

				 if (code ==1){ //text
					 if (taxa !=null && text !=null && data !=null) {

						 /*&&& the following is a check in place because of a bug in 1.02 and previous in which copies of NOTES blocks would be written in all linked files; 
						this allowed overwriting by old copies of the NOTES block */
						 if (data.getFile() != file && file != getProject().getHomeFile()){
							 if (!MesquiteInteger.isCombinable(whichTaxon)) {
								 String s = data.getAnnotation(whichCharacter) ;
								 if (s != null && !s.equals(text)) {
									 file.notesBugWarn = true;
									 file.notesBugVector.addElement("Character " + (whichCharacter+1));
								 }

							 }
							 else {
								 String s = data.getAnnotation(whichCharacter, whichTaxon) ;
								 if (s != null && !s.equals(text)) {
									 file.notesBugWarn = true;
									 file.notesBugVector.addElement("Character " + (whichCharacter+1) + " in taxon " + (whichTaxon+1));
								 }
							 }
						 }
						 /*&&&*/

						 if (!MesquiteInteger.isCombinable(whichTaxon))
							 data.setAnnotation(whichCharacter, text);
						 else
							 data.setAnnotation(whichCharacter, whichTaxon, text);
						 return true;
					 }
				 }
				 else if (code ==2){ //integer
					 if (taxa !=null  && MesquiteInteger.isCombinable(integer) && data !=null && name !=null) {
						 if (MesquiteInteger.isCombinable(whichTaxon))
							 data.setCellObject(NameReference.getNameReference(name), whichCharacter, whichTaxon, new MesquiteInteger(integer));
						 else
							 data.setAssociatedLong(NameReference.getNameReference(name), whichCharacter, integer);
						 return true;
					 }
				 }
				 else if (code ==3 || code == 4){ //supplemental
					 if (taxa !=null && data !=null && name !=null) {
						 if (boolSet){
							 if (MesquiteInteger.isCombinable(whichTaxon)) {
								 if (code == 4) {
									 Associable as = data.getTaxaInfo(true);
									 as.setAssociatedBit(NameReference.getNameReference(name), whichTaxon, bool);
								 }
								 else
									 data.setCellObject(NameReference.getNameReference(name), whichCharacter, whichTaxon, new MesquiteBoolean(bool));
							 }
							 else
								 data.setAssociatedBit(NameReference.getNameReference(name), whichCharacter, bool);
							 return true;
						 }
						 else if (MesquiteInteger.isCombinable(integer)){
							 if (MesquiteInteger.isCombinable(whichTaxon)) {
								 if (code == 4) {
									 Associable as = data.getTaxaInfo(true);
									 as.setAssociatedLong(NameReference.getNameReference(name), whichTaxon, integer);
								 }
								 else
									 data.setCellObject(NameReference.getNameReference(name), whichCharacter, whichTaxon, new MesquiteInteger(integer));
							 }
							 else
								 data.setAssociatedLong(NameReference.getNameReference(name), whichCharacter, integer);
							 return true;
						 }
						 else if (MesquiteDouble.isCombinable(doub)){
							 if (MesquiteInteger.isCombinable(whichTaxon)){
								 if (code == 4) {
									 Associable as = data.getTaxaInfo(true);
									 as.setAssociatedDouble(NameReference.getNameReference(name), whichTaxon, doub);
								 }
								 else
									 data.setCellObject(NameReference.getNameReference(name), whichCharacter, whichTaxon, new MesquiteDouble(doub));
							 }
							 else
								 data.setAssociatedDouble(NameReference.getNameReference(name), whichCharacter, doub);
							 return true;
						 }
						 else if (string != null){
							 /*&&& the following is a check in place because of a bug in 1.02 and previous in which copies of NOTES blocks would be written in all linked files; 
							this allowed overwriting by old copies of the NOTES block */
							 if (data.getFile() != file && file != getProject().getHomeFile()){
								 if (MesquiteInteger.isCombinable(whichTaxon)) {
									 String s = (String)data.getCellObject(NameReference.getNameReference(name), whichCharacter, whichTaxon);
									 if (s != null && !s.equals(string)) {
										 file.notesBugWarn = true;
										 file.notesBugVector.addElement("Character " + (whichCharacter+1) + "(*)");
									 }
								 }
								 else {
									 String s = (String)data.getAssociatedObject(NameReference.getNameReference(name), whichCharacter);
									 if (s != null && !s.equals(string)) {
										 file.notesBugWarn = true;
										 file.notesBugVector.addElement("Character " + (whichCharacter+1) + " in taxon " + (whichTaxon+1) + "(*)");
									 }
								 }
							 }
							 /*&&&*/

							 if (MesquiteInteger.isCombinable(whichTaxon)) {
								 if (code == 4) {
									 Associable as = data.getTaxaInfo(true);

									 Object previous = as.getAssociatedObject(NameReference.getNameReference(name), whichTaxon);
									 if (!StringUtil.blank((String)previous))
										 string = (String)previous + "; " + string;
									 if (name.equals(MolecularData.genBankNumberName)) {  // let's clean it up because of Mesquite 3.1 and before's confounding of GenBank numbers and other annotations
											cleanUpGenBankAssociatedObject (as, whichTaxon, string);  
									 }
									 else
										 as.setAssociatedObject(NameReference.getNameReference(name), whichTaxon, string);
								 }
								 else
									 data.setCellObject(NameReference.getNameReference(name), whichCharacter, whichTaxon, string);
							 }
							 else
								 data.setAssociatedObject(NameReference.getNameReference(name), whichCharacter, string);
							 return true;
						 }
						 else if (strings.size()>0){
							 String[] sts = new String[strings.size()];
							 for (int k = 0; k<strings.size(); k++)
								 sts[k] = (String)strings.elementAt(k);
							 if (MesquiteInteger.isCombinable(whichTaxon)) {
								 if (code == 4) {
									 Associable as = data.getTaxaInfo(true);
									 as.setAssociatedObject(NameReference.getNameReference(name), whichTaxon, sts);
								 }
								 else
									 data.setCellObject(NameReference.getNameReference(name), whichCharacter, whichTaxon, sts);
							 }
							 else
								 data.setAssociatedObject(NameReference.getNameReference(name), whichCharacter, sts);
							 return true;
						 }
					 }
				 }
				 else if (code == 5){

					 if (string != null && name != null){

						 MesquiteString ms = new MesquiteString(name, string);
						 data.attachIfUniqueName(ms);
						 return true;
					 }
				 }

			 }
		 }
		 return false;
	 }
	 /*.................................................................................................................*/
	 public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		 CharacterData data=null;
		 Parser commandParser = new Parser();
		 commandParser.setString(block.toString());
		 MesquiteInteger startCharC = new MesquiteInteger(0);
		 String title=null;
		 //String commandString;
		 Taxa taxa= null;
		 if (getProject().getNumberTaxas(file)==1)
			 taxa = getProject().getTaxa(file, 0); //as default
		 else if (getProject().getNumberTaxas()==1)
			 taxa = getProject().getTaxa(0); //as default
		 NexusBlock b=null;
		 int numChars=0;
		 if (getProject().getNumberCharMatrices(file)>1)
			 title = getProject().getCharacterMatrices().getUniqueName("Matrix " + (getProject().getNumberCharMatrices(file)+1) + " in file \"" + file.getName() + "\"");
		 else
			 title = getProject().getCharacterMatrices().getUniqueName("Matrix in file \"" + file.getName() + "\"");
		 boolean fuse = parser.hasFileReadingArgument(fileReadingArguments, "fuseTaxaCharBlocks");

		 /*Problem: for most parts of block lineends are white, even if interleaved.  But Matrix must be pulled in
		with lineends as dark if interleave.  How to do this?  Best to remember previous stringpos, and once matrix
		pulled in, if interleave go back and set stringpos and reread with lineends dark*/
		 int previousPos = startCharC.getValue();
		 boolean taxaLinkFound = false;
		 boolean newTaxaFlag = false;

		 String commandName = null;
		 while (!commandParser.blankByCurrentWhitespace(commandName=commandParser.getNextCommandName(startCharC))) {
			 CommandRecord.tick("Reading " + commandName);
			 if (commandName.equalsIgnoreCase("DIMENSIONS")) {
				 String com = commandParser.getNextCommand(startCharC);
				 if (StringUtil.indexOfIgnoreCase(com, "newtaxa")>=0)
					 newTaxaFlag = true;
				 parser.setString(com); 
				 String tk = null;
				 boolean done = false;
				 while (!done && !StringUtil.blank(tk = parser.getNextToken()))
					 if (tk.equalsIgnoreCase("NCHAR"))
						 done = true;
				 if (done) {
					 tk = parser.getNextToken();
					 numChars = MesquiteInteger.fromString(parser.getNextToken());
					 if (!MesquiteInteger.isCombinable(numChars) || numChars<0){
						 alert("Sorry, the DIMENSIONS statement of the CHARACTERS block appears to be misformatted.  The number of characters is not validly specified.  File reading will fail.");
						 return null;
					 }
				 }
				 log("   " + MesquiteInteger.toString(numChars) + " characters");
				 //numChars = MesquiteInteger.fromString(parser.getTokenNumber(4));
			 }
			 else if (commandName.equalsIgnoreCase("TITLE")) {
				 parser.setString(commandParser.getNextCommand(startCharC)); 
				 title = parser.getTokenNumber(2);
				 logln("Reading CHARACTERS block " + title);

			 }
			 else if (commandName.equalsIgnoreCase("LINK")) {
				 parser.setString(commandParser.getNextCommand(startCharC)); 
				 if ("TAXA".equalsIgnoreCase(parser.getTokenNumber(2))) {
					 taxaLinkFound = true;
					 String taxaTitle = parser.getTokenNumber(4);
					 //logln("       for taxa " + taxaTitle);
					 taxa = getProject().getTaxaLastFirst(taxaTitle);
					 if (taxa == null) {
						 if (getProject().getNumberTaxas()==1)
							 taxa = getProject().getTaxa(0);
						 else
							 alert("Taxa block not found for characters block (name of taxa block \"" + taxaTitle + "\")");
					 }
				 }
			 }
			 else if (commandName.equalsIgnoreCase("FORMAT")) {
				 if (taxa == null && fuse && !MesquiteThread.isScripting()){
					 taxa = getProject().chooseTaxa(containerOfModule(), "A characters block \"" + title + "\" is without a clear reference to a taxa block.  To which taxa block does it belong?", true);
				 }
				 if (taxa==null) {
					 if (taxaLinkFound)
						 alert("Because taxa block not found, the matrix cannot be read.  The CHARACTERS block will be treated as a foreign block in the file.");
					 else {
						 alert("NEXUS file format error: FORMAT statement in CHARACTERS or DATA block occurs before Taxa have been specified in file with more than one set of taxa");
					 }
					 return null;
				 }

				 logln(" for taxa block " + taxa.getName());
				 data = processFormat(file, taxa, commandParser.getNextCommand(startCharC), numChars, title, fileReadingArguments);
				 if (data==null) {
					 alert("Sorry, the CHARACTERS block could not be read, possibly because it is of an unrecognized format.  You may need to activate or install other modules that would allow you to read the data block");
					 return null;
				 }
				 if (getProject().getMatrixNumber(data)<0) { // a new block
					 if (!fuse)
						 data.setName(title);
					 data.deleteUniqueIDs(); //if read from file, then use file's id's and feel free to leave some blank
					 b = data.addToFile(file, getProject(), this); 
				 }
				 else
					 b = data.getNexusBlock();
			 }
			 else if (commandName.equalsIgnoreCase("OPTIONS")) {
				 stringPos.setValue(0);
				 String commandString = commandParser.getNextCommand(startCharC);
				 String subCommand = ParseUtil.getToken(commandString, stringPos);
				 while ((subCommand = ParseUtil.getToken(commandString, stringPos)) !=null){
					 if ("LINKCHARACTERS".equalsIgnoreCase(subCommand)){
						 ParseUtil.getToken(commandString, stringPos); // =
						 String dataTitle = ParseUtil.getToken(commandString, stringPos);
						 CharacterData other = getProject().getCharacterMatrixReverseOrder(dataTitle);
						 if (other != null && data!=null && other!=data) {
							 other.addToLinkageGroup(data);
						 }
					 }
				 }
			 }
			 else if (commandName.equalsIgnoreCase("CHARLABELS")) {
				 if (data == null){
					 alert("Error in NEXUS file:  CHARLABELS before FORMAT statement");
				 }
				 else {
					 MesquiteInteger stc = new MesquiteInteger(startCharC.getValue());
					 parser.setString(commandParser.getNextCommand(stc)); 
					 parser.getNextToken();
					 String cN = parser.getNextToken();
					 int charNumber = 0;
					 while (cN != null && !cN.equals(";") ) {
						 data.setCharacterName(charNumber++, cN);
						 cN = parser.getNextToken();
					 }
					 commandParser.getNextCommand(startCharC); //eating up the full command
				 }
			 }
			 else if (commandName.equalsIgnoreCase("MATRIX")) {
				 if (data==null) {
					 alert("Error in NEXUS file:  Matrix without FORMAT statement");
				 }
				 else if (data.getMatrixManager()!=null) {
					 if (data.interleaved) {    
						 startCharC.setValue(previousPos);
						 commandParser.setLineEndingsDark(true);
						 commandParser.setPosition(previousPos);
						 commandParser.getNextToken();
					 }
					 boolean wassave = data.saveChangeHistory;
					 data.saveChangeHistory = false;
					 data.getMatrixManager().processMatrix(taxa, data, commandParser, numChars, false, 0, newTaxaFlag, fuse, file);
					 if (data.interleaved) 
						 commandParser.setLineEndingsDark(false);
					 startCharC.setValue(commandParser.getPosition());
					 String token = commandParser.getNextCommand();
					 if (token == null || !token.equals(";"))
						 commandParser.setPosition(startCharC.getValue());
					 data.saveChangeHistory = wassave;
				 }
			 }
			 else if (commandName.equalsIgnoreCase("IDS")) {
				 //			parser.setString(commandParser.getNextCommand(startCharC)); 
				 MesquiteInteger stc = new MesquiteInteger(startCharC.getValue());
				 parser.setString(commandParser.getNextCommand(stc)); 
				 parser.getNextToken();
				 String cN = parser.getNextToken();
				 int charNumber = 0;
				 while (cN != null && !cN.equals(";") ) {
					 if (!StringUtil.blank(cN))
						 data.setUniqueID(charNumber, cN);
					 charNumber++;
					 cN = parser.getNextToken();
				 }

				 commandParser.getNextCommand(startCharC); //eating up the full command
			 }
			 else if (commandName.equalsIgnoreCase("BLOCKID")) {
				 MesquiteInteger stc = new MesquiteInteger(startCharC.getValue());
				 parser.setString(commandParser.getNextCommand(stc)); 
				 //				parser.setString(commandParser.getNextCommand(startCharC)); 
				 parser.getNextToken();
				 String cN = parser.getNextToken();
				 if (cN != null && !cN.equals(";")){
					 if (!StringUtil.blank(cN))
						 data.setUniqueID(cN);
					 cN = parser.getNextToken();
				 }
				 commandParser.getNextCommand(startCharC); //eating up the full command
			 }
			 else if (!(commandName.equalsIgnoreCase("BEGIN") || commandName.equalsIgnoreCase("END")  || commandName.equalsIgnoreCase("ENDBLOCK"))) {
				 boolean success = false;
				 String commandString = commandParser.getNextCommand(startCharC);
				 if (data !=null && data.getMatrixManager()!=null)
					 success = data.getMatrixManager().processCommand(data, commandName, commandString);
				 if (!success && b != null) 
					 readUnrecognizedCommand(file,b, name, block, commandName, commandString, blockComments, null);
			 }
			 else
				 commandParser.getNextCommand(startCharC); //eating up the full command
			 previousPos = startCharC.getValue();
		 }
		 if (!fuse && StringUtil.blank(title))
			 data.setName(getProject().getCharacterMatrices().getUniqueName("Untitled (" + data.getDataTypeName() + ")"));
		 if (data != null && blockComments!=null && blockComments.length()>0)
			 data.setAnnotation(blockComments.toString(), false);
		 if (data !=null) {
			 data.resetCellMetadata();
		 }
		 if (fuse) {
			 data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
		 }
		 file.setCurrentData(data);
		 return b;
	 }
	 /*.................................................................................................................*/
	 public String getNameForMenuItem() {
		 return "Character Matrix Manager";
	 }

}

/* ======================================================================== */
class CharactersBlockTest extends NexusBlockTest  {
	public CharactersBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("CHARACTERS") || blockName.equalsIgnoreCase("CONTINUOUS") ;
	}
}

/* ======================================================================== */
/** An object of this kind can be returned by getNexusCommandTest that will be stored in the modulesinfo vector and used
to search for modules that can read a particular command in a particular block.  (Much as the NexusBlockObject.)*/
class CHARNexusCommandTest extends NexusCommandTest  {
	MesquiteInteger pos = new MesquiteInteger();
	/**returns whether or not the module can deal with command*/
	public boolean readsWritesCommand(String blockName, String commandName, String command){
		if (blockName.equalsIgnoreCase("NOTES")  && (commandName.equalsIgnoreCase("CHARACTERS") || commandName.equalsIgnoreCase("SUTM") || commandName.equalsIgnoreCase("SUCM")))
			return true;
		boolean b = (blockName.equalsIgnoreCase("NOTES")  && (commandName.equalsIgnoreCase("SUPPLEMENTAL") || commandName.equalsIgnoreCase("SU") || commandName.equalsIgnoreCase("TEXT") || commandName.equalsIgnoreCase("INTEGER")));
		if (b){
			pos.setValue(0);
			String firstToken = ParseUtil.getFirstToken(command,  pos);

			String[][] subcommands  = ParseUtil.getSubcommands(command, pos);
			if (subcommands == null)
				return false;
			if (StringArray.indexOfIgnoreCase(subcommands, 0, "CHARACTER")<0 && StringArray.indexOfIgnoreCase(subcommands, 0, "C")<0)
				return false;
			if (StringArray.indexOfIgnoreCase(subcommands, 0, "STATE")>=0)
				return false;
			return true;
		}
		return false;
	} 
}


