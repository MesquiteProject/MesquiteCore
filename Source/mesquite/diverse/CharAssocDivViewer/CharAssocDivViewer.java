/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.CharAssocDivViewer;
/*~~  */

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.diverse.lib.*;


public class CharAssocDivViewer extends TreeWindowAssistantA    {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumForCharAndTreeDivers.class, getName() + "  needs a method to calculate diversification statistics.",
		"You can choose the diversification calculation initially or under the Diversification Measure submenu.");
		e.setPriority(2);
		EmployeeNeed ew = registerEmployeeNeed(CharSourceCoordObed.class, getName() + "  needs a source of characters.",
		"The source of characters is arranged initially");
	}
	/*.................................................................................................................*/
	int current = 0;
	Tree tree;
	NumForCharAndTreeDivers numberTask;
	CharSourceCoordObed characterSourceTask;
	Taxa taxa;
	Class stateClass;
	MesquiteWindow containingWindow;
	CADPanel panel;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	///CharSourceCoord contTask;
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numberTask = (NumForCharAndTreeDivers)hireEmployee(NumForCharAndTreeDivers.class, "Calculator of association between character and diversification");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");
		makeMenu("Diversification(Ch)");
		ntC =makeCommand("setCalculator",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(NumForCharAndTreeDivers.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Diversification Measure", ntC, NumForCharAndTreeDivers.class);
			mss.setSelected(numberTaskName);
		}

		//	contTask = (CharSourceCoord)hireCompatibleEmployee(CharSourceCoord.class, ContinuousState.class, "Source of clade state frequency data");

		characterSourceTask = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, numberTask.getCompatibilityTest(), "Source of  characters (for " + getName() + ")");
		if (characterSourceTask == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");
		final MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			containingWindow = (MesquiteWindow)f;
			containingWindow.addSidePanel(panel = new CADPanel(), 200);
		}

		addMenuItem( "Choose Character...", makeCommand("chooseCharacter",  this));
		addMenuItem( "Close Character-Associated Diversification Analysis", makeCommand("close",  this));
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
		final Snapshot temp = new Snapshot();
		temp.addLine("setCalculator ", numberTask); 
		temp.addLine("getCharSource ", characterSourceTask); 
		temp.addLine("setCharacter " + CharacterStates.toExternal(current)); 
		temp.addLine("doCounts");

		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the module that calculates diversification statistics", "[name of module]", commandName, "setCalculator")) {
			final NumForCharAndTreeDivers temp =  (NumForCharAndTreeDivers)replaceEmployee(NumForCharAndTreeDivers.class, arguments, "Diversification Measure", numberTask);
			if (temp!=null) {
					CompatibilityTest test = temp.getCompatibilityTest();
				if (!MesquiteThread.isScripting()){
					if (test != null && stateClass != null){
						if (!test.isCompatible(stateClass, getProject(), this)){
							discreetAlert( "The calculation module \"" +temp.getName() + "\" is not compatible with the current data type");
							return null;
						}
					}
					
				}
				characterSourceTask.setHiringCondition(test);
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (!MesquiteThread.isScripting())
					doCounts();
				return numberTask;
			}
		}

		else   	if (checker.compare(this.getClass(), "Returns employee", null, commandName, "getCharSource")) {
			return characterSourceTask;
		}
		else if (checker.compare(this.getClass(), "Queries the user about what character to use", null, commandName, "chooseCharacter")) {
			int ic=characterSourceTask.queryUserChoose(taxa, " Character to use for Diversification analysis ");
			if (MesquiteInteger.isCombinable(ic) && ic != current) {
				current = ic;
				doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the character to use", "[character number]", commandName, "setCharacter")) {
			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && characterSourceTask.getNumberOfCharacters(taxa)==0) {
				current = ic;

			}
			else if ((ic>=0) && (ic<=characterSourceTask.getNumberOfCharacters(taxa)-1)) {
				current = ic;

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

	public String nameForWritableResults(){
		if (numberTask == null)
			return null;
		return numberTask.getName();
	}
	public boolean suppliesWritableResults(){
		if (numberTask == null)
			return false;
		return true;
	}
	public Object getWritableResults(){
		return lastResult;
	}
	public Object getResultsHeading(){
		return lastResult;
	}
	CharacterDistribution contMatrix = null;
	/*.................................................................................................................*/
	public void doCounts() {
		if (taxa == null || panel == null)
			return;

		MesquiteNumber result = new MesquiteNumber();
		MesquiteString rs = new MesquiteString();
		CharacterDistribution states = characterSourceTask.getCharacter(taxa, current);
		if (states == null )
			rs.setValue("Sorry, no character was not obtained.  The diversification analysis could not be completed.");
		stateClass = states.getStateClass();
		//		window.setText("");
		clearLastResult();
		panel.setStatus(true);
		panel.repaint();
		panel.setText("\nAnalysis of speciation and extinction as associated with the states of a character\n");
		panel.append("\nCalculation: " + numberTask.getNameAndParameters() + "\n");
		panel.append("\nTree: " + tree.getName() );
		panel.append("\nCharacter: " + characterSourceTask.getCharacterName(taxa, current) +  "\n");
			numberTask.calculateNumber(tree, states, result, rs);
			saveLastResult(result);
			saveLastResultString(rs);
		
		panel.append("\n\n" + blankIfNull(result.getName()) + '\t' + result);
		MesquiteNumber[] aux = result.getAuxiliaries();
		if (aux != null){
			panel.append("\n");
			for (int i = 0; i< aux.length; i++){
				panel.append('\n' + blankIfNull(aux[i].getName()) + '\t' + aux[i].toString());
			}
		}
		else
			panel.append("\n\n" + rs);

		panel.append("\n\n" + rs);
		panel.append("\n\nExplanation of calculation:\n" + numberTask.getExplanation());
		panel.setStatus(false);
		panel.repaint();
		//		window.append("\n\n  " + rs);
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Character-Associated Diversification";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates analyses for the effect of a character on diversification (speciation/extinction)." ;
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
	int labelHeight = 52;
	public CADPanel(){
		super();
		text = new TextArea(" ", 50, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
		setLayout(null);
		add(text);
		text.setLocation(0,labelHeight);
		text.setVisible(true);
		text.setEditable(false);
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
		text.setSize(w, h-labelHeight);
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		text.setSize(w, h-labelHeight);
	}
	public void paint(Graphics g){
		g.setFont(df);

		if (!calculating){
			g.setColor(Color.white);
			g.drawString("Character-Associated", 8, 20);
			g.drawString("Diversification Analysis", 8, 46);
		}
		else{
			g.setColor(Color.black);
			g.fillRect(0,0, getBounds().width, 50);
			g.setColor(Color.red);
			g.drawString("Diversification Analysis", 8, 20);
			g.drawString("Calculating...", 8, 46);
		}
	}
}

