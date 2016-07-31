/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.InterpretNEXUS;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharactersBlock;
import mesquite.lib.duties.*;


/** A file interpreter for a NEXUS file format.  Sends blocks to various managing modules for reading.  */
public class InterpretNEXUS extends NexusFileInterpreter implements NEXUSInterpreter {
	public String getName() {
		return "NEXUS file";
	}
	public String getNameForMenuItem() {
		return "NEXUS file";
	}
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates the reading and writing of NEXUS files." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.minimal.ManageForeignBlocks.ManageForeignBlocks.class, getName() + " needs a module to manage foreign blocks.",
				"It is activated automatically. ");
	}
	/*.................................................................................................................*/
	MesquiteModule foreignTask;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (mesquiteTrunk.mesquiteModulesInfoVector.findModule(mesquite.minimal.ManageForeignBlocks.ManageForeignBlocks.class, "Manage Foreign Blocks") != null)
			foreignTask = hireNamedEmployee(mesquite.minimal.ManageForeignBlocks.ManageForeignBlocks.class, StringUtil.tokenize("Manage Foreign Blocks"));
		return true;  //make this depend on taxa reader being found?)
	}
	/*.................................................................................................................*/
	public boolean canExportEver() {  
		return true;
	}
	/*.................................................................................................................*/
	public boolean canExportProject(MesquiteProject project) {  
		return true;
	}
	/*.................................................................................................................*/
	public boolean canExportData(Class dataClass) {  
		return true;
	}
	/*.................................................................................................................*/
	public boolean canImport(Class dataClass){
		return true;
	}
	/*.................................................................................................................*/
	public boolean exportFile(MesquiteFile f, String arguments){
		writeFile(f.getProject(), f);
		return true;
	}
	/*.................................................................................................................*/
	public boolean canImport() {  
		return true;
	}
	/** Returns whether the module can read (import) files so as to fuse taxa and matrices */
	public boolean canImport(String arguments){
		return true;
	}
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		//getFileCoordinator().addMenuItem(MesquiteTrunk.editMenu,"List of Nexus Blocks", makeCommand("showBlocks",  this));
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows list of NEXUS blocks", null, commandName, "showBlocks")) { //todo: this should use a real listwindow
			//Check to see if already has lister for this
			boolean found = false;
			int numemp = getNumberOfEmployees();
			for (int i = 0; i<numemp; i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ManagerAssistant)
					if (((ManagerAssistant)e).getName().equalsIgnoreCase("NEXUS blocks list")) {
						((ManagerAssistant)e).getModuleWindow().setVisible(true);
						return e;
					}
			}
			ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("NEXUS Blocks List"));
			if (lister==null){
				alert("Sorry, no module was found to list the NEXUS blocks"); 
				return null;
			}
			if (getProject().getNexusBlocks()!=null) {
				lister.showListWindow(getProject().getNexusBlocks());
				if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
					lister.getModuleWindow().setVisible(true);
				return lister;
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	/** Sort blocks.*/
	private boolean bubbleBlock(ListableVector blocks, NexusBlock nb){
		if (nb==null)
			return false;
		/* pre-2. 02 version: bubbled downward if needed.  Problem: foreign blocks tend to drift upward.
		boolean foundIncoming = false;
		//make sure block is in OK place
		for (int i = blocks.size()-1; i>=0; i--) {
			NexusBlock nR = (NexusBlock)blocks.elementAt(i);
			if (nb==nR)
				foundIncoming = true;
			else if (foundIncoming && (nR.mustBeAfter(nb) && (nb.getFile()==nR.getFile()))) {
				blocks.removeElement(nR, false);
				blocks.insertElementAt(nR, blocks.indexOf(nb)+1, false);
				return true;
			}
		}
		return false; */
		int index = blocks.indexOf(nb); //find current height
		if (index >=0){
			int height = index;
			//make sure block is in OK place
			for (int i = index-1; i>=0; i--) { //now look to all higher to see if nb should jump above them
				NexusBlock nR = (NexusBlock)blocks.elementAt(i);
				if (!nb.mustBeAfter(nR) && (nb.getFile()==nR.getFile())) { //nb needs to jump above nR
					height = i;  //remember height of nR
				}
			}
			if (height < index){ //nb needs to jump to height; move it
				blocks.removeElement(nb, false);
				blocks.insertElementAt(nb, height, false);
				return true;
			}
		}
		return false;

	}
	/** Sort blocks.*/
	private  void sortBlocks(ListableVector blocks){
		if (blocks==null)
			return;
		Object[] bs = new Object[blocks.size()];
		Enumeration e = blocks.elements();
		int count=0;
		while(e.hasMoreElements())
			bs[count++]=e.nextElement();

		for (int i=count-1; i>=0; i--) {
			while (bubbleBlock(blocks, (NexusBlock)bs[i]))
				;
		}
		//This is a kludge because of problems with mustBeAfter (as of 3. 03)
		for (int i=count-1; i>=0; i--) {
			if (bs[i] instanceof CharactersBlock){
				blocks.removeElement((NexusBlock)bs[i], false);
				blocks.insertElementAt((NexusBlock)bs[i], 0, false);
			}
			if (bs[i] instanceof TaxaBlock){
				blocks.removeElement((NexusBlock)bs[i], false);
				blocks.insertElementAt((NexusBlock)bs[i], 0, false);
			}
		}

	}
	/** Asks if block is sorted relative to other blocks in its file and before its file in file read order.
	NOT USED as of 3. 02 */
	private boolean needsToMove(ListableVector blocks, NexusBlock nb){
		if (nb==null)
			return false;
		MesquiteFile fileOfBlock = nb.getFile();
		ListableVector files = getProject().getFiles();
		boolean done  = false;
		for (int f = 0; f<files.size(); f++){  //for all files earier or same in list as file containing block
			MesquiteFile fileToCheck = (MesquiteFile)files.elementAt(f);
			boolean afterTarget = false;
			NexusBlock nR = null;
			//make sure block is in OK place
			for (int i = 0; i<blocks.size(); i++) {  //look through blocks list
				nR = (NexusBlock)blocks.elementAt(i);
				if (nb!=nR && fileToCheck==nR.getFile()){
					if (afterTarget){
						if (nb.mustBeAfter(nR))
							return true;
					}
					else {
						if (nR.mustBeAfter(nb))
							return true;
					}
				}
				if (nb == nR) {
					afterTarget = true;
				}
			}
		}
		return false;
	}
	/** Check to see if blocks sorted properly across all files in order of file saving.
	NOT USED as of 3. 02 */
	private  boolean sortedAcrossFiles(ListableVector blocks){
		if (blocks==null)
			return true;
		Enumeration e = blocks.elements();
		while(e.hasMoreElements()) {
			NexusBlock nb = (NexusBlock)e.nextElement();
			if (needsToMove(blocks, nb))
				return false;
		}

		return true;
	}
	/** adds nexus block to given file.*/
	public  void addBlock(NexusBlock nb){
		if (nb==null)
			return;
		getProject().addNexusBlock(nb);
		sortBlocks(getProject().getNexusBlocks());
	}
	/** adds nexus block to given file.*/
	public  void removeBlock(NexusBlock nb){
		if (nb==null)
			return;
		getProject().removeNexusBlock(nb);
	}
	/** adds nexus block to given file.*/
	public  NexusBlock findBlock(FileElement e){
		ListableVector blocks = getProject().getNexusBlocks();
		if (e==null || blocks == null)
			return null;
		for (int i=0; i<blocks.size(); i++) {
			NexusBlock nb = (NexusBlock)blocks.elementAt(i);
			if (nb.contains(e)) 
				return nb;
		}
		return null;
	}
	/*.................................................................................................................*/
	public boolean canReadFile(MesquiteFile f) {

		if (f ==null) {
			return false;
		}
		else if (f.getFileName().endsWith(".nex")) {
			return true;
		}
		else {
			if (f.openReading()){
				String s = parser.getFirstToken(f.firstToken(null));
				f.closeReading();
				if (s==null)
					return false;
				return s.equalsIgnoreCase("#NEXUS");
			}
		}
		System.out.println("file can't be interpreted: " + f.getFileName());
		return false;
	}

	/*.................................................................................................................*/
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public void readFile(MesquiteProject mf, MesquiteFile mNF, String arguments) {
		incrementMenuResetSuppression();
		int length = (int)mNF.existingLength();
		int readToNow = 0;
		ProgressIndicator progIndicator = null;
		Thread mt = Thread.currentThread();
		boolean piMine = false;
		if (mt instanceof MesquiteThread) 
			progIndicator = ((MesquiteThread)mt).getProgressIndicator();
		if (progIndicator ==null) {
			progIndicator = new ProgressIndicator(mf,"Reading File "+ mNF.getName(), mNF.existingLength());
			piMine = true;
			if (mt instanceof MesquiteThread)
				((MesquiteThread)mt).setProgressIndicator(progIndicator);
		}
		progIndicator.setButtonMode(ProgressIndicator.FLAG_AND_HIDE);
		progIndicator.start();
		mNF.linkProgressIndicator(progIndicator);
		if (mNF.openReading()) {
			try {
				if (!MesquiteFile.suppressReadWriteLogging) 
					logln("Reading NEXUS file " + mNF.getFileName());
				mNF.foreignElements = new Vector();
				String token= mNF.firstToken(null);
				if (token!=null) {
					if (!token.equalsIgnoreCase("#NEXUS")) {
						alert("Not a valid NEXUS file (first token is \"" + token + "\"");
					}
					else {
						FileBlock block;
						boolean abort = false;
						boolean mesquiteBlockFound = false;
						MesquiteString blockName = new MesquiteString();
						StringBuffer fileComments = new StringBuffer(100);
						StringBuffer blockComments = new StringBuffer(100);
						MesquiteFile sF = progIndicator.getScriptingFile();
						progIndicator.setScriptingFile(mNF);
						int buttonMode = progIndicator.getButtonMode();
						String buttonName = progIndicator.getStopButtonName();
						while (!abort && !StringUtil.blank(block = mNF.getNextBlock( blockName, fileComments, blockComments))) {
							CommandRecord.tick("Reading block " + blockName);
							if ("Mesquite".equalsIgnoreCase(blockName.getValue())) {
								mesquiteBlockFound = true;
								mf.virginProject = false;
								progIndicator.setButtonMode(ProgressIndicator.OFFER_CONTINUE_FORCEQUIT);
								progIndicator.setStopButtonName("Emergency Stop");
							}
							else
								progIndicator.setButtonMode(ProgressIndicator.FLAG_AND_HIDE);
							if (mNF.mrBayesReadingMode && "Trees".equalsIgnoreCase(blockName.getValue())){
								mNF.setTranslatedCharacter('[', '<');
								mNF.setTranslatedCharacter(']', '>');
							}
							progIndicator.setText("Processing block: " + blockName.getValue(), false, true);
							NexusBlock nb = sendBlockToReader(mf, mNF, block, blockName.getValue(), length, readToNow, blockComments, arguments);
							if (false && mNF.mrBayesReadingMode && "Trees".equalsIgnoreCase(blockName.getValue())){
								mNF.clearTranslatedCharacter('[');
								mNF.clearTranslatedCharacter(']');
							}
							progIndicator.setText("Reading next block", false);
							progIndicator.toFront();
							readToNow += block.getNumCommands();
							if (mNF.getFileAborted())
								abort = true;
						}
						progIndicator.setScriptingFile(sF);
						progIndicator.setButtonMode(buttonMode);
						progIndicator.setStopButtonName(buttonName);
						if (piMine)
							progIndicator.setOwnerThread(null);

						if (abort){ //���		
							progIndicator.goAway();
							mNF.closeReading();
							mNF.close();
							resetAllMenuBars();
							decrementMenuResetSuppression();
							return;
						}
						if (fileComments.length()>0) {
							if (mNF.getAnnotation()!=null)
								mNF.setAnnotation(mNF.getAnnotation() + fileComments.toString(), false);
							else
								mNF.setAnnotation(fileComments.toString(), false);
						}
						if (!mesquiteBlockFound && (mNF == mf.getHomeFile())) {
							MesquiteModule mb = getProject().getCoordinatorModule();
							if (mb != null){
								MesquiteWindow mw = mb.getModuleWindow();
								if (mw != null)
									mw.setWindowSize(700, 500);
								if (getProject().getNumberCharMatrices()>0){
									MesquiteModule mbb = findNearestColleagueWithName("Data Window Coordinator");
									if (mbb != null)
										mbb.doCommand("showDataWindow", "0", CommandChecker.defaultChecker);
								}
								else if (getProject().getNumberTaxas()>0){
									MesquiteModule mbb = findNearestColleagueWithName("Manage TAXA blocks");
									if (mbb != null)
										mbb.doCommand("showTaxa", "0", CommandChecker.defaultChecker);
								}
							}

						}
						progIndicator.goAway();
						if (!MesquiteFile.suppressReadWriteLogging) 
							logln("File reading complete (file " + mNF.getFileName() + ")");
					}
				}
			}
			catch (MesquiteException e){
			}
			mNF.closeReading();
			//mNF.reportTimes();
			if (mNF.foreignElements.size()>0){


				logln("");
				logln("This file contained one or more blocks or commands not recognized; a list is written below.  It is possible that some packages of Mesquite that are not currently loaded might be able to recognize the commands; try rereading after choosing \"Use All Installed Modules\" under \"Activate/Deactivate Packages\".\n");
				for (int i=0; i<mNF.foreignElements.size(); i++) {
					logln("Unrecognized " + mNF.foreignElements.elementAt(i));
				}
				logln("");
			}
			mNF.foreignElements = null;
		}
		if (mf.windowToActivate !=null) {
			MesquiteWindow w = mf.windowToActivate;
			if (w.isPoppedOut() && !w.getPopAsTile()){
				MesquiteFrame f = mf.getCoordinatorModule().getModuleWindow().getParentFrame();
				MesquiteWindow pw = f.frontMostInLocation(MesquiteFrame.MAIN);
				if (pw != null)
					w = pw;
			}

			MesquiteFrame f = w.getParentFrame();
			if (f != null){
				f.setAsFrontWindow(w);
				f.showFrontWindow();
			}
			mf.windowToActivate = null;
		}
		String s = null;
		if ((s = mNF.getOpenAsUntitled())!=null ){
			if (mNF.getReadCategory() == MesquiteFile.INCLUDED){
				alert("File reading encountered the following issues.  Some components of the file might not have been read properly.  Please check any data in the file.\n" + s);
			}
			else {
				alert("File reading encountered the following issues.  Some components of the file might not have been read properly, and could be lost upon resaving.  For safety's sake, you will be asked to choose another file name to avoid writing over the current file.\n" + s);
				mNF.changeLocation(mNF.getDirectoryName(),"Untitled");
				boolean newFilenameChosen = mNF.changeLocation("Please save the file under a new name");
				mNF.setDirtiedByCommand(true);
				getFileCoordinator().writeFile(mNF);
			}
		}
		if (getProject() != null) {
			resolveCharMatrixIDs();
		}
		decrementMenuResetSuppression();
	}
	/*.................................................................................................................*/
	private int  getNumberCharMatricesWithAssignedID(long ref) {   //MOVE TO InterpretNEXUS
		ListableVector datasVector = getProject().getCharacterMatrices();
		int count = 0;
		for (int i=0; i<datasVector.size(); i++) {
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datasVector.elementAt(i);
			if (!data.isDoomed() && ref  == (data.getAssignedIDNumber())) {
				count++;
			}
		}

		return count;  
	}
	/*.................................................................................................................*/
	/** resolves conflicts among assigned id's */
	public void  resolveCharMatrixIDs() {  //MOVE TO InterpretNEXUS

		ListableVector datasVector = getProject().getCharacterMatrices();
		for (int i=datasVector.size()-1; i>=0; i--) {
			mesquite.lib.characters.CharacterData data = (mesquite.lib.characters.CharacterData)datasVector.elementAt(i);
			if (getNumberCharMatricesWithAssignedID(data.getAssignedIDNumber())>1) {
				long a = MesquiteLong.unassigned;
				while (!MesquiteLong.isCombinable(a))
					a = Math.abs(FileElement.randomNumberGenerator.nextLong());
				data.setAssignedIDNumber(a);
			}
		}
	}

	/*.................................................................................................................*/
	/** finds the ith block of a given type and returns it raw.*/
	public FileBlock readOneBlock(MesquiteProject mf, MesquiteFile mNF, String blockType, int i){
		if (blockType == null)
			return null;
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator =  new ProgressIndicator(mf,"Processing File "+ mNF.getName() + " to find " + blockType + " block", mNF.existingLength());
		progIndicator.start();
		FileBlock blockSought = null;
		if (mNF.openReading()) {
			try {
				logln("Processing File "+ mNF.getName() + " to find " + blockType + " block");
				String token= mNF.firstToken(null);
				if (token!=null) {
					if (!token.equalsIgnoreCase("#NEXUS")) {
						alert("Not a valid NEXUS file (first token is \"" + token + "\"");
					}
					else {
						MesquiteString blockName = new MesquiteString();
						StringBuffer blockComments = new StringBuffer(100);
						int count = 0;
						FileBlock block = null;
						while (blockSought == null && !StringUtil.blank(block = mNF.getNextBlock( blockName, null, null))) {
							if (blockType.equalsIgnoreCase(blockName.toString())){
								if (count == i) {
									//block found!
									blockSought = block;
									block.setFile(mNF);

								}
								count++;
							}
						}
						progIndicator.goAway();
						logln("File reading complete (file " + mNF.getFileName() + ")");
					}
				}
			}
			catch (MesquiteException e){
			}
			mNF.closeReading();
		}
		decrementMenuResetSuppression();
		return blockSought;
	}
	/*.................................................................................................................*/
	/** Finds the first employee in the heirarchy that  has a particular name.*/
	private MesquiteModule findEmployeeThatCanRead(MesquiteModule module, FileBlock block, String blockName) {
		if (blockName ==null || module == null)
			return null;
		Enumeration enumeration=module.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule mb = (MesquiteModule)enumeration.nextElement();
			MesquiteModuleInfo mbi = mb.getModuleInfo();
			if (mbi.getNexusBlockTest()!=null && mbi.getNexusBlockTest().readsWritesBlock(blockName, block)) {
				return mb;
			}
		}
		//not found among immediate employees; look deeper
		enumeration=module.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			MesquiteModule mb = (MesquiteModule)enumeration.nextElement();
			MesquiteModule result = findEmployeeThatCanRead(mb, block, blockName);
			if (result != null)
				return result;
		}
		return null;
	}
	/*.................................................................................................................*/
	boolean isForeignBlock(String blockName){		//first check employees to see if they can understand block
		if (blockName == null)
			return true;
		MesquiteModule rM = findEmployeeThatCanRead(getFileCoordinator(), null, blockName);
		if (rM!=null) {
			return false;
		}

		//then look for others to hire that might understand block
		Enumeration enumeration=MesquiteTrunk.mesquiteModulesInfoVector.elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			MesquiteModuleInfo mbi = (MesquiteModuleInfo)obj;
			if ((rM==null || rM.getModuleInfo()!=mbi) && mbi.getNexusBlockTest()!=null && mbi.getNexusBlockTest().readsWritesBlock(blockName, null)) {
				return false;
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	private NexusBlock sendBlockToReader(MesquiteProject mp, MesquiteFile mf, FileBlock block, String blockName, int totalLength, int readToNow, StringBuffer blockComments, String fileReadingArguments) {
		if (blockName == null)
			return null;

		//first check employees to see if they can understand block
		MesquiteModule rM = findEmployeeThatCanRead(getFileCoordinator(), block, blockName);
		ListableVector blocks = getProject().getNexusBlocks();
		if (rM!=null) {
			if (!MesquiteFile.suppressReadWriteLogging) 
				logln("Reading block: " + blockName);
			long startTimeForBlock = System.currentTimeMillis();
			NexusBlock nb = rM.readNexusBlock( mf, blockName, block, blockComments, fileReadingArguments);
			if (MesquiteTrunk.debugMode)
				logln("    time to read:  " + ((System.currentTimeMillis() - startTimeForBlock)/1000.00) + " sec.");
			if (nb!=null){
				if (blocks.indexOf(nb)<0) {
					blocks.addElement(nb, false);
				}
				return nb;
			}
		}

		//then look for others to hire that might understand block
		Enumeration enumeration=MesquiteTrunk.mesquiteModulesInfoVector.elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			MesquiteModuleInfo mbi = (MesquiteModuleInfo)obj;
			if ((rM==null || rM.getModuleInfo()!=mbi) && mbi.getNexusBlockTest()!=null && mbi.getNexusBlockTest().readsWritesBlock(blockName, block)) {
				logln("Reading special block: " + blockName);
				MesquiteModule mb = hireEmployeeFromModuleInfo(mbi, MesquiteModule.class);
				NexusBlock nb = mb.readNexusBlock( mf, blockName, block, blockComments, fileReadingArguments);
				if (nb!=null){
					if (blocks.indexOf(nb)<0) {
						blocks.addElement(nb, false);
					}
					return nb;
				}
			}
		}
		if (mf.foreignElements!=null && !("paup".equalsIgnoreCase(blockName) || "macclade".equalsIgnoreCase(blockName)))
			mf.foreignElements.addElement("Block: " + blockName);
		if (foreignTask!=null) {
			NexusBlock nb = foreignTask.readNexusBlock(mf, blockName, block, blockComments, fileReadingArguments);
			logln("Storing foreign block: " + blockName);
			if (nb!=null) {
				blocks.addElement(nb, false);
				return nb;
			}
		}
		else
			logln("No module found to read block: " + blockName);
		return null;
	}

	String addendum = "";
	public void getExportOptions(boolean dataSelected, boolean taxaSelected){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog exportDialog = new ExtensibleDialog(containerOfModule(), "NEXUS Options", buttonPressed);
		//exportDialog.setSuppressLineEndQuery(true);
		exportDialog.setDefaultButton(null);
		exportDialog.addLabel("Addendum to file: ");
		TextArea fsText =exportDialog.addTextAreaSmallFont(addendum,4);

		exportDialog.completeAndShowDialog();

		boolean ok = (buttonPressed.getValue()==0);
		if (ok)
			addendum = fsText.getText();
		exportDialog.dispose();
	}	
	/*.................................................................................................................*/
	/** Called to see if file ready to be written.*/
	void checkIntegrityForWriting(MesquiteModule module, MesquiteFile file) {
		if (module==null)
			return;

		EmployeeVector e = module.getEmployeeVector();
		if (e==null)
			return;
		for (int i = 0; i<e.size(); i++) {
			MesquiteModule employee = (MesquiteModule)e.elementAt(i);

			if (employee!=null){
				employee.preWritingCheck(file, "NEXUS");
				checkIntegrityForWriting(employee, file);
			}
		}
	}
	/*.................................................................................................................*/
	public void writeFile(MesquiteProject mf, MesquiteFile mNF) {
		//boolean setTC = !MesquiteFile.fileExists(mNF.getPath());
		if (mNF.openWriting(true)) {
			if (mNF.exporting == 1){
				if (okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying about options"))
					getExportOptions(false, false);
			}
			checkIntegrityForWriting(getFileCoordinator(), mNF);
			mNF.setIsNexus(true);
			ListableVector blocks = getProject().getNexusBlocks();
			MesquiteTimer time = new MesquiteTimer();
			time.start();
			MesquiteBoolean finishedWriting = new MesquiteBoolean(false);

			sortBlocks(blocks);
			ProgressIndicator progIndicator = new ProgressIndicator(mf,"Writing File "+ mNF.getName(), blocks.size(), false);
			progIndicator.start();

			mNF.writeLine("#NEXUS");

			Date d = new Date(System.currentTimeMillis());
			String s = "";
			String loc = "";
			try {
				loc = " at " + java.net.InetAddress.getLocalHost();
			}
			catch (java.net.UnknownHostException e){
			}
			if (!MesquiteModule.author.hasDefaultSettings())
				loc += " (" + author.getName() + ")";
			mNF.writeLine("[written " + d.toString() + " by Mesquite " + s + " version " + getMesquiteVersion()  + getBuildVersion() + loc + "]"); 
			//mNF.writeLine("[!" + mNF.getAnnotation() + "]");

			for (int i=0; i<blocks.size(); i++) {
				NexusBlock nb = (NexusBlock)blocks.elementAt(i);
				if (nb.getFile() == mNF && nb.getWritable()) {
					progIndicator.setCurrentValue(i);
					progIndicator.setText("Preparing to write " + nb.getName() );
					//	logln("      Writing " + nb.getName());

					nb.writeNEXUSBlock(mNF, progIndicator);
				}
			}
			if (addendum != null)
				mNF.writeLine(addendum);
			mf.fileSaved(mNF);
			progIndicator.goAway();
			time.end();
			logln("      File writing finished " + time.getAccumulatedTime());
			mNF.closeWriting(NexusBlock.numBackups);
			//if (setTC) setFileTypeCreator(mNF);
		}

	}
	/*.................................................................................................................*/
	public String preferredDataFileExtension() {
		return "nex";
	}
	/*.................................................................................................................*/
	/** Returns wether this interpreter uses a flavour of NEXUS.  Used only to determine whether or not to add "nex" as a file extension to imported files (if already NEXUS, doesn't).**/
	/*public boolean usesNEXUSflavor(){
		return true;
	}
*/

}

