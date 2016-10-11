/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.CharIndepDivViewer;
/*~~  */

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.diverse.lib.*;


public class CharIndepDivViewer extends TreeWindowAssistantA    {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberForTreeDivers.class, getName() + "  needs a method to calculate diversification statistics.",
		"The method is arranged initially");
		e.setPriority(2);
	}
	/*.................................................................................................................*/
	int current = 0;
	Tree tree;
	NumberForTreeDivers numberTask;
	Taxa taxa;
	MesquiteWindow containingWindow;
	CADPanel panel;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numberTask = (NumberForTreeDivers)hireEmployee(NumberForTreeDivers.class, "Calculator of diversification statistics");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");
		makeMenu("Diversification");
		ntC =makeCommand("setCalculator",  this);
 		numberTask.setHiringCommand(ntC);
 		numberTaskName = new MesquiteString();
 		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(NumberForTreeDivers.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Diversification Measure", ntC, NumberForTreeDivers.class);
 			mss.setSelected(numberTaskName);
		}

		final MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			containingWindow = (MesquiteWindow)f;
			containingWindow.addSidePanel(panel = new CADPanel(), 200);
		}

		addMenuItem( "Close Diversification Analysis", makeCommand("close",  this));
		addMenuSeparator();

		return true;
	}


	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
	public boolean requestPrimaryChoice(){
		return true;  
	}
	/*.................................................................................................................*/
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		if (whichWindow == null)
			return;
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		temp.addLine("setCalculator ", numberTask); 
		temp.addLine("doCounts");

		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the module that calculates likelihoods", "[name of module]", commandName, "setCalculator")) {
			NumberForTreeDivers temp =  (NumberForTreeDivers)replaceEmployee(NumberForTreeDivers.class, arguments, "Diversification Measure", numberTask);
			if (temp!=null) {
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (!MesquiteThread.isScripting())
					doCounts();
				return numberTask;
			}
		}

		else if (checker.compare(this.getClass(), "Provokes Calculation", null, commandName, "doCounts")) {
			doCounts();
		}
		else if (checker.compare(this.getClass(), "Quits", null, commandName, "close")) {
			if (panel != null && containingWindow != null)
				containingWindow.removeSidePanel(panel);
			iQuit();
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}
	long oldTreeID = -1;
	long oldTreeVersion = 0;
	/*.................................................................................................................*/
	public   void setTree(Tree tree) {
		if (tree==null)
			return;
		this.tree=tree;
		taxa = tree.getTaxa();
		if ((tree.getID() != oldTreeID || tree.getVersionNumber() != oldTreeVersion) && !MesquiteThread.isScripting()) {
			doCounts();  //only do counts if tree has changed
		}
		oldTreeID = tree.getID();
		oldTreeVersion = tree.getVersionNumber();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if (numberTask!=null && !MesquiteThread.isScripting())
			doCounts();
	}
	String blankIfNull(String s){
		if (s == null)
			return "";
		return s;
	}
	/*.................................................................................................................*/
	public void doCounts() {
		if (taxa == null)
			return;
		final MesquiteNumber result = new MesquiteNumber();
		final MesquiteString rs = new MesquiteString();
//		window.setText("");
		panel.setStatus(true);
		panel.repaint();
		panel.setText("\nAnalysis of speciation and extinction using a tree (character independent)\n");
		panel.append("\nCalculation: " + numberTask.getNameAndParameters() + "\n");
		panel.append("\nTree: " + tree.getName() );
			numberTask.calculateNumber(tree, result, rs);
		panel.append("\n\n" + blankIfNull(result.getName()) + '\t' + result);
		final MesquiteNumber[] aux = result.getAuxiliaries();
		if (aux != null){
			panel.append("\n");
			for (int i = 0; i< aux.length; i++){
				panel.append('\n' + blankIfNull(aux[i].getName()) + '\t' + aux[i].toString());
			}
		}
		else
			panel.append("\n\n" + rs);
		panel.append("\n\nExplanation of calculation:\n" + numberTask.getExplanation());
		panel.setStatus(false);
		panel.repaint();
//		window.append("\n\n  " + rs);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Diversification (Char. Indep.)";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates analyses of diversification (speciation/extinction)." ;
	}
	public void endJob() {
		if (panel != null && containingWindow != null)
			containingWindow.removeSidePanel(panel);
		super.endJob();
	}
}

class CADPanel extends MousePanel{
	TextArea text;
	Font df = new Font("Dialog", Font.BOLD, 14);
	boolean calculating = false;
	public CADPanel(){
		super();
		text = new TextArea(" ", 50, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
		setLayout(null);
		add(text);
		text.setLocation(0,26);
		text.setEditable(false);
		text.setVisible(true);
		setBackground(Color.darkGray);
		text.setBackground(Color.white);
	}
	public void setStatus(boolean calculating){
		this.calculating = calculating;
	}
	public void setText(String t){
		text.setText(t);
	}
	public void append(String t){
		text.append(t);
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		text.setSize(w, h-26);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		text.setSize(w, h-26);
	}
	public void paint(Graphics g){
		g.setFont(df);

		if (!calculating){
			g.setColor(Color.white);
			g.drawString("Diversification Analysis", 8, 20);
		}
		else{
			g.setColor(Color.black);
			g.fillRect(0,0, getBounds().width, 50);
			g.setColor(Color.red);
			g.drawString("Diversification: Calculating...", 8, 20);
		}
	}
}

