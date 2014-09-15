/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 
. 

Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.diverse.LineagesThroughTime;

import java.awt.*;

import mesquite.lib.*;
import mesquite.lib.duties.*;

/** ======================================================================== */
public class LineagesThroughTime extends TreeWindowAssistantA {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e = registerEmployeeNeed(DrawChart.class, getName() + "  needs a module to draw charts.",
		"The module to draw charts is arranged automatically");
	}
	/*.................................................................................................................*/
	LineagesWindow lineagesWindow;
	Tree tree;

	public DrawChart charterTask;
	int numTrees = MesquiteInteger.unassigned;

	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 200;  
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return false;
	}
	/*.................................................................................................................*/
	public boolean showCitation(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		charterTask = (DrawChart)hireNamedEmployee(DrawChart.class, "Histogram", new MesquiteBoolean[]
		      {new MesquiteBoolean("showMenus", false), 
			new MesquiteBoolean("showAsBarChart", false), 
			new MesquiteBoolean("setNoSum", true), 
			new MesquiteBoolean("setnativeMode", true)});
		if (charterTask == null)
			return sorry(getName() + " couldn't start because no charting module was obtained.");
		makeMenu("Lineages");
		lineagesWindow= new LineagesWindow( this);
		setModuleWindow(lineagesWindow);
		lineagesWindow.setVisible(true);
		resetContainingMenuBar();
		resetAllWindowsMenus();

		return true;
	}
	public  void setTree(Tree tree){
		this.tree = tree;
		if (lineagesWindow != null) {
			lineagesWindow.setTree(tree);
			lineagesWindow.renew();
		}
	}
	public String getTreeWindow(){
		if (getEmployer() instanceof TreeWindowMaker){
		MesquiteWindow mw = getEmployer().containerOfModule();
		return mw.getTitle();
		}
		return " in tree window";
		
	}
	public void employeeQuit(MesquiteModule m){
		if (m == charterTask)
			iQuit();
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		if ((lineagesWindow!=null) ) 
			lineagesWindow.renew();
	}
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) {
		if (lineagesWindow ==null)
			return null;
		Snapshot fromWindow = lineagesWindow.getSnapshot(file);
		Snapshot temp = new Snapshot();

		temp.addLine("makeWindow");
		temp.addLine("getWindow");
		temp.addLine("tell It");
		temp.incorporate(fromWindow, true);
		temp.addLine("endTell");
		temp.addLine("showWindow");
		return temp;
	}
	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Makes but doesn't show the window", null, commandName, "makeWindow")) {
			if (getModuleWindow()==null) {
				lineagesWindow= new LineagesWindow( this);
				setModuleWindow(lineagesWindow);
				resetContainingMenuBar();
				resetAllWindowsMenus();
			}
			return lineagesWindow;
		}
		else if (checker.compare(this.getClass(), "Shows the window", null, commandName, "showWindow")) {
			if (lineagesWindow!=null)
				lineagesWindow.setVisible(true);
			return lineagesWindow;
		}

		else
			return  super.doCommand(commandName, arguments, checker);


	}
	/*.................................................................................................................*/
	public String getName() {
		return "Lineages Through Time";
	}
	/*.................................................................................................................*/
	public void windowGoAway(MesquiteWindow whichWindow) {
		whichWindow.hide();
		whichWindow.dispose();
		iQuit();
	}
	/*.................................................................................................................*/
	public String getVersion() {
		return null;
	}

	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Displays a window showing plot of lineages through time averaged over many trees." ;
	}
}

/** ======================================================================== */
class LineagesWindow extends MesquiteWindow implements ChartListener  {
	LineagesThroughTime MTWmodule;

	int totalWidth;
	int totalHeight;
	MessagePanel messagePanel;
	MesquiteChart chart;
	Tree tree;
	private NumberArray valuesX, valuesY, valuesZ;
	LineagesThroughTime ownerModule;
	public LineagesWindow (LineagesThroughTime ownerModule){
		super(ownerModule, true); //infobar
		this.ownerModule = ownerModule;
		setWindowSize(500,400);
		valuesX = new NumberArray(0);
		valuesY = new NumberArray(0);
		MTWmodule=ownerModule;
		
		setBackground(Color.white);

		messagePanel=new MessagePanel(getColorScheme());
		addToWindow(messagePanel);
		messagePanel.setVisible(true);

		chart=new MesquiteChart(ownerModule, 100, 0, ownerModule.charterTask.createCharter(this));
		chart.deassignChart();
		chart.setUseAverage(true);
		addToWindow(chart);
		//plot.setVisible(true);
		sizeDisplays(false);
		resetTitle();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Lineages Through Time (from " + ownerModule.getTreeWindow() + ")"); //TODO: what tree?
	}
	/*...................................................................................................................*/
	public void pointMouseUp(MesquiteChart chart, int whichPoint, int x, int y, int modifiers, String message){
	}
	public void pointMouseDown(MesquiteChart chart, int whichPoint, MesquiteNumber valueX, MesquiteNumber valueY, int x, int y, int modifiers, String message){
	}
	/*.................................................................................................................*/
	public void setTree(Tree tree){
		this.tree = tree;
	}
	public void renew() {
		doCalcs();
	}
	/*.................................................................................................................*/
	public void setWindowSize(int width, int height){
		super.setWindowSize(width,height);
		sizeDisplays(false);
	}
	/*.................................................................................................................*/
	public void sizeDisplays(boolean hide){
		if (messagePanel == null)
			return;
		totalWidth = getWidth()-16;
		totalHeight = getHeight() - 16;
		chart.setBounds(0,0, totalWidth, totalHeight);
		chart.setLocation(32, 32);
		chart.setChartSize(getWidth()-64, getHeight()-64);
		messagePanel.setSize(totalWidth, 16);
		messagePanel.setLocation(0, totalHeight);
		messagePanel.repaint();
	}
	/*.................................................................................................................*/
	public void doCalcs(){
		if (tree == null)
			return;
		final int numTaxa = tree.getTaxa().getNumTaxa();
		valuesX.resetSize(numTaxa);  
		valuesY.resetSize(numTaxa); 
		valuesX.deassignArray();
		valuesY.deassignArray();
		MesquiteInteger count = new MesquiteInteger(0); 
		double[] splits = new double[numTaxa];
		double[] balances = new double[numTaxa];
		int point = 0;

		CommandRecord.tick("Assessing lineage plot for tree ");
		if (tree!=null && tree.allLengthsAssigned(false)) {
			count.setValue(0);
			double height = tree.tallestPathAboveNode(tree.getRoot());
			if (height>0){
				DoubleArray.zeroArray(splits);
				if (true){
					getSplits(tree, tree.getRoot(), splits, 0.0, count);
					DoubleArray.sort(splits);
					int c = 1;
					for (int i = 0; i < splits.length; i++) {
						if (splits[i]>0){
							c++;
							valuesX.setValue(point, splits[i]/height);
							valuesY.setValue(point, Math.log(c));
							point++;
						}
					}
				}
				else {
					DoubleArray.zeroArray(balances);
					getSplitsAndBalances(tree, tree.getRoot(), splits, balances, 0.0, count);
					DoubleArray.sortByFirst(splits, balances);
					for (int i = 0; i < splits.length; i++) {
						if (MesquiteDouble.isCombinable(balances[i])){
							valuesX.setValue(point, splits[i]/height);
							valuesY.setValue(point++, balances[i]);
						}
					}
				}
			}
		}

		recalcChart();
	}
	void getSplits(Tree tree, int node, double[] splits, double distanceFromRoot, MesquiteInteger count){
		if (tree.nodeIsInternal(node)) { 
			if (tree.getRoot() != node && tree.numberOfDaughtersOfNode(node)>1) {
				distanceFromRoot += tree.getBranchLength(node);
				splits[count.getValue()] = distanceFromRoot;
				count.increment();
			}
			else if (tree.getRoot() != node)
				distanceFromRoot += tree.getBranchLength(node);

			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				getSplits(tree, daughter, splits, distanceFromRoot, count);
			}
		}
	}
	void getSplitsAndBalances(Tree tree, int node, double[] splits, double[] balances, double distanceFromRoot, MesquiteInteger count){
		if (tree.nodeIsInternal(node)) { 
			if (tree.getRoot() != node && tree.numberOfDaughtersOfNode(node)>1) {
				distanceFromRoot += tree.getBranchLength(node, 1.0);
				splits[count.getValue()] = distanceFromRoot;
				balances[count.getValue()] = balance(tree, node);
				count.increment();
			}
			else if (tree.numberOfDaughtersOfNode(node)>1) {
				splits[count.getValue()] = 0;
				balances[count.getValue()] = balance(tree, node);
				count.increment();
			}
			else if (tree.getRoot() != node)
				distanceFromRoot += tree.getBranchLength(node, 1.0);
			for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
				getSplitsAndBalances(tree, daughter, splits, balances, distanceFromRoot, count);
			}
		}
	}
	double balance(Tree tree, int node){
		final int numLeft =tree.numberOfTerminalsInClade(tree.firstDaughterOfNode(node));
		final int numRight =tree.numberOfTerminalsInClade(tree.lastDaughterOfNode(node));
		if ((numLeft+numRight)/2*2 == numLeft + numRight)
			return MesquiteDouble.unassigned;
		else if (numRight>numLeft)
			return (1.0*numLeft/(numRight + numLeft));
		else
			return (1.0*numRight/(numRight + numLeft));
	}
	/*.................................................................................................................*/
	public void recalcChart(){
		chart.deassignChart();
		chart.getCharter().setShowNames(true);
		if (tree == null){
		    chart.setXAxisName("Length from Root");
		    chart.setYAxisName("Log of Number of Lineages");
		}
		else {
			chart.setXAxisName("Length from Root (tree: " + tree.getName() + ")");
			chart.setYAxisName("Log of Number of Lineages (tree: " + tree.getName() + ")");
		}
		final MesquiteNumber resultX = new MesquiteNumber();
		final MesquiteNumber resultY = new MesquiteNumber();

		for (int i=0; i<valuesX.getSize(); i++) {
			valuesX.placeValue(i, resultX);
			valuesY.placeValue(i, resultY);
			chart.addPoint(resultX, resultY);
		}
		chart.munch();
	}
	/*.................................................................................................................*/
	public void windowResized() {
		if (MesquiteWindow.checkDoomed(this))
			return;
		sizeDisplays(false);

		MesquiteWindow.uncheckDoomed(this);
	}
}

class LinPanel extends MesquitePanel {
	LineagesWindow window;
	public LinPanel (LineagesWindow window) {
		this.window = window;
		setBackground(Color.blue);
	}
}
