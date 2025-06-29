/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.lib; 


import mesquite.categ.lib.CategoricalData;
import mesquite.categ.lib.CategoricalState;
import mesquite.categ.lib.DNAData;
import mesquite.categ.lib.MCategoricalDistribution;
import mesquite.categ.lib.MolecularDataUtil;
import mesquite.categ.lib.RequiresAnyMolecularData;
import mesquite.lib.CommandChecker;
import mesquite.lib.CompatibilityTest;
import mesquite.lib.FunctionExplanation;
import mesquite.lib.Integer2DArray;
import mesquite.lib.Long2DArray;
import mesquite.lib.MesquiteBoolean;
import mesquite.lib.MesquiteCommand;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteListener;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteNumber;
import mesquite.lib.MesquiteThread;
import mesquite.lib.Notification;
import mesquite.lib.Parser;
import mesquite.lib.Snapshot;
import mesquite.lib.StringUtil;
import mesquite.lib.UndoInstructions;
import mesquite.lib.UndoReference;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowAssistantI;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.table.TableTool;
import mesquite.lib.ui.MesquiteWindow;
import mesquite.lib.ui.ProgressIndicator;


/* ======================================================================== */
public abstract class AlignShiftToDroppedBase extends DataWindowAssistantI {
	public abstract String getFunctionIconPath();
	
	protected MesquiteTable table;
	protected CategoricalData data;
	protected TableTool alignShiftDropTool;
	protected MesquiteInteger firstColumnTouched = new MesquiteInteger(-2);
	protected int firstRowTouched = -2;
	protected boolean defaultWarnCheckSum  =true;
	protected MesquiteBoolean warnCheckSum = new MesquiteBoolean(defaultWarnCheckSum);
	protected boolean defaultReverseComplementIfNecessary  =false;
	protected MesquiteBoolean reverseComplementIfNecessary = new MesquiteBoolean(defaultReverseComplementIfNecessary);
	protected boolean defaultIgnoreFileSettings  =false;
	protected MesquiteBoolean ignoreFileSettings = new MesquiteBoolean(defaultIgnoreFileSettings);
	protected long originalCheckSum;
	protected MesquiteInteger gapOpen = new MesquiteInteger();
	protected MesquiteInteger gapExtend = new MesquiteInteger();
	protected MesquiteInteger gapOpenTerminal = new MesquiteInteger();
	protected MesquiteInteger gapExtendTerminal = new MesquiteInteger();
	protected int[][] subs =null;
	protected int alphabetLength;	 
	protected PairwiseAligner aligner;
	protected AlignUtil alignUtil = new AlignUtil();
	protected static boolean preferencesProcessed=false;
	protected boolean shiftOnlySelectedPiece = false;

	

	/*.................................................................................................................*/
	public abstract TableTool getTool(MesquiteCommand touchCommand, MesquiteCommand dragCommand, MesquiteCommand dropCommand);
	/*.................................................................................................................*/
	public void addExtraMenus(){
	//	addMenuItem("Gap Costs...", MesquiteModule.makeCommand("gapCosts", this));
	//	addMenuItem("Substitution Costs...", MesquiteModule.makeCommand("subCosts", this));
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			MesquiteCommand touchCommand = MesquiteModule.makeCommand("alignDropTouched",  this);
			touchCommand.setSuppressLogging(true);
			MesquiteCommand dragCommand = MesquiteModule.makeCommand("alignDropDragged",  this);
			dragCommand.setSuppressLogging(true);
			MesquiteCommand dropCommand = MesquiteModule.makeCommand("alignDropDropped",  this);
			dropCommand.setSuppressLogging(true);
			alignShiftDropTool = getTool(touchCommand, dragCommand, dropCommand);
			alignShiftDropTool.setWorksOnRowNames(true);
			//alignDropTool.setWorksAsArrowOnRowColumnNumbers(true);
			alignShiftDropTool.setPopUpOwner(this);
			alignShiftDropTool.setEmphasizeRowsOnMouseDrag(true);
			((MesquiteWindow)containerOfModule()).addTool(alignShiftDropTool);

		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		setUseMenubar(false); //menu available by touching on button
		addExtraMenus();
		addCheckMenuItem(null, "Reverse Complement if Necessary", makeCommand("toggleReverseComplementIfNecessary",  this), reverseComplementIfNecessary);
		addCheckMenuItem(null, "Check Data Integrity", makeCommand("toggleWarnCheckSum",  this), warnCheckSum);
		addMenuSeparator();
		addMenuItem("Save Current Settings as Defaults", MesquiteModule.makeCommand("saveDefaults", this));
		addCheckMenuItem(null, "Ignore File Settings; Use Defaults", MesquiteModule.makeCommand("toggleIgnoreFileSettings", this), ignoreFileSettings);
		AlignUtil.getDefaultGapCostsLowTerminal(gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal); 

		loadPreferences();
		preferencesProcessed = true;

		
		return true;
	}
	/*.................................................................................................................*/
	public abstract FunctionExplanation getFunctionExplanation();
	/*.................................................................................................................*/
	public void getSubfunctions(){
		registerSubfunction(getFunctionExplanation());
		super.getSubfunctions();
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public void addToSnapshot(Snapshot temp) {
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (warnCheckSum.getValue()!=defaultWarnCheckSum)
			temp.addLine("toggleWarnCheckSum " + warnCheckSum.toOffOnString());
		if (reverseComplementIfNecessary.getValue()!=defaultReverseComplementIfNecessary)
			temp.addLine("toggleReverseComplementIfNecessary " + reverseComplementIfNecessary.toOffOnString());
		addToSnapshot(temp);
		return temp;
	}
	/*.................................................................................................................*/
	public void processExtraSingleXMLPreference (String tag, String content) {
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("ignoreFileSettings".equalsIgnoreCase(tag))
			ignoreFileSettings.setValue(MesquiteBoolean.fromTrueFalseString(content));
		 if (!preferencesProcessed || ignoreFileSettings.getValue()) {
			if ("warnCheckSum".equalsIgnoreCase(tag))
				warnCheckSum.setValue(MesquiteBoolean.fromTrueFalseString(content));
			if ("reverseComplementIfNecessary".equalsIgnoreCase(tag))
				reverseComplementIfNecessary.setValue(MesquiteBoolean.fromTrueFalseString(content));
			processExtraSingleXMLPreference(tag,content);
		}
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "ignoreFileSettings",ignoreFileSettings);
		StringUtil.appendXMLTag(buffer, 2, "warnCheckSum", warnCheckSum);  
		StringUtil.appendXMLTag(buffer, 2, "reverseComplementIfNecessary", reverseComplementIfNecessary);  
		
		return buffer.toString();
	}


	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		this.data = (CategoricalData)data;
		alphabetLength = ((CategoricalState)data.makeCharacterState()).getMaxPossibleState()+1;	 
		if (subs==null)
			subs = AlignUtil.getDefaultSubstitutionCosts(alphabetLength); 
		data.addListener(this);
		inhibitionChanged();
		if (ignoreFileSettings.getValue()){
			loadPreferences();
		}
	}
	/* ................................................................................................................. */
	void inhibitionChanged(){
		if (alignShiftDropTool!=null && data!=null)
			alignShiftDropTool.setEnabled(!data.isEditInhibited() && data.canMoveChars());
	}
	/* ................................................................................................................. */
	/** passes which object changed, along with optional integer (e.g. for character) (from MesquiteListener interface) */
	public void changed(Object caller, Object obj, Notification notification) {
		int code = Notification.getCode(notification);
		if (obj instanceof CharacterData && (CharacterData) obj == data) {
			if (code == MesquiteListener.LOCK_CHANGED) {
				inhibitionChanged();
			}
		}
		table.setMessage(data.getCellContentsDescription());
		super.changed(caller, obj, notification);
	}

	/*.................................................................................................................*/
	/** Returns CompatibilityTest so other modules know if this is compatible with some object. */
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}


	/*.................................................................................................................*/
	protected abstract void alignShiftTouchedToDropped(long[][] aligned, long[] newAlignment, int rowToAlign, int recipientRow, MesquiteInteger columnDropped, MesquiteInteger columnDragged, boolean droppedOnData, boolean draggedOnData, MesquiteInteger charsAddedToFront);
	/*.................................................................................................................*/
	protected boolean alwaysAlignEntireSequences() {
		return true;
	}
	/*.................................................................................................................*/
	public void preRevCompSetup(int rowToAlign, int recipientRow, int columnDropped, int columnDragged){
	}


	/*.................................................................................................................*/
	protected MesquiteInteger firstColumnSelected= new MesquiteInteger();
	protected MesquiteInteger lastColumnSelected= new MesquiteInteger();

	/*.................................................................................................................*/
	protected boolean useWindow() {
		return false;
	}
	/*.................................................................................................................*/
	protected long[][] windowAlignment(int rowToAlign, int recipientRow, MesquiteInteger columnDropped, MesquiteInteger columnDragged) {
		return null;
	}
	
	/*.................................................................................................................*/
	protected boolean alignTouchedToDroppedBase(int rowToAlign, int recipientRow, MesquiteInteger columnDropped, MesquiteInteger columnDragged, MesquiteInteger charsAddedToFront){
		MesquiteNumber score = new MesquiteNumber();
		boolean revComplemented=false;
		boolean droppedOnData = !data.isInapplicable(columnDropped.getValue(), recipientRow);
		boolean draggedOnData = !data.isInapplicable(columnDragged.getValue(), rowToAlign);
		preRevCompSetup(rowToAlign,  recipientRow,  columnDropped.getValue(),  columnDragged.getValue());

		if (reverseComplementIfNecessary.getValue() && data instanceof DNAData) {
				revComplemented=MolecularDataUtil.reverseComplementSequencesIfNecessary((DNAData)data, this, data.getTaxa(),rowToAlign, rowToAlign,recipientRow, false, false, false);
		}

		if (aligner==null) {
			aligner = new PairwiseAligner(true,true, subs,gapOpen.getValue(), gapExtend.getValue(), gapOpenTerminal.getValue(), gapExtendTerminal.getValue(), alphabetLength);
			//aligner.setUseLowMem(true);
		}
		if (aligner!=null){
			//aligner.setUseLowMem(data.getNumChars()>aligner.getCharThresholdForLowMemory());
			originalCheckSum = ((CategoricalData)data).storeCheckSum(0, data.getNumChars()-1,rowToAlign, rowToAlign);
			aligner.setAllowNewInternalGaps(true);
			//aligner.setGapCosts(8,3,1, 1);
			long[][] aligned = null;
			if (alwaysAlignEntireSequences())   // Pairwise aligner
				aligned = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), recipientRow, rowToAlign,MesquiteInteger.unassigned,MesquiteInteger.unassigned,true,score);
			else{   // pairwise shifter
				firstColumnSelected.setToUnassigned();
				lastColumnSelected.setToUnassigned();
				if (useWindow()) { // there is a single block selected in the row
					aligned = windowAlignment(rowToAlign, recipientRow,  columnDropped,  columnDragged);
					/*				} else if (table.singleContiguousBlockSelected(rowToAlign, firstColumnSelected, lastColumnSelected)) { // there is a single block selected in the row
					shiftOnlySelectedPiece=true;
					aligned = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), recipientRow, 0, data.getNumChars(), rowToAlign,firstColumnSelected.getValue(),lastColumnSelected.getValue(),true,score);
					 */
				} else
					aligned = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), recipientRow, rowToAlign,MesquiteInteger.unassigned,MesquiteInteger.unassigned,true,score);
			}
			if (!AlignUtil.hasSomeAlignedSites(aligned)){
				logln("Sequence " +(rowToAlign+1) + " relative to sequence " + (recipientRow+1)+": " + getProductName() + " not done, as there was no overlap between the two sequences.");
				return false;
			}
			if (aligned==null) {
				logln("Sequence " +(rowToAlign+1) + " relative to sequence " + (recipientRow+1)+": " + getProductName() + " failed!");
				return false;
			}
			long[] rowToAlignAlignment = Long2DArray.extractRow(aligned,1);   

			alignShiftTouchedToDropped(aligned,rowToAlignAlignment,  rowToAlign,  recipientRow,  columnDropped,  columnDragged, droppedOnData, draggedOnData, charsAddedToFront);

			((CategoricalData)data).examineCheckSum(0, data.getNumChars()-1,rowToAlign, rowToAlign, "Bad checksum; alignment has inappropriately altered data!", warnCheckSum, originalCheckSum);
			return true;
		}

		return false;
	}
	protected boolean ignoreCommand(){
		return (ignoreFileSettings.getValue() && MesquiteThread.isScripting());
	}
	/*.................................................................................................................*/
	public String getProductName() {
		return "Alignment";
	}
	/*.................................................................................................................*/
	public String getActionName() {
		return "Align";
	}
	/*.................................................................................................................*/
	protected boolean alignJustTouchedRow = true;

	protected boolean optionDown=false;
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "AlignToDropped tool touched on row.", "[column touched] [row touched]", commandName, "alignDropTouched")) {
			if (table!=null && data !=null){
				alignJustTouchedRow = true;
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				if (arguments.indexOf("option")>=0)
					optionDown = true;
				MesquiteInteger io = new MesquiteInteger(0);
				firstColumnTouched= new MesquiteInteger(MesquiteInteger.fromString(arguments, io));
				firstRowTouched= MesquiteInteger.fromString(arguments, io);
				shiftOnlySelectedPiece = false;

				if (!table.rowLegal(firstRowTouched))
					return null;
				if (table.isRowSelected(firstRowTouched)) {
					alignJustTouchedRow = false;
				}
				else{  // it's not select, so deselect everyone else
					table.offAllEdits();
					if (alwaysAlignEntireSequences()){
						table.deselectAndRedrawAllSelectedRows();
						table.selectRow(firstRowTouched);
						table.redrawFullRow(firstRowTouched);
					}
				}
				//shimmerVerticalOn();
				// table.shimmerHorizontalOn(_);
			}
		}
		else if (checker.compare(this.getClass(), "AlignToDropped tool dragged.", "[column dragged] [row dragged]", commandName, "alignDropDragged")) {
			if (table!=null && data !=null && (firstRowTouched>=0)){
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				MesquiteInteger io = new MesquiteInteger(0);
				int columnDragged = MesquiteInteger.fromString(arguments, io);
				int rowDragged= MesquiteInteger.fromString(arguments, io);
				if (!table.rowLegal(rowDragged)) {
					return null;
				}
			}
		}
		else if (checker.compare(this.getClass(), "AlignToDropped tool dropped.", "[column dropped] [row dropped]", commandName, "alignDropDropped")) {

			if (table!=null && data !=null && (firstRowTouched>=0)){
				//	table.deEmphasizeRow(previousRowDragged);
				if (data.isEditInhibited()){
					discreetAlert("This matrix is marked as locked against editing. To unlock, uncheck the menu item Matrix>Current Matrix>Editing Not Permitted");
					return null;
				}
				MesquiteInteger io = new MesquiteInteger(0);
				MesquiteInteger columnDropped = new MesquiteInteger(MesquiteInteger.fromString(arguments, io));
				int rowDropped= MesquiteInteger.fromString(arguments, io);
				MesquiteInteger charsAddedToFront= new MesquiteInteger(0);
				boolean changed=false;

				if (!table.rowLegal(rowDropped))
					return null;

				if (arguments.indexOf("option")>=0)
					optionDown = true;
				

				if  (!alignJustTouchedRow){  // we are going to align all selected rows 
					if (!table.isRowSelected(rowDropped)){     // we didn't drop it on a selected row
						ProgressIndicator progIndicator = new ProgressIndicator(getProject(),getName(), "Aligning sequences", table.numRowsSelected(), true);
						if (progIndicator!=null){
							progIndicator.setButtonMode(ProgressIndicator.OFFER_CONTINUE);
							progIndicator.setOfferContinueMessageString("Are you sure you want to stop the alignment?");
							progIndicator.start();
						}
						UndoReference undoReference = new UndoReference(data,this, new int[] {UndoInstructions.CHAR_ADDED});
						int count = 0;
						int oldNumChars = data.getNumChars();
						for (int it = 0; it<table.getNumRows(); it++) 
							if (table.isRowSelected(it) && (it!=rowDropped)) {
								if (alignTouchedToDroppedBase(it,rowDropped, columnDropped, firstColumnTouched, charsAddedToFront)) {
									changed = true;
								}
								if (progIndicator != null) {
									if (progIndicator.isAborted()) {
										progIndicator.goAway();
										return null;
									}
									count++;
									progIndicator.setText("Aligning " + data.getTaxa().getTaxonName(it) + " to " + data.getTaxa().getTaxonName(rowDropped));
									progIndicator.setCurrentValue(count);
								}
							}
						if (changed){
							UndoReference uR = undoReference;
							if (data.getDataLinkages() != null && data.getDataLinkages().size()==0)
								uR = null;
							data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, null, uR));
							data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED, null, null));
							if (oldNumChars!=data.getNumChars()){
								data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null, null));
								data.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED, null, null));
							}
								
						}
						if (progIndicator != null)
							progIndicator.goAway();

					}
				}			 
				else if (firstRowTouched!=rowDropped) {
					UndoReference undoReference = new UndoReference();
					UndoInstructions undoInstructions = data.getUndoInstructionsAllMatrixCells(new int[] {UndoInstructions.CHAR_ADDED});
					int oldNumChars = data.getNumChars();
					if (alignTouchedToDroppedBase(firstRowTouched,rowDropped, columnDropped, firstColumnTouched, charsAddedToFront))
						changed = true;
					UndoReference uR=null;
					if (undoInstructions!=null) {
						undoInstructions.setNewData(data);
						if (undoReference!=null && changed){
							undoReference.setUndoer(undoInstructions);
							undoReference.setResponsibleModule(this);
							uR = undoReference;
							if (data.getDataLinkages() != null && data.getDataLinkages().size()==0)
								uR = null;
						}
					} 
					if (changed) {
						data.notifyListeners(this, new Notification(MesquiteListener.DATA_CHANGED, null, uR));
						data.notifyInLinked(new Notification(MesquiteListener.DATA_CHANGED, null, null));
						if (oldNumChars!=data.getNumChars()){
							data.notifyListeners(this, new Notification(MesquiteListener.PARTS_ADDED, null, null));
							data.notifyInLinked(new Notification(MesquiteListener.PARTS_ADDED, null, null));
						}
					}
				}
				if (changed && charsAddedToFront.getValue()>0 && table!=null) {  // we've added characters to front, let's scroll over to maintain view
					table.shiftHorizScroll(charsAddedToFront.getValue());
				}

				alignJustTouchedRow = true;
			}
		}

		else  if (checker.compare(this.getClass(), "Toggles whether the data integrity is checked or not after each use.", "[on; off]", commandName, "toggleWarnCheckSum")) {
			if (ignoreCommand()) return null;
			boolean current = warnCheckSum.getValue();
			warnCheckSum.toggleValue(parser.getFirstToken(arguments));
		}
		else  if (checker.compare(this.getClass(), "Toggles whether the new gaps can be introduced into one or the other sequence.", "[on; off]", commandName, "toggleAllowNewGaps")) {
		}
		else  if (checker.compare(this.getClass(), "Toggles whether each sequence to be aligned should check to see if it needs to be reverse complemented before aligning.", "[on; off]", commandName, "toggleReverseComplementIfNecessary")) {
			if (ignoreCommand()) return null;
			boolean current = reverseComplementIfNecessary.getValue();
			reverseComplementIfNecessary.toggleValue(parser.getFirstToken(arguments));
		}
		else if (checker.compare(this.getClass(), "Saves current settings as defaults", "[none]", commandName, "saveDefaults")) {
			storePreferences();
		}
		else if (checker.compare(this.getClass(), "Specifies whether or not file settings are to be ignored", "[on; off]", commandName, "toggleIgnoreFileSettings")) {
			ignoreFileSettings.toggleValue(new Parser().getFirstToken(arguments));
			storePreferences();
		}

		else  if (checker.compare(this.getClass(), "Allows one to specify gap opening and extension costs.", "[open; extend]", commandName, "gapCosts")) {
			MesquiteInteger io = new MesquiteInteger(0);
			int newGapOpen = MesquiteInteger.fromString(arguments, io);
			int newGapExtend= MesquiteInteger.fromString(arguments, io);
			int newGapOpenTerminal = MesquiteInteger.fromString(arguments, io);
			int newGapExtendTerminal= MesquiteInteger.fromString(arguments, io);
			if (newGapOpen<0 || newGapExtend<0 || !MesquiteInteger.isCombinable(newGapOpen) || !MesquiteInteger.isCombinable(newGapExtend) || newGapOpenTerminal<0 || newGapExtendTerminal<0 || !MesquiteInteger.isCombinable(newGapOpenTerminal) || !MesquiteInteger.isCombinable(newGapExtendTerminal)){
				if (!MesquiteThread.isScripting())
					AlignUtil.queryGapCosts(containerOfModule(),this,gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal);
			}
			else{
				gapOpen.setValue(newGapOpen);
				gapExtend.setValue(newGapExtend);
				gapOpenTerminal.setValue(newGapOpenTerminal);
				gapExtendTerminal.setValue(newGapExtendTerminal);
			}
			resetAligner();
			//parametersChanged(null);

		}
		else  if (checker.compare(this.getClass(), "Allows one to specify substitution costs.", "[matrix of costs]", commandName, "subCosts")) {
			MesquiteInteger io = new MesquiteInteger(0);
			if (subs==null)
				subs = AlignUtil.getDefaultSubstitutionCosts(alphabetLength); 
			boolean badCost = false;
			for (int i = 0; i<alphabetLength && !badCost; i++)
				for (int j = 0; j<alphabetLength && !badCost; j++) 
					if (i!=j) {
						int newSubCost = MesquiteInteger.fromString(arguments, io);
						if (newSubCost<0 ||  !MesquiteInteger.isCombinable(newSubCost)) {
							badCost = true;
						} else
							subs[i][j] = newSubCost;
					}
			if (badCost && !MesquiteThread.isScripting()){
				Integer2DArray subArray = new Integer2DArray(subs);
				if (AlignUtil.querySubCosts(containerOfModule(),this,subArray,data.getSymbols()))
					subs = subArray.getMatrix();
			}
			resetAligner();
			//parametersChanged(null);

		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	/*.................................................................................................................*/
	public void resetAligner() {
		if (aligner!=null) {
			aligner.setGapCosts(gapOpen.getValue(), gapExtend.getValue(), gapOpenTerminal.getValue(), gapExtendTerminal.getValue());
			aligner.setSubsCostMatrix(subs);
			aligner.setAllowNewInternalGaps(true);
		}
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return -100;  
	}

}


