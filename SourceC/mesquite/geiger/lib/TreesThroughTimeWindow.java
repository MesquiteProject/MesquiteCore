package mesquite.geiger.lib;
import java.util.*;
import java.awt.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


/** ======================================================================== */
class LineagesWindow extends MesquiteWindow implements ChartListener  {
	TreeSource treeSourceTask;
	LineagesThroughTime MTWmodule;
	Taxa taxa;
	int totalWidth;
	int totalHeight;
	MessagePanel messagePanel;
	MesquiteChart chart;
	int numTrees;
	private NumberArray valuesX, valuesY, valuesZ;
	
	public LineagesWindow (LineagesThroughTime ownerModule, TreeSource treeSourceTask, CommandRecord commandRec){
		super(ownerModule, true); //infobar
      		setWindowSize(500,400);
		valuesX = new NumberArray(0);
		valuesY = new NumberArray(0);
  		MTWmodule=ownerModule;
		taxa = ownerModule.taxa;
		if (taxa==null) {
			taxa = ownerModule.getProject().chooseTaxa(this, "For which block of taxa do you want to show a Multi-tree window?",commandRec);
		}
		setBackground(Color.white);
		
		messagePanel=new MessagePanel(getColorScheme());
		addToWindow(messagePanel);
		messagePanel.setVisible(true);

		chart=new MesquiteChart(ownerModule, 100, 0, ownerModule.charterTask.createCharter(this), commandRec);
		chart.deassignChart();
		chart.setUseAverage(true);
		addToWindow(chart);
		//plot.setVisible(true);
		setTreeSource(treeSourceTask, commandRec);

		sizeDisplays(false);
		resetTitle();
	}
	/*.................................................................................................................*/
	/** When called the window will determine its own title.  MesquiteWindows need
	to be self-titling so that when things change (names of files, tree blocks, etc.)
	they can reset their titles properly*/
	public void resetTitle(){
		setTitle("Lineages through time"); //TODO: what tree?
	}
	/*...................................................................................................................*/
	public void pointMouseUp(MesquiteChart chart, int whichPoint, int x, int y, int modifiers, String message){
	}
	public void pointMouseDown(MesquiteChart chart, int whichPoint, MesquiteNumber valueX, MesquiteNumber valueY, int x, int y, int modifiers, String message){
	}
	/*.................................................................................................................*/
	
	public void renew(CommandRecord commandRec) {
			if (treeSourceTask!=null)
				messagePanel.setMessage("Trees from " + treeSourceTask.getNameAndParameters());
			doCalcs(commandRec);
	}
	/*.................................................................................................................*/
	
	public void setTreeSource(TreeSource tsTask, CommandRecord commandRec) {
		treeSourceTask = tsTask;
		tsTask.initialize(taxa, commandRec);
		numTrees = MTWmodule.numTrees;
		if (!MesquiteInteger.isCombinable(MTWmodule.numTrees)){
			numTrees = treeSourceTask.getNumberOfTrees(taxa, commandRec);
			if (!MesquiteInteger.isCombinable(numTrees)) {
 				numTrees = MesquiteInteger.queryInteger(this, "Number of Trees", "Number of Trees", numTrees);
				if (!MesquiteInteger.isCombinable(numTrees))
 					numTrees = 100;
 			}
 			MTWmodule.numTrees = numTrees;
		}
		messagePanel.setMessage("Trees from " + treeSourceTask.getNameAndParameters());
    	 	doCalcs(commandRec);
	}
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
	NumberArray numbersAtNodes;
	double getCladeValue(Tree tree, int node){
		
		return numbersAtNodes.getDouble(node);
	}
	/*.................................................................................................................*/
	public void doCalcs(CommandRecord commandRec){
		int numTaxa = taxa.getNumTaxa();
		valuesX.resetSize(numTrees*numTaxa);  
		valuesY.resetSize(numTrees*numTaxa); 
		valuesX.deassignArray();
		valuesY.deassignArray();
		numbersAtNodes = new NumberArray();
		MesquiteInteger count = new MesquiteInteger(0); 
		double[] splits = new double[numTaxa];
		double[] stemAge = new double[numTaxa];
		double[] nodeAge = new double[numTaxa];
		double[] disp = new double[numTaxa];
		double[] timeDisp;
		int point = 0;
		boolean doAverage = false;
		
		for (int itree=0; itree<numTrees; itree++) {
			Tree tree = treeSourceTask.getTree(taxa, itree, commandRec);
			if (numbersAtNodes.getSize() < tree.getNumNodeSpaces()) 
				numbersAtNodes = new NumberArray(tree.getNumNodeSpaces());
			else
				numbersAtNodes.deassignArray();			
			MTWmodule.numForNodesTask.calculateNumbers(tree, numbersAtNodes, null, commandRec);
			commandRec.tick("Assessing lineage plot for tree " + itree);
			if (tree!=null && tree.allLengthsAssigned(false)) {
				double height = tree.tallestPathAboveNode(tree.getRoot());
				if (height>0){
					DoubleArray.zeroArray(splits);
					DoubleArray.zeroArray(disp);
					count.setValue(0);
					getSplits(tree, tree.getRoot(), splits, 0.0, count);
					count.setValue(0);
					getStemNodeAges(tree, tree.getRoot(), stemAge, nodeAge, 0.0, count);
					count.setValue(0);
					getDisps(tree, tree.getRoot(), disp, count);
					DoubleArray.sort(splits);
					timeDisp=getTimeDispar(disp, splits, stemAge, nodeAge, numTaxa);
					int c = 1;
					for (int i = 0; i < splits.length; i++) {
						if (splits[i]>0){
							c++;
							valuesX.setValue(point, splits[i]/height);
							valuesY.setValue(point, timeDisp[i]);
							point++;
						}
					}
					
				}
			}
		}
		recalcChart(commandRec);
	}
  	void getSplits(Tree tree, int node, double[] splits, double distanceFromRoot, MesquiteInteger count){
  	 	if (tree.nodeIsInternal(node)) { 
  	 		if (tree.getRoot() != node) {
  	 			distanceFromRoot += tree.getBranchLength(node);
  	 			splits[count.getValue()] = distanceFromRoot;
  	 			count.increment();
  	 		}
	  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			getSplits(tree, daughter, splits, distanceFromRoot, count);
	   	 	}
 		}
  	}
  	void getStemNodeAges(Tree tree, int node, double[] stemAge, double[] nodeAge, double distanceFromRoot, MesquiteInteger count){
  	 	if (tree.nodeIsInternal(node)) { 
  	 		if (tree.getRoot() != node) {
  	 			stemAge[count.getValue()] = distanceFromRoot;
  	 			distanceFromRoot += tree.getBranchLength(node);
  	 			nodeAge[count.getValue()] = distanceFromRoot;
  	 			count.increment();
  	 		}
	  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			getStemNodeAges(tree, daughter, stemAge, nodeAge, distanceFromRoot, count);
	   	 	}
 		}
  	}
  	
  	void getDisps(Tree tree, int node, double[] disp, MesquiteInteger count){
  	 	if (tree.nodeIsInternal(node)) { 
  	 			disp[count.getValue()] = getCladeValue(tree, node);
  	 			count.increment();
  	 		
	  	 	for (int daughter = tree.firstDaughterOfNode(node); tree.nodeExists(daughter); daughter = tree.nextSisterOfNode(daughter)) {
	  			getDisps(tree, daughter, disp, count);
	   	 	}
 		}
  	}
  	
  	/* get_time_dispar - creates disparity-through-time plot */
  	/* returns array with average disparity of nodes alive at that time */
  	double[] getTimeDispar(double[] disp, double[] splits, double[] stemAge, double[] nodeAge, int nspecies){
  		
  		double[] result;
  		result=new double[nspecies];

  		for(int i=0; i<nspecies; i++)

  		{
  			double sum=0;
  			int count=0;

  			for(int j=0; j<nspecies; j++)
  				if(stemAge[j]<=splits[i] && nodeAge[j]>splits[i])
  				{
  					sum += disp[j];
  					count++;
  				}  			
  			result[i]=sum/(double)count;
  		}

  		return(result);
  	}

  	/* End get_time_dispar */
	/*.................................................................................................................*/
	public void recalcChart(CommandRecord commandRec){
		chart.deassignChart();
		chart.getCharter().setShowNames(true);
		chart.setXAxisName("Length from root");
		chart.setYAxisName("Average disparity / log number of lineages");
		
		MesquiteNumber resultX = new MesquiteNumber();
		MesquiteNumber resultY = new MesquiteNumber();

		for (int i=0; i<valuesX.getSize(); i++) {
			valuesX.placeValue(i, resultX);
			valuesY.placeValue(i, resultY);
			chart.addPoint(resultX, resultY);
		}
		chart.munch(commandRec);
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