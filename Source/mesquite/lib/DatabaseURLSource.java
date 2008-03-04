package mesquite.lib;

import java.util.*;


public abstract class DatabaseURLSource extends MesquiteModule {
	Vector keyValuePairs;
	String baseURL;
	String pageURL;
	
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
