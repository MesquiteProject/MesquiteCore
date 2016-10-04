/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


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
import java.io.*;

import org.dom4j.*;
import org.dom4j.io.*;

public class XMLUtil {
	
	public static final String FLAVOR = "flavor";
	
	/*.................................................................................................................*/
	public static Element addFilledElement(Element containingElement, String name, String content) {
		if (content == null || name == null)
			return null;
		Element element = DocumentHelper.createElement(name);
		element.addText(content);
		containingElement.add(element);
		return element;
	}
	/*.................................................................................................................*/
	public static Element addFilledElement(Element containingElement, String name, CDATA cdata) {
		if (cdata == null || name == null)
			return null;
		Element element = DocumentHelper.createElement(name);
		element.add(cdata);
		containingElement.add(element);
		return element;
	}
	public static String getTextFromElement(Element containingElement, String name){
		Element e = containingElement.element(name);
		if (e == null)
			return null;
		else return e.getText();
	}
	/*.................................................................................................................*/

	public static String getDocumentAsXMLString(Document doc, boolean escapeText)
	{
		try {
			String encoding = doc.getXMLEncoding();

			if (encoding == null)
				encoding = "UTF-8";

			Writer osw = new StringWriter();
			OutputFormat opf = new OutputFormat("  ", true, encoding);
			XMLWriter writer = new XMLWriter(osw, opf);
			writer.setEscapeText(escapeText);
			writer.write(doc);
			writer.close();
			return osw.toString();
		} catch (IOException e) {
			MesquiteMessage.warnProgrammer("XML Document could not be returned as string.");
		}
		return null;
	}
	/*.................................................................................................................*/

	public static String getElementAsXMLString(Element doc, String encoding, boolean escapeText)
	{
		try {

			Writer osw = new StringWriter();
			OutputFormat opf = new OutputFormat("  ", true, encoding);
			XMLWriter writer = new XMLWriter(osw, opf);
			writer.setEscapeText(escapeText);
			writer.write(doc);
			writer.close();
			return osw.toString();
		} catch (IOException e) {
			MesquiteMessage.warnProgrammer("XML Document could not be returned as string.");
		}
		return null;
	}
	/*.................................................................................................................*/

	public static String getDocumentAsXMLString(Document doc) {
		return getDocumentAsXMLString(doc,true);
	}

	/*.................................................................................................................*/

	public static String getDocumentAsXMLString2(Document doc)
	{
		try {
			String encoding = doc.getXMLEncoding();

			//if (encoding == null)
			//	encoding = "UTF-8";

			Writer osw = new StringWriter();
			OutputFormat opf = new OutputFormat("  ", true);
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
	public static String stripSchema(String contents) {
		if (contents==null)
			return null;
		int pos = contents.indexOf("<!DOCTYPE");
		if (pos>=0) {
			int pos2 = contents.indexOf(">", pos+1);
			String newVersion = contents.substring(0, pos) + contents.substring(pos2+1,contents.length());
			return newVersion;
		}
		return contents;
	}
	/*.................................................................................................................*/
	public static Document getDocumentFromString(String rootElementName, String contents,boolean stripSchema) {
		if (stripSchema)
			contents = stripSchema(contents);
		Document doc = null;
		try { 
			doc = DocumentHelper.parseText(contents);   //WARNING: this can't handle UTF-8 and other encodings.  Thus, accented characters are disallowed.  Use SAXReader instead
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
	public static Document getDocumentFromString(String rootElementName, String contents) {
		return getDocumentFromString(rootElementName,contents, true);
	}
	/*.................................................................................................................*/
	public static Document getDocumentFromString(String contents) {
		return getDocumentFromString("",contents, true);
	}
	/*.................................................................................................................*/
	public static Element getRootXMLElementFromString(String rootElementName, String contents, boolean stripSchema) {
		Document doc = getDocumentFromString(rootElementName, contents, stripSchema);
		if (doc==null)
			return null;
		return doc.getRootElement();
	}
	/*.................................................................................................................*/
	public static Element getRootXMLElementFromString(String rootElementName, String contents) {
		return getRootXMLElementFromString(rootElementName,contents, true);
	}
	/*.................................................................................................................*/
	public static Element getRootXMLElementFromString(String contents) {
		return getRootXMLElementFromString("",contents, true);
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
	public static Element getRootXMLElementFromFile(String rootElementName, String path) {
		SAXReader saxReader = new SAXReader();
		Document doc = null;
		try { 
			doc = saxReader.read(new File(path)); 
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
		return getRootXMLElementFromURL("",url);
	}

	/*.................................................................................................................*/
	public static void readXMLPreferences(MesquiteModule module, XMLPreferencesProcessor xmlPrefProcessor, String contents) {
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
					processPreferencesFromXML(xmlPrefProcessor, element);
				else
					return;
			}
		} 
	}
	/*.................................................................................................................*/
	public static void readXMLPreferencesFromFile (MesquiteModule module, XMLPreferencesProcessor xmlPrefProcessor, String path) {
		Element root = getRootXMLElementFromFile("mesquite",path);
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
					processPreferencesFromXML(xmlPrefProcessor, element);
				else
					return;
			}
		} 
	}
	
	/*.................................................................................................................*/
	public static void processPreferencesFromXML ( XMLPreferencesProcessor xmlPrefProcessor, Element element) {

		List prefElement = element.elements();
		for (Iterator iter = prefElement.iterator(); iter.hasNext();) {   // this is going through all of the notices
			Element messageElement = (Element) iter.next();
			String flavor = messageElement.attributeValue(FLAVOR);
			if (StringUtil.notEmpty(flavor))
				xmlPrefProcessor.processSingleXMLPreference(messageElement.getName(), flavor, messageElement.getText());
			else
				xmlPrefProcessor.processSingleXMLPreference(messageElement.getName(), messageElement.getText());
		}
		
	}


}
