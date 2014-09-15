/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.ManageTaxaPartitions;
/*~~  */

import java.util.*;
import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.characters.CharacterPartition;
import mesquite.lib.duties.*;
import mesquite.lists.lib.GroupDialog;

/** Manages specifications of partitions of taxa, including reading and writing from NEXUS file. */
public class ManageTaxaPartitions extends SpecsSetManager {
	final static String listOfTaxonGroupsName = "List of Taxon Group Labels";

	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(mesquite.lists.TaxaPartitionList.TaxaPartitionList.class, getName() + "  uses an assistant to display a list window.",
		"The assistant is arranged automatically");
	}
	TaxaGroupVector groups; //TODO: dealing with taxa groups should probably be a separate module to allow it to be recognized as element manager
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		groups = new TaxaGroupVector();
		getProject().addFileElement(groups);
		return true;
	}


	public void elementsReordered(ListableVector v){
	}
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		if (e instanceof TaxaGroup){
			TaxaGroup group = (TaxaGroup)e;
			GroupDialog d = new GroupDialog(getProject(),getProject().getCoordinatorModule().containerOfModule(), "Edit Taxa Group", group.getName(), group.getColor(), group.getSymbol(), group.supportsSymbols());
			d.completeAndShowDialog();
			String name = d.getName();
			MesquiteSymbol symbol = d.getSymbol();
			boolean ok = d.query()==0;
			Color c = d.getColor();
			d.dispose();
			if (!ok)
				return null;


			if (!StringUtil.blank(name)) {
				group.setName(name);
			}
			group.setColor(c);
			group.setSymbol(symbol);
			notifyOfGroupChange(group);
			return null;
		}
		//TODO:
		if (e != null)
			alert("Sorry, the " + e.getTypeName() + "  cannot be shown by this means yet.");
		return null;
	}
	public void notifyOfGroupChange(TaxaGroup e){
		if (e instanceof TaxaGroup){
			e.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			ListableVector d = getProject().getTaxas();
			for (int im = 0; im<d.size(); im++){
				Taxa taxa = (Taxa)d.elementAt(im);
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaPartition.class);
				TaxaPartition cp = (TaxaPartition)ssv.getCurrentSpecsSet();
				boolean done = false;
				if (cp != null)
					for (int ic = 0; ic< taxa.getNumTaxa() && ! done; ic++){
						if (cp.getTaxaGroup(ic) == e) {
							taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
							done = true;
						}
					}
				for (int is = 0; is< ssv.size() && !done; is++){
					cp = (TaxaPartition)ssv.elementAt(is);
					for (int ic = 0; ic< taxa.getNumTaxa() && !done; ic++){
						if (cp.getTaxaGroup(ic) == e) {
							taxa.notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  
							done = true;
						}
					}
				}
			}
		}
	}
	public NexusBlock elementAdded(FileElement e){
		if (e instanceof TaxaGroup){
			
			if (groups.indexOf(e)<0) {
				groups.addElement(e, true);
				e.addListener(groups);
			}
			e.setManager(this);
			return null;
		}
		else if (e!=null && e instanceof Taxa){
			e.prepareSpecsSetVector(TaxaPartition.class, "Taxa Partitions");
		}
		return null;
	}
	public void elementDisposed(FileElement e){
		if (groups !=null)
			groups.removeElement(e, true);
	}
	public void deleteElement(FileElement e){
		if (e instanceof TaxaGroup){
			ListableVector d = getProject().getTaxas();
			for (int im = 0; im<d.size(); im++){
				Taxa taxa = (Taxa)d.elementAt(im);
				boolean changed = false;
				SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaPartition.class);
				TaxaPartition cp = (TaxaPartition)ssv.getCurrentSpecsSet();
				if (cp != null)
					for (int ic = 0; ic< taxa.getNumTaxa(); ic++){
						if (cp.getTaxaGroup(ic) == e) {
							cp.setProperty(cp.getDefaultProperty(ic), ic);
							changed = true;
						}
					}
				for (int is = 0; is< ssv.size(); is++){
					cp = (TaxaPartition)ssv.elementAt(is);
					for (int ic = 0; ic< taxa.getNumTaxa(); ic++){
						if (cp.getTaxaGroup(ic) == e) {
							cp.setProperty(cp.getDefaultProperty(ic), ic);
							changed = true;
						}
					}
				}
				if (changed)
					taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
			}
			getProject().removeFileElement(e);//must remove first, before disposing
			groups.removeElement(e, true);
			e.dispose();
		}
	}
	public Class getElementClass(){
		return TaxaPartition.class;
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		MesquiteSubmenuSpec mmis = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu,"List of Taxa Partitions", makeCommand("showPartitions",  this), (ListableVector)getProject().taxas);
		mmis.setOwnerModuleID(getID());
		mmis.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, listOfTaxonGroupsName, makeCommand("showTaxonGroups",  this));
		groups.addToFile(getProject().getHomeFile(), getProject(), this);
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals("List of Taxa Partitions")) {
				Object o = e.doCommand("getTaxa", null, CommandChecker.defaultChecker);

				if (o !=null && o instanceof Taxa) {
					//int wh =getProject().getTaxaReference((Taxa)o);
					temp.addLine("showPartitions " + getProject().getTaxaReferenceExternal((Taxa)o), e); 
				}
				else
					temp.addLine("showPartitions ", e); 
			} else if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals(listOfTaxonGroupsName)) {
				temp.addLine("showTaxonGroups ", e); 
			}
		}
		return temp;
	}

	/*.................................................................................................................*/
	 public ManagerAssistant showTaxonGroupList(Object obj, String listerName){
	 		//Check to see if already has lister for this
/*	 		boolean found = false;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant)
				if (((ManagerAssistant)e).showing(obj)) {
					((ManagerAssistant)e).getModuleWindow().setVisible(true);
					return ((ManagerAssistant)e);
				}
		}
		*/
		ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize(listerName));
			if (lister!=null) {
				lister.showListWindow(obj);
	 			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
	 				lister.getModuleWindow().setVisible(true);
	 		}
		return lister;
	 		
}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Shows lists of the taxa partitions", null, commandName, "showPartitions")) {
			if (StringUtil.blank(arguments)) {
				for (int i = 0; i< getProject().getNumberTaxas(checker.getFile()); i++) {
					showSpecsSets(getProject().getTaxa(checker.getFile(), i), "List of Taxa Partitions");
				}
			}
			else {
				Taxa t = getProject().getTaxa(checker.getFile(), parser.getFirstToken(arguments));
				if (t!=null ) {
					return showSpecsSets(t, "List of Taxa Partitions");
				}
			}
//			alert("Sorry, there are no taxa partitions");
		}
		else if (checker.compare(this.getClass(), "Shows list of the taxon groups", null, commandName, "showTaxonGroups")) {
					return showTaxonGroupList(null, listOfTaxonGroupsName);
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	private TaxaGroup findGroup(String token){ 
		if (token ==null)
			return null;
		for (int i = 0; i< groups.size(); i++){
			TaxaGroup cg = (TaxaGroup)groups.elementAt(i);
			if (token.equalsIgnoreCase(cg.getName())){
				return cg;
			}
		}
		return null;
	}

	private TaxaGroup makeGroup(String name, Parser subcommands, MesquiteFile file){
		TaxaGroup group = findGroup(name);
		if (group==null) {
			group = new TaxaGroup();
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
					token = subcommands.getNextToken(); // should be RGB
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
				else if (token.equalsIgnoreCase("SYMBOL")){
					token = subcommands.getNextToken(); //=
					token = subcommands.getNextToken(); // (
					token = subcommands.getNextToken(); // should be NAME					
					if (token!=null && token.equalsIgnoreCase("NAME")) {
						token = subcommands.getNextToken(); //=
						token = subcommands.getNextToken(); // name of symbol
						Listable[] list = getProject().getFileElements(SymbolsVector.class);
						if (list != null && list.length >  0){
							SymbolsVector symVector = (SymbolsVector)list[0];
							MesquiteSymbol symbol = (MesquiteSymbol)symVector.elementWithName(token);
							if (symbol!=null) {
								MesquiteSymbol groupSymbol = symbol.cloneMethod();
								Parser remaining = new Parser();
								remaining.setString(subcommands.getRemainingUntilChar(')',true));
								groupSymbol.interpretNexus(remaining);
								group.setSymbol(groupSymbol);
							}
						}
					}
				}
				else if (token.equalsIgnoreCase("HIDDEN")){
					group.setVisible(false);
				}
			}
		}
		return group;
	}
	/*.................................................................................................................*/
	String nexusStringForSpecsSet(TaxaPartition taxaPartition, Taxa taxa, MesquiteFile file, boolean isCurrent){
		String s= "";
		if (taxaPartition !=null && (taxaPartition.getFile()==file || (taxaPartition.getFile()==null && taxa.getFile()==file))) {
			String sT = " ";
			TaxaGroup[] parts = taxaPartition.getGroups();
			boolean firstTime =true;

			if (parts!=null)
				for (int i=0; i<parts.length; i++) {
					String q = ListableVector.getListOfMatches((Listable[])taxaPartition.getProperties(), parts[i], 1, false);
					if (q != null) {
						if (!firstTime)
							sT += ", ";
						firstTime = false;
						sT += StringUtil.tokenize(parts[i].getName()) + " : " + q ;
					}
				}

			if (!StringUtil.blank(sT)) {
				s+= "\tTAXPARTITION " ;
				if (isCurrent)
					s += "* ";
				s+= StringUtil.tokenize(taxaPartition.getName()) + " ";
				if (file.getProject().getNumberTaxas()>1)
					s+= " (TAXA = " +  StringUtil.tokenize(taxa.getName()) + ")";
				s+= " = "+  sT + ";" + StringUtil.lineEnding();
			}
		}
		return s;
	}
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("LABELS")) {
			String s = "";
			for (int i = 0; i< groups.size(); i++){
				TaxaGroup cg = (TaxaGroup)groups.elementAt(i);
				if (cg.getFile() == file){
					s += "\tTAXAGROUPLABEL " + ParseUtil.tokenize(cg.getName());
					if (cg.colorSet()){
						Color c = cg.getColor();
						s += " COLOR = (RGB " + MesquiteDouble.toString(c.getRed()/255.0) + " " + MesquiteDouble.toString(c.getGreen()/255.0) + " " + MesquiteDouble.toString(c.getBlue()/255.0) + ") ";
					}
					if (cg.symbolSet()){
						MesquiteSymbol symbol = cg.getSymbol();
						if (symbol != null)
							s += " SYMBOL = (NAME="+ParseUtil.tokenize(symbol.getName()) + " SIZE="+symbol.getSize() + " "+ symbol.getBasicNexusOptions()+ " "+ symbol.getExtraNexusOptions() + ") ";
					}
					if (!cg.isVisible()){
						Color c = cg.getColor();
						s += " HIDDEN ";
					}
					s += ";" + StringUtil.lineEnding();
				}
			}
			if (StringUtil.blank(s))
				return null;
			else
				return s;
		}
		else  if (blockName.equalsIgnoreCase("SETS")) {
			String s= "";
			for (int ids = 0; ids<file.getProject().getNumberTaxas(); ids++) {

				Taxa taxa =  file.getProject().getTaxa(ids);
				if (taxa.getFile() == file){
					int numSets = taxa.getNumSpecsSets(TaxaPartition.class);
					SpecsSetVector ssv = taxa.getSpecSetsVector(TaxaPartition.class);
					if (ssv!=null){
						TaxaPartition ms = (TaxaPartition)taxa.getCurrentSpecsSet(TaxaPartition.class);
						if (ms!=null && (ms.getNexusBlockStored()==null || blockName.equalsIgnoreCase(ms.getNexusBlockStored()))) {
							ms.setNexusBlockStored(blockName);
							ms.setName("UNTITLED");
							s += nexusStringForSpecsSet(ms, taxa, file, true);
						}


						for (int ims = 0; ims<numSets; ims++) {
							s += nexusStringForSpecsSet((TaxaPartition)taxa.getSpecsSet(ims, TaxaPartition.class), taxa, file, false);
						}
					}
				}

			}
			return s;
		}
		return null;
	}
	/*.................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
		if (fuse)
			return false;
		if (blockName.equalsIgnoreCase("LABELS")) {
			String commandName = parser.getFirstToken(command);
			if ("TAXAGROUPLABEL".equalsIgnoreCase(commandName)) {
				String name = parser.getNextToken();
				makeGroup(name, parser, file); //pass whole command
				return true;
			}
			return false;
		}
		else if (blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS")) {
			MesquiteInteger startCharT = new MesquiteInteger(0);

			String commandName = ParseUtil.getToken(command, startCharT);
			if (commandName.equalsIgnoreCase("TAXAPARTITION") || commandName.equalsIgnoreCase("TAXPARTITION")) {
				String token = ParseUtil.getToken(command, startCharT);
				boolean isDefault = false;
				if ("*".equals(token)) {
					token = ParseUtil.getToken(command, startCharT);
					isDefault = true;
				}
				String nameOfTypeset = token; // name of typeset 
				token = ParseUtil.getToken(command, startCharT);
				String paradigmString = null;
				Taxa taxa = null;
				if (token.equalsIgnoreCase("(")) {
					token = ParseUtil.getToken(command, startCharT); //TAXA  //TODO: check to see what parameter is being set!
					token = ParseUtil.getToken(command, startCharT); //=
					token = (ParseUtil.getToken(command, startCharT)); // name of data
					taxa = file.getProject().getTaxaLastFirst(token); //taxa is named; seek without restriction to current file
					token = ParseUtil.getToken(command, startCharT); //)
					token = ParseUtil.getToken(command, startCharT);  //=
				}
				else if (file.getProject().getNumberTaxas(file)>0)//taxa not named; seek with restriction to current file
					taxa = file.getProject().getTaxa(file, 0);
				else  
					taxa = file.getProject().getTaxa(0);

				if (taxa == null)
					return false;

				if (token.equals("="))
					token = ParseUtil.getToken(command, startCharT); 

				TaxaGroup defaultProperty =  null;

				TaxaGroup lastCM =makeGroup(token, null, file);
				//lastCM.setGroupNumber(MesquiteInteger.fromString(token));
				TaxaPartition taxaPartition= new TaxaPartition(nameOfTypeset, taxa.getNumTaxa(), defaultProperty, taxa);
				taxaPartition.setNexusBlockStored(blockName);

				int lastChar = -1;
				boolean join = false;
				boolean nextIsPart = false;
				while (!token.equals(";") && token.length()>0) {
					if (token.equals("-")) {
						if (lastChar!=-1)
							join = true;
					}
					else if (token!=null) {
						if (token != null && token.equals("."))
							token = Integer.toString(taxa.getNumTaxa());

						if (token.startsWith("-")) {
							if (lastChar!=-1)
								join = true;
							token = token.substring(1, token.length());
						}
						if (token.equals(":")) {
							nextIsPart = true;
						}
						else if (token.equals(","))
							nextIsPart=false;
						else if (nextIsPart) {
							//MesquiteInteger cm = new MesquiteInteger(MesquiteInteger.fromString(token));
							int whichTaxon = Taxon.toInternal(MesquiteInteger.fromString(token, false));
							if (MesquiteInteger.isCombinable(whichTaxon)) {
								if (join) {
									for (int j = lastChar; j<=whichTaxon; j++) {
										taxaPartition.setProperty(lastCM,j);
									}
									join = false;
									lastChar = -1;
								}
								else {
									lastChar = whichTaxon;
									taxaPartition.setProperty(lastCM,whichTaxon);
								}
							}
						}
						else {
							lastCM = makeGroup(token, null, file);
							/*int whichGroup = MesquiteInteger.fromString(token, false);
							if (!MesquiteInteger.isCombinable(whichGroup))
								whichGroup = ColorDistribution.getStandardColorNumber(token);
							lastCM.setGroupNumber(whichGroup);
							 */
							nextIsPart = true;
						}
					}
					token = ParseUtil.getToken(command, startCharT); 
				}

				if (isDefault) { //todo: OR IS FIRST
					if (!"UNTITLED".equals(taxaPartition.getName())) {
						taxa.storeSpecsSet(taxaPartition, TaxaPartition.class);
					}
					taxaPartition.addToFile(file, getProject(), this);
					SpecsSet ss = taxaPartition.cloneSpecsSet();
					ss.addToFile(file, getProject(), this);
					taxa.setCurrentSpecsSet(ss, TaxaPartition.class);
				}
				else {
					taxa.storeSpecsSet(taxaPartition, TaxaPartition.class);
					taxaPartition.addToFile(file, getProject(), this);
				}
				return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new TPartNexusCommandTest();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Manage taxa partititions";
	}

	/*.................................................................................................................*/

	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages (including NEXUS read/write) taxa partitions." ;
	}
	/*.................................................................................................................*/

}

class TPartNexusCommandTest  extends NexusCommandTest{
	public boolean readsWritesCommand(String blockName, String commandName, String command){  //returns whether or not can deal with command
		if ((blockName.equalsIgnoreCase("SETS") || blockName.equalsIgnoreCase("ASSUMPTIONS"))  && (commandName.equalsIgnoreCase("TAXAPARTITION") ||commandName.equalsIgnoreCase("TAXPARTITION")))
			return true;
		if ((blockName.equalsIgnoreCase("LABELS")) && commandName.equalsIgnoreCase("TAXAGROUPLABEL"))
			return true;
		return false;
	}
}


