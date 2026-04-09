/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ManageMatrixPartitions;
/*~~  */

import java.awt.Color;

import mesquite.lib.AssociableWithSpecs;
import mesquite.lib.CommandChecker;
import mesquite.lib.FileElement;
import mesquite.lib.Listable;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteDouble;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.MesquiteString;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.NexusBlock;
import mesquite.lib.NexusCommandTest;
import mesquite.lib.Notification;
import mesquite.lib.ObjectArray;
import mesquite.lib.ObjectSpecsSet;
import mesquite.lib.ParseUtil;
import mesquite.lib.Parser;
import mesquite.lib.Snapshot;
import mesquite.lib.SpecsSet;
import mesquite.lib.SpecsSetVector;
import mesquite.lib.StringUtil;
import mesquite.lib.characters.MatricesGroup;
import mesquite.lib.characters.MatricesGroupVector;
import mesquite.lib.characters.MatrixPartition;
import mesquite.lib.duties.ManagerAssistant;
import mesquite.lib.duties.MatrixSpecsSetManager;
import mesquite.lib.duties.NexusFileInterpreter;
import mesquite.lib.duties.TaxaManager;
import mesquite.lib.taxa.Taxa;
import mesquite.lib.taxa.TaxaGroup;
import mesquite.lib.taxa.TaxaGroupVector;
import mesquite.lib.taxa.TaxaPartition;
import mesquite.lists.lib.GroupDialog;

/** Manages specifications of character partitions, including reading and writing from NEXUS files */
public class ManageMatrixPartitions extends MatrixSpecsSetManager {
	//"@MATRIXGROUP

	final static String listOfMatrixGroupsName = "List of Matrix Group Labels";


	/*	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.MatrixPartitionList.MatrixPartitionList.class, getName() + "  uses an assistant to display a list window.",
				"The assistant is arranged automatically");
	}
	 */
	MatricesGroupVector groups; 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = new MatricesGroupVector();
		getProject().addFileElement(groups);
		return true;
	}
	public void elementsReordered(ListableVector v){
	}
	public NexusBlock elementAdded(FileElement e){
		if (e instanceof MatricesGroup){
			if (groups.indexOf(e)<0) {
				groups.addElement(e, true);
				e.addListener(groups);
			}
			e.setManager(this);
			return null;
		}
		else
			return super.elementAdded(e);
	}
	public MesquiteModule showElement(FileElement e){
		if (e instanceof MatricesGroup){
			MatricesGroup group = (MatricesGroup)e;
			GroupDialog d = new GroupDialog(getProject(),getProject().getCoordinatorModule().containerOfModule(), "Edit Matrix Group", group.getName(), group.getColor(), group.getSymbol(), group.supportsSymbols());
			d.completeAndShowDialog();
			String name = d.getName();
			boolean ok = d.query()==0;
			Color c = d.getColor();
			d.dispose();
			if (!ok)
				return null;


			if (!StringUtil.blank(name)) {
				group.setName(name);
			}
			group.setColor(c);
			notifyOfGroupChange(group);
			return null;
		}
		//TODO:
		if (e != null)
			alert("Sorry, the " + e.getTypeName() + "  cannot be shown by this means yet.");
		return null;
	}
	public void notifyOfGroupChange(MatricesGroup e){
		if (e instanceof MatricesGroup){
			e.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			ListableVector datas = getProject().getCharacterMatrices();
			SpecsSetVector ssv = datas.getSpecSetsVector(MatrixPartition.class);
			MatrixPartition cp = (MatrixPartition)ssv.getCurrentSpecsSet();
			boolean done = false;
			if (cp != null)
				for (int ic = 0; ic< datas.size() && ! done; ic++){
					if (cp.getMatricesGroup(ic) == e) {
						datas.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
						done = true;
					}
				}
			for (int is = 0; is< ssv.size() && !done; is++){
				cp = (MatrixPartition)ssv.elementAt(is);
				for (int ic = 0; ic< datas.size() && !done; ic++){
					if (cp.getMatricesGroup(ic) == e) {
						datas.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
						done = true;
					}
				}
			}
		}
	}

	public void deleteElement(FileElement e){
		if (e instanceof MatricesGroup){
			ListableVector datas = getProject().getCharacterMatrices();
			boolean changed = false;
			SpecsSetVector ssv = datas.getSpecSetsVector(MatrixPartition.class);
			MatrixPartition cp = (MatrixPartition)ssv.getCurrentSpecsSet();
			if (cp != null)
				for (int ic = 0; ic< datas.size(); ic++){
					if (cp.getMatricesGroup(ic) == e) {
						cp.setProperty(cp.getDefaultProperty(ic), ic);
						changed = true;
					}
				}
			for (int is = 0; is< ssv.size(); is++){
				cp = (MatrixPartition)ssv.elementAt(is);
				for (int ic = 0; ic< datas.size(); ic++){
					if (cp.getMatricesGroup(ic) == e) {
						cp.setProperty(cp.getDefaultProperty(ic), ic);
						changed = true;
					}
				}
			}
			if (changed)
				datas.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));

			getProject().removeFileElement(e);//must remove first, before disposing
			groups.removeElement(e, true);
			e.dispose();
		}
	}
	public void elementDisposed(FileElement e){
		if (groups!=null)
			groups.removeElement(e, true);
	}
	public void projectEstablished(){
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, listOfMatrixGroupsName, makeCommand("showMatrixGroups",  this));
		//		MesquiteSubmenuSpec mmis2 = getFileCoordinator().addSubmenu(MesquiteTrunk.charactersMenu,"List of Character Groups", makeCommand("showCharacterGroups",  this),  (ListableVector)getProject().taxas);
		//		mmis2.setOwnerModuleID(getID());
		//		mmis2.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		groups.addToFile(getProject().getHomeFile(), getProject(), this);
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals(listOfMatrixGroupsName)) {
				temp.addLine("showMatrixGroups ", e); 
			}
		}
		return temp;
	}
	ManagerAssistant lister = null;
	/*.................................................................................................................*/
	public ManagerAssistant showMatrixGroupList(Object obj, String listerName){

		if (lister == null)
			lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize(listerName));
		if (lister!=null) {
			lister.showListWindow(obj);
			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
				lister.getModuleWindow().setVisible(true);
		}
		return lister;

	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows list of the matrix groups", null, commandName, "showMatrixGroups")) {
			return showMatrixGroupList(null, listOfMatrixGroupsName);
		}
		else if (checker.compare(this.getClass(), "Exports group labels/colors to a NEXUS file for later import.", "[]", commandName, "exportLabels")) {
			MatricesGroupVector groups = (MatricesGroupVector)getProject().getFileElement(MatricesGroupVector.class, 0);
			if (groups == null)
				return null;
			String s = "#NEXUS\nBEGIN LABELS;\n\n";
			for (int ig = 0; ig<groups.size(); ig++){
				MatricesGroup group = (MatricesGroup)groups.elementAt(ig);
				s += getGroupLabelNexusCommand(group) + "\n";
			}
			s += "END;";
			if (!StringUtil.blank(s)){
				MesquiteFile.putFileContentsQuery("Exported NEXUS file of group labels/colors, for later import into other files", s, true);
			}
		}
		else if (checker.compare(this.getClass(), "Imports group labels from a NEXUS file.", null, commandName, "importLabels")) {
			MesquiteProject proj = getProject();
			MatricesGroupVector groupsVector = (MatricesGroupVector)proj.getFileElement(MatricesGroupVector.class, 0);
			Listable[] oldGroups = groupsVector.getElementArray();
			MesquiteString directoryName = new MesquiteString();
			MesquiteString fileName = new MesquiteString();
			MesquiteFile.openFileDialog("Please select a NEXUS file that has character group labels to import.", directoryName, fileName);
			if (!fileName.isBlank()){
				MesquiteFile fileToRead = new MesquiteFile(directoryName.getValue(), fileName.getValue());
				proj.addFile(fileToRead);
				fileToRead.setProject(proj);
				fileToRead.setReadCategory(MesquiteFile.INCLUDED);
				NexusFileInterpreter mb = (NexusFileInterpreter)findNearestColleagueWithDuty(NexusFileInterpreter.class);
				proj.setNotificationsOnOff(false);
				mb.readFile(getProject(), fileToRead, " @noWarnMissingReferent  @noWarnUnrecognized @justTheseBlocks.LABELS  @justTheseCommands.MATRIXGROUPLABEL"); 

				Listable[] combinedGroups = groupsVector.getElementArray();
				for (int i = 0; i<combinedGroups.length; i++){
					MatricesGroup group = (MatricesGroup)combinedGroups[i];
					if (ObjectArray.indexOf(oldGroups, group)<0){//a new object, though may have same name as old
						int whichCurrentByName = ListableVector.indexOfByName(oldGroups, group.getName());
						if (whichCurrentByName>=0){
							MatricesGroup oldGroup = (MatricesGroup)oldGroups[whichCurrentByName];
							oldGroup.equalizeAs(group);
						}
						else { //just move it over
							MatricesGroup newGroup = new MatricesGroup();
							newGroup.equalizeAs(group);
							newGroup.addToFile(getProject().getHomeFile(), proj, null);
						}
					}
				}
				
				//***************
				proj.getCoordinatorModule().closeFile(fileToRead, true);
				proj.setNotificationsOnOff(true);
				groupsVector.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));
			}
		}
		else if (checker.compare(this.getClass(), "Imports groups and their labels from a NEXUS file.", "[]", commandName, "importPartitions")) {
			MesquiteProject proj = getProject();
			MatricesGroupVector groupsVector = (MatricesGroupVector)proj.getFileElement(MatricesGroupVector.class, 0);
			Listable[] previousGroups = groupsVector.getElementArray();
			ListableVector datas = getProject().getCharacterMatrices();
			if (datas != null){
				MesquiteString directoryName = new MesquiteString();
				MesquiteString fileName = new MesquiteString();
				MesquiteFile.openFileDialog("Please select the NEXUS file with a partition (groups) of matrices.", directoryName, fileName);
				if (!fileName.isBlank()){
					MesquiteFile fileToRead = new MesquiteFile(directoryName.getValue(), fileName.getValue());
					proj.addFile(fileToRead);
					fileToRead.setProject(proj);
					fileToRead.setReadCategory(MesquiteFile.INCLUDED);
					NexusFileInterpreter mb = (NexusFileInterpreter)findNearestColleagueWithDuty(NexusFileInterpreter.class);
					proj.setNotificationsOnOff(false);
					mb.readFile(getProject(), fileToRead, " @noWarnUnrecognized @justTheseBlocks.SETS.LABELS  @justTheseCommands.MATRIXGROUPLABEL.MATRIXPARTITION");
					
					//***************
					MatrixPartition partition = (MatrixPartition)datas.getCurrentSpecsSet(MatrixPartition.class);
					for (int im = 0; im<datas.size(); im++){
						MatricesGroup group = (MatricesGroup)partition.getProperty(im);
						MatricesGroup useGroup;
						if (group.getFile() == fileToRead){
							int previousI = ListableVector.indexOf(previousGroups, group.getName());
							if (previousI>=0){
								useGroup = (MatricesGroup)previousGroups[previousI];
								useGroup.equalizeAs(group);
							}
							else {
								useGroup = new MatricesGroup();
								useGroup.equalizeAs(group);
								useGroup.addToFile(getProject().getHomeFile(), proj, null);
						}
							for (int imK= im; imK<datas.size(); imK++){
								MatricesGroup currgroup = (MatricesGroup)partition.getProperty(imK);
								if (currgroup.getName().equals(useGroup.getName()))
									partition.setProperty(useGroup, imK);
							}
						}
					}
					partition.addToFile(proj.getHomeFile(), proj, null);
					
					proj.getCoordinatorModule().closeFile(fileToRead, true);
				proj.setNotificationsOnOff(true);

				}
				datas.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
				groupsVector.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));
			}
		}
		else if (checker.compare(this.getClass(), "Exports current groups and group labels/colors to a NEXUS file for later import.", "[]", commandName, "exportPartitionAndLabels")) {
			ListableVector datas = getProject().getCharacterMatrices();
			MatrixPartition partition = (MatrixPartition)datas.getCurrentSpecsSet(MatrixPartition.class);
			if (partition == null){
				discreetAlert("Sorry, there isn't a current partition to export");
				return null;
			}
			MatricesGroupVector groups = (MatricesGroupVector)getProject().getFileElement(MatricesGroupVector.class, 0);

			String s = "#NEXUS\n";

			if (groups != null){
				s+= "BEGIN LABELS;\n\n";
				for (int ig = 0; ig<groups.size(); ig++){
					MatricesGroup group = (MatricesGroup)groups.elementAt(ig);
					s += getGroupLabelNexusCommand(group) + "\n";
				}
				s += "END;";
			}
			s += "\nBEGIN SETS;\n";
			s += nexusStringForSpecsSet(partition, checker.getFile(), true);
			s += "\nEND;";
			if (!StringUtil.blank(s)){
				MesquiteFile.putFileContentsQuery("Exported NEXUS file of current partition and group labels/colors, for later import into other files", s, true);
			}
		}		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	

	public Class getElementClass(){
		return MatrixPartition.class;
	}
	public String upperCaseTypeName(){
		return "Matrix Partition";
	}
	public String lowerCaseTypeName(){
		return "matrix partition";
	}
	public String nexusToken(){
		return "MATRIXPARTITION";
	}

	private MatricesGroup makeGroup(String name, Parser subcommands, MesquiteFile file){
		MatricesGroup group = groups.findGroup(name);
		if (group==null) {
			group = new MatricesGroup();
			group.setName(name);
			group.addToFile(file, getProject(), this);
			if (groups.indexOf(group)<0) 
				groups.addElement(group, false);
		}
		if (subcommands !=null){ //this should be passed into group to handle?
			String token = null;
			while ((token = subcommands.getNextToken())!=null){
				if (token.equalsIgnoreCase("COLOR")){
					token = subcommands.getNextToken(); //=
					token = subcommands.getNextToken(); // (
					token = subcommands.getNextToken(); // (
					if (token!=null && token.equalsIgnoreCase("RGB")) {
						double red = MesquiteDouble.fromString(subcommands.getNextToken()); //Red
						double green = MesquiteDouble.fromString(subcommands.getNextToken()); //green
						double blue = MesquiteDouble.fromString(subcommands.getNextToken()); //blue
						if (MesquiteDouble.isCombinable(red) && MesquiteDouble.isCombinable(green) && MesquiteDouble.isCombinable(blue)){
							Color c = new Color((float)red, (float)green, (float)blue);
							group.setColor(c);
						}
					}
				}
			}
		}
		return group;
	}
	public Object getSpecification(String token){ //NEED TO PASS FILE
		return makeGroup(token, null, getProject().getHomeFile());
	}
	public void setSpecification(SpecsSet specsSet, Object specification, int ic){
		if (specsSet==null || !(specsSet instanceof MatrixPartition) || !(specification instanceof MatricesGroup))
			return;
		MatrixPartition MatrixPartition = (MatrixPartition)specsSet;
		MatrixPartition.setProperty(specification,ic);
	}
	public SpecsSet getNewSpecsSet(String name){
		return new MatrixPartition(name, null, getProject().getCharacterMatrices());
	}
	public boolean appropriateBlockForWriting(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("SETS");
	}
	public boolean appropriateBlockForReading(String blockName){
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS");
	}

	/*.................................................................................................................*/
	public String nexusStringForSpecsSet(ObjectSpecsSet specsSet, MesquiteFile file, boolean isCurrent){
		if (specsSet ==null || !(specsSet instanceof MatrixPartition))
			return null;
		MatrixPartition matrixPartition = (MatrixPartition)specsSet;
		String s= "";
		if (matrixPartition !=null && (matrixPartition.getFile()==file || (matrixPartition.getFile()==null))) {
			String sT = " ";
			MatricesGroup[] parts = matrixPartition.getGroups();
			boolean firstTime = true;

			if (parts!=null)
				for (int i=0; i<parts.length; i++) {
					String q = ListableVector.getListOfMatches((Listable[])matrixPartition.getProperties(), parts[i], 1);
					if (q != null) {
						if (!firstTime)
							sT += ", ";
						firstTime = false;
						sT += StringUtil.tokenize(parts[i].getName()) + " : " + q;
					}
				}

			if (!StringUtil.blank(sT)) {
				s+= "\tMATRIXPARTITION " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(matrixPartition.getName()) + " ";
				s+= " = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment, String fileReadingArguments){ 
		if (blockName.equalsIgnoreCase("LABELS")) {
			String commandName = parser.getFirstToken(command);
			if ("MATRIXGROUPLABEL".equalsIgnoreCase(commandName)) {
				String name = parser.getNextToken();
				makeGroup(name, parser, file); //pass whole command
				return true;
			}
			return false;
		}
		else 
			return super.readNexusCommand(file, nBlock, blockName, command, comment,  fileReadingArguments);
	}

	public String getGroupLabelNexusCommand(MatricesGroup cg){
		String s = "";
		s += "\tMATRIXGROUPLABEL " + ParseUtil.tokenize(cg.getName());
		if (cg.colorSet()){
			Color c = cg.getColor();
			s += " COLOR = (RGB " + MesquiteDouble.toString(c.getRed()/255.0) + " " + MesquiteDouble.toString(c.getGreen()/255.0) + " " + MesquiteDouble.toString(c.getBlue()/255.0) + ") ";
		}
		s += ";" + StringUtil.lineEnding();
		return s;
	}

	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("LABELS")) {
			String s = "";
			for (int i = 0; i< groups.size(); i++){
				MatricesGroup cg = (MatricesGroup)groups.elementAt(i);
				if (cg.getFile() == file){
					s += getGroupLabelNexusCommand(cg);
				}
			}
			if (StringUtil.blank(s))
				return null;
			else
				return s;
		}
		else  {
			return super.getNexusCommands(file, blockName);
		}
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new PartitionNexusCommandTest();
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage matrix partititions";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages (including NEXUS read/write) matrix partitions." ;
	}

}

/* ======================================================================== */
class PartitionNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		if ((blockName.equalsIgnoreCase("SETS")) && commandName.equalsIgnoreCase("MATRIXPARTITION"))
			return true;

		if ((blockName.equalsIgnoreCase("LABELS")) && commandName.equalsIgnoreCase("MATRIXGROUPLABEL"))
			return true;

		return false;
	}
}


