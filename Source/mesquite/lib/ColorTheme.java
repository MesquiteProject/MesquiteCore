package mesquite.lib;

import java.awt.Color;

/* ======================================================================== */
/**A class to deal with color themes of windows.*/
public class ColorTheme {
	public static int THEME = 1;
	public static final int DARKCHOCOLATE = 1;
	public static final int MILKCHOCOLATE = 2;
	public static final int SLATE = 3;

	public static Color getContentBackground(){
		return new Color(235,235,235);
	}
	public static Color getContentBackgroundPale(){
		return new Color(240,240,240);
	}
	public static Color getContentElement(){
		return new Color(225,225,225);
	}
	public static Color getContentDarkElement(){
		return new Color(150,150,150);
	}
	public static Color getContentEdgePale(){
		return new Color(240,240,240);
	}
	public static Color getContentEdgeDark(){
		return new Color(150,150,150);
	}

	

	//THESE SHOULD ALL BE REPLACED BY references to static colors, rather than having to instantiate a colour each time

	//EXTERNAL INTERFACE AREA (main tab panel, project resources panel)
	public static Color getExtInterfaceBackground(){  //general background to main tabs and project panel
		if (THEME == DARKCHOCOLATE)
			return new Color(74, 68, 59);//chocolate theme
		else if (THEME == SLATE)
			return new Color(168,168,179); //slate theme
		else
			return new Color(88, 82, 74);//chocolate theme

	}
	public static Color getExtInterfaceElement(){ // slightly contrasting color for unselected main tabs and some parts of project panel
		if (THEME == DARKCHOCOLATE)
			return new Color(88, 82, 74);//chocolate theme
		else if (THEME == SLATE)
			return new Color(188,188,200);//slate theme
		else
			return new Color(108, 102, 94);//chocolate theme
	}
	public static Color getExtInterfaceElementContrast(){   //element that is contrasted, e.g. the highlighted one, in the main tabs/project area
		if (THEME == DARKCHOCOLATE)
			return new Color(102, 98, 86); //chocolate theme
		else if (THEME == SLATE)
			return new Color(221,221,232);//slate theme
		else
			return new Color(102, 98, 86); //chocolate theme
	}
	public static Color getExtInterfaceEdgeContrast(){
		if (THEME == DARKCHOCOLATE)
			return new Color(190,185,175);//chocolate theme
		else if (THEME == SLATE)
			return new Color(80,80,90);//slate theme
		else
			return new Color(190,185,175);//chocolate theme
	}
	public static Color getExtInterfaceTextContrast(){  //high contrast text in external interface area, e.g. highlighted tab
		if (THEME == DARKCHOCOLATE)
			return new Color(238,224,210);//chocolate theme
		else if (THEME == SLATE)
			return new Color(28,28,33); //slate theme
		else
			return new Color(238,224,210);//chocolate theme
	}
	public static Color getExtInterfaceTextMedium(){ //medium contrast text in external interface area, e.g. project resources text
		if (THEME == DARKCHOCOLATE)
			return new Color(210,190,154);//chocolate theme
		else if (THEME == SLATE)
			return new Color(48,48,56); //slate theme
		else
			return new Color(210,190,154); //chocolate theme
	}
	public static Color getExtInterfaceTextMuted(){  //muted text in external interface area,e.g. unselected tabs
		if (THEME == DARKCHOCOLATE)
			return new Color(190,178,130);//chocolate theme
		else if (THEME == SLATE)
			return new Color(68,68,78);//slate theme
		else
			return new Color(190,178,130);//chocolate theme

	}

	//INTERNAL INTERFACE AREA (tool palettes, graphics/text/etc tabs)
	public static Color getInterfaceBackground(){  //background of tool palettes and small tabs bar
		if (THEME == DARKCHOCOLATE)
			return new Color(216,204,172);  // chocolate theme  
		else if (THEME == SLATE)
			return new Color(224,229,244);  // slate theme  
//			return new Color(178,186,204);  // slate theme  
		else
			return new Color(216,204,172);  // chocolate theme  
	}
	public static Color getInterfaceBackgroundPale(){
		if (THEME == DARKCHOCOLATE)
			return new Color(234, 228,212); // chocolate theme 
		else if (THEME == SLATE)

			return new Color(234,239,249);  // slate theme 
		else
			return new Color(234, 228,212); // chocolate theme 
	}
	public static Color getInterfaceElement(){ //unselected tool buttons and small tab
		if (THEME == DARKCHOCOLATE)
			return new Color(234, 228,212); // chocolate theme 
		else if (THEME == SLATE)
			return new Color(216,212,212);  // slate theme 
		else
			return new Color(234, 228,212); // chocolate theme 
	}
	public static Color getInterfaceElementContrast(){ //selected small tab
		if (THEME == DARKCHOCOLATE)
			return new Color(236,230,222); // chocolate theme 
		else if (THEME == SLATE)
			return new Color(228,228,228);  // slate theme 
		else
			return new Color(236,230,222); // chocolate theme 

	}
	public static Color getInterfaceEdgeNegative(){  //edge to unselected small tab and tool button
		if (THEME == DARKCHOCOLATE)
			return new Color(226,222,222); // chocolate theme 
		else if (THEME == SLATE)
			return new Color(142,142,146); // slate theme 
		else
			return new Color(226,222,222); // chocolate theme 

	}
	public static Color getInterfaceEdgePositive(){  //edge to selected small tab
		if (THEME == DARKCHOCOLATE)
			return new Color(114,100,90); // chocolate theme 
		else if (THEME == SLATE)
			return new Color(70,80,104);  // slate theme 
		else
			return new Color(114,100,90); // chocolate theme 
	}
	public static Color getInterfaceTextContrast(){  //contrasting text; must be opposite to element contrast; e.g. for selected tab
		if (THEME == DARKCHOCOLATE)
			return new Color(44,40,40);  //chocolate theme
		else if (THEME == SLATE)
			return new Color(40,40,44);  // slate theme 
		else
			return new Color(44,40,40);  //chocolate theme
	}
	public static Color getInterfaceTextMuted(){  //muted text; e.g. for unselected tabs
		if (THEME == DARKCHOCOLATE)
			return new Color(64,60,60); // chocolate theme 
		else if (THEME == SLATE)
			return new Color(60,60,64);  // slate theme 
		else
			return new Color(64,60,60); // chocolate theme 
	}


	public static Color getActiveLight(){  //currently used for selected tool button
		if (THEME == DARKCHOCOLATE)
			return new Color(252, 239, 170);
		else if (THEME == SLATE)
			return new Color(185, 214, 252);
		else
			return new Color(242, 224, 185);

	}
	public static Color getActiveDark(){ //currently used for selected tool button edge
		if (THEME == DARKCHOCOLATE)
		return new Color(160, 82, 45);   //just brown
		else if (THEME == SLATE)
			return new Color(45, 82, 190);  
		else
			return new Color(160, 82, 45);   
	}

}
