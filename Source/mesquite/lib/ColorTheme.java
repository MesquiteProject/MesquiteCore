package mesquite.lib;

import java.awt.Color;
import mesquite.lib.simplicity.*;

/* ======================================================================== */
/**A class to deal with color themes of windows.*/
public class ColorTheme {
	public static final int ORIGINAL = 0;
	public static final int PAPER = 1;
	public static final int MILKCHOCOLATE = 2;
	public static final int SLATE = 3;
	public static final int DARKCHOCOLATE = 4;

	public static int THEME = PAPER;
	public static int THEME_FOR_NEXT_STARTUP = PAPER;


	public static StringArray getColorThemes(){
		StringArray themes = new StringArray(5);
		themes.setValue(ORIGINAL, "Original");
		themes.setValue(DARKCHOCOLATE, "Dark Chocolate");
		themes.setValue(MILKCHOCOLATE, "Milk Chocolate");
		themes.setValue(SLATE, "Slate");
		themes.setValue(PAPER, "Paper");
		return themes;
	}
	static Color ContentBackground = new Color(235,235,235);
	static Color ContentBackgroundPale = new Color(240,240,240);
	static Color ContentElement = new Color(225,225,225);
	static Color ContentDarkElement = new Color(150,150,150);
	static Color ContentEdgePale = new Color(240,240,240);
	static Color ContentEdgeDark = new Color(150,150,150);
	static Color ContentFrame = new Color(88, 88, 88);

	public static Color getContentBackground(){
		return ContentBackground;
	}
	public static Color getContentBackgroundPale(){
		return ContentBackgroundPale;
	}
	public static Color getContentElement(){
		return ContentElement;
	}
	public static Color getContentDarkElement(){
		return ContentDarkElement;
	}
	public static Color getContentEdgePale(){
		return ContentEdgePale;
	}
	public static Color getContentEdgeDark(){
		return ContentEdgeDark;
	}

	public static Color getContentFrame(){
		return ContentFrame;
	}

	//background of EXTERNAL INTERFACE AREA (main tab panel, project resources panel)
	static Color[] ExtInterfaceBackground = new Color[5];
	static {
		ExtInterfaceBackground[ORIGINAL] = ColorDistribution.paleGoldenRod;
		ExtInterfaceBackground[DARKCHOCOLATE] = new Color(74, 68, 59);
		ExtInterfaceBackground[SLATE] = new Color(168,168,179); //slate theme
		ExtInterfaceBackground[MILKCHOCOLATE] = new Color(88, 82, 74);
		ExtInterfaceBackground[PAPER] = new Color(237, 232, 220); // lightening of tool palette background color
		//ExtInterfaceBackground[PAPER] = new Color(230, 234, 210); // pale greenish yellow new Color(249, 255, 237);;  greenish: new Color(236, 249, 244)
	}
	public static Color getExtInterfaceBackground(){  //general background to main tabs and project panel
		return ExtInterfaceBackground[THEME];

	}
	public static Color getExtInterfaceBackground(boolean isMesquiteWindow){  //general background to main tabs and project panel
		if (isMesquiteWindow) //main mesquite window with log etc. has chocolate color scheme
			return ExtInterfaceBackground[MILKCHOCOLATE];
		else
			return ExtInterfaceBackground[THEME];

	}
	/*-------------background of unselected outer tabs*/
	static Color[] ExtInterfaceElement = new Color[5];
	static {
		ExtInterfaceElement[ORIGINAL] = ColorDistribution.veryPaleGoldenRod; 
		ExtInterfaceElement[DARKCHOCOLATE] = new Color(88, 82, 74);//chocolate theme
		ExtInterfaceElement[SLATE] = new Color(188,188,200);//slate theme
		ExtInterfaceElement[MILKCHOCOLATE] = new Color(88, 82, 74);
		ExtInterfaceElement[PAPER] = new Color(237, 232, 220); // lightening of tool palette background color
		//ExtInterfaceBackground[PAPER] = new Color(230, 234, 210); // pale greenish yellow new Color(249, 255, 237);;  greenish: new Color(236, 249, 244)
	}
	public static Color getExtInterfaceElement(){ // slightly contrasting color for unselected main tabs and some parts of project panel
		return ExtInterfaceElement[THEME];
	}
	public static Color getExtInterfaceElement(boolean isMesquiteWindow){  //general background to main tabs and project panel
		if (isMesquiteWindow)
			return ExtInterfaceElement[MILKCHOCOLATE];
		else
			return ExtInterfaceElement[THEME];

	}
	/*-------------background of selected outer tabs (pale top color)*/
	static Color[] ExtInterfaceElementContrast = new Color[5];
	static {
		ExtInterfaceElementContrast[ORIGINAL] = ColorDistribution.bisque;
		ExtInterfaceElementContrast[DARKCHOCOLATE] = new Color(113, 108, 96); //chocolate theme   //formerly new Color(102, 98, 86); //chocolate theme
		ExtInterfaceElementContrast[SLATE] = new Color(221,221,232);//slate theme
		ExtInterfaceElementContrast[MILKCHOCOLATE] = new Color(102, 98, 86); //chocolate theme
		ExtInterfaceElementContrast[PAPER] = Color.white;
	}
	public static Color getExtInterfaceElementContrast(){   //element that is contrasted, e.g. the highlighted one, in the main tabs/project area
		return ExtInterfaceElementContrast[THEME];
	}
	public static Color getExtInterfaceElementContrast(boolean isMesquiteWindow){  //general background to main tabs and project panel
		if (isMesquiteWindow)
			return ExtInterfaceElementContrast[MILKCHOCOLATE];
		else
			return ExtInterfaceElementContrast[THEME];

	}
	/*-------------background of selected but NOT FOREMOST outer tabs (pale top color)*/
	static Color[] ExtInterfaceElementContrast2 = new Color[5];
	static {
		ExtInterfaceElementContrast2[ORIGINAL] = ColorDistribution.bisque;
		ExtInterfaceElementContrast2[DARKCHOCOLATE] = new Color(113, 108, 96); //chocolate theme   //formerly new Color(102, 98, 86); //chocolate theme
		ExtInterfaceElementContrast2[SLATE] = new Color(221,221,232);//slate theme
		ExtInterfaceElementContrast2[MILKCHOCOLATE] = new Color(102, 98, 86); //chocolate theme
		ExtInterfaceElementContrast2[PAPER] =new Color(242, 237, 225); // lightening of tool palette background color
		// ExtInterfaceElementContrast2[PAPER] = new Color(234, 239, 215);  //greenish new Color(242, 255, 250)
	}
	public static Color getExtInterfaceElementContrast2(){   //element that is contrasted, e.g. the highlighted one, in the main tabs/project area
		return ExtInterfaceElementContrast2[THEME];
	}
	public static Color getExtInterfaceElementContrast2(boolean isMesquiteWindow){  //general background to main tabs and project panel
		if (isMesquiteWindow)
			return ExtInterfaceElementContrast2[MILKCHOCOLATE];
		else
			return ExtInterfaceElementContrast2[THEME];

	}
	/*------------- edge highlight of selected outer tabs*/
	static Color[] ExtInterfaceEdgeContrast = new Color[5];
	static {
		ExtInterfaceEdgeContrast[ORIGINAL] = ColorDistribution.sienna;
		ExtInterfaceEdgeContrast[DARKCHOCOLATE] = new Color(210,205,195);//chocolate theme  //formerly new Color(190,185,175);//chocolate theme
		ExtInterfaceEdgeContrast[SLATE] = new Color(80,80,90);//slate theme
		ExtInterfaceEdgeContrast[MILKCHOCOLATE] = new Color(190,185,175);//chocolate theme
		ExtInterfaceEdgeContrast[PAPER] = ColorDistribution.veryDarkMesquiteBrown;
	}
	public static Color getExtInterfaceEdgeContrast(){
		return ExtInterfaceEdgeContrast[THEME];
	}
	public static Color getExtInterfaceEdgeContrast(boolean isMesquiteWindow){  //general background to main tabs and project panel
		if (isMesquiteWindow)
			return ExtInterfaceEdgeContrast[MILKCHOCOLATE];
		else
			return ExtInterfaceEdgeContrast[THEME];

	}
	/*-------------edge of unselected outer tabs*/
	static Color[] ExtInterfaceEdgeMuted = new Color[5];
	static {
		ExtInterfaceEdgeMuted[ORIGINAL] = ColorDistribution.sienna;
		ExtInterfaceEdgeMuted[DARKCHOCOLATE] = new Color(190,178,130);//chocolate theme
		ExtInterfaceEdgeMuted[SLATE] = new Color(68,68,78);//slate theme
		ExtInterfaceEdgeMuted[MILKCHOCOLATE] = new Color(190,178,130);//chocolate theme
		ExtInterfaceEdgeMuted[PAPER] = new Color(176,162,150);  // to go with pale brown color
		//ExtInterfaceEdgeMuted[PAPER] = new Color(176,176,150);  // to go with greenish color
	}
	public static Color getExtInterfaceEdgeMuted(){  //muted text in external interface area,e.g. unselected tabs
		return ExtInterfaceEdgeMuted[THEME];
	}
	public static Color getExtInterfaceEdgeMuted(boolean isMesquiteWindow){  //general background to main tabs and project panel
		if (isMesquiteWindow)
			return ExtInterfaceEdgeMuted[MILKCHOCOLATE];
		else
			return ExtInterfaceEdgeMuted[THEME];

	}
	/*-------------text of selected outer tabs*/
	static Color[] ExtInterfaceTextContrast = new Color[5];
	static {
		ExtInterfaceTextContrast[ORIGINAL] = Color.black;
		ExtInterfaceTextContrast[DARKCHOCOLATE] = new Color(238,224,210);//chocolate theme
		ExtInterfaceTextContrast[SLATE] = new Color(28,28,33); //slate theme
		ExtInterfaceTextContrast[MILKCHOCOLATE] = new Color(244,234,218);//chocolate theme
		ExtInterfaceTextContrast[PAPER] = Color.black;
	}
	public static Color getExtInterfaceTextContrast(){  //high contrast text in external interface area, e.g. highlighted tab
		return ExtInterfaceTextContrast[THEME];
	}
	public static Color getExtInterfaceTextContrast(boolean isMesquiteWindow){  //general background to main tabs and project panel
		if (isMesquiteWindow)
			return ExtInterfaceTextContrast[MILKCHOCOLATE];
		else
			return ExtInterfaceTextContrast[THEME];

	}
	/*-------------text of unselected outer tabs*/
	static Color[] ExtInterfaceTextMuted = new Color[5];
	static {
		ExtInterfaceTextMuted[ORIGINAL] = ColorDistribution.sienna;
		ExtInterfaceTextMuted[DARKCHOCOLATE] = new Color(190,178,130);//chocolate theme
		ExtInterfaceTextMuted[SLATE] = new Color(68,68,78);//slate theme
		ExtInterfaceTextMuted[MILKCHOCOLATE] = new Color(190,178,130);//chocolate theme
		ExtInterfaceTextMuted[PAPER] = new Color(106,98,80);  // to go with pale brown color
//		ExtInterfaceTextMuted[PAPER] = new Color(106,106,80);  // to go with greenish color
	}
	public static Color getExtInterfaceTextMuted(){  //muted text in external interface area,e.g. unselected tabs
		return ExtInterfaceTextMuted[THEME];
	}
	public static Color getExtInterfaceTextMuted(boolean isMesquiteWindow){  //general background to main tabs and project panel
		if (isMesquiteWindow)
			return ExtInterfaceTextMuted[MILKCHOCOLATE];
		else
			return ExtInterfaceTextMuted[THEME];

	}
	/*-------------text of project panel*/
	static Color[] ExtInterfaceTextMedium = new Color[5];
	static {
		ExtInterfaceTextMedium[ORIGINAL] = ColorDistribution.sienna;
		ExtInterfaceTextMedium[DARKCHOCOLATE] = new Color(210,190,154);//chocolate theme
		ExtInterfaceTextMedium[SLATE] = new Color(48,48,56); //slate theme
		ExtInterfaceTextMedium[MILKCHOCOLATE] = new Color(219,199,164); //chocolate theme
		ExtInterfaceTextMedium[PAPER] = new Color(106,98,80);  // to go with pale brown color
//		ExtInterfaceTextMedium[PAPER] = new Color(106,106,80);  // to go with greenish color
	}
	public static Color getExtInterfaceTextMedium(){ //medium contrast text in external interface area, e.g. project resources text
		return ExtInterfaceTextMedium[THEME];
	}
	/*-------------link text of project panel*/
	static Color[] ExtInterfaceTextLink = new Color[5];
	static {
		ExtInterfaceTextLink[ORIGINAL] =  Color.blue;
		ExtInterfaceTextLink[DARKCHOCOLATE] = Color.cyan;
		ExtInterfaceTextLink[SLATE]  = Color.blue;
		ExtInterfaceTextLink[MILKCHOCOLATE]  = Color.cyan;
		ExtInterfaceTextLink[PAPER] = Color.gray;
	}
	public static Color getExtInterfaceTextLink(){ //medium contrast text in external interface area, e.g. project resources text
		return ExtInterfaceTextLink[THEME];
	}
	
	/*-------------------------*/
	//INTERNAL INTERFACE AREA (tool palettes, graphics/text/etc tabs)
	static Color[] InterfaceBackground = new Color[5];
	static {
		InterfaceBackground[ORIGINAL] = ColorDistribution.paleGoldenRod;
		InterfaceBackground[DARKCHOCOLATE] = new Color(216,204,172);  // chocolate theme
		InterfaceBackground[SLATE] = new Color(224,229,244);  // slate theme
		InterfaceBackground[MILKCHOCOLATE] = new Color(216,204,172);  // chocolate theme
		InterfaceBackground[PAPER] = InterfaceBackground[DARKCHOCOLATE];
	}
	public static Color getInterfaceBackground(){  //background of tool palettes and small tabs bar
		return InterfaceBackground[THEME];
	}
	/*-------------*/
	static Color[] InterfaceBackgroundPale = new Color[5];
	static {
		InterfaceBackgroundPale[ORIGINAL] = ColorDistribution.veryPaleGoldenRod;
		InterfaceBackgroundPale[DARKCHOCOLATE] = new Color(234, 228,212); // chocolate theme
		InterfaceBackgroundPale[SLATE] = new Color(234,239,249);  // slate theme
		InterfaceBackgroundPale[MILKCHOCOLATE] = new Color(234, 228,212); // chocolate theme
		InterfaceBackgroundPale[PAPER] = InterfaceBackgroundPale[DARKCHOCOLATE];
	}
	public static Color getInterfaceBackgroundPale(){
		return InterfaceBackgroundPale[THEME];
	}
	/*-------------Unselected tools; unselected inner tabs*/
	static Color[] InterfaceElement = new Color[5];
	static {
		InterfaceElement[ORIGINAL] = ColorDistribution.bisque;
		InterfaceElement[DARKCHOCOLATE] = new Color(234, 228,212); // chocolate theme
		InterfaceElement[SLATE] = new Color(216,212,212);  // slate theme
		InterfaceElement[MILKCHOCOLATE] = new Color(234, 228,212); // chocolate theme
		InterfaceElement[PAPER] = InterfaceElement[DARKCHOCOLATE];
	}
	public static Color getInterfaceElement(){ //unselected tool buttons and small tab
		return InterfaceElement[THEME];
	}
	/*-------------selected inner tab */
	static Color[] InterfaceElementContrast = new Color[5];
	static {
		InterfaceElementContrast[ORIGINAL] = ColorDistribution.lightMesquiteBrown;
		InterfaceElementContrast[DARKCHOCOLATE] = new Color(236,230,222); // chocolate theme
		InterfaceElementContrast[SLATE] = new Color(228,228,228);  // slate theme
		InterfaceElementContrast[MILKCHOCOLATE] = new Color(236,230,222); // chocolate theme
		InterfaceElementContrast[PAPER] = InterfaceElementContrast[DARKCHOCOLATE];
	}
	public static Color getInterfaceElementContrast(){ //selected small tab
		return InterfaceElementContrast[THEME];

	}
	/*-------------*/
	static Color[] InterfaceEdgeNegative = new Color[5];
	static {
		InterfaceEdgeNegative[ORIGINAL] = ColorDistribution.sienna;
		InterfaceEdgeNegative[DARKCHOCOLATE] = new Color(226,222,222); // chocolate theme
		InterfaceEdgeNegative[SLATE] = new Color(142,142,146); // slate theme
		InterfaceEdgeNegative[MILKCHOCOLATE] = new Color(226,222,222); // chocolate theme
		InterfaceEdgeNegative[PAPER] = InterfaceEdgeNegative[DARKCHOCOLATE];
	}
	public static Color getInterfaceEdgeNegative(){  //edge to unselected small tab and tool button
		return InterfaceEdgeNegative[THEME];
	}
	/*-------------*/
	static Color[] InterfaceEdgePositive = new Color[5];
	static {
		InterfaceEdgePositive[ORIGINAL] = ColorDistribution.straw;
		InterfaceEdgePositive[DARKCHOCOLATE] = new Color(114,100,90); // chocolate theme
		InterfaceEdgePositive[SLATE] = new Color(70,80,104);  // slate theme
		InterfaceEdgePositive[MILKCHOCOLATE] = new Color(114,100,90); // chocolate theme
		InterfaceEdgePositive[PAPER] = InterfaceEdgePositive[DARKCHOCOLATE];
	}
	public static Color getInterfaceEdgePositive(){  //edge to selected small tab
		return InterfaceEdgePositive[THEME];
	}
	/*-------------*/
	static Color[] InterfaceTextContrast = new Color[5];
	static {
		InterfaceTextContrast[ORIGINAL] = Color.black;
		InterfaceTextContrast[DARKCHOCOLATE] = new Color(44,40,40);  //chocolate theme
		InterfaceTextContrast[SLATE] = new Color(40,40,44);  // slate theme
		InterfaceTextContrast[MILKCHOCOLATE] = new Color(44,40,40);  //chocolate theme
		InterfaceTextContrast[PAPER] = InterfaceTextContrast[DARKCHOCOLATE];
	}
	public static Color getInterfaceTextContrast(){  //contrasting text; must be opposite to element contrast; e.g. for selected tab
		return InterfaceTextContrast[THEME];
	}
	/*-------------*/
	static Color[] InterfaceTextMuted = new Color[5];
	static {
		InterfaceTextMuted[ORIGINAL] = ColorDistribution.sienna;
		InterfaceTextMuted[DARKCHOCOLATE] = new Color(64,60,60); // chocolate theme
		InterfaceTextMuted[SLATE] = new Color(60,60,64);  // slate theme
		InterfaceTextMuted[MILKCHOCOLATE] = new Color(64,60,60); // chocolate theme
		InterfaceTextMuted[PAPER] = InterfaceTextMuted[DARKCHOCOLATE];
	}
	public static Color getInterfaceTextMuted(){  //muted text; e.g. for unselected tabs
		return InterfaceTextMuted[THEME];
	}


	/*-------------*/
	static Color[] ActiveLight = new Color[5];
	static {
		ActiveLight[ORIGINAL] = ColorDistribution.lightMesquiteBrown;
		ActiveLight[DARKCHOCOLATE] = new Color(252, 239, 170);  // chocolate theme
		ActiveLight[SLATE] = new Color(185, 214, 252);  // slate theme
		ActiveLight[MILKCHOCOLATE] = new Color(242, 224, 185);  // chocolate theme
		ActiveLight[PAPER] = ActiveLight[DARKCHOCOLATE];
	}
	public static Color getActiveLight(){  //currently used for selected tool button
		return ActiveLight[THEME];
	}
	/*-------------*/
	static Color[] ActiveDark = new Color[5];
	static {
		ActiveDark[ORIGINAL] = ColorDistribution.mesquiteBrown;
		ActiveDark[DARKCHOCOLATE] = new Color(160, 82, 45);  // chocolate theme
		ActiveDark[SLATE] = new Color(45, 82, 190);  // slate theme
		ActiveDark[MILKCHOCOLATE] = new Color(160, 82, 45);  // chocolate theme
		ActiveDark[PAPER] = ActiveDark[DARKCHOCOLATE];
	}
	public static Color getActiveDark(){ //currently used for selected tool button edge
		return ActiveDark[THEME];
	}

}
