/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lists.DatasetsListProcess;

import mesquite.lists.lib.*;

import java.util.*;

import javax.swing.JLabel;

import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FilenameFilter;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;

/* ======================================================================== */
public class DatasetsListProcess extends DatasetsListUtility implements ActionListener {
	/*.................................................................................................................*/
	public String getName() {
		return "Process Matrices ";
	}

	public String getNameForMenuItem() {
		return "Process Matrices...";
	}

	public String getExplanation() {
		return "Processes selected matrices in List of Character Matrices window through a series of steps." ;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/

	/** if returns true, then requests to remain on even after operateOnTaxas is called.  Default is false*/
	public boolean pleaseLeaveMeOn(){
		return false;
	}
	String preferencesScript = null;
	String currentScript = null;
	boolean incorporateScript = false;
	Vector matrixProcessors = null;
	boolean cancelProcessing = false;
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("script".equalsIgnoreCase(tag))
			preferencesScript = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "script", preferencesScript);  
		return buffer.toString();
	}
	/*.................................................................................................................*/


	/*=============================================================================================================*/
	/* Primarily user interface methods
	 *    */
	/*.................................................................................................................*/
	List processorList = null;
	boolean fromSavedScript = false;

	void removeAllProcessors() {
		if (processorList != null)
			processorList.removeAll();
		if (matrixProcessors == null)
			return;
		for (int i= 0; i< matrixProcessors.size(); i++){
			DatasetsListProcessorUtility fProcessor = (DatasetsListProcessorUtility)matrixProcessors.elementAt(i);
			fireEmployee(fProcessor);
		}
		if (matrixProcessors != null)
			matrixProcessors.removeAllElements();

	}

	void resetProcessorList() {
		if (processorList != null)
			processorList.removeAll();
		for (int i = 0; i<matrixProcessors.size(); i++){
			if (matrixProcessors.elementAt(i)!=null)
				processorList.add("(" + (i+1) + ") " + ((DatasetsListProcessorUtility)matrixProcessors.elementAt(i)).getNameAndParameters());
		}

	}

	JLabel intro1, intro2;
	void setIntro(boolean fromSavedScript) {
		if (matrixProcessors.size()==0) {
			intro1.setText("How do you want to process each matrix?");
			intro2.setText("");
		}
		else {
			if (fromSavedScript) {
				intro1.setText("How do you want to process each matrix?");
				intro2.setText("The processing steps used previously are:");

			}
			else {
				intro1.setText("Do you want to add another step in processing each matrix?");
				intro2.setText("The processing steps already requested are:");
			}
		}
	}
	/*.................................................................................................................*/
	public boolean showProcessDialog() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Processing Matrices",buttonPressed);  

		intro1 = dialog.addLabel("xxxxxxxxxxxxxxxx  xxxxxxxxxxxxxxxx  xxxxxxxxxxxxxxx  xxxxxxxxx");
		intro2 = dialog.addLabel("                  ");
		setIntro(fromSavedScript);

		String[] steps = new String[matrixProcessors.size()];
		for (int i = 0; i<steps.length; i++){
			if (matrixProcessors.elementAt(i)!=null)
				steps[i] = "(" + (i+1) + ") " + ((DatasetsListProcessorUtility)matrixProcessors.elementAt(i)).getNameAndParameters();
		}
		processorList = dialog.addList (steps, null, null, 8);

		dialog.addBlankLine();
		Button loadButton = null;
		loadButton = dialog.addAListenedButton("Load Script", null, this);
		loadButton.setActionCommand("load");
		Button saveButton = null;
		saveButton = dialog.addAListenedButton("Save Script", null, this);
		saveButton.setActionCommand("save");
		Button clearButton = null;
		clearButton = dialog.addAListenedButton("Clear All", null, this);
		clearButton.setActionCommand("clear");
		Button addButton = null;
		addButton = dialog.addAListenedButton("Add Step", null, this);
		addButton.setActionCommand("add");
		dialog.addHorizontalLine(1);
		dialog.addBlankLine();

		Button resetParamButton = null;
		resetParamButton = dialog.addAListenedButton("Review Settings", null, this);
		resetParamButton.setActionCommand("resetParams");
		dialog.addHorizontalLine(1);
		dialog.completeAndShowDialog("Process", "Cancel", null, "PROCESS");

		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Add")) { //You have hit ADD, so let's add to current script. 
			//Look for and hire the next processor, and capture its script for later use
			boolean wasUTIS = MesquiteThread.unknownThreadIsScripting;
			MesquiteThread.unknownThreadIsScripting = false;
			DatasetsListProcessorUtility processor = (DatasetsListProcessorUtility)hireEmployee(DatasetsListProcessorUtility.class, "Matrix processor (" + (matrixProcessors.size() + 1)+ ")");
			MesquiteThread.unknownThreadIsScripting = wasUTIS;
			if (processor != null) {
				currentScript += "\naddProcessor " + " #" + processor.getClass().getName() + ";\n";
				String sn =Snapshot.getSnapshotCommands(processor, getProject().getHomeFile(), "  ");
				currentScript +="\ntell It;\n" + sn + "\nendTell;";
				recordProcessor(processor);
				fromSavedScript = false;
				setIntro(fromSavedScript);
				resetProcessorList();
			}
		}
		else if (e.getActionCommand().equalsIgnoreCase("Clear")) { //You have hit ADD, so remove all processors. 
			removeAllProcessors();
			fromSavedScript = false;
			setIntro(fromSavedScript);
			currentScript = "";
			preferencesScript = "";
		} 
		else if (e.getActionCommand().equalsIgnoreCase("Save")) {  //You have hit Load, choose and execute stored script
			String script = recaptureScript();
			MesquiteFile.putFileContentsQuery("Save processing steps as script file", script, false);
		} 
		else if (e.getActionCommand().equalsIgnoreCase("Load")) {  //You have hit Load, choose and execute stored script
			MesquiteFile f = MesquiteFile.open(true, (FilenameFilter)null, "Open text file with processing script", null);
			if (f!= null) {
				String script = MesquiteFile.getFileContentsAsString(f.getPath());
				if (!StringUtil.blank(script)) {
					removeAllProcessors();

					executeScript(script);
					currentScript = script;
					preferencesScript = script;
					resetProcessorList();
					fromSavedScript = true;
					setIntro(fromSavedScript);
				}
			}
		} 
		else if (e.getActionCommand().equalsIgnoreCase("resetParams")) {//Ask all processors to re-query regarding options
			for (int i= 0; i< matrixProcessors.size(); i++){
				DatasetsListProcessorUtility fProcessor = (DatasetsListProcessorUtility)matrixProcessors.elementAt(i);
				fProcessor.employeesQueryLocalOptions();
			}
			currentScript = recaptureScript();
			preferencesScript = currentScript;
			resetProcessorList();

		}
	}
	/*.................................................................................................................*/
	void recordProcessor(DatasetsListProcessorUtility processor){
		if (matrixProcessors == null)
			matrixProcessors = new Vector();
		if (matrixProcessors.indexOf(processor)<0)
			matrixProcessors.addElement(processor);
	}
	/*.................................................................................................................*/
	//execute script to load previous processors and their parameters
	void executeScript(String script) {
		Puppeteer p = new Puppeteer(this);
		CommandRecord mr = MesquiteThread.getCurrentCommandRecord();

		MesquiteThread.setCurrentCommandRecord(CommandRecord.macroRecord); //was macroRecord
		p.execute(this, script, new MesquiteInteger(0), null, false);
		MesquiteThread.setCurrentCommandRecord(mr);

		EmployeeVector employees = getEmployeeVector();
		for (int i= 0; i<employees.size(); i++) {
			MesquiteModule mb = (MesquiteModule)employees.elementAt(i);
			if (mb instanceof DatasetsListProcessorUtility)
				recordProcessor((DatasetsListProcessorUtility)mb);
		}

	}
	/*.....................................................................................................*/
	String recaptureScript() {
		if (matrixProcessors == null)
			return "";
		String s = "";
		for (int i = 0; i< matrixProcessors.size(); i++) {
			DatasetsListProcessorUtility processor = (DatasetsListProcessorUtility)matrixProcessors.elementAt(i);
			s += "\naddProcessor " + " #" + processor.getClass().getName() + ";\n";
			String sn =Snapshot.getSnapshotCommands(processor, getProject().getHomeFile(), "  ");
			s +="\ntell It;\n" + sn + "\nendTell;";
		}
		return s;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets the module processing matrices", "[name of module]", commandName, "addProcessor")) {
			DatasetsListProcessorUtility processor = (DatasetsListProcessorUtility)hireNamedEmployee(DatasetsListProcessorUtility.class, arguments);
			if (processor!=null) {
				recordProcessor(processor);
			}
			return processor;
		}
		else 
			super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*===================================*/
	/** Called to operate on the CharacterData blocks.  Returns true if taxa altered*/
	public boolean operateOnDatas(ListableVector datas, MesquiteTable table){
		if (datas.size() == 0)
			return false;
		incrementMenuResetSuppression();
		getProject().incrementProjectWindowSuppression();

		if (matrixProcessors == null)
			matrixProcessors = new Vector();
		if (preferencesScript != null){
			executeScript(preferencesScript); 			
			fromSavedScript = true;
		}	
		currentScript = preferencesScript;
		if (currentScript == null)
			currentScript = "";

		boolean processesChosen = showProcessDialog();
		if (processesChosen && matrixProcessors != null){
			//Process must have been hit. Capture the current script.
			preferencesScript = currentScript;
			storePreferences();
			Vector v = pauseAllPausables();
			MesquiteTimer timer = new MesquiteTimer();
			timer.start();
			boolean continuePlease = true;
			boolean[] warned = new boolean[matrixProcessors.size()];
			for (int i= 0; i< matrixProcessors.size(); i++)
				warned[i] = false;
			for (int i= 0; i< matrixProcessors.size() && continuePlease; i++){
				DatasetsListProcessorUtility mProcessor = (DatasetsListProcessorUtility)matrixProcessors.elementAt(i);
				if (mProcessor!=null) {
					logln("Processing with " + mProcessor.getNameAndParameters());
					boolean success = mProcessor.operateOnDatas(datas, table); //This could change how many matrices are in data, e.g. if it concatenates
					if (!success) { 
						logln("Sorry,  " + mProcessor.getNameAndParameters() + " did not succeed in processing the matrices ");
						if (!warned[i]) { 
							continuePlease = AlertDialog.query(containerOfModule(), "Processing step failed", "Processing of matrices by " + mProcessor.getNameAndParameters() + " failed. Do you want to continue?", "Continue", "Stop with This Matrix");
							warned[i] = true;
						}
					}

				}

			}
			timer.end();
			long time = timer.getAccumulatedTime();
			if (time>1000)
				logln("Matrix processing finished after " + MesquiteTimer.getHoursMinutesSecondsFromMilliseconds(time));
			unpauseAllPausables(v);
		}
		else {
			removeAllProcessors();
		}

		resetAllMenuBars();
		getProject().decrementProjectWindowSuppression();
		decrementMenuResetSuppression();	
		return processesChosen;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;  
	}
	public void endJob() {
		super.endJob();
	}

}

