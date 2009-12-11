/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison.
Version 2.72, December 2009.
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
	String baseURL;
	String pageURL;
	static final String COUNT = "count";
	
	public boolean needsKeyValuePairAuthorization() {
		return false;
	}
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
  	
  	public void removeKeyValuePair(String key){
  		for (int i=0; i<keyValuePairs.size(); i++) {
  			String[] pair= (String[])keyValuePairs.get(i);
  			if (pair!=null && pair[0].equalsIgnoreCase(key))
  				keyValuePairs.remove(i);
  		}
  	}

	public String getKeyString(int key) {
		return "";
	}

	public String getElementName(int elementID) {
		return "";
	}

  	
	public String getBaseURL() {
		return baseURL;
	}
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	public String getPageURL() {
		return pageURL;
	}
	public void setPageURL(String pageURL) {
		this.pageURL = pageURL;
	}
	
	public String getPage(int whichPage) {
		return "";
	}

	public String getKey() {
		return "";
	}

}
