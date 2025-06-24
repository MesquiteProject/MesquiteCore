/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.basic.PrefixedStringMatcher;

import mesquite.lib.IntegerField;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.StringUtil;
import mesquite.lib.duties.StringMatcher;
import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

public class PrefixedStringMatcher extends StringMatcher {
	String prefix = "DNA";
	int charAfterPrefix = 4;

	public boolean loadModule() {  
		return false;
	}

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		return true;
	}
	/** returns true if the options are set and accepted.*/
	public  boolean queryOptions(){
			if (!okToInteractWithUser(CAN_PROCEED_ANYWAY, "Querying Options")) 
				return true;

			MesquiteInteger buttonPressed = new MesquiteInteger(1);
			ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Match Text After Prefix",buttonPressed); 

			SingleLineTextField prefixField = dialog.addTextField("Prefix before characters to compare", prefix, 15);
			IntegerField charAfterPrefixField = dialog.addIntegerField("Number of characters to compare after prefix", charAfterPrefix, 8, 1, MesquiteInteger.infinite);

			dialog.completeAndShowDialog(true);

			if (buttonPressed.getValue()==0)  {
				charAfterPrefix=charAfterPrefixField.getValue();
				prefix= prefixField.getText();
			}
			dialog.dispose();

			return (buttonPressed.getValue()==0 && charAfterPrefix>0);
	}
	public boolean stringsMatch(String s1, String s2) {
		if (StringUtil.blank(prefix)) 
			return false;
		int location1 = s1.indexOf(prefix);
		int location2 = s2.indexOf(prefix);
		if (location1<0 || location2 < 0)
			return false;
		String nextChars1 = null;
		String nextChars2 = null;
		try {
			nextChars1 = s1.substring(location1+prefix.length(),location1+prefix.length()+charAfterPrefix);
			nextChars2 = s2.substring(location2+prefix.length(),location2+prefix.length()+charAfterPrefix);
		}
		catch (Exception e) {
			return false;
		}
		if (StringUtil.blank(nextChars1) || StringUtil.blank(nextChars2))
			return false;
		return nextChars1.equalsIgnoreCase(nextChars2);
	}


	public Class getDutyClass() {
		return StringMatcher.class;
	}

	public String getName() {
		return "Match Text After Prefix";
	}
	public String getExplanation() {
		return "Judges two text strings to match if it finds the same text in a specified number of characters after the first occurence of a designated prefix.";
	}

}
