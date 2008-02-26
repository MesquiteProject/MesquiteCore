package mesquite.tol.lib;

import mesquite.lib.MesquiteXMLUtilities;

public class MesquiteXMLToLUtilities extends MesquiteXMLUtilities {
	private static final String APP_URL = "onlinecontributors/app";

	public static String getTOLBaseDatabaseURL(String URL) {
		// make sure it starts with the proper protocol
		if (!URL.startsWith(HTTP)) {
			URL = HTTP + URL; 
		}
		// check to see if it ends in a slash
		if (!URL.endsWith("/")) {
			URL += "/";
		}
		URL += APP_URL;
		//baseDatabaseURL = URL;
		//databaseURL = baseDatabaseURL + "?page=";
		return URL ;
	}
	public static String getTOLPageDatabaseURL(String URL) {
		// make sure it starts with the proper protocol
		if (!URL.startsWith(HTTP)) {
			URL = HTTP + URL; 
		}
		// check to see if it ends in a slash
		if (!URL.endsWith("/")) {
			URL += "/";
		}
		URL += APP_URL;
		//baseDatabaseURL = URL;
		//databaseURL = baseDatabaseURL + "?page=";
		return URL + "?page=";
	}

}
