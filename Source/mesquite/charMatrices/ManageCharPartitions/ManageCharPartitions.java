/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.charMatrices.ManageCharPartitions;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lists.lib.GroupDialog;

/** Manages specifications of character partitions, including reading and writing from NEXUS files */
public class ManageCharPartitions extends CharSpecsSetManager {
	final static String listOfCharacterGroupsName = "List of Character Group Labels";


	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.CharPartitionList.CharPartitionList.class, getName() + "  uses an assistant to display a list window.",
				"The assistant is arranged automatically");
	}
	CharactersGroupVector groups; 
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = new CharactersGroupVector();
		getProject().addFileElement(groups);
		return true;
	}
	public void elementsReordered(ListableVector v){
	}
	public NexusBlock elementAdded(FileElement e){
		if (e instanceof CharactersGroup){
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
		if (e instanceof CharactersGroup){
			CharactersGroup group = (CharactersGroup)e;
			GroupDialog d = new GroupDialog(getProject(),getProject().getCoordinatorModule().containerOfModule(), "Edit Character Group", group.getName(), group.getColor(), group.getSymbol(), group.supportsSymbols());
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
	public void notifyOfGroupChange(CharactersGroup e){
		if (e instanceof CharactersGroup){
			e.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			ListableVector d = getProject().getCharacterMatrices();
			for (int im = 0; im<d.size(); im++){
				CharacterData data = (CharacterData)d.elementAt(im);
				SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);
				CharacterPartition cp = (CharacterPartition)ssv.getCurrentSpecsSet();
				boolean done = false;
				if (cp != null)
					for (int ic = 0; ic< data.getNumChars() && ! done; ic++){
						if (cp.getCharactersGroup(ic) == e) {
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
							done = true;
						}
					}
				for (int is = 0; is< ssv.size() && !done; is++){
					cp = (CharacterPartition)ssv.elementAt(is);
					for (int ic = 0; ic< data.getNumChars() && !done; ic++){
						if (cp.getCharactersGroup(ic) == e) {
							data.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
							done = true;
						}
					}
				}
			}
		}
	}

	public void deleteElement(FileElement e){
		if (e instanceof CharactersGroup){
			ListableVector d = getProject().getCharacterMatrices();
			for (int im = 0; im<d.size(); im++){
				CharacterData data = (CharacterData)d.elementAt(im);
				boolean changed = false;
				SpecsSetVector ssv = data.getSpecSetsVector(CharacterPartition.class);
				CharacterPartition cp = (CharacterPartition)ssv.getCurrentSpecsSet();
				if (cp != null)
					for (int ic = 0; ic< data.getNumChars(); ic++){
						if (cp.getCharactersGroup(ic) == e) {
							cp.setProperty(cp.getDefaultProperty(ic), ic);
							changed = true;
						}
					}
				for (int is = 0; is< ssv.size(); is++){
					cp = (CharacterPartition)ssv.elementAt(is);
					for (int ic = 0; ic< data.getNumChars(); ic++){
						if (cp.getCharactersGroup(ic) == e) {
							cp.setProperty(cp.getDefaultProperty(ic), ic);
							changed = true;
						}
					}
				}
				if (changed)
					data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			}
			getProject().removeFileElement(e);//must remove first, before disposing
			groups.removeElement(e, true);
			e.dispose();
		}
	}
	public void elementDisposed(FileElement e){
		if (groups!=null)
			groups.removeElement(e, false);
	}
	public void projectEstablished(){
		getFileCoordinator().addMenuItem(MesquiteTrunk.charactersMenu, listOfCharacterGroupsName, makeCommand("showCharacterGroups",  this));
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
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals(listOfCharacterGroupsName)) {
				temp.addLine("showCharacterGroups ", e); 
			}
		}
		return temp;
	}
	ManagerAssistant lister = null;
	/*.................................................................................................................*/
	public ManagerAssistant showCharacterGroupList(Object obj, String listerName){
		
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
		if (checker.compare(this.getClass(), "Shows list of the character groups", null, commandName, "showCharacterGroups")) {
			return showCharacterGroupList(null, listOfCharacterGroupsName);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		//return null;
	}
	public Class getElementClass(){
		return CharacterPartition.class;
	}
	public String upperCaseTypeName(){
		return "Character Partition";
	}
	public String lowerCaseTypeName(){
		return "character partition";
	}
	public String nexusToken(){
		return "CHARPARTITION";
	}

	private CharactersGroup makeGroup(String name, Parser subcommands, MesquiteFile file){
		CharactersGroup group = groups.findGroup(name);
		if (group==null) {
			group = new CharactersGroup();
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
		if (specsSet==null || !(specsSet instanceof CharacterPartition) || !(specification instanceof CharactersGroup))
			return;
		CharacterPartition characterPartition = (CharacterPartition)specsSet;
		characterPartition.setProperty(specification,ic);
	}
	public SpecsSet getNewSpecsSet(String name, CharacterData data){
		return new CharacterPartition(name, data.getNumChars(), null, data);
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
	public String nexusStringForSpecsSet(CharSpecsSet specsSet, CharacterData data, MesquiteFile file, boolean isCurrent){
		if (specsSet ==null || !(specsSet instanceof CharacterPartition))
			return null;
		CharacterPartition characterPartition = (CharacterPartition)specsSet;
		String s= "";
		if (characterPartition !=null && (characterPartition.getFile()==file || (characterPartition.getFile()==null && data.getFile()==file))) {
			String sT = " ";
			CharactersGroup[] parts = characterPartition.getGroups();
			boolean firstTime = true;

			if (parts!=null)
				for (int i=0; i<parts.length; i++) {
					String q = ListableVector.getListOfMatches((Listable[])characterPartition.getProperties(), parts[i], CharacterStates.toExternal(0));
					if (q != null) {
						if (!firstTime)
							sT += ", ";
						firstTime = false;
						sT += StringUtil.tokenize(parts[i].getName()) + " : " + q;
					}
				}

			if (!StringUtil.blank(sT)) {
				s+= "\tCHARPARTITION " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(characterPartition.getName()) + " ";
				if (writeLinkWithCharacterMatrixName(file, data))
					s+= " (CHARACTERS = " +  StringUtil.tokenize(data.getName()) + ")";
				s+= " = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		if (blockName.equalsIgnoreCase("LABELS")) {
			String commandName = parser.getFirstToken(command);
			if ("CHARGROUPLABEL".equalsIgnoreCase(commandName)) {
				String name = parser.getNextToken();
				makeGroup(name, parser, file); //pass whole command
				return true;
			}
			return false;
		}
		else 
			return super.readNexusCommand(file, nBlock, blockName, command, comment);
	}
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("LABELS")) {
			String s = "";
			for (int i = 0; i< groups.size(); i++){
				CharactersGroup cg = (CharactersGroup)groups.elementAt(i);
				if (cg.getFile() == file){
					s += "\tCHARGROUPLABEL " + ParseUtil.tokenize(cg.getName());
					if (cg.colorSet()){
						Color c = cg.getColor();
						s += " COLOR = (RGB " + MesquiteDouble.toString(c.getRed()/255.0) + " " + MesquiteDouble.toString(c.getGreen()/255.0) + " " + MesquiteDouble.toString(c.getBlue()/255.0) + ") ";
					}
					s += ";" + StringUtil.lineEnding();
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
		return false;
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage character partititions";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages (including NEXUS read/write) character partitions." ;
	}
	/*.................................................................................................................*/

}

/* ======================================================================== */
class PartitionNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		if ((blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS")) && commandName.equalsIgnoreCase("CHARPARTITION"))
			return true;

		if ((blockName.equalsIgnoreCase("LABELS")) && commandName.equalsIgnoreCase("CHARGROUPLABEL"))
			return true;

		return false;
	}
}


