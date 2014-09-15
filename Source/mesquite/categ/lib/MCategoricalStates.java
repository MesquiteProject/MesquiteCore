/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.categ.lib;

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**A class for an array of  categorical character states for many characters, at each of the taxa  or nodes.*/
public abstract class MCategoricalStates extends MCharactersStates {
	protected double[][][] frequencies; //[node][state][character]
	protected double[][][] extraFrequencies; //[node][extraFreq][character]
	
	public MCategoricalStates (Taxa taxa) {
		super(taxa);
	}
	/*..........................................  MCategoricalStates  ..................................................*/
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return CategoricalState.class;
	}
	
	/*..........................................  MCategoricalStates  ..................................................*
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return CategoricalData.class;
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** return if frequencies exist for states at each node.*/
	public boolean frequenciesExist(){
		return (frequencies !=null);
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** return if frequencies exist for states at each node.*/
	public boolean extraFrequenciesExist(){
		return (extraFrequencies !=null);
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** Copy frequency information from first to second CategoricalStates.*/
	public static void copyFrequencies(CategoricalStates source, CategoricalStates sink) {
		if (source!=null && sink!=null && source.frequenciesExist()) {
			for (int i=0; i<source.getNumNodes() && i<sink.getNumNodes(); i++) {
				sink.setFrequencies(i, source.getFrequencies(i));
			}
		}
	}
	
	private void prepareFrequencyStorage(int node, int numCategories){
		if (frequencies == null) {
			frequencies = new double[getNumNodes()][][];
			frequencies[node] = new double[numCategories][getNumChars()];
		}
		else if (frequencies[node]== null || frequencies[node].length != numCategories || (numCategories != 0 && (frequencies[node].length<=0 || frequencies[node][0].length != getNumChars()))) {
			frequencies[node] = new double[numCategories][getNumChars()];
		}
	}
	
	protected int getNumFreqCateg(){
		if (frequencies == null) {
			return 0;
		}
		int max = 0;
		for (int i= 0; i<frequencies.length; i++){
			if (frequencies[i] !=null && frequencies[i].length > max)
				max = frequencies[i].length;
		}
		return max;
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** set freqency information*/
	public void setFrequencies(int node, double[][] freqs) {
		if (checkIllegalNode(node, 0))
			return;
		if (freqs!=null) {
			prepareFrequencyStorage(node, freqs.length);
			for (int i=0; i<freqs.length; i++) {
				for (int ic=0; ic<getNumChars(); ic++)
					frequencies[node][i][ic] = freqs[i][ic];
			}
		}
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** set freqency information*/
	public void setFrequencies(int ic, int node, double[] freqs) {
		if (checkIllegalNode(node, 0))
			return;
		if (freqs!=null) {
			prepareFrequencyStorage(node, freqs.length);
			for (int i=0; i<freqs.length; i++) {
				frequencies[node][i][ic] = freqs[i];
			}
		}
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** get freqency information at a particular node*/
	public double[][] getFrequencies(int node) {
		if (checkIllegalNode(node, 1))
			return null;
		if (frequencies != null)
			return frequencies[node];
		return null;
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** get freqency of particular category at a particular node*/
	public double getFrequency(int ic, int node, int category) {
		if (checkIllegalNode(node, 2))
			return 0;
		if (frequencies == null || frequencies[node]== null  || category >= frequencies[node].length || frequencies[node][category]== null)
			return MesquiteDouble.unassigned;
		else
			return frequencies[node][category][ic];
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** dispose frequency information (frees memory). */
	public void disposeFrequencies() {
		frequencies = null;
	}

	
	/*..........................................  MCategoricalStates  ...................................................*/
	/** Copy extra frequency information from first to second CategoricalStates.*/
	public static void copyExtraFrequencies(CategoricalStates source, CategoricalStates sink) {
		if (source!=null && sink!=null && source.extraFrequenciesExist()) {
			for (int i=0; i<source.getNumNodes() && i<sink.getNumNodes(); i++) {
				sink.setExtraFrequencies(i, source.getExtraFrequencies(i));
			}
		}
	}
	
	private void prepareExtraFrequencyStorage(int node, int numCategories){
		if (extraFrequencies == null) {
			extraFrequencies = new double[getNumNodes()][][];
			extraFrequencies[node] = new double[numCategories][getNumChars()];
		}
		else if (extraFrequencies[node]== null || extraFrequencies[node].length != numCategories || (numCategories != 0 && (extraFrequencies[node].length<=0 || extraFrequencies[node][0].length != getNumChars()))) {
			extraFrequencies[node] = new double[numCategories][getNumChars()];
		}
	}
	
	protected int getNumExtraFreqCateg(){
		if (extraFrequencies == null) {
			return 0;
		}
		int max = 0;
		for (int i= 0; i<extraFrequencies.length; i++){
			if (extraFrequencies[i] !=null && extraFrequencies[i].length > max)
				max = extraFrequencies[i].length;
		}
		return max;
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** set extra freqency information*/
	public void setExtraFrequencies(int node, double[][] freqs) {
		if (checkIllegalNode(node, 0))
			return;
		if (freqs!=null) {
			prepareExtraFrequencyStorage(node, freqs.length);
			for (int i=0; i<freqs.length; i++) {
				for (int ic=0; ic<getNumChars(); ic++)
					extraFrequencies[node][i][ic] = freqs[i][ic];
			}
		}
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** set extra freqency information*/
	public void setExtraFrequencies(int ic, int node, double[] freqs) {
		if (checkIllegalNode(node, 0))
			return;
		if (freqs!=null) {
			prepareExtraFrequencyStorage(node, freqs.length);
			for (int i=0; i<freqs.length; i++) {
				extraFrequencies[node][i][ic] = freqs[i];
			}
		}
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** get extra freqency information at a particular node*/
	public double[][] getExtraFrequencies(int node) {
		if (checkIllegalNode(node, 1))
			return null;
		if (extraFrequencies != null)
			return extraFrequencies[node];
		return null;
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** get extra freqency of particular category at a particular node*/
	public double getExtraFrequency(int ic, int node, int category) {
		if (checkIllegalNode(node, 2))
			return 0;
		if (extraFrequencies == null || extraFrequencies[node]== null  || category >= extraFrequencies[node].length || extraFrequencies[node][category]== null)
			return MesquiteDouble.unassigned;
		else
			return extraFrequencies[node][category][ic];
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** dispose extra frequency information (frees memory). */
	public void disposeExtraFrequencies() {
		extraFrequencies = null;
	}
/*..........................................  MCategoricalStates  ..................................................*/
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return CategoricalData.DATATYPENAME;
	}
	/*..........................................  MCategoricalStates  ..................................................*/
	/**returns state set of character ic in taxon */
	public abstract long getState (int ic, int it);

	/*..........................................MCategoricalStates................*/
	/** get CharacterState at node N*/
	public CharacterState getCharacterState (CharacterState cs, int ic, int it){  
		if (cs !=null && cs instanceof CategoricalState) {
			((CategoricalState)cs).setValue(getState(ic, it));
			return cs;
		}
		return new CategoricalState(getState(ic, it)); 
	}
	/*..........................................MCategoricalStates................*/
	/** get CharacterState at node N*/
	public CategoricalState getCategoricalState (CategoricalState cs, int ic, int it){  
		if (cs !=null) {
			cs.setValue(getState(ic, it));
			return cs;
		}
		return new CategoricalState(getState(ic, it)); 
	}
	/*..........................................  MCategoricalStates  ...................................................*/
	/** returns the union of all state sets*/
	public long getAllStates() {  //
		long s=0;
		for (int it=0; it< getNumNodes(); it++)
			for (int ic=0; ic< getNumChars(); ic++) 
				s |= getState(ic, it);
		
		return CategoricalState.dataBitsMask & s;
	}
}

