/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.minimal.ManageTaxa;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.*;

/** Manages the blocks of TAXA, including reading and writing from a NEXUS file.*/
public class ManageTaxa extends TaxaManager {
	public String getName() {
		return "Manage TAXA blocks"; //name must be updated in BasicFileCoord
	}
	public String getNameForMenuItem() {
		return "Taxon Manager";
	}
	public String getExplanation() {
		return "Manages sets of taxa (including read/write TAXA block in NEXUS file)." ;
	}
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed

		EmployeeNeed e3 = registerEmployeeNeed(mesquite.lists.TaxonList.TaxonList.class, "The List of Taxa window permits viewing and modifying of taxon properties",
				"The List of Taxa window is available in the Taxa&Trees menu.");
	}
	/*.................................................................................................................*/

	MesquiteBoolean alerts;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		setMenuToUse(MesquiteTrunk.treesMenu);
		alerts = new MesquiteBoolean(true);

		return true;
	}
	public void elementsReordered(ListableVector v){
		if (v == getProject().taxas){
			NexusBlock.equalizeOrdering(v, getProject().getNexusBlocks());
		}
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "-", null);
		MesquiteSubmenuSpec mmis = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "List of Taxa", makeCommand("showTaxa",  this),  (ListableVector)getProject().taxas);
		//getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "-", null);
		mmis.setBehaviorIfNoChoice(MesquiteSubmenuSpec.ONEMENUITEM_ZERODISABLE);
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "New Block of Taxa...", makeCommand("newTaxa",  this));
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "List of Taxa Blocks", makeCommand("showTaxasList",  this));
		//getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "Merge taxa blocks...", makeCommand("mergeTaxa",  this));
		//	getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "-", null);
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in or completely set up (if a new file).*/
	public void fileReadIn(MesquiteFile f) {
		for (int i= 0; i< getProject().getNumberTaxas(); i++){
			Taxa t = getProject().getTaxa(i);
			restoreOrderIfNeeded(t);
		}
	}
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		if (e instanceof Taxa){
			Taxa t = (Taxa)e;
			return showTaxa(t);
		}
		return null;
	}
	public void deleteElement(FileElement e){
		if (e instanceof Taxa){
			for (int i = 0; i<getNumberOfEmployees(); i++) {
				Object ma=getEmployeeVector().elementAt(i);
				if (ma instanceof ManagerAssistant)
					if (((ManagerAssistant)ma).showing(e)) {
						fireEmployee(((ManagerAssistant)ma));
					}
			}
			Taxa taxa = (Taxa)e;
			taxa.doom();
			getProject().removeFileElement(taxa);//must remove first, before disposing
			taxa.dispose();
		}
	}
	/*.................................................................................................................*/
	public Taxa makeNewTaxa(String title, int numTaxa, boolean userQuery){
		Taxa newTaxa=null;
		if (userQuery) {
			title= getProject().getTaxas().getUniqueName("Taxa");
			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			MesquiteInteger ion = new MesquiteInteger(3);
			MesquiteString ios = new MesquiteString(title);
			String s = "Use this dialog box to create a new, named block of taxa.";
			StringIntegerDialog dialog = new StringIntegerDialog(containerOfModule(), "New Block of Taxa", "Name of new block", "Number of taxa", ios,ion, 1, MesquiteInteger.unassigned, buttonPressed,s);

			if (buttonPressed.getValue()!=0)
				return null;
			numTaxa= ion.getValue();
			title= ios.getValue();

			if (getProject().getTaxas().getElementIgnoreCase(title)!=null) {
				title= getProject().getTaxas().getUniqueName("Taxa");
				message("Sorry, that name is already taken.  The taxa block will be assigned the name " + title);
			}
		}
		if (MesquiteInteger.isCombinable(numTaxa) && numTaxa >= Taxa.MAXNUMTAXA) {
			message("Sorry, the maximum number of taxa in a taxa block is " + Taxa.MAXNUMTAXA);
		}
		else if (numTaxa>=0 && numTaxa< Taxa.MAXNUMTAXA) {
			newTaxa = new Taxa(numTaxa);
			if (title!=null)
				newTaxa.setName(title);
			String taxonName;
			for (int it=0; it<numTaxa; it++) {
				newTaxa.getTaxon(it).setName("taxon " + Taxon.toExternal(it));
				newTaxa.getTaxon(it).setNameIsDefault(true);
			}
		}
		return newTaxa;
	}
	/*.................................................................................................................*/
	public void elementDisposed(FileElement e){
		if (e==null || !(e instanceof Taxa))
			return;
		NexusBlock nb = findNEXUSBlock(e);
		if (nb!=null) {
			removeNEXUSBlock(nb);
		}
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object ma=getEmployeeVector().elementAt(i);
			if (ma instanceof ManagerAssistant)
				if (((ManagerAssistant)ma).showing(e)) {
					fireEmployee(((ManagerAssistant)ma));
				}
		}
	}
	/*.................................................................................................................*/
	public NexusBlock elementAdded(FileElement taxa){
		if (taxa==null || !(taxa instanceof Taxa))
			return null;
		NexusBlock nb = findNEXUSBlock(taxa);
		if (nb==null) {
			TaxaBlock t = new TaxaBlock(taxa.getFile(), this);
			t.setTaxa((Taxa)taxa);
			addNEXUSBlock(t);
			resetAllMenuBars();
			return t;
		}
		else return nb;
	}
	/*.................................................................................................................*/
	/** check if any adjustments are needed before writing, .e.g. resolve name conflicts.  Format is file type, e.g. NEXUS, NEXML. */
	public void preWritingCheck(MesquiteFile file, String format){	
		if (format.equalsIgnoreCase("NEXUS")){
			int numSets = getProject().getNumberTaxas();
			ListableVector taxas = getProject().getTaxas();
			String[] names = new String[numSets];
			boolean changed = false;
			String changes = "";
			for (int i=0; i<numSets; i++) {
				Taxa taxa = getProject().getTaxa(i);
				if (taxa.getFile() == file){
					String cName = taxa.getName();
					if (StringArray.indexOf(names, cName)>=0){
						taxa.setName(taxas.getUniqueName(cName));
						changed = true;
						changes += " Taxa block " + cName + " renamed to " + taxa.getName();
					}
					else
						names[i] = taxa.getName();

				}
			}
			if (changed)
				discreetAlert("Some taxa blocks had the same name (i.e., the same title).  This is not permitted in NEXUS files; some names were changed (" + changes + ")");
		}
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new TaxaBlockTest();}
	/*.................................................................................................................*/
	public NexusCommandTest getNexusCommandTest(){ 
		return new TAXANexusCommandTest();
	}
	/*.................................................................................................................*/
	public Snapshot getIDSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (file == null || file == getProject().getHomeFile()){
			for (int i = 0; i< getProject().getNumberTaxas(); i++) {
				if (StringUtil.blank(getProject().getTaxa(i).getUniqueID()))
					temp.addLine("setID " + i + " " + getProject().getTaxa(i).getAssignedIDNumber());
				else
					temp.addLine("setID " + i + " " + getProject().getTaxa(i).getAssignedIDNumber() + " " + getProject().getTaxa(i).getUniqueID());
				Snapshot fromTaxa = getProject().getTaxa(i).getSnapshot(file);
				if (fromTaxa != null && fromTaxa.getNumLines() > 0) {
					temp.addLine("tell It");
					temp.incorporate(fromTaxa, true);
					temp.addLine("endTell");
				}
			}
		}
		return temp;
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();

		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals("Taxa blocks list")) {
				temp.addLine("showTaxasList ", e); 
			}
		}
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			MesquiteModule e=(MesquiteModule)getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant && (e.getModuleWindow()!=null) && e.getModuleWindow().isVisible() && e.getName().equals("Taxon List")) {
				Object o = e.doCommand("getTaxa", null, CommandChecker.defaultChecker);

				if (o !=null && o instanceof Taxa) {
					//int wh =getProject().getTaxaReference((Taxa)o);
					temp.addLine("showTaxa " + getProject().getTaxaReferenceExternal((Taxa)o), e); 
				}
			}
		}
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Requests that a new taxa block be made", "[number of taxa] [title]", commandName, "newTaxa")) {
			//ask user how many taxa
			//create taxa block and add to file
			//return taxa

			incrementMenuResetSuppression();
			Taxa newTaxa=null;
			int numTaxa= 3;
			String title= getProject().getTaxas().getUniqueName("Taxa");
			MesquiteBoolean answer = new MesquiteBoolean(true);
			MesquiteFile file = getProject().chooseFile( "Select file to which to add the new block of taxa"); //added 20 Dec 01
			Object o = null;
			if (StringUtil.blank(arguments)) {
				newTaxa = makeNewTaxa(title, numTaxa, true);
				if (newTaxa==null)
					return null;
				newTaxa.addToFile(file, getProject(), this);

				if (!MesquiteThread.isScripting())
					newTaxa.showMe(); //o = doCommand("showTaxa", getProject().getTaxaReferenceExternal(newTaxa), checker); //changed to scriptingRecord so as not to provoke alert 19Jan02
			}
			else {
				MesquiteInteger io = new MesquiteInteger(0);
				numTaxa= MesquiteInteger.fromString(arguments, io);
				title= ParseUtil.getToken(arguments, io);
				newTaxa = makeNewTaxa(title, numTaxa, false);
				if (newTaxa==null)
					return null;
				newTaxa.addToFile(file, getProject(), this);
				if (!MesquiteThread.isScripting())
					newTaxa.showMe(); //o = doCommand("showTaxa", getProject().getTaxaReferenceExternal(newTaxa), checker);//changed to scriptingRecord so as not to provoke alert 19Jan02
			}
			/*
			if (!MesquiteThread.isScripting() && o!=null && o instanceof MesquiteModule){  
				MesquiteModule mb = (MesquiteModule)o;
				mb.doCommand("showWindow", null, checker);
			}
			 */
			decrementMenuResetSuppression();
			return newTaxa;
		}
		else if (checker.compare(this.getClass(), "Toggles whether alerts are on or off", "[on or off]", commandName, "alerts")) {
			alerts.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Shows list of taxa blocks", null, commandName, "showTaxasList")) {
			//Check to see if already has lister for this
			boolean found = false;
			int numemp = getNumberOfEmployees();
			for (int i = 0; i<numemp; i++) {
				Object e=getEmployeeVector().elementAt(i);
				if (e instanceof ManagerAssistant)
					if (((ManagerAssistant)e).getName().equals("Taxa blocks list")) {
						((ManagerAssistant)e).getModuleWindow().setVisible(true);
						return e;
					}
			}
			ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("Taxa blocks list"));
			if (lister==null){
				message("Sorry, no module was found to list the sets of taxa");
				return null;
			}
			lister.showListWindow(null);
			if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
				lister.getModuleWindow().setVisible(true);
			return lister;
		}
		else if (checker.compare(this.getClass(), "Shows a taxa list window", "[number of taxa block]", commandName, "showTaxa")) {
			if (StringUtil.blank(arguments)) {
				for (int i = 0; i< getProject().getNumberTaxas(); i++) {
					showTaxa(getProject().getTaxa(i));
				}
			}
			else {
				String id = parser.getFirstToken(arguments);
				Taxa t = getProject().getTaxa(checker.getFile(), id);  //first try within file
				if (t == null)
					t = getProject().getTaxa(id);
				if (t!=null) {
					return showTaxa(t);
				}
			}
		}
		else if (checker.compare(this.getClass(), "Sets the ID number of a taxa block", "[id number of taxa block]", commandName, "setID")) {
			int t = MesquiteInteger.fromString(parser.getFirstToken(arguments));
			if (MesquiteInteger.isCombinable(t) && t< getProject().getNumberTaxas()) {
				long id  = MesquiteLong.fromString(parser.getNextToken());
				String uniqueID = parser.getNextToken();
				if (!StringUtil.blank(uniqueID) && getProject().getNumberTaxas(uniqueID)==1){
					Taxa tx = getProject().getTaxa(uniqueID, 0);
					if (tx!=null) {
						tx.setAssignedIDNumber(id);
						return tx;
					}
				}
				Taxa tx = getProject().getTaxa(t);
				if (tx!=null) {
					tx.setAssignedIDNumber(id);
					return tx;
				}
			}

		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	public  MesquiteModule getListOfTaxaModule(Taxa taxa, boolean show){
		boolean found = false;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant)
				if (((ManagerAssistant)e).showing(taxa)) {
					((ManagerAssistant)e).getModuleWindow().setVisible(true);
					return ((ManagerAssistant)e);
				}
		}
		if (show)
			return showTaxa(taxa);
		return null;
	}
	MesquiteModule showTaxa(Taxa tx){
		boolean found = false;
		for (int i = 0; i<getNumberOfEmployees(); i++) {
			Object e=getEmployeeVector().elementAt(i);
			if (e instanceof ManagerAssistant)
				if (((ManagerAssistant)e).showing(tx)) {
					((ManagerAssistant)e).getModuleWindow().setVisible(true);
					return ((ManagerAssistant)e);
				}
		}

		ManagerAssistant lister= (ManagerAssistant)hireNamedEmployee(ManagerAssistant.class, StringUtil.tokenize("Taxon List"));
		if (lister==null){
			if (!MesquiteThread.isScripting()) 
				message("Sorry, no module was found to list the taxa.");
			return null;
		}
		lister.showListWindow(tx);
		if (!MesquiteThread.isScripting() && lister.getModuleWindow()!=null)
			lister.getModuleWindow().setVisible(true);
		return lister;
	}
	NameReference commentsRef = NameReference.getNameReference("comments");
	/*.................................................................................................................*/
	public String getNexusCommands(MesquiteFile file, String blockName){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			StringBuffer s = new StringBuffer();
			boolean found = false;
			MesquiteProject project = file.getProject();
			for (int i=0; i<project.getNumberTaxas(); i++){
				Taxa taxa = getProject().getTaxa(i);
				String taxonReference = "";
				if (taxa.getName() != null && (project.getNumberTaxas()>1 && MesquiteFile.okToWriteTitleOfNEXUSBlock(file, taxa)))
					taxonReference = " TAXA = "+ StringUtil.tokenize(taxa.getName());
				if (taxa.getFile() == file) {

					//look through all attached bits 
					int numBits = taxa.getNumberAssociatedBits();

					for (int v = 0; v<numBits; v++){  //added September 2011
						Bits array = taxa.getAssociatedBits(v);
						if (!array.getNameReference().getValue().equals("selected")){
							s.append("\tTAXABITS  "+ taxonReference);
							s.append(" NAME = ");
							s.append( ParseUtil.tokenize(array.getNameReference().getValue()));
							s.append(" on = ");

							String sT = "";
							int continuing = 0;
							int lastWritten = -1;
							for (int ic=0; ic<taxa.getNumTaxa(); ic++) {
								if (array.isBitOn(ic)) {
									if (continuing == 0) {
										sT += " " + Taxon.toExternal(ic);
										lastWritten = ic;
										continuing = 1;
									}
									else if (continuing == 1) {
										sT += " - ";
										continuing = 2;
									}
								}
								else if (continuing>0) {
									if (lastWritten != ic-1) {
										sT += " " + Taxon.toExternal(ic-1);
										lastWritten = ic-1;
									}
									else
										lastWritten = -1;
									continuing = 0;
								}

							}
							if (continuing>1)
								sT += " " + Taxon.toExternal(taxa.getNumTaxa()-1);
							s.append(sT);
							s.append( ";" + StringUtil.lineEnding());
							found = true;
						}

					}

					for (int it = 0; it<taxa.getNumTaxa(); it++){
						String obj = taxa.getAnnotation(it);
						if (!StringUtil.blank(obj)){
							s.append("\tTEXT "+ taxonReference + " TAXON = " + Taxon.toExternal(it) + " TEXT = " + StringUtil.tokenize(obj) + ";" + StringUtil.lineEnding());
							found = true;
						}
						//look through all attached longs 
						int numLongs = taxa.getNumberAssociatedLongs();

						for (int v = 0; v<numLongs; v++){
							LongArray array = taxa.getAssociatedLongs(v);
							long c = array.getValue(it);

							if (MesquiteLong.isCombinable(c)){

								s.append("\tSUT  "+ taxonReference);
								s.append(" TAXON = ");
								s.append(Integer.toString(it+1));
								s.append(" NAME = ");
								s.append( ParseUtil.tokenize(array.getNameReference().getValue()));
								s.append(" INTEGER = ");
								s.append(Long.toString(c));
								s.append( ";" + StringUtil.lineEnding());
								found = true;
							}
						}
						//look through all attached doubles 
						int numDoubs = taxa.getNumberAssociatedDoubles();

						for (int v = 0; v<numDoubs; v++){
							DoubleArray array = taxa.getAssociatedDoubles(v);
							double c = array.getValue(it);

							if (MesquiteDouble.isCombinable(c)){
								s.append("\tSUT  "+ taxonReference);
								s.append(" TAXON = ");
								s.append(Integer.toString(it+1));
								s.append(" NAME = ");
								s.append( ParseUtil.tokenize(array.getNameReference().getValue()));
								s.append(" REAL = ");
								s.append(Double.toString(c));
								s.append( ";" + StringUtil.lineEnding());
								found = true;
							}
						}
						//look through all attached objects 
						int numObs = taxa.getNumberAssociatedObjects();
						for (int v = 0; v<numObs; v++){
							ObjectArray array = taxa.getAssociatedObjects(v);
							if (!commentsRef.equals(array.getNameReference())){
								Object c = array.getValue(it);

								if (c != null && c instanceof String){
									s.append("\tSUT  "+ taxonReference);
									s.append(" TAXON = ");
									s.append(Integer.toString(it+1));
									s.append(" NAME = ");
									s.append( ParseUtil.tokenize(array.getNameReference().getValue()));
									s.append(" STRING = ");
									s.append(StringUtil.tokenize((String)c));
									s.append( ";" + StringUtil.lineEnding());
									found = true;
								}
							}
						}
					}
				}
			}
			if (found)
				return s.toString();
			else
				return null;
		}
		return null;
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
	 */

	int findWhichTaxon(int provisional, boolean fuse, Taxa taxa, MesquiteFile file){
		int it = Taxon.toInternal(provisional);
		if (!fuse)
			return it;
		IntegerArray translationTable = (IntegerArray)taxa.getAttachment("OrigIndex" + file.getFileName());
		if (translationTable != null) {
			int w = translationTable.getValue(it);
			if (MesquiteInteger.isCombinable(w))
				return w;
		}
		IntegerArray oi = (IntegerArray)taxa.getAttachment("originalIndicesDupRead", IntegerArray.class);
		if (oi == null) //don't have translation; don't trust and indicate whichTaxon -1
			return MesquiteInteger.unassigned;
		int w = oi.getValue(it);
		if (MesquiteInteger.isCombinable(w))
			return w;
		return MesquiteInteger.unassigned;
	}
	/*...................................................................................................................*/
	public boolean readNexusCommand(MesquiteFile file, NexusBlock nBlock, String blockName, String command, MesquiteString comment){ 
		if (blockName.equalsIgnoreCase("NOTES")) {
			boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
			MesquiteProject project = file.getProject();
			String commandName = parser.getFirstToken(command);
			if (commandName.equalsIgnoreCase("TAXA")) {
				String ttoken  = parser.getNextToken(); //=
				ttoken  = parser.getNextToken(); //TAXA block (optional)
				Taxa t = getProject().findTaxa(file, ttoken); 
				/*getTaxaLastFirst(ttoken);
				if (t==null){
					int wt = MesquiteInteger.fromString(ttoken);
					if (MesquiteInteger.isCombinable(wt))
						t = getProject().getTaxa(wt-1);
				}
				if (t == null && getProject().getNumberTaxas(file)==1)
					t = getProject().getTaxa(file, 0);
				if (t == null && getProject().getNumberTaxas()==1)
					t = getProject().getTaxa(0);
				 */
				if (t!=null) {
					nBlock.setDefaultTaxa(t);
					return true;
				}
				else
					return false;
			}
			else if (commandName.equalsIgnoreCase("TEXT")) {
				stringPos.setValue(parser.getPosition());
				String[][] subcommands  = ParseUtil.getSubcommands(command, stringPos);
				if (subcommands == null || subcommands.length == 0 || subcommands[0] == null || subcommands[0].length == 0)
					return false;
				int whichTaxon = MesquiteInteger.unassigned;
				String text = null;
				Taxa taxa = nBlock.getDefaultTaxa();
				for (int i=0; i<subcommands[0].length; i++){
					String subC = subcommands[0][i];
					if ("TAXON".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						whichTaxon = MesquiteInteger.fromString(token);
						if (!MesquiteInteger.isCombinable(whichTaxon))
							return false;
						whichTaxon = findWhichTaxon(whichTaxon, fuse, taxa, file);
					}
					else if ("TAXA".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						Taxa t = getProject().findTaxa(file, token); /*getTaxa(token);
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
						if (t!=null)
							taxa = t;
						else
							return false;
					}
					else if ("TEXT".equalsIgnoreCase(subC)) {
						text = subcommands[1][i];
					}
					else if ("CHARACTER".equalsIgnoreCase(subC)) {
						return false;
					}
				}
				if (!MesquiteInteger.isCombinable(whichTaxon))
					return false;
				if (taxa !=null && text !=null) {
					/*&&& the following is a check in place because of a bug in 1.02 and previous in which copies of NOTES blocks would be written in all linked files; 
					this allowed overwriting by old copies of the NOTES block */
					if (taxa.getFile() != file && file != getProject().getHomeFile()){
						String s = taxa.getAnnotation(whichTaxon);
						if (s != null && !s.equals(text)) {
							file.notesBugWarn = true;
							file.notesBugVector.addElement("Taxon " + (whichTaxon+1));
						}

					}
					/*&&&*/

					taxa.setAnnotation(whichTaxon, text);
					taxa.notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
					return true;
				}

			}
			else if (commandName.equalsIgnoreCase("INTEGER") || commandName.equalsIgnoreCase("SUPPLEMENTAL")  || commandName.equalsIgnoreCase("SUT")) {
				stringPos.setValue(parser.getPosition());
				String[][] subcommands  = ParseUtil.getSubcommands(command, stringPos);
				if (subcommands == null || subcommands.length == 0 || subcommands[0] == null || subcommands[0].length == 0)
					return false;
				int whichTaxon = MesquiteInteger.unassigned;
				int integer = MesquiteInteger.unassigned;
				double doub = MesquiteDouble.unassigned;
				String string = null;
				String text = null;
				String name = null;
				Taxa taxa = getProject().getTaxa(0);
				for (int i=0; i<subcommands[0].length; i++){
					String subC = subcommands[0][i];
					if ("TAXON".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						whichTaxon = MesquiteInteger.fromString(token);
						if (!MesquiteInteger.isCombinable(whichTaxon))
							return false;
						whichTaxon = findWhichTaxon(whichTaxon, fuse, taxa, file);
					}
					else if ("NAME".equalsIgnoreCase(subC)) {
						name = subcommands[1][i];
					}
					else if ("TAXA".equalsIgnoreCase(subC)) {
						String token = subcommands[1][i];
						Taxa t = getProject().findTaxa(file, token);
						/*getTaxa(token);
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
						if (t!=null)
							taxa = t;
						else
							return false;
					}
					else if ("INTEGER".equalsIgnoreCase(subC)) {
						String t = subcommands[1][i];
						integer = MesquiteInteger.fromString(t);
					}
					else if ("REAL".equalsIgnoreCase(subC)) {
						doub = MesquiteDouble.fromString(subcommands[1][i]);
					}
					else if ("STRING".equalsIgnoreCase(subC)) {
						string = subcommands[1][i];
					}
					else if ("CHARACTER".equalsIgnoreCase(subC)) {
						return false;
					}
				}
				if (!MesquiteInteger.isCombinable(whichTaxon))
					return false;
				if (taxa !=null && name != null) {
					if (MesquiteInteger.isCombinable(integer)){
						taxa.setAssociatedLong(NameReference.getNameReference(name), whichTaxon, integer);
						return true;
					}
					else if (MesquiteDouble.isCombinable(doub)){
						taxa.setAssociatedDouble(NameReference.getNameReference(name), whichTaxon, doub);
						return true;
					}
					else if (string != null){
						taxa.setAssociatedObject(NameReference.getNameReference(name), whichTaxon, string);
						return true;
					}
				}

			}
			else if (commandName.equalsIgnoreCase("TAXABITS")){
				String ttoken  = parser.getNextToken(); //TAXA
				Taxa t= getProject().getTaxa(getProject().getNumberTaxas()-1);  //last first
				if ("taxa".equalsIgnoreCase(ttoken)){
					ttoken  = parser.getNextToken(); //=
				
					ttoken  = parser.getNextToken(); //TAXA block id
					t = getProject().findTaxa(file, ttoken); 
					ttoken  = parser.getNextToken(); //NAME
				}
				if (t == null)
					return false;
				ttoken  = parser.getNextToken(); //=
				ttoken  = parser.getNextToken(); //name of bits
				NameReference nRef = NameReference.getNameReference(ttoken);
				String token = parser.getNextToken(); 
				int lastChar = -1;
				boolean join = false;
				while (!token.equals(";") && token.length()>0) {
					if (token.equals("-")) {
						if (lastChar!=-1)
							join = true;
					}
					else if (token!=null) {
						if (token != null && token.equals("."))
							token = Integer.toString(t.getNumTaxa());
					
						if (token.startsWith("-")) {
							if (lastChar!=-1)
								join = true;
							token = token.substring(1, token.length());
						}
						int whichChar = Taxon.toInternal(MesquiteInteger.fromString(token, false));
						if (MesquiteInteger.isCombinable(whichChar)) {
							if (join) {
								for (int j = lastChar; j<= whichChar; j++)
									t.setAssociatedBit(nRef,  j, true);
								lastChar = -1;
								join = false;
							}
							else {
								t.setAssociatedBit(nRef,  whichChar, true);
								lastChar = whichChar;
							}
						}
					}
					token = parser.getNextToken(); 
				}
				return true;
			}
		}
		return false;
	}
	//NameReference importSourceRef = NameReference.getNameReference("importsource");
	//NameReference origIndexRef = NameReference.getNameReference("OrigIndex");
	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		Parser commandParser = new Parser();

		commandParser.setString(block.toString());
		MesquiteInteger startCharC = new MesquiteInteger(0);
		Taxa newTaxa=null;
		String title=getProject().getTaxas().getUniqueName("Taxa");



		boolean titleFound = false;
		String s;
		NexusBlock t=null;
		Vector unrec = new Vector();
		Vector unrecName = new Vector();
		String blockID = null;
		boolean fuse = parser.hasFileReadingArgument(fileReadingArguments, "fuseTaxaCharBlocks");
		boolean hadDuplicateNames= false;
		int firstNewTaxon = 0;
		boolean nameProblems = false;
		boolean merging = false;

		while (!StringUtil.blank(s=commandParser.getNextCommand(startCharC))) {
			String commandName = parser.getFirstToken(s);
			if (commandName.equalsIgnoreCase("DIMENSIONS")) {
				if (fuse){
					String message = "In the file being imported, there is a taxa block called \"" + title + "\". Mesquite will either fuse this taxa block into the taxa block you select below, or it will import that taxa block as new, separate taxa block.";
					newTaxa = getProject().chooseTaxa(containerOfModule(), message, true, "Fuse with Selected Taxa Block", "Add as New Taxa Block");
					if (newTaxa != null){
						firstNewTaxon = newTaxa.getNumTaxa();
						merging = true;
						//This taxa block is to be fused to an existing; thus add to translation table of taxa and characters block names
						if (titleFound)
							file.taxaNameTranslationTable.addElement(new MesquiteString(title, newTaxa.getName()), false);
						hadDuplicateNames= !StringUtil.blank(newTaxa.hasDuplicateNames());

					}
				}
				int numTaxa = MesquiteInteger.fromString(parser.getTokenNumber(4));
				if (!MesquiteInteger.isCombinable(numTaxa) || numTaxa <0){
					alert("Sorry, the DIMENSIONS statement of the TAXA block appears to be misformatted.  The number of taxa is not validly specified. File reading will fail.");
					return null;
				}
				String inBlock = "";
				if (titleFound)
					inBlock = " in block " + title;
				logln("   " + MesquiteInteger.toString(numTaxa) + " taxa" + inBlock);
				if (newTaxa != null && fuse){

					newTaxa.addTaxa( newTaxa.getNumTaxa()-1, numTaxa, true);
					NameReference colorNameRef = NameReference.getNameReference("color");
					for (int it = firstNewTaxon; it<newTaxa.getNumTaxa(); it++)
						newTaxa.setAssociatedLong(colorNameRef, it, 10, true);
				}
				else {

					newTaxa = new Taxa(numTaxa);

					newTaxa.setName(title);
					//t = newTaxa.addToFile(file, getProject(), this);
					newTaxa.deleteUniqueIDs();
					if (!StringUtil.blank(blockID))
						newTaxa.setUniqueID(blockID);

				}

			}
			else if (commandName.equalsIgnoreCase("TITLE")) {
				title = parser.getTokenNumber(2);
				if (fuse && merging && newTaxa != null)
					file.taxaNameTranslationTable.addElement(new MesquiteString(title, newTaxa.getName()), false);
				titleFound = true;
			}
			else if (commandName.equalsIgnoreCase("TAXLABELS") && newTaxa!=null) {
				String taxonName;
				boolean first = true;
				IntegerArray translationTable = null;
				if (fuse){
					translationTable = new IntegerArray(newTaxa.getNumTaxa() - firstNewTaxon);  
					translationTable.setNameReference(NameReference.getNameReference("OrigIndex" + file.getFileName()));
					/*for (int it = 0; it<firstNewTaxon; it++){
						Object o = newTaxa.getAssociatedObject(importSourceRef, it);
						if (o == null){
							newTaxa.setAssociatedObject(importSourceRef, it, newTaxa.getFile().getFileName());
						}
					}*/
				}
				int itNew = firstNewTaxon;
				for (int it=firstNewTaxon; it<newTaxa.getNumTaxa() && !(taxonName=parser.getNextToken()).equals(";"); it++) {
					if (!fuse){
						String w = newTaxa.checkNameLegality(it, it, taxonName);
						if (it != 0 &&  (it % 1000) == 999)
							CommandRecord.tick("Taxon " + (it+1) + " name read: \"" + taxonName + "\"");
						if (w!=null) {
							nameProblems = true;
							if (first) 
								file.setOpenAsUntitled(w);

							first = false;
						}
					}
					/**/else if (translationTable != null) { 
						// THIS SYSTEM had already existed as originalIndicesDupRead, but the latter seems not be working properly
						//here find if name exists already; if so, then record which existing taxon and store as ORIGINDEX
						int wT = newTaxa.whichTaxonNumber(taxonName, false);  //use reverse order lookup in case newly added taxa with identical names as previous
						if (wT >= 0)
							translationTable.setValue(it-firstNewTaxon, wT);
						else 
							translationTable.setValue(it-firstNewTaxon, itNew++);

					}/**/
					newTaxa.setTaxonNameNoWarnNoNotify(it, taxonName);
					/*if (fuse){
						newTaxa.setAssociatedObject(importSourceRef, it, file.getFileName());
					}*/

				}
				/**/	if (fuse) {
					newTaxa.attach(translationTable);

				}
				/**/
				CommandRecord.tick("TAXLABELS statement read");
			}
			else if (commandName.equalsIgnoreCase("IDS")) {
				String cN = parser.getNextToken();
				int taxNumber = firstNewTaxon;
				while (cN != null && !cN.equals(";") ) {
					if (!StringUtil.blank(cN))
						newTaxa.setUniqueID(taxNumber, cN);
					taxNumber++;
					cN = parser.getNextToken();
				}
				CommandRecord.tick("IDS statement read");
			}
			else if (commandName.equalsIgnoreCase("BLOCKID")) {
				String cN = parser.getNextToken();
				if (cN != null && !cN.equals(";")){
					if (!StringUtil.blank(cN))
						blockID = cN;
					cN = parser.getNextToken();
				}
				if (newTaxa != null)
					newTaxa.setUniqueID(blockID);
			}
			else if (!(commandName.equalsIgnoreCase("BEGIN") || commandName.equalsIgnoreCase("END")  || commandName.equalsIgnoreCase("ENDBLOCK")))  {
				unrec.addElement(s); //store unrecognized commands because they can't be stored until issue of duplicate taxa blocks is resolved
				unrecName.addElement(commandName);
			}
		}
		CommandRecord.tick("TAXA block read; checking");
		if (newTaxa!=null) {

			Taxa eT = existsInOtherFile(newTaxa, file, true, false);
			if (eT !=null){  //block of taxa with same names found
				boolean autoDelete = false;
				String ftn = "";
				String helpString ="";
				if (newTaxa.getTaxon(0)!=null)
					ftn = "; Name of first taxon: " + newTaxa.getTaxon(0).getName();
				Taxa eTOrder = existsInOtherFile(newTaxa, file, false); //this is considering taxon order; if null then order must differ

				String w = ("There is taxa block that appears to be a duplicate.  \n\nFirst block: \"" + eT.getName() + "\"; \nSecond block: \"" + newTaxa.getName() + "\"" + ftn + "."); 
				helpString = "In deleting the second taxon block, any other information (e.g., character matrices) associated with that second block will be reattached to the first block";
				String button = "Delete";
				//if unique block IDs match AND order same, then delete this block and proceed without querying user
				//if unique block IDs match and order different, then query with warning that ordering will change

				if (newTaxa.getUniqueID()!= null && eT.getUniqueID() != null && eT.getUniqueID().equals(newTaxa.getUniqueID())){  //same id's; names must be same
					w = "This file has a taxa block with same ID and taxon names and is thus a duplicate.  First block: \"" + eT.getName() + "\"; Second block: \"" + newTaxa.getName() + "\"" + ftn + ". ";
					if (eTOrder != null)
						autoDelete = true;
				}
				if (eTOrder == null) {
					w += " The two taxa blocks have a different ordering of taxa.  If you delete the second block, the ordering of the first block will be maintained.";
				}


				if (!autoDelete && (alerts.getValue() && !MesquiteThread.isScripting())) {
					if (AlertDialog.query(containerOfModule(), "Duplicate taxa block?", w + " \n\nDo you want to delete the second block?", button, "Keep", 1, helpString)){
						if (eTOrder == null)
							setOrder(eT, newTaxa);
						newTaxa.dispose();
						file.setCurrentTaxa(eT);
						if (eTOrder == null) 
							eT.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
						return new TaxaBlock(null, null);
					}
				}
				else {
					discreetAlert(w + "\nOnly the first block will be kept.  Any other information (e.g., character matrices) associated with that second block will be reattached to the first block. " +
							" If you are reading a linked file and do not intend to delete this taxon block from the linked file, then do not save the file!");
					if (eTOrder == null)
						setOrder(eT, newTaxa);
					newTaxa.dispose();
					file.setCurrentTaxa(eT);
					if (eTOrder == null) 
						eT.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
					return new TaxaBlock(null, null);
				}
			}
			if (eT == null){
				eT = existsInOtherFileByID(newTaxa, file); 
				if (eT != null) { //taxa block exists according to id, but has either different names or different number of taxa

					if (eT.getNumTaxa() == newTaxa.getNumTaxa()){//same number of Taxa

					}
				}
				//taxa claimed to be same ID but names have changed
			}


			if (t==null) {
				if (fuse && merging)
					t = newTaxa.getNexusBlock();
				else
					t = newTaxa.addToFile(file, getProject(), this);
			}
			file.setCurrentTaxa(newTaxa);
			if (unrec.size()>0){
				for (int i = 0; i<unrec.size(); i++){
					String commandName = (String)unrecName.elementAt(i);
					s = (String)unrec.elementAt(i);
					readUnrecognizedCommand(file, t, name, block, commandName, s, blockComments, null);
				}
			}
			if (almostExistsInOtherFile(newTaxa, file)) {
				String ftn = "";
				if (newTaxa.getTaxon(0)!=null)
					ftn = "; name of first taxon: " + newTaxa.getTaxon(0).getName();
				message("A taxa block (\"" + newTaxa.getName() + "\"" + ftn + ") has been found that has more than 80% overlap in taxon names with another taxa block.  If you had intended them to be the same, review them to see that they contain the same number of taxa, and that their taxon names are identical.");

			}

			if (!fuse) 
				newTaxa.setName(title);
		}
		if (newTaxa!=null  && (nameProblems || fuse)){
			String d = newTaxa.hasDuplicateNames();
			if (d !=null){
				if (fuse && !hadDuplicateNames){
					if (AlertDialog.query(containerOfModule(), "Duplicated taxa", "Some taxon names in the file being read are the same as some already in the project for the taxa block \"" + newTaxa.getName() + "\". Do you want to merge these taxa? \n\n(duplicated names: " + d + ").  WARNING: if these taxa have data in matrices that you are fusing to existing matrices, then the taxon will take on the newly fused values. (mt)")){
						IntegerArray originalIndices = new IntegerArray(newTaxa.getNumTaxa());
						originalIndices.setNameReference(NameReference.getNameReference("originalIndicesDupRead"));
						newTaxa.attach(originalIndices);

						newTaxa.deleteTaxaWithDuplicateNames();

						originalIndices.deleteParts(0, firstNewTaxon);

					}
				}
				else {
					message("A taxa block has duplicated taxon names.  You should fix this.  (duplicated names: " + d + ")  This message can be viewed again in the log.");
					MesquiteTrunk.errorReportedDuringRun = true;
				}
			}
		}
		if (!fuse && newTaxa != null && blockComments!=null && blockComments.length()>0)
			newTaxa.setAnnotation(blockComments.toString(), false);
		if (fuse)
			newTaxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));

		return t;
	}

	void setOrder(Taxa taxa, Taxa oTaxa){  //sets order of taxa to be same as oTaxa
		String[][] storedOrder = new String[taxa.getNumTaxa()][2];
		for (int it = 0; it< storedOrder.length; it++){
			storedOrder[it][0] = taxa.getUniqueID(it);
			storedOrder[it][1] = taxa.getTaxonName(it);
		}
		taxa.attach(new ObjectContainer("restOrderAftRead", storedOrder));
		boolean anyIDMoves = false;
		for (int ic = 0; ic<taxa.getNumTaxa(); ic++){ //first reorder by id's
			String id = oTaxa.getUniqueID(ic);
			if (!StringUtil.blank(id)){
				int whichTaxon = taxa.findByUniqueID(id);
				if (whichTaxon >=0 && whichTaxon!= ic) {
					taxa.swapParts(ic, whichTaxon);
					anyIDMoves = true;
					//logln("Taxon " + (whichTaxon+1) + " moved into position " + (ic+1) + " (a)");
				}
			}
		}
		if (!anyIDMoves){
			for (int ic = 0; ic<taxa.getNumTaxa(); ic++){ //first reorder by id's
				int whichTaxon = taxa.whichTaxonNumber(oTaxa.getTaxonName(ic));
				if (whichTaxon >=0 && whichTaxon!= ic) {
					taxa.swapParts(ic, whichTaxon);
					//logln("Taxon " + (whichTaxon+1) + " moved into position " + (ic+1) + " (b)");
				}
			}
		}
	}
	void restoreOrderIfNeeded(Taxa taxa){  //sets order of taxa to be same as uniqueID string vector attached to Taxa
		Object obj = taxa.getAttachment("restOrderAftRead"); //if this exists, it had been temporary attached
		if (obj == null || !(obj instanceof ObjectContainer))
			return;

		String[][] storedOrder = (String[][])((ObjectContainer)obj).getObject();
		boolean anyIDMoves = false;
		for (int ic = 0; ic<taxa.getNumTaxa(); ic++){ //first reorder by id's
			String id = storedOrder[ic][0];
			if (!StringUtil.blank(id)){
				int whichTaxon = taxa.findByUniqueID(id);
				if (whichTaxon >=0 && whichTaxon!= ic) {
					taxa.swapParts(ic, whichTaxon);
					anyIDMoves = true;
					//logln("Taxon " + (whichTaxon+1) + " moved into position " + (ic+1) + " (a)");
				}
			}
		}
		if (!anyIDMoves){
			for (int ic = 0; ic<taxa.getNumTaxa(); ic++){ //first reorder by id's
				int whichTaxon = taxa.whichTaxonNumber(storedOrder[ic][1]);
				if (whichTaxon >=0 && whichTaxon!= ic) {
					taxa.swapParts(ic, whichTaxon);
					//logln("Taxon " + (whichTaxon+1) + " moved into position " + (ic+1) + " (b)");
				}
			}
		}
		taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
	}
	/** returns the Taxa object that is already contained in the project that matches the input Taxa in names, if there exists such a Taxa object in the project*/
	/*.................................................................................................................*/
	Taxa existsInOtherFile (Taxa taxa, MesquiteFile file, boolean ignoreOrder, boolean allowInputTaxaToBeSubset){
		if (allowInputTaxaToBeSubset) 
			return supersetExistsInOtherFile(taxa,file,ignoreOrder);
		else
			return existsInOtherFile(taxa,file,ignoreOrder);
	}
	/** returns the Taxa object that is already contained in the project that matches the input Taxa in names, if there exists such a Taxa object in the project*/
	/*.................................................................................................................*/
	Taxa existsInOtherFile (Taxa taxa, MesquiteFile file, boolean ignoreOrder){
		for (int i =0; i<getProject().getNumberTaxas(); i++){
			Taxa t = getProject().getTaxa(i);
			if (t.getFile() != file &&  t != taxa && t.equals(taxa, true, ignoreOrder)) {
				return t;
			}
		}
		return null;
	}
	/** returns the Taxa object that is already contained in the project that matches the input Taxa in names, if there exists such a Taxa object in the project.  The input Taxa can have missing taxa.*/
	/*.................................................................................................................*/
	Taxa supersetExistsInOtherFile (Taxa taxa, MesquiteFile file, boolean ignoreOrder){
		for (int i =0; i<getProject().getNumberTaxas(); i++){
			Taxa t = getProject().getTaxa(i);
			if (t.getFile() != file &&  t != taxa && t.contains(taxa, true, ignoreOrder)) {
				return t;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	Taxa existsInOtherFileByID (Taxa taxa, MesquiteFile file){

		for (int i =0; i<getProject().getNumberTaxas(); i++){
			Taxa t = getProject().getTaxa(i);
			if (t.getFile() != file && (t.getUniqueID()!= null && taxa.getUniqueID()!=null  && taxa.getUniqueID().equals(t.getUniqueID()))) {
				return t;
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	boolean almostExistsInOtherFile (Taxa taxa, MesquiteFile file){
		if (taxa == null || taxa.getNumTaxa()==0)
			return false;
		for (int i =0; i<getProject().getNumberTaxas(); i++){
			Taxa t = getProject().getTaxa(i);
			if (t!=taxa && t.getFile()!=file){
				if (t == null || t.getNumTaxa()==0)
					return false;
				double matches = 0.0;
				for (int it=0; it<t.getNumTaxa(); it++){
					if (t.getTaxonName(it).equals("Col.Ade.Bembidion_28S.orig")) {
						matches = matches + 0.0;
					}
					if (taxa.getTaxon(t.getTaxonName(it))!=null)
						matches+= 1.0;
					//else logln("unmatched: " + t.getTaxonName(it));
				}
				double avg = matches/t.getNumTaxa();
				double matches2 = 0.0;
				for (int it=0; it<taxa.getNumTaxa(); it++)
					if (t.getTaxon(taxa.getTaxonName(it))!=null)
						matches2+= 1.0;
					//else logln("unmatched: " + t.getTaxonName(it));
				double avg2 = matches2/t.getNumTaxa();
				if (MesquiteDouble.minimum( avg2, avg) > 0.8)
					return true;
			}
		}
		return false;
	}
	/*.................................................................................................................*/
	String fixDuplicateNames (Taxa taxa){
		String list = "";
		for (int i=0; i<taxa.getNumTaxa(); i++){
			String name = taxa.getTaxonName(i);
			for (int j=i+1; j<taxa.getNumTaxa(); j++){
				String name2 = taxa.getTaxonName(j);
				if (name!=null && name.equalsIgnoreCase(name2)) {
					String s = taxa.getUniqueName(name2);
					taxa.setTaxonName(j, s);
					list += "Taxon \"" + name2 + "\" changed to \"" + s + "\"\n";
				}
			}
		}
		return list;
	}
	/*.................................................................................................................*/
	String fixBlankNames (Taxa taxa){
		String list = "";
		for (int i=0; i<taxa.getNumTaxa(); i++){
			String name = taxa.getTaxonName(i);
			if (StringUtil.blank(name)) {
				name = taxa.getUniqueName("Unnamed Taxon");
				taxa.setTaxonName(i, name);
				list += "Unnamed Taxon \"" + (i+1) + "\" named as \"" + name + "\"\n";
			}
		}
		return list;
	}
	/*.................................................................................................................*/
	boolean hasBlankNames (Taxa taxa){
		for (int i=0; i<taxa.getNumTaxa(); i++){
			String name = taxa.getTaxonName(i);
			if (StringUtil.blank(name)) 
				return true;
		}
		return false;
	}
	void message(String s){
		if (alerts.getValue() && !MesquiteThread.isScripting())
			alert(s);
		else
			logln(s);
	}
	/*.................................................................................................................*/
	public String getTaxaBlock(Taxa taxa, TaxaBlock tB, MesquiteFile file){
		//check first for file ready to write.  In future should have general call to modules, but for now just check for duplicate taxon names
		if (hasBlankNames(taxa)){
			discreetAlert("The block of taxa being saved (" + taxa.getName() + ") has blank taxon names.  This will cause problems in saving and reading trees and other functions, and will be fixed (Summary: " +fixBlankNames(taxa) + ")");
		}
		CommandRecord.tick("Checking for duplicate taxon names");
		String d = taxa.hasDuplicateNames(true);
		if (d !=null){
			if (MesquiteThread.isScripting())
				logln("Summary of name changes:\n" + fixDuplicateNames(taxa));
			else if (AlertDialog.query(containerOfModule(), "Duplicate names!", "The block of taxa being saved (" + taxa.getName() + ") has duplicate taxon names.  This may cause problems in saving and reading trees and other functions.  Do you want Mesquite to fix this by generating unique names by suffixing a number to duplicate names?   (Duplicates: " +d + ")", "Fix", "Don't Fix"))
				alert("Summary of name changes:\n" + fixDuplicateNames(taxa));
		}

		if (file == null)
			file = taxa.getFile();
		if (file!=null && file.useDataBlocks) //removed 19Jan02 so as not to make minimal dependent on categorical: &&  file.getFileElements().size(mesquite.categ.lib.CategoricalData.class)>0
			return null;
		StringBuffer block = new StringBuffer();
		String end = StringUtil.lineEnding();
		block.append(end);
		CommandRecord.tick("Composing taxa block");
		block.append("BEGIN TAXA");
		if (taxa.getAnnotation()!=null) 
			block.append("[!" + StringUtil.tokenize(taxa.getAnnotation()) + "]");
		block.append(';');
		block.append(end);

		if (MesquiteFile.okToWriteTitleOfNEXUSBlock(file, taxa))
			block.append("\tTITLE " + StringUtil.tokenize(taxa.getName()) + ";" + end);
		int numTaxaWrite = taxa.getNumTaxa();
		if (file.writeOnlySelectedTaxa)
			numTaxaWrite = taxa.numberSelected();
		block.append("\tDIMENSIONS NTAX=" + numTaxaWrite + ";" + end + "\tTAXLABELS" + end + "\t\t");
		String taxonName = "";
		for (int it=0; it<taxa.getNumTaxa(); it++) {
			if (!file.writeOnlySelectedTaxa || taxa.getSelected(it)){
				taxonName = taxa.getTaxon(it).getName();
				if (taxonName!=null){
					if (file.useStandardizedTaxonNames)
						block.append("t" + it);
					else
						block.append(StringUtil.simplifyIfNeededForOutput(taxonName,file.simplifyNames) + " ");
				}
				else
					block.append(StringUtil.tokenize(" "));
			}
			CommandRecord.tick("Writing Taxon " + taxonName);
		}
		block.append(end + "\t;" + end);

		CommandRecord.tick("Writing IDs ");
		int last = lastID(taxa);
		if (!file.useSimplifiedNexus  && !file.useConservativeNexus && last>-1){
			block.append("\tIDS ");
			for (int it=0; it<= last; it++) {

				if (!file.writeOnlySelectedTaxa || taxa.getSelected(it)){
					String id = taxa.getUniqueID(it);
					if (StringUtil.blank(id))
						block.append(" _ ");
					else
						block.append( id + " ");
					CommandRecord.tick("Writing Taxon ID " + (it+1));
				}
			}
			block.append(";" + end);
		}
		CommandRecord.tick("Taxa block composed ");
		if (!file.useSimplifiedNexus  && !file.useConservativeNexus && !StringUtil.blank(taxa.getUniqueID()) && !NexusBlock.suppressNEXUSIDS)
			block.append("\tBLOCKID " + taxa.getUniqueID() + ";" + end);
		if (tB != null) block.append( tB.getUnrecognizedCommands()+ end);
		block.append("END;" + end+ end);
		return block.toString();
	}

	int lastID(Taxa taxa){
		for (int it=taxa.getNumTaxa()-1; it>=0; it--) {
			String id = taxa.getUniqueID(it);
			if (!StringUtil.blank(id))
				return it;
		}
		return -1;
	}
}


/* ======================================================================== */
class TaxaBlockTest extends NexusBlockTest  {
	public TaxaBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		if (blockName == null)
			return false;
		return blockName.equalsIgnoreCase("TAXA");
	}
}

/** An object of this kind can be returned by getNexusCommandTest that will be stored in the modulesinfo vector and used
to search for modules that can read a particular command in a particular block.  (Much as the NexusBlockObject.)*/
class TAXANexusCommandTest extends NexusCommandTest  {
	MesquiteInteger pos = new MesquiteInteger();
	/**returns whether or not the module can deal with command*/
	public boolean readsWritesCommand(String blockName, String commandName, String command){
		if (blockName.equalsIgnoreCase("NOTES")  && (commandName.equalsIgnoreCase("TAXA") || commandName.equalsIgnoreCase("TAXABITS")))
			return true;
		boolean b = (blockName.equalsIgnoreCase("NOTES")  && (commandName.equalsIgnoreCase("TEXT") || commandName.equalsIgnoreCase("SUPPLEMENTAL") || commandName.equalsIgnoreCase("SUT") |commandName.equalsIgnoreCase("INTEGER")));
		if (b){
			pos.setValue(0);
			String firstToken = ParseUtil.getFirstToken(command,  pos);

			String[][] subcommands  = ParseUtil.getSubcommands(command, pos);
			if (subcommands == null)
				return false;
			if (StringArray.indexOfIgnoreCase(subcommands, 0, "TAXON")<0 && StringArray.indexOfIgnoreCase(subcommands, 0, "T")<0)
				return false;
			if (StringArray.indexOfIgnoreCase(subcommands, 0, "CHARACTER")>=0 || StringArray.indexOfIgnoreCase(subcommands, 0, "C")>=0)
				return false;
			return true;
		}
		return false;
	} 
}

