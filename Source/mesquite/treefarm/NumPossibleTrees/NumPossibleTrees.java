/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.treefarm.NumPossibleTrees;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteModule;
import mesquite.lib.duties.TreeUtility;
import mesquite.lib.tree.TextTree;
import mesquite.lib.tree.Tree;

/** ======================================================================== */

public class NumPossibleTrees extends TreeUtility {
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;  
 	}
 	
	public  void useTree(Tree tree) {
		BigInteger num = new BigInteger("1");
		int numTaxa = tree.getNumTaxa();
        NumberFormat formatter = new DecimalFormat("0.####E0", DecimalFormatSymbols.getInstance(Locale.ROOT));
        for (int it=3; it<=numTaxa; it++) {
			int value = 2*it-3;
			String valueString = "" +  value;
			if (it==numTaxa) {
				logln("\nNumber of possible unrooted, dichotomous trees for " + numTaxa+ " terminal taxa: \n" + num.toString());
				logln("   = " + formatter.format(num));
			}
			num=num.multiply(new BigInteger(valueString));
		}
		logln("\nNumber of possible rooted, dichotomous trees for " + numTaxa+ " terminal taxa: \n" + num.toString());
 //       BigDecimal bd = new BigDecimal(num);

		logln("   = " + formatter.format(num));
	}
	
	
	public boolean isSubstantive(){
		return false;
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Write Number of Possible Trees to Log";
   	 }
	/*.................................................................................................................*/
 	/** returns an explanation of what the module does.*/
 	public String getExplanation() {
 		return "Writes number of possible dichotomus trees into log.";
   	 }
   	 
}


