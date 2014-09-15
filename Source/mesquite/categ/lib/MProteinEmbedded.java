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
public class MProteinEmbedded extends MCategoricalEmbedded {
	public MProteinEmbedded (CharacterData data) {
		super(data);
	}
	/*..........................................  MProteinEmbedded  ..................................................*/
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return ProteinState.class;
	}
	/**returns the corresponding CharacterData subclass*/
	public Class getCharacterDataClass (){
		return ProteinData.class;
	}
	/*..........................................  MProteinEmbedded  ..................................................*/
	/** returns the name of the type of data stored */
	public String getDataTypeName(){
		return ProteinData.DATATYPENAME;
	}
	/*..........................................MProteinEmbedded................*/
	/** get CharacterState at node N*/
	public CharacterState getCharacterState (CharacterState cs, int ic, int it){  
		if (cs !=null && cs instanceof ProteinState) {
			((ProteinState)cs).setValue(getState(ic, it));
			return cs;
		}
		return new ProteinState(getState(ic, it)); 
	}
	/*..........................................MProteinEmbedded................*/
	/**return blank adjustable MCharactersDistribution if this same type */
	public MAdjustableDistribution makeBlankAdjustable(){
		return new MProteinAdjustable(getTaxa(), getNumChars(), getNumTaxa());
	}
	/*..........................................MProteinEmbedded................*/
	/**return CharacterData filled with same values as this matrix */
	public CharacterData makeCharacterData(CharMatrixManager manager, Taxa taxa){
		ProteinData data = new ProteinData(manager, getNumTaxa(), getNumChars(), taxa);
		data.setMatrix(this);
		if (getParentData()!=null && getParentData().getAnnotation()!=null)
			data.setAnnotation(getParentData().getAnnotation(), false);
		return data;
	}
	/** returns the union of all state sets*/
	public long getAllStates() {  //
		long s=0;
		for (int i=0; i<= ProteinState.maxProteinState; i++) {
			s = CategoricalState.addToSet(s, i);
		}
		return s;
	}
}

