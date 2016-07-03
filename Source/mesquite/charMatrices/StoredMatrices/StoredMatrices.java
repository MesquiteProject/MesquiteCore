/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.StoredMatrices;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/** Supplies character matrices stored in the project.*/
public class StoredMatrices extends CharMatrixSource implements MesquiteListener {
	Taxa taxa=null;
	CharacterData data;
	MCharactersDistribution states;
	Class dataClass = null;
	MesquiteString dataName;
	MesquiteSubmenuSpec mss;
	boolean iveQuit = false; //need special means to quit if multiple taxa blocks, because then harder to know on startup if matrices are available
	MesquiteBoolean autoQuit;
	/*.................................................................................................................*/
	/** condition passed to this module must be subclass of CharacterState */
	public boolean startJob(String arguments, Object condition, boolean hiredByName) { 
		autoQuit = new MesquiteBoolean(true);
		String sPurpose = whatIsMyPurpose();
		if (!StringUtil.blank(sPurpose))
			sPurpose = " (" + sPurpose + ")";
		else sPurpose = "";
		if (condition!=null) {
			dataName = new MesquiteString();
			if (condition instanceof Class)   //NOTE: class should be subclass of CharacterState, not CharacterData!!!
				dataClass = (Class)condition;
			else if (condition instanceof CompatibilityTest)
				dataClass = ((CompatibilityTest)condition).getAcceptedClass();
			if (getProject().getNumberCharMatricesVisible(dataClass)<=0 && !MesquiteThread.isScripting()) { //if scripting, matrix might be available later
				return sorry("There are no stored matrices of the requested type available");
			}
			if (getHiredAs() != (CharMatrixObedSource.class)) {	//not hired as a obedient source 
				mss = addSubmenu(null, "Stored Matrix" + sPurpose, makeCommand("setDataSet",  this), (ListableVector)getProject().datas);
				mss.setSelected(dataName);
				setDataClass(dataClass);
			}
			return true;
		}
		else {
			if (getProject().getNumberCharMatricesVisible()<=0 && !MesquiteThread.isScripting()) {
				return sorry("There are no stored matrices available");
			}
			dataName = new MesquiteString();
			if (getHiredAs() != (CharMatrixObedSource.class)) {	//not hired as a obedient source  
				mss = addSubmenu(null, "Stored Matrix" + sPurpose, makeCommand("setDataSet",  this), (ListableVector)getProject().datas);
				mss.setSelected(dataName);
			}
		}
		return true;
	}
	/*.................................................................................................................*/
	private void setDataClass(Class dataClass){
		boolean filter = false;
		CharacterState cs=null;
		try {
			cs = (CharacterState)dataClass.newInstance();

			filter = true;
		}
		catch (IllegalAccessException e){
			alert("iae 17m"); 
		}
		catch (InstantiationException e){
			alert("ie 17m"); 
		}
		if (filter && mss != null)
			mss.setListableFilter(cs.getCharacterDataClass());
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public CompatibilityTest getCompatibilityTest() {
		return new CSMCompatibilityTest();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (taxa!=null && getProject() != null && getProject().getNumberTaxas()>1)
			temp.addLine("setTaxa " + getProject().getTaxaReferenceExternal(taxa));
		if (data !=null && getProject() != null)
			temp.addLine("setDataSet " + getProject().getCharMatrixReferenceExternal(data));
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets which stored data matrix to use", "[matrix reference]", commandName, "setDataSet")) { 
			CharacterData d = getProject().getCharacterMatrixByReference(checker.getFile(), taxa, dataClass, parser.getFirstToken(arguments), true);  //31 jul '10 added true for visible only
			if (d == null)
				d = getProject().getCharacterMatrixByReference(checker.getFile(), taxa, dataClass, parser.getFirstToken(arguments));
			if (d==null && CommandRecord.macro()){ //macro; at this point ask for user to choose
				int which = queryUser(taxa);
				if (MesquiteInteger.isCombinable(which)) {
					d = getProject().getCharacterMatrixVisible(which);
					setMatrix(taxa, d);
					parametersChanged();
				}
				else
					return null;
			}
			else if (d !=null && d!=data) {
				if (taxa == null)
					taxa = d.getTaxa();
				setMatrix(taxa, d);
				parametersChanged();
				resetContainingMenuBar();
			}
		}
		else if (checker.compare(this.getClass(), "Sets which taxa block to use", "[block reference, number, or name]", commandName, "setTaxa")) { 
			Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
			if (t!=null){
				if (taxa!=null)
					taxa.removeListener(this);
				taxa = t;
				if (taxa!=null)
					taxa.addListener(this);
				parametersChanged();
				resetContainingMenuBar();
				return taxa;
			}
		}
		else if (checker.compare(this.getClass(), "Sets whether or not to auto-quit when no matrices are available", "[on or off]", commandName, "autoQuit")) { 
			autoQuit.toggleValue(parser.getFirstToken(arguments));
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public void disposing(Object obj){
		if (obj == data && !doomed) {
			data.removeListener(this);
			if (!autoQuit.getValue()) {
				data = null;
				return;
			}
			
			if (taxa !=null && taxa.isDoomed()) {
				taxa = null;
				if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Taxa block that is in use has been deleted"))  
					return;
				
				logln("Taxa null or being disposed; StoredMatrices will quit.");
				iQuit();
				return;
				
			}
			data = null;
			dataName.setReferentID(null);
			dataName.setValue("No matrix is currently in use");
			if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Character matrix that is in use has been deleted"))  
				return;
			
			MesquiteModule.showLogWindow(true);

			MesquiteMessage.warnUser("A character data matrix in use (" + whatIsMyPurpose() + ") has been deleted.  Another matrix will be sought.");  
			if (dataClass!=null) {
				if (getProject().getNumberCharMatricesVisible(taxa, dataClass)<=0) {
					MesquiteModule.showLogWindow(true);
					MesquiteMessage.warnUser("No compatible character matrices were found " + whatIsMyPurpose() + ", and so Stored Matrices cannot be used.");
					iQuit(false);
					return;
				}
				data = getProject().getCharacterMatrixVisible(taxa, 0, dataClass);
			}
			else if (getProject().getNumberCharMatricesVisible(taxa)>0)
				data = getProject().getCharacterMatrixVisible(taxa, 0);
			else data = null;
			if (data==null){
				MesquiteModule.showLogWindow(true);
				MesquiteMessage.warnUser("No character matrices found " + whatIsMyPurpose() + ", and so Stored Matrices cannot be used.");
				iQuit(false);
				return;
			}
			data.addListener(this);
			dataName.setReferentID(Long.toString(data.getID()));
			dataName.setValue(data.getName());
			
			parametersChanged();
		}
	}
	/*.................................................................................................................*/
	/** passes which object is being disposed (from MesquiteListener interface)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;  //Disposal prohibition not used in Mesquite yet; once used then this should return false if currently in use
	}
	/*.................................................................................................................*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		if (obj==data && (code == AssociableWithSpecs.SPECSSET_CHANGED || code == MesquiteListener.VALUE_CHANGED ||code == MesquiteListener.DATA_CHANGED || code == MesquiteListener.PARTS_CHANGED || code == MesquiteListener.PARTS_ADDED || code == MesquiteListener.PARTS_DELETED || code == MesquiteListener.PARTS_MOVED)){
			parametersChanged(notification);
		}
	}
	/*.................................................................................................................*/
	public  int getNumberOfMatrices(Taxa taxa){
		if (getProject()==null)
			return 0;
		else if (dataClass == null)
			return getProject().getNumberCharMatricesVisible(taxa); 
		else
			return getProject().getNumberCharMatricesVisible(taxa, dataClass); 
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public  void setMatrix(Taxa taxa, CharacterData d){
		checkCurrentDataSet(taxa, d,  false, true);
		this.taxa = taxa;
	}

	private int queryUser(Taxa taxa){
		int nd = getProject().getNumberCharMatricesVisible(taxa, dataClass);
		String[] list = new String[nd];
		for (int i=0; i< nd; i++)
			list[i]=getProject().getCharacterMatrixVisible(taxa, i, dataClass).getName();
		String purposeString = "";
		if (!StringUtil.blank(whatIsMyPurpose()))
			purposeString = whatIsMyPurpose() + "; ";
		return ListDialog.queryList(containerOfModule(), "Use which matrix?", "Use which matrix? \n(" + purposeString + "for " + employer.getName() + ")", MesquiteString.helpString,list, 0);
	}

	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
	public void initialize(Taxa taxa){
		checkCurrentDataSet(taxa, null, false, getHiredAs() != (CharMatrixObedSource.class));
	}

	private void setTaxa(Taxa taxa){
		if (this.taxa != taxa && mss != null) {
			mss.setCompatibilityCheck(taxa);
			resetContainingMenuBar();
		}
		this.taxa = taxa;
	}
	boolean warned = false;
	private void checkCurrentDataSet(Taxa taxa, CharacterData proposed,  boolean warn, boolean queryIfNeeded) {
		if (iveQuit)
			return;
		setTaxa(taxa);
		if (proposed != null && proposed.getTaxa() == taxa){ //new matrix being proposed
			if (proposed == data) 
				return;
			dataName.setReferentID(Long.toString(proposed.getID()));
			dataName.setValue(proposed.getName());
			if (data !=null)
				data.removeListener(this);
			proposed.addListener(this);

			data = proposed;
		}
		if (getProject() == null)
			return;
		if (data == null) { // data == null && proposed == null; need to choose one
			if (MesquiteThread.isScripting() || getHiredAs() == CharMatrixObedSource.class) 
				data = getProject().getCharacterMatrixVisible(taxa, 0, dataClass);
			else if (getProject().getNumberCharMatricesVisible(taxa, dataClass)<=1)
				data = getProject().getCharacterMatrixVisible(taxa, 0, dataClass);
			else {
				int currentDataSet = 0;
				if (queryIfNeeded){
					currentDataSet = queryUser(taxa); //ListDialog.queryList(containerOfModule(), "Use which matrix?", "Use which matrix? \n(" + purposeString + "for " + employer.getName() + ")", list, 0);
					if (!MesquiteInteger.isCombinable(currentDataSet))
						currentDataSet = 0;
				}
				data = getProject().getCharacterMatrixVisible(taxa, currentDataSet, dataClass);
			}
			if (data !=null) {
				if (taxa == null)
					setTaxa(data.getTaxa());
				data.addListener(this);
			}
			else {
				if (!MesquiteThread.isScripting()){ //if scripting, hope that will soon be fired!
					if (taxa == null)
						alert("There are no appropriate stored character matrices available.");
					else 
						alert("There are no appropriate stored character matrices available for the block of taxa (" + taxa.getName() + ").");
					iveQuit = true;
					iQuit(false);
				}
			}

		}
	}
	/*.................................................................................................................*/
	private MCharactersDistribution getM(Taxa taxa) {
		setTaxa(taxa);
		checkCurrentDataSet(taxa, null,  true, true); //just to insure that OK
		if (data!=null) {
			states = data.getMCharactersDistribution();
			return states;
		}
		return null;
	}
	/*.................................................................................................................*/
	public String getMatrixName(Taxa taxa, int ic) {
		CharacterData data;
		if (!MesquiteInteger.isCombinable(ic))
			return "";
		if (dataClass == null)
			data = getProject().getCharacterMatrixVisible(taxa, ic);
		else
			data =  getProject().getCharacterMatrixVisible(taxa, ic, dataClass);
		if (data !=null)
			return data.getName();
		return "";
	}
	/*.................................................................................................................*/
	public  MCharactersDistribution getMatrix(Taxa taxa, int im){
		try {
			checkCurrentDataSet(taxa, getProject().getCharacterMatrixVisible(taxa, im, dataClass),  false, true);
			CommandRecord.tick("Getting stored matrix " + im);
			return getM(taxa);
		}
		catch(NullPointerException e){
		}
		return null;
	}
	/*.................................................................................................................*/
	/** gets the current matrix.*/
	public MCharactersDistribution getCurrentMatrix(Taxa taxa){
		if (taxa!=null && !taxa.equals(this.taxa, false)){//taxa==null || 
			if (data!=null)
				data.removeListener(this);
			data = null;
		}
		checkCurrentDataSet(taxa, null,  false, true);
		return getM(taxa);
	}
	/** returns the number of the current matrix*/
	public int getNumberCurrentMatrix(){
		return getProject().getMatrixNumber(data);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Stored Matrices";  
	}

	/*.................................................................................................................*/
	public boolean showCitation() {
		return true;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies character matrices from data files (as opposed to simulated characters, for example)." ;
	}
	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getParameters() {
		if (data!=null && getHiredAs() == CharMatrixOneSource.class)
			return "Current Matrix: " + data.getName();
		else if (getProject() != null)
			return "Character Matrices from file: " + getProject().getName();
		else
			return null;
	}

	/*.................................................................................................................*/
	/** returns current parameters, for logging etc..*/
	public String getNameAndParameters() {
		if (data!=null && getHiredAs() == CharMatrixOneSource.class)
			return "Stored Matrix: " + data.getName();
		else if (getProject() != null)
			return "Character Matrices from file: " + getProject().getName();
		else
			return null;
	}
	public void endJob() {
		if (data!=null) data.removeListener(this);
		super.endJob();
	}

}

class CSMCompatibilityTest extends CompatibilityTest{
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		return isCompatible(obj, project, prospectiveEmployer, null);
	}
	public  boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
		if (obj == null) {
			boolean can = (project == null || project.getNumberCharMatricesVisible()>0);
			if (!can && report != null)
				report.setValue("there are no character matrices stored in the data file or project");
			return can;
		}
		if (!(obj instanceof Class) || !(CharacterState.class.isAssignableFrom((Class)obj)))
			return true;
		if (project==null)
			return true;
		else {
			boolean matricesExist = ( project.getNumberCharMatricesVisible((Class)obj)>0); //still not perfect, since data set might apply to other taxa
			if (!matricesExist && report != null)
				report.setValue("there are no appropriate character matrices stored in the data file or project");
			return matricesExist;
		}
	}
}

