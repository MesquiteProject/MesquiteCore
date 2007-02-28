package mesquite.lib;

import java.util.Iterator;
import java.util.List;

import org.apache.hivemind.util.PropertyUtils;
import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.tolweb.base.xml.BaseXMLReader;
import org.tolweb.base.xml.BaseXMLWriter;


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
		Document preferencesDoc = new Document();
		Element rootElement = new Element("mesquite");
		preferencesDoc.setRootElement(rootElement);
		Element classElement = new Element(getShortClassName(provider.getClass()));
		rootElement.addContent(classElement);
		Element versionElement = new Element(VERSION);
		versionElement.setText("" + versionInt);
		classElement.addContent(versionElement);
		for (int i = 0; i < propertyNames.length; i++) {
			String currentPropertyName = propertyNames[i];
			try {
				Object prefsContent = PropertyUtils.read(provider, currentPropertyName);
				if (prefsContent != null) {
					Element nextPrefsElement = new Element(PREFERENCE);
					nextPrefsElement.setAttribute(KEY, currentPropertyName);
					nextPrefsElement.addContent(new CDATA(prefsContent.toString()));
					classElement.addContent(nextPrefsElement);
				}
			} catch (Exception e) {
				MesquiteMessage.warnProgrammer("Could not read property value " + currentPropertyName + " for writing xml preferences on module: " + provider);
			}
			//nextPrefsElement.addContent(new CDATA())
		}
		return BaseXMLWriter.getDocumentAsString(preferencesDoc);
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
		Document doc = BaseXMLReader.getDocumentFromString(prefsXML);
		if (doc == null) {
			// not xml -- can't parse it
			return false;
		}
		Element rootElement = doc.getRootElement();
		String shortClassName = getShortClassName(provider.getClass());
		Element classElement = rootElement.getChild(shortClassName);
		if (classElement != null) {
			String versionString = classElement.getChildText(VERSION);
			int versionInXml = MesquiteInteger.fromString(versionString);
			boolean acceptableVersion = (versionInXml == version || !versionsMustMatch);
			if (isCorrectRootTag(classElement.getName(), provider.getClass()) && acceptableVersion) {
				List prefsChildren = classElement.getChildren(PREFERENCE);
				for (Iterator iter = prefsChildren.iterator(); iter.hasNext();) {
					Element nextPreferenceElement = (Element) iter.next();
					String key = nextPreferenceElement.getAttributeValue(KEY);
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
