/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.util.*;


public abstract class DatabaseURLSource extends MesquiteModule {
	Vector keyValuePairs;
	static final String COUNT = "count";
	
  	 public Class getDutyClass() {
    	 	return DatabaseURLSource.class;
    	 }
  	public String getDutyName() {
  		return "Database URL Source";
    	 }
  	
  	public void addKeyValuePair(String key, String value){
  		if (keyValuePairs==null)
  			keyValuePairs = new Vector();
  		String[] pair = new String[2];
  		pair[0] = key;
  		pair[1] = value;
  		keyValuePairs.addElement(pair);
  	}
  	
	public void addKeyValuePair(int key, String value) {
		addKeyValuePair(getKeyString(key), value);
	}

  	public void removeKeyValuePair(String key){
  		for (int i=0; i<keyValuePairs.size(); i++) {
  			String[] pair= (String[])keyValuePairs.get(i);
  			if (pair!=null && pair[0].equalsIgnoreCase(key))
  				keyValuePairs.remove(i);
  		}
  	}

	public boolean needsKeyValuePairAuthorization() {
		return false;
	}

  	
	public abstract String getBaseURL();
	
	public abstract String getPage(int whichPage);

	public abstract String getKeyString(int key) ;

	public abstract String getElementName(int elementID);

	public  String getKey() {
		return "";
	}

}
