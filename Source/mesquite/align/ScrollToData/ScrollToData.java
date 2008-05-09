package mesquite.align.ScrollToData;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.CharacterData;
import mesquite.lib.duties.DataWindowAssistantI;
import mesquite.lib.table.MesquiteTable;
import mesquite.lib.table.TableTool;

public class ScrollToData extends DataWindowAssistantI {
	protected MesquiteTable table;
	protected CharacterData  data;
	protected ScrollToDataTool scrollTool;

	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		getCompatibilityTest();
		if (containerOfModule() instanceof MesquiteWindow) {
			scrollTool = new ScrollToDataTool(this, "scrollerRight", getPath(), "scrollerRight.gif", 8,8,"Scrolls to next data cell","This tool scrolls to the next cell in the sequence with data.", MesquiteModule.makeCommand("scrollTouchCell",  this) , MesquiteModule.makeCommand("scrollDragCell",  this), MesquiteModule.makeCommand("scrollDropCell",  this));
			scrollTool.setDeselectIfOutsideOfCells(false);
			scrollTool.setOptionImageFileName( "scrollerLeft.gif", 8, 8);

			((MesquiteWindow)containerOfModule()).addTool(scrollTool);
		}
		else return sorry(getName() + " couldn't start because the window with which it would be associated is not a tool container.");
		return true;
	}
	public CompatibilityTest getCompatibilityTest(){
		return new RequiresAnyMolecularData();
	}
	/*.................................................................................................................*/
	public void setTableAndData(MesquiteTable table, CharacterData data){
		if (!(data instanceof CategoricalData))
			return;
		this.table = table;
		this.data = data;
	}

	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Touched.", "[column touched] [row touched] [percent horizontal] [percent vertical] [modifiers]", commandName, "scrollTouchCell")) {
		}
		else if (checker.compare(this.getClass(), "Dragging", "[column dragged] [row dragged] [percent horizontal] [percent vertical] [modifiers]", commandName, "scrollDragCell")) {
		}
		else if (checker.compare(this.getClass(), "Dropping.", "[column dropped] [row dropped] [percent horizontal] [percent vertical] [modifiers]", commandName, "scrollDropCell")) {
			if (table!=null && data !=null){
				boolean optionDown = arguments.indexOf("option")>=0;
				MesquiteInteger io = new MesquiteInteger(0);
				int firstColumnTouched= MesquiteInteger.fromString(arguments, io);
				int firstRowTouched= MesquiteInteger.fromString(arguments, io);

				if (data.isInapplicable(firstColumnTouched, firstRowTouched)) {
					if (optionDown) {
						for (int ic = firstColumnTouched-1; ic>=0; ic--) {
							if (!data.isInapplicable(ic, firstRowTouched)) {
								table.scrollToColumn(ic);
								break;
							}
						}
					} else {
						for (int ic = firstColumnTouched+1; ic<table.numColumnsTotal; ic++) {
							if (!data.isInapplicable(ic, firstRowTouched)) {
								table.scrollToColumn(ic);
								break;
							}
						}

					}
				}
			}
		}
		else
			return super.doCommand(commandName, arguments, checker);
		return null;
	}

	public String getName() {
		return "Scroll To Data";
	}


	public class ScrollToDataTool extends TableTool {

		public ScrollToDataTool (Object initiator, String name, String imageDirectoryPath, String imageFileName, int hotX, int hotY,  String fullDescription, String explanation, MesquiteCommand touchedCommand, MesquiteCommand dragCommand, MesquiteCommand droppedCommand) {
			super(initiator, name, imageDirectoryPath, imageFileName, hotX, hotY, fullDescription, explanation, touchedCommand, dragCommand, droppedCommand);
			this.initiator = initiator;
			this.name = name;
			setDeselectIfOutsideOfCells(true);
		}
	}

}
