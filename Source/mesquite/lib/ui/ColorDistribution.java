/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib.ui;

import java.awt.*;

import mesquite.lib.Debugg;
import mesquite.lib.MesquiteInteger;
import mesquite.lib.MesquiteModule;
import mesquite.lib.MesquiteProject;
import mesquite.lib.NameReference;
import mesquite.lib.ObjectArray;
import mesquite.lib.Parser;
import mesquite.lib.RandomBetween;
import mesquite.lib.StringArray;
import mesquite.lib.duties.*;

/* ======================================================================== */
/**A class to indicate how something is to be colored, either an array of colors to be shown in equal pieces, or 
same with distribution of weights for pie slices.  A maximum number of MAXCOLORS colors may be shown.*/
public class ColorDistribution {
	Color[] colors, colorsDimmed;
	int numColors=0;
	double[] weights;
	boolean sequential = false;
	static final int MAXCOLORS = 64;

	public static int numberOfRed = 5;
	public static int numberOfGreen = 11;
	public static int numberOfBlue = 14;
	public static Color lightGreen, veryLightGreen, darkGreen, lightGreenYellow, lightGreenYellowish, lightBlue, darkBlue, veryLightBlue, veryVeryLightBlue, violetBlue, veryLightGray, veryVeryLightGray, veryVeryVeryLightGray, lightRed, darkRed, veryVeryLightGreen;
	public static Color darkBrown, brown, lightOrange, lightPurple, orange, straw, lightYellow, veryLightYellow, tabLineBrown, mesquiteBrown, darkMesquiteBrown, veryDarkMesquiteBrown, lightMesquiteBrown, brightMesquiteBrown;
	public static Color lightBlueGray;
	public static Color uneditable;
	public static Color unassigned;
	public static Color inapplicable;

	public static Color [] codPosMedium, codPosDark;
	public static Color spinDark, spinLight;
	//	public static Color[] projectLight, projectDark; //pale, light, medium, dark, project, 
	public final static int numColorSchemes = 4;
	public static Color burlyWood, navajoWhite, bisque, sienna, paleGoldenRod, veryPaleGoldenRod;

	public static NameReference colorRGBNameReference;

	public static StringArray standardColorNames;
	static ObjectArray standardColors, standardColorsDimmed, standardColorsAsHex;
	static int[] translationOldToNewStandardNumbers;
	public static double dimmingConstant = 0.3;
	public static int NO_COLOR = 18;
	static {
		spinLight = new Color((float)0.3, (float)0.6, (float)0.99);
		spinDark = new Color((float)0.1, (float)0.1, (float)0.70);
		veryLightGray = brighter(Color.lightGray, 0.5);
		veryVeryLightGray = brighter(veryLightGray, 0.5);

		//inapplicable = veryLightGray;
		inapplicable =  new Color((float)0.93, (float)0.90, (float)0.87);  //ColorDistribution.inapplicable;

		//unassigned = new Color(230, 230, 230);
		unassigned = new Color((float)0.92, (float)0.94, (float)0.98); //ColorDistribution.unassigned;

		veryVeryVeryLightGray = new Color((float)0.98, (float)0.98, (float)0.98);
		darkRed = new Color((float)0.5, (float)0.2, (float)0.1);
		lightRed = new Color((float)0.9, (float)0.48, (float)0.35);
		darkGreen = new Color((float)0.1, (float)0.5, (float)0.2);
		lightGreen = new Color((float)0.35, (float)0.9, (float)0.48);
		lightGreenYellowish =  new Color((float)0.50, (float)0.99, (float)0.46);  
		lightGreenYellow =  new Color((float)0.46, (float)0.99, (float)0.25);  
		veryLightGreen = new Color((float)0.55, (float)0.99, (float)0.68);
		veryVeryLightGreen = new Color((float)0.70, (float)0.99, (float)0.83);
		darkBlue = new Color((float)0.0, (float)0.0, (float)0.7);
		lightBlue = new Color((float)0.35, (float)0.48, (float)0.9);
		veryLightBlue = new Color((float)0.55, (float)0.68, (float)0.99);
		veryVeryLightBlue = new Color((float)0.85, (float)0.85, (float)0.99);
		lightOrange = new Color((float)1, (float)0.8, (float)0);
		orange = new Color((float)1, (float)0.5, (float)0);

		lightPurple = new Color((float)0.7, (float)0.40, (float)0.7);
		violetBlue = new Color((float)0.55, (float)0.40, (float)0.89);
		tabLineBrown = new Color((float)0.47, (float)0.41, (float)0.26);

		darkBrown = new Color((float)0.45, (float)0.40, (float)0.15);
		brown = new Color((float)0.65, (float)0.58, (float)0.25);
		straw = new Color((float)0.85, (float)0.80, (float)0.38);
		lightYellow = new Color((float)0.95, (float)0.95, (float)0.64);
		veryLightYellow = new Color((float)0.99, (float)0.99, (float)0.78);

		burlyWood = new Color((float)0.87, (float)0.7216, (float)0.5294); //222, 184, 135  DEB887; medium  SHOULD BE 0.87, (float)0.7216, (float)0.5294
		navajoWhite =  new Color((float)1.0, (float)0.87, (float)0.6784); //FFDEAD; light
		bisque =  new Color((float)1.0, (float)0.894, (float)0.7686); // FFE4C4; pale
		sienna =  new Color((float)0.6275, (float)0.3216, (float)0.1765); // A0522D; dark
		paleGoldenRod = new Color((float)0.9333, (float)0.9398, (float)0.6667); //EEE8AA  green should be 0.9333, 0.9098, 0.66667
		veryPaleGoldenRod = brighter(paleGoldenRod,0.5);

		lightMesquiteBrown = new Color(188, 168, 122);
		brightMesquiteBrown = new Color(228, 200, 132);
		mesquiteBrown = new Color(108, 98, 82);
		darkMesquiteBrown = new Color(92, 82, 70);
		veryDarkMesquiteBrown = new Color(78, 68, 55);

		lightBlueGray = new Color((float)0.7, (float)0.7, (float)0.8);

		//spinLight = new Color((float)0.6, (float)0.9, (float)0.6);
		//spinDark = new Color((float)0.05, (float)0.5, (float)0.05);
		//		darkMesquiteBrown = new Color(82, 72, 60);
		//mesquiteBrown = new Color(88, 78, 62);
		//mesquiteBrown = new Color(77, 65, 47);

		codPosMedium = new Color[4];
		codPosDark = new Color[4];
		codPosDark[0] = new Color((float)0.1, (float)0.2, (float)0.5);   // first positions, blue
		//codPosMedium[0] = new Color((float)0.35, (float)0.48, (float)0.9);
		codPosMedium[0] = new Color((float)0.45, (float)0.55, (float)0.94);
		codPosDark[1] = new Color((float)0.1, (float)0.5, (float)0.2);   // second positions, green
		codPosMedium[1] = new Color((float)0.35, (float)0.9, (float)0.48);
		codPosDark[2] = new Color((float)0.5, (float)0.2, (float)0.1);   // third positions, red
		codPosMedium[2] = new Color((float)0.9, (float)0.48, (float)0.35);
		codPosDark[3] = Color.gray;       // noncoding, unspecified
		codPosMedium[3] = Color.lightGray;


		uneditable = lightYellow;

		colorRGBNameReference = NameReference.getNameReference("!color");
		standardColors = new ObjectArray(18);
		standardColors.setValue(0, Color.black);
		standardColors.setValue(1, Color.darkGray);
		standardColors.setValue(2, Color.gray);
		standardColors.setValue(3, Color.lightGray);
		standardColors.setValue(4, Color.white);
		standardColors.setValue(5, Color.red); //	public static int numberOfRed = 5;

		standardColors.setValue(6, Color.orange);
		standardColors.setValue(7, Color.yellow);
		standardColors.setValue(8, paleGoldenRod);
		standardColors.setValue(9, burlyWood);
		standardColors.setValue(10, sienna);
		standardColors.setValue(11, Color.green);//	public static int numberOfGreen = 11;
		standardColors.setValue(12, lightGreen);
		standardColors.setValue(13, Color.cyan);
		standardColors.setValue(14, Color.blue); // public static int numberOfBlue = 14;
		standardColors.setValue(15, lightBlue);
		standardColors.setValue(16, Color.magenta);
		standardColors.setValue(17, Color.pink);

		standardColorsAsHex = new ObjectArray(18);
		for (int i=0; i<standardColorsAsHex.getSize(); i++)
			standardColorsAsHex.setValue(i, hexFromColor((Color)standardColors.getValue(i)));


		//DO NOT ASSIGN A COLOR AT OR ABOVE NO_COLOR (currently 18)
		standardColorNames = new StringArray(18);
		standardColorNames.setValue(0, "Black");
		standardColorNames.setValue(5, "Red");
		standardColorNames.setValue(11, "Green");
		standardColorNames.setValue(14, "Blue");
		standardColorNames.setValue(1, "Dark Gray");
		standardColorNames.setValue(2, "Gray");
		standardColorNames.setValue(3, "Light Gray");
		standardColorNames.setValue(4, "White");
		standardColorNames.setValue(16, "Magenta");
		standardColorNames.setValue(17, "Pink");
		standardColorNames.setValue(6, "Orange");
		standardColorNames.setValue(7, "Yellow");
		standardColorNames.setValue(8, "Goldenrod");
		standardColorNames.setValue(9, "Wood");
		standardColorNames.setValue(10, "Sienna");
		standardColorNames.setValue(12, "Light Green");
		standardColorNames.setValue(13, "Cyan");
		standardColorNames.setValue(15, "Light Blue");

		translationOldToNewStandardNumbers = new int[]{}; //Debugg.println()

		//DO NOT ASSIGN A COLOR AT OR ABOVE NO_COLOR (currently 18)
		standardColorsDimmed = new ObjectArray(18);
		standardColorsDimmed.setValue(0, Color.gray);
		standardColorsDimmed.setValue(1, brighter(Color.darkGray, dimmingConstant));
		standardColorsDimmed.setValue(2, brighter(Color.gray, dimmingConstant));
		standardColorsDimmed.setValue(3, brighter(Color.lightGray, dimmingConstant));
		standardColorsDimmed.setValue(4, Color.white);
		standardColorsDimmed.setValue(5, brighter(Color.red, dimmingConstant));
		standardColorsDimmed.setValue(6, brighter(Color.orange, dimmingConstant));
		standardColorsDimmed.setValue(7, brighter(Color.yellow, dimmingConstant));
		standardColorsDimmed.setValue(8, brighter(paleGoldenRod, dimmingConstant));
		standardColorsDimmed.setValue(9, brighter(burlyWood, dimmingConstant));
		standardColorsDimmed.setValue(10, brighter(sienna, dimmingConstant));
		standardColorsDimmed.setValue(11, brighter(Color.green, dimmingConstant));
		standardColorsDimmed.setValue(12, brighter(lightGreen, dimmingConstant));
		standardColorsDimmed.setValue(13, brighter(Color.cyan, dimmingConstant));
		standardColorsDimmed.setValue(14, brighter(Color.blue, dimmingConstant));
		standardColorsDimmed.setValue(15, brighter(lightBlue, dimmingConstant));
		standardColorsDimmed.setValue(16, brighter(Color.magenta, dimmingConstant));
		standardColorsDimmed.setValue(17, brighter(Color.pink, dimmingConstant));
		//DO NOT ASSIGN A COLOR AT OR ABOVE NO_COLOR (currently 18)
	}
	public ColorDistribution( Color c) {
		this();
		setColor(0, c);
	}
	public ColorDistribution() {
		colors = new Color[MAXCOLORS];
		colorsDimmed = new Color[MAXCOLORS];
		weights = new double[MAXCOLORS];

	}

	/*--------------------------------------------------------------*/
	/* Random colors originally made for auto-coloring of character groups during matrix concatenation*/

	static int enhancement = 20;
	private static int enhance(int v){
		v = v*enhancement/10;
		if (v>255)
			v = 255;
		return v;
	}
	private static int dehance(int v){
		if (RandomBetween.getIntStatic(0,100)>50)
			v = v*10/enhancement*10/enhancement;
		return v;
	}

	private static boolean tooClose(int red, int green, int blue, Color previous){
		if (previous == null)
			return false;
		//red is biggest in both
		if (red > green && red > blue && previous.getRed()>previous.getGreen() && previous.getRed()>previous.getBlue())
			return true;
		//green is biggest in both
		if (green > red && green > blue && previous.getGreen()>previous.getRed() && previous.getGreen()>previous.getBlue())
			return true;
		//blue is biggest in both
		if (blue > red && blue > green && previous.getBlue()>previous.getRed() && previous.getBlue()>previous.getGreen())
			return true;
		return false;
		//return (Math.abs(previous.getRed()-red) + Math.abs(previous.getGreen()-green) + Math.abs(previous.getBlue()-blue)<300);
	}
	public static Color getRandomColor(Color previous){
		int red, green, blue = 0;
		do {
			red = RandomBetween.getIntStatic(100,255);
			green = RandomBetween.getIntStatic(100,255);
			blue = RandomBetween.getIntStatic(100,255);
		} 
		while (tooClose(red, green, blue, previous));

		if (red>green){
			if (red>blue){ //red is biggest
				red = enhance(red);
				blue = dehance(blue);
				if (RandomBetween.getIntStatic(0,100)>75){//give it a chance to shift yellow
					green = enhance(green);
					blue = dehance(blue);
				}
				else {
					green = dehance(green);
				}
			}
			else { //blue is biggest
				if (RandomBetween.getIntStatic(0,100)>10) //usually shift down red and green
					red = dehance(red);
				if (RandomBetween.getIntStatic(0,100)>10)
					green = dehance(green);
				blue = enhance(blue);
			}
		}
		else if (blue>green){ //blue is biggest
			if (RandomBetween.getIntStatic(0,100)>10) //usually shift down red and green
				red = dehance(red);
			if (RandomBetween.getIntStatic(0,100)>10)
				green = dehance(green);
			blue = enhance(blue);
		}
		else { //green is biggest
			if (RandomBetween.getIntStatic(0,100)>75){ //give it a chance to shift yellow
				red = enhance(red);
				blue = dehance(blue);  //extra dehance here
			}
			else
				red = dehance(red);
			green = enhance(green);
			blue = dehance(blue);
		}

		return new Color(red, green, blue);
	}
	/*--------------------------------------------------------------*/

	private static float brighten(int v, double percent){
		float b = (float)((255-(255-v)*percent)/255);
		if (b<0)
			b=0;
		else if (b>1)
			b=1;
		return b;
	}

	public static Composite getComposite(Graphics g) {
		if (g!=null && (g instanceof Graphics2D)) {
			return ((Graphics2D)g).getComposite(); 
		}
		return null;
	}

	public static void setComposite(Graphics g, Composite composite) {
		if (g!=null) {
			if (g instanceof Graphics2D) {
				if (composite ==null)
					((Graphics2D)g).setComposite(AlphaComposite.SrcOver);
				else 
					((Graphics2D)g).setComposite(composite); 
			}

			else g.setPaintMode();
		}
	}

	static Composite alphaComposite2 =  AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
	static Composite alphaComposite3 =  AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
	static Composite alphaComposite5 =  AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	static Composite storedComposite = null;
	static float prevCompositeFloat = 0;

	public static void setTransparentGraphics(Graphics g, float f) {
		if (g!=null && (g instanceof Graphics2D)) {
			if (f>0.0f && f<1.0f) {
				if (storedComposite == null || f != prevCompositeFloat) {
					storedComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f);
				}
				((Graphics2D)g).setComposite(storedComposite); 
			}
			//			else if (f==0.0f)
			//				((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.00001f)); 
		}
	}
	public static void setTransparentGraphics(Graphics g, Composite composite) {
		if (g!=null && (g instanceof Graphics2D)) {
			((Graphics2D)g).setComposite(composite); 
		}

	}
	public static void setTransparentGraphics2(Graphics g) {
		setTransparentGraphics(g,alphaComposite2); 
	}
	public static void setTransparentGraphics3(Graphics g) {
		setTransparentGraphics(g,alphaComposite3); 
	}
	public static void setTransparentGraphics5(Graphics g) {
		setTransparentGraphics(g,alphaComposite5); 
	}
	public static void setTransparentGraphics(Graphics g) {
		setTransparentGraphics(g,alphaComposite5); 
	}
	public static void setOpaqueGraphics(Graphics g) {
		if (g!=null  && (g instanceof Graphics2D))
			((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1));   
	}
	
	
	public static Color brighter(Color c, double percent){
		if (c==null)
			return null;
		int green = c.getGreen();
		int red = c.getRed();
		int blue = c.getBlue();
		return new Color(brighten(red, percent), brighten(green, percent), brighten(blue, percent));
	}
	private static float darken(int v, double percent){
		float b = (float)((v*percent)/255);
		if (b<0)
			b=0;
		else if (b>1)
			b=1;
		return b;
	}

	public static Color darker(Color c, double percent){
		if (c==null)
			return null;
		int green = c.getGreen();
		int red = c.getRed();
		int blue = c.getBlue();
		return new Color(darken(red, percent), darken(green, percent), darken(blue, percent));
	}
	public static Color getContrasting(boolean selected, Color background, float[] hsbBackground, Color light, Color dark){
		if (selected){
			if (background != null && background.equals(light)){
				return dark;
			}
			return light;
		}
		else if (hsbBackground[2]>0.5)
			return dark;
		else
			return light;
	}
	private static boolean lightColor(Color c) {
		int highBreak=155;
		int lowBreak = 100;
		if (c.getRed()>highBreak || c.getGreen() > highBreak || c.getGreen() > highBreak)  // all are high values
			return true;
		if (c.getRed()>lowBreak && c.getGreen() > lowBreak && c.getGreen() > lowBreak && c.getRed()<highBreak && c.getGreen() < highBreak && c.getGreen() < highBreak)  // grey
			return true;
		return false;
	}
	private static boolean darkColor(Color c) {
		int highBreak=155;
		int lowBreak = 100;
		if (c.getRed()<lowBreak && c.getGreen() < lowBreak && c.getGreen() < lowBreak)
			return true;
		return false;
	}
	public static Color getContrastingTextColor(Color backgroundColor){
		if (backgroundColor==null){
			return Color.black;
		}
		int highBreak=155;
		int lowBreak = 100;
		if (lightColor(backgroundColor))
			return Color.black;
		if (darkColor(backgroundColor))
			return Color.white;
		int red = (255-backgroundColor.getRed());
		int green = (255-backgroundColor.getGreen());
		int blue = (255-backgroundColor.getBlue());
		return new Color(red,green,blue);
	}
	public static int getNumStandardColors(){
		return standardColors.getSize();
	}
	public static int getStandardColorNumber(String name){
		int ci = standardColorNames.indexOf(name);
		return ci;
	}
	public static int getStandardColorNumber(Color color){
		int ci = standardColors.indexOf(color);
		return ci;
	}
	public static Color getStandardColor(String name){
		int ci = standardColorNames.indexOf(name);
		if (ci<0)
			return null;
		return (Color)standardColors.getValue(ci);
	}
	public static String getStandardColorName(Color color){
		if (color==null)
			return null;
		int ic = standardColors.indexOf(color);
		if (ic>=0)
			return standardColorNames.getValue(ic);
		else
			return null;
	}
	public static String getStandardColorName(int ci){
		if (ci<0 || ci>=standardColorNames.getSize())
			return null;
		return (String)standardColorNames.getValue(ci);
	}
	public static Color getStandardColor(int ci){
		if (ci<0 || ci>=standardColors.getSize())
			return null;
		return (Color)standardColors.getValue(ci);
	}
	public static String getStandardColorAsHex(int ci){
		if (ci<0)
			return null;
		return (String)standardColorsAsHex.getValue(ci);
	}
	public static Color getStandardColorDimmed(int ci){
		if (ci<0 || ci>=standardColorsDimmed.getSize())
			return null;
		return (Color)standardColorsDimmed.getValue(ci);
	}
	public static boolean equalColors(Color color1, Color color2) {
		if (color1==null || color2==null)
			return false;
		return (color1.getBlue()==color2.getBlue()&&color1.getRed()==color2.getRed()&&color1.getGreen()==color2.getGreen());
	}
	/** Initialize colors by setting the number of colors to 0, the weights to 0, and the colors to null*/
	public void initialize() {
		numColors=0;
		for (int i=0; i<MAXCOLORS; i++) {
			colors[i]=null;
			colorsDimmed[i]=null;
			weights[i]=0;
		}
	}
	/** set color for state (or other unit) i to the given color.*/
	public void setColor(int i, Color color) {
		if (i>=0 && i<MAXCOLORS) {
			if (colors[i]==null)
				numColors++;
			else if (color==null)
				numColors--;
			colors[i]=color;
			Color dimmed = color;
			if (color !=null){
				if ( color.getGreen()==0 && color.getRed() ==0 && color.getBlue()==0)
					dimmed = Color.gray;
				else
					dimmed = ColorDistribution.brighter(color, dimmingConstant);
			}
			colorsDimmed[i] = dimmed;
		}
	}
	/** add a color to an available slot.*/
	public void addColor(Color color) {
		if (color == null || colors == null)
			return;
		for (int i=0; i<colors.length; i++){
			if (colors[i] == null) {
				setColor(i, color);
				return;
			}
		}
	}
	/** whether a color is already in the distribution.*/
	public boolean colorPresent(Color color) {
		if (colors == null || color == null)
			return false;
		for (int i=0; i<colors.length; i++){
			if (colors[i] !=null)
				if (color.getRed() == colors[i].getRed() && color.getGreen() == colors[i].getGreen() && color.getBlue() == colors[i].getBlue())
					return true;
		}
		return false;
	}
	/** set color for state (or other unit) i to the given color.*/
	public void concatenate(ColorDistribution other) {
		if (other == null)
			return;
		for (int k = 0; k<other.numColors && numColors <MAXCOLORS; k++) {
			if (other.colors[k]!=null && !colorPresent(other.colors[k]))
				addColor(other.colors[k]);
		}
	}

	public static boolean hexColorsEqual(String c1, String c2){
		if (c1 == null || c2== null)
			return (c1 == null && c2 == null);
		return c1.equalsIgnoreCase(c2);
	}
	/** finds name of color.*/
	public static String standardName(Color color) {
		if (color == null)
			return "";
		int which = indexOfStandard(color);
		if (which >=0 && which<standardColorNames.getSize())
			return standardColorNames.getValue(which);
		return "";
	}
	/** finds name of color.*/
	public static String standardName(String hex) {
		Color color = colorFromHex(hex);
		if (color == null)
			return "";
		int which = indexOfStandard(color);
		if (which >=0 && which<standardColorNames.getSize())
			return standardColorNames.getValue(which);
		return "";
	}
	/** finds index of color.*/
	public static int indexOfStandard(Color color) {
		if (color == null)
			return -1;
		for (int i=0; i<standardColors.getSize(); i++)
			if (standardColors.getValue(i) != null && color.equals(standardColors.getValue(i))) 
				return i;
		return -1;
	}
	/** finds index of color.*/
	public int indexOf(Color color) {
		if (color == null)
			return -1;
		for (int i=0; i<colors.length; i++)
			if (colors[i] != null && color.equals(colors[i])) 
				return i;
		return -1;
	}
	/*--------------------------------------------------------------*/
	private static String fillToTwo(String s){
		if (s.length() >=2)
			return s;
		else if (s.length() == 1)
			return "0" + s;
		else
			return "00";
	}
	public static String hexFromColor(long oldCode){
		Color std = getStandardColor((int)oldCode);
		return hexFromColor(std);
	}
	public static String hexFromColor(Color c){
		if (c == null)
			return null;
		return "#" + fillToTwo(Integer.toHexString(c.getRed())) + fillToTwo(Integer.toHexString(c.getGreen())) + fillToTwo(Integer.toHexString(c.getBlue()));
	}
	public static Color getColorFromArguments(String arguments, MesquiteInteger pos) {
		String token = Parser.getNextTabbedToken(arguments, pos);
		return colorFromHex(token);
	}
	public static Color colorFromHex(String hex) {
		try {
			Color color = Color.decode(hex);
			return color;
		}
		catch (Exception e){
		}
		return null;
	}
	/*--------------------------------------------------------------*/

	public static String getColorStringForSnapshot(Color color) {
		if (color == null)
			return "";
		return color.getRed() + " " + color.getGreen() + " " + color.getBlue();
	}

	/** gets the color for unit i*/
	public Color getColor(int i) {
		return colors[i];
	}
	/** gets the color for unit i*/
	public Color getColor(int i, boolean regularStrength) {
		if (regularStrength)
			return colors[i];
		else
			return colorsDimmed[i];
	}
	/** sets the weight (for pie charts) for unit i*/
	public void setWeight(int i, double weight) {
		if (i>=0 && i<MAXCOLORS) {
			weights[i]=weight;
		}
	}
	/** get the weight for state (or other unit) i*/
	public double getWeight(int i) {
		return weights[i];
	}
	/** sets whether or not the colors are sequential, e.g. as for stochastic character mapping. 
	 In this case the weights are the point at which the change occurs, e.g. proportional position on branch*/
	public void setSequential(boolean s){
		sequential = s;
	}
	/** returns whether the colors are to be considered sequential */
	public boolean getSequential(){
		return sequential;
	}
	/** get the total number of colors assigned*/
	public boolean anyColors() {
		return !(numColors== 0 || colors == null || colors.length == 0);
	}
	/** get the total number of colors assigned*/
	public int getNumColors() {
		return numColors;
	}
	public static int getColorScheme(MesquiteModule mod){
		if (mod == null || mod.getProject()==null)
			return 0;
		else
			return mod.getProject().getProjectColor();
	}
	public static int getColorScheme(MesquiteProject mp){
		if (mp == null)
			return 0;
		else
			return mp.getProjectColor();
	}
	public String toString(){
		String s = "ColorDistribution: ";
		if (colors == null)
			return s + " NO COLORS";
		for (int i=0; i <colors.length && i<numColors; i++)
			s += " " + i + ": " + colors[i] + " (weight " + weights[i] + ");";
		return s;
	}
}

