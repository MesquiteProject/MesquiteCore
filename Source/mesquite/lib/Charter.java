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


/* ======================================================================== */
/** Charters draw the charts.  The available charters are stored in a vector in MesquiteTrunk (to which they can be added by
addCharter() which is passed the Class).  Charters must have constructor with no arguments because they are instantiated using
newInstance. */
public abstract class Charter  {
	protected MesquiteNumber maxX =new MesquiteNumber();
	protected MesquiteNumber minX=new MesquiteNumber();
	protected MesquiteNumber maxY =new MesquiteNumber();
	protected MesquiteNumber minY=new MesquiteNumber();
	protected MesquiteNumber tempNum=new MesquiteNumber();
	protected boolean chartDone = false;
	protected int minTicks = 3;
	protected int maxTicks =1000;
	protected int markerWidth = 4;
	protected Color bgColor = Color.white;
	protected Color axisColor = Color.blue;
	protected Color gridColor = Color.cyan;
	protected boolean drawWarningSuppressed = false;
	protected boolean suspendDrawing = false;
	protected boolean suspendChartCalculations = false;
	
	public abstract void calculateChart(MesquiteChart chart);
	public  abstract void drawChartBackground(Graphics g, MesquiteChart chart);
	public  abstract void drawChart(Graphics g, MesquiteChart chart);
	public  abstract void drawBlank(Graphics g, MesquiteChart chart);
	public void setShowNames(boolean showNames) {}
	
	public String getTextVersion(MesquiteChart chart){
		if (chart==null)
			return null;
		StringBuffer s = new StringBuffer();
		chart.putRawTextVersion(null, ": ", StringUtil.lineEnding(), false, false, s);
		return s.toString();
	}
	StringInABox xAxisWriter;
	MesquiteNumber minVisX = new MesquiteNumber();
	MesquiteNumber minVisY = new MesquiteNumber();
	public void drawAxes(Graphics g, boolean printing, int chartWidth, int chartHeight, MesquiteChart chart){
		//currently does not pay attention to whether printing
		MesquiteNumber tickNumber = new MesquiteNumber();
		MesquiteNumber tickIncrementNumber = new MesquiteNumber();
		MesquiteNumber firstTick = new MesquiteNumber();
		g.setColor(chart.getBackground());
		g.fillRect(0,0,chartWidth, chartHeight);
		int fieldWidth = chart.getFieldWidth();
		int fieldHeight = chart.getFieldHeight();
		int margin = chart.getMargin();
		g.setColor(Color.black);
		if (chart.xAxisName!=null && chart.getXAxisEdge()>0) {
			if (xAxisWriter==null)
				xAxisWriter = new StringInABox(chart.xAxisName, g.getFont(), chartWidth - chart.getXAxisEdge() - 16);
			else {
				xAxisWriter.setWidth(chartWidth - chart.getXAxisEdge() - 16);
				xAxisWriter.setString(chart.xAxisName);
				xAxisWriter.setFont(g.getFont());
			}
			xAxisWriter.draw(g,chart.getXAxisEdge()+30, chartHeight-chart.getYAxisEdge() +36); //had been 48
		}
		int leftMost = chart.getXAxisEdge()-20;
		if (chartDone){
			Font font = g.getFont();
			FontMetrics fontMet = g.getFontMetrics(font);
			int textOffset = (fontMet.getMaxAscent() + fontMet.getMaxDescent())/2;
			if (chart.getYAxisEdge()>0) {
				
				// scale ======== on X axis
				chart.putVisMinimumX(minVisX);
				chart.putVisMinimumY(minVisY);
				double min = chart.getAxisMinimumX().getDoubleValue(); //should be able to find out leftmost visible
				double max = chart.getAxisMaximumX().getDoubleValue();
				
				firstTick.setToFirstScaleMark(chart.getAxisMinimumX(), chart.getAxisMaximumX(), minTicks);
				tickNumber.setValue(firstTick);
				tickIncrementNumber.setToScaleIncrement(chart.getAxisMinimumX(), chart.getAxisMaximumX(), minTicks);
				//g.setColor(gridColor);
				int i =1;
				
				if (!tickIncrementNumber.isNegative() && (max-tickNumber.getDoubleValue())/tickIncrementNumber.getDoubleValue()<maxTicks) 
					for (int count = 0; tickNumber.getDoubleValue()<max; count++) {
						i++;
						int x = xToPixel(tickNumber.getDoubleValue(),chart) + chart.getXAxisEdge() - chart.scrollXOffset();
						if ((x>=chart.getXAxisEdge() && tickNumber.getDoubleValue()>= minVisX.getDoubleValue())){
							String t =tickNumber.toString(Math.abs(max-min));
							//g.setColor(axisColor);
							chart.textRotator.drawRotatedText(t, i, g, chart, x - textOffset, chartHeight-chart.getYAxisEdge()+32);
						}
						tickNumber.setValue(tickIncrementNumber);
						tickNumber.multiplyBy(count);
						tickNumber.add(firstTick);
					}
			}
			if (chart.getXAxisEdge()>0) {
				// scale ======== on Y axis
				double min = minY.getDoubleValue();
				double max = maxY.getDoubleValue();
				tickNumber.setToFirstScaleMark(minY, maxY, minTicks);
				tickIncrementNumber.setToScaleIncrement(minY, maxY, minTicks);
				//g.setColor(gridColor);
				int hPos = 0;
				if (!tickIncrementNumber.isNegative() && (max-tickNumber.getDoubleValue())/tickIncrementNumber.getDoubleValue()<maxTicks) {
					for (; tickNumber.getDoubleValue()<max; tickNumber.add(tickIncrementNumber)) {
						int y = yToPixel(tickNumber.getDoubleValue(), chart) - chart.scrollYOffset();
						if ((y<chart.getChartHeight()-chart.getYAxisEdge() && tickNumber.getDoubleValue()> minVisY.getDoubleValue())){
							String t =tickNumber.toString(Math.abs(max-min));
							hPos = chart.getXAxisEdge() - 8 - fontMet.stringWidth(t);
							leftMost = MesquiteInteger.minimum(hPos, leftMost);
							g.drawString(t, hPos, y + textOffset);
						}
					}
				}
			}
		}
			
		g.setColor(Color.black);
		if (chart.yAxisName!=null && chart.yAxisWriter!=null && chart.getYAxisEdge()>0 && chart.textRotator!=null && chartDone) {
			chart.yAxisWriter.setWidth( chartHeight-chart.getYAxisEdge()-20);
			chart.yAxisWriter.setFont(g.getFont());
			chart.yAxisWriter.setString(getYAxisName(chart));
			chart.yAxisWriter.draw(g, leftMost - chart.yAxisWriter.getHeight()-16, chartHeight-chart.getYAxisEdge()-24, 0,chart.getBounds().width, chart, false);
		}

	}
   	public boolean getShowPercent() {
   		return true;
   	}
   	public void setDrawWarningSuppressed(boolean drawWarningSuppressed) {
   		this.drawWarningSuppressed = drawWarningSuppressed;
   	}
   	public boolean getDrawWarningSuppressed() {
   		return drawWarningSuppressed;
   	}
   	public void setSuspendDrawing(boolean suspendDrawing) {
   		this.suspendDrawing = suspendDrawing;
   	}
   	public boolean getSuspendDrawing() {
   		return suspendDrawing;
   	}
   	public void setSuspendChartCalculations(boolean suspendChartCalculations) {
   		this.suspendChartCalculations = suspendChartCalculations;
   	}
   	public boolean getSuspendChartCalculations() {
   		return suspendChartCalculations;
   	}
	/* ----------------------------------*/
	public void pixelToY(int y, MesquiteChart chart, MesquiteNumber yValue){
		if (yValue==null)
			return;
		yValue.findWithinBounds(minY, maxY, chart.getFieldHeight()-y, chart.getFieldHeight());
	}
	/* ----------------------------------*/
	public synchronized int yToPixel(double y, MesquiteChart chart){
		tempNum.setValue(y);
		int fieldHeight =chart.getFieldHeight();
		int margin =chart.getMargin();
		int value = fieldHeight-margin-tempNum.setWithinBounds(minY, maxY, fieldHeight- 2*margin) - chart.getYPixelBase();
		return value;

	}
	/* ----------------------------------*/
	public void pixelToX(int x,  MesquiteChart chart, MesquiteNumber xValue){
		if (xValue==null)
			return;
		int fieldWidth =chart.getFieldWidth();
		int margin =chart.getMargin();
		
		xValue.findWithinBounds(minX, maxX, x- margin + chart.getXPixelBase(), fieldWidth - 2*margin-markerWidth);
		//xValue.findWithinBounds(minX, maxX, x, chart.getFieldWidth());
	}
	/* ----------------------------------*/
	public synchronized int xToPixel(double x,  MesquiteChart chart){
		tempNum.setValue(x);
		int fieldWidth =chart.getFieldWidth();
		int margin =chart.getMargin();
		int value =   margin+tempNum.setWithinBounds(minX, maxX, fieldWidth - 2*margin-markerWidth) - chart.getXPixelBase();
		return value;
	}
	
	/* ----------------------------------*/
	public boolean isNative(){
		return true;
	}
	/* ----------------------------------*/
	public MesquiteNumber leftmostXDrawn(MesquiteChart chart){
		return null;
	}
	/* ----------------------------------*/
	public MesquiteNumber rightmostXDrawn(MesquiteChart chart){
		return null;
	}
	/* ----------------------------------*/
	public MesquiteNumber topmostYDrawn(MesquiteChart chart){
		return null;
	}
	/* ----------------------------------*/
	public MesquiteNumber bottommostYDrawn(MesquiteChart chart){  
		return null; 
	}
	/* ----------------------------------*/
	public MesquiteNumber rightmostXOfInterval(MesquiteNumber x, MesquiteChart chart){
		return null;
	}
	/* ----------------------------------*/
	public MesquiteNumber leftmostXOfInterval(MesquiteNumber x, MesquiteChart chart){
		return null;
	}
	/* ----------------------------------*/
	public MesquiteNumber bottommostYOfInterval(MesquiteNumber y, MesquiteChart chart){
		return null;
	}
	/* ----------------------------------*/
	public MesquiteNumber topmostYOfInterval(MesquiteNumber y, MesquiteChart chart){
		return null;
	}
	/* ----------------------------------*/
	public boolean moreThanOneValueInInterval(MesquiteNumber x, MesquiteChart chart) {
		return false;
	}
	/* ----------------------------------*/
	public boolean showingClumpSums(){
		return false;
	}
	/* ----------------------------------*/
	public String getYAxisNameSuffix(){
		return "";
	}
	/* ----------------------------------*/
	public String getYAxisName(MesquiteChart chart){
		if (chart==null)
			return "";
		else
			return chart.yAxisName;
	}
	public void drawBackground(Graphics g, MesquiteChart chart){
	}
	public void drawGrid(Graphics g, MesquiteChart chart){
		if (!minX.isCombinable())
			minX.setValue(chart.getAxisMinimumX());
		if (!maxX.isCombinable())
			maxX.setValue(chart.getAxisMaximumX());
		if (!minY.isCombinable())
			minY.setValue(chart.getAxisMinimumY());
		if (!maxY.isCombinable()) {
			maxY.setValue(chart.getAxisMaximumY());
		}
		drawBackground(g, chart);
		int fieldWidth = chart.getFieldWidth();
		int fieldHeight = chart.getFieldHeight();
		int margin = chart.getMargin();
		double min = minX.getDoubleValue();
		double max = maxX.getDoubleValue();
		g.setColor(gridColor);
		
		/**/
		double firstTick = MesquiteDouble.firstScaleMark(max-min, min, minTicks);
		double tickIncrement = MesquiteDouble.scaleIncrement(max-min, min, minTicks);
		if (tickIncrement>0 && (max-firstTick)/tickIncrement<maxTicks) {
			double tick = firstTick;
			for (int count = 0;  tick<max; count++) {
			//for (double tick=firstTick; tick<max; tick += tickIncrement)
				drawXLine(tick, g, fieldHeight, chart);
				tick = tickIncrement*count + firstTick;
			}
		}
		min = minY.getDoubleValue();
		max = maxY.getDoubleValue();
		firstTick = MesquiteDouble.firstScaleMark(max-min, min, minTicks);
		tickIncrement = MesquiteDouble.scaleIncrement(max-min, min, minTicks);
		if (tickIncrement>0 && (max-firstTick)/tickIncrement<maxTicks)
			for (double tick=firstTick; tick<max; tick += tickIncrement)
				drawYLine(tick, g, fieldWidth, chart);
	}
	/* ----------------------------------*/
	protected void drawXLine(double x, Graphics g, int fieldHeight, MesquiteChart chart){
		int lineX = xToPixel(x, chart);
		g.drawLine(lineX, 0 , lineX, fieldHeight);
	}
	/* ----------------------------------*/
	protected void drawYLine(double y, Graphics g, int fieldWidth, MesquiteChart chart){
		int lineY =yToPixel(y, chart);
		g.drawLine(0, lineY,fieldWidth, lineY);
	}
	public abstract void open(MesquiteChart chart);
	public void close(){}
	public void mouseMoveInField(int modifiers, int x, int y, MesquiteChart chart, ChartTool tool) {}
	public void mouseDownInField(int modifiers, int x, int y, MesquiteChart chart, ChartTool tool) {}
	public void mouseDragInField(int modifiers, int x, int y, MesquiteChart chart, ChartTool tool) {}
	public void mouseUpInField(int modifiers, int x, int y, MesquiteChart chart, ChartTool tool) {}
	public void showQuickMessage(MesquiteChart chart, int whichPoint, int x, int y, String message){}//TODO: should be abstract to force inclusion!
	public void hideQuickMessage(MesquiteChart chart){}
}

