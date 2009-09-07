/* Mesquite source code.  Copyright 1997-2009 W. Maddison and D. Maddison. 
Version 2.71, September 2009.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
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
