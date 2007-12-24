package mesquite.lib;

import java.util.*;
import java.io.*;

import org.dom4j.*;
import org.dom4j.io.*;

public class XMLUtil {
	
	/*.................................................................................................................*/
	public static void addFilledElement(Element containingElement, String name, String content) {
		Element element = DocumentHelper.createElement(name);
		element.addText(content);
		containingElement.add(element);
	}
	/*.................................................................................................................*/
	public static void addFilledElement(Element containingElement, String name, CDATA cdata) {
		Element element = DocumentHelper.createElement(name);
		element.add(cdata);
		containingElement.add(element);
	}

	/*.................................................................................................................*/

	public static String getDocumentAsXMLString(Document doc)
	{
		try {
			String encoding = doc.getXMLEncoding();

			if (encoding == null)
				encoding = "UTF-8";

			Writer osw = new StringWriter();
			OutputFormat opf = new OutputFormat("  ", true, encoding);
			XMLWriter writer = new XMLWriter(osw, opf);
			writer.write(doc);
			writer.close();
			return osw.toString();
		} catch (IOException e) {
			MesquiteMessage.warnProgrammer("XML Document could not be returned as string.");
		}
		return null;
	}
	/*.................................................................................................................*/
	public static Document getDocumentFromString(String rootElementName, String contents) {
		Document doc = null;
		try { 
			doc = DocumentHelper.parseText(contents); 
		} catch (Exception e) {
			return null;
		}

		if (doc == null || doc.getRootElement() == null) {
			return  null;
		} else if (!StringUtil.blank(rootElementName) && !doc.getRootElement().getName().equals(rootElementName)) {
			return null;
		}
		return doc;
	}
	/*.................................................................................................................*/
	public static Document getDocumentFromString(String contents) {
		return getDocumentFromString("",contents);
	}
	/*.................................................................................................................*/
	public static Element getRootXMLElementFromString(String rootElementName, String contents) {
		Document doc = getDocumentFromString(rootElementName, contents);
		if (doc==null)
			return null;
		return doc.getRootElement();
	}
	/*.................................................................................................................*/
	public static Element getRootXMLElementFromString(String contents) {
		return getRootXMLElementFromString("",contents);
	}
	/*.................................................................................................................*/
	public static Element getRootXMLElementFromURL(String rootElementName, String url) {
		SAXReader saxReader = new SAXReader();
		Document doc = null;
		try { 
			doc = saxReader.read(url); 
		} catch (Exception e) {
			return null;
		}

		if (doc == null || doc.getRootElement() == null) {
			return  null;
		} else if (!StringUtil.blank(rootElementName) && !doc.getRootElement().getName().equals(rootElementName)) {
			return null;
		}
		Element root = doc.getRootElement();
		return root;
	}
	/*.................................................................................................................*/
	public static Element getRootXMLElementFromURL(String url) {
		return getRootXMLElementFromString("",url);
	}

	/*.................................................................................................................*/
	public static void readXMLPreferences(MesquiteModule module, String contents) {
		Element root = getRootXMLElementFromString("mesquite",contents);
		if (root==null)
			return;
		Element element = root.element(module.getXMLModuleName());
		if (element != null) {
			Element versionElement = element.element("version");
			if (versionElement == null)
				return ;
			else {
				int version = MesquiteInteger.fromString(element.elementText("version"));
				boolean acceptableVersion = (module.getXMLPrefsVersion()==version || !module.xmlPrefsVersionMustMatch());
				if (acceptableVersion) 
					processPreferencesFromXML(module, element);
				else
					return;
			}
		} 
	}
	
	/*.................................................................................................................*/
	public static void processPreferencesFromXML ( MesquiteModule module, Element element) {

		List prefElement = element.elements();
		for (Iterator iter = prefElement.iterator(); iter.hasNext();) {   // this is going through all of the notices
			Element messageElement = (Element) iter.next();
			module.processSingleXMLPreference(messageElement.getName(), messageElement.getText());
		}
		
	}


}
