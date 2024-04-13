/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.genomic.HighlightDiscordantSeq; 

import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.lib.table.*;
import mesquite.categ.lib.*;

/* ======================================================================== */
public class HighlightDiscordantSeq extends DataWindowAssistantID implements CellColorer, CellColorerMatrix {
	MesquiteTable table;
	CharacterData data;
	int[][] freqs;
	double[][] scores;
	long oldID = -1;
	long oldStatesVersion = -1;
	int spanRadius = 12;
	double unusualityCutoff = 0.7;
	double mostUnusual = 0;
	long A = CategoricalState.makeSet(0);
	long C = CategoricalState.makeSet(1);
	long G = CategoricalState.makeSet(2);
	long T = CategoricalState.makeSet(3);
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		return true;
	}
	public boolean setActiveColors(boolean active){
		boolean wasActive = isActive();
		setActive(active);
		if (isActive() && !wasActive){
			calculateNums();

		}
		else {
		}
		resetContainingMenuBar();
		return true; //TODO: check success
	}
	public void endJob(){
		if (data!=null)
			data.removeListener(this);
		super.endJob();
	}
	public String getColorsExplanation(){
		return null;
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void viewChanged(){
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		this.table = table;
		if (this.data!=data && this.data!=null)
			this.data.removeListener(this);
		this.data = data;
		data.addListener(this);
		calculateNums();
	}
	double compareStateToSite(long state, int ic){
		int notIn = 0;
		if ((A & state) == 0L)  //state doesn't include A; therefore column's freq of A to cost
			notIn += freqs[ic][0];
		if ((C & state) == 0L) //likewise for C
			notIn += freqs[ic][1];
		if ((G & state) == 0L)
			notIn += freqs[ic][2];
		if ((T & state) == 0L)
			notIn += freqs[ic][3];
		int tot = freqs[ic][0] + freqs[ic][1] + freqs[ic][2] + freqs[ic][3];
		if (tot == 0)
			return 0;
		return (notIn*1.0/(tot));
	}

	/*give score of comparison of sequence of states around ic in taxon it to 
	double score(DNAData dData, int ic, int it, int offset){
		double s = 0;
		for (int ic2 = ic-widthHalf; ic2<= ic + widthHalf; ic2++){ //surveying window
			if (ic2>=0 && ic2<dData.getNumChars() && ic2+offset>=0 && ic2+offset<dData.getNumChars()){
				long state = dData.getState(ic2, it);
				s += compareStateToSite(state, ic2+offset);
			}
		}
		return s;

	}
	*/
	boolean notCalculated = true;

	double howUnusual(int[][] freqs, DNAData data, int ic, int it) {
		if (ic<0 || ic>=data.getNumChars())
			return 0;
		int dominantFreq = -1;
		int thisFreq = -1;
		int f = freqs[ic][0];
		if (f>dominantFreq)
			dominantFreq = f;
		if ((data.getState(ic, it) & A) != 0L && f> thisFreq) 
			thisFreq = f;
		f = freqs[ic][1];
		if (f>dominantFreq)
			dominantFreq = f;
		if ((data.getState(ic, it) & C) != 0L && f> thisFreq) 
			thisFreq = f;
		f = freqs[ic][2];
		if (f>dominantFreq)
			dominantFreq = f;
		if ((data.getState(ic, it) & G) != 0L && f> thisFreq) 
			thisFreq = f;
		f = freqs[ic][3];
		if (f>dominantFreq)
			dominantFreq = f;
		if ((data.getState(ic, it) & T) != 0L && f> thisFreq) 
			thisFreq = f;
		if (thisFreq != 0)
			return 1.0*(dominantFreq)/thisFreq;
		return 0;
	}




	public void calculateNums(){
		notCalculated = true;
		if (!isActive())
			return;
		if (data == null || !(data instanceof DNAData)) {
			return;
		}
		DNAData dData = (DNAData)data;
		if (freqs == null || freqs.length!=dData.getNumChars()){
			freqs = new int[dData.getNumChars()][4];
		}
		if (scores == null || scores.length != dData.getNumChars() || scores[0].length != dData.getNumTaxa()){
			scores = new double[dData.getNumChars()][dData.getNumTaxa()];
		}
		 mostUnusual = 0;
		//calculating frequencies for each character
		for (int ic2 = 0; ic2 < dData.getNumChars(); ic2++)
			for (int it2 = 0; it2 < dData.getNumTaxa(); it2++){
				if ((A & dData.getState(ic2, it2)) != 0L)
					freqs[ic2][0]++;
				if ((C & dData.getState(ic2, it2)) != 0L)
					freqs[ic2][1]++;
				if ((G & dData.getState(ic2, it2)) != 0L)
					freqs[ic2][2]++;
				if ((T & dData.getState(ic2, it2)) != 0L)
					freqs[ic2][3]++;
			}
		//filling score and offsets matrices
		//Score is average over blockRadius bases left and right (separated by not more than gapMax gaps) of how unusual is base in this taxon
		//How unusual is base is the number of taxa with dominant state opposing it. If dominant state, nothing.
		for (int ic = 0; ic<dData.getNumChars(); ic++){
			for (int it = 0; it< dData.getNumTaxa(); it++){
				double spanUnusual = 0;
				int spanAvailable = 0;
				for (int offset= -spanRadius; offset<=spanRadius; offset++){
					if (ic+offset>=0 && ic+offset<dData.getNumChars())
						spanAvailable++;
					double sD = howUnusual(freqs, dData,  ic+offset, it);
					if (sD >=0)
						spanUnusual += sD;
				}
				if (spanAvailable>0)
				scores[ic][it] = spanUnusual * (spanRadius*2+1)/spanAvailable;
				if (spanUnusual> mostUnusual)
					mostUnusual = spanUnusual;
			}
		}
		Debugg.println("mostUnusual " + mostUnusual);
		notCalculated = false;
		table.repaintAll();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		calculateNums();
		if (table !=null)
			table.repaintAll();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Highlight Unusual Subsequences";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public String getExplanation() {
		return "Colors aligned sequences to emphasize sections that seem discordant with those of the other taxa.";
	}
	/*.................................................................................................................*/
	ColorRecord[] legend;
	public ColorRecord[] getLegendColors(){
		return null;
		/*
		if (legend == null) {
			legend = new ColorRecord[6];
			legend[0] = new ColorRecord(Color.white, "Apparently aligned");
			legend[1] = new ColorRecord(Color.yellow, "Better shifted to right 1 site");
			legend[2] = new ColorRecord(Color.red, "Better shifted to right 2 sites");
			legend[3] = new ColorRecord(Color.green, "Better shifted to left 1 site");
			legend[4] = new ColorRecord(Color.blue, "Better shifted to left 2 sited");
			legend[5] = new ColorRecord(ColorDistribution.straw, "Inapplicable");
		}
		return legend;
		 */
	}
	/*.................................................................................................................*/
	public Color getCellColor(int ic, int it){
		if (ic<0 || it<0 || notCalculated)
			return null;

		if (data == null || scores == null)
			return null;
		else if (data.isInapplicable(ic, it)){
			return ColorDistribution.straw;
		}
		else {
			if (!(data instanceof DNAData))
				return Color.white;
			DNAData	dData = (DNAData)data;
			long state = dData.getState(ic, it);
			if (scores[ic][it] < mostUnusual*unusualityCutoff) {
				if (A == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStatePale(0);
				else if (C == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStatePale(1);
				else if (G == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStatePale(2);
				else if (T == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfStatePale(3);
			}
			else {
				if (A == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfState(0);
				else if (C == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfState(1);
				else if (G == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfState(2);
				else if (T == (state & CategoricalState.statesBitsMask))
					return DNAData.getDNAColorOfState(3);
			}
			return ColorDistribution.veryLightGray;
			/*
			if (offsets[ic][it] == 0)
				return Color.white;
			if (offsets[ic][it] ==1)
				return MesquiteColorTable.getYellowScale(scores[ic][it], 0, 5, false);
			if (offsets[ic][it] >1)
				return MesquiteColorTable.getRedScale(scores[ic][it], 0, 5, false);
			if (offsets[ic][it] ==-1)
				return MesquiteColorTable.getGreenScale(scores[ic][it], 0, 5, false);
			if (offsets[ic][it] <-1)
				return MesquiteColorTable.getBlueScale(scores[ic][it], 0, 5, false);
			 */
		}
		//		return Color.black;
	}
	/*.................................................................................................................*/
	public String getCellString(int ic, int it){
		if (ic<0 || it<0 || notCalculated)
			return null;

		if (data == null || scores == null)
			return null;
		else if (data.isInapplicable(ic, it)){
			return "Gap";
		}
		else {
			if (scores[ic][it] < mostUnusual/2.0)
				return "In unusal subsequence";
		}
		return null;
	}
	/** passes which object changed, along with optional code number (type of change) and integers (e.g. which character)*/
	public void changed(Object caller, Object obj, Notification notification){
		if (obj instanceof CharacterData){
			if (Notification.appearsCosmetic(notification))
				return;
			calculateNums();
			if (table!=null)
				table.repaintAll();
		}
	}

	/*.................................................................................................................*/
	public boolean isSubstantive() {
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease() {
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyDNAData();
	}
}


