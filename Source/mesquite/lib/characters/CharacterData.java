/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison.

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.characters; 

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.*;
import java.util.zip.*;

import javax.swing.text.JTextComponent;

import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.MolecularData;
import mesquite.lib.duties.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lists.lib.ListModule;
import mesquite.lib.table.*;

/* last documented: April 2003 */
/* ======================================================================== */
/**An object of this class represents a Characters block within a NEXUS file.  It is basically a data matrix
plus additional information about the characters, symbols, etc. Includes methods to add, delete and move characters. 
Note: methods to adjust taxa are in Taxa, but they need to appear here also so that matrices may be adjusted
 as needed (to delete rows).<p>
This is subclassed separately for categorical and continuous data; it is there that the matrices appear. Also, methods to set and get character
states occur in generic form using CharacterState objects, but also datatype-specific methods appear in the subclasses to set or return directly 
the character state in its specific form (e.g. long getState(ic, it). 
<p>
CharacterData is a subclass of Associable, which is considered to consist of a series of parts to each of which information may be attached.
The "parts" of a CharacterData are the characters.  Thus, when addParts, deleteParts, moveParts and so on are called, the response is
to add, delete and move characters.  To change the rows (taxa) of a matrix, the methods referring explicitly to taxa (addTaxa, etc.) 
are to be used.
<p>
Attached to the CharacterData object are the typesets, weightsets, character sets and so on.  Each type of set is grouped into a SpecsSetVector of SpecsSets.
These are handled by the superclass AssociableWithSpecs.  These different types of sets are not predefined within
the CharacterData object, but are established by their managing modules and stored here.<p>
  See general discussion of character storage classes under CharacterState
 */

public abstract class CharacterData extends FileElement implements MesquiteListener, StringLister, Identifiable, CompatibilityChecker  {
	public static String DATATYPENAME="Character Data";
	protected  int numTaxa; //number of taxa (rows): also determinable by taxa.getNumTaxa()
	protected  int numChars; //number of characters (columns)

	//	protected UndoInstructions undoInstructions;

	public static boolean defaultInventUniqueIDs = false;
	protected boolean inventUniqueIDs = defaultInventUniqueIDs;

	protected boolean charNumChanging = false;

	public static final NameReference publicationCodeNameRef = NameReference.getNameReference("publicationCode");//String: tInfo
	public static final NameReference taxonMatrixNotesRef = NameReference.getNameReference("taxonMatrixNotes");//String: tInfo

	private Taxa taxa; //taxa to which this matrix belongs
	private long[] taxaIDs; //the remembered id's of the taxa; to use to reconcile changed Taxa with last used here
	private long[] doubleCheckTaxaIDs; //the remembered id's of the taxa; to use to reconcile changed Taxa with last used here

	private long[] charIDs; // id's of the characters; used for error checking, especially 
	private String[] uniqueIDs; // id's of the characters; intended to be globally unique
	private String uniqueID; // id's of the matrix; intended to be globally unique
	private static long totalCharsCreated = 0;

	private String[] characterNames; //names of characters.  State names are needed only for categorical characters, and are to be found in CategoricalData
	private Image[] characterIllustrations; 
	private String[] characterIllustrationPath; 

	//information attached to individual cells of the matrix
	private String[][] footnotes; //strings attached to each cell
	private Vector cellObjects; //Vector of arrays of objects (Object2DArray) that are attached to cells.  A courtesy to modules, so that they can attach and maintain info at the cells
	private boolean[][] cellObjectsDisplay; //indicates whether there exist cell objects at a cell that need to be displayed in any way
	private boolean[][] changedSinceSave; //records whether changed since last save.  
	private int[] firstApplicable; //records the character number of the first non-applicable character.  
	private int[] lastApplicable; //records the character number of the first non-applicable character.  

	//the following four fields are for the experimental facility to record history of changes to cells
	public static boolean defaultSaveChangeHistory = false;
	public boolean saveChangeHistory = defaultSaveChangeHistory;//todo: make, set
	public static boolean defaultRequireChangeAuthority = false;
	public boolean requireChangeAuthority = defaultRequireChangeAuthority;//todo: make, set
	ChangeAuthority currentChangeAuthority;

	private boolean userVisible = true;
	private boolean inhibitEditor = false;
	//	private boolean locked = false;

	public String problemReading = null;
	private boolean checksumValid = false;
	private long rememberedChecksum = 0;
	private CRC32 crc32 = new CRC32();

	public static final char defaultInapplicableChar = '-';
	public static final char defaultMissingChar = '?';
	public static final char defaultMatchChar = '.';
	private char inapplicableChar = defaultInapplicableChar;
	private char missingChar = defaultMissingChar;
	private char matchChar = defaultMatchChar;


	private boolean colorCellsByDefault = false;
	private boolean useDiagonalCharacterNames = false;

	protected CharMatrixManager matrixManager; //module which will supervise this (e.g., writing Characters block to file)

	Tree basisTree; //the tree that was the basis of this matrix (i.e. on which it was simulated), if any
	protected int nAdd = 0, nDel = 0, nMove = 0; //for debugging


	//boolean locked = false; LOCKING SYSTEM not yet in place

	boolean columnWidthAutoSize=true;
	public boolean interleaved = false; //todo: used as a flag in reading.  Better to be part of general system of info storage for options and format issues
	public int interleavedLength = 100;

	public boolean suppressChecksum = false;
	public boolean badImport = false;
	public final static int OK = 1;
	public final static int ERROR = 2;
	public final static int OUTOFBOUNDS = 3;
	public final static int EOL = 4;
	public final static int COMMENT = 5;


	/** Vector of other CharacterData linked to this one.  Linkage requires a 1-to-1 correspondence of characters, so that for each character in one matrix there's
	a corresponding charcter in another.  This allows compound data, e.g. a ProteinData (when it exists!) could be linked to a ContinuousData holding landmark
	positions of amino acid so that both the sequence and 3D conformation of protein are stored. */
	protected Vector linkedDatas = null;

	private long id;

	public static long totalCreated = 0;
	public static long totalDisposed = 0;


	public CharacterData(CharMatrixManager matrixManager, int numTaxa, int numChars, Taxa taxa){
		super(numChars); // for Associable
		NameReference sN = makeAssociatedBits("selected"); //this won't make new Bits if not needed, just return reference
		selected = getWhichAssociatedBits(sN);
		this.matrixManager = matrixManager;
		this.numChars=numChars;
		this.numTaxa = numTaxa;
		this.taxa = taxa;
		linkedDatas = new Vector();
		taxaIDs = taxa.getTaxaIDs();
		doubleCheckTaxaIDs = taxa.getTaxaIDs();
		taxa.addListenerHighPriority(this);  //TODO: this is dangerous for simulation matrices; if not disposed will be source of memory leak

		/* the following should probably be done only on demand (e.g., so that simulation matrices don't need to remake this)*/
		characterNames = new String[numChars];
		cellObjects = new Vector();
		cellObjectsDisplay = new boolean[numChars][numTaxa];
		changedSinceSave = new boolean[numChars][numTaxa];
		firstApplicable = new int[numTaxa];
		lastApplicable = new int[numTaxa];
		uniqueIDs = new String[numChars];
		String base = MesquiteTrunk.getUniqueIDBase();
		charIDs = new long[numChars];
		for (int i=0; i<numChars; i++){
			if (inventUniqueIDs)
				uniqueIDs[i] = base + totalCharsCreated;
			charIDs[i] = totalCharsCreated++;
		}
		if (inventUniqueIDs)
			setUniqueID(MesquiteTrunk.getUniqueIDBase() + id);
		totalCreated++;
		id = totalCreated;
		recordDefaultOrder();
	}
	//used only for cases like UndoInstructions, whose local copies shouldn't listen
	public void disconnectListening(){
		if (taxa != null)
			taxa.removeListener(this);  

	}
	public void nullifyBooleanArrays () {
		cellObjectsDisplay = null;
		changedSinceSave = null;
	}
	/*.................................................................................................................*/
	public boolean getCharNumChanging() {
		return charNumChanging;
	}
	/*.................................................................................................................*/
	public void setCharNumChanging(boolean charNumChanging) {
		this.charNumChanging = charNumChanging;
	}
	/*.................................................................................................................*/
	private boolean suppressSpecsetReading = false;  //for fuse taxa & matrices, so that on merge the subsequent specsets don't take over
	public void setSuppressSpecssetReading(boolean s){
		suppressSpecsetReading = s;
	}
	public boolean getSuppressSpecssetReading(){
		return suppressSpecsetReading;
	}

	/*.................................................................................................................*/
	public UndoInstructions getUndoInstructionsAllMatrixCells(int[] changesThatMightHappen){
		if (allowFullUndo())
			return new UndoInstructions (UndoInstructions.ALLDATACELLS, this, this, changesThatMightHappen);
		else 
			return null;
	}
	/*.................................................................................................................*/
	/** This is deprecated and will be removed as soon as all modules no longer call this **/  
	@Deprecated
	public UndoInstructions getUndoInstructionsAllData(){   //needs to be removed from gataga and, ideally, pdap, and above method called instead.
		return getUndoInstructionsAllMatrixCells(null); 
	}


	/** true if the matrix is a valid one */
	public boolean isValid(){
		return numTaxa>0 && numChars>0;
	}


	protected boolean allowFullUndo() {
		boolean taxaAcceptable = MesquiteTrunk.mesquiteTrunk.maxNumMatrixUndoTaxa<0 || numTaxa<MesquiteTrunk.mesquiteTrunk.maxNumMatrixUndoTaxa;
		boolean charsAcceptable = MesquiteTrunk.mesquiteTrunk.maxNumMatrixUndoChars<0 || numChars<MesquiteTrunk.mesquiteTrunk.maxNumMatrixUndoChars;

		return (taxaAcceptable || charsAcceptable);
	}

	/*.................................................................................................................*/
	public boolean useDiagonalCharacterNames() {
		return useDiagonalCharacterNames;
	}
	/*.................................................................................................................*/
	public void setUseDiagonalCharacterNames(boolean useDiagonalNames) {
		this.useDiagonalCharacterNames = useDiagonalNames;
	}

	/*.................................................................................................................*/
	public String searchData(String s, MesquiteString commandResult) {
		if (commandResult != null)
			commandResult.setValue((String)null);
		if (StringUtil.blank(s))
			return null;
		String list = "";
		String fc =""; //to receive the direct command
		int numFound = 0;
		for (int ic=0; ic< getNumChars(); ic++){
			String name = getCharacterName(ic);
			if (name != null && StringUtil.foundIgnoreCase(name, s)){
				list += "<li>Character " + (ic+1) + ": <strong>" + StringUtil.protectForXML(name) + "</strong>. <a href=\"selectCharacter:" + ic+ " " + getID()  + "\">Touch character</a></li>";
				numFound++;
				fc = "selectCharacter:" + ic+ " " + getID() ;
			}
		}

		if (commandResult != null && numFound == 1)
			commandResult.setValue(fc);
		if (StringUtil.blank(list))
			return list;
		return "<h2>Characters of matrix <strong>" + StringUtil.protectForXML(getName()) + "</strong></h2><ul>" + list + "</ul>";
	}
	public boolean uniquelyNamed(){
		MesquiteProject p = null;
		if (matrixManager != null)
			p = matrixManager.getProject();
		else if (getFile() != null)
			p = getFile().getProject();
		if (p == null)
			return true;
		if (getName() == null)
			return false;
		for (int i = 0; i< p.getNumberCharMatrices(); i++){
			CharacterData d = p.getCharacterMatrix(i);
			if (d != this && d.getName() != null && d.getName().equals(getName()))
				return false;
		}
		return true;
	}
	public void setInventUniqueIDs(boolean invent){
		inventUniqueIDs = invent;
	}
	public boolean getInventUniqueIDs(){
		return inventUniqueIDs;
	}
	public void dispose(){

		checkThread(true);
		if (taxa!=null)
			taxa.removeListener(this);
		if (getProject()!=null && getProject().getCentralModelListener() !=null)
			getProject().getCentralModelListener().removeListener(this);
		else if (getFile() !=null && getFile().getProject() != null && getFile().getProject().getCentralModelListener() != null)
			getFile().getProject().getCentralModelListener().removeListener(this);
		if (basisTree !=null) {
			basisTree.dispose();
		}
		resignFromLinkageGroup();
		linkedDatas=null;
		taxa = null;
		characterNames = null; 
		characterIllustrations = null; 
		characterIllustrationPath = null; 
		CharacterData.totalDisposed++;
		super.dispose();
	} 

	public long getID(){
		return id;
	}

	public void deleteUniqueIDs(){
		for (int i=0; i<numChars; i++){
			uniqueIDs[i] = null;
		}
	}
	public void stampUniqueIDs(boolean replaceExisting){
		String base = MesquiteTrunk.getUniqueIDBase();
		for (int i=0; i<numChars; i++){
			if (replaceExisting || StringUtil.blank(uniqueIDs[i]))
				uniqueIDs[i] = base + totalCharsCreated++;
		}
	}
	public void stampUniqueID(int i, boolean replaceExisting){
		String base = MesquiteTrunk.getUniqueIDBase();
		if (replaceExisting || StringUtil.blank(uniqueIDs[i]))
			uniqueIDs[i] = base + totalCharsCreated++;
	}
	//setting uniqueID for matrix
	public void setUniqueID(String id){
		uniqueID = id;
	}
	//getting uniqueID for matrix
	public String getUniqueID(){
		return uniqueID;
	}
	//setting unique id for character
	public void setUniqueID(int ic, String id){
		if (ic>=0 && ic<numChars)
			uniqueIDs[ic] = id;
	}

	public String getUniqueID(int ic){
		if (ic>=0 && ic<numChars)
			return uniqueIDs[ic];
		return null;
	}

	public CharactersGroup getCurrentGroup(int ic) {
		CharacterPartition partition = (CharacterPartition) getCurrentSpecsSet(CharacterPartition.class);
		if (partition==null){
			return null;
		}
		return (CharactersGroup)partition.getProperty(ic);

	}
	public void setCurrentGroup(CharactersGroup group, int icStart, int icEnd, MesquiteModule ownerModule) {
		if (icEnd<icStart || group==null)
			return;
		CharacterPartition partition = (CharacterPartition) getCurrentSpecsSet(CharacterPartition.class);
		if (partition==null){
			partition= new CharacterPartition("Partition", getNumChars(), null, this);
			partition.addToFile(getFile(), getProject(), ownerModule.findElementManager(CharacterPartition.class));
			setCurrentSpecsSet(partition, CharacterPartition.class);
		}
		if (group != null) {
			if (partition != null) {
				boolean changed = false;
				for (int ic=icStart; ic<getNumChars() && ic<=icEnd; ic++) {
					partition.setProperty(group, ic);
					changed = true;

				}
				if (changed)
					notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
			}
		}
	}
	public void setToNewGroup(String name, int icStart, int icEnd, MesquiteModule ownerModule) {
		if (icEnd<icStart)
			return;
		CharacterPartition partition = (CharacterPartition) getCurrentSpecsSet(CharacterPartition.class);
		if (partition==null){
			partition= new CharacterPartition("Partition", getNumChars(), null, this);
			partition.addToFile(getFile(), getProject(), ownerModule.findElementManager(CharacterPartition.class));
			setCurrentSpecsSet(partition, CharacterPartition.class);
		}
		CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		CharactersGroup group = groups.findGroup(name);
		int count = 0;
		String groupName = name;
		while (group!=null) {
			count++;
			groupName = name + count;
			group = groups.findGroup(groupName);
		}
		group = new CharactersGroup();
		group.setName(groupName);
		group.addToFile(getFile(), getProject(), ownerModule.findElementManager(CharactersGroup.class));
		if (groups.indexOf(group)<0) 
			groups.addElement(group, false);

		if (group != null) {
			if (partition != null) {
				boolean changed = false;
				for (int ic=icStart; ic<getNumChars() && ic<=icEnd; ic++) {
					partition.setProperty(group, ic);
					changed = true;

				}
				if (changed)
					notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???
			}
		}
	}
	public CharactersGroup createNewGroup(CharactersGroupVector groups, String groupName, MesquiteModule ownerModule){
		CharactersGroup group = new CharactersGroup();
		group.setName(groupName);
		group.addToFile(getFile(), getProject(), ownerModule.findElementManager(CharactersGroup.class));
		if (groups.indexOf(group)<0) 
			groups.addElement(group, false);	
		return group;
	}

	public void adjustGroupLabels(String prefix, int icStart, int icEnd, boolean createNewGroups, boolean prefixGroupNamesIfAlreadyAssigned, MesquiteModule ownerModule) {
		if (icEnd<icStart)
			return;
		CharacterPartition partition = (CharacterPartition) getCurrentSpecsSet(CharacterPartition.class);
		if (partition==null){
			partition= new CharacterPartition("Partition", getNumChars(), null, this);
			partition.addToFile(getFile(), getProject(), ownerModule.findElementManager(CharacterPartition.class));
			setCurrentSpecsSet(partition, CharacterPartition.class);
		}
		CharactersGroup group;
		CharactersGroup newGroup=null;
		CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		for (int i=0; i<groups.size(); i++){
			group = (CharactersGroup)groups.elementAt(i);
			group.setRecentlyModified(false);
		}




		if (partition != null) {
			boolean changed = false;
			for (int ic=icStart; ic<getNumChars() && ic<=icEnd; ic++) {
				CharactersGroup currentGroup = (CharactersGroup)partition.getProperty(ic);
				if (currentGroup==null) { // this character doesn't have a group assigned to it; will assign a new group based upon "prefix"
					if (newGroup==null) { //make a new group
						group = groups.findGroup(prefix);  //see if one with prefix already exists
						String groupName = prefix;
						if (group==null) {
							newGroup = createNewGroup(groups,groupName,ownerModule);
							newGroup.setRecentlyModified(true);
							currentGroup=newGroup;
						} else {
							currentGroup=group;
							currentGroup.setRecentlyModified(true);  // because the character is not assigned to any group, this character will get the group that uses the prefix name, which we don't want to modify, so we set it as already modified.
						}
					} else 
						currentGroup=newGroup;
				}
				if (currentGroup!=null && !currentGroup.isRecentlyModified() && prefixGroupNamesIfAlreadyAssigned) {  // rename it with prefix
					String groupName = prefix+"."+currentGroup.getName();
					if (createNewGroups) {  // we need to create a new group with the prefix, rather than renaming the one that exists
						group = groups.findGroup(groupName);  //see if one with prefix already exists
						if (group==null) {  // there is no group of this name; let's create it
							currentGroup = createNewGroup(groups,groupName,ownerModule);
							//							newGroup2.setRecentlyModified(true);
							//							currentGroup=newGroup2;
						} else {
							currentGroup=group;
							//							currentGroup.setRecentlyModified(true);  // because the character is not assigned to any group, this character will get the group that uses the prefix name, which we don't want to modify, so we set it as already modified.
						}
					} else
						currentGroup.setName(groupName);
					currentGroup.setRecentlyModified(true);
				}
				partition.setProperty(currentGroup, ic);
				changed = true;

			}
			if (changed)
				notifyListeners(this, new Notification(MesquiteListener.NAMES_CHANGED)); //TODO: bogus! should notify via specs not data???

		}
		for (int i=0; i<groups.size(); i++){
			group = (CharactersGroup)groups.elementAt(i);
			group.setRecentlyModified(false);
		}

	}
	/*.................................................................................................................*/
	public  boolean hasCharacterGroups(){
		CharactersGroup[] parts =null;
		CharacterPartition characterPartition = (CharacterPartition)getCurrentSpecsSet(CharacterPartition.class);
		if (characterPartition!=null) {
			parts = characterPartition.getGroups();
		}
		if (parts!=null){
			return true;
		}
		return false;
	}

	public int findByUniqueID(String target){
		if (target == null)
			return -1;
		String base = MesquiteTrunk.getUniqueIDBase();
		for (int i=0; i<numChars; i++){
			String id = getUniqueID(i);
			if (id != null && id.equals(target))
				return i;
		}
		return -1;
	}
	public NexusBlock addToFile(MesquiteFile f, MesquiteProject proj, ElementManager manager){
		if (proj!=null && proj.getCentralModelListener() !=null) {
			proj.getCentralModelListener().addListener(this);
		}
		else if (f !=null && f.getProject() != null && f.getProject().getCentralModelListener() != null){
			f.getProject().getCentralModelListener().addListener(this);
		}
		return super.addToFile(f, proj, manager);
	}
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer){
		return isCompatible(obj, project, prospectiveEmployer, null);
	}

	/** Takes a listable, that in theory should be of length numChars, and returns a copy of it from which all 
	 * entries corresponding to excluded characters are removed from the list */
	public Listable[] removeExcludedFromListable(Listable[] listable) {
		if (listable==null) return null;
		int numIncluded = getNumCharsIncluded();
		Listable[] newListable = new Listable[numIncluded];
		int count = 0;
		for (int ic=0; count<newListable.length && ic<listable.length; ic++) {
			if (isCurrentlyIncluded(ic)){
				newListable[count] = listable[ic];
				count++;
			}
		}
		return newListable;
	}
	/** Takes a listable, that in theory should be of length numChars, and returns a copy of it from which all 
	 * entries corresponding to excluded characters are removed from the list */
	public boolean[] getBooleanArrayOfIncluded() {
		boolean[] newArray = new boolean[numChars]; 
		for (int ic=0; ic<numChars; ic++) {
			newArray[ic] = isCurrentlyIncluded(ic);
		}
		return newArray;
	}
	public boolean isCompatible(Object obj, MesquiteProject project, EmployerEmployee prospectiveEmployer, MesquiteString report){
		if (obj ==null)
			return true;
		else if (obj instanceof Taxa){
			if (taxa != obj && report != null)
				report.setValue("matrix applies to a different taxa block");
			return taxa == obj;
		}
		else if (obj instanceof Class){
			Class c = (Class)obj;
			boolean compatibleType =  c.isAssignableFrom(getClass()) || c.isAssignableFrom(getStateClass());
			if (!compatibleType && report != null)
				report.setValue("matrix is not of the requested data type");
			return compatibleType;
		}
		return true;
	}
	/** sets name of data matrix */
	public void setName(String name) {
		this.name= name;
		notifyListeners(this, new Notification(NAMES_CHANGED));
		if (getHShow()) {
			if (getProject() != null)
				getProject().refreshProjectWindow();
		}
	}
	/** returns title of data if it has a title, or "Character Matrix" if has no title*/
	public String getName() {
		if (name==null)
			return "Character Matrix";
		else
			return name;
	}
	/** returns true if this has name equivalent to default name*/
	public boolean hasDefaultName() {
		return  (name==null) || name.equals("Character Matrix");
	}
	/** returns true if data has a title*/
	public boolean hasTitle() {
		return  (name!=null);
	}
	public String getTypeName(){
		return "Character matrix";
	}

	/**Returns the type of data stored. */
	public abstract String getDataTypeName();

	/** clones the data set.  Does not clone the associated specs sets etc.*/
	public abstract CharacterData cloneData(); //SHOULD HERE PASS boolean to say whether to DEAL WITH CHARACTER SPEC SETS, character names, etc.

	public void copyCurrentSpecsSetsTo(CharacterData targetData){
		Vector specsVectors = getSpecSetsVectorVector();
		if (specsVectors!=null){ 
			for (int i=0; i<specsVectors.size(); i++) { 
				SpecsSetVector origSV = (SpecsSetVector)specsVectors.elementAt(i);
				SpecsSet origSS = origSV.getCurrentSpecsSet();
				if (origSS!=null) {
					Class specsClass = origSV.getType();
					SpecsSet cloneSS = targetData.getCurrentSpecsSet(specsClass);
					if (cloneSS == null){
						cloneSS = origSS.makeSpecsSet(targetData, getNumberOfParts());
						if (cloneSS != null){
							if (targetData instanceof FileElement){
								cloneSS.addToFile(((FileElement)targetData).getFile(), ((FileElement)targetData).getProject(), ((FileElement)targetData).getProject().getCoordinatorModule().findElementManager(specsClass)); 
							}
							targetData.setCurrentSpecsSet(cloneSS, specsClass);
						}
					}
					if (cloneSS != null){
						for (int ic = 0; ic< getNumChars() && ic< targetData.getNumChars(); ic++){
							cloneSS.equalizeSpecs(origSS, ic, ic);
						}
					}
				}

			}
		}
	}
	//TODO: also need setToClone(data) method to set specsets and names etc. including super.setToClone()

	/**clones a portion of CharacterData and return new copy.  Does not clone the associated specs sets etc.*/ //TODO: here should use super.setToClone(data) to handle specssets etc.???
	public CharacterData cloneDataBlock(int icStart, int icEnd, int itStart, int itEnd){
		return cloneData();  //should be overridden to restrict to certain characters and taxa!
	}

	/** copy the basic data.  Does not copy the associated specs sets etc.*/
	public abstract void copyData(CharacterData sourceData); //TODO: ? SHOULD HERE PASS boolean to say whether to DEAL WITH CHARACTER SPEC SETS, character names, etc.

	/** copy data from the datablock .  Does not copy the associated specs sets etc.*/
	public void copyDataBlock(CharacterData sourceData, int icStart, int icEnd, int itStart, int itEnd){ //TODO: ? SHOULD HERE PASS boolean to say whether to DEAL WITH CHARACTER SPEC SETS, character names, etc.
		copyData(sourceData);
	}
	/** copy the basic data.  Does not copy the associated specs sets etc.*/
	public void copyData(CharacterData sourceData, boolean allowDifferentSizes){ //TODO: ? SHOULD HERE PASS boolean to say whether to DEAL WITH CHARACTER SPEC SETS, character names, etc.
		copyData(sourceData);
	}
	/** returns the module in charge of managing the data matrix, including reading and writing the corresponding block in a NEXUS file*/
	public CharMatrixManager getMatrixManager() {
		return matrixManager;
	}
	/** sets the module in charge of managing the data matrix, including reading and writing the corresponding block in a NEXUS file*/
	public void setMatrixManager(CharMatrixManager matrixManager) {
		this.matrixManager= matrixManager;
	}
	public int getMaxNumChars(){
		return MesquiteInteger.infinite;
	}
	public boolean canMoveChars(){
		return true;
	}
	/** returns number of taxa in data matrix*/
	public int getNumTaxa(boolean notifyIfError) {
		return numTaxa;
	}
	/** returns number of taxa in data matrix*/
	public int getNumTaxa() {
		return getNumTaxa(true);
	}
	/** returns number of taxa in data matrix*/
	public int getNumTaxaWithAnyApplicable() {
		int count=0;
		for (int it=0; it<getNumTaxa(); it++) 
			if (anyApplicableAfter(0,it))
				count++;
		return count;
	}
	/** returns number of characters in data matrix*/
	public int getNumChars() {
		return getNumChars(true);
	}
	/** returns number of characters in data matrix*/
	public int getNumChars(boolean notifyIfError) {
		return numChars;
	}

	/** returns the Taxa object to which it applies*/
	public Taxa getTaxa() {
		return taxa;
	}
	/** returns whether or not one can add characters manually to this matrix; some data classes, e.g., geographic, don't allow it.*/
	public boolean canAddCharacters() {
		return true;
	}

	/*-----------------------------------------------------------*/
	/** A equivalent of addParts but with notification added */
	public final boolean addCharacters(int starting, int num, boolean notify){
		boolean added = addParts(starting, num);
		if (added && notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {starting, num}));
		return added;
	}

	public void calculateFirstLastApplicable(int it){
		/*   enable once the system of calling this is more refined
		if (firstApplicable!=null){
			int first = -1;
			for (int ic=0; ic<numChars; ic++) {
				if (!isInapplicable(ic, it)) {
					first=ic;
					break;
				}
			}
			if (it<firstApplicable.length)
				firstApplicable[it] = first;

		}
		if (lastApplicable!=null){
			int last = -1;
			for (int ic=numChars-1; ic>=0; ic--) {
				if (!isInapplicable(ic, it)) {
					last=ic;
					break;
				}
			}
			if (it<lastApplicable.length)
				lastApplicable[it] = last;
		}
		 */

	}
	public void calculateFirstLastApplicable(){
		for (int it = 0; it<numTaxa; it++)
			calculateFirstLastApplicable(it);
	}

	/*-----------------------------------------------------------*/
	/**Adds num characters after position "starting".  If "starting" = -1, then inserts at start.  If "starting" >
	number of characters, adds to end.  Any linked CharacterDatas are
	to be adjusted separately.  Returns true iff successful.*/
	public boolean addParts(int starting, int num){
		if (getMaxNumChars()!=MesquiteInteger.infinite && numChars+num>getMaxNumChars())
			return false;
		if (num<=0)
			return false;
		if (!checkThread(false))
			return false;
		nAdd++;
		if (starting<0)
			starting = -1;
		else if (starting>=numChars)
			starting = numChars-1;
		int newNumChars = numChars + num;

		//adjusting unique id's 
		if (uniqueIDs != null) {
			uniqueIDs = StringArray.addParts(uniqueIDs, starting, num);
		}
		String base = MesquiteTrunk.getUniqueIDBase();

		//adjusting character id's 
		long[] newCharIDs = new long[numChars + num];
		for (int i=0; i<=starting; i++)
			newCharIDs[i] = charIDs[i];
		for (int i=0; i<num; i++) {
			if (uniqueIDs != null && inventUniqueIDs)
				uniqueIDs[starting + i + 1] = base + totalCharsCreated; 
			newCharIDs[starting + i + 1] = totalCharsCreated++;
		}
		for (int i=0; i<numChars-starting-1; i++) 
			newCharIDs[i + starting+num+1] = charIDs[starting + i+1];
		charIDs = newCharIDs;



		if (characterNames!=null){
			characterNames = StringArray.addParts(characterNames, starting, num);
		}

		if (footnotes!=null){
			String[][] newFootnotes = new String[newNumChars][numTaxa];
			for (int j = 0; j<numTaxa; j++){
				for (int i=0; i<=starting; i++)
					newFootnotes[i][j] = footnotes[i][j];
				for (int i=0; i<num; i++)
					newFootnotes[starting + i + 1][j] = null;
				for (int i=0; i<numChars-starting-1; i++) 
					newFootnotes[i + starting+num+1][j] = footnotes[starting + i+1][j];
			}
			footnotes = newFootnotes;
		}
		if (cellObjects.size()>0){
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object[][] oldObjects = objArray.getMatrix();
				Object[][] newObjects = new Object[newNumChars][numTaxa];
				for (int j = 0; j<numTaxa; j++){
					for (int i=0; i<=starting; i++)
						newObjects[i][j] = oldObjects[i][j];
					for (int i=0; i<num; i++)
						newObjects[starting + i + 1][j] = null;
					for (int i=0; i<numChars-starting-1; i++) 
						newObjects[i +starting+num+1][j] = oldObjects[starting + i+1][j];
				}
				objArray.setMatrix(newObjects);
			}
		}
		if (cellObjectsDisplay!=null){
			boolean[][] newCOD = new boolean[newNumChars][numTaxa];
			for (int j = 0; j<numTaxa; j++){
				for (int i=0; i<=starting; i++)
					newCOD[i][j] = cellObjectsDisplay[i][j];
				for (int i=0; i<num; i++)
					newCOD[starting + i + 1][j] = false;
				for (int i=0; i<numChars-starting-1; i++) 
					newCOD[i + starting+num+1][j] = cellObjectsDisplay[starting + i+1][j];
			}
			cellObjectsDisplay = newCOD;
		}
		if (changedSinceSave!=null){
			boolean[][] newCOD = new boolean[newNumChars][numTaxa];
			for (int j = 0; j<numTaxa; j++){
				for (int i=0; i<=starting; i++)
					newCOD[i][j] = changedSinceSave[i][j];
				for (int i=0; i<num; i++)
					newCOD[starting + i + 1][j] = false;
				for (int i=0; i<numChars-starting-1; i++) 
					newCOD[i + starting+num+1][j] = changedSinceSave[starting + i+1][j];
			}
			changedSinceSave = newCOD;
		}

		if (characterIllustrations!=null){
			Image[] newCharacterIllustrations = new Image[newNumChars];
			for (int i=0; i<=starting; i++) {
				newCharacterIllustrations[i] = characterIllustrations[i];
			}
			for (int i=0; i<num; i++) {
				newCharacterIllustrations[starting + i + 1] = null;
			}
			for (int i=0; i<numChars-starting-1; i++) {
				newCharacterIllustrations[i +starting+num+1] = characterIllustrations[starting + i+1];
			}
			characterIllustrations = newCharacterIllustrations;
		}
		numChars = newNumChars;
		calculateFirstLastApplicable();
		super.addParts(starting, num); //for specssets
		uncheckThread();
		notifyOfChangeLowLevel(MesquiteListener.PARTS_ADDED, starting, num, 0);  
		return true;
	}
	/*-----------------------------------------------------------*/
	/** Adds characters in linked data files. */
	public void addInLinked(int starting, int num, boolean notify){
		if (linkedDatas != null && linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.addParts(starting, num);
				if (notify)
					d.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {starting, num}));
			}
		}
	}
	/*-----------------------------------------------------------*/
	/** An equivalent to deleteParts but with notification added. Final because overriding should be done of the Parts method instead*/
	public final boolean deleteCharacters(int starting, int num, boolean notify){
		boolean deleted = deleteParts(starting, num);
		if (deleted && notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED, new int[] {starting, num}));
		return deleted;
	}
	/*-----------------------------------------------------------*/
	/** Deletes characters that are turned on in a Bits. */
	public final boolean deleteCharacters(Bits bits, String progressNote, boolean notify){
		int ic = numChars;
		boolean deleted = false;
		while (ic>=0) {
			int start = bits.startOfBlock(ic);
			if (bits.isBitOn(ic)){
				boolean del = deleteParts(start, ic-start+1);
				if (del)
					deleted=true;
			} 
			if (StringUtil.notEmpty(progressNote)) 
				CommandRecord.setDetailsOfProgress(progressNote + " " + ic);
			ic=start-1;
		}
		if (deleted && notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED));
		return deleted;
	}
	/*-----------------------------------------------------------*/
	/** deletes num characters from (and including) position "starting"; returns true iff successful.  Should be overridden by particular subclasses, but this called via super so it can clean up.*/
	public boolean deleteParts(int starting, int num){
		if (num<=0)
			return false;
		if (starting<0)
			return false;
		else if (starting>numChars)
			return false;
		if (!checkThread(false))
			return false;
		nDel++;
		if (num+starting>numChars)
			num = numChars-starting;
		int newNumChars = numChars - num;

		//adjusting character id's 
		long[] newCharIDs = new long[newNumChars];
		for (int i=0; i<starting; i++) {
			newCharIDs[i] = charIDs[i];
		}
		for (int i=starting+num; i<charIDs.length; i++) {
			newCharIDs[i -num]= charIDs[i];
		}
		notifyOfChangeLowLevel(MesquiteListener.PARTS_DELETED, starting, num, 0);  

		charIDs = newCharIDs;

		if (uniqueIDs != null)
			uniqueIDs = StringArray.deleteParts(uniqueIDs, starting, num);

		if (characterNames!=null){
			characterNames = StringArray.deleteParts(characterNames, starting, num);
		}
		if (footnotes!=null){
			String[][] newFootnotes = new String[newNumChars][numTaxa];
			for (int j = 0; j<numTaxa; j++){
				for (int i=0; i<starting; i++) {
					newFootnotes[i][j] = footnotes[i][j];
				}
				for (int i=starting+num; i<footnotes.length; i++) {
					newFootnotes[i -num][j] = footnotes[i][j];
				}
			}
			footnotes = newFootnotes;
		}
		if (cellObjects.size()>0){
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object[][] oldObjects = objArray.getMatrix();
				Object[][] newObjects = new Object[newNumChars][numTaxa];
				for (int j = 0; j<numTaxa; j++){
					for (int i=0; i<starting; i++) {
						newObjects[i][j] = oldObjects[i][j];
					}
					for (int i=starting+num; i<oldObjects.length; i++) { //bug had been here in 1.03
						newObjects[i -num][j] = oldObjects[i][j];
					}
				}
				objArray.setMatrix(newObjects);
			}
		}
		if (cellObjectsDisplay!=null){
			boolean[][] newCOD = new boolean[newNumChars][numTaxa];
			for (int j = 0; j<numTaxa; j++){
				for (int i=0; i<starting; i++) {
					newCOD[i][j] = cellObjectsDisplay[i][j];
				}
				for (int i=starting+num; i<cellObjectsDisplay.length; i++) {
					newCOD[i -num][j] = cellObjectsDisplay[i][j];
				}
			}
			cellObjectsDisplay = newCOD;
		}
		if (changedSinceSave!=null){
			boolean[][] newCOD = new boolean[newNumChars][numTaxa];
			for (int j = 0; j<numTaxa; j++){
				for (int i=0; i<starting; i++) {
					newCOD[i][j] = changedSinceSave[i][j];
				}
				for (int i=starting+num; i<changedSinceSave.length; i++) {
					newCOD[i -num][j] = changedSinceSave[i][j];
				}
			}
			changedSinceSave = newCOD;
		}

		if (characterIllustrations!=null){
			Image[] newCharacterIllustrations = new Image[newNumChars];
			for (int i=0; i<starting; i++) {
				newCharacterIllustrations[i] = characterIllustrations[i];
			}
			for (int i=starting+num; i<characterIllustrations.length; i++) {
				newCharacterIllustrations[i-num ] = characterIllustrations[i];
			}
			characterIllustrations = newCharacterIllustrations;
		}
		numChars = newNumChars;
		calculateFirstLastApplicable();
		super.deleteParts(starting, num); //for specs sets
		uncheckThread();
		return true;
	}
	/*-----------------------------------------------------------*/
	/** Deletes characters in linked data matrices. */
	public final void deleteInLinked(int starting, int num, boolean notify){
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.deleteParts(starting, num);
				if (notify)
					d.notifyListeners(this, new Notification(MesquiteListener.PARTS_DELETED, new int[] {starting, num}));
			}
		}
	}
	/*-----------------------------------------------------------*/
	/** Deletes characters flagged in the Bits in linked data matrices. */
	public final void deleteInLinked(Bits bits, String progressNote,  boolean notify){
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.deleteCharacters(bits, progressNote, notify);
			}
		}
	}
	/*-----------------------------------------------------------*/
	/** An equivalent to moveParts but with notification added.*/
	public final boolean moveCharacters(int starting, int num, int justAfter, boolean notify){
		boolean moved = moveParts(starting, num, justAfter);
		if (moved && notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, new int[] {starting, num, justAfter}));
		return moved;
	}

	public String[][] getFootnotes(){	
		return footnotes;
	}
	/*-----------------------------------------------------------*/
	/**Moves num characters from position "first" to just after position "justAfter"; returns true iff successful.*/
	public boolean moveParts(int starting, int num, int justAfter){
		if (!canMoveChars()) 
			return false;
		if (!checkThread(false))
			return false;
		StringArray.moveParts(characterNames, starting, num, justAfter); 
		StringArray.moveColumns(footnotes, starting, num, justAfter); 
		StringArray.moveParts(characterIllustrationPath, starting, num, justAfter); 
		if (cellObjects.size()>0){
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object[][] oldObjects = objArray.getMatrix();
				Object2DArray.moveColumns(oldObjects, starting, num, justAfter);
			}
		}
		notifyOfChangeLowLevel(MesquiteListener.PARTS_MOVED, starting, num, justAfter);  

		Bits.moveColumns(cellObjectsDisplay, starting, num, justAfter);
		Bits.moveColumns(changedSinceSave, starting, num, justAfter);
		calculateFirstLastApplicable();
		MesquiteImage.moveParts( characterIllustrations, starting, num, justAfter); 
		charIDs = LongArray.getMoveParts(charIDs, starting, num, justAfter);
		StringArray.moveParts(uniqueIDs, starting, num, justAfter);
		nMove++;
		boolean moved =  super.moveParts(starting, num, justAfter);
		uncheckThread();
		return moved;
	}
	/*-----------------------------------------------------------*/
	/** Move characters in linked data matrices. */
	public final void moveInLinked(int starting, int num, int justAfter, boolean notify){
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.moveParts(starting, num, justAfter);
				if (notify)
					d.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED, new int[] {starting, num, justAfter}));
			}
		}
	}
	/*-----------------------------------------------------------*/
	/**Swaps metadata for cells of characters first and second.*/
	public boolean swapCellMetadata(int first, int second){
		if (!checkThread(false))
			return false;
		StringArray.swapColumns(footnotes,  first, second); 
		if (cellObjects.size()>0){
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object[][] oldObjects = objArray.getMatrix();
				Object2DArray.swapColumns(oldObjects,  first, second);
			}
		}
		Bits.swapColumns(cellObjectsDisplay,  first, second);
		Bits.swapColumns(changedSinceSave,  first, second);

		uncheckThread();
		return true;
	}
	/*-----------------------------------------------------------*/
	/**Swaps metadata for cells of characters first and second in taxon it.*/
	public boolean swapCellMetadata(int first, int second, int it){
		if (!checkThread(false))
			return false;
		StringArray.swapCell(footnotes,  first, second, it); 
		if (cellObjects.size()>0){
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object[][] oldObjects = objArray.getMatrix();
				Object2DArray.swapCell(oldObjects,  first, second, it);
			}
		}
		Bits.swapCell(cellObjectsDisplay,  first, second, it);
		Bits.swapCell(changedSinceSave,  first, second, it);

		uncheckThread();
		return true;
	}
	/*-----------------------------------------------------------*/
	/**Swaps metadata for characters first and second.*/
	public boolean swapCharacterMetadata(int first, int second){
		if (!checkThread(false))
			return false;

		StringArray.swapParts(characterIllustrationPath,  first, second); 
		StringArray.swapParts(characterNames, first, second); 
		StringArray.swapParts(uniqueIDs, first, second);
		//adjusting character id's 
		if (first<charIDs.length && second<charIDs.length){
			long oldFirst  = charIDs[first];
			charIDs[first] = charIDs[second];
			charIDs[second] = oldFirst;
		}
		notifyOfChangeLowLevel(MesquiteListener.PARTS_SWAPPED, first, second, 0);  

		calculateFirstLastApplicable();
		MesquiteImage.swapParts( characterIllustrations,  first, second); 
		nMove++;
		boolean swapped =  super.swapParts( first, second);
		uncheckThread();
		return swapped;
	}
	/*-----------------------------------------------------------*/
	/**Swaps characters first and second.*/
	public boolean swapParts(int first, int second){
		boolean meta = swapCellMetadata(first, second);
		if (!meta)
			return false;
		boolean swapped =  swapCharacterMetadata( first, second);  // this includes call to super.swapParts!!!!!
		return swapped;
	}

	/*-----------------------------------------------------------*/
	/** Swap characters in linked data matrices. */
	public final void swapInLinked(int first, int second, boolean notify){
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.swapParts(first, second);
				if (notify)
					d.notifyListeners(this, new Notification(MesquiteListener.PARTS_MOVED));
			}
		}
	}
	/*.................................................................................................................*/
	public int checkCellMoveDistanceAvailable(int distance, int startBlock, int endBlock, Bits whichTaxa, MesquiteBoolean isTerminalBlock, MesquiteInteger boundaryOfAvailableSpace, boolean canExpand){
		if (!checkThread(false))
			return 0;
		boolean terminalBlock=false;
		int firstTaxon = whichTaxa.firstBitOn();
		int lastTaxon = whichTaxa.lastBitOn();
		int gapsAvailable = 0;
		int g=0;
		if (distance>0){ //moving right
			//first check to see whether gaps are to the right 
			g = endBlock+1;  // start one past end of block  
			if (!canExpand && g>=numChars) {
				distance = 0;
				g=numChars-1;
				terminalBlock=true;
			}
			else {

				while (g<numChars && inapplicableBlock(g,g,whichTaxa, firstTaxon, lastTaxon))  //counting gaps beyond the current one
					g++;
				if (g>=numChars) //we reached the end and all gaps
					terminalBlock = true;
				g--;  //decrement by one as we must be one over what we should be.  g now stores the number of the last available character
				gapsAvailable = g-endBlock;
				if (!(terminalBlock && canExpand) && distance>gapsAvailable) {
					distance = gapsAvailable;
				}
			}
		}
		else if (distance<0){ //moving left
			//first check to see whether gaps can eat at end
			g = startBlock-1;
			if (!canExpand && g<0) {
				distance = 0;
				g=0;
				terminalBlock=true;
			}
			else {
				while (g>=0 && inapplicableBlock(g,g,whichTaxa, firstTaxon, lastTaxon))  //counting terminal gaps
					g--;
				if (g<0)
					terminalBlock=true;
				g++;
				gapsAvailable = startBlock-g;
				if (!(terminalBlock && canExpand) && distance<-gapsAvailable) {
					distance = -gapsAvailable;
				}
			}
		}
		isTerminalBlock.setValue(terminalBlock);
		boundaryOfAvailableSpace.setValue(g);
		uncheckThread();
		return distance;
	}
	/*.................................................................................................................*/
	public int checkCellMoveDistanceAvailable(int distance, int startBlock, int endBlock, int itStart, int itEnd, MesquiteBoolean isTerminalBlock, MesquiteInteger boundaryOfAvailableSpace, boolean canExpand){
		Bits whichTaxa = new Bits(numTaxa);
		for (int it=itStart; it<=itEnd; it++)
			whichTaxa.setBit(it);
		return checkCellMoveDistanceAvailable( distance,  startBlock,  endBlock,  whichTaxa,  isTerminalBlock,  boundaryOfAvailableSpace,  canExpand);
	}
	/*.................................................................................................................*/
	protected CharacterState moveOne(int i, int distance, int it, CharacterState cs,  MesquiteBoolean dataChanged){
		cs = getCharacterState(cs, i, it);
		if (cs==null)
			return null;
		if (!checkThread(false))
			return null;
		if (i+distance >= getNumChars())
			dataIntegrityAlert("moveOne request to move cells beyond limits of matrix!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! @ i " + i + " distance " + distance + " getNumChars() " + getNumChars() + " it " + it  + " this " + this);
		setState(i+distance, it, cs);
		cs.setToInapplicable();
		setState(i, it, cs);
		if (dataChanged!=null)
			dataChanged.setValue(true);

		if (i+distance>=0 && i+distance< getNumChars()){
			//footnotes
			if (footnotes!=null){

				footnotes[i+distance][it] = footnotes[i][it];
				footnotes[i][it]=null;
			}

			//cell objects
			if (cellObjects !=null)
				for (int k =0; k<cellObjects.size(); k++){
					Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
					Object[][] objects = objArray.getMatrix();
					objects[i+distance][it] = objects[i][it];
					objects[i][it]=null;
				}
		}
		uncheckThread();
		return cs;
	}
	/*.................................................................................................................*/
	/** moves the cells from character startBlock to character endBlock a distance "distance" for taxon it.  
	If canExpand is set, then the procedure will add characters to the front or end of the matrix. 
	If canOverwrite is set, then the procedure can overwrite data (otherwise it can only overwrite inapplicable cells).

	Returns how many characters are added to the front (if value is -ve) or end (if value is +ve) of the matrix.
	 */
	public int moveCells(int startBlock, int endBlock, int distance, Bits whichTaxa,  boolean canExpand, boolean canOverwrite, boolean includingLinked, boolean notify, MesquiteBoolean dataChanged,MesquiteInteger charAdded, MesquiteInteger distanceMoved){  //startBlock and endBlock are 0-based
		if (!checkThread(false))
			return 0;
		CharacterState cs = null;
		MesquiteBoolean isTerminalBlock = new MesquiteBoolean(false);
		MesquiteInteger boundaryOfAvailableSpace = new MesquiteInteger(0);
		int gapsAvailable = 0;
		int openUp = 0;
		int added = 0;
		int origStartBlock = startBlock;
		int origEndBlock = endBlock;
		if (distance>0){ //moving right
			int gResultingEndBlock = 0;
			if (!canOverwrite){ //adjust distance to avoid overwriting
				distance = checkCellMoveDistanceAvailable(distance, startBlock, endBlock, whichTaxa, isTerminalBlock, boundaryOfAvailableSpace, canExpand);	   	
				gResultingEndBlock = boundaryOfAvailableSpace.getValue();		
			}
			else
				gResultingEndBlock = endBlock + distance;
			if (distanceMoved!=null)
				distanceMoved.setValue(distance);
			if (isTerminalBlock.getValue() && canExpand) {
				openUp = distance-(getNumChars()-endBlock)+1;
				if (openUp>0) {  //makeNewCharacters
					addCharacters(getNumChars(), openUp, false);
					if (charAdded!=null)
						charAdded.setValue(openUp);
					if (includingLinked)
						addInLinked(getNumChars(), openUp, false);
					added = openUp;
					gResultingEndBlock= endBlock + distance; //NOTE:   this should be end of block, NOT numchars
					if (dataChanged!=null)
						dataChanged.setValue(true);
				}
			}
			//now move from end
			if (distance!=0)
				for (int it=0; it<getNumTaxa(); it++) {
					if (whichTaxa.isBitOn(it))
						for (int i = gResultingEndBlock-distance; i>=startBlock; i--){
							cs = moveOne(i, distance, it, cs, dataChanged);
						}
				}
		} 
		else if (distance<0){ //moving left
			int g = 0;
			if (!canOverwrite) { //adjust distance to avoid overwriting
				distance = checkCellMoveDistanceAvailable(distance, startBlock, endBlock, whichTaxa, isTerminalBlock, boundaryOfAvailableSpace,canExpand);	   	
				g = boundaryOfAvailableSpace.getValue();		
			}
			if (distanceMoved!=null)
				distanceMoved.setValue(distance);

			if (isTerminalBlock.getValue() && canExpand) {
				openUp = -distance-startBlock;

				//if not then make new characters
				if (openUp>0) {
					addCharacters(-1, openUp, false);
					if (charAdded!=null)
						charAdded.setValue(-openUp);
					if (includingLinked)
						addInLinked(-1, openUp, false);
					added = -openUp;
					if (dataChanged!=null)
						dataChanged.setValue(true);
					startBlock += openUp;
					endBlock += openUp;
					origStartBlock += openUp;
					origEndBlock += openUp;
				}
			}

			//now move from front end
			if (distance!=0)
				for (int it=0; it<getNumTaxa(); it++)
					if (whichTaxa.isBitOn(it))
						for (int i = startBlock; i<=endBlock; i++){
							cs = moveOne(i, distance, it, cs, dataChanged);
						}

		}
		if (includingLinked){
			if (linkedDatas.size()>0){
				for (int i=0; i<linkedDatas.size(); i++){
					CharacterData d = (CharacterData)linkedDatas.elementAt(i);
					d.moveCells(origStartBlock, origEndBlock, distance, whichTaxa,  false, true, false, notify, null,null, null);
				}
			}
		}
		uncheckThread();
		return added;
	}
	/*.................................................................................................................*/
	/** moves the cells from character startBlock to character endBlock a distance "distance" for taxon it.  
	If canExpand is set, then the procedure will add characters to the front or end of the matrix. 
	If canOverwrite is set, then the procedure can overwrite data (otherwise it can only overwrite inapplicable cells).

	Returns how many characters are added to the front (if value is -ve) or end (if value is +ve) of the matrix.
	 */
	public int moveCells(int startBlock, int endBlock, int distance, int itStart, int itEnd,  boolean canExpand, boolean canOverwrite, boolean includingLinked, boolean notify, MesquiteBoolean dataChanged, MesquiteInteger charAdded, MesquiteInteger distanceMoved){  //startBlock and endBlock are 0-based
		Bits whichTaxa = new Bits(getNumTaxa());
		for (int it=itStart; it<=itEnd; it++)
			whichTaxa.setBit(it);
		return moveCells( startBlock,  endBlock,  distance, whichTaxa,   canExpand,  canOverwrite,  includingLinked,  notify,  dataChanged, charAdded, distanceMoved);
	}
	/*.................................................................................................................*/
	public int shiftAllCells(int distance, int it,  boolean canExpand,  boolean includingLinked, boolean notify, MesquiteBoolean dataChanged, MesquiteInteger charAdded, MesquiteInteger distanceMoved){  //startBlock and endBlock are 0-based
		int first = firstApplicable(it);
		int last = lastApplicable(it);
		if (first<0)
			return 0;
		else {
			int dist =  moveCells(first,last, distance, it, it, canExpand, false, includingLinked,  notify, dataChanged, charAdded, distanceMoved);
			return dist;
		}
	}
	/*-----------------------------------------------------------*/
	/** Has linked data matrices send out notifications of change. */
	public final void notifyInLinked(Notification notification){
		if (linkedDatas == null)
			return;
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.notifyListeners(this, notification);
			}
		}
	}
	/*-----------------------------------------------------------*/
	/** Has linked data matrices record current order. */
	public final void copyCurrentToPreviousOrderInLinked(){
		if (linkedDatas == null)
			return;
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.copyCurrentToPreviousOrder();
			}
		}
	}
	/*-----------------------------------------------------------*/
	/** Has linked data matrices record current order. */
	public final void recordCurrentOrderInLinked(){
		if (linkedDatas == null)
			return;
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.recordCurrentOrder();
			}
		}
	}
	/*-----------------------------------------------------------*/
	/** Has linked data matrices record previous order. */
	public final void recordPreviousOrderInLinked(){
		if (linkedDatas == null)
			return;
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.recordPreviousOrder();
			}
		}
	}
	/*-----------------------------------------------------------*/
	/** Has linked data matrices record previous order. */
	public final void restoreToPreviousOrderInLinked(){
		if (linkedDatas == null)
			return;
		if (linkedDatas.size()>0){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d = (CharacterData)linkedDatas.elementAt(i);
				d.restoreToPreviousOrder();
			}
		}
	}
	/*-----------------------------------------------------------*/
	private NameReference notesNameRef = NameReference.getNameReference("notes");
	private NameReference historyNameRef = NameReference.getNameReference("ChangeHistory");
	private AttachedNotesVector getVector(CharacterData d, int ic, int it){
		if (it == -1)
			return (AttachedNotesVector)d.getAssociatedObject(notesNameRef, ic);
		else if (ic>= 0 && it>=0)
			return (AttachedNotesVector)d.getCellObject(notesNameRef, ic, it);
		return null;
	}
	private void copyAnnotations(int ic, int it, CharacterData oData, int oic, int oit){
		AttachedNotesVector v = getVector(this, ic, it);
		AttachedNotesVector vO = getVector(oData, oic, oit);
		if (vO == null) { //no notes; do nothing
		}
		else if (ic>=0){
			v = vO.cloneVector(this);
			if (it < 0)
				setAssociatedObject(notesNameRef, ic, v);
			else {
				setCellObject(notesNameRef, ic, it, v);
				setCellObjectDisplay(ic, it);
			}
		}
	}
	/*-----------------------------------------------------------*/
	public void equalizeCharacter(CharacterData oData, int oic, int ic){
		//doesn't yet incorporate colors, etc
		CharacterState cs2 = null;
		if (oData.characterNames != null && oic<oData.characterNames.length){
			characterNames[ic] = oData.characterNames[oic];
			notifyOfChangeLowLevel(MesquiteListener.NAMES_CHANGED, ic, -1, 0);  

		}
		for (int it = 0; it<getNumTaxa(); it++){
			incrementSuppressHistoryStamp();
			int oit = oData.getTaxa().findEquivalentTaxon(getTaxa(), it);
			if (oit >= 0){
				cs2 = oData.getCharacterState(cs2, oic, oit);
				if (cs2 !=null) 
					setState(ic, it, cs2);
				notifyOfChangeLowLevel(MesquiteListener.DATA_CHANGED, ic, it, 0);  
				copyAnnotations(ic, it, oData, oic, oit);
				//add: cellObjectsDisplay
				ChangeHistory h2 = (ChangeHistory)oData.getCellObject(historyNameRef, oic, oit);
				if (h2 != null)	
					setCellObject(historyNameRef, ic, it, h2.cloneHistory());
			}
		}
		equalizeParts(oData, oic, ic);
		decrementSuppressHistoryStamp();
		setAnnotation(ic, oData.getAnnotation(oic));
		copyAnnotations( ic, -1, oData, oic, -1);
	}
	/*-----------------------------------------------------------*/
	/**Adds num taxa after position "starting"; returns true iff successful.  Assumes details already handled in subclasses, and numTaxa reset there.*/
	public boolean addTaxa(int starting, int num){
		if (!checkThread(false))
			return false;
		doubleCheckTaxaIDs = LongArray.addParts(doubleCheckTaxaIDs, starting, num, 0L);
		for (int i = starting+1; i<= starting+num; i++) {
			Taxon taxon = taxa.getTaxon(i);
			if (taxon != null)
				doubleCheckTaxaIDs[i] = taxon.getID();
		}

		taxaIDs = taxa.getTaxaIDs();
		if (footnotes !=null) {
			footnotes = StringArray.addRows(footnotes, starting, num);
		}
		if (cellObjectsDisplay !=null) {
			cellObjectsDisplay = Bits.addRows(cellObjectsDisplay, starting, num);
		}
		if (changedSinceSave !=null) {
			changedSinceSave = Bits.addRows(changedSinceSave, starting, num);
		}
		if (firstApplicable !=null) {
			firstApplicable = IntegerArray.addParts(firstApplicable, starting, num);
		}
		if (lastApplicable !=null) {
			lastApplicable = IntegerArray.addParts(lastApplicable, starting, num);
		}

		if (cellObjects != null && cellObjects.size()>0){//Vector of arrays of objects that are attached to cells
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object[][] objects = objArray.getMatrix();
				objects = Object2DArray.addRows(objects, starting, num);
				objArray.setMatrix(objects);
			}
		}
		if (taxaInfo != null)
			taxaInfo.addParts(starting, num);
		uncheckThread();
		return true;
	}

	/*-----------------------------------------------------------*/

	/**Deletes num taxa from position "starting"; returns true iff successful.  Assumes details already handled in subclasses, and numTaxa reset there.*/
	public boolean deleteTaxa(int starting, int num){
		if (taxa == null)
			return false;
		if (!checkThread(false))
			return false;
		doubleCheckTaxaIDs = LongArray.deleteParts(doubleCheckTaxaIDs, starting, num);
		taxaIDs = taxa.getTaxaIDs();
		if (footnotes !=null) {
			footnotes = StringArray.deleteRows(footnotes, starting, num);
		}
		if (cellObjectsDisplay !=null) {
			cellObjectsDisplay = Bits.deleteRows(cellObjectsDisplay, starting, num);
		}
		if (changedSinceSave !=null) {
			changedSinceSave = Bits.deleteRows(changedSinceSave, starting, num);
		}
		if (firstApplicable !=null) {
			firstApplicable = IntegerArray.deleteParts(firstApplicable, starting, num);
		}
		if (lastApplicable !=null) {
			lastApplicable = IntegerArray.deleteParts(lastApplicable, starting, num);
		}

		if (cellObjects != null && cellObjects.size()>0){//Vector of arrays of objects that are attached to cells
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object[][] objects = objArray.getMatrix();
				objects = Object2DArray.deleteRows(objects, starting, num);
				objArray.setMatrix(objects);
			}
		}
		if (taxaInfo != null)
			taxaInfo.deleteParts(starting, num);
		uncheckThread();
		return true;
	}

	/**moves num taxa from position "starting" to just after position "justAfter"; returns true iff successful.*/
	public boolean moveTaxa(int starting, int num, int justAfter){
		if (!checkThread(false))
			return false;
		if (footnotes !=null) {
			StringArray.moveRows(footnotes, starting, num, justAfter);
		}
		if (cellObjectsDisplay !=null) {
			Bits.moveRows(cellObjectsDisplay, starting, num, justAfter);
		}
		if (changedSinceSave !=null) {
			Bits.moveRows(changedSinceSave, starting, num, justAfter);
		}
		if (firstApplicable !=null) {
			firstApplicable = IntegerArray.moveParts(firstApplicable, starting, num, justAfter);
		}
		if (lastApplicable !=null) {
			lastApplicable = IntegerArray.moveParts(lastApplicable, starting, num, justAfter);
		}


		if (cellObjects != null && cellObjects.size()>0){//Vector of arrays of objects that are attached to cells
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object[][] objects = objArray.getMatrix();
				Object2DArray.moveRows(objects, starting, num, justAfter);
				objArray.setMatrix(objects);
			}
		}
		LongArray.moveParts(taxaIDs, starting, num, justAfter);
		LongArray.moveParts(doubleCheckTaxaIDs, starting, num, justAfter);

		if (taxaInfo != null)
			taxaInfo.moveParts(starting, num, justAfter);
		uncheckThread();
		return true;
	}
	private long[] lastNotifications = new long[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1}; //a partial protection against responding to the same notification twice, e.g. coming via two different pathways.
	private boolean notificationFound(Notification notification){
		if (notification ==null)
			return false;
		long id = notification.getNotificationNumber();
		if (id <0)
			return false;
		if (LongArray.indexOf(lastNotifications, id)>=0)
			return true;
		return false;
	}
	private void rememberNotification(Notification notification){
		if (notification ==null)
			return;
		long id = notification.getNotificationNumber();
		if (id <0)
			return;
		for (int i = 0; i< lastNotifications.length-1; i++)
			lastNotifications[i+1] = lastNotifications[i];
		lastNotifications[0] = id;
	}
	/** For MesquiteListener interface; passes which object changed, along with optional integer (e.g. for character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (notificationFound(notification))
			return;
		rememberNotification(notification);
		if (obj == taxa) {
			if (Notification.appearsCosmetic(notification) || notification == null   || notification.getCode() == MesquiteListener.SELECTION_CHANGED)
				return;
			int code = Notification.getCode(notification);
			int[] parameters = Notification.getParameters(notification);
			if (parameters==null && obj instanceof Taxa && (Taxa)obj==taxa)
				reconcileTaxa(code);
			else {
				try {
					if (code== MesquiteListener.PARTS_ADDED)
						addTaxa(parameters[0],parameters[1]);
					else if (code== MesquiteListener.PARTS_DELETED)
						deleteTaxa(parameters[0],parameters[1]);
					else if (code== MesquiteListener.PARTS_MOVED)
						moveTaxa(parameters[0],parameters[1], parameters[2]);
					notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
				}
				catch (ArrayIndexOutOfBoundsException e){}
			}
			if (basisTree!=null && basisTree instanceof MesquiteListener)
				((MesquiteListener)basisTree).changed(caller, obj, notification);
		}
	}
	/** For MesquiteListener interface*
	public void disposing(Object obj){
		super.disposing(obj);
	}
	/** For MesquiteListener interface*/
	public boolean okToDispose(Object obj, int queryUser){
		return true;
	}


	public boolean checkTaxaIDs(){  //here for compatibility with older modules
		String s =  checkIntegrity();
		if (s == null)
			return true;
		dataIntegrityAlert(s);
		return false;
	}
	public void dataIntegrityAlert(String s) {
		if (badImport){
			discreetAlert(s);
			MesquiteMessage.printStackTrace();
			return;
		}
		Thread current = Thread.currentThread();
		if ((current instanceof MesquiteThread) && !MesquiteThread.isScripting())
			MesquiteTrunk.mesquiteTrunk.reportableProblemAlert(s);
		else
			MesquiteMessage.warnProgrammer(s);
		MesquiteMessage.printStackTrace();
	}


	public String checkIntegrity(){
		String warning = null;
		if (taxa == null)
			return null;
		if (numTaxa != taxa.getNumTaxa()) 
			warning = "Error in CharacterData: numTaxa (" + numTaxa + ") != taxa.getNumTaxa() (" + taxa.getNumTaxa() + ") ";
		if (taxaIDs.length !=numTaxa) {
			warning = "Error in CharacterData: numTaxa (" + numTaxa + ") != taxaIDs.length (" + taxaIDs.length + ") (taxa.getNumTaxa() " + taxa.getNumTaxa() + ")";
		}
		for (int i = 0; i<taxa.getNumTaxa() && warning == null; i++)
			if (i>= taxaIDs.length || taxa.getTaxon(i).getID() != taxaIDs[i])
				warning = "Error in CharacterData: id of taxon " + i +" in Taxa doesn't match id recorded in CharacterData";
		for (int i = 0; i<taxa.getNumTaxa() && warning == null; i++)
			if (i>= doubleCheckTaxaIDs.length || taxa.getTaxon(i).getID() != doubleCheckTaxaIDs[i])
				warning = "Error in CharacterData: id of taxon " + i +" in Taxa doesn't match id* recorded in CharacterData";
		return warning;

		/*	if (warning == null)
			return true;
		MesquiteTrunk.mesquiteTrunk.alert(warning + " (" + this + ")");
	//	MesquiteMessage.warnProgrammer(warning + " (" + this + ")");
		return false;*/
	}

	/*
	private void dumpIDs(){
		MesquiteMessage.println("local " + LongArray.toString(taxaIDs));
		String s = "[ ";
		for (int i = 0; i<taxa.getNumTaxa(); i++) //go through list of taxa
			s +=  taxa.getTaxon(i).getID() + "  ";
		MesquiteMessage.println("in taxa " + s + "]");
	}
	 */
	private void reconcileTaxa(int code){
		if (taxa == null)
			return;

		//check id list of taxa to see that it matches; otherwise adjust matrix to match taxa sequence and presence/absence
		int newNumTaxa = taxa.getNumTaxa();
		if (newNumTaxa == numTaxa) {
			if (code== MesquiteListener.PARTS_CHANGED || code== MesquiteListener.PARTS_MOVED) {
				/*go through list of taxa.  If any taxon is not in sequence expected from Taxa then find where it is in the list of taxaID's
				and move it into place*/
				for (int i = 0; i<taxa.getNumTaxa(); i++){ //!!!!! && taxa.getTaxon(i) != null; i++){ //go through list of taxa
					if (taxa.getTaxon(i).getID() != taxaIDs[i]){ //taxon i is not in sequence expected from Taxa
						int loc = LongArray.indexOf(taxaIDs, taxa.getTaxon(i).getID());
						if (loc <0) {
							MesquiteTrunk.mesquiteTrunk.discreetAlert( "Error in CharacterData: taxaID's cannot be reconciled with current Taxa");
							return;
						}
						else {
							//move taxon that should be here into this place
							moveTaxa(loc, 1, i-1);
						}
					}
				}
				Notification notification = new Notification(MesquiteListener.PARTS_CHANGED);
				notification.setSubcodes(new int[] {MesquiteListener.TAXA_CHANGED});
				notifyListeners(this, notification);
			}
			else {
				String s = checkIntegrity();
				if (s !=null) {

					dataIntegrityAlert(s + " (" + this + ")");
				}

			}
		}
		else {
			long[] oldTaxaIDs = taxaIDs;
			if (code== MesquiteListener.PARTS_ADDED) {
				//cycle through finding which taxa in Taxa are not in matrix, and adding them
				for (int i=0; i<newNumTaxa; i++) {
					Taxon t = taxa.getTaxon(i);
					long tid = t.getID();
					if (LongArray.indexOf(oldTaxaIDs, tid)<0){
						int tN = taxa.whichTaxonNumber(t);
						addTaxa(tN-1, 1);
						//should instead find contiguous block and add all at once!
					}
				}

			}
			else if (code== MesquiteListener.PARTS_DELETED) {
				deleteByBlocks(oldTaxaIDs);

			}
			else {
				deleteByBlocks(oldTaxaIDs);
				//cycle through finding which taxa in Taxa are not in matrix, and adding them
				for (int i=0; i<newNumTaxa; i++) {
					Taxon t = taxa.getTaxon(i);
					if (t != null){
						long tid = t.getID();
						if (LongArray.indexOf(oldTaxaIDs, tid)<0){
							int tN = taxa.whichTaxonNumber(t);
							addTaxa(tN-1, 1);
							//should instead find contiguous block and add all at once!
						}
					}
				}
			}
			taxaIDs = taxa.getTaxaIDs();
			String s = checkIntegrity();
			if (s !=null)
				MesquiteTrunk.mesquiteTrunk.alert(s + " (" + this + ")");
			notifyListeners(this, new Notification(MesquiteListener.PARTS_CHANGED));
		}
	}
	private void deleteByBlocks(long[] oldTaxaIDs){
		int firstOfBlock = -1;
		int lastOfBlock = -1;
		//cycle through finding which taxa in matrix have been deleted && deleting them from matrix
		for (int i=numTaxa-1; i>=0; i--) {
			Taxon t = taxa.getTaxonByID(oldTaxaIDs[i]);
			if (t==null) { // taxon was deleted
				if (lastOfBlock <0){  // no current block growing; establish rightmost of block (going backwards!)
					firstOfBlock = i;
					lastOfBlock = i;
				}
				else if (firstOfBlock == i+1)  //last deleted was immediately later, so now turn this into last deleted
					firstOfBlock = i;
				else {  //there is current block, but last deleted was not immediately after, so delete the current block and establish i as start of new block
					deleteTaxa(firstOfBlock, lastOfBlock-firstOfBlock +1);
					firstOfBlock = i;
					lastOfBlock = i;
				}						
			}
			else if (lastOfBlock>=0){  // this taxon was not deleted, but there is a current block, so delete it
				deleteTaxa(firstOfBlock, lastOfBlock-firstOfBlock +1);
				firstOfBlock = -1;
				lastOfBlock = -1;
			}

		}
		if (lastOfBlock>=0){  // this taxon was not deleted, but there is a current block, so delete it
			deleteTaxa(firstOfBlock, lastOfBlock-firstOfBlock +1);
			firstOfBlock = -1;
			lastOfBlock = -1;
		}

	}
	long statesVersion = 0;
	public long getStatesVersion(){
		return statesVersion;
	}
	protected void incrementStatesVersion(){
		statesVersion++;
		checksumValid = false;
	}

	/** extracts data from matrix for character ic, and returns it in a CharacterDistribution object.*/
	public abstract CharacterDistribution getCharacterDistribution(int ic); //TODO: should pass a CharacterDistribution to save the instantiation
	/** returns data from matrix wrapped  in a embedded MCharactersDistribution object.*/
	public abstract MCharactersDistribution getMCharactersDistribution();
	/** extracts data from matrix, and returns it in an independent MCharactersDistribution object.*/
	public MCharactersDistribution getIndependentMCharactersDistribution(){
		CharacterState cs =  null;				
		MCharactersDistribution md = getMCharactersDistribution(); //this will be embedded; need independent
		MAdjustableDistribution mda = md.makeBlankAdjustable();
		for (int ic= 0; ic<getNumChars(); ic++)
			for (int it = 0; it<getNumTaxa(); it++)
				mda.setCharacterState(cs = getCharacterState(cs, ic, it), ic, it);
		return mda;
	}
	/** Fills matrix with data from passed MCharactersDistribution object.*/
	public abstract void setMatrix(MCharactersDistribution states);
	/** creates an empty CharacterState object of the same data type as CharacterData subclass used.*/
	public abstract CharacterState makeCharacterState();
	/** creates an empty CharacterDistribution object of the same data type as CharacterData subclass used.*/
	public abstract CharacterDistribution makeCharacterDistribution();
	/** creates an empty CharacterData object of the same data type as CharacterData subclass used.*/
	public abstract CharacterData makeCharacterData();
	/** creates an empty CharacterData object of the same data type as CharacterData subclass used, based on CharMatrixManager & Taxa passed.*/
	public abstract CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa); 
	/** creates a CharacterData object of the same data type as CharacterData subclass used, with ntaxa and nchars.*/
	public abstract CharacterData makeCharacterData(int ntaxa, int nchars);

	/** trades the states of character ic and ic2 in taxon it.  Used for reversing sequences (for example).*/
	public void tradeStatesBetweenCharacters(int ic, int ic2, int it, boolean adjustCellLinked){
		//trade cell footnotes
		if (footnotes!= null)  {
			if (ic<footnotes.length && ic2<footnotes.length && it<footnotes[ic].length) {
				String ct = footnotes[ic][it];
				footnotes[ic][it] = footnotes[ic2][it];
				footnotes[ic2][it] = ct;
			}
		}
		//trade cell objects
		if (cellObjects.size()>0){
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object ob = objArray.getValue(ic, it);
				objArray.setValue(ic, it, objArray.getValue(ic2, it));
				objArray.setValue(ic2, it, ob);
			}
		}
		boolean t = cellObjectsDisplay[ic][it];
		cellObjectsDisplay[ic][it] = cellObjectsDisplay[ic2][it];
		cellObjectsDisplay[ic2][it] = t;
		if (adjustCellLinked){
			for (int i=0; i<linkedDatas.size(); i++){
				CharacterData d= (CharacterData)linkedDatas.elementAt(i);
				d.tradeStatesBetweenCharacters(ic,ic2,it,false);
			}
		}
	}
	/** trades the states of character ic between taxa it and it2.  Used for reshuffling (for example).*/
	public void tradeStatesBetweenTaxa(int ic, int it, int it2){
		//trade cell footnotes
		if (footnotes!= null)  {
			if (ic<footnotes.length && it2<footnotes[ic].length && it<footnotes[ic].length) {
				String ct = footnotes[ic][it];
				footnotes[ic][it] = footnotes[ic][it2];
				footnotes[ic][it2] = ct;
			}
		}
		//trade cell objects
		if (cellObjects.size()>0){
			for (int k =0; k<cellObjects.size(); k++){
				Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
				Object ob = objArray.getValue(ic, it);
				objArray.setValue(ic, it, objArray.getValue(ic, it2));
				objArray.setValue(ic, it2, ob);
			}
		}
		boolean t = cellObjectsDisplay[ic][it];
		cellObjectsDisplay[ic][it] = cellObjectsDisplay[ic][it2];
		cellObjectsDisplay[ic][it2] = t;
	}

	/** returns whether the matrix would prefer to have columns sized individually in editors.  Default is true.*/
	public boolean pleaseAutoSizeColumns() {
		return columnWidthAutoSize;
	}
	/** Sets whether to autoSize columns, if this is setable.*/
	public void setAutoSizeColumns(boolean autoSize) {
		columnWidthAutoSize = autoSize;
	}
	/** returns default column width in editors.  Default is 16.*/
	public int getDefaultColumnWidth() {
		return 16;
	}
	/** returns default narrow column width in editors.  Default is 16.*/
	public int getNarrowDefaultColumnWidth() {
		return 16;
	}

	/** appends to buffer string describing the state(s) of character ic in taxon it.*/
	public abstract void statesIntoStringBuffer(int ic, int it, StringBuffer sb, boolean forDisplay, boolean includeInapplicable, boolean includeUnassigned);
	/** appends to buffer string describing the state(s) of character ic in taxon it.*/
	public abstract void statesIntoStringBuffer(int ic, int it, StringBuffer sb, boolean forDisplay);
	/** appends to buffer string describing the state(s) of character ic in taxon it.*/
	public abstract void statesIntoNEXUSStringBuffer(int ic, int it, StringBuffer sb);
	/**Set the state at character ic and taxon it from the string in the parser, beginning at current parser position in the string. 
	Updates current position in string.  If fromEditor is true, should assume whole string is state. Returns a result code (OK, ERROR, EOL, COMMENT).
	EOL is returned if data are interleaved and end of line found.  If there is an error or a comment, an error message or the comment,
	respectively, will be returned in the result MesquiteString.*/
	public abstract int setState(int ic, int it, Parser parser, boolean fromEditor, MesquiteString result);

	/**Override to provide faster and more risky state setting for reading large files.  Assumes it and ic are in bounds, and parser string is not blank*/
	public int setStateQuickNexusReading(int ic, int it, Parser parser){
		return setState(ic, it, parser, false, null);
	}
	/** sets the state of character ic in taxon it from CharacterState cs*/
	public abstract void setState(int ic, int it, CharacterState cs);

	/** returns whether the character ic in taxon it has a terminal inapplicable*/
	public boolean isTerminalInapplicable(int ic, int it){
		if (!isInapplicable(ic,it)) 
			return false;
		if (firstApplicable!=null && it<firstApplicable.length && (firstApplicable[it]<0 || ic<firstApplicable[it]))
			return true;
		if (lastApplicable!=null && it<lastApplicable.length && ic>lastApplicable[it] && lastApplicable[it]>=0)
			return true;
		/*		boolean terminal = true;
		for (int i=ic-1; i>=0; i--)
			if (!isInapplicable(i,it)){
				terminal=false;
				break;
			}
		if (terminal)
			return true;
		terminal=true;
		for (int i=ic+1; i<numChars; i++)
			if (!isInapplicable(i,it))
				return false;
		 */
		return false;

	}

	/** returns whether the character ic is inapplicable to taxon it*/
	public abstract boolean isInapplicable(int ic, int it);
	/** returns whether the character ic is entirely inapplicable codings*/

	/** sets the state of character ic in taxon it to inapplicable*/
	public  abstract void setToInapplicable(int ic, int it);
	/** sets the state of all characters in taxon it to the default state (which in some circumstances may be inapplicable, e.g. gaps for molecular data)*/
	public void setToInapplicable(int it) {
		for (int ic=0; ic<numChars; ic++)
			setToInapplicable(ic,it);
	}
	/** sets the state of character ic in taxon it to unassigned*/
	public  abstract void setToUnassigned(int ic, int it);
	/** sets the state of all characters in taxon it to unassigned*/
	public void setToUnassigned(int it) {
		for (int ic=0; ic<numChars; ic++)
			setToUnassigned(ic,it);
	}
	/** sets the state of character ic in taxon it to the default state (which in some circumstances may be inapplicable, e.g. gaps for molecular data)*/
	public  abstract void deassign(int ic, int it);

	public boolean hasDataForTaxon(int it){
		int numChars = getNumChars();
		for (int ic=0; ic<numChars; ic++) {
			if (!isInapplicable(ic, it) && !isUnassigned(ic, it))
				return true;
		}		
		return false;
	}
	public boolean hasMissingForTaxon(int it){
		int numChars = getNumChars();
		for (int ic=0; ic<numChars; ic++) {
			if (isUnassigned(ic, it))
				return true;
		}		
		return false;
	}
	public boolean hasDataForTaxon(int it, boolean considerExcluded){
		int numChars = getNumChars();
		for (int ic=0; ic<numChars; ic++) {
			if (!isInapplicable(ic, it) && !isUnassigned(ic, it) && (considerExcluded || isCurrentlyIncluded(ic)))
				return true;
		}		
		return false;
	}

	public boolean hasDataForTaxa(int itStart, int itEnd){
		for (int it=itStart; it<=itEnd; it++)
			if (hasDataForTaxon(it))
				return true;
		return false;
	}

	public boolean hasDataForCharacter(int ic){
		int numTaxa = getNumTaxa();
		for (int it=0; it<numTaxa; it++) {
			if (!isInapplicable(ic, it) && !isUnassigned(ic, it))
				return true;
		}		
		return false;
	}

	public boolean hasDataForCharacters(int icStart, int icEnd){
		for (int ic=icStart; ic<=icEnd; ic++)
			if (hasDataForCharacter(ic))
				return true;
		return false;
	}

	/*.................................................................................................................*/
	public boolean anyApplicableBefore(int ic, int it){
		for (int i = 0; i<ic; i++)
			if (!isInapplicable(i,it))
				return true;

		return false;
	}
	/*.................................................................................................................*/
	public boolean anyApplicableAfter(int ic, int it) {
		int numChars = getNumChars();
		for (int i = ic+1; i<numChars; i++)
			if (!isInapplicable(i, it))
				return true;
		return false;
	}

	public boolean removeTaxaThatAreEntirelyGaps(){
		boolean removedSome = false;
		int numT = getNumTaxa();
		for (int it = numT; it>=0; it--){
			if (entirelyInapplicableTaxon(it)) {
				int numToDelete = 1;
				int firstToDelete = it;
				for (int it2 =it-1; it2>=0; it2--){
					if (entirelyInapplicableTaxon(it2)) {
						numToDelete++;
						firstToDelete= it2;
					} else break;
				}
				deleteTaxa(firstToDelete, numToDelete);
				it=it-numToDelete+1;
				removedSome=true;
			}
		}
		return removedSome;
	}




	public boolean removeCharactersThatAreEntirelyGaps(int icStart, int icEnd, boolean notify){
		boolean removedSome = false;
		for (int ic = icEnd; ic>=icStart; ic--){
			if (entirelyInapplicable(ic)) {
				int numToDelete = 1;
				int firstToDelete = ic;
				for (int ic2 =ic-1; ic2>=0; ic2--){
					if (entirelyInapplicable(ic2)) {
						numToDelete++;
						firstToDelete= ic2;
					} else break;
				}
				deleteCharacters(firstToDelete, numToDelete, notify);
				deleteInLinked(firstToDelete,numToDelete,notify);
				ic=ic-numToDelete+1;
				removedSome=true;
			}
		}
		return removedSome;
	}

	public boolean removeCharactersThatAreEntirelyGaps(boolean notify){
		boolean removedSome = false;
		for (int ic = getNumChars()-1; ic>=0; ic--){
			if (entirelyInapplicable(ic)) {
				int numToDelete = 1;
				int firstToDelete = ic;
				for (int ic2 =ic-1; ic2>=0; ic2--){
					if (entirelyInapplicable(ic2)) {
						numToDelete++;
						firstToDelete= ic2;
					} else break;
				}
				deleteCharacters(firstToDelete, numToDelete, notify);
				deleteInLinked(firstToDelete,numToDelete,notify);
				ic=ic-numToDelete+1;
				removedSome=true;
			}
		}
		return removedSome;
	}



	public boolean removeCharactersThatAreEntirelyUnassigned(boolean notify){
		boolean removedSome = false;
		for (int ic = getNumChars()-1; ic>=0; ic--){
			if (entirelyUnassigned(ic)) {
				int numToDelete = 1;
				int firstToDelete = ic;
				for (int ic2 =ic-1; ic2>=0; ic2--){
					if (entirelyUnassigned(ic2)) {
						numToDelete++;
						firstToDelete= ic2;
					} else break;
				}
				deleteCharacters(firstToDelete, numToDelete, notify);
				deleteInLinked(firstToDelete,numToDelete,notify);
				ic=ic-numToDelete+1;
				removedSome=true;
			}
		}
		return removedSome;
	}
	public boolean removeCharactersThatAreEntirelyUnassignedOrInapplicable(boolean notify){
		boolean removedSome = false;
		for (int ic = getNumChars()-1; ic>=0; ic--){
			if (entirelyUnassignedOrInapplicable(ic)) {
				int numToDelete = 1;
				int firstToDelete = ic;
				for (int ic2 =ic-1; ic2>=0; ic2--){
					if (entirelyUnassignedOrInapplicable(ic2)) {
						numToDelete++;
						firstToDelete= ic2;
					} else break;
				}
				deleteCharacters(firstToDelete, numToDelete, notify);
				deleteInLinked(firstToDelete,numToDelete,notify);
				ic=ic-numToDelete+1;
				removedSome=true;
			}
		}
		return removedSome;
	}

	public boolean entirelyUnassigned(int ic){
		for (int it = 0; it< getNumTaxa(); it++)
			if (!isUnassigned(ic, it))
				return false;
		return true;
	}
	/** returns true iff all data is at left, with no inapplicable before the data ends */
	public boolean isUnalignedLeft(int it){
		boolean inapplicableFound = false;
		for (int ic = 0; ic< numChars; ic++)
			if (isInapplicable(ic, it)) {
				inapplicableFound = true;
			} else if (inapplicableFound)
				return false;
		return true;
	}

	public int numInapplicable(int it){
		int count=0;
		for (int ic = 0; ic< numChars; ic++)
			if (isInapplicable(ic, it))
				count++;
		return count;
	}
	public int numUnassigned(int it){
		int count=0;
		for (int ic = 0; ic< numChars; ic++)
			if (isUnassigned(ic, it))
				count++;
		return count;
	}
	public int numNotInapplicableNotUnassigned(int it){
		int count=0;
		for (int ic = 0; ic< numChars; ic++)
			if (!isUnassigned(ic, it) && !isInapplicable(ic,it))
				count++;
		return count;
	}
	public boolean entirelyInapplicable(int ic){
		for (int it = 0; it< numTaxa; it++)
			if (!isInapplicable(ic, it))
				return false;
		return true;
	}
	public boolean entirelyInapplicableTaxon(int it){
		for (int ic = 0; ic< numChars; ic++)
			if (!isInapplicable(ic, it))
				return false;
		return true;
	}
	public boolean inapplicableBlock(int icStart, int icEnd, Bits whichTaxa, int firstTaxon, int lastTaxon){
		for (int it = firstTaxon; it>=0 && it<=lastTaxon && it< numTaxa; it++)
			if (whichTaxa.isBitOn(it))
				for (int ic = icStart; ic< getNumChars() && ic<=icEnd; ic++)
					if (!isInapplicable(ic, it))
						return false;
		return true;
	}
	public boolean inapplicableBlock(int icStart, int icEnd, Bits whichTaxa){
		for (int it = 0; it< numTaxa; it++)
			if (whichTaxa.isBitOn(it))
				for (int ic = icStart; ic< getNumChars() && ic<=icEnd; ic++)
					if (!isInapplicable(ic, it))
						return false;
		return true;
	}

	public boolean inapplicableBlock(int icStart, int icEnd, int itStart, int itEnd){
		for (int it = itStart; it< numTaxa && it<=itEnd; it++)
			for (int ic = icStart; ic< getNumChars() && ic<=icEnd; ic++)
				if (!isInapplicable(ic, it))
					return false;
		return true;
	}
	public boolean applicableInBothCharacters(int ic1, int ic2, int itStart, int itEnd){
		if (ic1<0 || ic1>=numChars || ic2<0 || ic2>=numChars)
			return false;
		for (int it = itStart; it< numTaxa && it<=itEnd; it++)
			if (!isInapplicable(ic1, it) && !isInapplicable(ic2, it))
				return true;
		return false;
	}
	public boolean applicableInBothCharacters(int ic1, int ic2, Bits whichTaxa){
		if (ic1<0 || ic1>=numChars || ic2<0 || ic2>=numChars)
			return false;
		for (int it = 0; it< numTaxa; it++)
			if (whichTaxa.isBitOn(it) && !isInapplicable(ic1, it) && !isInapplicable(ic2, it))
				return true;
		return false;
	}
	public boolean entirelyUnassignedOrInapplicable(int ic){
		for (int it = 0; it< numTaxa; it++)
			if (!isInapplicable(ic, it) && !isUnassigned(ic, it))
				return false;
		return true;
	}
	/** returns whether the state of character ic is missing in taxon it*/
	public abstract boolean isUnassigned(int ic, int it);

	/** returns whether the state of character ic is valid in taxon it*/
	public boolean isValid(int ic, int it){
		return true;
	}

	/** returns whether the character ic is included (i.e. not currently excluded)*/
	public boolean isCurrentlyIncluded(int ic){
		CharInclusionSet incl = (CharInclusionSet)getCurrentSpecsSet(CharInclusionSet.class);
		return (incl==null || incl.isBitOn(ic));

	}
	/** returns number of currently included characters*/
	public int numCharsCurrentlyIncluded(){
		CharInclusionSet incl = (CharInclusionSet)getCurrentSpecsSet(CharInclusionSet.class);
		if (incl!=null)
			return incl.numBitsOn();
		return numChars;
	}
	/** returns number of currently included characters*/
	public int numCharsCurrentlyIncluded(boolean countSelectedOnly){
		CharInclusionSet incl = (CharInclusionSet)getCurrentSpecsSet(CharInclusionSet.class);
		if (countSelectedOnly) {
			int count = 0;
			for (int ic=0; ic<numChars; ic++){
				if (incl.isBitOn(ic) && selected.isBitOn(ic))
					count++;
			}
			return count;
		} else {
			if (incl!=null)
				return incl.numBitsOn();
		}
		return numChars;
	}
	public int firstApplicable(Bits whichTaxa){
		int first = -1;
		for (int it = 0; it<numTaxa; it++)
			if (whichTaxa.isBitOn(it)) {
				int firstInTaxon = firstApplicable(it);
				if (first==-1)
					first=firstInTaxon;
				else if (firstInTaxon>=0)
					first = MesquiteInteger.minimum(first,firstInTaxon);
			}

		return first;
	}

	public int firstApplicable(int itStart, int itEnd){
		int first = -1;
		for (int it = itStart; it<=itEnd; it++){
			int firstInTaxon = firstApplicable(it);
			if (first==-1)
				first=firstInTaxon;
			else if (firstInTaxon>=0)
				first = MesquiteInteger.minimum(first,firstInTaxon);
		}

		return first;
	}
	public int firstApplicable(int it){
		for (int ic= 0; ic<numChars; ic++) {
			if (!isInapplicable(ic,it))
				return ic;
		}
		return -1;
	}
	public int lastApplicable(Bits whichTaxa){
		int last = -1;
		for (int it = 0; it<numTaxa; it++)
			if (whichTaxa.isBitOn(it)){
				int lastInTaxon = lastApplicable(it);
				last = MesquiteInteger.maximum(last,lastInTaxon);
			}
		return last;
	}
	public int lastApplicable(int itStart, int itEnd){
		int last = -1;
		for (int it = itStart; it<=itEnd; it++){
			int lastInTaxon = lastApplicable(it);
			last = MesquiteInteger.maximum(last,lastInTaxon);
		}
		return last;
	}
	public int lastApplicable(){
		return lastApplicable(0,numTaxa);
	}
	public int lastApplicable(int it){
		for (int ic= numChars-1; ic>=0; ic--) {
			if (!isInapplicable(ic,it))
				return ic;
		}
		return -1;
	}
	/*.................................................................................................................*/
	/** if state of character ic, taxon it is applicable, returns ic; otherwise, returns the next character that has applicable data*/
	public int getNumCharsIncluded() {
		int count=0;
		for (int i = 0; i< getNumChars(); i++){
			if (isCurrentlyIncluded(i))
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public String getExcludedCharactersList(CharSpecsSet specsSet){
		if (specsSet ==null || !(specsSet instanceof CharInclusionSet))
			return "";
		CharInclusionSet inclusionSet = (CharInclusionSet)specsSet;
		String sT = "";
		if (inclusionSet!=null) {
			int continuing = 0;
			int lastWritten = -1;
			for (int ic=0; ic<getNumChars(); ic++) {
				if (!inclusionSet.isBitOn(ic)) {
					if (continuing == 0) {
						sT += " " + CharacterStates.toExternal(ic);
						lastWritten = ic;
						continuing = 1;
					}
					else if (continuing == 1) {
						sT += "-";
						continuing = 2;
					}
				}
				else if (continuing>0) {
					if (lastWritten !=ic-1){
						sT += " " + CharacterStates.toExternal(ic-1);
						lastWritten = ic-1;
					}
					else
						lastWritten = -1;
					continuing = 0;
				}

			}
			if (continuing>1)
				sT += " " + CharacterStates.toExternal(getNumChars()-1);

		}
		return sT;
	}
	/*.................................................................................................................*/
	/** if state of character ic, taxon it is applicable, returns ic; otherwise, returns the previous character that has applicable data*/
	public int thisOrPreviousApplicableChar(int icC, int it){
		for (int ic= icC; ic>=0; ic--) {
			if (!isInapplicable(ic,it))
				return ic;
		}
		return -1;
	}
	/*.................................................................................................................*/
	/** if state of character ic, taxon it is applicable, returns ic; otherwise, returns the next character that has applicable data*/
	public int thisOrNextApplicableChar(int ic, int it) {
		for (int i = 0; ic+i< getNumChars(); i++){
			if (!isInapplicable(ic+i,it)) {
				return ic+i;
			}
		}
		return -1;
	}
	/*..........................................   ..................................................*/
	public boolean dataMatches(int it, int checkChar, int masterTaxon, int masterStart, int masterEnd, MesquiteInteger matchEnd, boolean allowMissing, boolean allowNearExact, double matchFraction, CharacterState cs1, CharacterState cs2) {
		if (checkChar + (masterEnd-masterStart)>=getNumChars()){ //would extend past end of data
			return false;
		}
		int mismatches = 0;
		int allowedMismatches = (int)((masterEnd-masterStart+1) * (1.0-matchFraction));
		for (int ic= 0; ic < masterEnd-masterStart+1; ic++){
			cs1 = getCharacterState(cs1, checkChar + ic, it);  
			cs2 = getCharacterState(cs2,masterStart+ic, masterTaxon);  // 
			if (!cs2.equals(cs1,allowMissing, allowNearExact)) {
				mismatches++;
				if (matchFraction==1.0 || mismatches>allowedMismatches) {
					return false;
				}
			} 
			matchEnd.setValue(ic);
		}
		return true;

	}
	/*..........................................   ..................................................*/
	public boolean dataMatches(int it, int checkChar, int masterTaxon, int masterStart, int masterEnd, MesquiteInteger matchEnd, boolean allowMissing, boolean allowNearExact, CharacterState cs1, CharacterState cs2) {
		return dataMatches(it, checkChar, masterTaxon, masterStart, masterEnd, matchEnd, allowMissing, allowNearExact, 1.0, cs1, cs2);
	}
	/** returns the state of character ic in taxon it*/
	public abstract CharacterState getCharacterState(CharacterState cs, int ic, int it);


	/*..........................................   ..................................................*/
	/** returns a CharacterState array containing the data in taxon it from icStart to icEnd*/
	public CharacterState[] getCharacterStateArray(int it, int icStart, int icEnd){
		CharacterState[] csArray = new CharacterState[icEnd-icStart+1];
		for (int ic = icStart; ic<=  icEnd && ic < getNumChars(); ic++){
			csArray[ic-icStart] = makeCharacterState();
			csArray[ic-icStart] = getCharacterState(csArray[ic-icStart], ic,  it);
		}
		return csArray;
	}
	/*..........................................   ..................................................*/



	/** sets the symbol used for inapplicable character (e.g., gap)*/
	public void setInapplicableSymbol(char inapp) {
		setDirty(true);
		inapplicableChar = inapp;
	}
	/** returns symbol used for inapplicable character (e.g., gap)*/
	public char getInapplicableSymbol() {
		return inapplicableChar;
	}
	/** sets the symbol used for missing data (unassigned) character*/
	public void setUnassignedSymbol(char mc) { 
		setDirty(true);
		missingChar = mc;
	}
	/** returns symbol used for missing data (unassigned) character*/
	public char getUnassignedSymbol() { 
		return missingChar;
	}
	/** sets the symbol used for matchChar*/
	public void setMatchChar(char mc) { 
		matchChar = mc;
	}
	/** returns symbol used for matchChar*/
	public char getMatchChar() { 
		return matchChar;
	}
	/** sets whether cells are to be colored by default*/
	public void setColorCellsByDefault(boolean colorCells){
		colorCellsByDefault = colorCells;
	}

	/** returns whether cells are to be colored by default*/
	public boolean colorCellsByDefault(){
		return colorCellsByDefault;
	}
	/** returns the color of character ic; e.g., to indicate codon positions */
	public Color getDefaultCharacterColor(int ic){
		return null;
	}
	/** returns the dark color of character ic; e.g., to indicate codon positions */
	public Color getDarkDefaultCharacterColor(int ic){
		return null;
	}
	/** returns a String summarizing the states of a character (e.g., "2 states", "0.1-0.9").*/
	public  String getStatesSummary(int ic, boolean selectedOnly){
		return getStatesSummary(ic);
	}

	/** returns a String summarizing the states of a character (e.g., "2 states", "0.1-0.9").*/
	public abstract String getStatesSummary(int ic);


	/*.................................................................................................................*/
	/** Indicates whether the data are molecular sequence data or not */ 
	public boolean isMolecularSequence() {
		return false;
	}

	/** Indicates the type of character stored */ 
	public abstract Class getStateClass();
	/** Gets the color representing state(s) of character ic in taxon it */ 
	public abstract Color getColorOfStates(int ic, int it);

	/*.................................................................................................................*/
	/** returns the default character model for the paradigm (e.g., "parsimony") given in the String */ 
	public abstract CharacterModel getDefaultModel(String paradigm);
	/*.................................................................................................................*/
	/** returns a list of names of the characters (StringLister interface)*/
	public String[] getStrings(){
		String[] s = new String[numChars];
		for (int i=0; i<numChars; i++){
			if (characterNames == null ||  characterNames[i]==null)
				s[i]= "Character " + CharacterStates.toExternal(i); 
			else
				s[i]= Integer.toString(CharacterStates.toExternal(i)) + ". " + characterNames[i]; 
		}
		return s;
	}
	/*.................................................................................................................*/
	/** returns whether whether any characters have names*/
	public boolean characterNamesExist() {
		if (characterNames == null)
			return false;
		for (int ic=0; ic<numChars; ic++) {
			if (characterNames[ic]!=null)
				return true; 
		}
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether character ic has a name*/
	public boolean characterHasName(int ic) {
		if (ic<0 || ic>=numChars) {
			return false; 
		}
		else if (characterNames == null ||  characterNames[ic]==null)
			return false; 
		else
			return true;
	}
	/*.................................................................................................................*/
	/** returns the name of character ic*/
	public String getCharacterName(int ic) {
		if (ic<0 || ic>=numChars) {
			MesquiteMessage.warnProgrammer("Error: character number out of bounds (getCharacterName) " + ic);
			return ""; 
		}
		else if (characterNames == null ||  characterNames[ic]==null)
			return "Character " + CharacterStates.toExternal(ic); 
		else
			return characterNames[ic];
	}
	/*.................................................................................................................*/
	/** sets the name of character ic*/
	public void setCharacterName(int ic, String name) {
		if (ic<0 || ic>=numChars) {
			MesquiteMessage.warnProgrammer("Error: character number out of bounds (setCharacterName) " + ic);
		}
		else if (characterNames == null)
			;
		else {
			try {
				int i = Integer.parseInt(name);
				name = "#" + name;  //autoadjust to add character
			}
			catch (NumberFormatException e){
			}
			if (StringUtil.blank(name))
				name = null;
			characterNames[ic]= name;
			notifyOfChangeLowLevel(MesquiteListener.NAMES_CHANGED, ic, -1, 0);  
			notifyListeners(this, new Notification(NAMES_CHANGED, new int[] {ic}));
		}
	}
	/*.................................................................................................................*/
	public String checkNameLegality(int it, String s){
		if (s==null) {
			return null; //characters can have null names
		}
		try {
			int i = Integer.parseInt(s);
			return "The character name \"" + s + "\" is illegal because it consists only of numbers";
		}
		catch (NumberFormatException e){
		}
		return null;
	}
	/*.................................................................................................................*/
	/** sets the annotation (footnote) of character ic, taxon it*/
	public void setAnnotation(int ic, int it, String expl) {
		if (ic<0 || ic>=numChars || it<0 || it>=numTaxa) {
			if (ic<-2|| ic>=numChars || it<-2 || it>=numTaxa)
				MesquiteMessage.println("Error: character or taxon number out of bounds (setAnnotation) " + ic);
		}
		else {
			if (footnotes == null){
				footnotes = new String[numChars][numTaxa];
			}
			if (!(ic<0 || ic>=footnotes.length || it<0 || footnotes[ic]==null || it>=footnotes[ic].length)){
				if (expl == null)
					footnotes[ic][it]= null;
				else
					footnotes[ic][it]= new String(expl);
				notifyListeners(this, new Notification(ANNOTATION_CHANGED));
			}
		}
	}
	/*.................................................................................................................*/
	/** gets the annotation (footnote) of character ic, taxon it*/
	public String getAnnotation(int ic, int it) {
		if (ic<0 || ic>=numChars || it<0 || it>=numTaxa) {
			return null;
		}
		else {
			if (footnotes == null)
				return null;
			if (ic<0 || ic>=footnotes.length || it<0 || footnotes[ic]==null || it>=footnotes[ic].length){
				return null;
			}
			return footnotes[ic][it];
		}
	}
	/*.................................................................................................................*/
	/** returns whether the matrix cell has been changed since the last file save*/
	public boolean getChangedSinceSave(int ic, int it) {
		if (ic<0 || ic>=numChars || it<0 || it>=numTaxa) {
			return false;
		}
		else if (changedSinceSave!=null && !(ic<0 || ic>=changedSinceSave.length || it<0 || changedSinceSave[ic]==null || it>=changedSinceSave[ic].length))
			return changedSinceSave[ic][it];
		return false;
	}
	/*.................................................................................................................*/
	/** resets to false for all cells whether the matrix cell has been changed since the last file save*/
	public void resetChangedSinceSave() {
		boolean c = false;
		if (changedSinceSave!=null)
			for (int ic = 0; ic<changedSinceSave.length; ic++)
				for (int it=0; it<changedSinceSave[ic].length; it++){
					if (changedSinceSave[ic][it]) {
						changedSinceSave[ic][it] = false;
						c = true;
					}
				}
		watchForChange = true;	
		checksumValid = false;
		if (c)
			notifyListeners(this, new Notification(MesquiteListener.ANNOTATION_CHANGED));
	}
	/*.................................................................................................................*/
	public void resetCellMetadata() {
		resetChangedSinceSave();
		calculateFirstLastApplicable();
	}
	/*.................................................................................................................*/
	public Vector getCellObjectsVector(){
		return cellObjects;
	}
	/*.................................................................................................................*/
	public Object2DArray getWhichCellObjects(NameReference nRef){
		if (cellObjects!=null && nRef!=null) {
			for (int i=0; i<cellObjects.size(); i++) {
				Object2DArray b = (Object2DArray)cellObjects.elementAt(i);
				if (b !=null && nRef.equals(b.getNameReference())) {
					return b; 
				}
			}
		}
		return null;
	}
	/*.................................................................................................................*/
	/** sets whether something drawing the matrix should check for special symbols for cell ic, it*/
	public void setCellObjectDisplay(int ic, int it) {
		if (ic<0 || ic>=numChars || it<0 || it>=numTaxa) {
			return;
		}
		else if (cellObjectsDisplay!=null && !(ic<0 || ic>=cellObjectsDisplay.length || it<0 || cellObjectsDisplay[ic]==null || it>=cellObjectsDisplay[ic].length)){
			cellObjectsDisplay[ic][it] = true;
		}
	}
	/*.................................................................................................................*/
	/** returns whether something drawing the matrix should check for special symbols for cell ic, it*/
	public boolean getCellObjectDisplay(int ic, int it) {
		if (ic<0 || ic>=numChars || it<0 || it>=numTaxa) {
			return false;
		}
		else if (cellObjectsDisplay!=null && !(ic<0 || ic>=cellObjectsDisplay.length || it<0 || cellObjectsDisplay[ic]==null || it>=cellObjectsDisplay[ic].length))
			return cellObjectsDisplay[ic][it];
		return false;
	}
	/*.................................................................................................................*/
	/** gets the object of type "name" attached to character ic, taxon it*/
	public void setCellObject(NameReference nr, int ic, int it, Object obj) {
		if (ic<0 || ic>=numChars || it<0 || it>=numTaxa) {
			return;
		}
		else {
			Object2DArray array = getOrMakeCellObjects(nr);
			if (array !=null) {
				array.setValue(ic, it, obj);
			}
		}
	}
	/*.................................................................................................................*/
	/** gets the object of type "name" attached to character ic, taxon it*/
	public Object getCellObject(NameReference nr, int ic, int it) {
		if (ic<0 || ic>=numChars || it<0 || it>=numTaxa) {
			return null;
		}
		else {
			Object2DArray array = getWhichCellObjects(nr);
			if (array!=null)
				return array.getValue(ic, it);
		}
		return null;
	}
	/*.................................................................................................................*/
	/** gets the object of type "name" attached to character ic, taxon it*/
	public Object2DArray getOrMakeCellObjects(NameReference nr) {
		Object2DArray array = getWhichCellObjects(nr);
		if (array==null) {
			array = new Object2DArray(numChars, numTaxa);
			array.setNameReference(nr);
			cellObjects.addElement(array);
		}
		return array;
	}
	/*.................................................................................................................*/
	/** removes the array of object of type "name" attached to character ic, taxon it*/
	public void removeCellObjects(NameReference nr) {
		Object2DArray array = getWhichCellObjects(nr);
		if (array!=null) 
			cellObjects.removeElement(array);

	}
	/*.................................................................................................................*/
	//this stores matrix-specific information on the taxa, e.g. regarding the sequences
	Associable taxaInfo; 
	public Associable getTaxaInfo(boolean makeIfNotPresent){
		if (makeIfNotPresent && taxaInfo == null){
			taxaInfo = new TaxaInfo(numTaxa, this);
		}
		return taxaInfo;
	}

	/*.................................................................................................................*/
	boolean watchForChange = false;
	/*.................................................................................................................*/
	int suppressHistoryStamp = 0;
	public void incrementSuppressHistoryStamp(){
		suppressHistoryStamp++;
	}
	public void decrementSuppressHistoryStamp(){
		suppressHistoryStamp--;
	}
	/*.................................................................................................................*/
	Vector llListeners = new Vector();

	public void addLowLevelListener(LowLevelListener listener){
		llListeners.addElement(listener);
	}
	public void removeLowLevelListener(LowLevelListener listener){
		llListeners.removeElement(listener);
	}
	// -1 -2 means that character number/order/etc changed; -2 -1 means that taxa number/order/etc changed
	void notifyOfChangeLowLevel(int code, int i1, int i2, int i3){
		if (MesquiteThread.getListenerSuppressionLevel()>0)
			return;

		for (int i = 0; i< llListeners.size(); i++){
			LowLevelListener lll = (LowLevelListener)llListeners.elementAt(i);
			try {
				lll.llChange(this, code, i1, i2, i3);
			}
			catch (Throwable e){  //don't want problem in one of these to stop notifications
			}
		}
	}

	/* ................................................................................................................. */
	String nextPasteString(StringBuffer sb) {
		if (sb.length() == 0)
			return null;
		String result = sb.substring(0, 1);
		sb.delete(0, 1);
		return result;
	}
	/* ............................................................................................................... */
	public void pasteCell(Parser parser, int column, int row, String s) { 
		if (StringUtil.blank(s))
			return;
		CharacterState csBefore = getCharacterState(null, column, row);
		parser.setString(s);
		MesquiteString result = new MesquiteString("");
		int response = setState(column, row, parser, true, result); // receive errors?
		if (response == CharacterData.OK) {
			CharacterState csAfter = getCharacterState(null, column, row);
			if (csBefore != null && !csBefore.equals(csAfter)) {
				int[] subcodes = new int[] { MesquiteListener.SINGLE_CELL };
				if (csBefore.isInapplicable() == csAfter.isInapplicable())
					subcodes = new int[] { MesquiteListener.SINGLE_CELL, MesquiteListener.CELL_SUBSTITUTION };
			}
		}
	}	
	/* ................................................................................................................. */
	boolean pasteDataOld(int it, String s) {
		String[] lines = StringUtil.getLines(s);
		StringBuffer sb = new StringBuffer(lines[0]);
		Parser parser = new Parser();
		if (sb.indexOf("\t") >= 0) {
			String result = sb.substring(0, sb.indexOf("\t"));
			sb.delete(0, sb.indexOf("\t") + 1);
		}
		for (int i = 0; i < numChars && sb.length()>0; i++) {
			pasteCell(parser, i, it, nextPasteString(sb));

		}
		return true;
	}
	/* ................................................................................................................. */
	boolean pasteData(int it, String s) {
		String[] lines = StringUtil.getLines(s);
		Parser parser = new Parser();
		MesquiteInteger pos = new MesquiteInteger(0);
		s = lines[0];
		String t = StringUtil.getNextTabbedToken(s, pos);
		for (int i = 0; i < numChars && pos.getValue()<s.length(); i++) {
			t = StringUtil.getNextTabbedToken(s, pos);
			pasteCell(parser, i, it, t);
		}
		return true;
	}


	/* ................................................................................................................. */

	public void pasteDataFromStringIntoTaxon(int it, String s) {
		if (StringUtil.notEmpty(s)) {
			String[] lines = StringUtil.getLines(s);
			if (lines.length==1) {
				pasteData(it, s);
				notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED));
			}
		}
	}
	/* ................................................................................................................. */

	public void pasteDataIntoTaxon(int it) {

		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = clip.getContents(this);
		try {
			String s = (String) t.getTransferData(DataFlavor.stringFlavor);
			pasteDataFromStringIntoTaxon(it, s);
		} catch (Exception e) {
			MesquiteMessage.printStackTrace(e);
		}
	}

	/*.................................................................................................................*/

	public void copyDataFromRowIntoBuffer(int row, StringBuffer sb) {
		if (sb ==null)
			return;
		String t = null;
		t = taxa.getTaxonName(row);
		if (t != null)
			sb.append(t);
		sb.append('\t');

		for (int i = 0; i < numChars; i++) {
			statesIntoStringBuffer(i, row, sb, false);
			sb.append('\t');
		}
		sb.append(StringUtil.lineEnding());
	}
	/*.................................................................................................................*/

	public void copyDataFromRow(int row) {
		StringBuffer sb = new StringBuffer();
		copyDataFromRowIntoBuffer(row, sb);

		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection ss = new StringSelection(sb.toString());
		clip.setContents(ss, ss);
	}

	/*.................................................................................................................*/
	NameReference historyRef = NameReference.getNameReference("ChangeHistory");
	/** Marks the data for character ic, taxon it as having changed*/
	public void stampHistoryChange(int ic, int it) {
		if (ic<0 || ic>=numChars || it<0 || it>=numTaxa || suppressHistoryStamp>0) { //|| MesquiteTrunk.currentAuthor == null
			return;
		}
		else {
			if (changedSinceSave!=null && !(ic<0 || ic>=changedSinceSave.length || it<0 || changedSinceSave[ic]==null || it>=changedSinceSave[ic].length))
				changedSinceSave[ic][it] = true;
			notifyOfChangeLowLevel(MesquiteListener.DATA_CHANGED, ic, it, 0);  
			checksumValid = false;
			//if (watchForChange)
			//MesquiteMessage.warnProgrammer("SHC " + ic + " " + it);
			watchForChange = false;
			if (saveChangeHistory){ //|| MesquiteTrunk.currentAuthor == null
				ChangeHistory history = (ChangeHistory)getCellObject(historyRef, ic, it);
				ChangeEvent ce = null;
				StringBuffer sb = new StringBuffer();
				statesIntoStringBuffer(ic, it, sb, false);
				long baseTime = MesquiteTrunk.startupTime;
				if (getProject() != null)
					baseTime  = getProject().startupTime;
				if (history != null) {
					if (!history.timeExists(baseTime)) {
						ce = history.addEvent(MesquiteModule.author, baseTime, sb.toString()); //MesquiteTrunk.currentAuthor
					}
					else {
						ce = history.updateEvent(MesquiteModule.author, baseTime, sb.toString()); //MesquiteTrunk.currentAuthor
					}
				}
				else {
					ChangeHistory cH = new ChangeHistory();
					ce = cH.addEvent(MesquiteModule.author, baseTime, sb.toString());
					setCellObject(historyRef, ic, it, cH);
				}
				stampLastModifiedAuthor();
				if (ce !=null && requireChangeAuthority){ 
					if (currentChangeAuthority == null){//no ChangeAuthority on record
						String s = MesquiteString.queryString(MesquiteFrame.activeWindow.getMesquiteWindow(), "Authority", "On what authority do you change these data?", "I said so");
						ChangeAuthority ca = new ChangeAuthority();
						if (s == null)
							s = "I said so";
						ca.set(s);
						currentChangeAuthority = ca; //this is to be reset to null before an independent change is made.
						//NOTE:  it is assumed this change will be fully done when notifyListeners is called
						//(Such a convention is a bit delicate, but seems best at moment)
					}
					ce.setAuthority(currentChangeAuthority);
				}
			}
		}
	}

	public void stampHistoryChange() {
		if (changedSinceSave!=null)
			for (int ic = 0; ic<getNumChars(); ic++)
				for (int it = 0; it<numTaxa; it++)
					changedSinceSave[ic][it] = true;
	}

	/*.................................................................................................................*/
	/** checks to see if the Notification that what's changed is a single cell */
	public boolean singleCellChange(Notification notification) {
		if (notification==null)
			return false;
		int code = notification.getCode();
		if (code==MesquiteListener.DATA_CHANGED && notification.subcodesContains(MesquiteListener.SINGLE_CELL))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	/** checks to see if the Notification that what's changed is a simple shifting of the entire block of cells from first applicable to last */
	public boolean onlyAllCellsShifted(Notification notification) {
		if (notification==null)
			return false;
		int code = notification.getCode();
		if (code==MesquiteListener.DATA_CHANGED && notification.subcodesContains(MesquiteListener.ALL_CELLS_ONLY_SHIFTED))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	/** checks to see if the Notification that what's changed is state substitution for a single cell */
	public boolean singleCellSubstitution(Notification notification) {
		if (notification==null)
			return false;
		int code = notification.getCode();
		if (code==MesquiteListener.DATA_CHANGED && notification.subcodesContains(MesquiteListener.SINGLE_CELL) && notification.subcodesContains(MesquiteListener.CELL_SUBSTITUTION))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	/** checks to see if the Notification that what's changed is changes in cell content for a single taxon.  Does not include addition subtraction of characters*/
	public boolean singleTaxonCellChanges(Notification notification) {
		if (notification==null)
			return false;
		int code = notification.getCode();
		if (code==MesquiteListener.DATA_CHANGED && (notification.subcodesContains(MesquiteListener.SINGLE_CELL)||notification.subcodesContains(MesquiteListener.SINGLE_TAXON)))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	/** checks to see if the Notification that what's changed is changes in a single taxon.  Does not include addition subtraction of characters*/
	public boolean singleTaxonChange(Notification notification) {
		if (notification==null)
			return false;
		int code = notification.getCode();
		if (code==MesquiteListener.DATA_CHANGED && (notification.subcodesContains(MesquiteListener.SINGLE_TAXON)))
			return true;
		return false;
	}
	/*.................................................................................................................*/
	public void doAfterNotify(Notification notification){
		currentChangeAuthority = null;
	}
	/*.................................................................................................................*/
	/** compares checksum arrays, and if requested gives warnings.  Returns whether or not the arrays check as the same **/
	public boolean compareChecksums(long[] before, long[] after, boolean givewarnings, String operationName){
		if (before == null || after == null || before.length != after.length){
			if (givewarnings) {
				MesquiteMessage.printStackTrace("Stack trace");
				MesquiteTrunk.mesquiteTrunk.reportableAlert( "WARNING: checksum failed during " + operationName + " because checksum arrays not matching sizes or null.", "");
			}
			return false;
		}
		String problemFound = "";
		int mostSeriousProblemFound = NUMCSC;
		for (int i=0; i<before.length; i++){
			if (before[i] != after[i]) {
				if (givewarnings){
					problemFound = Integer.toString(i) + " ";
					if (i<mostSeriousProblemFound)
						mostSeriousProblemFound = i;
				}
			}
		}
		if (problemFound.length()>0){
			MesquiteMessage.printStackTrace("Stack trace");
			MesquiteTrunk.mesquiteTrunk.reportableAlert( "WARNING: " + operationName + " appears to have damaged the integrity of the data matrix or its associated information.  Make sure you have a backup copy of your data file!!!!", "[Problems found: " + problemFound + "]");
			return false;
		}
		return true;
	}

	static final int CS_Overall = 0;
	static final int CS_CellStates = 1;
	static final int CS_CORE = 1;
	static final int CS_SpecsSets = 2;
	static final int CS_CAssocLong = 3;
	static final int CS_CAssocBits = 4;
	static final int CS_CAssocDoubles = 5;
	static final int CS_CAssocObjects = 6;
	static final int CS_CellObjects = 7;
	static final int CS_UNCLEAR = 7;
	static final int CS_MName = 8;
	static final int CS_MAnnot = 9;
	static final int CS_CNames = 10;
	static final int CS_CAnnots = 11;
	static final int CS_CSelected = 12;
	static final int CS_CellFootnotes = 13;
	static final int CS_CellObjectsDisp = 14;
	static final int CS_ChangedSinceSave = 15;
	static final int CS_CharIllustr = 16;
	static final int CS_COSMETIC = 16;
	static final  int NUMCSC = 17;
	/*.................................................................................................................*/
	/** calculates a full checksum on all aspects of the matrix, including char names and specsets, done in order of character ids.
 	Returned as array so that in future can give checksums on various components independently.  
 	Should call compareChecksums to compare arrays and issue warnings */
	public long[] getIDOrderedFullChecksum(){
		//finding character ordering according to id's
		long[] tempIDs = new long [charIDs.length];
		int[] tempNums = new int[charIDs.length];
		for (int i=0; i<charIDs.length; i++){
			tempIDs[i] = charIDs[i];
			tempNums[i] = i;
		}
		for (int i=1; i<tempIDs.length; i++) {  //sorting the id's
			for (int j= i-1; j>=0 && tempIDs[j]>tempIDs[j+1]; j--) {
				long temp = tempIDs[j];
				tempIDs[j] = tempIDs[j+1];
				tempIDs[j+1]=temp;
				int tempI = tempNums[j];
				tempNums[j] = tempNums[j+1];
				tempNums[j+1]=tempI;
			}
		}
		/**/
		CRC32[] components = new CRC32[NUMCSC];
		for (int i = 0; i<NUMCSC; i++){
			components[i] = new CRC32();
			components[i].reset();
		}
		//CRC32 checksum = components[CS_Overall];
		StringBuffer sb = new StringBuffer(20);
		//checksum.reset();
		//updateChecksum(checksum, getName());   //MName  Cosmetic
		updateChecksum(components[CS_MName], getName());   //MName  Cosmetic
		//updateChecksum(checksum, getAnnotation()); //MAnnot  Cosmetic
		updateChecksum(components[CS_MAnnot], getAnnotation()); //MAnnot  Cosmetic
		for (int i = 0; i< tempNums.length; i++){
			int ic = tempNums[i];

			//calculate portion of sum for character ic
			if (characterHasName(ic)){
				//updateChecksum(checksum, getCharacterName(ic)); //CNames  Cosmetic
				updateChecksum(components[CS_CNames], getCharacterName(ic)); //CNames  Cosmetic
			}
			//updateChecksum(checksum, getAnnotation(ic)); //CAnnots  Cosmetic
			updateChecksum(components[CS_CAnnots], getAnnotation(ic)); //CAnnots  Cosmetic
			//updateChecksum(checksum, getSelected(ic)); //CSelected  Cosmetic
			updateChecksum(components[CS_CSelected], getSelected(ic)); //CSelected  Cosmetic

			int num = getNumberAssociatedLongs();
			for (int iA = 0; iA<num; iA++){
				LongArray array = getAssociatedLongs(iA);
				//	updateChecksum(checksum, array.getValue(ic)); //CAssocLong
				updateChecksum(components[CS_CAssocLong], array.getValue(ic)); //CAssocLong
			}
			num = getNumberAssociatedBits();
			for (int iA = 0; iA<num; iA++){
				Bits array = getAssociatedBits(iA);
				//	updateChecksum(checksum, array.isBitOn(ic)); //CAssocBits
				updateChecksum(components[CS_CAssocBits], array.isBitOn(ic)); //CAssocBits
			}
			num = getNumberAssociatedDoubles();
			for (int iA = 0; iA<num; iA++){
				DoubleArray array = getAssociatedDoubles(iA); //CAssocDoubles
				//	updateChecksum(checksum, array.getValue(ic));
				updateChecksum(components[CS_CAssocDoubles], array.getValue(ic));
			}
			num = getNumberAssociatedObjects();
			for (int iA = 0; iA<num; iA++){
				ObjectArray array = getAssociatedObjects(iA);
				//	updateChecksum(checksum, array.getValue(ic)); //CAssocObjects
				updateChecksum(components[CS_CAssocObjects], array.getValue(ic)); //CAssocObjects
			}

			Vector specsVectors = getSpecSetsVectorVector();
			if (specsVectors!=null){ //update size of specification sets
				for (int iv=0; iv<specsVectors.size(); iv++) {
					SpecsSetVector sv = (SpecsSetVector)specsVectors.elementAt(iv);
					for (int j=0; j<sv.size(); j++) {
						SpecsSet css = (SpecsSet)sv.elementAt(j);
						//	updateChecksum(checksum, css.toString(ic)); //SpecsSets
						updateChecksum(components[CS_SpecsSets], css.toString(ic)); //SpecsSets
					}
					SpecsSet currentSS = sv.getCurrentSpecsSet();
					if (currentSS!=null) {
						//	updateChecksum(checksum, currentSS.toString(ic));//SpecsSets
						updateChecksum(components[CS_SpecsSets], currentSS.toString(ic));//SpecsSets
					}
				}
			}

			if (footnotes!=null){
				for (int j = 0; j<numTaxa; j++){
					//	updateChecksum(checksum, footnotes[ic][j]);//CellFootnotes
					updateChecksum(components[CS_CellFootnotes], footnotes[ic][j]);//CellFootnotes
				}
			}
			if (cellObjects.size()>0){
				for (int k =0; k<cellObjects.size(); k++){
					Object2DArray objArray = (Object2DArray)cellObjects.elementAt(k);
					Object[][] objects = objArray.getMatrix();
					for (int j = 0; j<numTaxa; j++){
						//		updateChecksum(checksum, objects[ic][j]);//CellObjects
						updateChecksum(components[CS_CellObjects], objects[ic][j]);//CellObjects
					}
				}
			}
			if (cellObjectsDisplay!=null){
				for (int j = 0; j<numTaxa; j++){
					//	updateChecksum(checksum, cellObjectsDisplay[ic][j]);//CellObjectsDisp  COSMETIC
					updateChecksum(components[CS_CellObjectsDisp], cellObjectsDisplay[ic][j]);//CellObjectsDisp  COSMETIC
				}
			}

			if (changedSinceSave!=null){
				for (int j = 0; j<numTaxa; j++){
					//	updateChecksum(checksum, changedSinceSave[ic][j]);//ChangedSinceSave  COSMETIC
					updateChecksum(components[CS_ChangedSinceSave], changedSinceSave[ic][j]);//ChangedSinceSave  COSMETIC
				}
			}
			if (characterIllustrations!=null){
				//	updateChecksum(checksum, characterIllustrations[ic]); //CharIllustr COSMETIC
				updateChecksum(components[CS_CharIllustr], characterIllustrations[ic]); //CharIllustr COSMETIC
			}


			for (int it=0; it<numTaxa; it++)  {
				sb.setLength(0);
				statesIntoStringBuffer(ic, it, sb, true);
				//	updateChecksum(checksum, sb);  //STATES
				updateChecksum(components[CS_CellStates], sb);  //STATES
			}

		}
		long[] result = new long[NUMCSC];
		for (int i=0; i<NUMCSC; i++)
			result[i] = components[i].getValue();
		return result;
	}

	static final byte[] nullbytes = "null".getBytes();
	private void updateChecksum(CRC32  checksum, String i){
		if (i == null)
			checksum.update(nullbytes);
		else
			checksum.update(i.getBytes());
	}
	private void updateChecksum(CRC32  checksum, long i){
		updateChecksum(checksum, Long.toString(i));
	}
	private void updateChecksum(CRC32  checksum, Object i){
		if (i == null)
			checksum.update(nullbytes);
		else
			updateChecksum(checksum, i.toString());
	}
	private void updateChecksum(CRC32  checksum, double i){
		updateChecksum(checksum, Double.toString(i));
	}
	private void updateChecksum(CRC32  checksum, boolean i){
		if (i)
			updateChecksum(checksum, "true");
		else
			updateChecksum(checksum, "false");
	}
	/*.................................................................................................................*/
	public static int getCurrentChecksumVersion(){
		return 3;
	}
	/*.................................................................................................................*/
	public long getChecksumForFileRecord(int version){
		//if (checksumValid)  deleted dec 09 to ensure checksum up to date for file saving
		//	return rememberedChecksum;

		rememberedChecksum = calculateChecksum(crc32, version);
		checksumValid = true;
		return rememberedChecksum;
	}
	/*.................................................................................................................*/
	public String getChecksumSummaryString(){
		return "numChars = " + getNumChars();
	}
	/*.................................................................................................................*/
	public long getChecksum(){
		if (checksumValid)
			return rememberedChecksum;

		rememberedChecksum = calculateChecksum(crc32);
		checksumValid = true;
		return rememberedChecksum;
	}
	/*.................................................................................................................*/
	public long calculateChecksum(CRC32 crc32, int version){
		return calculateChecksum(crc32);
	}
	/*.................................................................................................................*/
	public abstract long calculateChecksum(CRC32 crc32);

	/*.................................................................................................................*/
	public boolean isUserVisible() {
		return userVisible;
	}
	public void setUserVisible(boolean userVisible) {
		this.userVisible = userVisible;
	}

	private int inhibitEdit = 0;
	/*.................................................................................................................*/
	public void incrementEditInhibition(){
		boolean sendNotification = inhibitEdit==0;
		inhibitEdit++;
		if (sendNotification)
			notifyListeners(this, new Notification(MesquiteListener.LOCK_CHANGED, null,null));
	}
	/*.................................................................................................................*/
	public void decrementEditInhibition(){
		boolean sendNotification = inhibitEdit>0;
		inhibitEdit--;
		if (inhibitEdit < 0)
			inhibitEdit = 0;
		if (sendNotification && inhibitEdit==0)
			notifyListeners(this, new Notification(MesquiteListener.LOCK_CHANGED, null,null));
	}
	public boolean isEditInhibited(){
		return inhibitEdit>0;
	}
	public int inhibitionLevels(){
		return inhibitEdit;
	}

	/*.................................................................................................................*
	public boolean getEditorInhibition(){
		return inhibitEditor;
	}
	/*.................................................................................................................*/
	public void setEditorInhibition(boolean i){
		if (!isEditInhibited() && i) // wasn't inhibited, so need to make it so.
			incrementEditInhibition();
		else if (isEditInhibited() && !i){  // was inhibited, so need to turn it off
			inhibitEdit=0;
			notifyListeners(this, new Notification(MesquiteListener.LOCK_CHANGED, null,null));
		}
	}
	/*.................................................................................................................*/
	protected void setDirty(boolean d, int ic, int it){
		setDirty(d); 
		stampHistoryChange(ic, it);
		calculateFirstLastApplicable(it);
	}
	/*.................................................................................................................*/
	public boolean someApplicableInTaxon(int it, boolean countMissing){
		return  getNumberApplicableInTaxon(it,countMissing)>0;
	}
	/*.................................................................................................................*/
	public int numTaxaWithSomeApplicable(boolean countMissing){
		int count = 0;
		for (int it = 0; it<numTaxa; it++) {
			if (someApplicableInTaxon(it,countMissing))
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public int numSelectedTaxaWithSomeApplicable(boolean countMissing){
		int count = 0;
		for (int it = 0; it<numTaxa; it++) {
			if (taxa.getSelected(it) && someApplicableInTaxon(it,countMissing))
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public int numSelectedTaxa(){
		int count = 0;
		for (int it = 0; it<numTaxa; it++) {
			if (taxa.getSelected(it))
				count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public int getNumberApplicableInTaxon(int it, boolean countMissing){
		int count = 0;
		for (int i = 0; i<numChars; i++) {
			if (!isInapplicable(i,it))
				if (!isUnassigned(i, it) || countMissing)
					count++;
		}
		return count;
	}
	/*.................................................................................................................*/
	public int nextApplicable(int it, int ic, boolean countMissing){
		for (int i = ic; i<numChars; i++) {
			if (!isInapplicable(i,it))
				if (!isUnassigned(i, it) || countMissing)
					return i;
		}
		return -1;
	}
	/*.................................................................................................................*/
	public int prevApplicable(int it, int ic, boolean countMissing){
		for (int i = ic; i>=0; i--) {
			if (!isInapplicable(i,it))
				if (!isUnassigned(i, it) || countMissing)
					return i;
		}
		return -1;
	}

	/*.................................................................................................................*/
	/** gets the get titles for tabbed summary data about matrix*/
	public String getTabbedTitles() {
		return "Name\tNumber of Taxa\tNumber of Characters\tA\tC\tG\tT";
	}
	/*.................................................................................................................*/
	/** gets the get  tabbed summary data about matrix*/
	public String getTabbedSummary() {
		return getName()+ "\t"+getNumTaxa() + "\t" + getNumChars();
	}
	/*.................................................................................................................*/
	/** gets the explanation of this matrix*/
	public String getExplanation() {
		if (taxa == null)
			return null;
		String lastModAuthor = getLastModifiedAuthor();
		if (lastModAuthor == null)
			lastModAuthor = "";
		else
			lastModAuthor = " (Last modified by " + lastModAuthor + ")";
		String extra = "This character matrix" + lastModAuthor + " for the taxa block \"" + taxa.getName() + "\" has " + getNumChars() + " characters for the " + getNumTaxa() + " taxa. Category of data: " + getDataTypeName() + "\n";
		return extra;

	}
	/*.................................................................................................................*/
	/** sets the illustration tied to character ic to the given Image at the given path (URL path or file path)*/
	public void setIllustration(int ic, Image illustration, String path) {
		if (ic<0 || ic>=numChars) {
			MesquiteMessage.println("Error: character number out of bounds (setIllustration) " + ic);
		}
		else {
			if (characterIllustrations==null) {
				characterIllustrations = new Image[numChars];
				characterIllustrationPath = new String[numChars];
			}
			characterIllustrations[ic] = illustration;
			characterIllustrationPath[ic] = path;
		}
	}
	/*.................................................................................................................*/
	/** returns the path of the illustration tied to character ic*/
	public String getIllustrationPath(int ic) {
		if (ic<0 || ic>=numChars) {
			MesquiteMessage.println("Error: character number out of bounds (getIllustrationPath) " + ic);
			return null;
		}
		else if (characterIllustrationPath != null)
			return characterIllustrationPath[ic] ;
		else
			return null;
	}
	/*.................................................................................................................*/
	/** returns the illustration tied to character ic*/
	public Image getIllustration(int ic) {
		if (ic<0 || ic>=numChars) {
			MesquiteMessage.println("Error: character number out of bounds (getIllustration) " + ic);
			return null;
		}
		else if (characterIllustrations != null)
			return characterIllustrations[ic] ;
		else
			return null;
	}
	/*.................................................................................................................*/
	/** Returns a string describing the contents of the cells (by default, null, but may be overridden to convey
 	special information, such as what are the items in a continuous data matrix)*/
	public String getCellContentsDescription(){
		return null;
	}
	/*.................................................................................................................*/
	/** outputs to log file the matrix*/
	public abstract void logMatrix();
	/*.................................................................................................................*/
	/*(LOCKING SYSTEM NOT READY)
 	 // locks data
	public void lockData() {
		locked = true;
	}
 	//unlocks data
	public void unlockData() {
		locked = false;
	}
 	/*.................................................................................................................*/
	private boolean addDataLinkage(CharacterData other){
		if (other==null)
			return false;
		if (other.getTaxa()!=getTaxa())
			return false;
		if (other.getNumChars() != numChars)
			return false;
		if (other == this)
			return false;
		if (linkedDatas.indexOf(other)>=0)
			return false;
		linkedDatas.addElement(other);
		return true;
	}
	private void removeDataLinkage(CharacterData other){
		if (other==null)
			return;
		if (linkedDatas == null)
			return;
		linkedDatas.removeElement(other);
	}
	/** Adds the passed Character data into this's linkage group */
	public void addToLinkageGroup(CharacterData other){
		if (other==null)
			return;
		if (linkedDatas == null)
			return;
		other.getDataLinkages().removeAllElements();
		//mutually link other to all of linked ones
		for (int i=0; i<linkedDatas.size(); i++){
			CharacterData d= (CharacterData)linkedDatas.elementAt(i);
			other.addDataLinkage(d);
			d.addDataLinkage(other);
		}
		//mutually link other to this
		addDataLinkage(other);
		other.addDataLinkage(this);
	}
	/** Removes this Character data from its linkage group */
	public void resignFromLinkageGroup(){
		if (linkedDatas == null)
			return;
		//cut out of all linked ones
		for (int i=0; i<linkedDatas.size(); i++){
			CharacterData d= (CharacterData)linkedDatas.elementAt(i);
			d.removeDataLinkage(this);
		}
		linkedDatas.removeAllElements();
	}
	public Vector getDataLinkages(){
		return linkedDatas;
	}
	public boolean isLinked(CharacterData other){
		if (linkedDatas == null)
			return false;
		return (other!=null && linkedDatas.indexOf(other)>=0);
	}
	public boolean isLinked(){
		if (linkedDatas == null)
			return false;
		return (linkedDatas.size()>0);
	}
	/*------------------*/
	public boolean concatenate(CharacterData oData, boolean addTaxaIfNew, boolean explainIfProblem, boolean notify){
		return concatenate(oData, addTaxaIfNew, true, false, false, explainIfProblem,notify);
	}

	/** Concatenates the CharacterData oData to this object. */
	public boolean concatenate(CharacterData oData, boolean addTaxaIfNew, boolean concatExcludedCharacters, boolean adjustGroupLabels, boolean prefixGroupNamesIfAlreadyAssigned, boolean explainIfProblem, boolean notify){
		if (oData==null)
			return false;
		if (oData.isLinked(this) || isLinked(oData)) {
			if (explainIfProblem)
				discreetAlert( "Sorry, those two matrices cannot be concatenated because they are linked");
			return false;
		}
		if (!((getClass().isAssignableFrom(oData.getClass())) || oData.getClass() == getClass())){
			if (explainIfProblem)
				discreetAlert( "Sorry, those two matrices cannot be concatenated because they are of different types");
			return false;
		}
		CommandRecord.tick("Concatenating matrices");
		MesquiteModule module = MesquiteTrunk.mesquiteTrunk;
		if (getManager() != null)
			module = ((MesquiteModule)getManager());
		if (!oData.getTaxa().equals(getTaxa(), true, true)){
			Taxa oTaxa = oData.getTaxa();
			Taxa taxa = getTaxa();
			boolean extra = false;
			for (int oit = 0; oit<oTaxa.getNumTaxa() && !extra; oit++)
				if (taxa.findEquivalentTaxon(oTaxa, oit)<0)
					extra = true;
			//different taxa block, with different names.  Offer to add names
			if (extra && addTaxaIfNew){
				if (AlertDialog.query(module.containerOfModule(), "Import taxa from other matrix?", "The matrix you are concatenating to this one is based on a different block of taxa, and includes taxa not in this matrix.  Do you want to add these taxa to this matrix before concatenating?")){
					String names = "";

					for (int oit = 0; oit<oTaxa.getNumTaxa(); oit++){
						if (taxa.findEquivalentTaxon(oTaxa, oit)<0){
							taxa.addTaxa(taxa.getNumTaxa(), 1, false);
							taxa.equalizeTaxon(oTaxa, oit, taxa.getNumTaxa()-1);
							names += taxa.getTaxonName(taxa.getNumTaxa()-1) + "\n";
							CommandRecord.tick("Added taxon " + taxa.getTaxonName(taxa.getNumTaxa()-1));

						}
					}
					if (!StringUtil.blank(names)){
						logln("Added to taxa block were:\n" + names);
						taxa.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null,null));
					}
				}
			}

		}
		int origNumChars = getNumChars();
		if (concatExcludedCharacters)
			addParts(origNumChars+1, oData.getNumChars());
		else 
			addParts(origNumChars+1, oData.numCharsCurrentlyIncluded());
		CharacterPartition partition = (CharacterPartition) getCurrentSpecsSet(CharacterPartition.class);   // partition of this object
		CharactersGroupVector groups = (CharactersGroupVector)getProject().getFileElement(CharactersGroupVector.class, 0);
		CharactersGroup group = null;  //see if one with prefix already exists
		if (partition==null && origNumChars-1>=0){ // let's give the original ones a group, as they didn't have any before
			group = groups.findGroup(getName());  //let's see if there already exists a group with this matrix name
			if (group==null)
				setToNewGroup(getName(), 0, origNumChars-1, module);  //set group
			else
				setCurrentGroup(group,0, origNumChars-1, module);  
		}
		CharacterPartition oPartition = (CharacterPartition) oData.getCurrentSpecsSet(CharacterPartition.class);   // partition in incoming. This by default will be used.
		if (oPartition == null){
			group = groups.findGroup(oData.getName());  //let's see if there already exists a group with this matrix name
			if (group==null)
				setToNewGroup(oData.getName(), origNumChars, getNumChars()-1, module);  //set group
			else
				setCurrentGroup(group,origNumChars, getNumChars()-1, module);   //set group
		}

		addInLinked(getNumChars()+1, oData.getNumChars(), true);

		CharacterState cs = null;
		int count=0;
		for (int ic = 0; ic<oData.getNumChars(); ic++){
			if (concatExcludedCharacters || oData.isCurrentlyIncluded(ic)) {
				CommandRecord.tick("Copying character " + (ic+1) + " in concatenation");
				equalizeCharacter(oData, ic, count+origNumChars);
				count++;
			}
		}

		if (oPartition != null && adjustGroupLabels)
			adjustGroupLabels(oData.getName(), origNumChars, getNumChars()-1, true, prefixGroupNamesIfAlreadyAssigned, module);  //there exists a partition in the incoming, so just redo the names for the groups there.

		if (notify)
			notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, new int[] {origNumChars, oData.getNumChars()}));
		return true;
	}
	/*------------------*/
	public void setBasisTree(Tree tree){
		if (tree == null) {
			basisTree = null;
		}
		else {
			basisTree = tree.cloneTree();
		}
	}
	public Tree getBasisTree(){
		return basisTree;
	}
	/* ---------------- for HNode interface ----------------------*/
	public Image getHImage(){
		return null;
	}
	/* ---------------- for HNode interface ----------------------*/
	public Color getHColor(){

		return ColorTheme.getInterfaceBackground();  //project color
	}
	/* ---------------- for HNode interface ----------------------*/
	public boolean getHShow(){
		return true; 
	}
	/* ---------------- for use with touched from HNode interface ----------------------*/
	public void addToBrowserPopup(MesquitePopup popup){
		super.addToBrowserPopup(popup);
		ElementManager manager = getManager();
		if (manager!=null && manager instanceof MesquiteModule) {
			MesquiteFile file = getFile();
			if (file!=null && file.getProject()!=null) {
				popup.add(new MenuItem("-"));
				popup.add(new MesquiteMenuItem("Show List of Characters of \"" + getName() + "\"", MesquiteTrunk.mesquiteTrunk, MesquiteTrunk.mesquiteTrunk.makeCommand("showCharacters", (MesquiteModule)manager), getFile().getProject().getCharMatrixReferenceInternal(this)));
				popup.add(new MesquiteMenuItem("Show Matrix Editor for \"" + getName() + "\"", MesquiteTrunk.mesquiteTrunk, MesquiteTrunk.mesquiteTrunk.makeCommand("showDataWindow", (MesquiteModule)manager), getFile().getProject().getCharMatrixReferenceInternal(this)));
			}
		}
	}

	public MesquiteModule showMatrix(){
		if (getManager() != null && getFile() != null) {
			return (MesquiteModule)((Commandable)getManager()).doCommand("showDataWindow", getFile().getProject().getCharMatrixReferenceInternal(this), CommandChecker.defaultChecker);
		}
		return null;
	}
	public void showList(){
		if (getManager() != null && getFile() != null) 
			((Commandable)getManager()).doCommand("showCharacters", getFile().getProject().getCharMatrixReferenceInternal(this), CommandChecker.defaultChecker);
	}

	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = super.getSnapshot(file);
		if (temp == null)
			temp = new Snapshot();
		if (isEditInhibited()){
			temp.addLine("inhibitEditing "+inhibitionLevels());
		}
		if (!isUserVisible())
			temp.addLine("setHidden");
		if (temp.getNumLines() == 0)
			return null;
		return temp;
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Sets editor inhibition",  "[inhibition levels]", commandName, "inhibitEditing")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int levels= MesquiteInteger.fromString(arguments, io);
			if (MesquiteInteger.isCombinable(levels) && levels>0) {
				for (int i = 0; i<levels; i++)
					incrementEditInhibition();
			} else
				incrementEditInhibition();
		}
		else if (checker.compare(this.getClass(), "Sets editor inhibition to false", null, commandName, "uninhibitEditing")) {
			decrementEditInhibition();
		}
		else if (checker.compare(this.getClass(), "Sets user visibility to false", null, commandName, "setHidden")) {
			setUserVisible(false);
		}
		else if (checker.compare(this.getClass(), "Sets user visibility to true", null, commandName, "setVisible")) {
			setUserVisible(true);
		}
		else if (checker.compare(this.getClass(), "Duplicates the matrix", null, commandName, "duplicateMe")) {
			if (getProject() != null)
				getProject().incrementProjectWindowSuppression();

			CharacterData starter = makeCharacterData(getMatrixManager(), getTaxa());  
			if (getMatrixManager() != null)
				starter.addToFile(getProject().getHomeFile(), getProject(),  getMatrixManager().findElementManager(CharacterData.class));  

			boolean success = starter.concatenate(this, false, true, false, false, false, false);
			if (success){
				String name = getName() + " (duplicate)";
				if (getProject()!= null)
					name = getProject().getCharacterMatrices().getUniqueName(name);
				starter.setName(name);
			}
			if (getProject() != null)
				getProject().decrementProjectWindowSuppression();
			return starter;
		}
		else if (checker.compare(this.getClass(), "Exports the matrix", null, commandName, "exportMe")) {
			ElementManager manager = getManager();
			if (manager!=null) {
				((Commandable)manager).doCommand("exportMatrix", getFile().getProject().getCharMatrixReferenceInternal(this), CommandChecker.defaultChecker);
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	public String toString(){
		return getDataTypeName() + "\"" + getName() + "\" : " + getNumTaxa(false) + " taxa " + getNumChars(false) + " characters (id " + getID() +")";
	}
	public String toHTMLStringDescription(){
		//String s = "<li>Character Matrix: <strong>" + getName() + "</strong> (" + getDataTypeName() + ").  Number of characters: " + getNumChars() + ".  (<a href =\"showMatrix:" + getID() + "\">View &amp; Edit</a>)  (<a href =\"listMatrix:" + getID() + "\">List &amp; Manage</a>) (<a href =\"chartCharacters:" + getID() + "\">Chart Characters</a>) (<a href =\"renameMatrix:" + getID() + "\">Rename</a>)  (<a href =\"editCommentMatrix:" + getID() + "\">Edit Comment</a>)  (<a href =\"deleteMatrix:" + getID() + "\">Delete</a>)";
		String s = "<li>Character Matrix: <strong>" + getName() + "</strong> (" + getDataTypeName() + ").  Number of characters: " + getNumChars() + ". ";
		String comment = getAnnotation();
		if (!StringUtil.blank(comment))
			s+= "<br><font color=\"#777777\">" + comment + "</font>";
		if (HTMLDescribable.verbose){
			String st = super.toHTMLStringDescription();
			if (!StringUtil.blank(st))
				return s + "<ul>" + st + "</ul></li>";
		}
		return s + "</li>";
	}

	public String[] toStringsContig(boolean includeInapplicable){
		String[] s = new String[getNumTaxa()];
		StringBuffer sb = new StringBuffer(getNumChars());
		for (int it= 0; it<getNumTaxa(); it++){
			for (int ic = 0; ic<getNumChars(); ic++){
				statesIntoStringBuffer(ic, it,  sb, true, includeInapplicable, true);
			}
			s[it] = sb.toString();
			sb.setLength(0);
		}
		return s;

	}

	public boolean setInclusionExclusion(MesquiteModule module, MesquiteTable table, boolean include){
		boolean changed=false;
		if (table !=null) {

			CharInclusionSet inclusionSet = (CharInclusionSet) getCurrentSpecsSet(CharInclusionSet.class);
			if (inclusionSet == null) {
				inclusionSet= new CharInclusionSet("Inclusion Set", getNumChars(), this);
				inclusionSet.selectAll();
				inclusionSet.addToFile(getFile(), getProject(), module.findElementManager(CharInclusionSet.class)); //THIS
				setCurrentSpecsSet(inclusionSet, CharInclusionSet.class);
			}
			if (inclusionSet != null) {
				for (int i=0; i<getNumChars(); i++) {
					if (table.wholeColumnSelectedAnyWay(i) || table.isRowNameSelected(i)) {
						if (include) //include
							inclusionSet.setSelected(i, true);
						else if (!include) //exclude
							inclusionSet.setSelected(i, false);
						changed = true;
					}
				}
			}


			if (changed)
				notifyListeners(this, new Notification(AssociableWithSpecs.SPECSSET_CHANGED));  //not quite kosher; HOW TO HAVE MODEL SET LISTENERS??? -- modelSource
		}
		return changed;
	}

	public int getTotalNumApplicable(int it, boolean ignoreExcluded) {
		CharInclusionSet incl = null;
		if (ignoreExcluded)
			incl = (CharInclusionSet)getCurrentSpecsSet(CharInclusionSet.class);
		int numChars = getNumChars();
		int seqLen = 0;
		if (numChars != 0) {
			CharacterState cs = null;
			for (int ic=0; ic<numChars; ic++) {
				if (incl == null || incl.isBitOn(ic)){  // adjusted 2. 01 to consider inclusion
					cs = getCharacterState(cs, ic, it);
					if (!cs.isInapplicable())
						seqLen++;
				}
			}
		}	
		return seqLen;

	}

	public String getNameOfCellEntry(int number){
		if (number==1)
			return "entry";
		else
			return "entries";
	}

	/*..........................................CharacterData.....................................*/
	/**merges the states for taxon it2 into it1  within this Data object */
	public  boolean mergeSecondTaxonIntoFirst(int it1, int it2, boolean mergeMultistateAsUncertainty) {
		if ( it1<0 || it1>=getNumTaxa() || it2<0 || it2>=getNumTaxa() )
			return false;

		boolean mergedAssigned = false;
		CharacterState cs1= null;
		CharacterState cs2= null;
		for (int ic=0; ic<getNumChars(); ic++) {
			cs1 = getCharacterState(cs1, ic,it1);
			cs2 = getCharacterState(cs2, ic,it2);
			if (cs1.isCombinable() && cs2.isCombinable()){ //both have states; just leave first state as is
				mergedAssigned = true;
			}
			else if (cs1.isCombinable()){  // taxon 1 has state but not taxon 2; just use first
			}
			else if (cs2.isCombinable()){  // taxon 2 has state but not taxon 1; just use second
				setState( ic, it1, cs2);
			}
			else {
				setToUnassigned( ic, it1);
			}
		}
		return mergedAssigned;
	}
	/*..........................................CharacterData.....................................*/
	/**merges the states for taxon it2 into it1  within this Data object */
	public  boolean mergeSecondTaxonIntoFirst(int it1, int it2) {
		return mergeSecondTaxonIntoFirst(it1,it2,false);
	}
	/*..........................................CharacterData.....................................*/
	/**merges the states for the taxa recorded in taxaToMerge into taxon it  within this Data object.  
	 * Returns a boolean array of which taxa had states merged  (i.e. something other than 
	 * unassigned + assigned or inapplicable + assigned */
	public boolean[] mergeTaxa(int sinkTaxon, boolean[]taxaToMerge, boolean mergeMultistateAsUncertainty) {
		if (!(MesquiteInteger.isCombinable(sinkTaxon)) || sinkTaxon<0 || sinkTaxon>=getNumTaxa() || taxaToMerge==null)
			return null;
		boolean[] mA = new boolean[taxaToMerge.length];
		boolean mergedAssigned = false;
		boolean firstHasData = hasDataForTaxon(sinkTaxon);
		for (int it=0; it<getNumTaxa() && it<taxaToMerge.length; it++) {
			if (it!=sinkTaxon && taxaToMerge[it]){
				boolean mergingHadData = hasDataForTaxon(it);
				boolean ma = mergeSecondTaxonIntoFirst(sinkTaxon, it, mergeMultistateAsUncertainty);
				if (mergingHadData && ! firstHasData){
					//in this case tInfo brought in from merging.  This isn't ideal, as should fuse tInfo if both have data
					Associable a = getTaxaInfo(false);
					if (a != null)
						a.swapParts(sinkTaxon, it);
				}
				mA[it] = ma;   
				mergedAssigned = mergedAssigned | ma;

			}
		}
		if (mergedAssigned)
			return mA;
		else
			return null;
	}

	/*..........................................CharacterData.....................................*/
	/**merges the states for the taxa recorded in taxaToMerge into taxon it  within this Data object.  
	 * Returns a boolean array of which taxa had states merged  (i.e. something other than 
	 * unassigned + assigned or inapplicable + assigned */
	public boolean[] mergeTaxa(int sinkTaxon, boolean[]taxaToMerge) {
		return mergeTaxa(sinkTaxon, taxaToMerge, false);
	}
	
	/*..........................................CharacterData.....................................*/
	/**Gets the CharacterData object for an MCharactersDistribution.  It first checks to see if the CharacterData object
	 * already exists, and if so, returns it; otherwise, it created one.  */
	public static CharacterData getData (MesquiteModule mb, MCharactersDistribution matrix, Taxa taxa) {
		if (matrix.getParentData()==null) {
			CharactersManager manageCharacters = (CharactersManager)mb.findElementManager(CharacterData.class);
			CharMatrixManager manager = manageCharacters.getMatrixManager(matrix.getCharacterDataClass());
			return matrix.makeCharacterData(manager, taxa);
		}
		return matrix.getParentData();
	}

	/*...............................................................................................................*/
	/** Sets the publication code of a particular taxon in this data object. */
	public void setPublicationCode(int it, String s){
		Taxon taxon = getTaxa().getTaxon(it);
		Associable tInfo = getTaxaInfo(true);
		if (tInfo != null && taxon != null) {
			tInfo.setAssociatedObject(CharacterData.publicationCodeNameRef, it, s);
		}
	}
	/*...............................................................................................................*/
	/** Gets the publication code of a particular taxon in this data object. */
	public String getPublicationCode(int it){
		Associable tInfo = getTaxaInfo(true);
		if (tInfo == null)
			return null;
		Object obj = tInfo.getAssociatedObject(CharacterData.publicationCodeNameRef, it);
		if (obj == null || !(obj instanceof String))
			return null;
		return (String)obj;
	}

}


