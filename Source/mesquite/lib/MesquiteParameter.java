/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;


/* ======================================================================== */
/**An object to wrap a parameter used.  New after 1. 12.  Limited use; in future could replace many uses of MesquiteDouble for module parameters.*/
public class MesquiteParameter extends MesquiteDouble implements Explainable {
	double minimumAllowed = MesquiteDouble.unassigned;
	double maximumAllowed = MesquiteDouble.unassigned;
	double minimumSuggested = MesquiteDouble.unassigned;
	double maximumSuggested = MesquiteDouble.unassigned;
	String explanation = "";
	boolean valueLocked = false;
	double provisional = MesquiteDouble.unassigned;
	MesquiteParameter constrainedToBe;
	public MesquiteParameter(){
	}
	public MesquiteParameter(String name, String explanation,	double value, double minimumAllowed, double maximumAllowed, double minimumSuggested, double maximumSuggested){
		setName(name);
		this.explanation = explanation;
		setValue(value);
		this.minimumAllowed = minimumAllowed;
		this.maximumAllowed = maximumAllowed;
		this.minimumSuggested = minimumSuggested;
		this.maximumSuggested = maximumSuggested;
	}
	public MesquiteParameter(MesquiteParameter param){
		setName(param.getName());
		this.explanation = param.explanation;
		setValue(param.getValue());
		this.minimumAllowed = param.minimumAllowed;
		this.maximumAllowed = param.maximumAllowed;
		this.minimumSuggested = param.minimumSuggested;
		this.maximumSuggested = param.maximumSuggested;
	}
	public MesquiteParameter getConstrainedTo(){
		return constrainedToBe;
	}
	public void setConstrainedTo(MesquiteParameter other, boolean resetValue){
		constrainedToBe = other;
		if (other != null && resetValue){
			setValue(other.getValue());
		}
	}
	public void setValues(MesquiteParameter param){
		setName(param.getName());
		this.explanation = param.explanation;
		setValue(param.getValue());
		this.minimumAllowed = param.minimumAllowed;
		this.maximumAllowed = param.maximumAllowed;
		this.minimumSuggested = param.minimumSuggested;
		this.maximumSuggested = param.maximumSuggested;
		this.provisional = provisional;
	}
	public int whichParameter(MesquiteParameter[] p){
		if (p == null)
			return -1;
		for (int i=0; i<p.length; i++){
			if (this == p[i])
				return i;
		}
		return -1;
	}
	public void setValue(double d){
		super.setValue(d);
		if (constrainedToBe != null){
			//constrainedToBe.setValueButNotConstrainee(d);
		}
	}
	public void setValueButNotConstrainee(double d){
		super.setValue(d);
	}
 	public double getMinimumAllowed(){
 		return minimumAllowed;
 	}
 	public void setMinimumAllowed(double m){
 		minimumAllowed = m;
 	}
	public double getMaximumAllowed(){
 		return maximumAllowed;
 	}
 	public void setMaximumAllowed(double m){
 		maximumAllowed = m;
 	}
 	public double getMinimumSuggested(){
 		return minimumSuggested;
 	}
 	public void setMinimumSuggested(double m){
 		minimumSuggested = m;
 	}
	public double getMaximumSuggested(){
 		return maximumSuggested;
 	}
 	public void setMaximumSuggested(double m){
 		maximumSuggested = m;
 	}
	public String getExplanation(){
 		return explanation;
 	}
 	public void setExplanation(String e){
 		explanation = e;
 	}
 	public boolean isValueLocked(){
 		return valueLocked;
 	}
 	public void setValueLocked(boolean vl){
 		valueLocked = vl;
 	}
 	
 	public void setProvisionalValue(double p){ //for dialog boxes etc. to set value without it being fixed until acceptProvisionalValue() is called
 		provisional = p;
 	}
	public double getProvisionalValue(){ //for dialog boxes etc. to set value without it being fixed until acceptProvisionalValue() is called
		return provisional;
 	}
	public void acceptProvisionalValue(){ //for dialog boxes etc. to set value without it being fixed until acceptProvisionalValue() is called
		setValue(provisional);
 	}
	
		public static int getWhichConstrained(MesquiteParameter[] params, int i){
		if (params == null)
			return -1;
		MesquiteParameter p = params[i].getConstrainedTo();
		if (p == null)
			return -1;
		for (int k=0; k<params.length; k++){
			if (params[k] == p)
				return k;
		}
		return -1;
	}
		public static String paramsToScriptString(MesquiteParameter[] p){
			if (p == null)
				return null;
			String pString = "";
			for (int i = 0; i<p.length; i++)
				pString += " " + p[i].toString()+ " " + MesquiteParameter.constraintsToString(p, i);
			return pString;
		}
		public static String constraintsToString(MesquiteParameter[] params, int i){
			if (params == null)
				return "";
			MesquiteParameter p = params[i].getConstrainedTo();
			if (p == null)
				return "";
			for (int k=0; k<params.length; k++){
				if (params[k] == p)
					return " =" + k;
			}
			return "";
		}
	public static MesquiteParameter[] cloneArray(MesquiteParameter[] original, MesquiteParameter[] destination){
		if (original == null)
			return null;
		if (destination == null || destination.length != original.length)
			destination = new MesquiteParameter[original.length];
		for (int i=0; i<destination.length; i++){
			if (destination[i] == null)
				destination[i] = new MesquiteParameter();
			destination[i].setValues(original[i]);
		}
		for (int i=0; i<destination.length; i++){
			MesquiteParameter con = original[i].getConstrainedTo();
			if (con != null)
				destination[i].setConstrainedTo(destination[MesquiteParameter.getWhichConstrained(original, i)], true);
			else
				destination[i].setConstrainedTo(null, false);
		}
		return destination;
	}
	public static int numberSpecified(MesquiteParameter[] params){
		if (params == null)
			return 0;
		int count = 0;
		for (int i=0; i<params.length; i++){
			if (params[i].isCombinable())
				count++;
		}
		return count;
	}
	public static String toString(MesquiteParameter[] params){
		if (params == null)
			return "";
		String s = "";
		for (int i=0; i<params.length; i++)
			s += params[i].getName() + "=" + MesquiteDouble.toStringDigitsSpecified(params[i].getValue(), 5) + " ";
		return s;
	}
	public static String toStringForAnalysis(MesquiteParameter[] params){
		if (params == null)
			return "";
		String s = "";
		for (int i=0; i<params.length; i++){
			if (i>0)
				s += "\t";
			s += MesquiteDouble.toString(params[i].getValue());
		}
		return s;
	}
	public static String toString(double[] params){
		if (params == null)
			return "";
		String s = "";
		for (int i=0; i<params.length; i++)
			s += MesquiteDouble.toStringDigitsSpecified(params[i], 5) + " ";
		return s;
	}
	public static String toStringWithConstraints(MesquiteParameter[] params){
		if (params == null)
			return "";
		String s = toString(params);
		boolean cfound = false;
		for (int i=0; i<params.length; i++){
			MesquiteParameter con = params[i].getConstrainedTo();
			if (con != null) {
				s += i + "=" + MesquiteParameter.getWhichConstrained(params, i) + " ";
				cfound = true;
			}
		}
		if (!cfound)
			s += " [no constraints]";
		return s;
	}
}

