/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.basic.NameParserOnElementName;



import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.lib.characters.CharacterData;

/* ======================================================================== */
public class NameParserOnElementName extends ListableNameAlterer {
	NameParser nameParser;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName){
		nameParser = new NameParser(this, getElementNameSingular());
		loadPreferences();
		return true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer();
		if (nameParser!=null){
			String s = nameParser.preparePreferencesForXML(); 
			if (StringUtil.notEmpty(s))
				buffer.append(s);
		}
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if (nameParser!=null)
			nameParser.processSingleXMLPreference(tag,content);
	}
	/*.................................................................................................................*/
	public boolean getOptions(ListableVector elements, int firstSelected){
		if (MesquiteThread.isScripting())
			return true;
		if (elements == null || elements.size()==0)
			return false;
		Object obj = elements.elementAt(0);
		if (obj == null || !(obj instanceof CharacterData))
			return false;
		CharacterData data = (CharacterData)obj;
		nameParser.setExamples(new String[]{data.getName()});
		
		String helpString = null;

		boolean ok = nameParser.queryOptions("Options for trimming " + getElementNameSingular() + " names", "Names will be trimmed by keeping or deleting parts of them", 
				"In trimming the " + getElementNameSingular() + " names,", helpString);
		if (ok)
			storePreferences();
		return ok;

	}


	/*.................................................................................................................*/
	/** Called to alter the element name in a single cell.  If you use the alterContentOfCells method of this class, 
   	then you must supply a real method for this, not just this stub. */
	public boolean alterName(ListableVector elements, int it){
		boolean nameChanged = false;
		Object obj = elements.elementAt(it);
		if (obj == null || !(obj instanceof NameableWithNotify))
			return false;
		NameableWithNotify element = (NameableWithNotify)obj;
		String name = ((Listable)obj).getName();

		if (name!=null){
			String newName = nameParser.extractPart(name);
			element.setName( newName, false);
			nameChanged = true;
		}
		return nameChanged;
	}
	/*.................................................................................................................*
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	if (checker.compare(this.getClass(), "Adds prefix/suffix to taxon names", "[text]", commandName, "addText")) {
	   	 		if (taxa !=null){
	   	 			String textToAdd = parser.getFirstToken(arguments);
    	 			boolean toEnd = MesquiteBoolean.fromOffOnString(parser.getNextToken());
    	 			prefixToAdd="";
    	 			suffixToAdd="";
	   	 			if (toEnd)
	   	 				suffixToAdd=textToAdd;
	   	 			else
	   	 				prefixToAdd = textToAdd;
	   	 			alterTaxonNames(taxa,table);
	   	 		}
     	 	}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
		return null;
   	 }
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Keep/Delete Parts of Names...";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Keep/Delete Parts of Element Names";
	}

	/*.................................................................................................................*/
	public String getExplanation() {
		return "Changes names by applying a set of rules about keeping and deleting portions of the names.";
	}
	
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return MesquiteModule.NEXTRELEASE;  
	}

}





