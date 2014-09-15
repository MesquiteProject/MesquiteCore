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

import java.util.Iterator;
import java.util.List;

import org.apache.hivemind.util.PropertyUtils;
import org.dom4j.*;


/**
 * MesquiteModule subclass that automatically handles writing preferences to
 * xml.  Simply override getPreferencePropertyNames to return the names of
 * the properties one is interested in and they will automatically get
 * serialized and de-serialized
 * @author dmandel
 *
 */
public abstract class MesquiteXMLPreferencesModule extends MesquiteModule implements PropertyNamesProvider {
	private static final String VERSION = "version";
	private static final String PREFERENCE = "preference";
	private static final String KEY = "key";
	
	/**
	 * Ideally this should not be static but since we don't always have control over the superclass,
	 * make it available for classes that might otherwise not be able to use it due to inheritance
	 * constraints
	 * @param provider
	 * @param versionInt
	 * @return
	 */
	public static String preparePreferencesForXML (PropertyNamesProvider provider, int versionInt) {
		String[] propertyNames = provider.getPreferencePropertyNames();
		Element rootElement = DocumentHelper.createElement("mesquite");
		Document preferencesDoc = DocumentHelper.createDocument(rootElement);
		preferencesDoc.setRootElement(rootElement);
		Element classElement = DocumentHelper.createElement(getShortClassName(provider.getClass()));
		rootElement.add(classElement);
		Element versionElement = DocumentHelper.createElement(VERSION);
		versionElement.setText("" + versionInt);
		classElement.add(versionElement);
		for (int i = 0; i < propertyNames.length; i++) {
			String currentPropertyName = propertyNames[i];
			try {
				Object prefsContent = PropertyUtils.read(provider, currentPropertyName);
				if (prefsContent != null) {
					Element nextPrefsElement = DocumentHelper.createElement(PREFERENCE);
					nextPrefsElement.addAttribute(KEY, currentPropertyName);
					nextPrefsElement.add(DocumentHelper.createCDATA(prefsContent.toString()));
					classElement.add(nextPrefsElement);
				}
			} catch (Exception e) {
				MesquiteMessage.warnProgrammer("Could not read property value " + currentPropertyName + " for writing xml preferences on module: " + provider);
			}
			//nextPrefsElement.addContent(new CDATA())
		}
		return XMLUtil.getDocumentAsXMLString(preferencesDoc);
	}
	/**
	 * delegate to the static method
	 */
	protected boolean parseFullXMLDocument(String prefsXML) {
		return parseFullXMLDocument(prefsXML, this, getVersionInt(), xmlPrefsVersionMustMatch());
	}
	/**
	 * Ideally this should not be static but since we don't always have control over the superclass,
	 * make it available for classes that might otherwise not be able to use it due to inheritance
	 * constraints
	 * @param prefsXML
	 * @param provider
	 * @param version
	 * @param versionsMustMatch
	 * @return
	 */
	public static boolean parseFullXMLDocument(String prefsXML, PropertyNamesProvider provider, 
			int version, boolean versionsMustMatch) {
		Document doc = XMLUtil.getDocumentFromString(prefsXML);
		if (doc == null) {
			// not xml -- can't parse it
			return false;
		}
		Element rootElement = doc.getRootElement();
		String shortClassName = getShortClassName(provider.getClass());
		Element classElement = rootElement.element(shortClassName);
		if (classElement != null) {
			String versionString = classElement.elementText(VERSION);
			int versionInXml = MesquiteInteger.fromString(versionString);
			boolean acceptableVersion = (versionInXml == version || !versionsMustMatch);
			if (isCorrectRootTag(classElement.getName(), provider.getClass()) && acceptableVersion) {
				List prefsChildren = classElement.elements(PREFERENCE);
				for (Iterator iter = prefsChildren.iterator(); iter.hasNext();) {
					Element nextPreferenceElement = (Element) iter.next();
					String key = nextPreferenceElement.attributeValue(KEY);
					String value = nextPreferenceElement.getText();
					try {
						PropertyUtils.smartWrite(provider, key, value);
					} catch (Exception e) {
						MesquiteMessage.warnProgrammer("Could not write property value " + key + " for loading xml preferences on module: " + provider);
					}
				}
				return true;			
			}
		}
		return false;
	}
	/**
	 * delegate to the static method
	 * @param versionInt
	 * @return
	 */
	public String preparePreferencesForXML() {
		return MesquiteXMLPreferencesModule.preparePreferencesForXML(this, getVersionInt());
	}
}
