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


import java.io.IOException;
import java.util.Vector;

import org.apache.commons.httpclient.NameValuePair;

import mesquite.lib.*;
import mesquite.lib.ui.AlertDialog;
import mesquite.tol.lib.BaseHttpRequestMaker;

/* ======================================================================== */
public class PhoneHomeThread extends Thread {

	/*
	 * 
	 * The following are in MesquiteModule:
	public static String versionReportURL =  "http://startup.mesquiteproject.org/mesquite/mesquiteStartup.php"; //(see PhoneHomeThread, checkForMessagesFromAllHomes)
	public static String errorReportURL =  "http://error.mesquiteproject.org/mesquite/mesquiteError.php"; //see exceptionAlert in MesquiteModule
	public static String prereleaseErrorReportURL =  "http://error.mesquiteproject.org/mesquite/mesquitePrereleaseError.php"; //see exceptionAlert in MesquiteModule
	public static String beansReportURL = "http://beans.mesquiteproject.org/mesquite/mesquiteBeans.php";
	
	//See Mesquite.java for notices.xml URLs
	//See Installer for updates.xml URLs

	 * */
	Vector beans = new Vector();
	Vector errorReports = new Vector();
	public PhoneHomeThread () {
		setPriority(Thread.MIN_PRIORITY);
	}
	public void run() {
		/*NOTICES =====Checking website to see if there are any notices or updates*/
		//Put here on a separate thread so Mesquite doesn't hang if website is unavailable
		checkForMessagesFromAllHomes();

		while (!MesquiteTrunk.mesquiteExiting) { 
			try {
				Thread.sleep(1000);
				if (beans.size()>0){
					NameValuePair[] b = (NameValuePair[])beans.elementAt(0);
					beans.removeElement(b);
					BaseHttpRequestMaker.sendInfoToServer(b, MesquiteModule.beansReportURL, null, 0);
				}
				if (errorReports.size()>0){
					String report = (String)errorReports.elementAt(0);
					errorReports.removeElement(report);
					StringBuffer response = new StringBuffer();
					String url = MesquiteModule.errorReportURL;
					if (MesquiteTrunk.mesquiteTrunk.isPrerelease())
						url = MesquiteModule.prereleaseErrorReportURL;
					if (BaseHttpRequestMaker.contactServer("", report, url, response)){
					String r = response.toString();
					if (r == null || (r.indexOf("mq4v")<0 && r.indexOf("Thank")<0))
						MesquiteMessage.println("Sorry, Mesquite was unable to communicate properly with the server to send the report.");
					else
						MesquiteMessage.println("Error reported to home Mesquite server.");
						
				}
				else
					MesquiteMessage.println("Sorry, Mesquite was unable to communicate properly with the server to send the report.");
				}
			}
			catch (Throwable e){
			}
		}
	}
	 
	String contactMessage = "";
	public void setContactMessage(String contactMessage) {
		this.contactMessage = contactMessage;
	}
	
	public void postBean(NameValuePair[] pairs){
		beans.addElement(pairs);
	}
	public void recordError(String s){
		errorReports.addElement(s);
	}
	/*.................................................................................................................*/
	public void checkForMessagesFromAllHomes(){
		//MesquiteTrunk.incrementMenuResetSuppression();

		try {
			if (!MesquiteTrunk.suppressVersionReporting){ 
				StringBuffer response = new StringBuffer();
				String url = MesquiteModule.versionReportURL;
				if (MesquiteTrunk.developmentMode)
					url = MesquiteModule.devVersionReportURL;
				BaseHttpRequestMaker.contactServer(contactMessage, "", url, response);
				String r = response.toString();
				System.err.println(r);
				//if mqrv or Feedback included in response, then this is real response
				if (!StringUtil.blank(r) && (r.indexOf("mq4v")>=0 || r.indexOf("Version")>=0)){
					PhoneHomeUtil.phoneHomeSuccessful = true;
					MesquiteTrunk.mesquiteTrunk.logln("Mesquite server was contacted to log your version and check for notices. To turn this off, choose \"Contact Mesquite Server on Startup\" in File>Defaults> submenu.");
					if (MesquiteTrunk.mesquiteTrunk.isPrerelease())
						MesquiteTrunk.mesquiteTrunk.logln("Because this is a prerelease version, any crashes will be reported automatically to Mesquite's server. "
								+"None of your data file(s) will be sent, just your basic system information and the point in Mesquite's code where the crash happened.");
					if (r.indexOf("mq4rsshow")>=0){  //show dialog at startup!!!!
						AlertDialog.noticeHTML(MesquiteTrunk.mesquiteTrunk.containerOfModule(),"Note", r, 600, 400, null);
					}
				}
				else { 
					if (MesquiteTrunk.debugMode)
					MesquiteMessage.warnProgrammer("no response or incorrect response from server on startup");
				}
				response.setLength(0);

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
					if (MesquiteTrunk.debugMode){
						MesquiteTrunk.mesquiteTrunk.logln("Checking server for notices regarding " + mmi.getPackageName());
					}
					
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



