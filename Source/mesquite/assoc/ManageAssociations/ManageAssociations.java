/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.assoc.ManageAssociations;
/*~~  */

import java.util.*;
import java.awt.*;
import java.io.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.assoc.lib.*;

/* ========================================================================  */
public class ManageAssociations extends AssociationsManager {
	ListableVector associationsVector; //establish listeners to all of the taxa
	ListableVector blocks;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		associationsVector = new ListableVector();
		blocks = new ListableVector();
		return true;
	}
 	public String getKeywords(){
 		return "genes species";
 	}
 	public void endJob(){
 		associationsVector.dispose(true);
 		blocks.dispose(true);
		super.endJob();
 	}
	public boolean isPrerelease(){
		return false;
	}

	public void elementsReordered(ListableVector v){
		if (v == associationsVector){
			NexusBlock.equalizeOrdering(v, getProject().getNexusBlocks());
		}
	}
	/*.................................................................................................................*/
	/** A method called immediately after the file has been read in.*/
	public void projectEstablished() {
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "New Association...", makeCommand("newAssociation",  this));
		MesquiteSubmenuSpec mss = getFileCoordinator().addSubmenu(MesquiteTrunk.treesMenu, "Edit Association", makeCommand("editAssociation",  this), associationsVector);
		getFileCoordinator().addMenuItem(MesquiteTrunk.treesMenu, "-", null);
		super.projectEstablished();
	}
	/*.................................................................................................................*/
	public MesquiteModule showElement(FileElement e){
		if (e instanceof TaxaAssociation){
			TaxaAssociation t = (TaxaAssociation)e;
			editInTaxonList(t, false);
			/*
			for (int i=0; i<blocks.size(); i++){
				AssociationsBlock ab = (AssociationsBlock)blocks.elementAt(i);
				if (ab.getAssociation()==t){
					edit(ab);
					break;
				}
			}*/
		}
		return null;
	}
	/*.................................................................................................................*/
	public void deleteElement(FileElement e){
		if (e instanceof TaxaAssociation){
			TaxaAssociation t = (TaxaAssociation)e;
			t.doom();
			getProject().removeFileElement(t);//must remove first, before disposing
			associationsVector.removeElement(t, false);
			for (int i=0; i<blocks.size(); i++){
				AssociationsBlock ab = (AssociationsBlock)blocks.elementAt(i);
				if (ab.getAssociation()==t){
					blocks.removeElement(ab, false);
					break;
				}
			}
			t.dispose();
		}
	}
	/*.................................................................................................................*/
	public int getNumberOfAssociations(Taxa taxaA, Taxa taxaB){
		int count=0;
		for (int i=0; i<associationsVector.size(); i++){
			TaxaAssociation association = (TaxaAssociation)associationsVector.elementAt(i);
			if ((association.getTaxa(0) == taxaA && association.getTaxa(1) == taxaB)||(association.getTaxa(0) == taxaB && association.getTaxa(1) == taxaA)) //TODO: use looser rules of taxa being equal, using taxa.equals()
				count++;
		}
		return count;
	}
	public int getNumberOfAssociations(Taxa taxa){
		int count=0;
		for (int i=0; i<associationsVector.size(); i++){
			TaxaAssociation association = (TaxaAssociation)associationsVector.elementAt(i);
			if (association.getTaxa(0) == taxa || association.getTaxa(1) == taxa) //TODO: use looser rules of taxa being equal, using taxa.equals()
				count++;
		}
		return count;
	}
	public int getNumberOfAssociations(){
		return associationsVector.size();
	}
	public TaxaAssociation getAssociation(Taxa taxaA, Taxa taxaB, int index){
		int count=0;
		for (int i=0; i<associationsVector.size(); i++){
			TaxaAssociation association = (TaxaAssociation)associationsVector.elementAt(i);
			if ((association.getTaxa(0) == taxaA && association.getTaxa(1) == taxaB)||(association.getTaxa(0) == taxaB && association.getTaxa(1) == taxaA)){ //TODO: use looser rules of taxa being equal, using taxa.equals()
				if (count == index)
					return association;
				count++;
			}
		}
		return null;
	}
	public int getWhichAssociation(Taxa taxa, TaxaAssociation assoc){
		int count=0;
		for (int i=0; i<associationsVector.size(); i++){
			TaxaAssociation association = (TaxaAssociation)associationsVector.elementAt(i);
			if (assoc == association)
				return count;
			if (association.getTaxa(0) == taxa || association.getTaxa(1) == taxa) {//TODO: use looser rules of taxa being equal, using taxa.equals()
				count++;
			}
		}
		return -1;
	}
	public TaxaAssociation getAssociation(Taxa taxa, int index){
		int count=0;
		for (int i=0; i<associationsVector.size(); i++){
			TaxaAssociation association = (TaxaAssociation)associationsVector.elementAt(i);
			if (association.getTaxa(0) == taxa || association.getTaxa(1) == taxa) {//TODO: use looser rules of taxa being equal, using taxa.equals()
				if (count == index)
					return association;
				count++;
			}
		}
		return null;
	}
	public TaxaAssociation getAssociation(int i){
		return (TaxaAssociation)associationsVector.elementAt(i);

	}
	public TaxaAssociation findAssociationByID(long id, Taxa taxa){
		for (int i=0; i<associationsVector.size(); i++){
			TaxaAssociation association = (TaxaAssociation)associationsVector.elementAt(i);

			if ((association.getTaxa(0) == taxa || association.getTaxa(1) == taxa) && association.getID() == id) {
				return association;
			}
		}
		return null;
	}
	public ListableVector getAssociationsVector(){
		return associationsVector;
	}
	/*.................................................................................................................*/
	public void elementDisposed(FileElement e){
		int place = associationsVector.indexOf(e);
		if (place>=0) {
			//review all current associations; if taxa included don't exist in project, then remove listener from taxa
			associationsVector.removeElement(e, true);
			blocks.removeElement(blocks.elementAt(place), false);
		}
	}
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		int code = Notification.getCode(notification);
		int[] parameters = Notification.getParameters(notification);
		if (obj instanceof Taxa){
			if (code==MesquiteListener.PARTS_CHANGED) {
				for (int i = 0; i<associationsVector.size(); i++){
					TaxaAssociation assoc = (TaxaAssociation)associationsVector.elementAt(i);
					if (obj == assoc.getTaxa(0) || obj == assoc.getTaxa(1))
						assoc.resetTaxaAfterChange();

				}
			}
			else if (code==MesquiteListener.PARTS_ADDED) {
				for (int i = 0; i<associationsVector.size(); i++){
					TaxaAssociation assoc = (TaxaAssociation)associationsVector.elementAt(i);
					if (obj == assoc.getTaxa(0) || obj == assoc.getTaxa(1))
						assoc.resetTaxaAfterChange();

				}
			}
			else if (code==MesquiteListener.PARTS_DELETED) {
				for (int i = 0; i<associationsVector.size(); i++){
					TaxaAssociation assoc = (TaxaAssociation)associationsVector.elementAt(i);
					if (obj == assoc.getTaxa(0) || obj == assoc.getTaxa(1))
						assoc.resetTaxaAfterChange();

				}
			}
			else if (code==MesquiteListener.PARTS_MOVED) {
			}
		}
	}
	/** passes which object was disposed*/
	public void disposing(Object obj){
		if (obj instanceof Taxa){
			//delete associations involving those taxa
			for (int i = 0; i<associationsVector.size(); i++){
				TaxaAssociation assoc = (TaxaAssociation)associationsVector.elementAt(i);
				if (obj == assoc.getTaxa(0) || obj == assoc.getTaxa(1))
					deleteElement((FileElement)assoc);

			}
		}
	}
	/** Asks whether it's ok to delete the object as far as the listener is concerned (e.g., is it in use?)*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}
	/*.................................................................................................................*/
	public NexusBlock elementAdded(FileElement e){
		if (e==null || !(e instanceof TaxaAssociation))
			return null;
		TaxaAssociation assoc = (TaxaAssociation)e;
		NexusBlock nb = findNEXUSBlock(assoc);
		if (nb==null) {
			AssociationsBlock t = new AssociationsBlock(assoc.getFile(), this);
			Taxa tax = assoc.getTaxa(0);
			if (tax!=null && !tax.amIListening(this))
				tax.addListener(this);
			tax = assoc.getTaxa(1);
			if (tax!=null && !tax.amIListening(this))
				tax.addListener(this);
			t.setAssociation(assoc);
			addNEXUSBlock(t);
			resetAllMenuBars();
			if (blocks.indexOf(t)<0)
				blocks.addElement(t, false);
			if (associationsVector.indexOf(assoc)<0)
				associationsVector.addElement(assoc, false);
			return t;
		}
		else return nb;
	}
	/*.................................................................................................................*/
	public NexusBlockTest getNexusBlockTest(){ return new AssociationBlockTest();}
	/*.................................................................................................................*/
	/* public Snapshot getSnapshot(MesquiteFile file) {
   	 	if (editor!=null && blocks !=null && editor.getCurrentBlock()!=null) {
	   	 	Snapshot temp = new Snapshot();
	   	 	temp.addLine("editAssociation " + blocks.indexOf(editor.getCurrentBlock()), editor);
	 	 	return temp;
 	 	}
 	 	else return null;
  	 }*/
	MesquiteInteger pos = new MesquiteInteger(0);

	/*.................................................................................................................*/
	private Object editInTaxonList(TaxaAssociation toBeEdited, boolean useFirst){
		if (toBeEdited == null)
			return null;
		TaxaManager manageTaxa = (TaxaManager)findElementManager(Taxa.class);
		int whichTaxa = 0;
		if (!useFirst && !MesquiteThread.isScripting()){
			String f = toBeEdited.getTaxa(0).getName();
			String s = toBeEdited.getTaxa(1).getName();
			String fString = f;
			String sString = s;
			if (fString.length()>20)
				fString = fString.substring(0, 19);
			if (sString.length()>20)
				sString = sString.substring(0, 19);
			if (fString.equalsIgnoreCase(sString)){
				fString = "First Block";
				sString = "Second Block";
			}
			
			boolean t = AlertDialog.query(containerOfModule(), "Which taxa?", "This is an association between a first block of taxa:\n\n\"" + f + "\"\n\n and a second block of taxa:\n\n\"" + s + "\"\n\nFrom the perspective of which block of taxa do you want to edit the association?", fString, sString, -1);
			if (t)
				whichTaxa = 0;
			else
				whichTaxa = 1;
		}
		MesquiteModule list = manageTaxa.getListOfTaxaModule(toBeEdited.getTaxa(whichTaxa), true);
		


		if (list == null) 
			return null;
		//here ask each list imployee if they have this assocaition shown; otherwise finish script
		EmployeeVector e = list.getEmployeeVector();
		if (e != null){
			for (int i = 0; i< e.size(); i++){
				if (e.elementAt(i) instanceof mesquite.assoc.TaxonListAssoc.TaxonListAssoc){
					mesquite.assoc.TaxonListAssoc.TaxonListAssoc t = (mesquite.assoc.TaxonListAssoc.TaxonListAssoc)e.elementAt(i);
					if (t.isShowing(toBeEdited))
						return toBeEdited;
				}
			}
		}
		Puppeteer p = new Puppeteer(this);
		MesquiteInteger pos = new MesquiteInteger(0);

		String commands =  "getWindow; tell It; setSize 680 400; newAssistant  #mesquite.assoc.TaxonListAssoc.TaxonListAssoc; tell It;";
		commands +=  "getEmployee #mesquite.assoc.StoredAssociations.StoredAssociations; tell It; setCurrentAssociationID ";
		//here put number of this taxa assoc
		commands +=  toBeEdited.getID();
		commands +=  "; endTell;";
		commands +=  "endTell; endTell;";
		pos.setValue(0);
		CommandRecord cRecord = new CommandRecord(true);
		CommandRecord prevR = MesquiteThread.getCurrentCommandRecord();
		MesquiteThread.setCurrentCommandRecord(cRecord);
		
		p.execute(list, commands, pos, "", false);
		MesquiteThread.setCurrentCommandRecord(prevR);
		return toBeEdited;
	}
	/*.................................................................................................................*/
	private Object edit(AssociationsBlock toBeEdited){
		if (toBeEdited == null)
			return null;
		String s = toBeEdited.getText();
		String newBlock = MesquiteString.queryMultiLineString(containerOfModule(), "Edit Assocation", "Edit TaxaAssociation block \"" + toBeEdited.getAssociation().getName() + "\"", s, 18, false, true);
		if (newBlock!=null)
			toBeEdited.setText(newBlock);
		return null;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Calls up the association editor window", "[number of association block to edit]", commandName, "editAssociation")) {
			//find which block first
			int which = MesquiteInteger.fromFirstToken(arguments, pos);
			if (!MesquiteInteger.isCombinable(which) || which>= blocks.size())
				return null;
			AssociationsBlock toBeEdited = (AssociationsBlock)blocks.elementAt(which);
			if (toBeEdited == null)
				return null;
			return editInTaxonList(toBeEdited.getAssociation(), false);
		}
		else if (checker.compare(this.getClass(), "Shows a dialog box with an example association block", null, commandName, "exampleAssociation")) {
			String lin = StringUtil.lineEnding();
			String example = "BEGIN TaxaAssociation;"+lin+"TITLE genes_in_species;"+lin+"TAXA  species ,  genes;"+lin+"ASSOCIATES"+lin+"	speciesA /  geneA1 geneA2 geneA3 , "+lin+"	speciesB /  geneB1 geneB2, "+lin+"	speciesC /  geneC1 geneC2 geneC3  geneC4, "+lin+";"+lin+"END;";
			AlertDialog.bigNotice(containerOfModule(), "Example Association block", example);
		}
		else if (checker.compare(this.getClass(), "Creates a new Association block (for scripting)", "[number of taxa A][number of taxa B][name of association]", commandName, "makeAssociation")) {
			MesquiteProject project = getProject();
			if (project.getNumberTaxas()==1){
				discreetAlert( "You can't create an Association between two sets of taxa if there is only one set of taxa available.  Please make a second set of taxa.");
				return null;
			}
			MesquiteInteger io = new MesquiteInteger(0);
			int a = MesquiteInteger.fromString(arguments, io);
			int b = MesquiteInteger.fromString(arguments, io);
			String name = ParseUtil.getToken(arguments, io);

			if (!MesquiteInteger.isCombinable(a) || !MesquiteInteger.isCombinable(b))
				return null;
			Taxa taxaA = project.getTaxa(a);
			if (taxaA == null)
				return null;
			Taxa taxaB = project.getTaxa(b);
			if (taxaA == null)
				return null;
			MesquiteFile file=project.getHomeFile();

			if (StringUtil.blank(name))
				name=associationsVector.getUniqueName("Taxa Association");
			TaxaAssociation association = new TaxaAssociation();
			associationsVector.addElement(association, false);
			association.setTaxa(taxaA, 0);
			association.setTaxa(taxaB, 1);
			association.setName(name);
			association.setAssociation(taxaA.getTaxon(0), taxaB.getTaxon(0), true);
			AssociationsBlock toBeEdited = (AssociationsBlock)association.addToFile(file, project, this); 
			if (blocks.indexOf(toBeEdited)<0)
				blocks.addElement(toBeEdited, false);
			resetAllMenuBars();
			return association;
		}
		else if (checker.compare(this.getClass(), "Creates a new Association block", null, commandName, "newAssociation")) {
			
			TaxaAssociation association = makeNewAssociation(null, null);
			if (association == null)
				return null;
			return editInTaxonList(association, true);
		}

		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}

	public TaxaAssociation makeNewAssociation(Taxa taxaA, Taxa taxaB){
		/* get taxa A, taxa B */
		MesquiteProject project = getProject();
		if (project.getNumberTaxas()==1){
			discreetAlert( "You can't create an Association between two sets of taxa if there is only one set of taxa available.  Please make a second set of taxa.");
			return null;
		}
		ListableVector taxas = project.getTaxas();
		String helpString = "If you are interested in building this association between two sets of  taxa to reflect an association between species trees and gene trees, host and parasite, and the like, ";
		helpString += "then we suggest you choose the first set of taxa to be the containing taxa (e.g., species, host), and the second set to be the contained taxa (e.g., gene, parasite). ";
		
		helpString += "\n\nIf, on the other hand, you are building the association between sets of taxa to link two matrices (e.g., two matrices from similar taxa but for different genes), ";
		helpString += "then we suggest you choose the first set of taxa to be the master one, containing the 'official' names for the taxa.";
		if (taxaA == null) 
			taxaA = (Taxa)ListDialog.queryList(containerOfModule(), "Select taxa", "Select first block of taxa for the association, e.g. the containing or master taxa.  If you are analyzing gene trees within species trees, select here the species taxa block.", helpString, taxas, 0);
		if (taxaA == null)
			return null;
		if (taxaB == null){
			if (project.getNumberTaxas()>2) {
				Listable[] others = new Listable[project.getNumberTaxas()-1];
				int count =0;
				for (int i=0; i<project.getNumberTaxas(); i++){
					Taxa t = project.getTaxa(i);
					if (t!= taxaA)
						others[count++] = t;
				}
				taxaB = (Taxa)ListDialog.queryList(containerOfModule(), "Select taxa", "Select second block of taxa for the association, e.g. the contained taxa.  If you are analyzing gene trees within species trees, select here the genes taxa block.", helpString, others, 0);
				if (taxaB==null)
					return null;
			}
			else {
				int a = project.getTaxaNumber(taxaA);
				if (a==0)
					taxaB = project.getTaxa(1);
				else
					taxaB = project.getTaxa(0);
			}
		}
		MesquiteFile file=chooseFile( taxaA,taxaB);

		if (taxaA==null ||taxaB==null || file == null)
			return null;
		String name = MesquiteString.queryString(containerOfModule(), "Name of Association", "Association", associationsVector.getUniqueName("Taxa Association"));
		if (name==null)
			return null;
		if (StringUtil.blank(name))
			name=associationsVector.getUniqueName("Taxa Association");
		resetAllMenuBars();
		/* user choose title */
		TaxaAssociation association = new TaxaAssociation();
		associationsVector.addElement(association, false);
		association.setTaxa(taxaA, 0);
		association.setTaxa(taxaB, 1);
		association.setName(name);
		AssociationsBlock toBeEdited = (AssociationsBlock)association.addToFile(file, project, this); 
		if (blocks.indexOf(toBeEdited)<0)
			blocks.addElement(toBeEdited, false);
		return association;
	}
	/*.................................................................................................................*/
	public MesquiteFile chooseFile( Taxa taxa0, Taxa taxa1){  //changed 13 Dec 01 to specify data so as to do check on which file can be
		MesquiteFile file=null;
		if (getProject().getNumberLinkedFiles()==1)
			file = getProject().getHomeFile();
		else {
			Listable[] files = getProject().getFiles().getElementArray();
			if (files.length >1) {

				int count = 0;
				int taxaFound = 0;
				for (int i=0; i<files.length; i++) {
					if (files[i] == taxa0.getFile())
						taxaFound++;
					if (files[i] == taxa1.getFile())
						taxaFound++;
					if (taxaFound==2)
						count++;
				}
				if (count!=files.length){
					Listable[] legalFiles = new Listable[count];
					count = 0;
					taxaFound = 0;
					for (int i=0; i<files.length; i++) {
						if (files[i] == taxa0.getFile())
							taxaFound++;
						if (files[i] == taxa1.getFile())
							taxaFound++;
						if (taxaFound==2) {
							legalFiles[count] = files[i];
							count++;
						}
					}
					files = legalFiles;
				}

			}
			if (files.length == 1 || MesquiteThread.isScripting())
				return (MesquiteFile)files[0];
			file = (MesquiteFile)ListDialog.queryList(containerOfModule(), "Select file", "Select file to which to add the new association",MesquiteString.helpString,  files, 0);
		}
		return file;
	}

	/*.................................................................................................................*/
	public NexusBlock readNexusBlock(MesquiteFile file, String name, FileBlock block, StringBuffer blockComments, String fileReadingArguments){
		boolean fuse = parser.hasFileReadingArgument(file.fileReadingArguments, "fuseTaxaCharBlocks");
		if (fuse)
			return null;
		return processText(file, name, block.toString(), block, null, blockComments);
	}
	/*.................................................................................................................*/
	public NexusBlock processText(MesquiteFile file, String name, String blockAsString, FileBlock block, NexusBlock current, StringBuffer blockComments){
		Parser commandParser = new Parser();
		commandParser.setString(blockAsString);
		MesquiteInteger startCharC = new MesquiteInteger(0);
		TaxaAssociation association=null;
		Taxa taxaA=null;
		Taxa taxaB=null;
		String title=null;
		String s;
		while (!StringUtil.blank(s=commandParser.getNextCommand(startCharC))) {
			String commandName = parser.getFirstToken(s);
			if (commandName.equalsIgnoreCase("TAXA")) {
				String nameTaxaA = parser.getNextToken();
				if ("=".equals(nameTaxaA))
					nameTaxaA = parser.getNextToken();
				taxaA = getProject().getTaxaLastFirst(nameTaxaA);
				if (taxaA == null) 
					return null;
				parser.getNextToken(); //eating ","
				String nameTaxaB = parser.getNextToken();
				taxaB = getProject().getTaxaLastFirst(nameTaxaB);
				if (taxaB == null) 
					return null;

				if (current==null){
					association = new TaxaAssociation();
					associationsVector.addElement(association, false);
				}
				else
					association = ((AssociationsBlock)current).getAssociation();
				association.setTaxa(taxaA, 0);
				association.setTaxa(taxaB, 1);
				if (title!=null)
					association.setName(title);
				if (current ==null) {
					current = association.addToFile(file, getProject(), this);  
					if (blocks.indexOf(current)<0)
						blocks.addElement(current, false);
				}
			}
			else if (commandName.equalsIgnoreCase("TITLE")) {
				title = parser.getTokenNumber(2);
			}
			else if (commandName.equalsIgnoreCase("ASSOCIATES") && association!=null && taxaA!=null && taxaB!=null) {
				String taxonName;
				boolean doneTaxa = false;
				int[] whichA = new int[taxaA.getNumTaxa()];
				for (int i=0; i<whichA.length; i++)
					whichA[i]=-1;
				while (!doneTaxa){
					taxonName=parser.getNextToken();
					if (StringUtil.blank(taxonName) || ";".equals(taxonName))
						doneTaxa = true;
					else {
						//find which taxon is taxonName
						int iFound = 0;
						int wA = -1;
						do {
							if ("/".equals(taxonName))
								wA = -1;
							else {
								wA = taxaA.whichTaxonNumber(taxonName);
								if (wA<0) {
									String sw = ("Illegal taxon name in TAXAASSOCIATION block: " + taxonName + " for taxa set " + taxaA.getName());
									if (file ==null)
										MesquiteMessage.warnProgrammer(sw);
									else
										file.setOpenAsUntitled(sw);
								}
								else {
									whichA[iFound] = wA;
									iFound++;
								}
								taxonName = parser.getNextToken();//eating up next
							}
						} while (wA >=0);

						String associateName;
						boolean doneAssociates = false;
						while (!doneAssociates){
							associateName=parser.getNextToken();
							if (StringUtil.blank(associateName) || ",".equals(associateName) || ";".equals(associateName)){
								doneAssociates = true;
								if (";".equals(associateName))
									doneTaxa = true;
							}
							else {
								int whichB = taxaB.whichTaxonNumber(associateName);
								if (whichB<0) {
									String sw = ("Illegal taxon name in TAXAASSOCIATIONS block: " + associateName + " for taxa set " + taxaB.getName());
									if (file ==null)
										MesquiteMessage.warnProgrammer(sw);
									else
										file.setOpenAsUntitled(sw);
								}
								for (int i =0; i<whichA.length && whichA[i] >= 0; i++){
									if (whichA[i] != -1 && whichB != -1 && taxaA.getTaxon(whichA[i])!=null && taxaB.getTaxon(whichB)!=null) {
										association.setAssociation(taxaA.getTaxon(whichA[i]), taxaB.getTaxon(whichB), true);
									}
								}
							}
						}
					}
					// read /
					//read associates until comma
				}
			}
			else if (current!=null && !(commandName.equalsIgnoreCase("BEGIN") || commandName.equalsIgnoreCase("END")  || commandName.equalsIgnoreCase("ENDBLOCK"))) 
				readUnrecognizedCommand(file, current, name, block, commandName, s, blockComments, null);
		}
		//a.setAssociation(association);
		return current;
	}
	/*.................................................................................................................*/
	public String getAssocBlock(TaxaAssociation association, AssociationsBlock aB){
		String block = "";
		block+="BEGIN TaxaAssociation;" + StringUtil.lineEnding() + "TITLE " + StringUtil.tokenize(association.getName()) + ";" + StringUtil.lineEnding();
		Taxa taxaA =association.getTaxa(0);
		Taxa taxaB =association.getTaxa(1);
		block+="TAXA " + StringUtil.tokenize(taxaA.getName()) +  " ,  " + StringUtil.tokenize(taxaB.getName())+ ";"  + StringUtil.lineEnding();
		block+="ASSOCIATES " + StringUtil.lineEnding();
		String assoc = "";
		boolean first = true;
		for (int a=0; a<taxaA.getNumTaxa(); a++) {
			Taxon taxonA = taxaA.getTaxon(a);
			if (association.getNumAssociates(taxonA)>0) {
				if (!first)
					assoc += " , ";
				first = false;
				assoc += StringUtil.lineEnding();

				assoc += "\t" + StringUtil.tokenize(taxonA.getName()) + " / ";
				for (int b = 0; b<taxaB.getNumTaxa(); b++) {
					Taxon taxonB = taxaB.getTaxon(b);
					if (association.getAssociation(taxonA, taxonB))
						assoc += " " + StringUtil.tokenize(taxonB.getName());

				}
			}
		}
		assoc += StringUtil.lineEnding();
		if (assoc.length() == StringUtil.lineEnding().length()){
			assoc += "[Adjust the following specification to indicate which taxa from the second block of taxa are associated which which taxa from the first block.  Note that as listed by default, all taxa from the second block are associated with the first taxon of the first block.]" + StringUtil.lineEnding() + StringUtil.lineEnding();
			for (int a=0; a<taxaA.getNumTaxa(); a++) {
				Taxon taxonA = taxaA.getTaxon(a);
				if (a>0)
					assoc += " , " + StringUtil.lineEnding() ;
				assoc += "\t" + StringUtil.tokenize(taxonA.getName()) + " / ";
				if (a==0) 
					for (int b = 0; b<taxaB.getNumTaxa(); b++) {
						Taxon taxonB = taxaB.getTaxon(b);
						assoc += " " + StringUtil.tokenize(taxonB.getName());

					}
				//assoc +=  StringUtil.lineEnding();
			}
			assoc += StringUtil.lineEnding();
		}

		block += assoc;
		block += StringUtil.lineEnding()+ ";";
		if (aB != null) block += aB.getUnrecognizedCommands() + StringUtil.lineEnding();
		block += StringUtil.lineEnding() + "END;" + StringUtil.lineEnding()+ StringUtil.lineEnding();
		return block;
	}

	/*.................................................................................................................*/
	public String getName() {
		return "Manage TaxaAssociation blocks";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Manages TaxaAssociation blocks in NEXUS file." ;
	}
}

class AssociationsBlock extends NexusBlockEditableRaw {  
	TaxaAssociation association;
	ManageAssociations owner;
	public AssociationsBlock(MesquiteFile f, ManageAssociations mb){
		super(f, mb);
		owner = mb;
	}
	public void setAssociation(TaxaAssociation a) {
		association = a;
	}
	public boolean contains(FileElement e) {
		return e!=null && association == e;
	}
	/** DOCUMENT */
	public String getText(){
		return getNEXUSBlock();
	}
	/** DOCUMENT */
	public void setText(String n){
		owner.processText(getFile(), getName(), n, null, this, null);
		association.notifyListeners(this, new Notification(MesquiteListener.UNKNOWN));
	}

	public TaxaAssociation getAssociation() {
		return association;
	}
	public boolean mustBeAfter(NexusBlock block){ 
		if (block==null)
			return false;
		if (association!=null && block instanceof TaxaBlock) {
			return association.getTaxa(0) == ((TaxaBlock)block).getTaxa() || association.getTaxa(1) == ((TaxaBlock)block).getTaxa();
		}
		return (block.getBlockName().equalsIgnoreCase("TAXA"));

	}
	public String getBlockName(){
		return "TaxaAssociation";
	}
	public String getName(){
		return "Taxa associations block";
	}
	/** Returns the NEXUS block as a string for writing into the file*/
	public String getNEXUSBlock(){
		if (getEditor()!=null)
			getEditor().recordBlock(this);
		if (association!=null)
			return owner.getAssocBlock(association, this);
		else 
			return null;
	}
}

/* ======================================================================== */
class AssociationBlockTest extends NexusBlockTest  {
	public AssociationBlockTest () {
	}
	public  boolean readsWritesBlock(String blockName, FileBlock block){ //returns whether or not can deal with block
		return blockName.equalsIgnoreCase("ASSOCIATION") || blockName.equalsIgnoreCase("TAXAASSOCIATION");
	}
}


