package mesquite.lib;

import java.util.Map;


import org.jdom.Document;
import org.tolweb.base.http.BaseHttpRequestMaker;
import org.tolweb.base.xml.BaseXMLReader;
import org.tolweb.treegrow.main.XMLConstants;

/**
 * Class used for xml communication over http
 * 
 * Note that this class is currently NOT thread safe --
 * only one thread may use it at a time
 * @author dmandel
 *
 */
public class XMLUtilities {
	private static String checkConnectionURL = "http://google.com";
	public static String baseDatabaseURL = "http://btol.tolweb.org/onlinecontributors/app"; 	
	//public static String baseDatabaseURL = "http://zissou.cals.arizona.edu/onlinecontributors/app";	
	private static String databaseURL = baseDatabaseURL + "?page=";
	/**
	 * tapestry application name
	 */
	private static final String APP_URL = "onlinecontributors/app";
	private static final String HTTP = "http://";
	
	public static Document getDocumentFromTapestryPageName(String pageName, Map args) {
		return getDocumentFromTapestryPageName(pageName, args, false);
	}
	public static Document getDocumentFromTapestryPageNameMultipart(String pageName, Map stringArgs, 
			Map fileArgs) {
		String url = databaseURL;
		// need this here so tapestry will call the external service
		stringArgs.put("service", "external");
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
	public static Document getDocumentFromTapestryPageName(String pageName, Map args, boolean isPost) {
		Document returnDoc = null;
		args.put("service", "external");
		if (checkConnection()) {
			String url = getDatabaseURL();
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
		return BaseXMLReader.getDocumentFromString(docString);		
	}
	
	public static Document getDocumentFromFilePath(String path) {
		String docString = MesquiteFile.getFileContentsAsString(path);
		return getDocumentFromString(docString);
	}
	
	public static void outputRequestXMLError() {
		String errorMessage = "A network error has occurred, this usually means one of two things:\n(1) You don't have an internet connection" + 
			"\n(2)The server you are attempting to contact is having problems\nPlease make sure you have an internet connection and try again.";
		MesquiteMessage.warnUser(errorMessage);
	}
	
	private static boolean checkConnection() {
		/*byte[] bytes = BaseHttpRequestMaker.makeHttpRequest(checkConnectionURL);
		return bytes != null;*/
		return true;
	}
	public static String getDatabaseURL() {
		return databaseURL;
	}
	/**
	 * This needs to be called before any database communication can occur
	 * @param URL
	 */
	public static void setDatabaseURL(String URL) {
		// make sure it starts with the proper protocol
		if (!URL.startsWith(HTTP)) {
			URL = HTTP + URL; 
		}
		// check to see if it ends in a slash
		if (!URL.endsWith("/")) {
			URL += "/";
		}
		URL += APP_URL;
		baseDatabaseURL = URL;
		databaseURL = baseDatabaseURL + "?page=";
	}
	
	public static boolean getIsError(Document doc) {
		return doc == null || doc.getRootElement() == null ||
			doc.getRootElement().getName().equals(XMLConstants.ERROR);		
	}
}
