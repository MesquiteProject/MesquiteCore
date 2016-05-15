/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.align.AlignToDropped; 


import java.awt.*;

import mesquite.align.lib.*;
import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.table.*;
import mesquite.lib.duties.*;


/* ======================================================================== */
public class AlignToDropped extends DataWindowAssistantI {
	public String getFunctionIconPath(){  //path to icon explaining function, e.g. a tool
		return getPath() + "alignToDropped.gif";
	}
	MesquiteTable table;
	CategoricalData data;
	TableTool alignDropTool;
	int firstColumnTouched = -2;
	int firstRowTouched = -2;
	boolean defaultWarnCheckSum  =true;
	MesquiteBoolean warnCheckSum = new MesquiteBoolean(defaultWarnCheckSum);
//	boolean defaultAllowNewGaps  =true;
//	MesquiteBoolean allowNewGaps = new MesquiteBoolean(defaultAllowNewGaps);
	boolean defaultReverseComplementIfNecessary  =false;
	MesquiteBoolean reverseComplementIfNecessary = new MesquiteBoolean(defaultReverseComplementIfNecessary);
	boolean defaultIgnoreFileSettings  =false;
	MesquiteBoolean ignoreFileSettings = new MesquiteBoolean(defaultIgnoreFileSettings);
	boolean defaultShiftOnly  =false;
	MesquiteBoolean shiftOnly = new MesquiteBoolean(defaultShiftOnly);
	long originalCheckSum;
	MesquiteInteger gapOpen = new MesquiteInteger();
	MesquiteInteger gapExtend = new MesquiteInteger();
	MesquiteInteger gapOpenTerminal = new MesquiteInteger();
	MesquiteInteger gapExtendTerminal = new MesquiteInteger();
	int[][] subs =null;
	int alphabetLength;	 
	PairwiseAligner aligner;
	AlignUtil alignUtil = new AlignUtil();
	static boolean preferencesProcessed=false;

	

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (containerOfModule() instanceof MesquiteWindow) {
			MesquiteCommand touchCommand = MesquiteModule.makeCommand("alignDropTouched",  this);
			touchCommand.setSuppressLogging(true);
			MesquiteCommand dragCommand = MesquiteModule.makeCommand("alignDropDragged",  this);
			dragCommand.setSuppressLogging(true);
			MesquiteCommand dropCommand = MesquiteModule.makeCommand("alignDropDropped",  this);
			dropCommand.setSuppressLogging(true);
			alignDropTool = new TableTool(this, "alignToDropped", getPath(), "alignToDropped.gif", 13,14,"Pairwise Aligner: Aligns touched sequences to the sequence on which they are dropped.", "Aligns touched sequences to the sequence on which they are dropped.", touchCommand, dragCommand, dropCommand);
			alignDropTool.setWorksOnRowNames(true);
			//alignDropTool.setWorksAsArrowOnRowColumnNumbers(true);
			alignDropTool.setPopUpOwner(this);
			alignDropTool.setEmphasizeRowsOnMouseDrag(true);
			((MesquiteWindow)containerOfModule()).addTool(alignDropTool);

		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		setUseMenubar(false); //menu available by touching on button
		addMenuItem("Gap Costs...", MesquiteModule.makeCommand("gapCosts", this));
		addMenuItem("Substitution Costs...", MesquiteModule.makeCommand("subCosts", this));
		addCheckMenuItem(null, "Check Data Integrity", makeCommand("toggleWarnCheckSum",  this), warnCheckSum);
//		addCheckMenuItem(null, "Allow New Internal Gaps", makeCommand("toggleAllowNewGaps",  this), allowNewGaps);
		addCheckMenuItem(null, "Reverse Complement if Necessary", makeCommand("toggleReverseComplementIfNecessary",  this), reverseComplementIfNecessary);
		addMenuItem(null, "-", null);
		addCheckMenuItem(null, "Shift Sequence Without Full Alignment", makeCommand("toggleShiftOnly",  this), shiftOnly);
		addMenuItem(null, "-", null);
		addMenuItem("Save Current Settings as Defaults", MesquiteModule.makeCommand("saveDefaults", this));
		addCheckMenuItem(null, "Ignore File Settings; Use Defaults", MesquiteModule.makeCommand("toggleIgnoreFileSettings", this), ignoreFileSettings);
		AlignUtil.getDefaultGapCosts(gapOpen, gapExtend, gapOpenTerminal, gapExtendTerminal); 

		loadPreferences();
		preferencesProcessed = true;

		
		return true;
	}
	/*.................................................................................................................*/
	public void getSubfunctions(){
		registerSubfunction(new FunctionExplanation("Pairwise Aligner", "(A tool of a Character Matrix Window) Aligns touched sequences to the sequence on which they are dropped.", null, getPath() + "alignToDropped.gif"));
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
	public Snapshot getSnapshot(MesquiteFile file) {
		Snapshot temp = new Snapshot();
		if (warnCheckSum.getValue()!=defaultWarnCheckSum)
			temp.addLine("toggleWarnCheckSum " + warnCheckSum.toOffOnString());
//		if (allowNewGaps.getValue()!=defaultAllowNewGaps)
//			temp.addLine("toggleAllowNewGaps " + allowNewGaps.toOffOnString());
		if (shiftOnly.getValue()!=defaultShiftOnly)
			temp.addLine("toggleShiftOnly " + shiftOnly.toOffOnString());
		if (reverseComplementIfNecessary.getValue()!=defaultReverseComplementIfNecessary)
			temp.addLine("toggleReverseComplementIfNecessary " + reverseComplementIfNecessary.toOffOnString());
		temp.addLine("gapCosts " + gapOpen + " " + gapExtend + " " + gapOpenTerminal + " "+ gapExtendTerminal);


		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i<alphabetLength; i++)
			for (int j = 0; j<alphabetLength; j++) 
				if (i!=j && i<=subs.length && j<=subs[i].length)
					sb.append(" " + subs[i][j]);
		temp.addLine("subCosts " + sb.toString());
		//		if (subs==null)
		//			subs = AlignUtil.getDefaultSubstitutionCosts(alphabetLength); 
		return temp;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("ignoreFileSettings".equalsIgnoreCase(tag))
			ignoreFileSettings.setValue(MesquiteBoolean.fromTrueFalseString(content));
		 if (!preferencesProcessed || ignoreFileSettings.getValue()) {   
			if ("warnCheckSum".equalsIgnoreCase(tag))
				warnCheckSum.setValue(MesquiteBoolean.fromTrueFalseString(content));
//			if ("allowNewGaps".equalsIgnoreCase(tag))
//				allowNewGaps.setValue(MesquiteBoolean.fromTrueFalseString(content));
			if ("shiftOnly".equalsIgnoreCase(tag))
				shiftOnly.setValue(MesquiteBoolean.fromTrueFalseString(content));
			if ("reverseComplementIfNecessary".equalsIgnoreCase(tag))
				reverseComplementIfNecessary.setValue(MesquiteBoolean.fromTrueFalseString(content));
		}
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(60);	
		StringUtil.appendXMLTag(buffer, 2, "ignoreFileSettings",ignoreFileSettings);
		StringUtil.appendXMLTag(buffer, 2, "warnCheckSum", warnCheckSum);  
//		StringUtil.appendXMLTag(buffer, 2, "allowNewGaps", allowNewGaps);  
		StringUtil.appendXMLTag(buffer, 2, "shiftOnly", shiftOnly);  
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
		if (alignDropTool!=null && data!=null)
			alignDropTool.setEnabled(!data.isEditInhibited() && data.canMoveChars());
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
	private void shiftTouchedToDropped(long[][] aligned, long[] newAlignment, int rowToAlign, int recipientRow, int columnDropped,int droppedCellCount, boolean droppedOnData){

		/*now let's take stock of where we are.  By default we will find the cell in the dragged sequence that maps onto the cell dropped onto,
		 * but this will be overridden if there are no dragged cells that map to it.
		 * */
		if (!droppedOnData) {  // not dropped on data; find nearest dropcell
			if (droppedCellCount<=0){  //this means it was dropped to the left of the first cell in the dropped sequence 
				droppedCellCount=1;
				for (int icDropped=0; icDropped<data.getNumChars(); icDropped++) {  // let's add up how many data-filled cells are up to the sequence dropped
					if (!data.isInapplicable(icDropped, recipientRow)){
						columnDropped=icDropped;
						break;
					}
				}
			} else { // was either internal gap or terminal gap at right end
				for (int icDropped=columnDropped; icDropped>=0; icDropped--) {  // let's shift downward until we find the first cell
					if (!data.isInapplicable(icDropped, recipientRow)){
						columnDropped=icDropped;
						break;
					}
				}
			}
		} 

		//let's find where the dropped cell is in the new alignment
		int droppedAlignmentCount=0;
		int droppedCellPositionInAlignment = 0;

		for (int ic=0; ic<newAlignment.length; ic++) {  // let's see the position of droppedCellCount in the alignment of this sequence
			if (!CategoricalState.isInapplicable(aligned[ic][0])) {
				droppedAlignmentCount++;
				if (droppedAlignmentCount>=droppedCellCount) { // we have found the position in the alignment
					droppedCellPositionInAlignment = ic;   
					break;
				}
			}
		}
		// we know it was dropped on the position that is at droppedCellPositionInAlignment in the new alignment.

		//let's see how many cells are in the alignment up to that point in the dragged sequence
		int draggedAlignmentCellCount=0;
		int lastFilledCellDraggedAlignmentPosition =-1;
		for (int ic=0; ic<newAlignment.length && ic<=droppedCellPositionInAlignment; ic++) {  // let's see how many cells are in the dragged sequence up to that point
			if (!CategoricalState.isInapplicable(newAlignment[ic])) {
				draggedAlignmentCellCount++;
				lastFilledCellDraggedAlignmentPosition=ic;
			}
		}
		int extraGapsInNewAlignment= droppedCellPositionInAlignment-lastFilledCellDraggedAlignmentPosition;
		
		// we now know what cell was moved onto the dropped cell.  Where was this cell in the original alignment?
		
		int draggedCellCountInOriginal=0;
		int lastFilledCellDraggedOriginalPosition =0;
		for (int ic=0; ic<data.getNumChars() && draggedCellCountInOriginal<=draggedAlignmentCellCount; ic++) {  
			if (!data.isInapplicable(ic, rowToAlign)){
				draggedCellCountInOriginal++;
				lastFilledCellDraggedOriginalPosition=ic;
			}
		}
		
		int positionInOriginalAlignment= lastFilledCellDraggedOriginalPosition+extraGapsInNewAlignment;
		int amountToMove = columnDropped-positionInOriginalAlignment+1;

		MesquiteBoolean dataChanged = new MesquiteBoolean (false);
		MesquiteInteger charAdded = new MesquiteInteger(0);
		int added = data.shiftAllCells(amountToMove, rowToAlign, true, true, true, dataChanged,charAdded, null);
		if (charAdded.isCombinable() && charAdded.getValue()!=0 && data instanceof DNAData) {
			((DNAData)data).assignCodonPositionsToTerminalChars(charAdded.getValue());
			//						((DNAData)data).assignGeneticCodeToTerminalChars(charAdded.getValue());
		}
		//MAY NEED TO NOTIFY!!!!!!


		}


	/*.................................................................................................................*/
	private boolean alignTouchedToDropped(int rowToAlign, int recipientRow, int columnDropped, int columnDragged){
		MesquiteNumber score = new MesquiteNumber();
		int droppedCellCount=0;
		boolean revComplemented=false;
		boolean droppedOnData = !data.isInapplicable(columnDropped, recipientRow);
		if (shiftOnly.getValue()!=optionDown) { // let's find out how much data is in front of the dropped cell
			for (int icDropped=0; icDropped<=columnDropped && icDropped<data.getNumChars(); icDropped++) {  // let's add up how many data-filled cells are up to the sequence dropped
				if (!data.isInapplicable(icDropped, recipientRow))
					droppedCellCount++;
			}
		}

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
			long[][] aligned = aligner.alignSequences((MCategoricalDistribution)data.getMCharactersDistribution(), recipientRow, rowToAlign,MesquiteInteger.unassigned,MesquiteInteger.unassigned,true,score);
			if (aligned==null) {
				logln("Alignment failed!");
				return false;
			}
			logln("Align " + (rowToAlign+1) + " onto " + (recipientRow+1));
			long[] newAlignment = Long2DArray.extractRow(aligned,1);

			if (shiftOnly.getValue()!=optionDown) {
				shiftTouchedToDropped(aligned,newAlignment,  rowToAlign,  recipientRow,  columnDropped, droppedCellCount,  droppedOnData);
			} else {  // standard alignment
				int[] newGaps = aligner.getGapInsertionArray();
				if (newGaps!=null) 
					alignUtil.insertNewGaps((MolecularData)data, newGaps, aligner.getPreSequenceTerminalFlaggedGap(), aligner.getPostSequenceTerminalFlaggedGap());
				Rectangle problem = alignUtil.forceAlignment((MolecularData)data, 0, data.getNumChars()-1, rowToAlign, rowToAlign, 1, aligned);
			}

			((CategoricalData)data).examineCheckSum(0, data.getNumChars()-1,rowToAlign, rowToAlign, "Bad checksum; alignment has inappropriately altered data!", warnCheckSum, originalCheckSum);
			return true;
		}

		return false;
	}
	private boolean ignoreCommand(){
		return (ignoreFileSettings.getValue() && MesquiteThread.isScripting());
	}
	/*.................................................................................................................*/
	boolean alignJustTouchedRow = true;
//	int columnDragged = MesquiteInteger.unassigned;

	boolean optionDown=false;
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
				firstColumnTouched= MesquiteInteger.fromString(arguments, io);
				firstRowTouched= MesquiteInteger.fromString(arguments, io);

				if (!table.rowLegal(firstRowTouched))
					return null;
				if (table.isRowSelected(firstRowTouched)) {
					alignJustTouchedRow = false;
				}
				else{  // it's not select, so deselect everyone else
					table.offAllEdits();
					table.deselectAndRedrawAllSelectedRows();

					table.selectRow(firstRowTouched);
					table.redrawFullRow(firstRowTouched);
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
				int columnDropped = MesquiteInteger.fromString(arguments, io);
				int rowDropped= MesquiteInteger.fromString(arguments, io);

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
						boolean changed = false;
						int oldNumChars = data.getNumChars();
						for (int it = 0; it<table.getNumRows(); it++) 
							if (table.isRowSelected(it) && (it!=rowDropped)) {
								if (alignTouchedToDropped(it,rowDropped, columnDropped, firstColumnTouched))
									changed = true;
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
					boolean changed=false;
					int oldNumChars = data.getNumChars();
					if (alignTouchedToDropped(firstRowTouched,rowDropped, columnDropped, firstColumnTouched))
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
		else  if (checker.compare(this.getClass(), "Toggles whether the sequences is simply shifted, as opposed do a full pairwise alignment.", "[on; off]", commandName, "toggleShiftOnly")) {
			if (ignoreCommand()) return null;
			boolean current = shiftOnly.getValue();
			shiftOnly.toggleValue(parser.getFirstToken(arguments));
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
	void resetAligner() {
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
	/*.................................................................................................................*/
	public String getName() {
		return "Align To Dropped";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies an alignment tool that can be used on a set of sequences.  Sequences dropped by this tool on another sequence will be aligned to that other sequence (pairwise)." ;
	}

}


