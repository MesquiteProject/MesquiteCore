/* Mesquite source code.  Copyright 1997-2011 W. Maddison and D. Maddison.
Version 2.75, September 2011.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.util.Map;


import org.dom4j.*;
import mesquite.tol.lib.*;

import mesquite.tol.lib.XMLConstants;

/**
 * Class used for xml communication over http
 * 
 * Note that this class is currently NOT thread safe --
 * only one thread may use it at a time
 * @author dmandel
 * 
 * 25 Feb 08:  made it thread safe by removing dependency on static Strings - DRM
 *
 */
public class MesquiteXMLUtilities {
	private static String checkConnectionURL = "http://google.com";
	//public static String baseDatabaseURL = "http://btol.tolweb.org/onlinecontributors/app"; 	
	//public static String baseDatabaseURL = "http://zissou.cals.arizona.edu/onlinecontributors/app";	
	//private static String databaseURL = baseDatabaseURL + "?page=";
	/**
	 * tapestry application name
	 */
	protected static final String HTTP = "http://";
	
	public static Document getDocumentFromTapestryPageName(String url, String pageName, Map args) {
		return getDocumentFromTapestryPageName(url, pageName, args, false);
	}
	public static Document getDocumentFromTapestryPageNameMultipart(String databaseURL,String pageName, Map stringArgs, 
			Map fileArgs) {
		String url = databaseURL;
		// need this here so tapestry will call the external service
		stringArgs.put("page", pageName);
		try {
			Document returnDoc = BaseHttpRequestMaker.getTap4ExternalUrlDocumentMultipart(url, pageName, 
		    		stringArgs, fileArgs);
			return returnDoc;
		} catch (Exception e) {
			e.printStackTrace();
			// error in communication, likely a dead connection on one end or the other
			return null;
		}
	}
	public static Document getDocumentFromTapestryPageName(String url, String pageName, Map args, boolean isPost) {
		Document returnDoc = null;
		args.put("service", "external");
		if (checkConnection()) {
			//String url = getDatabaseURL();
			try {
				returnDoc = BaseHttpRequestMaker.getTap4ExternalUrlDocument(url, 
						pageName, args, isPost);
			} catch (Exception e) {
				// communication error, allow doc to remain null
			}
			if (returnDoc == null || returnDoc.getRootElement() == null) {
				returnDoc = null;
			}
		} else {
			String error = "You don't appear to have an internet connection.  This operation cannot complete without a connection";
			MesquiteMessage.println(error);
		}
		return returnDoc;
	}
	
	public static Document getDocumentFromString(String docString) {
		return XMLUtil.getDocumentFromString(docString);		
	}
	
	public static Document getDocumentFromFilePath(String path) {
		String docString = MesquiteFile.getFileContentsAsString(path);
		return getDocumentFromString(docString);
	}
	
	public static void outputRequestXMLError() {
		String errorMessage = "A network error has occurred, this usually means one of three things:\n(1) You don't have an internet connection" + 
		"\n(2) The server you are attempting to contact is having problems"+
		"\n(3) The information sent from the server is not formatted in a way Mesquite can understand."+
			"\nPlease make sure you have an internet connection and try again.";
		MesquiteMessage.warnUser(errorMessage);
	}
	
	private static boolean checkConnection() {
		/*byte[] bytes = BaseHttpRequestMaker.makeHttpRequest(checkConnectionURL);
		return bytes != null;*/
		return true;
	}
	/*
	public static String getDatabaseURL() {
		return databaseURL;
	}
	/**
	 * This needs to be called before any database communication can occur
	 * @param URL
	 */
	

	
	
	public static boolean getIsError(Document doc) {
		return doc == null || doc.getRootElement() == null ||
			doc.getRootElement().getName().equals(XMLConstants.ERROR);		
	}
}
