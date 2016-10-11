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

import java.awt.*;
import java.util.*;
import java.io.*;

import mesquite.lib.duties.*;

/* ======================================================================== */
/** This is a superclass of MesquiteModule that handles the employee-employer relations. It is intended to be used only as a superclass of MesquiteModule */
public abstract class EmployerEmployee extends MenuOwner implements HNode, Listable {
	/** The Class the module was hired as; i.e., the duty is was hired to fulfill */
	Class hiredAs = null;


	/** Stored here by the employer so that Mesquite can use it to hire a replacement if this module quits */
	MesquiteCommand hiringCommand = null;

	/** The condition under which this module was hired */
	Object hiringCondition = null;

	/** The employee tasks (employee MesquiteModule objects) of this task (module) */
	protected EmployeeVector employees = null; // TODO: make private and force MesquiteModule to use method????

	/** The employer task (i.e., the MesquiteModule that hired this module) */
	protected MesquiteModule employer = null;

	/** Suppresses the dialogs to choose alternative employees on hiring; acts as if */
	public static boolean suppressHireQueries = false;

	/** Divide menu choices into primary and secondary */
	public static boolean useOtherChoices = false;

	/** Whether secondary choices are shown by default in dialog boxes */
	public static boolean secondaryChoicesOnInDialogs = true;

	/** Whether subchoices are shown in module choice dialog boxes */
	public static boolean subChoicesOnInDialogs = true;

	private static boolean showHiringPath = false;

	private boolean fixedPriority = false;

	private String explanationByWhichHired;

	boolean inStartup = false;

	protected boolean startupBailOut = false; // used if startup process cancelled, so that "sorry" and attempted rehires don't appear

	Vector employeeNeedsVector; // a vector of registered employee needs, used for documentation/searches of how to do analyses, but perhaps eventually also used in hiring

	static MesquiteTimer moduleStartupTime;

	public static int totalCreated = 0;

	public static int totalDisposed = 0;

	protected MesquiteInteger stringPos = new MesquiteInteger(0);
	static {
		moduleStartupTime = new MesquiteTimer();
	}
	public EmployerEmployee() {
		totalCreated++;
	}
	public boolean isInStartup(){
		return inStartup;
	}
	public void setEmployeesInStartup(boolean i){
		setInStartup(i);
		if (employees == null)
			return;
		for (int k = 0; k< employees.size(); k++){
			MesquiteModule mb = (MesquiteModule)employees.elementAt(k);
			mb.setEmployeesInStartup(i);
		}
	}
	public void setInStartup(boolean i){
		inStartup = i;
	}
	public static void setShowHiringPath(boolean show) {
		showHiringPath = show;
	}

	protected Vector getEmployeeNeedsVector() { // This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		return employeeNeedsVector;
	}

	protected void getEmployeeNeeds() { // This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		/*
		 * EmployeeNeed e = registerEmployeeNeed(DUTYCLASS.class, getName() + " needs XXXX.", "You can request xxxxx when " + getName() + " starts, or later under xxxxx."); //e.setAsEntryPoint(true);
		 */
	}

	public EmployeeNeed findEmployeeNeed(Class employeeClass) {
		Vector v = module.getModuleInfo().getEmployeeNeedsVector();
		if (v == null)
			return null;
		for (int i=0; i< v.size(); i++){
			EmployeeNeed need = (EmployeeNeed)v.elementAt(i);
			if (need.getDutyClass().isAssignableFrom(employeeClass))
				return need;
		}
		return null;
	}
	protected String listNeeds() {
		Vector v = module.getModuleInfo().getEmployeeNeedsVector();
		if (v == null)
			return "";
		String s = "";
		for (int i=0; i< v.size(); i++){
			EmployeeNeed need = (EmployeeNeed)v.elementAt(i);
			s += "   " + need.getDutyClass().getName() + "\n";
		}
		return s;
	}
	protected EmployeeNeed registerEmployeeNeed(Class dutyClass, String whatINeed, String entryPoint) {
		EmployeeNeed e = new EmployeeNeed(dutyClass, whatINeed, entryPoint);
		if (employeeNeedsVector == null)
			employeeNeedsVector = new Vector();
		employeeNeedsVector.addElement(e);
		return e;
	}

	protected EmployeeNeed registerEmployeeNeed(Class dutyClass, int whichOne, String whatINeed, String entryPoint) {
		EmployeeNeed e = new EmployeeNeed(dutyClass, whatINeed, entryPoint);
		e.setWhichOne(whichOne);
		if (employeeNeedsVector == null)
			employeeNeedsVector = new Vector();
		employeeNeedsVector.addElement(e);
		return e;
	}

	public boolean getSearchableAsModule() {
		return true;
	}

	/* ................................................................................................................. */
	/** for HNode interface */
	public HNode[] getHDaughters() {
		if (employees == null || employees.size() == 0)
			return null;
		int num = employees.size();
		HNode[] daughters = new HNode[num];
		for (int i = 0; i < num; i++)
			daughters[i] = (HNode) employees.elementAt(i);
		return daughters;
	}

	/* ................................................................................................................. */
	/** for HNode interface */
	public HNode getHMother() {
		return employer;
	}

	/* ................................................................................................................. */
	/** for HNode interface */
	public int getNumSupplements() {
		int numSupp = 0;
		if (module.getManualPath() != null)
			numSupp++;
		if (module.getModuleWindow() != null)
			numSupp++;
		// if (module.getCommandPagePath()!= null)
		// numSupp++;
		return numSupp;
	}

	/* ................................................................................................................. */
	/** for HNode interface */
	public String getSupplementName(int index) {
		// if (module.getManualPath()!= null && index == 0)
		if (index == 0) {
			if (module.getManualPath() != null)
				return "Manual";
			else
				return "Window";
		}
		else if (index == 1)
			return "Window";
		else
			return "";
		// else if (module.getCommandPagePath()!= null)
		// return "Menus & Commands";
		// else
		// return "";
	}

	/* ................................................................................................................. */
	/** for HNode interface */
	public void hNodeAction(Container c, int x, int y, int action) {
		if (c == null)
			return;
		if (action == HNode.MOUSEMOVE) {
			MesquiteWindow f = MesquiteWindow.windowOfItem(c);
			if (f != null && f instanceof MesquiteWindow) {
				String versionString = module.getVersion();
				if (versionString == null)
					versionString = "?";
				String t = "Module: " + module.getName() + "     Version: " + versionString + "     Author(s): " + module.getAuthors() + "\nClass: " + module.getClass().getName() + "\nExplanation: " + module.getExplanation() + "\n" + "Current Parameters: " + module
						.getParameters() + "\n" + "[id: " + module.getID() + "]";
				((MesquiteWindow) f).setExplanation(t);
			}
		}
		else if (action == HNode.MOUSEEXIT) {
			/*
			 * Frame f = module.containerOfModule(); if (f != null && f instanceof MesquiteWindow){ ((MesquiteWindow)f).setExplanation(""); }
			 */
		}
		else if (action == HNode.MOUSEDOWN) {
			MesquitePopup popup = new MesquitePopup(c);
			popup.addItem("Show Information page", module, module.makeCommand("showCommandPage", module));
			popup.addItem("Show Mini Info Window", module, module.makeCommand("showMiniInfoWindow", module));
			// Danny's code for eclipse integration. I don't know if it should be on by default so
			// I'm commenting it out for now.
			// popup.addItem("Open source in Eclipse", module, module.makeCommand("openSourceInEclipse", module));
			popup.showPopup(x, y);
		}
	}

	/** for HNode interface */
	public String getTypeName() {
		return "Module";
	}

	/* ................................................................................................................. */
	/** for HNode interface */
	public void hSupplementTouched(int index) {
		// if (module.getManualPath()!= null)
		module.showManual();
		// else if (module.getCommandPagePath()!= null)
		// module.showWebPage(module.getCommandPagePath());
	}

	/* ---------------- for HNode interface ---------------------- */
	public Image getHImage() {
		if (module.isPrerelease() && module.isSubstantive())
			return InfoBar.prereleaseImage;
		return null;
	}

	/* ---------------- for HNode interface ---------------------- */
	public Color getHColor() {
		if (!module.isSubstantive())
			return ColorDistribution.lightGreen;
		if (module.isPrerelease())
			return Color.red;
		else
			return Color.green;
	}

	/* ---------------- for HNode interface ---------------------- */
	public boolean getHShow() {
		return true;
	}

	/* ................................................................................................................. */
	/** Returns Class the module was hired as, thus implicitly the duty it is to perform */
	public Class getHiredAs() {
		return hiredAs;
	}

	/* ................................................................................................................. */
	/** Returns object passed as a condition to hiring */
	public Object getHiringCondition() {
		return hiringCondition;
	}

	/* ................................................................................................................. */
	/**
	 * Returns whether or not it's appropriate for an employer to hire more than one instance of this module. If false then is hired only once; second attempt fails.
	 */
	public boolean canHireMoreThanOnce() {
		return true;
	}

	/* ................................................................................................................. */
	/** Returns the purpose for which this module was hired (response comes from employer). */
	public final String whatIsMyPurpose() {
		if (employer == null)
			return "";
		else
			return employer.purposeOfEmployee(module);
	}

	/* ................................................................................................................. */
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis"). */
	public String purposeOfEmployee(MesquiteModule employee) {
		return "for " + getName(); // to be overridden
	}

	/* ................................................................................................................. */
	void setExplanationByWhichHired(String exp) {
		explanationByWhichHired = exp;
	}

	/* ................................................................................................................. */
	public String getExplanationByWhichHired() {
		return explanationByWhichHired;
	}

	/* ................................................................................................................. */
	/** Returns vector of employees. */
	public void moveEmployeeToFront(MesquiteModule employee) {
		if (employee == null || employees.indexOf(employee) < 0)
			return;
		employees.removeElement(employee, false);
		employees.insertElementAt(employee, 0, false);
	}

	/* ................................................................................................................. */
	/** Returns vector of employees. */
	public EmployeeVector getEmployeeVector() {
		return employees;
	}

	/* ................................................................................................................. */
	/** Makes vector of employees. */
	protected void makeEmployeeVector() {
		employees = new EmployeeVector();
	}

	/* ................................................................................................................. */
	/** Returns number of employees modules of the MesquiteModule. */
	public int getNumberOfEmployees() {
		if (employees == null)
			return 0;
		else
			return employees.size();
	}

	/* ................................................................................................................. */
	/** returns true if this an employer of the passed MesquiteModule. */
	public boolean isEmployerOf(MesquiteModule mb) {
		if (this == mb || mb == null)
			return false;
		MesquiteModule mT = mb;
		while (mT != null) {
			if (mT == this)
				return true;
			else
				mT = mT.employer;
		}
		return false;
	}

	/* ................................................................................................................. */
	/** returns the immediate employee of which given module is an eventual employee. */
	public MesquiteModule employeeOfWhichEmployee(MesquiteModule mb) {
		if (mb == this)
			return null;
		else if (mb.employer == this) {
			return mb;
		}
		else {
			MesquiteModule lastMt = mb;
			MesquiteModule mT = mb;
			while (mT != this && mT != null) {
				lastMt = mT;
				mT = mT.employer;
			}
			if (mT != null)
				return lastMt;
			else
				return null;
		}
	}

	/* ................................................................................................................. */
	/** Lists the employees of this MesquiteModule. */
	// TODO: use stringbuffer here (pass it)
	public String listEmployees(String spacer) {
		String thisBranch = "";

		int num = employees.size();
		spacer += "  ";
		for (int i = 0; i < num; i++) {
			Object obj = employees.elementAt(i);
			MesquiteModule mb = (MesquiteModule) obj;
			thisBranch += spacer + mb.getName() + StringUtil.lineEnding();
			thisBranch += mb.listEmployees(spacer);
		}
		/*
		 * Enumeration e = employees.elements(); spacer +=" "; while (e.hasMoreElements()) { Object obj = e.nextElement(); MesquiteModule mb = (MesquiteModule)obj; thisBranch += spacer + mb.getName() + StringUtil.lineEnding(); thisBranch += mb.listEmployees(spacer); }
		 */
		return thisBranch;
		// don't need to trim spacer since not returned by reference
	}

	/* ................................................................................................................. */
	/**
	 * Return the name of a given employee as known to the module. This name is the one to be used by the doCommand "getEmployee". It is used to allow modules to override it so as to give their own reference name to an employee. Thus, if an employer has several employees that are instantiations of the same module, "getEmployee 'employee name' would be ambiguous, but the employer can return separate names e.g., 'emp1', 'emp2' and then the command "getEmployee 'emp1'" would be unambiguous. See NodeLocs2DPlot for an example.
	 */
	public String getEmployeeReference(MesquiteModule employee) {
		if (employee == null)
			return null;
		else
			return "#" + employee.getClass().getName();
	}

	/* ................................................................................................................. */
	/** Find the first employee of given name. */
	public MesquiteModule findEmployee(String name) {
		Enumeration e = employees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mb = (MesquiteModule) obj;
			if (mb.getName().equals(name))
				return mb;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Find the first employee of given class. */
	public MesquiteModule findEmployee(Class dutyClass) {
		if (dutyClass != null) {
			Enumeration e = employees.elements();
			while (e.hasMoreElements()) {
				Object obj = e.nextElement();
				MesquiteModule mb = (MesquiteModule) obj;
				if (dutyClass.isInstance(mb))
					return mb;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Returns the module's employer. */
	public String getEmployerPath() {
		if (employer == null)
			return getName();
		else
			return getEmployer().getEmployerPath() + ":" + getName();
	}

	/* ................................................................................................................. */
	/** Returns the module's employer. */
	public MesquiteModule getEmployer() {
		return employer;
	}
	/* ................................................................................................................. */
	/** Returns whether the given module is employer OR higher. */
	public boolean isEmployerOrHigher(EmployerEmployee e) {
		if (e == employer)
			return true;
		EmployerEmployee empr = getEmployer();
		while (empr != null){
			if (empr == e)
				return true;
			empr = empr.getEmployer();
		}
		return false;
	}
	/* ................................................................................................................. */
	/** Returns whether the given module is employer OR higher. */
	public String dumpEmployers() {
		String s = "";
		EmployerEmployee empr = getEmployer();
		while (empr != null){
			s += empr.getClass().getName() + "  " + empr.module.getID() + "\n";
			empr = empr.getEmployer();
		}
		return s;
	}

	/* ................................................................................................................. */
	/** Sets whether module's order in employee rank is allowed to be changed. */
	public void setPriorityFixed(boolean fixed) {
		fixedPriority = fixed;
	}

	/* ................................................................................................................. */
	/** Returns whether module's order in employee rank is allowed to be changed. */
	public boolean isPriorityFixed() {
		return fixedPriority;
	}

	/* ................................................................................................................. */
	/**
	 * Finds most recent common employer between this and mb, and if needed swaps the positions of the two branches from this employer to ensure that mb's branch comes first in the employer list as opposed to this. Used, for instance, by modules requiring .
	 */
	public void deferBranchPriority(MesquiteModule mb) {
		MesquiteModule mrce = getEmployer();
		MesquiteModule thisBranch = mrce;
		while (mrce != null) { // first, find mrce
			if (mrce.isEmployerOf(mb)) { // mrce found
				MesquiteModule otherBranch = mrce.employeeOfWhichEmployee(mb);
				if (otherBranch.isPriorityFixed() || thisBranch.isPriorityFixed())
					return;
				int indexOfOther = mrce.employees.indexOf(otherBranch);
				int indexOfThis = mrce.employees.indexOf(thisBranch);
				if (indexOfOther > indexOfThis) {
					mrce.employees.removeElement(otherBranch, false);
					mrce.employees.insertElementAt(otherBranch, indexOfThis, false);
					mrce.employees.removeElement(thisBranch, false);
					mrce.employees.insertElementAt(thisBranch, indexOfOther, false);
				}
				return;
			}
			else {
				thisBranch = mrce;
				mrce = mrce.getEmployer();
			}

		}
	}

	/* ................................................................................................................. */
	void doomAll() {
		doomed = true;
		if (employees == null)
			return;
		Enumeration e = employees.elements();
		while (e.hasMoreElements()) {
			MesquiteModule mbe = (MesquiteModule) e.nextElement();
			if (mbe != null) {
				mbe.doomAll();
			}
		}
	}

	/* ................................................................................................................. */
	/** Closes all windows. */
	public boolean closeEmployeeWindows(MesquiteModule butNotThis) {
		boolean closed = false;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			closed = mb.closeEmployeeWindows(butNotThis) || closed;
		}
		if (this != butNotThis){
			MesquiteWindow win = getModuleWindow();
			if (win != null && win.isVisible()){
				module.windowGoAway(win);
				return true;
			}
		}
		return closed;
	}

	/* ................................................................................................................. */
	/** Finds the first more senior employer in the heirarchy that belongs to a particular subclass. */
	public MesquiteModule findEmployerWithDuty(Class dutyClass) {
		if (employer == null)
			return null;
		else if (dutyClass.isInstance(employer))
			return employer;
		else
			return employer.findEmployerWithDuty(dutyClass);
	}

	/* ................................................................................................................. */
	/** Finds the first employee in the heirarchy that belongs to a particular subclass. */
	public MesquiteModule findEmployeeWithDuty(Class dutyClass) {
		return findEmployeeWithDuty(dutyClass, null);
	}

	/* ................................................................................................................. */
	/** Finds the first immediate employee that belongs to a particular subclass. */
	public MesquiteModule findImmediateEmployeeWithDuty(Class dutyClass) {
		if (employees == null)
			return null;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (dutyClass.isInstance(mb) && mb != null && !mb.isDoomed()) {
				return mb;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Returns an array of the immediate employees that belong to a particular subclass. */
	public MesquiteModule[] getImmediateEmployeesWithDuty(Class dutyClass) {
		if (employees == null)
			return null;
		Enumeration enumeration = employees.elements();
		int count = 0;
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (dutyClass.isInstance(mb) && mb != null && !mb.isDoomed()) {
				count++;
			}
		}
		if (count == 0)
			return null;
		MesquiteModule[] mods = new MesquiteModule[count];
		count = 0;
		enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (dutyClass.isInstance(mb) && mb != null && !mb.isDoomed()) {
				mods[count++] = mb;
			}
		}
		return mods;
	}

	/* ................................................................................................................. */
	/**
	 * Finds the first employee in the heirarchy that belongs to a particular subclass. Doesn't return or pursue employees of the module "excluding"
	 */
	public MesquiteModule findEmployeeWithDuty(Class dutyClass, MesquiteModule excluding) {
		if (employees == null)
			return null;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (dutyClass.isInstance(mb) && mb != excluding && mb != null && !mb.isDoomed()) {
				return mb;
			}
		}
		// not found among immediate employees; look deeper
		if (employees == null)
			return null;
		enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (mb != excluding && mb != null) {
				MesquiteModule result = mb.findEmployeeWithDuty(dutyClass, excluding);
				if (result != null)
					return result;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	/**
	 * Finds the first employee in the heirarchy that belongs to a particular subclass. Doesn't return or pursue employees of the module "excluding"
	 */
	public MesquiteModule findNextEmployeeWithDuty(Class dutyClass, MesquiteModule excluding, MesquiteModule current, MesquiteBoolean found) {
		if (employees == null)
			return null;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (dutyClass.isInstance(mb) && mb != excluding && mb != null && !mb.isDoomed()) {
				if (mb != current)
					found.setValue(true);
				else if (found.getValue())
					return mb;
			}
		}
		// not found among immediate employees; look deeper
		enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (mb != excluding) {
				MesquiteModule result;
				if (found.getValue())
					result = mb.findEmployeeWithDuty(dutyClass, excluding);
				else
					result = mb.findNextEmployeeWithDuty(dutyClass, excluding, current, found);
				if (result != null)
					return result;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Finds the first employee in the heirarchy that has a particular name. */
	public MesquiteModule findEmployeeWithName(String name) {
		return findEmployeeWithName(name, false);
	}

	/* ................................................................................................................. */
	/** Finds the first employee in the heirarchy that has a particular name. */
	public MesquiteModule findEmployeeWithName(String name, boolean immediate) {
		if (StringUtil.blank(name))
			return null;
		if (employees == null)
			return null;
		boolean className = (name.charAt(0) == '#');
		String useName;
		if (className)
			useName = name.substring(1, name.length());
		else
			useName = name;

		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (mb != null && !mb.isDoomed() && mb.nameMatches(useName)) {
				return mb;
			}
		}
		if (!immediate) {
			// not found among immediate employees; look deeper
			enumeration = employees.elements();
			while (enumeration.hasMoreElements()) {
				MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
				MesquiteModule result = mb.findEmployeeWithName(name);
				if (result != null)
					return result;
			}
		}
		enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (mb != null && !mb.isDoomed() && mb.getNameForMenuItem().equalsIgnoreCase(useName)) {
				return mb;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	/**
	 * Finds the first employee in the heirarchy that has a particular name. Doesn't return or pursue employees of the module "excluding"
	 */
	public MesquiteModule findEmployeeWithName(String name, MesquiteModule excluding) {
		if (StringUtil.blank(name))
			return null;
		if (employees == null)
			return null;
		boolean className = (name.charAt(0) == '#');
		String useName;
		if (className)
			useName = name.substring(1, name.length());
		else
			useName = name;

		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (mb != null && mb != excluding && !mb.isDoomed() && mb.nameMatches(useName)) {
				return mb;
			}
		}
		// not found among immediate employees; look deeper
		enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (mb != excluding) {
				MesquiteModule result = mb.findEmployeeWithName(name, excluding);
				if (result != null)
					return result;
			}
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Finds the employee with given idNumber */
	public MesquiteModule findEmployeeWithIDNumber(long id) {
		if (id < 0)
			return null;
		if (employees == null)
			return null;
		if (module.getID() == id)
			return module;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			MesquiteModule result = mb.findEmployeeWithIDNumber(id);
			if (result != null)
				return result;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Find the employee with given permanent IDString. */
	public MesquiteModule findEmployeeWithPermanentID(String id) {
		if (id == null)
			return null;
		if (employees == null)
			return null;
		if (id.equalsIgnoreCase(module.getPermanentIDString())) // had been getAssignedIDString
			return module;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			MesquiteModule result = mb.findEmployeeWithPermanentID(id);
			if (result != null)
				return result;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Finds the first colleague among employers & employers other employees that belongs to a particular subclass. */
	public void findModules(Class dutyClass, ListableVector found) {
		if (dutyClass.isInstance(this) && module != null && !module.isDoomed())
			found.addElement(this, false);
		if (employees == null)
			return;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			mb.findModules(dutyClass, found);
		}
	}

	/* ................................................................................................................. */
	/**
	 * Finds the first colleague among employers & employers other employees that belongs to a particular subclass, after the current. This searches only within this module's project.
	 */
	public ListableVector findModulesWithDuty(Class dutyClass) {
		ListableVector found = new ListableVector();
		FileCoordinator fc = getFileCoordinator();
		if (fc == null) {
			MesquiteMessage.println("oops, no file coordinator found in findModulesWithDuty");
			return null;
		}
		fc.findModules(dutyClass, found);
		return found;
	}

	/* ................................................................................................................. */
	/**
	 * Finds the nearest module that belongs to a particular subclass. Searches first among employees, then among colleagues, then among modules of this project, then among modules across Mesquite.
	 */
	public MesquiteModule findNearestModuleWithDuty(Class dutyClass) {
		MesquiteModule mb = findEmployeeWithDuty(dutyClass);
		if (mb == null)
			mb = findNearestColleagueWithDuty(dutyClass);
		if (mb == null && getFileCoordinator() != null)
			mb = getFileCoordinator().findEmployeeWithDuty(dutyClass);
		if (mb == null)
			mb = MesquiteTrunk.mesquiteTrunk.findEmployeeWithDuty(dutyClass);

		return mb;
	}

	/* ................................................................................................................. */
	/**
	 * Finds the first colleague among employers & employers other employees that belongs to a particular subclass. Searches only within project of module.
	 */
	public MesquiteModule findNearestColleagueWithDuty(Class dutyClass) {
		if (employer == null) // no employer, only employees; "colleagues" defined to exclude employees EXCEPT at root
			return null;
		else if (dutyClass.isInstance(employer) && employer != null && !employer.isDoomed()) // immediate employer suits
			return employer;
		else if (this != getFileCoordinator()) {
			MesquiteModule sisterWithDuty = employer.findEmployeeWithDuty(dutyClass, module); // ask for employee excluding current
			if (sisterWithDuty != null)// check sisters
				return sisterWithDuty;
			else
				// sisters don't satisfy, thus find nearest colleague at employer's level, requesting it doesn't include current
				return employer.findNearestColleagueWithDuty(dutyClass);
		}
		else
			return null;
	}

	/* ................................................................................................................. */
	/** Finds the first colleague among employers & employers other employees that belongs to a particular subclass. */
	// TODO: this should probably be avoided, since names may change. In most cases in which it is used, it may be better to define a specific duty class
	// TODO: check for # and classname
	public MesquiteModule findNearestColleagueWithName(String name) {
		if (employer == null || name == null) // no employer, only employees; "colleagues" defined to exclude employees
			return null;
		else if (name.equals(employer.getName()) && employer != null && !employer.isDoomed()) // immediate employer suits
			return employer;
		else if (this != getFileCoordinator()) {
			MesquiteModule sisterWithDuty = employer.findEmployeeWithName(name, module); // ask for employee excluding current
			if (sisterWithDuty != null)// check sisters
				return sisterWithDuty;
			else
				// sisters don't satisfy, thus find nearest colleague at employer's level, requesting it doesn't include current
				return employer.findNearestColleagueWithName(name);
		}
		else
			return null;
	}

	/* ................................................................................................................. */
	/** Returns the employee module of the file coordinator module that manages FileElements of the class passed. Used, for instance, to find the TreeManager */
	public ElementManager findElementManager(Class fileElementClass) {
		FileCoordinator fCoord = getFileCoordinator();
		if (fCoord == null)
			return null;
		return fCoord.findManager(fCoord, fileElementClass);
	}

	/* ................................................................................................................. */
	/** Returns the module in charge of showing the data editor for a CharacterData object, if any */
	public DataWindowMaker findCharacterDataEditorRec(mesquite.lib.characters.CharacterData data) {
		if (this instanceof DataWindowMaker && ((DataWindowMaker) this).getCharacterData() == data)
			return (DataWindowMaker) module;
		if (employees == null)
			return null;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			DataWindowMaker mbc = mb.findCharacterDataEditorRec(data);
			if (mbc != null)
				return mbc;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Returns the module in charge of showing the data editor for a CharacterData object, if any */
	public DataWindowMaker findCharacterDataEditor(mesquite.lib.characters.CharacterData data) {
		FileCoordinator fCoord = getFileCoordinator();
		if (fCoord == null)
			return null;
		return fCoord.findCharacterDataEditorRec(data);
	}

	/* ................................................................................................................. */
	/** Finds the employee that curates a given class of characterModels */
	public CharModelCurator findModelCuratorRec(Class modelClass) {
		if (this instanceof CharModelCurator && ((CharModelCurator) this).curatesModelClass(modelClass))
			return (CharModelCurator) module;
		if (employees == null)
			return null;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			CharModelCurator mbc = mb.findModelCuratorRec(modelClass);
			if (mbc != null)
				return mbc;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Finds the employee that curates a given class of characterModels */
	public CharModelCurator findModelCurator(Class modelClass) {
		FileCoordinator fCoord = getFileCoordinator();
		if (fCoord == null)
			return null;
		return fCoord.findModelCuratorRec(modelClass);
	}

	/* ................................................................................................................. */
	/** returns the file coordinator module for the project of this module */
	public FileCoordinator getFileCoordinator() {
		if (this instanceof FileCoordinator)
			return (FileCoordinator) this;
		else
			return (FileCoordinator) findEmployerWithDuty(FileCoordinator.class);
	}

	/* ................................................................................................................. */
	private boolean queryAbortFromInitiator(String explanation) {
		// find initator of hire sequence. If this is not in startup, then don't do anything. If employer module is not in startup, then this is initiator and don't touch
		// otherwise initiator is among employers; query use if want to stop, and if so go to initiator and mark all of its descendants as bail out
		// bail out should cause startupEmployee to return null
		// if (inStartup){
		EmployerEmployee initiator = this;
		while (initiator != null && initiator.employer != null && initiator.employer.inStartup) {
			initiator = initiator.employer;
		}
		if (initiator != null) {

			if (initiator != this) {
				module.discreetAlert( "The request for " + initiator.getName() + " failed or was cancelled.");
				markBailOut(initiator);
			}
			return true;
		}
		// }
		return false;

	}

	private void markBailOut(EmployerEmployee ee) {
		if (ee == null)
			return;
		ee.startupBailOut = true;
		Enumeration e = ee.employees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mb = (MesquiteModule) obj;
			markBailOut(mb);
		}
	}

	/* ................................................................................................................. */
	/** This is the word to be used to describe the module as "active". You might wish to override this and replace it with "shown", or something like that. */
	public String moduleActiveWord() {
		return "active";
	}

	/* ................................................................................................................. */
	public static String nameOfModuleBeingStarted = null;
	/** Start up employee, with a given condition as a startup condition */
	private boolean startupEmployee(MesquiteModule mb, Class dutyClass, Object condition, String arguments) {
		if (mb == null)
			return false;
		//CommandRecord.tick("Starting module \"" + mb.getName() + "\"");
		nameOfModuleBeingStarted = mb.getClass().getName();
		int mreset = getMenuResetSuppression();
		mb.inStartup = true;
		if (!mb.canHireMoreThanOnce()) {
			if (findImmediateEmployeeWithDuty(mb.getClass()) != null) {
				if (!MesquiteThread.isScripting())
					module.alert("\"" + mb.getName() + "\" is already " + mb.moduleActiveWord() + ".  Only one instance is allowed in this context.");
				mb.inStartup = false;
				mb.endJob();
				mb.dispose();
				nameOfModuleBeingStarted = null;
				return false;
			}
		}
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.3) starting up conditionally " + mb.getName());
		if (MesquiteTrunk.debugMode && MesquiteTrunk.reportUnregisteredNeeds){
			EmployeeNeed need = findEmployeeNeed(mb.getClass());
			if (need == null) {
				MesquiteMessage.println("@@@@@@@@@@@@@@@@@@@@");
				MesquiteMessage.println("UNREGISTERED NEED: " + dutyClass.getName() + " for " + module.getModuleInfo().getClassName() + " (hiring " + mb.getModuleInfo().getClassName() + ")");
				String ln = listNeeds();
				if (!StringUtil.blank(ln))
					MesquiteMessage.println(ln);
			}
		}
		incrementMenuResetSuppression();
		incrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
		refreshBrowser(MesquiteModule.class);
		mb.resetContainingMenuBar(); // added Oct 99
		mb.makeEmployeeVector();
		mb.employer = module;
		mb.proj = module.getProject();
		mb.hiredAs = dutyClass;

		int index = -1;
		if (employees == null)
			return false;
		for (int i = this.employees.size() - 1; i >= 0; i--) {
			MesquiteModule emb = (MesquiteModule) this.employees.elementAt(i);
			if (emb.getIfLastEmployee()) {
				index = i;
			}
		}
		if (index >= 0) {
			this.employees.insertElementAt(mb, index, false);
		}
		else
			this.employees.addElement(mb, false);

		boolean started = false;
		Error errorToThrow = null;
		try {
			moduleStartupTime.start();
			if (condition == null && StringUtil.blank(arguments)) {
				started = mb.superStartJob(null, null, false);
				if (started)
					started = mb.startJob(null, null, false);
				if (started)
					mb.incrementNumStarts();
			}
			else {
				started = mb.superStartJob(arguments, condition, false);
				if (started)
					started = mb.startJob(arguments, condition, false);
				if (started)
					mb.incrementNumStarts();
			}
		} 
		catch (Exception e) {
			started = false;
			moduleStartupTime.end();
			MesquiteDialog.closeWizard();
			if (Thread.currentThread() instanceof MesquiteThread && ((MesquiteThread)Thread.currentThread()).dead()) {

			}
			else {
				MesquiteFile.throwableToLog(this, e);
				module.exceptionAlert(e, "There has been a problem starting a module (" + e.getClass() + ").  (module: " + mb.getName() + "; arguments: " + arguments  + "; module being started: " + nameOfModuleBeingStarted + "; EXCEPTION: " + e.getMessage() + "; exception class " + e.getClass() +  " [1])");
			}
			MesquiteTrunk.zeroMenuResetSuppression(); //EXCEPTION HANDLER
			MesquiteTrunk.resetAllMenuBars();

		} 
		catch (Error e) {
			started = false;
			moduleStartupTime.end();
			if (e instanceof OutOfMemoryError)
				MesquiteTrunk.mesquiteTrunk.discreetAlert("OutofMemoryError.  See file startingMesquiteAndMemoryAllocation.txt in the Mesquite_Folder for information on how to increase memory allocated to Mesquite.");

			else if (!(e instanceof ThreadDeath)) {
				MesquiteFile.throwableToLog(this, e);
				errorToThrow = e;
				if (e instanceof AbstractMethodError || e instanceof NoSuchMethodError ){
					module.alert("There has been a problem starting a module (" + e.getClass() + ").  It appears that you have installed a package that is old or otherwise incompatible with this version of Mesquite.  The incompatible package is probably listed under Extra Packages Installed at the start of the log.  (module: " + mb.getName() + "; arguments: " + arguments + "; module being started: " + nameOfModuleBeingStarted + "; ERROR: " + e.getMessage() + "; error class " + e.getClass() + " [2'])");
					errorToThrow = null;
				}
				else if (e instanceof NoClassDefFoundError )
					module.alert("There has been a problem starting a module (" + e.getClass() + ").  It appears that a component of Mesquite or a required library is missing; this probably means that something was installed correctly.  (module: " + mb.getName() + "; arguments: " + arguments + "; module being started: " + nameOfModuleBeingStarted + "; ERROR: " + e.getMessage() + "; error class " + e.getClass() + " [2'])");
				else
					module.exceptionAlert(e, "There has been a problem starting a module (" + e.getClass() + ").  This may be the result of an old, incompatible package being used, or part of Mesquite was accidentally deleted.  (module: " + mb.getName() + "; arguments: " + arguments  + "; module being started: " + nameOfModuleBeingStarted + "; ERROR: " + e.getMessage() + "; error class " + e.getClass() + " [2])");
			}
			MesquiteTrunk.zeroMenuResetSuppression(); //EXCEPTION HANDLER
			MesquiteTrunk.resetAllMenuBars();
		}

		if (!started) {
			if (showHiringPath)
				MesquiteMessage.warnProgrammer("(HIRING.4) FAILED TO START " + mb.getName());
			mb.inStartup = false;
			mb.endJob();
			mb.dispose();
			/*
			 * mb.dispose(); this.employees.removeElement(mb);
			 */
			// MesquiteMessage.warnUser("Module \"" + mb.getName() + "\" unable to start.");
			if (errorToThrow != null) {
				setMenuResetSuppression(mreset);
				nameOfModuleBeingStarted = null;
				throw errorToThrow;
			}
			else {
				setMenuResetSuppression(mreset);
				decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
				nameOfModuleBeingStarted = null;
				return false;
			}
		}
		mb.internalStartUp();
		decrementEmployeeBrowserRefreshSuppression(MesquiteModule.class);
		decrementMenuResetSuppression();
		mb.inStartup = false;
		nameOfModuleBeingStarted = null;
		return !mb.startupBailOut;
	}

	/* ................................................................................................................. */
	/** calls c.newInstance() to instantiate a module. Catches exceptions and errors and records & reports them */
	public MesquiteModule instantiateModule(Class c) {
		if (c == null)
			return null;
		try {
			return (MesquiteModule) c.newInstance();
		} catch (Exception e) {
			if (Thread.currentThread() instanceof MesquiteThread && ((MesquiteThread)Thread.currentThread()).dead()) {
			}
			else {
				MesquiteFile.throwableToLog(this, e);
				module.exceptionAlert(e, "There has been a problem starting a module (" + e.getClass() + ").  This may be the result of an old, incompatible module being used.  The calling module is " + module.getName() + " and the module being started is of class " + c + ".  Error: " + e.getMessage() + "");
			}
		} catch (Error e) {
			MesquiteFile.throwableToLog(this, e);
			if (e instanceof NoSuchMethodError)
				module.exceptionAlert(e, "There has been a problem starting a module (NoSuchMethodError).  This may be the result of an old, incompatible module being used.  The calling module is " + module.getName() + " and the module being started is of class: " + c + "");
			else
				module.exceptionAlert(e, "There has been a problem starting a module (" + e.getClass() + ").  This may be the result of an old, incompatible module being used.  The calling module is " + module.getName() + " and the module being started is of class " + c + ".  Error: " + e.getMessage() + "");
			throw e;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Instantiate an employee and sets a few of its critical fields (employer, moduleInfo) */
	public MesquiteModule instantiateEmployee(MesquiteModuleInfo mbi) {
		if (mbi == null)
			return null;
		MesquiteModule mb = instantiateModule(mbi.mbClass);
		if (mb != null) {
			mb.employer = module;
			mb.moduleInfo = mbi;
			if (module == null)
				MesquiteMessage.warnProgrammer("employer null in instantiateEmployee " + this);
		}
		return mb;
	}

	/* ................................................................................................................. */
	/** hires an employee, perhaps with the passed supplementary string and condition */
	private MesquiteModule hire(MesquiteModuleInfo mbi, Class dutyClass, Object condition, String arguments, String explByWhichHired) {
		if (mbi == null)
			return null;
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.8) hire(MesquiteModule mb, Class dutyClass, String supplementary, Object condition)");
		Vector hiringPath = CommandRecord.getHiringPathS();
		if (hiringPath != null && hiringPath.size()>0){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)hiringPath.lastElement();
			if (mbi == mmi){
				hiringPath.removeElementAt(hiringPath.size()-1);
			}
		}
		incrementMenuResetSuppression();
		try {
			MesquiteModule mb = instantiateEmployee(mbi);
			mb.setExplanationByWhichHired(explByWhichHired);

			if (startupEmployee(mb, dutyClass, condition, arguments)) {
				if (getMenuResetSuppression() == 0) {
					MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteModule.class);
				}
				decrementMenuResetSuppression();
				if (mb != null)
					mb.hiringCondition = condition;
				return mb;
			}
		}
		catch (AbstractMethodError e){
			module.exceptionAlert(e, "There has been a problem starting a module (" + e.getClass() + ").  It appears that you have installed a package that is old or otherwise incompatible with this version of Mesquite.  (module: " + mbi.getName() + "; arguments: " + arguments + "; ERROR: " + e.getMessage() + "; error class " + e.getClass() + " [h'])");
			throw e;
		}
		decrementMenuResetSuppression();

		return null;
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employee the first MesquiteModule found that is instance of the given class and that has given name (name should be first token of arguments. If name is null, first MesquiteModule found of the class is employeed.
	 */
	public MesquiteModule hireNamedEmployee(Class dutyClass, String arguments) {
		return hireNamedEmployee(dutyClass, arguments, null, false);
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employee the first MesquiteModule found that is instance of the given class and that has given name. If name is null, first MesquiteModule found of the class is employeed.
	 */
	public MesquiteModule hireNamedEmployee(Class dutyClass, String arguments, Object condition) {
		return hireNamedEmployee(dutyClass, arguments, condition, false);
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employee the first MesquiteModule found that is instance of the given class and that has given name. If name is null, first MesquiteModule found of the class is employeed.
	 * <p><p>To pass multiple module names in arguments (i.e. for subsequent hiring instructions), use the format:
	 * <br><br>"$ #NameOfFirstModule supplementaryArguments" <br><br>For example, to hire a TreeSource that is a ConsensusTreesFromBlocks AND
	 * upon hiring, pass the arguments for the source of trees to consense to be SimulatedTreeBlocks, then the  
	 * arguments String would be: "$ #ConsensusTreesFromBlocks #SimulatedTreeBlocks"
	 */
	public MesquiteModule hireNamedEmployee(Class dutyClass, String arguments, Object condition, boolean warnIfNotFound) {
		if (startupBailOut)
			return null;
		if (arguments == null)
			return null;
		incrementMenuResetSuppression();
		String moduleName;
		String supplementary = null;
		if (ParseUtil.firstDarkChar(arguments) == '$') { // name is quoted; try to find actual name of module
			moduleName = parser.getTokenNumber(arguments, 2);
			supplementary = StringUtil.tokenize(parser.getNextToken()); // if second token after module name, pass to module via the arguments of startJob as hiring subchoice
		}
		else
			moduleName = parser.getFirstToken(arguments);
		if (StringUtil.blank(arguments)) {
			decrementMenuResetSuppression();
			return null;
		}
		if ("?".equals(moduleName)) {
			boolean wasScrip = MesquiteThread.isScripting();
			CommandRecord rec = MesquiteThread.getCurrentCommandRecord();
			if (rec != null)
				rec.setScripting(false);
			MesquiteModule mm = hireEmployee(dutyClass, "Choose Module");
			if (rec != null)
				rec.setScripting(wasScrip);
			decrementMenuResetSuppression();
			return null;
		}
		MesquiteModuleInfo c = MesquiteTrunk.mesquiteModulesInfoVector.findModule(dutyClass, moduleName);
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.12) named is sought " + whichModInfo(c));

		MesquiteModule mb = null;
		if (c != null) { // module info for that name was found; try to hire the module
			if (showHiringPath)
				MesquiteMessage.warnProgrammer("(HIRING.14) module about to be hired " + whichModInfo(c));
			mb = hire(c, dutyClass, condition, supplementary, null);
		}
		else if (warnIfNotFound) { // module info for that name was not found !MesquiteThread.isScripting() &&

			warnUserModuleNotFound(moduleName);
			// MesquiteMessage.warnUser("Named module not found for duty " + dutyClass.getName() + "; Modules sought: " + arguments);
		}
		decrementMenuResetSuppression();
		return mb;
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employee the first MesquiteModule found that is instance of the given class. If explanation is given and scripting is false, will query user first if more than one.
	 */
	public MesquiteModule hireEmployee(Class dutyClass, String explanation) {
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.5) hireEmployee ( Class dutyClass, String explanation)");
		return findAndHire(dutyClass, null, explanation);
	}
	/* ................................................................................................................. */
	protected Listable[] prioritize(Listable[] moduleInfos, Class dutyClass){
		if (moduleInfos == null)
			return null;
		boolean secondaryExist = false;
		Listable[] p = new Listable[moduleInfos.length];
		int count = 0;
		for (int i= 0; i< moduleInfos.length; i++){
			if (((Prioritizable)moduleInfos[i]).isFirstPriority(dutyClass))
				p[count++] = moduleInfos[i];
			else
				secondaryExist = true;
		}
		for (int i= 0; i< moduleInfos.length; i++)
			if (!((Prioritizable)moduleInfos[i]).isFirstPriority(dutyClass))
				p[count++] = moduleInfos[i];
		return p;
	}
	/* ................................................................................................................. */
	protected MesquiteModule[] prioritize(MesquiteModule[] modules, Class dutyClass){
		if (modules == null)
			return null;
		boolean secondaryExist = false;
		MesquiteModule[] p = new MesquiteModule[modules.length];
		int count = 0;
		for (int i= 0; i< modules.length; i++){
			MesquiteModuleInfo mmi = modules[i].getModuleInfo();
			if (((Prioritizable)mmi).isFirstPriority(dutyClass))
				p[count++] = modules[i];
			else
				secondaryExist = true;
		}
		for (int i= 0; i< modules.length; i++){
			MesquiteModuleInfo mmi = modules[i].getModuleInfo();
			if (!((Prioritizable)mmi).isFirstPriority(dutyClass))
				p[count++] = modules[i];
		}
		return p;
	}

	protected Listable queryChooseModule(String message, Class dutyClass, Object condition, MesquiteString arguments, Listable[] names, int current, StringBuffer compatibilityReport) {
		//		MesquiteInteger io = new MesquiteInteger(current);
		Vector hiringPath = CommandRecord.getHiringPathS();
		if (hiringPath != null && hiringPath.size()>0){
			MesquiteModuleInfo mmi = (MesquiteModuleInfo)hiringPath.lastElement();
			if (dutyClass.isAssignableFrom(mmi.getModuleClass()) && (condition == null || mmi.isCompatible(condition, module.getProject(), this))){
				return mmi;
			}
		}
		String s = MesquiteString.helpString;
		if (StringUtil.blank(s)) {
			String duty = MesquiteTrunk.mesquiteTrunk.mesquiteModulesInfoVector.getDutyName(dutyClass);

			s = "<h3>" + duty + "</h3>";
			EmployeeNeed need = findEmployeeNeed(dutyClass);
			if (need != null)
				s += need.getExplanation();
			else {
				s += "<p>(Needed by " + getName() + ")";
				if (MesquiteTrunk.debugMode)
					s += "<br>" + module.getModuleInfo().getShortClassName();
			}

		}
		if (!StringUtil.blank(compatibilityReport))
			s += "<hr><h4>Note</h4>" +compatibilityReport.toString();
		names = prioritize(names, dutyClass);
		if (subChoicesOnInDialogs){
			//embedding hiring subchoices (new to 2. 01)
			int countExtras = 0;
			for (int i=0; i< names.length; i++){
				MesquiteModuleInfo mci = (MesquiteModuleInfo)names[i];
				if (mci.getHireSubchoice()!= null){
					Listable[] sub = MesquiteTrunk.mesquiteModulesInfoVector.getModulesOfDuty(mci.getHireSubchoice(), condition, null, null);
					if (sub != null)
						countExtras += sub.length;
				}
			}
			if (countExtras > 0){
				boolean[] isSubchoice = new boolean[countExtras + names.length];
				Listable[] newList = new Listable[countExtras + names.length];
				int count = 0;
				for (int i=0; i< names.length; i++){
					MesquiteModuleInfo mci = (MesquiteModuleInfo)names[i];
					newList[count] = mci;
					isSubchoice[count++] = false;
					if (mci.getHireSubchoice()!= null){  
						Listable[] sub = MesquiteTrunk.mesquiteModulesInfoVector.getModulesOfDuty(mci.getHireSubchoice(), condition, null, null);
						sub = prioritize(sub, mci.getHireSubchoice());
						if (sub != null){
							for (int k = 0; k< sub.length; k++){
								isSubchoice[count] = true;
								newList[count++] = sub[k];
							}
						}
					}
				}

				int chosen =   ListDialog.queryModuleList(module, "Select", message, s, newList, isSubchoice, true, dutyClass, 0);

				if (!MesquiteInteger.isCombinable(chosen))
					return null;
				if (isSubchoice[chosen]){

					if (arguments != null){
						MesquiteModuleInfo mci = (MesquiteModuleInfo)newList[chosen];
						arguments.setValue("#" + mci.getClassName()); 
					}
					for (int k = chosen-1; k>=0; k--){
						if (!isSubchoice[k])
							return newList[k];
					}
					if (arguments != null)
						arguments.setValue((String)null);
					return null;
				}
				else
					return newList[chosen];
			}
		}
		return (MesquiteModuleInfo) ListDialog.queryModuleList(module, "Select", message, s, names, true, dutyClass, 0);
	}
	protected boolean acceptCancelInHireEmployee = true;
	protected void acceptEmployeeHireCancel(boolean accept){
		this.acceptCancelInHireEmployee = accept;
	}
	/* ................................................................................................................. */
	/** Looks for (perhaps asking the user) and hires a compatible MesquiteModule that is instance of the given class */
	private MesquiteModule findAndHire(Class dutyClass, Object condition, String explanation) {
		if (startupBailOut)
			return null;
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.9) findAndHire ( Class dutyClass, Object condition, String explanation)");
		if (MesquiteTrunk.mesquiteModulesInfoVector == null)
			return null;
		incrementMenuResetSuppression();
		int numAvailable = numCompatibleModulesAvailable(dutyClass, condition, module);

		// ask user for name if not scripting and explanation available
		if (!suppressHireQueries && !MesquiteThread.isScripting() && explanation != null && numAvailable > 1) {
			MesquiteModule mb = null;
			StringBuffer compatibilityReport = new StringBuffer();
			String prefix = "";
			Listable[] list = MesquiteTrunk.mesquiteModulesInfoVector.getModulesOfDuty(dutyClass, condition, module, compatibilityReport);
			while (mb == null && !startupBailOut) {
				MesquiteString args = new MesquiteString();
				MesquiteModuleInfo c = (MesquiteModuleInfo) queryChooseModule(prefix + explanation, dutyClass, condition, args, list, 0, compatibilityReport);
				String subchoice = null;
				if (!args.isBlank())
					subchoice = args.toString();
				if (c == null) {
					if (!acceptCancelInHireEmployee) 
						queryAbortFromInitiator(explanation);
					decrementMenuResetSuppression();
					return null;

				}
				if (c != null) {
					mb = hire(c, dutyClass, condition, subchoice, explanation);
					if (mb == null && !startupBailOut) {
						if (queryAbortFromInitiator(explanation)) {
							decrementMenuResetSuppression();
							return null;
						}

					}
					if (mb == null && !startupBailOut)
						prefix = "Attempt to start module \"" + c.getName() + "\" failed.  Choose an alternative module if desired:\n";
				}
			}
			decrementMenuResetSuppression();
			return mb;
		}
		else { // otherwise just hire first one you can
			MesquiteModuleInfo c = MesquiteTrunk.mesquiteModulesInfoVector.findModule(dutyClass, condition, module.getProject(), this);
			if (c == null || c.getModuleClass() == getClass()) // not allowed to hire oneself by default
				c = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(dutyClass, c, condition, module.getProject(), this);
			MesquiteModule mb = null;

			if (c != null) // trying to get first module that will work (default if possible);
				mb = hire(c, dutyClass, condition, null, explanation);
			if (mb == null && !startupBailOut) {
				if (queryAbortFromInitiator(explanation)) {
					decrementMenuResetSuppression();
					return null;
				}
			}

			while (c != null && mb == null && !startupBailOut) { // if wasn't successful, find first that works.
				c = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(dutyClass, c, condition, module.getProject(), this);
				if (c != null) {
					mb = hire(c, dutyClass, condition, null, explanation);
					if (mb != null && MesquiteThread.isScripting())
						mb.hiredAsDefaultInScripting = true;
				}
			}

			decrementMenuResetSuppression();

			return mb;
		}
	}

	/* ................................................................................................................. */
	/** Hires as employee the particular MesquiteModule corresponding to the MesquiteModuleInfo. */
	public MesquiteModule hireEmployeeFromModuleInfo(MesquiteModuleInfo mbi, Class dutyClass) {
		if (startupBailOut)
			return null;
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.16) hireEmployee ( MesquiteModuleInfo mbi, Class dutyClass");
		incrementMenuResetSuppression();
		MesquiteModule mb = hire(mbi, dutyClass, null, null, null);
		decrementMenuResetSuppression();
		return mb;
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employee the first MesquiteModule found that is instance of the given class and that claims compatibility with given condition.
	 */
	public MesquiteModule hireCompatibleEmployee(Class dutyClass, Object condition, String explanation) {
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.19) hireCompatibleEmployee dutyClass (" + dutyClass + ") (" + condition + ")");
		return findAndHire(dutyClass, condition, explanation);
	}

	/* ................................................................................................................. */
	/** Hires as employees all MesquiteModules found of given class */
	public void hireAllEmployees(Class dutyClass) {
		hireAllCompatibleEmployees(dutyClass, null);
		/*
		 * if (startupBailOut) return; incrementMenuResetSuppression(); if (MesquiteTrunk.mesquiteModulesInfoVector==null) return; Enumeration enumeration=MesquiteTrunk.mesquiteModulesInfoVector.elements(); MesquiteModuleInfo mbi; while (enumeration.hasMoreElements()){ Object obj = enumeration.nextElement(); mbi = (MesquiteModuleInfo)obj; if (mbi.doesDuty(dutyClass)) { MesquiteModule mb = hireEmployeeFromModuleInfo( mbi, dutyClass); } } decrementMenuResetSuppression();
		 */
	}

	/* ................................................................................................................. */
	/** Hires as employees all MesquiteModules found of given class */
	public void hireAllCompatibleEmployees(Class dutyClass, Object comp) {
		if (startupBailOut)
			return;
		incrementMenuResetSuppression();
		if (MesquiteTrunk.mesquiteModulesInfoVector == null)
			return;
		Enumeration enumeration = MesquiteTrunk.mesquiteModulesInfoVector.elements();
		MesquiteModuleInfo mbi;
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			mbi = (MesquiteModuleInfo) obj;
			if (mbi.doesDuty(dutyClass) && mbi.isCompatible(comp, module.getProject(), module)) {
				MesquiteModule mb = hireEmployeeFromModuleInfo(mbi, dutyClass);
			}
		}
		decrementMenuResetSuppression();
	}

	/* ................................................................................................................. */
	/** Hires as employees all MesquiteModules found of given class */
	public void hireAllOtherCompatibleEmployees(Class dutyClass, Object comp) {
		if (startupBailOut)
			return;
		incrementMenuResetSuppression();
		if (MesquiteTrunk.mesquiteModulesInfoVector == null)
			return;
		Enumeration enumeration = MesquiteTrunk.mesquiteModulesInfoVector.elements();
		MesquiteModuleInfo mbi;
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			mbi = (MesquiteModuleInfo) obj;
			if (mbi.doesDuty(dutyClass) && mbi.isCompatible(comp, module.getProject(), module) && (!employeeExists(mbi, dutyClass))) {
				MesquiteModule mb = hireEmployeeFromModuleInfo(mbi, dutyClass);
			}
		}
		decrementMenuResetSuppression();
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employees all MesquiteModules found of given class except those already hired of same class. Note: currently does this by checking name and duty class of existing employees
	 */
	public final void hireAllOtherEmployees(Class dutyClass) {
		if (startupBailOut)
			return;
		incrementMenuResetSuppression();
		if (MesquiteTrunk.mesquiteModulesInfoVector == null)
			return;
		Enumeration enumeration = MesquiteTrunk.mesquiteModulesInfoVector.elements();
		MesquiteModuleInfo mbi;
		while (enumeration.hasMoreElements()) {
			Object obj = enumeration.nextElement();
			mbi = (MesquiteModuleInfo) obj;
			if (mbi.doesDuty(dutyClass) && (!employeeExists(mbi, dutyClass))) {
				MesquiteModule mb = hireEmployeeFromModuleInfo(mbi, dutyClass);
			}
		}
		decrementMenuResetSuppression();
	}

	/* ................................................................................................................. */
	/**
	 * Attempts to hire the module of the name indicated by the first token of arguments passed. Condition passed is a condition of hiring (e.g. for compatibility); "Explanation" can be used to query user if arguments passed is blank or null. If a successful hiring is done, the old employee is fired.
	 */
	private MesquiteModule replace(Class dutyClass, String arguments, String explanation, Object condition, MesquiteModule oldEmployee) {
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.24X) replaceEmployee ( Class dutyClass, String arguments, String explanation, Object condition, MesquiteModule oldEmployee)");
		if (MesquiteTrunk.mesquiteModulesInfoVector == null || dutyClass == null)
			return null;
		incrementMenuResetSuppression();

		if (!StringUtil.blank(arguments)) {
			String supplementary = null;
			String moduleName = null;
			if (ParseUtil.firstDarkChar(arguments) == '$') { // $ signals two arguments, second being probably subemployee; try to find actual name of module to be hired
				moduleName = parser.getTokenNumber(arguments, 2);
				supplementary = StringUtil.tokenize(parser.getNextToken());// if second token after module name, pass to module via arguments of startJob
			}
			else
				moduleName = parser.getFirstToken(arguments);
			if (StringUtil.blank(arguments)) {
				decrementMenuResetSuppression();
				return null;
			}
			if ("?".equals(moduleName)) {
				boolean wasScrip = MesquiteThread.isScripting();
				CommandRecord rec = MesquiteThread.getCurrentCommandRecord();
				if (rec != null)
					rec.setScripting(false);
				MesquiteModule mb = hireEmployee(dutyClass, explanation);
				if (rec != null)
					rec.setScripting(wasScrip);
				if (mb != null) {
					if (oldEmployee != null)
						fireEmployee(oldEmployee);
					resetContainingMenuBar();
					MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteModule.class);
				}
				decrementMenuResetSuppression();
				return mb;
			}
			MesquiteModuleInfo c = MesquiteTrunk.mesquiteModulesInfoVector.findModule(dutyClass, moduleName);
			if (c != null) {
				MesquiteModule mb = hire(c, dutyClass, condition, supplementary, explanation);
				if (mb != null) {
					if (oldEmployee != null)
						fireEmployee(oldEmployee);
					resetContainingMenuBar();
					MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteModule.class);
				}
				decrementMenuResetSuppression();
				return mb;
			}
			else {
				warnUserModuleNotFound(moduleName);
				decrementMenuResetSuppression();
				return null;
			}
		}
		else {
			MesquiteModule mb = findAndHire(dutyClass, condition, explanation);
			if (mb != null) {
				if (oldEmployee != null)
					fireEmployee(oldEmployee);
				resetContainingMenuBar();
				MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteModule.class);
			}
			decrementMenuResetSuppression();
			return mb;
		}
	}

	/* ................................................................................................................. */
	private void warnUserModuleNotFound(String moduleName) {
		CommandRecord rec = MesquiteThread.getCurrentCommandRecord();
		if (rec != null)
			rec.setErrorFound();
		if (moduleName == null)
			return;
		String message = "The module \"" + StringUtil.getLastItem(moduleName, ".") + "\", named in a command, was not found.";
		boolean compoundClassName = (moduleName.charAt(0) == '#' && moduleName.indexOf(".") >= 0);
		if (moduleName.charAt(0) == '#' && moduleName.indexOf(".") >= 0) { // compound class name
			String useName = moduleName.substring(1, moduleName.length());
			message = "The module \"" + StringUtil.getLastItem(useName, ".") + "\", named in a command, was not found.";
			if (MesquiteThread.isScripting())
				message += "\nThe script or macro being executed may not execute properly.";
			String s = StringUtil.getAllButLastItem(useName, ".");
			while (s != null && StringUtil.characterCount(s, '.') > 1) {
				s = StringUtil.getAllButLastItem(s, ".");
			}
			if (s != null && StringUtil.characterCount(s, '.') == 1) {
				// s is now package name; find package and diagnose
				MesquitePackageRecord mpr = MesquitePackageRecord.findPackage(s);
				if (mpr == null)
					message += "\nThe package in which this module resides (" + s + ") appears not to be installed.";
				else if (mpr.loaded)
					message += "\nThe package in which this module resides (" + mpr.getName() + ") is installed and activated (loaded).  Perhaps the package has changed, or the command contains a misspelled name.";
				else
					message += "\nThe package in which this module resides (" + mpr.getName() + ") is installed but is not activated (loaded).  To use this module, change the activation/deactivation status of the package using the menu items in the File menu, and restart Mesquite.";
			}

		}
		else {
			String useName = moduleName;
			if (moduleName.charAt(0) == '#')
				useName = moduleName.substring(1, moduleName.length());
			message = "The module \"" + useName + "\", named in a command, was not found.";
			if (MesquiteThread.isScripting())
				message += "\nThe script or macro being executed may not execute properly.";
			if (!MesquitePackageRecord.allPackagesActivated())
				message += "\nSome packages of modules appear to be installed but not activated (loaded). Check and perhaps change the activation/deactivation status of packages using the menu items in the File menu.";
		}
		if (rec != null)

			if (rec.getModuleNotFoundWarning()) {
				if (!AlertDialog.query(module.containerOfModule(), "Module not found", message, "OK", "Suppress similar warnings")){
					rec.setModuleNotFoundWarning(false);
				}
			}
		module.logln("\nMODULE REQUESTED BY COMMAND NOT FOUND\n" + message + "\n");
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employee the first MesquiteModule found that is instance of the given class and that has given name (name should be first token of arguments passed!). If name passed is null then: if scripting, the first MesquiteModule found of the class is employed; if not, and an explanation is given, the user is queried; if no explanation is given, the first MesquiteModule found of the class is employed. Once the employement is done, the previous employee is fired.
	 */
	public MesquiteModule replaceEmployee(Class dutyClass, String arguments, String explanation, MesquiteModule oldEmployee) {
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.24) replaceEmployee ( Class dutyClass, String arguments, String explanation, MesquiteModule oldEmployee)");
		return replace(dutyClass, arguments, explanation, null, oldEmployee);
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employee the first MesquiteModule found that is instance of the given class and that claims compatibility with given object. First token of arguments is name of module. If name is null, first MesquiteModule found of the class is employeed. Once the employement is done, the previous employee is fired.
	 */
	public MesquiteModule replaceCompatibleEmployee(Class dutyClass, String arguments, MesquiteModule oldEmployee, Object obj) {
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.32) replaceCompatibleEmployee ( Class dutyClass, String arguments, MesquiteModule oldEmployee, Object obj)");
		return replace(dutyClass, arguments, null, obj, oldEmployee);
	}

	/* ................................................................................................................. */
	/**
	 * Hires as employee the next MesquiteModule beyond the current that is instance of the given class. Once the employement is done, the previous employee is fired.
	 */
	public MesquiteModule replaceEmployeeWithNext(Class dutyClass, MesquiteModule oldEmployee) {
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.30) replaceEmployeeWithNext( Class dutyClass, MesquiteModule oldEmployee)");
		if (MesquiteTrunk.mesquiteModulesInfoVector == null)
			return null;
		MesquiteModuleInfo c = null;
		if (oldEmployee != null)
			c = oldEmployee.moduleInfo;
		c = MesquiteTrunk.mesquiteModulesInfoVector.findNextModule(dutyClass, c);
		if (showHiringPath)
			MesquiteMessage.warnProgrammer("(HIRING.31) module found " + whichModInfo(c));

		if (c != null) {
			incrementMenuResetSuppression();
			MesquiteModule mb = hire(c, dutyClass, null, null, null);
			if (mb == null) {
				decrementMenuResetSuppression();
				return null; // should get next module with dutyclass
			}
			if (oldEmployee != null)
				fireEmployee(oldEmployee);
			resetContainingMenuBar();
			MesquiteTrunk.mesquiteTrunk.refreshBrowser(MesquiteModule.class);
			decrementMenuResetSuppression();
			return mb;
		}
		else {
			return null;
		}
	}

	/* ................................................................................................................. */
	/**
	 * Intended to be used by the employer to pass its command for hiring a replacement to this module, so that Mesquite can use it to hire a replacement if this module quits
	 */
	public final void setHiringCommand(MesquiteCommand hc) {
		hiringCommand = hc;
	}

	/* ................................................................................................................. */
	/** Returns the command for hiring a replacement to this module */
	public final MesquiteCommand getHiringCommand() {
		if (doesAnEmployerSuppressAutoRehiring())
			return null;
		return hiringCommand;
	}

	/* ................................................................................................................. */
	/**
	 * Before rehiring done, look into employer chain to seed if any request no rehiring
	 */
	boolean suppressAutoRehireInEmployeeTree = false;
	public final void setSuppressEmployeeAutoRehiring(boolean s) {
		suppressAutoRehireInEmployeeTree = s;
	}
	public final boolean getSuppressEmployeeAutoRehiring() {
		return suppressAutoRehireInEmployeeTree;
	}
	public final boolean doesAnEmployerSuppressAutoRehiring() {
		if (employer != null){
			if (employer.suppressAutoRehireInEmployeeTree)
				return true;
			return employer.doesAnEmployerSuppressAutoRehiring();
		}
		return false;
	}
	/* ................................................................................................................. */
	/**
	 * returns whether this module has an employee hired for the given dutyClass and having the given MesquiteModuleInfo. For use by hireAllOtherEmployees
	 */
	private boolean employeeExists(MesquiteModuleInfo mbi, Class dutyClass) {
		if (employees == null)
			return false;
		Enumeration enumeration = employees.elements();
		while (enumeration.hasMoreElements()) {
			MesquiteModule mb = (MesquiteModule) enumeration.nextElement();
			if (mb.getHiredAs() == dutyClass && mb.getName().equals(mbi.getName())) {
				return true;
			}
		}
		return false;
	}

	/* ................................................................................................................. */
	/** Returns command to hire employee if clonable */
	public String getClonableEmployeeCommand(MesquiteModule employee) {
		return null;
	}

	/* ................................................................................................................. */
	/** Clones employee if possible */
	public  Object cloneEmployee(MesquiteModule employee) {
		if (employee == null)
			return null;
		String cloneCommand = getClonableEmployeeCommand(employee);
		if (!StringUtil.blank(cloneCommand)){
			cloneCommand += "\ntell It;\n";
			cloneCommand += Snapshot.getSnapshotCommands(employee, null, "");
			cloneCommand += "\nendTell;";
			Puppeteer p = new Puppeteer(module);
			MesquiteInteger pos = new MesquiteInteger(0);
			CommandRecord previous = MesquiteThread.getCurrentCommandRecord();
			CommandRecord record = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(record);
			MesquiteModule.incrementMenuResetSuppression();	
			Object obj = p.sendCommands(this, cloneCommand, pos, "", false, null,CommandChecker.defaultChecker);
			MesquiteModule.decrementMenuResetSuppression();	
			MesquiteModule cloned = null;
			if (obj != null){
				MesquiteWindow w = null;
				if (obj instanceof MesquiteWindow){
					w = (MesquiteWindow)obj;
					cloned = w.getOwnerModule();
				}
				else if (obj instanceof MesquiteModule){
					w = ((MesquiteModule)obj).getModuleWindow(); // Not all employees may have a window, so this may return null
					cloned = ((MesquiteModule)obj);
				}
				if (w!=null) {
					w.doCommand("setLocation", Integer.toString(w.getLocation().x + 20) + " " + (w.getLocation().y + 20),CommandChecker.defaultChecker);
					w.getParentFrame().showPage(Integer.toString(w.getID())); // Only do this if w != null
				}

			}
			MesquiteThread.setCurrentCommandRecord(previous);
			return cloned;
		}
		return null;
	}
	
	/* ................................................................................................................. */
	/** Clones employee if possible */
	public synchronized Object synchronizedCloneEmployee(MesquiteModule employee) {
		synchronized(this) {
			return cloneEmployee(employee);
		}
	}
	
	private String whichModInfo(MesquiteModuleInfo mbi) {
		if (mbi == null)
			return "NULL";
		else
			return mbi.getName();
	}

	/* ................................................................................................................. */
	/**
	 * Returns whether or not in creating macros automatically, this module is an anchor to which macros by default will be inherited. At present (April 02) this will probably include only the tree window module and the project coordinator module.
	 */
	public boolean isMacroAnchor() {
		return false;
	}

	/* ................................................................................................................. */
	/**
	 * Returns whether or not in creating macros automatically, this module is an anchor to which macros by default will be inherited. At present (April 02) this will probably include only the tree window module and the project coordinator module.* public MesquiteModule getMacroAnchor(){ return false; } /*.................................................................................................................
	 */
	/** Returns number of different MesquiteModule classes (modules) that serve given duty (i.e., instances of given class). */
	public int numModulesAvailable(Class dutyClass) {
		return numCompatibleModulesAvailable(dutyClass, null, module);
		/*
		 * if (MesquiteTrunk.mesquiteModulesInfoVector==null) return 0; int count=0; Enumeration enumeration=MesquiteTrunk.mesquiteModulesInfoVector.elements(); MesquiteModuleInfo mbi; while (enumeration.hasMoreElements()){ Object obj = enumeration.nextElement(); mbi = (MesquiteModuleInfo)obj; if (mbi.doesDuty(dutyClass)) { count++; } } return count;
		 */
	}

	/* ................................................................................................................. */
	/** Returns number of different MesquiteModule classes (modules) that serve given duty (i.e., instances of given class). */
	public int numCompatibleModulesAvailable(Class dutyClass, Object obj, MesquiteModule prospectiveEmployer) {
		if (MesquiteTrunk.mesquiteModulesInfoVector == null)
			return 0;
		int count = 0;
		MesquiteProject proj = null;
		if (prospectiveEmployer != null)
			proj = prospectiveEmployer.getProject();
		Enumeration enumeration = MesquiteTrunk.mesquiteModulesInfoVector.elements();
		MesquiteModuleInfo mbi;
		while (enumeration.hasMoreElements()) {
			mbi = (MesquiteModuleInfo) enumeration.nextElement();
			if (mbi.doesDuty(dutyClass) && mbi.isCompatible(obj, proj, prospectiveEmployer)) {
				count++;
			}
		}
		return count;
	}

	/* ................................................................................................................. */
	/**
	 * Finds next employee of this MesquiteModule that is an instance of given class. Passed the previous employee found; if passed null then looks for first such employee.
	 */
	public MesquiteModule getNextEmployeeWithDuty(MesquiteModule last, Class dutyClass) {
		boolean found = (last == null);
		Enumeration e = employees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mbe = (MesquiteModule) obj;
			if (!found) {
				if (mbe == last)
					found = true;
			}
			else if (dutyClass.isInstance(mbe))
				return mbe;
		}
		return null;
	}

	/* ................................................................................................................. */
	/** Fires given employee. */
	public void fireEmployee(MesquiteModule mb) {
		if (mb != null) {
			mb.doomAll();
			mb.endJob();
			mb.dispose();
		}
		// mb.employer.employees.removeElement(mb);
	}
	/* ................................................................................................................. */
	/** Moves first employee to be just after second; if second is null, moves to start. */
	public void moveEmployeeAfter(MesquiteModule toBeMoved, MesquiteModule targetBefore) {
		if (toBeMoved == null)
			return;
		int mover = employees.indexOf(toBeMoved);
		if (mover <0)
			return;

		int target = 0;
		if (targetBefore != null) {
			target = employees.indexOf(targetBefore);
			if (target <0)
				return;
			target++; //to come after
		}
		employees.removeElement(toBeMoved, false);
		if (mover < target)
			target--;
		employees.insertElementAt(toBeMoved, target, false);
	}

	/* ................................................................................................................. */
	/** Fires all employees. */
	public void closeDownAllEmployees(MesquiteModule mb) {
	//	Debugg.println("@@@@@@@@@@closedownalleemployees " + this + " EMPLOYEES " + employees);
		if (mb.employees == null)
			return;
	//	Debugg.println("      @@@@@@@" + this + " EMPLOYEES " + employees.size());

		Enumeration e = mb.employees.elements( );
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mbe = (MesquiteModule) obj;
			if (mbe != null) {
				try {
					//Debugg.println("@@@@@@@@@@ENding job " + mbe);
					mbe.endJob();
				} catch (Throwable t) {
					module.logln("Error or exception in closing down employee (endJob) " + mbe.getName());
					t.printStackTrace();
					MesquiteFile.throwableToLog(this, t);
				}
				try {
					mbe.dispose();
				} catch (Throwable t) {
					module.logln("Error or exception in closing down employee (dispose) " + mbe.getName());
					MesquiteFile.throwableToLog(this, t);
				}
			}
		}
	}

	/* ................................................................................................................. */
	protected void internalStartUp() {
	}

	/* ................................................................................................................. */
	/** refreshes and employee tree window -- used both for the Active Module Tree and in each window */
	public void refreshBrowser(Class c) {
		if (module == null || doomed || !(MesquiteModule.class == c || MesquiteModule.class.isAssignableFrom(c))) {
			return;
		}
		if (employeeBrowserSuppression > 0) {
			employeeBrowserRefreshPending = true;
		}
		else {
			if (module.getModuleWindow() != null) {
				MesquiteWindow w = module.getModuleWindow();
				if (w.getMode() == InfoBar.EMPLOYEE_TREE) {
					w.updateEmployeeTree();
				}
			}
		}
		if (employees == null)
			return;
		Enumeration e = employees.elements();
		while (e.hasMoreElements()) {
			Object obj = e.nextElement();
			MesquiteModule mbe = (MesquiteModule) obj;
			mbe.refreshBrowser(c);
		}
	}

	int employeeBrowserSuppression = 0;

	boolean employeeBrowserRefreshPending = false;

	/* ................................................................................................................. */
	/** Indicates whether any employers have browser refresh suspended. */
	private final boolean employerBrowserRefreshSuspended() {
		return false;
		/*
		 * if (employer == null) return false; if (employer.employeeBrowserSuppression>0 || employer.isDoomed()) return true; return employer.employerBrowserRefreshSuspended();
		 */
	}

	/* ................................................................................................................. */
	/** Increments suppression level of hierarchy views (e.g., employee tree); if 0 then can be repainted. */
	public final void incrementEmployeeBrowserRefreshSuppression(Class c) {
		employeeBrowserSuppression++;
	}

	/* ................................................................................................................. */
	/** Decrements suppression level of hierarchy views (e.g., employee tree); if 0 then can be repainted. */
	public final void decrementEmployeeBrowserRefreshSuppression(Class c) {
		if (employeeBrowserSuppression == 0) {
			MesquiteMessage.warnProgrammer("decrementBrowserResetSuppression when already zero");
			return;
		}

		employeeBrowserSuppression--;
		if (employeeBrowserSuppression < 0)
			employeeBrowserSuppression = 0;
		if (employeeBrowserSuppression == 0) { // menu suppression just removed and requests pending; reset menus
			if (employeeBrowserRefreshPending && !employerBrowserRefreshSuspended()) {
				refreshBrowser(c);
			}
			employeeBrowserRefreshPending = false;
		}
	}
}

