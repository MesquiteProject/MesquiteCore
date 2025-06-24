/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.meristic.lib;

import mesquite.lib.NameReference;
import mesquite.lib.characters.CharacterData;

/* ======================================================================== */
/** Contains an array of  meristic character states for one character, at each of the taxa or nodes */
public class MeristicEmbedded  extends MeristicDistribution {
	MeristicData data;
	public MeristicEmbedded (MeristicData data, int ic) {
		super(data.getTaxa());
		this.data = data;
		this.characterNumber = ic;
	}
	/** Indicates the type of character stored */ 
	public Class getStateClass(){
		return data.getStateClass();
	}
	/*..........................................MeristicEmbedded................*/
	public CharacterData getParentData() {
		return data;
	}
	/*..........................................MeristicEmbedded................*/
	public int getNumItems() {
		return data.getNumItems();
	}
	/*..........................................MeristicEmbedded................*/
	public NameReference getItemReference(String name){
		return data.getItemReference(name);
		
	}
	/*..........................................MeristicEmbedded................*/
	public NameReference getItemReference(int index){
		return data.getItemReference(index);
	}
	/*..........................................MeristicEmbedded................*/
	public int getItemNumber(NameReference nr){
		return data.getItemNumber(nr);
	}
	/*..........................................MeristicEmbedded................*/
	public String getItemName(int index){
			return data.getItemName(index);
	}
	/*..........................................MeristicEmbedded................*/
	public int getNumTaxa() {
		return data.getNumTaxa();
	}
	/*..........................................MeristicEmbedded................*/
	public int getState (int N, int item) {
		return data.getState(characterNumber, N, item);
	}
	/*..........................................MeristicEmbedded................*/
	public int getState (int N) {
		return data.getState(characterNumber, N, 0);
	}
	public String getName(){
		return data.getCharacterName(characterNumber);
	}
}

