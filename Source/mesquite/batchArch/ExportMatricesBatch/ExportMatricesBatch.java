/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.batchArch.ExportMatricesBatch; 

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.batchArch.BatchTemplateManager.BatchTemplateManager;
import mesquite.batchArch.lib.*;

public class ExportMatricesBatch extends FileInit  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(CharMatrixSource.class, getName() + " needs a source of matrices for export.",
		"You can choose the source of character matrices for export when Export Matrices and Batch files is requested.");
		EmployeeNeed e = registerEmployeeNeed(mesquite.batchArch.ChartFromInstructions.ChartFromInstructions.class, getName() + " needs an assistant to draw charts.",
		"This is arranged automatically.");
		
	}
 	ExtensibleDialog dialog = null;
	TemplateRecord template;
	TemplateManager templateManager;
	MesquiteSubmenuSpec mss;//D!
	CharMatrixSource characterSourceTask;
	String directoryPath, baseName;
	boolean writeOnlySelectedTaxa=false;
	boolean writeOnlyTaxaWithData=true;

	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/*.................................................................................................................*/
 	/** A method called immediately after the file has been read in.*/
 	public void projectEstablished() {
		//D!
		mss = getFileCoordinator().addSubmenu(MesquiteTrunk.analysisMenu, "Batch Architect");
		mss.setZone(2);
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.analysisMenu, mss, "Export Matrices & Batch Files...", makeCommand("exportMatrixBatch",  this));
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.analysisMenu, mss, "Export Batch Files...", makeCommand("exportBatch",  this));
		getFileCoordinator().addItemToSubmenu(MesquiteTrunk.analysisMenu, mss, "Chart Results via Instruction File...", makeCommand("chartInstructionFile",  this));
		super.projectEstablished();
 	}
 	
/*.................................................................................................................*/
	private void saveFile(String matrixExportFormatName, MesquiteFile file, String fileName, String directoryPath, boolean usePrevious, FileCoordinator coord){
		if (matrixExportFormatName.equalsIgnoreCase(TemplateRecord.defaultExportFormat) || matrixExportFormatName.equalsIgnoreCase("NEXUS file") || matrixExportFormatName.equalsIgnoreCase("NEXUS file interpreter")) {
			coord.writeFile(file);
		}
		else {
			FileInterpreterI matrixExportFormat = (FileInterpreterI)coord.findEmployeeWithName(matrixExportFormatName);
			if (matrixExportFormat!=null) {
				String ext = matrixExportFormat.preferredDataFileExtension();
				if (StringUtil.blank(ext))
					ext = "";
				else
					ext = "." + ext;
				String s = "file = " + StringUtil.tokenize(fileName + ext) + " directory = " + StringUtil.tokenize(directoryPath);
				s += " noTrees ";
				if (usePrevious)
					s+= " usePrevious";
				boolean success = coord.export(matrixExportFormat, file, s);
				if (!success)
						MesquiteMessage.println("FILE SAVING FAILED (" + matrixExportFormatName + ") for " + file.getName());

			}
			else 
				MesquiteMessage.println("FILE SAVING FAILED because intepreter not found (" + matrixExportFormatName + ") for " + file.getName());

		}
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
		Enumeration enumeration=module.getEmployeeVector().elements();
		while (enumeration.hasMoreElements()){
			Object obj = enumeration.nextElement();
			if (obj instanceof mesquite.batchArch.ChartFromInstructions.ChartFromInstructions)
				//mesquite.genesis.ChartFromInstructions employee = (mesquite.batchArch.ChartFromInstructions)obj;
  	 			temp.suppressCommandsToEmployee( (MesquiteModule)obj); 
		}
 	 	return temp;
  	 }
  	 
  	Checkbox writeOnlySelectedTaxaCheckBox = null;
 	Checkbox writeOnlyTaxaWithDataCheckBox = null;

	/*.................................................................................................................*/
	public Object exportMatricesAndBatchFiles (String arguments,  boolean includeMatrices) {
 		//ask user how which taxa, how many characters
	 		//create chars block and add to file
	 		//return chars
	 		
 		Taxa taxa = null;
 		
		if (getProject().getNumberTaxas()==0) {
			alert("Data matrices cannot be created until taxa exist in file.");
			return null;
		}
		else 
			taxa = getProject().chooseTaxa(containerOfModule(), "For which block of taxa do you want to save copies of character matrices?");
		if (taxa == null)
			return null;
		

		MainThread.incrementSuppressWaitWindow();
		characterSourceTask = null;
		if (includeMatrices) {
			if (StringUtil.blank(arguments))
				characterSourceTask = (CharMatrixSource)hireEmployee(CharMatrixSource.class, "Export matrices from:");
			else
				characterSourceTask = (CharMatrixSource)hireNamedEmployee(CharMatrixSource.class, arguments);
		}
 		if (characterSourceTask != null || !includeMatrices) {
			incrementMenuResetSuppression();
			if (templateManager == null)
				templateManager= (TemplateManager)getFileCoordinator().findEmployeeWithName("#BatchTemplateManager");
			if (templateManager == null)
				return bailOut(null, characterSourceTask, null);
			
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
 			int num= MesquiteInteger.unassigned;
 			if (includeMatrices)
 				num = characterSourceTask.getNumberOfMatrices(taxa);
 			ObjectContainer templateContainer = new ObjectContainer();
 			
			String message="";
			ExtensibleDialog dialog =null;
			if (includeMatrices) {
				dialog = templateManager.getChooseTemplateDLOG(taxa, "Export Matrices & Batch Files", templateContainer, buttonPressed, includeMatrices);
				message = "Export Matrices and Batch Files will save a series of files, each containing a matrix, using the matrix source \"" + characterSourceTask.getName();
				message += "\".  In addition it will save batch files, based on the chosen template, which may serve to instruct another program (e.g., for parametric bootstrapping).";
				message += " The format of the matrix file (e.g., NEXUS) is determined by options in the template. ";
				message += " \n\nTo create and edit templates, or see existing ones, use the Template Manager, available by touching on the \"Edit Templates\" button. ";
				message += " To create and edit code snippets, or see existing ones, use the Code Snippet Manager, available by touching on the \"Edit Snippets\" button. ";
				message += " Information about where templates and code snippets are stored is available under the help facility of these managers.";
				dialog.addLabel("Matrices", Label.LEFT,true,true);
			}
			else {
				dialog = templateManager.getChooseTemplateDLOG(taxa, "Export Batch Files", templateContainer, buttonPressed, includeMatrices);
				message = "Export Batch Files will save batch files, based on the chosen template, which may serve to instruct another program (e.g., for parametric bootstrapping).";
				message += " \n\nTo create and edit templates, or see existing ones, use the Template Manager, available by touching on the \"Edit Templates\" button. ";
				message += " To create and edit code snippets, or see existing ones, use the Code Snippet Manager, available by touching on the \"Edit Snippets\" button. ";
				message += " Information about where templates and code snippets are stored is available under the help facility of these managers.";
				dialog.addLabel("Replicates", Label.LEFT,true,true);
			}
			dialog.appendToHelpString(message);
			dialog.setHelpURL(this,"");

			SingleLineTextField baseNameField = dialog.addTextField("Base name:  ", "untitled",20);
			SingleLineTextField numReps = null;
			if (!MesquiteInteger.isCombinable(num)) {
				numReps = dialog.addTextField("Number of replicates:  ","10",8);
			}
			if (taxa.anySelected()) {
				 writeOnlySelectedTaxaCheckBox = dialog.addCheckBox("include data only for selected taxa", writeOnlySelectedTaxa);
			}
			// writeOnlyTaxaWithDataCheckBox = dialog.addCheckBox("include data only for taxa with data", writeOnlyTaxaWithData);
			dialog.completeAndShowDialog();
			
			
			if (dialog.query() != 0)
				return bailOut(null, characterSourceTask, null);
			TemplateRecord template = (TemplateRecord)templateContainer.getObject();
			if (template == null) 
				return bailOut(null, characterSourceTask, null);
			
			String baseName = baseNameField.getText();
			if (numReps != null)
				num = MesquiteInteger.fromString(numReps.getText());
			if (!MesquiteInteger.isCombinable(num)) 
				return bailOut(null, characterSourceTask, null);

			String directoryPath = MesquiteFile.chooseDirectory("Choose location for saving files:"); //MesquiteFile.saveFileAsDialog("Base name for files (files will be named <name>1.nex, <name>2.nex, etc.)", baseName);
			if (StringUtil.blank(directoryPath))
				return bailOut(null, characterSourceTask, null);

			if (writeOnlySelectedTaxaCheckBox!=null)
				writeOnlySelectedTaxa= writeOnlySelectedTaxaCheckBox.getState();
			if (writeOnlyTaxaWithDataCheckBox!=null)
				writeOnlyTaxaWithData= writeOnlyTaxaWithDataCheckBox.getState();

				dialog.dispose();
			dialog = null;
			
			//StringBuffer outputBuffer=null;
			String s2 = "";
			
			export(template, directoryPath, baseName, taxa, includeMatrices, characterSourceTask, num);

			fireEmployee(characterSourceTask);
			resetAllMenuBars();
			decrementMenuResetSuppression();
 		}
		MainThread.decrementSuppressWaitWindow();
		return null;
	}
	
	
	boolean saveBasisTrees = true;
	
	/*.................................................................................................................*/

	private Object export(TemplateRecord template,String directoryPath, String baseName,Taxa taxa,  boolean includeMatrices, CharMatrixSource characterSourceTask, int num){
			if (template == null) {
				alert("Sorry, the batch file template was not found");
				return null;
			}
			else if (includeMatrices && taxa == null) {
				alert("Sorry, no taxa were found for Export Matrices & Batch Files");
				return null;
			}
			else if (includeMatrices && characterSourceTask == null) {
				alert("Sorry, no source of characters was found for Export Matrices & Batch Files");
				return null;
			}
			getProject().incrementProjectWindowSuppression();
			//Need block of taxa, basename, basePath, template, number of matrices, matrix source (along with its parameters), 
    			String basePath = directoryPath + MesquiteFile.fileSeparator + baseName;
			FileCoordinator coord = getFileCoordinator();
			MesquiteFile tempDataFile = null;
			if (includeMatrices) {
				tempDataFile = (MesquiteFile)coord.doCommand("newLinkedFile", StringUtil.tokenize(basePath + ".nex"), CommandChecker.defaultChecker); //TODO: never scripting???
				TaxaManager taxaManager = (TaxaManager)findElementManager(Taxa.class);
				Taxa newTaxa =taxa.cloneTaxa();
				newTaxa.addToFile(tempDataFile, null, taxaManager);
			}
			boolean showProgress = true;
			ProgressIndicator progIndicator=null;
			if (showProgress) {
				progIndicator = new ProgressIndicator(getProject(),"Saving files", num);
				progIndicator.start();
				progIndicator.setCurrentValue(0);
			}
			
			StringBuffer parameterInfoBuffer = new StringBuffer();
			parameterInfoBuffer.append("Mesquite version " + getMesquiteVersion() +", build " + getBuildVersion()+"\n");
			parameterInfoBuffer.append("Time of creation of files:" + StringUtil.getDateTime()+"\n\n");
			StringBuffer matrixInfoBuffer = new StringBuffer();
			
			
			template.composeAccessoryFilesStart(num, baseName, basePath);
			boolean usePrevious = false;
			boolean parametersWritten = false;
			boolean logVerbose = true;
			CharMatrixManager manager = null;
			TreeVector trees = null;
	 		CharacterData newMatrix=null;
	 		MesquiteTimer timer = new MesquiteTimer();
	 		timer.start();
	 		
			try {
				for (int iMatrix = 0; iMatrix<num; iMatrix++){
					if (progIndicator!=null)
						if (includeMatrices)
							progIndicator.setText("Saving file "+(iMatrix+1)+" of " + num);
						else
							progIndicator.setText("Processing replicate "+(iMatrix+1)+" of " + num);
					MesquiteThread.setSuppressAllProgressIndicatorsCurrentThread(true);
			 		MCharactersDistribution matrix = null;
			 		if (includeMatrices) {
						logln("Saving file " + basePath + iMatrix + template.fileExtension(template.matrixExportFormat,coord,true));	
				 		matrix = characterSourceTask.getMatrix(taxa, iMatrix);
						if (matrix==null)
							return bailOut(tempDataFile, characterSourceTask, progIndicator);

							CharactersManager manageCharacters = (CharactersManager)findElementManager(CharacterData.class);
							manager = manageCharacters.getMatrixManager(matrix.getCharacterDataClass());
					}
					if (!includeMatrices) {
						System.gc();
						String matrixName = null;
						if (matrix.getParentData()!= null)
							matrixName = matrix.getParentData().getName();
						else
							matrixName = matrix.getName();
						template.composeAccessoryFilesReplicate(iMatrix, matrixName, 0, baseName, basePath);
					}
					else if (manager != null){
						logVerbose = manager.isLogVerbose();
						manager.setLogVerbose(false);
						if (matrix.getBasisTree()!=null && saveBasisTrees) {
							if (trees == null)
								trees = new TreeVector(taxa);
							trees.addElement(matrix.getBasisTree().cloneTree(), false);//no need to establish listener to Taxa, as temporary
						}
						newMatrix = matrix.makeCharacterData(manager, taxa);
						if (writeOnlySelectedTaxa) {
							for (int it=0; it<taxa.getNumTaxa(); it++) 
								if (!taxa.getSelected(it))
									newMatrix.setToInapplicable(it);
						}
						newMatrix.setName(characterSourceTask.getMatrixName(taxa, iMatrix));
						
						logln(newMatrix.getExplanation() + "\n");	
						newMatrix.addToFile(tempDataFile, getProject(), null);
				 	 	tempDataFile.setPath(basePath + iMatrix + template.fileExtension(template.matrixExportFormat,coord, true));
						if (!parametersWritten) {
							matrixInfoBuffer.append("file\t"+newMatrix.getTabbedTitles()+"\n");
						}
						matrixInfoBuffer.append(baseName + iMatrix+"\t"+newMatrix.getTabbedSummary()+"\n");
				 	 	
				 	 	saveFile(template.matrixExportFormat, tempDataFile, baseName + iMatrix,directoryPath, usePrevious, coord); 
				 	 	newMatrix.deleteMe(false);
						newMatrix = null;
						if (matrix.getBasisTree()!=null) {
							matrix.getBasisTree().dispose();
							matrix.setBasisTree(null);
						}
						System.gc();
						template.composeAccessoryFilesReplicate(iMatrix, characterSourceTask.getMatrixName(taxa, iMatrix), 0, baseName, basePath);
						manager.setLogVerbose(logVerbose);
					}
					if (!parametersWritten) {
						parameterInfoBuffer.append(characterSourceTask.accumulateParameters("\n"));
						parametersWritten=true;
					}

					MesquiteThread.setSuppressAllProgressIndicatorsCurrentThread(false);
					if (progIndicator!=null) {
						progIndicator.setCurrentValue(iMatrix+1);
						if (progIndicator.isAborted()) {
							break;
						}
					}
					usePrevious = true;
					logln("File written by Export matrices & Batch Files: " + basePath + iMatrix + template.fileExtension(template.matrixExportFormat,coord,true) + ".\n   Time taken (ms): " + timer.timeSinceLast() + " ms");
				}
			}
			catch (Exception e) {
				// clean up anything added
				if (manager != null){
					logVerbose = manager.isLogVerbose();
					manager.setLogVerbose(logVerbose);
				}
				MesquiteMessage.notifyUser("There was a problem with creating or exporting the files, and the process was aborted: " + e.getMessage());
				if (newMatrix!=null) {
					newMatrix.deleteMe(false);
					newMatrix = null;
				}
			}
			timer.end();
			String analysisFilePath = MesquiteFile.getUniqueModifiedFileName(basePath+".AnalysisInfo", "txt");
			MesquiteFile.putFileContents(analysisFilePath, parameterInfoBuffer.toString(), true);
			if (includeMatrices && manager!=null) {
				String matrixSummaryFilePath = MesquiteFile.getUniqueModifiedFileName(basePath+".MatrixSummaries", "txt");
				MesquiteFile.putFileContents(matrixSummaryFilePath, matrixInfoBuffer.toString(), true);
			}
			if (trees !=null){
				if (saveBasisTrees){
					trees.addToFile(tempDataFile, getProject(), null);
		 	 		tempDataFile.setPath(basePath + "BasisTrees.nex");
					 coord.writeFile(tempDataFile);
				}
				 trees.dispose();
			}
 			if (tempDataFile !=null) 
 				tempDataFile.close();
			if (progIndicator!=null)//&& outputBuffer != null) 
				progIndicator.setText("Saving batch files");//���
			template.composeAccessoryFilesEnd(baseName, directoryPath + MesquiteFile.fileSeparator);
				
			if (progIndicator!=null) 
				progIndicator.goAway();//���
			getProject().decrementProjectWindowSuppression();
			return null;
	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
    	 	if (checker.compare(this.getClass(), "Starts module to read instruction file to present results", null, commandName, "chartInstructionFile")) {
			return (MesquiteModule)hireNamedEmployee(MesquiteModule.class, "#mesquite.batchArch.ChartFromInstructions");
    	 	} //D! above
    	 	else if (checker.compare(this.getClass(), "Saves copies of a series of character matrices to files along with batch files for processing (e.g., parametric bootstrapping)", "[name of module to fill the matrices]", commandName, "exportMatrixBatch")) {
 			return exportMatricesAndBatchFiles(arguments,  true);
 		}
    	 	else if (checker.compare(this.getClass(), "Saves batch files for processing (e.g., parametric bootstrapping)", "[name of module to fill the matrices]", commandName, "exportBatch")) {
 			return exportMatricesAndBatchFiles(arguments,  false);
 		}
    	 	else if (checker.compare(this.getClass(), "Sets the base name", "[path]", commandName, "setBaseName")) {
			baseName =  parser.getFirstToken(arguments);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Fires character matrix module", null, commandName, "fireCharSource")) {
			fireEmployee(characterSourceTask);
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the output directory", "[path]", commandName, "setDir")) {
			directoryPath = MesquiteFile.composePath(getProject().getHomeDirectoryName(), parser.getFirstToken(arguments));
    	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the module supplying character matrices", "[name of module]", commandName, "setCharacterSource")) {
    	 		CharMatrixSource newCharacterSourceTask=  (CharMatrixSource)replaceEmployee(CharMatrixSource.class, arguments, "Source of characters for Export Matrices & Batch Files", characterSourceTask);
   	 		if (newCharacterSourceTask!=null) {
	   			characterSourceTask = newCharacterSourceTask;
 			}
 			return characterSourceTask;
    	 	}
    	 	else if (checker.compare(this.getClass(), "Saves matrices & batch files for processing (e.g., parametric bootstrapping); for use in scripting.  Assumes matrix source, output directory & base name already set", "[template][taxa reference][include matrices][number of replicates]", commandName, "export")) {
			//e.g. of arguments: "'Simple List'  'ex' 0 include 5";
			if (characterSourceTask ==null)
				return null;
			String templateName = parser.getFirstToken(arguments);
			String ct = templateName;
			if (templateManager == null)
				templateManager= (TemplateManager)getFileCoordinator().findEmployeeWithName("#BatchTemplateManager");
			if (templateManager == null)
				 return null;
			TemplateRecord template = templateManager.getTemplate(templateName);
   	 		Taxa taxa = getProject().getTaxaLastFirst( ct = parser.getNextToken());
			boolean includeMatrices = "include".equalsIgnoreCase(ct = parser.getNextToken());
			int num = MesquiteInteger.fromString(ct = parser.getNextToken());
 			return export(template, directoryPath, baseName, taxa, includeMatrices, characterSourceTask, num);
 		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
   	
   	private Object bailOut(MesquiteFile tempDataFile, MesquiteModule characterSourceTask, ProgressIndicator progIndicator){
		if (tempDataFile !=null)
			tempDataFile.close();
		if (progIndicator !=null)
			progIndicator.goAway();
		if (dialog != null) {
			dialog.dispose();
			dialog = null;
		}
		if (characterSourceTask!=null)
			fireEmployee(characterSourceTask);
		MesquiteThread.setSuppressAllProgressIndicatorsCurrentThread(false);
		resetAllMenuBars();
		decrementMenuResetSuppression();
		MainThread.decrementSuppressWaitWindow();
		return null;
   	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Export Matrices & Batch Files";
   	 }
   	 
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Saves copies of matrices to separate files for subsequent batch analysis (e.g., parametric bootstrapping)." ;
   	 }
   	 
	/*.................................................................................................................*/
   	 public boolean isPrerelease(){
   	 	return false; 
   	 }
	/*.................................................................................................................*/
   	 public boolean showCitation(){
   	 	return true;
   	 }
   	 
	/*.................................................................................................................*
   	Tree getTree(Taxa taxa){
		Tree tree = null;
		if (treeSourceTask == null)
			treeSourceTask= (TreeSource)hireNamedEmployee(TreeSource.class, "#StoredTrees");
		if (treeSourceTask!=null) {
			int treeNum = treeSourceTask.queryUserChoose(taxa, "Which tree?");
			if (MesquiteInteger.isCombinable(treeNum))
				tree = treeSourceTask.getTree(taxa, treeNum);
		}
		return tree;
   	}
   	/**/
}
	

