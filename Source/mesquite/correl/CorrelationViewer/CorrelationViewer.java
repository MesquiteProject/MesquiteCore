/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.correl.CorrelationViewer;
/*~~  */

import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterDistribution;
import mesquite.lib.characters.CharacterStates;
import mesquite.lib.duties.*;
import mesquite.correl.lib.*;


public class CorrelationViewer extends TreeWindowAssistantA implements CLogger   {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(NumberFor2CharAndTree.class, getName() + "  needs a method to calculate correlation.",
		"The method to calculate correlation can be selected initially or in the Correlation Test submenu");
		EmployeeNeed ew = registerEmployeeNeed(CharSourceCoordObed.class, getName() + "  needs a source of characters.",
		"The source of characters is arranged initially");
	}
	/*.................................................................................................................*/
	int currentX = 0;
	int currentY = 1;
	Tree tree;
	NumberFor2CharAndTree numberTask;
	CharSourceCoordObed characterSourceTaskX, characterSourceTaskY;
	Taxa taxa;
	MesquiteString numberTaskName;
	MesquiteCommand ntC;
	Class stateClass;
	MesquiteWindow containingWindow;
	CorrelPanel panel;
	boolean askedForXY = false;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		numberTask = (NumberFor2CharAndTree)hireEmployee(NumberFor2CharAndTree.class, "Correlation analysis");
		if (numberTask == null)
			return sorry(getName() + " couldn't start because no calculator module obtained.");
		makeMenu("Correlation");

		ntC =makeCommand("setCorrelationCalculator",  this);
		numberTask.setHiringCommand(ntC);
		numberTaskName = new MesquiteString();
		numberTaskName.setValue(numberTask.getName());
		if (numModulesAvailable(NumberFor2CharAndTree.class)>1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Correlation Test", ntC, NumberFor2CharAndTree.class);
			mss.setSelected(numberTaskName);
		}

		characterSourceTaskX = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, numberTask.getCompatibilityTest(), "Source of characters (independent variable X for correlation analysis)");
		if (characterSourceTaskX == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");

		characterSourceTaskY = (CharSourceCoordObed)hireCompatibleEmployee(CharSourceCoordObed.class, numberTask.getCompatibilityTest(), "Source of characters (dependent variable Y for correlation analysis)");
		if (characterSourceTaskY == null)
			return sorry(getName() + " couldn't start because no source of characters was obtained.");

		MesquiteWindow f = containerOfModule();
		if (f instanceof MesquiteWindow){
			containingWindow = (MesquiteWindow)f;
			containingWindow.addSidePanel(panel = new CorrelPanel(), 200);
		}
		
		addMenuItem( "Choose Both Characters...", makeCommand("chooseBoth",  this));
		addMenuItem( "Choose Character X...", makeCommand("chooseX",  this));
		addMenuItem( "Choose Character Y...", makeCommand("chooseY",  this));
		addMenuItem( "Re-run analysis", makeCommand("rerun",  this));
		addMenuItem( "Close Correlation Analysis", makeCommand("close",  this));
		addMenuSeparator();

		return true;
	}
	/* ................................................................................................................. */
	/** Returns the purpose for which the employee was hired (e.g., "to reconstruct ancestral states" or "for X axis"). */
	public String purposeOfEmployee(MesquiteModule employee) {
		if (employee ==  characterSourceTaskX)
			return "for X variable, correlation analysis"; 
		if (employee ==  characterSourceTaskY)
			return "for Y variable, correlation analysis"; 
		return super.purposeOfEmployee(employee);
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
	public void fileReadIn(MesquiteFile file){
		if (panel != null)
			panel.resetBounds();
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	public void cwriteln(String s){
		panel.append(s + "\n");
	}
	public void cwrite(String s){
		panel.append(s);
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
		temp.addLine("setCorrelationCalculator ", numberTask); 
		temp.addLine("setCharSourceX ", characterSourceTaskX); 
		temp.addLine("setCharSourceY ", characterSourceTaskY); 
		temp.addLine("setX " + CharacterStates.toInternal(currentX)); 
		temp.addLine("setY " + CharacterStates.toInternal(currentY)); 
		temp.addLine("doCounts");

		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {

		if (checker.compare(this.getClass(), "Sets the module that calculates correlation", "[name of module]", commandName, "setCorrelationCalculator")) {
			NumberFor2CharAndTree temp =  (NumberFor2CharAndTree)replaceEmployee(NumberFor2CharAndTree.class, arguments, "Correlation calculator", numberTask);
			if (temp!=null) {
				CompatibilityTest test = temp.getCompatibilityTest();
				if (test != null && stateClass != null){
					if (!test.isCompatible(stateClass, getProject(), this)){
						discreetAlert( "The character correlation module \"" +temp.getName() + "\" is not compatible with the current data type");
						return null;
					}
				}
				numberTask = temp;
				numberTask.setHiringCommand(ntC);
				numberTaskName.setValue(numberTask.getName());
				if (!MesquiteThread.isScripting())
					doCounts();
				return numberTask;
			}
		}
		else if (checker.compare(this.getClass(), "Returns employee", null, commandName, "getCorrelationCalculator")) {
			return numberTask;
		}
		else   	if (checker.compare(this.getClass(), "Returns employee", null, commandName, "getCharSource")) {
			return characterSourceTaskX;
		}
		else   	if (checker.compare(this.getClass(), "Returns employee", null, commandName, "getCharSourceX")) {
			return characterSourceTaskX;
		}
		else   	if (checker.compare(this.getClass(), "Returns employee", null, commandName, "getCharSourceY")) {
			return characterSourceTaskY;
		}
		else   	if (checker.compare(this.getClass(), "Returns name of character X", null, commandName, "getCharNameX")) {
			if (characterSourceTaskX == null)
				return null;
			else
				return characterSourceTaskX.getCharacterName(taxa, currentX);
		}
		else   	if (checker.compare(this.getClass(), "Returns name of character Y", null, commandName, "getCharNameY")) {
			if (characterSourceTaskY == null)
				return null;
			else
				return characterSourceTaskY.getCharacterName(taxa, currentY);
		}
		else   	if (checker.compare(this.getClass(), "Sets character source", null, commandName, "setCharSource")) {
			CharSourceCoordObed temp  = (CharSourceCoordObed)replaceCompatibleEmployee(CharSourceCoordObed.class, arguments, characterSourceTaskX, numberTask.getCompatibilityTest());
			if (temp!=null) {
				characterSourceTaskX = temp;
				characterSourceTaskY = temp;  //assuming this is an old script, using the same character source for both X and Y
			}
			return characterSourceTaskX;
		}
		else   	if (checker.compare(this.getClass(), "Sets character source for X", null, commandName, "setCharSourceX")) {
			CharSourceCoordObed temp  = (CharSourceCoordObed)replaceCompatibleEmployee(CharSourceCoordObed.class, arguments, characterSourceTaskX, numberTask.getCompatibilityTest());
			if (temp!=null) {
				characterSourceTaskX = temp;
			}
			return characterSourceTaskX;
		}
		else   	if (checker.compare(this.getClass(), "Sets character source for Y", null, commandName, "setCharSourceY")) {
			CharSourceCoordObed temp  = (CharSourceCoordObed)replaceCompatibleEmployee(CharSourceCoordObed.class, arguments, characterSourceTaskY, numberTask.getCompatibilityTest());
			if (temp!=null) {
				characterSourceTaskY = temp;
			}
			return characterSourceTaskY;
		}
		else   	if (checker.compare(this.getClass(), "Gets text in panel", null, commandName, "getText")) {
			if (panel == null)
				return null;
			else
				return panel.getText();
		}
		else   	if (checker.compare(this.getClass(), "Logs text in panel", null, commandName, "logText")) {
			if (panel != null)
				log( panel.getText());
		}
		else if (checker.compare(this.getClass(), "Queries the user about what characters to use", null, commandName, "chooseBoth")) {
			int ic=characterSourceTaskX.queryUserChoose(taxa, " (X, for Correlation analysis) ");
			boolean redo = false;
			if (MesquiteInteger.isCombinable(ic)) {
				currentX = ic;
				redo = true;
			}
			ic=characterSourceTaskY.queryUserChoose(taxa, " (Y, for Correlation analysis) ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentY = ic;
				redo = true;
			}
			askedForXY = true;
			if (redo)
				doCounts();
		}
		else if (checker.compare(this.getClass(), "Queries the user about what character to use", null, commandName, "chooseX")) {
			int ic=characterSourceTaskX.queryUserChoose(taxa, " (X, for Correlation analysis) ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentX = ic;
				askedForXY = true;
				doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Sets the character to use for X", "[character number]", commandName, "setX")) {
			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			askedForXY = true;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && characterSourceTaskX.getNumberOfCharacters(taxa)==0) {
				currentX = ic;

			}
			else if ((ic>=0) && (ic<=characterSourceTaskX.getNumberOfCharacters(taxa)-1)) {
				currentX = ic;

			}
		}
		else if (checker.compare(this.getClass(), "Queries the user about what character to use", null, commandName, "chooseY")) {
			int ic=characterSourceTaskY.queryUserChoose(taxa, " (Y, for Correlation analysis) ");
			if (MesquiteInteger.isCombinable(ic)) {
				currentY = ic;
				askedForXY = true;
				doCounts();
			}
		}
		else if (checker.compare(this.getClass(), "Forces a rerunning", null, commandName, "rerun")) {
				doCounts();
		}
		else if (checker.compare(this.getClass(), "Sets the character to use for X", "[character number]", commandName, "setY")) {
			int icNum = MesquiteInteger.fromFirstToken(arguments, stringPos);
			if (!MesquiteInteger.isCombinable(icNum))
				return null;
			int ic = CharacterStates.toInternal(icNum);
			if ((ic>=0) && characterSourceTaskY.getNumberOfCharacters(taxa)==0) {
				currentY = ic;
			}
			else if ((ic>=0) && (ic<=characterSourceTaskY.getNumberOfCharacters(taxa)-1)) {
				currentY = ic;
			}
			askedForXY = true;
		}
		else if (checker.compare(this.getClass(), "Provokes Calculation", null, commandName, "doCounts")) {
			doCounts();
			if (panel != null){
				panel.invalidate();
				panel.validate();
				panel.doLayout();
				panel.repaint();
			}
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
	/*.................................................................................................................*/
	public void doCounts() {
		if (taxa == null || panel == null)
			return;
		if (!askedForXY && !MesquiteThread.isScripting()){
			int ic=characterSourceTaskX.queryUserChoose(taxa, " (X, for Correlation analysis) ");
			if (MesquiteInteger.isCombinable(ic))
				currentX = ic;
			ic=characterSourceTaskY.queryUserChoose(taxa, " (Y, for Correlation analysis) ");
			if (MesquiteInteger.isCombinable(ic))
				currentY = ic;
			askedForXY = true;
		}

		CharacterDistribution statesX = characterSourceTaskX.getCharacter(taxa, currentX);
		CharacterDistribution statesY = characterSourceTaskY.getCharacter(taxa, currentY);
		if (statesX == null || statesY == null)
			return;
		stateClass = statesX.getStateClass();
		MesquiteNumber result = new MesquiteNumber();
		MesquiteString rs = new MesquiteString();
//		window.setText("");
		panel.setStatus(true);
		panel.repaint();
		panel.setText("\nX: " + characterSourceTaskX.getCharacterName(taxa, currentX) +  "\n");
		panel.append("Y: " + characterSourceTaskY.getCharacterName(taxa, currentY) +  "\n");
		panel.append("Calculation: " + numberTask.getNameAndParameters() + "\n");
		if (statesX == null || statesY == null)
			rs.setValue("Sorry, one or both of the characters was not obtained.  The correlation analysis could not be completed.");
		/*		else if (!(statesX instanceof CategoricalDistribution) ||!(statesY instanceof CategoricalDistribution) ||  statesX.getStateClass() != CategoricalState.class || statesY.getStateClass() != CategoricalState.class)
			rs.setValue("Sorry, one or both of the characters is not a standard non-molecular categorical character.  The correlation analysis could not be completed.");
		else if (((CategoricalDistribution)statesX).getMaxState() >1 || ((CategoricalDistribution)statesY).getMaxState() >1)
			rs.setValue("Sorry, both characters need to be binary (states 0 and 1 only).  The correlation analysis could not be completed.");
		else if (((CategoricalDistribution)statesX).getMaxState()==-1 || ((CategoricalDistribution)statesY).getMaxState() ==-1)
			rs.setValue("Sorry, one or both of the characters seems to have no states or corresponds to a non-existent column in a character matrix.  The correlation analysis could not be completed.");
		 */		else 
			 numberTask.calculateNumber(tree, statesX, statesY, result, rs);
		saveLastResult(result);
		saveLastResultString(rs);
		panel.setStatus(false);
		panel.repaint();
//		window.append("\n\n  " + rs);
		panel.append("\n\n" + rs);
		panel.repaint();
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Correlation Analysis";
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Performs a Correlation analysis for categorical characters." ;
	}
	public void endJob() {
		if (panel != null && containingWindow != null)
			containingWindow.removeSidePanel(panel);
		super.endJob();
	}
}

class CorrelPanel extends MousePanel{
	TextArea text;
	Font df = new Font("Dialog", Font.BOLD, 14);
	boolean calculating = false;
	public CorrelPanel(){
		super();
		text = new TextArea(" ", 50, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
		setLayout(null);
		add(text);
		text.setLocation(0,26);
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
	public String getText(){
		return text.getText();
	}
	public void append(String t){
		text.append(t);
		text.repaint();
	}
	public void setSize(int w, int h){
		super.setSize(w, h);
		text.setSize(w, h-26);
		resetBounds();
	}
	public void setBounds(int x, int y, int w, int h){
		super.setBounds(x, y, w, h);
		text.setSize(w, h-26);
		resetBounds();
	}
	
	public void doLayout(){
		resetBounds();
	}
	public synchronized void resetBounds(){
		text.setBounds(0, 26, getWidth()-1, getHeight()-26);  //just to force! workaroudn for problem on os x
		text.invalidate();
		text.validate();
		text.doLayout();
		text.setBounds(0, 26, getWidth(), getHeight()-26);
		text.invalidate();
		text.validate();
		text.doLayout();
		text.repaint();
	}
	public void paint(Graphics g){
		g.setFont(df);

		if (!calculating){
			g.setColor(Color.white);
			g.drawString("Correlation Analysis", 8, 20);
		}
		else{
			g.setColor(Color.black);
			g.fillRect(0,0, getBounds().width, 50);
			g.setColor(Color.red);
			g.drawString("Correlation: Calculating", 8, 20);
		}
	}
}

