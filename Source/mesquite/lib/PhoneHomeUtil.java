package mesquite.lib;

import java.util.*;

import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.tolweb.base.xml.BaseXMLReader;
import org.tolweb.base.xml.BaseXMLWriter;

public class PhoneHomeUtil {	

	
	/*.................................................................................................................*/
	public static void readOldPhoneRecords(String path, ListableVector phoneRecords) {
		if (StringUtil.blank(path))
			return ;
		String oldPhoneRecords = null;
		try{
			oldPhoneRecords = MesquiteFile.getFileContentsAsString(path, -1, 100, false);
		} catch (Exception e) {
			return ;
		}
		if (StringUtil.blank(oldPhoneRecords))
			return;
		Document doc = null;
		try { doc = BaseXMLReader.getDocumentFromString(oldPhoneRecords); 
		} catch (Exception e) {
			return ;
		}

		if (doc == null || doc.getRootElement() == null) {
			return ;
		} else if (!doc.getRootElement().getName().equals("mesquite")) {
			return ;
		}
		Element messagesFromHome = doc.getRootElement().getChild("phoneRecords");
		if (messagesFromHome != null) {
			Element versionElement = messagesFromHome.getChild("version");
			if (versionElement == null || !versionElement.getText().equals("1")) {
				return ;
			}

//let's get the phone records
			List noticesFromHomeList = messagesFromHome.getChildren("record");
			for (Iterator iter = noticesFromHomeList.iterator(); iter.hasNext();) {   // this is going through all of the notices
				Element messageElement = (Element) iter.next();
				String moduleName = messageElement.getChildText("module");
				MesquiteModuleInfo mmi = MesquiteTrunk.mesquiteModulesInfoVector.findModule(MesquiteModule.class, moduleName);
				int lastVersionUsedInt = MesquiteInteger.fromString(messageElement.getChildText("lastVersionUsed"));
				int lastNotice = MesquiteInteger.fromString(messageElement.getChildText("lastNotice"));
				int lastNoticeForMyVersion = MesquiteInteger.fromString(messageElement.getChildText("lastNoticeForMyVersion"));
				if (mmi!=null && lastVersionUsedInt != mmi.getVersionInt())
					lastNoticeForMyVersion = 0;
				int lastVersionNoticed = MesquiteInteger.fromString(messageElement.getChildText("lastVersionNoticed"));
				int lastNewerVersionReported = MesquiteInteger.fromString(messageElement.getChildText("lastNewerVersionReported"));
				
				PhoneHomeRecord phoneRecord = new PhoneHomeRecord(moduleName, lastVersionUsedInt, lastNotice,  lastNoticeForMyVersion,  lastVersionNoticed, lastNewerVersionReported);
				phoneRecords.addElement(phoneRecord, false);
			}
			
		} 
	}
	/*.................................................................................................................*/
	public static void writePhoneRecords(String path, ListableVector phoneRecords) {
		if (StringUtil.blank(path))
			return ;
		Element mesquiteElement = new Element("mesquite");
		Document doc = new Document(mesquiteElement);
		Element phoneRecordElement = new Element("phoneRecords");
		mesquiteElement.addContent(phoneRecordElement);
		Element versionElement = new Element("version").addContent("1");
		phoneRecordElement.addContent(versionElement);

		for (int i= 0; i<phoneRecords.size(); i++){
			Element recordElement = new Element("record");
			phoneRecordElement.addContent(recordElement);
			PhoneHomeRecord phoneRecord = (PhoneHomeRecord)phoneRecords.elementAt(i);
			recordElement.addContent(new Element("module").addContent(new CDATA(phoneRecord.getModuleName())));
			MesquiteModuleInfo mmi = MesquiteTrunk.mesquiteModulesInfoVector.findModule(MesquiteModule.class, phoneRecord.getModuleName());
			recordElement.addContent(new Element("lastVersionUsed").addContent(MesquiteInteger.toString(phoneRecord.getLastVersionUsed())));
			recordElement.addContent(new Element("lastNotice").addContent(MesquiteInteger.toString(phoneRecord.getLastNotice())));
			recordElement.addContent(new Element("lastNoticeForMyVersion").addContent(MesquiteInteger.toString(phoneRecord.getLastNoticeForMyVersion())));
			recordElement.addContent(new Element("lastVersionNoticed").addContent(MesquiteInteger.toString(phoneRecord.getLastVersionNoticed())));
			recordElement.addContent(new Element("lastNewerVersionReported").addContent(MesquiteInteger.toString(phoneRecord.getLastNewerVersionReported())));
		}
		String xml = BaseXMLWriter.getDocumentAsString(doc);
		if (!StringUtil.blank(xml))
			MesquiteFile.putFileContents(path, xml, true);

	}



	

	
	/*.................................................................................................................*/
	public static void processSingleNotice(MesquiteModuleInfo mmi, StringBuffer notices, MesquiteInteger countNotices, int version, int notice, String noticeType, String message, PhoneHomeRecord phoneHomeRecord) {
		if (MesquiteInteger.isCombinable(version)){
			if (MesquiteInteger.isCombinable(notice)){

				//suppose Mesquite is version 2.01

				//notice assumed to have been seen before if its version number is less than current
				boolean seenBefore = version < mmi.getVersionInt();  //e.g., notice is version 2.0

				//or if Mesquite's version is same as notice's, but notice number is already seen for this version than last one noticed.
				seenBefore = seenBefore || (version ==  mmi.getVersionInt() && notice <= phoneHomeRecord.getLastNoticeForMyVersion());  //e.g., notice is 2.01; notice number has already been seen

				//or if Mesquite's version is less than notice's, and notice's is same as lastVersion noticed, but notice is already seen.
				seenBefore = seenBefore || (mmi.getVersionInt()<version && phoneHomeRecord.getLastVersionNoticed() == version && notice <= phoneHomeRecord.getLastNotice());  //e.g., notice is 2.02; 2.02 notices previously read; notice already seen

				//or if Mesquite's version is less than notice's, and notice's is less than as lastVersion noticed, but notice is already seen.
				seenBefore = seenBefore || (mmi.getVersionInt()<version && phoneHomeRecord.getLastVersionNoticed() > version);  //e.g., notice is 2.02; 2.03 notices previously read

				// otherwise assumed to have been seen before if version is same as current and notice is at or before recalled one
				if (!seenBefore){  //relevant
					if (noticeType != null && noticeType.equalsIgnoreCase("alert")){
						notices.append( countNotices.toString() + ". " + message + "\n");
						countNotices.increment();
					}
					else
						MesquiteMessage.println("NOTICE " + message);
					if (version ==  mmi.getVersionInt()){  //version of note is this version of Mesquite
						if (phoneHomeRecord.getLastNoticeForMyVersion() < notice)
							phoneHomeRecord.setLastNoticeForMyVersion(notice);
						if (version ==  phoneHomeRecord.getLastVersionNoticed() && notice >phoneHomeRecord.getLastNotice())
							phoneHomeRecord.setLastNotice(notice);
					}
					else if (version >=  phoneHomeRecord.getLastVersionNoticed()){
						phoneHomeRecord.setLastVersionNoticed(version);
						if (notice >phoneHomeRecord. getLastNotice())
							phoneHomeRecord.setLastNotice(notice);
					}

				}
			}

		} //

	}

	/*.................................................................................................................*/
	public static String retrieveMessagesFromHome(MesquiteModuleInfo mmi, PhoneHomeRecord phoneHomeRecord, StringBuffer logBuffer) {
		String url = mmi.getHomePhoneNumber();
		if (StringUtil.blank(url))
			return null;
		String noticesFromHome = null;
		try{
			noticesFromHome = MesquiteFile.getURLContentsAsString(url, -1, false);
		} catch (Exception e) {
			return null;
		}
		Document doc = null;
		if (StringUtil.blank(noticesFromHome))
			return null;
		try { doc = BaseXMLReader.getDocumentFromString(noticesFromHome); 
		} catch (Exception e) {
			return null;
		}

		if (doc == null || doc.getRootElement() == null) {
			return null;
		} else if (!doc.getRootElement().getName().equals("mesquite")) {
			return null;
		}
		Element messagesFromHome = doc.getRootElement().getChild("MessagesFromHome");
		if (messagesFromHome != null) {
			Element versionElement = messagesFromHome.getChild("version");
			if (versionElement == null || !versionElement.getText().equals("1")) { 
				return null;
			}

			StringBuffer notices = new StringBuffer();

			
			MesquiteInteger countNotices = new MesquiteInteger(1);

//let's get the notices
			List noticesFromHomeList = messagesFromHome.getChildren("notice");
			for (Iterator iter = noticesFromHomeList.iterator(); iter.hasNext();) {   // this is going through all of the notices
				Element messageElement = (Element) iter.next();
				int version = MesquiteInteger.fromString(messageElement.getChildText("forVersion"));
				int noticeNumber = MesquiteInteger.fromString(messageElement.getChildText("noticeNumber"));
				String messageType = messageElement.getChildText("messageType");
				String message = messageElement.getChildText("message");
				
				// process other notice tags here if they are present
				
				processSingleNotice(mmi, notices, countNotices, version, noticeNumber, messageType, message, phoneHomeRecord);

			}
			
// now see if there is a tag for the current release version
			Element currentReleaseVersion = messagesFromHome.getChild("currentReleaseVersion");
			if (currentReleaseVersion !=null) {
				String releaseString = "";
				String releaseStringHTML ="";
				String versionString = currentReleaseVersion.getChildText("versionString");
				String buildString = currentReleaseVersion.getChildText("build");
				String downloadURL = currentReleaseVersion.getChildText("downloadURL");
				int releaseVersionInt = MesquiteInteger.fromString(currentReleaseVersion.getChildText("version"));
				int userVersionInt = mmi.getVersionInt();
				if (mmi.getIsPackageIntro()) 
					userVersionInt = mmi.getPackageVersionInt();
				if (!StringUtil.blank(versionString))
					releaseString+="The current release version of " + mmi.getName() + " is " + versionString;
				if (!StringUtil.blank(buildString))
					releaseString+= " build " + buildString;
				if (!StringUtil.blank(releaseString)) {
					if (mmi.getIsPackageIntro()) {
						if (!StringUtil.blank(mmi.getPackageVersion()))
							releaseString+= " (the version you have installed is "+ mmi.getPackageVersion() + ").";
					}
					else if (!StringUtil.blank(mmi.getVersion()))
						releaseString+= " (the version you have installed is "+ mmi.getVersion() + ").";
					releaseStringHTML = releaseString;
					if (!StringUtil.blank(downloadURL)) {
						releaseStringHTML +=" <a href=\"" + downloadURL + "\">Download page</a>";
						releaseString+=" The latest version is downloadable at: " + downloadURL;
					}
				}

				if (MesquiteInteger.isCombinable(releaseVersionInt) && userVersionInt<releaseVersionInt) {
					if (phoneHomeRecord.getLastNewerVersionReported()<releaseVersionInt) { // we've not reported on this new version yet
						notices.append("\n" +releaseStringHTML);  // there is a newer version that has been released
						phoneHomeRecord.setLastNewerVersionReported(releaseVersionInt);
					}
					if (logBuffer!=null)
						logBuffer.append("\n" +releaseString);
				}

			}

// process other tags if they are there
			return notices.toString();
			
		} 
		return null;
	}
	
	/*.................................................................................................................*
	public static void displayMessagesFromHome(MesquiteModuleInfo module, PhoneHomeRecord phoneHomeRecord) {
					
		String notices = retrieveMessagesFromHome(module, phoneHomeRecord);
		if (!StringUtil.blank(notices)){
			module.discreetAlert("Notices from the Mesquite website:\n\n" + notices.toString() + "\n\n(You can ask Mesquite not to check its web site using the menu item in the Defaults menu of the Log window)");
		}
			
			
	}
*/

}
