/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.trunk;


import mesquite.lib.*;
import mesquite.tol.lib.BaseHttpRequestMaker;

/* ======================================================================== */
public class PhoneHomeThread extends Thread {

	public PhoneHomeThread () {
		setPriority(Thread.MIN_PRIORITY);
	}
	public void run() {
		/*NOTICES =====Checking website to see if there are any notices or updates*/
		//Put here on a separate thread so Mesquite doesn't hang if website is unavailable
		checkForMessagesFromAllHomes();


	}

	/*.................................................................................................................*/
	public void checkForMessagesFromAllHomes(){
		//MesquiteTrunk.incrementMenuResetSuppression();

		try {
			if (!MesquiteTrunk.suppressVersionReporting){
				StringBuffer response = new StringBuffer();
				String buildNum = Integer.toString(MesquiteTrunk.getBuildNumber());
				if (MesquiteTrunk.mesquiteTrunk.isPrerelease())
					buildNum = "PreRelease-" + buildNum;
				BaseHttpRequestMaker.contactServer(buildNum, MesquiteModule.versionReportURL, response);
				String r = response.toString();
			//if mq3rs is included in response, then this is real response
				if (!StringUtil.blank(r) && r.indexOf("mq3rs")>=0){
					if (r.indexOf("mq3rsshow")>=0){  //show dialog at startup!!!!
						AlertDialog.noticeHTML(MesquiteTrunk.mesquiteTrunk.containerOfModule(),"Note", r, 600, 400, null);
					}
				}
				else if (MesquiteTrunk.debugMode)
					MesquiteMessage.warnProgrammer("no response or incorrect response from server on startup");
			}
		}
		catch (Throwable t){
			if (MesquiteTrunk.debugMode)
				MesquiteMessage.warnProgrammer("PROBLEM PHONING HOME to report version\n" + t.getCause());
		}
		ListableVector phoneRecords = new ListableVector();
		StringBuffer notices = new StringBuffer();
		StringBuffer logBuffer = new StringBuffer();
		String path  = MesquiteModule.prefsDirectory+ MesquiteFile.fileSeparator+ "phoneRecords.xml";
		PhoneHomeUtil.readOldPhoneRecords(path, phoneRecords);

		for (int i= 0; i<MesquiteTrunk.mesquiteModulesInfoVector.size(); i++){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)MesquiteTrunk.mesquiteModulesInfoVector.elementAt(i);
			if (!StringUtil.blank(mmi.getHomePhoneNumber())) {
				try {
					int rec = phoneRecords.indexOfByName("#" + mmi.getClassName()); 
					PhoneHomeRecord phoneHomeRecord;
					if (!MesquiteInteger.isCombinable(rec) || rec<0) {// this module is not the phone records 
						phoneHomeRecord = new PhoneHomeRecord("#"+mmi.getClassName());
						phoneRecords.addElement(phoneHomeRecord, false);
					}
					else
						phoneHomeRecord = (PhoneHomeRecord)phoneRecords.elementAt(rec);
					String notice = PhoneHomeUtil.retrieveMessagesFromHome(mmi, phoneHomeRecord, logBuffer);
					
					phoneHomeRecord.setCurrentValues(mmi);
					if (!StringUtil.blank(notice)) {
						if (mmi.getModuleClass() == mesquite.Mesquite.class)
							notices.append("<h3>From Mesquite</h3>");
						else if (!StringUtil.blank(mmi.getPackageName()))
							notices.append("<h3>From " + mmi.getPackageName() + "</h3>");
						else
							notices.append("<h3>From " + mmi.getName() + "</h3>");
						notices.append(notice);
						//notices.append("<hr>");
					}
				}
				catch (Throwable t){
				}
			}
		}
		if (!StringUtil.blank(logBuffer.toString())){
			MesquiteTrunk.mesquiteTrunk.logln("\n*************************" + logBuffer.toString() + "\n*************************\n");
		}

		if (!StringUtil.blank(notices)){
			String note = ("<h2>Notices from the websites of Mesquite and installed packages</h2><hr>" + notices.toString() + "<br><h4>(You can ask Mesquite not to check for messages on its websites using the menu item in the Defaults submenu of the File menu)</h4>");
			if (!MesquiteThread.isScripting()){
				AlertDialog.noticeHTML(MesquiteTrunk.mesquiteTrunk.containerOfModule(),"Note", note, 600, 400, PhoneHomeUtil.getPhoneHomeDialogLinkCommand(), true);
			}
			else
				System.out.println(note);
		}
		if (phoneRecords.size()>0)
			PhoneHomeUtil.writePhoneRecords(path, phoneRecords);
		MesquiteTrunk.mesquiteTrunk.storePreferences();
		MesquiteTrunk.resetAllMenuBars();

		//	MesquiteTrunk.decrementMenuResetSuppression();
	}

}



