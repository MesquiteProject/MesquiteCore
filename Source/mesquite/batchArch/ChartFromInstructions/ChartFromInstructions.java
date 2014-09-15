/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.batchArch.ChartFromInstructions;
/*~~  */

import java.awt.*;
import java.util.*;
import mesquite.lib.*;
import mesquite.lib.duties.*;


public class ChartFromInstructions extends MesquiteModule implements NumberForItem  {
	public void getEmployeeNeeds(){  //This gets called on startup to harvest information; override this and inside, call registerEmployeeNeed
		EmployeeNeed e2 = registerEmployeeNeed(ItemsCharter.class, getName() + " needs a module to make a chart.",
		"The charting module is chosen automatically.");
	}
 	public boolean returnsMultipleValues(){
  		return false;
  	}
	ItemsCharter chartWindowTask;
	ChartWindow cWindow;
	//IntegerArray array1;
	//IntegerArray array2;
	Parser parser = new Parser();
	private Taxa taxa;
	static int maxNumFiles=2;
	static int maxNumVariables = 12;
	
	int numItems = 0;
	int numVariables = 0;
	int currentVariable =-1;
	int numFiles = 0;
	String[] arrayPath;
	String [] labels;
	MesquiteFile[] arrayFile;
	String[] files;
	int[] itemsPerReplicate;
	boolean[] variableDefined;
	int[][] variablePosition;  
	String[] operators;  // the string of operators in the formula;
	int[] variablesInFormula;
	NumberArray[] array;
	int[] numItemsInFile;
	String formula;
	String formulaLabel;  // stores the label for the x axis
	String yaxis;
	String fileNames;
	boolean useFormula=true;
	boolean formulaDefined=false;
	MesquiteBoolean showFormula;
	
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		showFormula = new MesquiteBoolean(false);
//		showVariable = new MesquiteBoolean(false);
		chartWindowTask = (ItemsCharter)hireEmployee(ItemsCharter.class, null);
		if (chartWindowTask == null)
			return sorry(getName() + " couldn't start because no charting module obtained.");
		makeMenu("Chart");
		addCheckMenuItem(null,"Show Formula", makeCommand("toggleFormula",  this),showFormula);
		addMenuItem("Set Formula...", makeCommand("setFormula",  this));
		addMenuItem("Next Variable", makeCommand("showNextVariable",  this));
		parser.setWhitespaceString("\t");
		parser.setPunctuationString("");
		
		files = new String[maxNumFiles];
		operators = new String[maxNumFiles];
		labels = new String[maxNumVariables];
		variablesInFormula = new int[maxNumVariables];
		itemsPerReplicate = new int[maxNumFiles];
		variablePosition = new int[maxNumFiles][maxNumVariables];
		arrayPath = new String[maxNumFiles];
		arrayFile = new MesquiteFile[maxNumFiles];
		numItemsInFile = new int[maxNumFiles];
		array = new NumberArray[maxNumVariables];
		variableDefined = new boolean [maxNumVariables];

		for (int i=0; i<maxNumFiles; i++) {
			itemsPerReplicate[i]=0;
			for (int j=0; j<maxNumVariables; j++)
				variablePosition[i][j]=0;
			operators[i]="";
		}
		for (int j=0; j<maxNumVariables; j++){
			variablesInFormula[j]=-1;
			variableDefined[j]=false;
		}
		
		MesquiteString directoryName = new MesquiteString("");
		MesquiteString fileName = new MesquiteString("");
		String instructionsPath = MesquiteFile.openFileDialog("Choose Instructions File.",  directoryName,  fileName);
		if (instructionsPath==null)
			return sorry(getName() + " couldn't start because the instructions file was not chosen.");
		MesquiteFile instructionsFile =MesquiteFile.open(true, instructionsPath);
		if (instructionsFile==null || StringUtil.blank(instructionsFile.getFileName())) 
			return sorry(getName() + " couldn't start because the instructions file could not be found.");
			
		String contents = MesquiteFile.getFileContentsAsString(instructionsPath);
		if (!StringUtil.blank(contents))
			if (!readInstructionsFile(contents))
				return sorry(getName() + " couldn't start because instruction file could not be read.  It may not be a valid instruction file."); //D!

				
		initCurrentVariable();
		if (numFiles>1)
			fileNames = "files ";
		else
			fileNames = "file ";
		
		for (int i=0; i<numFiles; i++) {
			if (numFiles==1)
				arrayPath[i] = MesquiteFile.openFileDialog("Choose results file",  directoryName,  fileName);
			else
				arrayPath[i] = MesquiteFile.openFileDialog("Choose results file #"+(i+1),  directoryName,  fileName);
			if (arrayPath[i]==null)
				return sorry(getName() + " couldn't start because the results file was not chosen.");
			arrayFile[i] =MesquiteFile.open(true, arrayPath[i]);
			if (arrayFile[i]==null || StringUtil.blank(arrayFile[i].getFileName())) 
				return sorry(getName() + " couldn't start because the results file could not be found.");
			if (i>0) {
				if (i==numFiles-1)
					fileNames += " and ";
				else 
					fileNames += ", ";
			}
			fileNames += "\"" + fileName + "\"";
		}

		for (int i=0; i<numFiles; i++) 
			if (itemsPerReplicate[i]>0) {
				numItemsInFile[i] = (int)numberTokensInFile(arrayFile[i])/itemsPerReplicate[i];
			}
		numItems = numItemsInFile[0];
		for (int i=1; i<numFiles; i++) {
			if (numItems < numItemsInFile[i])
				numItems=numItemsInFile[i];
		}

		for (int i=0; i<maxNumVariables; i++) 
			array[i] = new NumberArray(numItems);
		
			
		for (int i=0; i<numFiles; i++) {
			if (!readScoreFile(arrayFile[i],array[i], i))
				return  sorry(getName() + " couldn't start because the results file could not be read.");
		}
		
		cWindow = chartWindowTask.makeChartWindow(this);
		setModuleWindow( cWindow);
		chartWindowTask.setNumberTask(this);
		ChartInstrItemsSource chartInstructionsItemsSource= new ChartInstrItemsSource(this);
		chartWindowTask.setItemsSource(chartInstructionsItemsSource);
		cWindow.setChartTitle(getItemsInfo());
		cWindow.resetTitle();
		if (!MesquiteThread.isScripting()){
			cWindow.setChartVisible();
			cWindow.setVisible(true);
			chartWindowTask.doCounts();
			cWindow.toFront();
		}

 		resetContainingMenuBar();
		resetAllWindowsMenus();
		//getModuleWindow().setTitle("Trees: " + treeSourceTask.getSourceName());
		return true;
 	}
 	//D!
 	public Class getDutyClass(){
 		return ChartFromInstructions.class;
 	}
	/*.................................................................................................................*/
    	 public Object doCommand(String commandName, String arguments, CommandChecker checker) {
     	 	if (checker.compare(this.getClass(), "Suspends calculations", null, commandName, "suspendCalculations")){
			chartWindowTask.incrementSuspension();
     	 	}
     	 	else if (checker.compare(this.getClass(), "Resumes calculations", null, commandName, "resumeCalculations")){
			chartWindowTask.decrementSuspension();
     	 	}
    	 	else if (checker.compare(this.getClass(), "Sets the chart to visible", null, commandName, "setChartVisible")) {
			if (cWindow!=null)
				cWindow.setChartVisible();
    	 		
    	 	}
     	 	else if (checker.compare(this.getClass(), "Requests that chart calculations be redone", null, commandName, "doCounts")){
			if (cWindow!=null) {
				chartWindowTask.doCounts();
			}
     	 	}
    	 	else if (checker.compare(this.getClass(), "Returns the chart drawing module", null, commandName, "getCharter")) {
       			return chartWindowTask;
    	 	}
	       	else if (checker.compare(this.getClass(), "Shows formula", "[on = show; off]", commandName, "toggleFormula")) {
			if (cWindow!=null) {
	    	 		showFormula.toggleValue(parser.getFirstToken(arguments));
				parametersChanged();
				useFormula=showFormula.getValue();
				chartWindowTask.doCounts();
			}
		}
	       	else if (checker.compare(this.getClass(), "Sets formula", null, commandName, "setFormula")) {
			if (cWindow!=null) {
				if (getFormula()) {
					chartWindowTask.doCounts();
				}
			}
		}
	       	else if (checker.compare(this.getClass(), "Shows next variable", null, commandName, "showNextVariable")) {
			if (cWindow!=null) {
				if (showFormula.getValue())
					MesquiteMessage.notifyUser("In order to see individual variables, you first need to turn off Show Formula in the Histogram menu.");
				else {
					setCurrentVariableToNext();
					chartWindowTask.doCounts();
				}
			}
		}
    	 	else
    	 		return  super.doCommand(commandName, arguments, checker);
    	 	return null;
    	 }
	/*.................................................................................................................*/
   	public void calcFormulaLabel(){
		formulaLabel = "";
		if (variablesInFormula[0]>=0)
			formulaLabel+=labels[variablesInFormula[0]];
		if (variablesInFormula[1]>=0)
			formulaLabel+=" "+operators[1]+" "+labels[variablesInFormula[1]];
		if (operators[0].equals("-"))
			formulaLabel = "-(" + formulaLabel+")";
	}
	/*.................................................................................................................*/
   	public boolean getFormula(){
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "Choose Formula",  buttonPressed);
		dialog.addLabel("Choose Formula", Label.CENTER);
		int firstVariable=0;
		
		Choice signChoice = dialog.addPopUpMenu("Sign: ");
		signChoice.add(" + ");
		signChoice.add(" - ");
		signChoice.select(0);
		if (!StringUtil.blank(operators[0]))
			if (operators[0].equals("-"))
				signChoice.select(1);


		Choice firstVariableChoice = dialog.addPopUpMenu("First Variable: ");
		for (int i=0; i<maxNumVariables; i++)
			if (variableDefined[i]) {
				firstVariableChoice.add(labels[i]);
				if (variablesInFormula[0]==i)
					firstVariable = i;
			}
		firstVariableChoice.select(firstVariable);
		//dialog.suppressNewPanel();
		
		Choice operatorChoice = dialog.addPopUpMenu("Operator: ");
		operatorChoice.add(" + ");
		operatorChoice.add(" - ");
		operatorChoice.add(" * ");
		operatorChoice.add(" / ");
		operatorChoice.select(0);
		if (!StringUtil.blank(operators[1]))
			if (operators[1].equals("+"))
				operatorChoice.select(0);
			else if (operators[1].equals("-"))
				operatorChoice.select(1);
			else if (operators[1].equals("*"))
				operatorChoice.select(2);
			else if (operators[1].equals("/"))
				operatorChoice.select(3);
		
		//dialog.suppressNewPanel();
		
		int secondVariable=-1;
		Choice secondVariableChoice = dialog.addPopUpMenu("Second Variable: ");
		secondVariableChoice.add("(None)");
		for (int i=0; i<maxNumVariables; i++)
			if (variableDefined[i]){
				secondVariableChoice.add(labels[i]);
				if (variablesInFormula[1]==i)
					secondVariable =i;
			}
		secondVariableChoice.select(secondVariable+1);
		
		String helpString = "The formula will be\n\n   value = sign (FirstVariable operator SecondVariable).\n\nThus, if you have chosen a sign of \"-\",";
		helpString += " a first variable of x, and no second variable, the formula would be value = -x.  If you have chosen a sign of  \"-\",";
		helpString += " a first variable of x, an operator of \"+\", and a second variable of y, the formula would be value = -(x+y).";
		dialog.appendToHelpString(helpString);
		
		
		dialog.completeAndShowDialog(true);
			
		boolean ok = (dialog.query()==0);
		
		if (ok) {
			int count =0;
			int variableSelected = firstVariableChoice.getSelectedIndex();
			firstVariable=-1;
			for (int i=0; i<maxNumVariables; i++)
				if (variableDefined[i]) {
					if (variableSelected==count) {
						firstVariable=i;
						break;
					}
					count++;
				}
			variablesInFormula[0] = firstVariable;
			operators[0]=signChoice.getSelectedItem().substring(1,2);
			operators[1]= operatorChoice.getSelectedItem().substring(1,2);
			variableSelected =secondVariableChoice.getSelectedIndex();
			secondVariable=-1;
			count=0;
			for (int i=0; i<maxNumVariables; i++)
				if (variableDefined[i]) {
					if (variableSelected==count) {
						secondVariable=i-1;   // -1 because of the None item
						break;
					}
					count++;
				}
			variablesInFormula[1] = secondVariable;
			calcFormulaLabel();
		}
		
		dialog.dispose();

//   		operators[0]="";
//   		variablesInFormula[0]=1;
//   		variablesInFormula[0]=2;
   		
		return ok;
	}
	/*.................................................................................................................*/
   	public void initCurrentVariable(){
   		currentVariable = -1;
   		for (int i=0; i<maxNumVariables; i++) {
   			if (variableDefined[i]) {
   				currentVariable=i;
   				return;
   			}
   		}
   	}
	/*.................................................................................................................*/
   	public void setCurrentVariableToNext(){
   		if (currentVariable>=maxNumVariables)
   			currentVariable=-1;
   		for (int i=currentVariable+1; i<maxNumVariables; i++) {
   			if (variableDefined[i]) {
   				currentVariable=i;
   				return;
   			}
   		}
  		for (int i=0; i<maxNumVariables; i++) {
   			if (variableDefined[i]) {
   				currentVariable=i;
   				return;
   			}
   		}
   	}
	/*.................................................................................................................*/
	public boolean readInstructionsFile(String contents){
		Parser parser = new Parser();
		parser.setString(contents);
		String token = parser.getNextToken(); 
		if (!token.equalsIgnoreCase("MesquiteInstructions")) 
			return false;
		Arguments arguments = new Arguments(parser, true);
		int n = arguments.getParameterValueAsInt("numVariables");
		if (!MesquiteInteger.isCombinable(n))
			return false;
		numVariables = n;

		n = arguments.getParameterValueAsInt("numFiles");
		if (!MesquiteInteger.isCombinable(n))
			return false;
		numFiles = n;
		formulaDefined=true;
		formula = arguments.getParameterValue("formula");
		if (StringUtil.blank(formula)) {
			formula = "v1";
			useFormula = false;
			formulaDefined=false;
		}
		yaxis = arguments.getParameterValue("recordLabel");
		formulaLabel = arguments.getParameterValue("formulaLabel");
		for (int j=0; j<maxNumVariables; j++) 
			labels[j]=arguments.getParameterValue("label"+(j+1));

		yaxis = arguments.getParameterValue("recordLabel");

		for (int i=0; i<maxNumFiles; i++) {
			files[i] = arguments.getParameterValue("file"+(i+1));
		}
		for (int i=0; i<maxNumFiles; i++) {
			if (!StringUtil.blank(files[i])){
				parser.setString(files[i]);
				Arguments fileParams = new Arguments(parser, true);
				n = fileParams.getParameterValueAsInt("itemsPerRecord");
				if (!MesquiteInteger.isCombinable(n))
					return false;
				itemsPerReplicate[i] = n;
				for (int j=0; j<maxNumVariables; j++) {
					n = fileParams.getParameterValueAsInt("v"+(j+1));
					if (MesquiteInteger.isCombinable(n)) {
						variablePosition[i][j] = n;
						variableDefined[j]=true;
					}
				}
			}
		}
		if (itemsPerReplicate[0]==0 || variablePosition[0][0] == 0 )
			return false;
		int variableNum = 0;
		String s;
		
		try {
			int formulaPos=0;
			if (formula.charAt(0)=='v') {
				operators[0]="";
			}
			else {
				operators[0]=formula.substring(0,1);
				formulaPos++;
			}
			formulaPos++;
			s = formula.substring(formulaPos, formulaPos+1);
			variableNum = MesquiteInteger.fromString(s,true);
			if (!MesquiteInteger.isCombinable(variableNum))
				return false;
			variablesInFormula[0] = variableNum-1;
			formulaPos++;
			operators[1]=formula.substring(formulaPos, formulaPos+1);
			formulaPos += 2;
			s = formula.substring(formulaPos, formulaPos+1);
			variableNum = MesquiteInteger.fromString(s,true);
			if (!MesquiteInteger.isCombinable(variableNum))
				return false;
			variablesInFormula[1] = variableNum-1;
		}
		catch (Exception e) {}
		showFormula.setValue(formulaDefined);
		//parametersChanged();
		return true;
	}
	/*.................................................................................................................*/
	public long numberTokensInFile(MesquiteFile file){
		long count = 0;
		if (file.openReading()) {
			StringBuffer sb = new StringBuffer(1000);
			file.readLine(sb);
			String line = sb.toString();
			String token;
			while (!StringUtil.blank(line)) {
				parser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				token = parser.getNextToken();  // first token in line

				while(!StringUtil.blank(token)) {
					count++;
					token = parser.getNextToken();  // next token in line
				}
				file.readLine(sb);
				line = sb.toString();
			}
			file.closeReading();
		}
		return count;
	}
	/*.................................................................................................................*/
	public int numberLinesInFile(MesquiteFile file){
		int count = 0;
		if (file.openReading()) {
			StringBuffer sb = new StringBuffer(1000);
				file.readLine(sb);
				String line = sb.toString();		
			boolean abort = false;
			while (!StringUtil.blank(line)) {
				count++;
				file.readLine(sb);
				line = sb.toString();		
			}
			file.closeReading();
		}
		return count;
	}


	/*.................................................................................................................*/
	
	public boolean processFileToken(String token, int elementNumber, int scoreFile, int arrayNumber, ProgressIndicator progIndicator){
		if (elementNumber<=array[arrayNumber].getSize()) {
			MesquiteNumber value = new MesquiteNumber();
			value.setValue(token);
			if (!value.isCombinable()) {
				MesquiteMessage.notifyUser("An inappropriate entry was found for one of the numbers: \""+ token +"\". The instructions file may need to be revised.");
				progIndicator.goAway();
				arrayFile[scoreFile].closeReading();
				//arrayFile[scoreFile].close();
				decrementMenuResetSuppression();
				return false;
			}
			array[arrayNumber].setValue(elementNumber,value);
			return true;
		}
		return true;  // return true as don't want to abort the process just because we have found too many elements for this array.
	}
	/*.................................................................................................................*/
	public boolean readScoreFile(MesquiteFile file, NumberArray array, int scoreFile){
		incrementMenuResetSuppression();
		ProgressIndicator progIndicator = new ProgressIndicator(getProject(),"Reading Results File "+ file.getName(), file.existingLength());
		progIndicator.start();
		file.linkProgressIndicator(progIndicator);
		boolean writeTokens = true;
		if (file.openReading()) {
			StringBuffer sb = new StringBuffer(100);
			file.readLine(sb);
			String line = sb.toString();		
			//line = file.readLine();		
			String token;
			int count = 0;
			int tokenCount = 0;
			int num;
			if (writeTokens) {
				MesquiteTrunk.mesquiteTrunk.logln("Tokens read in from instruction file from first replicate:  ");
			}
			
			boolean abort = false;
			while (!StringUtil.blank(line) && !abort) {
				parser.setString(line); //sets the string to be used by the parser to "line" and sets the pos to 0
				
				token = parser.getNextToken();  // first token in line
					if (writeTokens) 
						MesquiteTrunk.mesquiteTrunk.logln("   token " + (tokenCount+1) + ":  " + token);
				
				while(!StringUtil.blank(token) && !abort) {
					tokenCount++;
					for (int i=0;i<maxNumVariables; i++)
						if (variablePosition[scoreFile][i] == tokenCount)  // we've found one of the tokens that we want
							if (!processFileToken(token, count,  scoreFile, i, progIndicator))
								return false;
					if (tokenCount>=itemsPerReplicate[scoreFile]) {
						tokenCount=0;
						count++;
						writeTokens = false;
					}
					token = parser.getNextToken();  // next token in line
					if (writeTokens) 
						MesquiteTrunk.mesquiteTrunk.logln("   token " + (tokenCount+1) + ":  " +  token);
				}
				file.readLine(sb);
				line = sb.toString();		
				if (file.getFileAborted()) {
					abort = true;
				}
			}
			if (progIndicator!=null)
				progIndicator.goAway();
			file.closeReading();
			if (abort){ 		
				//file.close();
				resetAllMenuBars();
			}
		}
		decrementMenuResetSuppression();
		return true;
	}
	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return false;
	}
 	public void employeeQuit(MesquiteModule m){
 		if (m== chartWindowTask)
 			iQuit();
 	}
	/*.................................................................................................................*/
 	public void windowGoAway(MesquiteWindow whichWindow) {
			whichWindow.hide();
			whichWindow.dispose();
			iQuit();
	}
	/*.................................................................................................................*/
  	 public Snapshot getSnapshot(MesquiteFile file) { 
   	 	Snapshot temp = new Snapshot();
  	 	temp.addLine("suspendCalculations"); 
  	 	temp.addLine("getCharter", chartWindowTask); 
  	 	temp.addLine("setChartVisible"); 
  	 	temp.addLine("doCounts"); 
  	 	temp.addLine("resumeCalculations"); 
  	 	temp.addLine("showWindow");
  	 	return temp;
  	 }
   	/** Called to provoke any necessary initialization.  This helps prevent the module's intialization queries to the user from
   	happening at inopportune times (e.g., while a long chart calculation is in mid-progress)*/
   	public void initialize(Object object1, Object object2){
   	}
    	public void initialize(Taxa taxa){
    	}
  	
	public String accumulateParameters(String spacer){
		return null;
	}
	/*@@@@@@@@@@@@@@@@ These are things that need to be overridden @@@@@@@@@@@@@@@@@@@@@@@@*/
	public  void calculateNumber(Object object1, Object object2, MesquiteNumber result, MesquiteString resultString){
		clearResultAndLastResult(result);
		if (object1 instanceof MesquiteInteger){	
			int i = (((MesquiteInteger)object1).getValue());
			
			if (currentVariable>=0 && !useFormula)
				array[currentVariable].placeValue(i, result);
			else {
				array[variablesInFormula[0]].placeValue(i, result);
				if (variablesInFormula[1] < 0) {  // only one value
					if (operators[0].equals("-")) {
						result.changeSign();
					}
				}
				else {
					MesquiteNumber n2 = new MesquiteNumber();
					array[variablesInFormula[1]].placeValue(i, n2);
					//array[variablesInFormula[1]].getValue(i));
					if (operators[1].equals("*"))
						result.multiplyBy(n2);
					else if (operators[1].equals("-")) {
						result.subtract(n2);
					}
					else if (operators[1].equals("+"))
						result.add(n2);
					else if (operators[1].equals("/"))
						result.divideBy(n2);
					if (operators[0].equals("-"))
						result.changeSign();
				}
			}
			saveLastResult(result);
			saveLastResultString(resultString);
		}
				
	}
  	public  void calculateNumberInContext(Object object1, Object object2, ItemsSource source, int whichItem, MesquiteNumber result, MesquiteString resultString){
		clearResultAndLastResult(result);
		calculateNumber(object1, object2, result, resultString);
		saveLastResult(result);
		saveLastResultString(resultString);
	}
 	public String getItemsInfo(){
   		if (useFormula)
   			return "Calculations from " + fileNames;
   		else
   			return "Values from " + fileNames;
   	}
   	/** returns number of items for given Taxa*/
   	public int getNumberOfItems(){
   		return numItems;
   	}
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName(){
   		return "Replicate";
   	}
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural(){
   		return "Replicates";
   	}
   	public String getNameOfValueCalculated(){ 
   		if (useFormula) {
   			if (StringUtil.blank(formulaLabel))
   				calcFormulaLabel();
   			return formulaLabel;
   		}
   		else if (currentVariable>=0)
   			if (StringUtil.blank(formulaLabel))
   				return "value";
   			else
   				return labels[currentVariable];
   		return "";
   	}
   	/** returns item numbered ic; this will be passed back above as object1*/
   	public Object getItem(Taxa taxa, int ic){
   		return new MesquiteInteger(ic);
   	}
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/
   	/** If the items given by this source are parts of an Associable, then this method allows charts and other things to query about it,
   	which allows coordination of item selection to work.*/
   	public Selectionable getSelectionable(){
   		return null;
   	}
    	public void setEnableWeights(boolean enable){
    	}
   	public boolean itemsHaveWeights(Taxa taxa){
   		return false;
   	}
   	public double getItemWeight(Taxa taxa, int ic){
   		return MesquiteDouble.unassigned;
   	}
   	public void prepareItemColors(Taxa taxa){
   	}
   	public Color getItemColor(Taxa taxa, int ic){
   		return null;
   	}
	/*.................................................................................................................*/

	MesquiteInteger pos = new MesquiteInteger();
	/*.................................................................................................................*/
	/** returns whether this module is requesting to appear as a primary choice */
   	public boolean requestPrimaryChoice(){
   		return true;  
   	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
			if (cWindow!=null && !MesquiteThread.isScripting()) {
					chartWindowTask.doCounts();
			}
	}
	/*.................................................................................................................*/
    	 public String getName() {
		return "Chart from Instruction File";
   	 }
	/*.................................................................................................................*/
    	 public String getNameForMenuItem() {
		return "Chart from Instruction File...";
   	 }
	/*.................................................................................................................*/
	/*.................................................................................................................*/
  	 public String getExplanation() {
		return "Displays a chart summarizing data contained in files, as directed by an instruction file.";
   	 }
   	 
}






class ChartInstrItemsSource implements ItemsSource  {
	ChartFromInstructions ownerModule;
	
	public ChartInstrItemsSource(ChartFromInstructions ownerModule){
		this.ownerModule = ownerModule;
	}
	
   	public void initialize(Taxa taxa){}
   	
   	public String accumulateParameters(String spacer){
   		return getParameters();
   	}
   	public String getParameters(){
   		return ownerModule.getItemsInfo(); 
   	}
   	public String getNameAndParameters(){
   		return getParameters();
   	}
   	public String getName(){
   		return "Chart Instructions Item Source";
   	}
   	
   	/** returns number of items for given Taxa*/
   	public int getNumberOfItems(Taxa taxa){
   		return ownerModule.getNumberOfItems();   
   	}
   	/** returns name of type of item, e.g. "Character", or "Taxon"*/
   	public String getItemTypeName(){
   		return "Replicate";
   	}
   	/** returns name of type of item, e.g. "Characters", or "Taxa"*/
   	public String getItemTypeNamePlural(){
   		return "Replicates";
   	}
   	public String getNameOfValueCalculated(){ 
   		return ownerModule.getNameOfValueCalculated();  //fed from the module?
   	}
   	/** returns item numbered ic; this will be passed back above as object1*/
   	public Object getItem(Taxa taxa, int ic){
   		return new MesquiteInteger(ic);
   	}
   	public Selectionable getSelectionable(){
   		return null;
   	}
    	public void setEnableWeights(boolean enable){
    	}
   	public boolean itemsHaveWeights(Taxa taxa){
   		return false;
   	}
   	public double getItemWeight(Taxa taxa, int ic){
   		return MesquiteDouble.unassigned;
   	}
   	public void prepareItemColors(Taxa taxa){
   	}
   	public Color getItemColor(Taxa taxa, int ic){
   		return null;
   	}
	/** zzzzzzzzzzzz*/
   	public String getItemName(Taxa taxa, int ic){
   		return null;
   	}
}


