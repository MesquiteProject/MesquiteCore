/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;

import java.text.Collator;

//an alternative to Collator that considers numerical values
public class MesquiteCollator {
	Collator collator;
	Parser pA, pB;
	public MesquiteCollator (){
		collator = Collator.getInstance();
		pA = new Parser();
		pB = new Parser();
	}
	boolean couldBeNumber(String a){
		if (a == null || a.length() == 0)
			return false;
		char c = a.charAt(0);
		if (c == '0')
			return true;
		else if (c == '1')
			return true;
		else if (c == '2')
			return true;
		else if (c == '3')
			return true;
		else if (c == '4')
			return true;
		else if (c == '5')
			return true;
		else if (c == '6')
			return true;
		else if (c == '7')
			return true;
		else if (c == '8')
			return true;
		else if (c == '9')
			return true;
		else if (c == '.')
			return true;
		else if (c == '-')
			return true;
		return false;
	}
	public int compare(String a, String b){
		pA.setString(a);
		pB.setString(b);

		if (couldBeNumber(a) && couldBeNumber(b)){
			double dA = MesquiteDouble.fromString(pA);
			double dB = MesquiteDouble.fromString(pB);
			if (MesquiteDouble.isCombinable(dA) && MesquiteDouble.isCombinable(dB)){
				if (dA < dB)
					return -1;
				else if (dA == dB)
					return 0;
				else
					return 1;
			}
		}
		return collator.compare(a,b);
	}

}


