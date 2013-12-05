/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
 Version 2.74, October 2010.
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
import java.awt.event.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/** Chart windows can subclass this. Provides standard versions of some methods.. */
public abstract class ChartWindow extends MesquiteWindow {
	private MesquiteChart chart;

	protected String chartTitle = "Chart";

	protected ChartTool arrowTool, infoTool;

	public ChartWindow(MesquiteModule ownerModule, boolean showInfoBar) {
		super(ownerModule, showInfoBar);
		arrowTool = new ChartTool(this, "arrow", MesquiteModule.getRootImageDirectoryPath(), "arrow.gif", 4, 2, "Select", null, null, null);
		arrowTool.setIsArrowTool(true);
		addTool(arrowTool);
		setCurrentTool(arrowTool);
		if (arrowTool != null)
			arrowTool.setInUse(true);
		infoTool = new ChartTool(this, "info", MesquiteModule.getRootImageDirectoryPath(), "info.gif", 1, 1, "Show Information", null, null, null);
		addTool(infoTool);
		setShowExplanation(true);
		setShowAnnotation(true);
		if (getPalette() != null)
			getPalette().setNumColumns(1);
		resetTitle();
	}

	public void contentsChanged() {
		if (chart != null) {
			chart.repaint();
			if (chart.getField() != null)
				chart.getField().repaint();
		}
		super.contentsChanged();
	}

	/* ................................................................................................................. */
	public void copyGraphicsPanel() {
		if (getChart() == null || getChart().getCopyCommand() == null)
			return;
		getChart().getCopyCommand().doItMainThread("", null, this); // command invoked
	}

	/* ................................................................................................................. */
	public int getMinimumContentHeight() {
		return 200; // ideally do this more sensibly
	}

	/* ................................................................................................................. */
	public String getTextContents() {
		if (chart != null && infoBar != null) {
			String s = StringUtil.lineEnding() + chart.getTextVersion() + StringUtil.lineEnding() + StringUtil.lineEnding() + "-----------------";
			s += StringUtil.lineEnding() + "Parameters of modules: " + StringUtil.lineEnding() + infoBar.getText(InfoBar.TEXT_PARAMETERS);
			s += StringUtil.lineEnding() + "====================" + StringUtil.lineEnding();
			return s;
		}
		else
			return null;
	}

	public void printWindow(MesquitePrintJob pjob) {
		if (pjob != null) {
			int mode = infoBar.getMode();
			if (mode > 0) { // text mode
				super.printWindow(pjob);
			}
			else {// graphical mode
				chart.printChart(pjob, this);
			}
		}
	}

	public String getPrintMenuItem() {
		return "Print Chart...";
	}

	/* ................................................................................................................. */
	public String getPrintToFitMenuItemName() {
		return "Print Chart To Fit Page...";
	}

	/* ................................................................................................................. */
	/**
	 * @author Peter Midford
	 */
	public void windowToPDF(MesquitePDFFile pdfFile, int fitToPage) {
		if (pdfFile != null) {
			if (infoBar.getMode() > 0)
				super.windowToPDF(pdfFile, fitToPage);
			else
				chart.chartToPDF(pdfFile, this, fitToPage);
		}
	}

	/* ................................................................................................................. */
	/**
	 * @author Peter Midford
	 * @return String
	 */
	public String getPrintToPDFMenuItemName() {
		return "Save Chart as PDF...";
	}

	public void setScroller(Panel panel) {
		MesquiteMessage.println("sorry, this chart window doesn't accommodate a scroller");
	}

	/* ................................................................................................................. */
	/**
	 * When called the window will determine its own title. MesquiteWindows need to be self-titling so that when things change (names of files, tree blocks, etc.) they can reset their titles properly
	 */
	public void resetTitle() {
		setTitle(chartTitle);
	}

	/* ................................................................................................................... */
	public void setChartTitle(String title) {
		this.chartTitle = title;
	}

	public MesquiteChart getChart() {
		return chart;
	}

	public void setChart(MesquiteChart chart) {
		this.chart = chart;
		if (chart != null) {
			chart.setArrowTool(arrowTool);
			infoTool.setTouchedCommand(MesquiteModule.makeCommand("infoTouch", chart));
			chart.setInfoTool(infoTool);
		}

	}

	protected void setContentsCursor(Cursor c) {
		if (c == null)
			MesquiteMessage.printStackTrace("Error: cursor of chart window null");
		else if (chart != null)
			chart.setCursor(c);
	}

	/** Returns menu location for item to bring the window to the for (0 = custom or don't show; 1 = system area of Windows menu; 2 = after system area of Windows menu) */
	public int getShowMenuLocation() {
		return 0;
	}

	public void setChartVisible() {
		addToWindow(chart);
		windowResized();
		chart.setVisible(true);
	}

	/* ................................................................................................................. */
	public abstract void recalcChart();

	/* ................................................................................................................. */
	public void blankChart() {
		chart.drawBlank();
	}
	/* ................................................................................................................. */
}

