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

import java.util.Vector;

import org.apache.commons.httpclient.NameValuePair;

import mesquite.lib.AlertDialog;
import mesquite.lib.ListableVector;
import mesquite.lib.MesquiteFile;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteMessage;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteModuleInfo;
import mesquite.lib.MesquiteThread;
import mesquite.lib.MesquiteTrunk;
import mesquite.lib.PhoneHomeRecord;
import mesquite.lib.PhoneHomeUtil;
import mesquite.lib.StringUtil;
import mesquite.tol.lib.BaseHttpRequestMaker;

/**
 * Phone Home to mesquite server. At thread startup, PhoneHomeThread will report
 * the mesquite version to the Mesquite server. It will then proceed to query
 * the mesquite server for any information about the installed module.
 * 
 * After the initial startup, Phone Home thread will attempt to post any "beans"
 * to the mesquite server every ten seconds
 */
public class PhoneHomeThread extends Thread {
	private Vector<NameValuePair[]> beans = new Vector<NameValuePair[]>();

	public PhoneHomeThread() {
		setPriority(Thread.MIN_PRIORITY);
	}

	@Override
	public void run() {
		/*
		 * TODO Make this call non-blocking so the phone home thread does not hang if
		 * website is unavailable
		 */
		checkForMessagesFromAllHomes();

		// Report beans to the Mesqutie server
		while (!MesquiteTrunk.mesquiteExiting) {
			try {
				Thread.sleep(1000);
				if (beans.size() > 0) {
					BaseHttpRequestMaker.sendInfoToServer(beans.elementAt(0), MesquiteModule.beansReportURL, null, 0);
					beans.removeElementAt(0);
				}
			} catch (Throwable e) { // TODO Catch and handle
			}
		}
	}

	public void postBean(NameValuePair[] pairs) {
		beans.addElement(pairs);
	}

	/**
	 * Reports version to Mesquite server and checks for information mesquite about
	 * installed modules
	 */
	public void checkForMessagesFromAllHomes() {
		// Report Version to server
		try {
			if (!MesquiteTrunk.suppressVersionReporting) {
				StringBuffer response = new StringBuffer();
				String buildNum = Integer.toString(MesquiteTrunk.getBuildNumber());
				if (MesquiteTrunk.mesquiteTrunk.isPrerelease())
					buildNum = "PreRelease-" + buildNum;
				BaseHttpRequestMaker.contactServer(buildNum, MesquiteModule.versionReportURL, response);
				String r = response.toString();
				if (!StringUtil.blank(r) && r.indexOf("mq3rs") >= 0) {
					if (r.indexOf("mq3rsshow") >= 0) { // show dialog at startup!!!!
						AlertDialog.noticeHTML(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Note", r, 600, 400,
								null);
					}
				} else if (MesquiteTrunk.debugMode)
					MesquiteMessage.warnProgrammer("no response or incorrect response from server on startup");
			}
		} catch (Throwable t) { // TODO catch and handle
			if (MesquiteTrunk.debugMode)
				MesquiteMessage.warnProgrammer("PROBLEM PHONING HOME to report version\n" + t.getCause());
		}

		// Check Server for notice regarding the mesquite and the various installed
		// modules
		ListableVector phoneRecords = new ListableVector();
		StringBuffer notices = new StringBuffer();
		StringBuffer logBuffer = new StringBuffer();
		String path = MesquiteModule.prefsDirectory + MesquiteFile.fileSeparator + "phoneRecords.xml";
		PhoneHomeUtil.readOldPhoneRecords(path, phoneRecords);
		for (int i = 0; i < MesquiteTrunk.mesquiteModulesInfoVector.size(); i++) {
			MesquiteModuleInfo mmi = (MesquiteModuleInfo) MesquiteTrunk.mesquiteModulesInfoVector.elementAt(i);
			if (StringUtil.blank(mmi.getHomePhoneNumber())) {
				continue;
			}
			try {
				int rec = phoneRecords.indexOfByName("#" + mmi.getClassName());
				if (MesquiteTrunk.debugMode) {
					MesquiteTrunk.mesquiteTrunk.logln("Checking server for notices regarding " + mmi.getPackageName());
				}

				PhoneHomeRecord phoneHomeRecord;
				if (!MesquiteInteger.isCombinable(rec) || rec < 0) {// this module is not the phone records
					phoneHomeRecord = new PhoneHomeRecord("#" + mmi.getClassName());
					phoneRecords.addElement(phoneHomeRecord, false);
				} else
					phoneHomeRecord = (PhoneHomeRecord) phoneRecords.elementAt(rec);
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
				}
			} catch (Throwable t) { // TODO catch and handle errors explicitly
			}
		}

		// Print Notices to console
		if (!StringUtil.blank(logBuffer.toString())) {
			MesquiteTrunk.mesquiteTrunk
					.logln("\n*************************" + logBuffer.toString() + "\n*************************\n");
		}
		if (!StringUtil.blank(notices)) {
			String note = ("<h2>Notices from the websites of Mesquite and installed packages</h2><hr>"
					+ notices.toString()
					+ "<br><h4>(You can ask Mesquite not to check for messages on its websites using the menu item in the Defaults submenu of the File menu)</h4>");
			if (!MesquiteThread.isScripting()) {
				AlertDialog.noticeHTML(MesquiteTrunk.mesquiteTrunk.containerOfModule(), "Note", note, 600, 400,
						PhoneHomeUtil.getPhoneHomeDialogLinkCommand(), true);
			} else
				System.out.println(note);
		}
		if (phoneRecords.size() > 0)
			PhoneHomeUtil.writePhoneRecords(path, phoneRecords);
		MesquiteTrunk.mesquiteTrunk.storePreferences();
		MesquiteTrunk.resetAllMenuBars();
	}
}
