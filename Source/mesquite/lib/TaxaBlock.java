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

import mesquite.lib.duties.*;

//Moved into mesquite.lib 13 Dec 01
/*===============================================*/
public class TaxaBlock extends NexusBlock {
	Taxa taxa = null;
	public TaxaBlock(MesquiteFile f, MesquiteModule mb){
		super(f, mb);
	}
	public boolean mustBeAfter(NexusBlock block){ //� 13 Dec 01
		if (block==null)
			return false;
		return block.getBlockName().equalsIgnoreCase("AUTHORS");
		
	}
	public void dispose(){
		taxa = null;
		super.dispose();
	}
	public void written() {
		taxa.setDirty(false);
	}
	public String getBlockName(){
		return "TAXA";
	}
	public boolean contains(FileElement e) {
		return e!=null && taxa == e;
	}
	public void setTaxa(Taxa taxa) {
		this.taxa = taxa;
	}
	public Taxa getTaxa() {
		return taxa;
	}
	public String getName(){
		if (taxa==null)
			return "empty taxa block";
		else
			return "Taxa block: " + taxa.getName();
	}
	public String getNEXUSBlock(){
		if (taxa==null)
			return null;
		else
			return ((TaxaManager)getManager()).getTaxaBlock(taxa, this, getFile());
	}
}

