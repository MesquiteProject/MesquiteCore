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
public class AlignToDropped extends AlignShiftToDropped {
	public String getFunctionIconPath(){  //path to icon explaining function, e.g. a tool
		return getPath() + "alignToDropped.gif";
	}

	

	/*.................................................................................................................*/
	public void addExtraMenus(){
		addMenuItem("Gap Costs...", MesquiteModule.makeCommand("gapCosts", this));
		addMenuItem("Substitution Costs...", MesquiteModule.makeCommand("subCosts", this));
	}

	public TableTool getTool(MesquiteCommand touchCommand, MesquiteCommand dragCommand, MesquiteCommand dropCommand) {
		return new TableTool(this, "alignToDropped", getPath(), "alignToDropped.gif", 13,14,"Pairwise Aligner: Aligns touched sequences to the sequence on which they are dropped.", "Aligns touched sequences to the sequence on which they are dropped.", touchCommand, dragCommand, dropCommand);
	}
	public FunctionExplanation getFunctionExplanation() {
		return registerSubfunction(new FunctionExplanation("Pairwise Aligner", "(A tool of a Character Matrix Window) Aligns touched sequences to the sequence on which they are dropped.", null, getPath() + "alignToDropped.gif"));
	}

	/*.................................................................................................................*/
	public void addToSnapshot(Snapshot temp) {
		temp.addLine("gapCosts " + gapOpen + " " + gapExtend + " " + gapOpenTerminal + " "+ gapExtendTerminal);
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i<alphabetLength; i++)
			for (int j = 0; j<alphabetLength; j++) 
				if (i!=j && i<=subs.length && j<=subs[i].length)
					sb.append(" " + subs[i][j]);
		temp.addLine("subCosts " + sb.toString());
	}


	/*.................................................................................................................*/
	protected void alignShiftTouchedToDropped(long[][] aligned, long[] newAlignment, int rowToAlign, int recipientRow, int columnDropped, boolean droppedOnData) {
		int[] newGaps = aligner.getGapInsertionArray();
		if (newGaps!=null) 
			alignUtil.insertNewGaps((MolecularData)data, newGaps, aligner.getPreSequenceTerminalFlaggedGap(), aligner.getPostSequenceTerminalFlaggedGap());
		Rectangle problem = alignUtil.forceAlignment((MolecularData)data, 0, data.getNumChars()-1, rowToAlign, rowToAlign, 1, aligned);
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Toggles whether the new gaps can be introduced into one or the other sequence.", "[on; off]", commandName, "toggleAllowNewGaps")) {
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
	public String getName() {
		return "Align To Dropped";
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Supplies an alignment tool that can be used on a set of sequences.  Sequences dropped by this tool on another sequence will be aligned to that other sequence (pairwise)." ;
	}

}


