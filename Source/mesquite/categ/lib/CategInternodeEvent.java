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


import mesquite.lib.*;


/* ======================================================================== */
public class CategInternodeEvent  {
	long state = 0L;
	double position = 0;
	boolean change = true;
	
	public CategInternodeEvent (long state, double position, boolean change) {
		this.state = state;
		this.position = position;
		this.change = change;
	}
	public CategInternodeEvent cloneEvent(){
		return new CategInternodeEvent (state, position, change);
	}
	public void setState(long state){
		this.state = state;
	}
	public long getState(){
		return state;
	}
	public void setPosition(double position){
		this.position = position;
	}
	public double getPosition(){
		return position;
	}
	public void setChangeVersusSample(boolean change){
		this.change = change;
	}
	/** indicates whether this is a point of character state change or a sample of state at a time*/
	public boolean getChangeVersusSample(){
		return change;
	}
	public String toString(){
		return "change " + change + "; position " + MesquiteDouble.toStringDigitsSpecified(position, 4) + "; state " + CategoricalState.toString(state);
	}
}


