/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.6, January 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.consensus.lib;


import java.awt.Button;
import java.awt.Choice;
import java.awt.Label;

import mesquite.lib.duties.*;
import mesquite.lib.*;


public abstract class BasicTreeConsenser extends IncrementalConsenser   {
	protected static final int ASIS=0;
	protected static final int ROOTED =1;
	protected static final int UNROOTED=2;
	protected int rooting = ASIS;   // controls and preferences and snapshot should be in subclass

	protected BipartitionVector bipartitions=null;
	protected int treeNumber = 0;
	boolean preferencesSet = false;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		bipartitions = new BipartitionVector();
		loadPreferences();
		if (!MesquiteThread.isScripting()) 
			if (!queryOptions())
				return false;
		return true;
	}

	/*.................................................................................................................*/
	public void processMorePreferences (String tag, String content) {
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("rooting".equalsIgnoreCase(tag))
			rooting = MesquiteInteger.fromString(content);
		processMorePreferences(tag, content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String prepareMorePreferencesForXML () {
		return "";
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "rooting", rooting);  
		buffer.append(prepareMorePreferencesForXML());
		preferencesSet = true;
		return buffer.toString();
	}
	
	public void queryOptionsSetup(ExtensibleDialog dialog) {
	}
	/*.................................................................................................................*/
	public void queryOptionsProcess(ExtensibleDialog dialog) {
	}

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), getName() + " Options",buttonPressed);  //MesquiteTrunk.mesquiteTrunk.containerOfModule()
		dialog.addLabel(getName() + " Options");
		String helpString = "Please choose the options for consensus trees. ";

		dialog.appendToHelpString(helpString);

		queryOptionsSetup(dialog);

		
		String[] rootingStrings = {"As specified in first tree", "Rooted", "Unrooted"};
		Choice rootingChoice  = dialog.addPopUpMenu("Treat trees as rooted or unrooted:", rootingStrings, 0);


		//TextArea PAUPOptionsField = queryFilesDialog.addTextArea(PAUPOptions, 20);

		dialog.completeAndShowDialog(true);
		if (buttonPressed.getValue()==0)  {
			queryOptionsProcess(dialog);
			int choiceValue = rootingChoice.getSelectedIndex();
			if (choiceValue>=0)
				rooting = choiceValue;
			storePreferences();

		}
		dialog.dispose();
		return (buttonPressed.getValue()==0);
	}

	/*.................................................................................................................*/

	/*.................................................................................................................*/
  	public void reset(Taxa taxa){
  		if (bipartitions==null)
  			bipartitions = new BipartitionVector();
  		else
  			bipartitions.removeAllElements();		// clean bipartition table
		bipartitions.setTaxa(taxa);
		bipartitions.zeroFrequencies();
		initialize();
	}
  	public abstract void addTree(Tree t);
  	
 	public abstract Tree getConsensus();
	/*.................................................................................................................*/
 	public void initialize() {
 	}
	/*.................................................................................................................*/
 	public void afterConsensus() {
 	}

	/*.................................................................................................................*/
	//ASSUMES TREES HAVE ALL THE SAME TAXA
	/*.................................................................................................................*/
	public Tree consense(Trees list){
		Taxa taxa = list.getTaxa();
		
		reset(taxa);
		ProgressIndicator progIndicator=null;
		//progIndicator = new ProgressIndicator(getProject(),getName(), "", list.size(), true);
		if (progIndicator!=null){
			progIndicator.start();
		}
	//	if (MesquiteTrunk.debugMode)
	//		logln("\n Consensus Tree Calculations");
		MesquiteTimer timer = new MesquiteTimer();
		timer.start();
		logln("");
		for (treeNumber = 0; treeNumber < list.size(); treeNumber++){
			if (treeNumber==0) {
				switch (rooting) {
				case ASIS: 
					bipartitions.setRooted(list.getTree(0).getRooted());
					break;
				case ROOTED: 
					bipartitions.setRooted(true);
					break;
				case UNROOTED: 
					bipartitions.setRooted(false);
					break;
				}
			}
			addTree(list.getTree(treeNumber));
			if (treeNumber%100==0)
				log(".");
			if (progIndicator!=null) {
				progIndicator.setText("Processing tree " + (treeNumber+1));
				progIndicator.spin();		
				if (progIndicator.isAborted())
					break;
			}
	//		if (MesquiteTrunk.debugMode)
	//			logln(" tree: " + (treeNumber+1)+ ", bipartitions: " + bipartitions.size() + ", memory: " + MesquiteTrunk.getMaxAvailableMemory());
		}
		Tree t = getConsensus();
		double time = 1.0*timer.timeSinceLast()/1000.0;
		if (progIndicator!=null)
			progIndicator.goAway();
	//	afterConsensus();
		logln("\n" + list.size() + " trees processed in " + time + " seconds");
		return t;
	}

}

