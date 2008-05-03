package mesquite.lib;

import java.awt.Color;

	/* ======================================================================== */
	/**A class to deal with color themes of windows.*/
	public class ColorTheme {

		
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
			return new Color(74, 68, 59);//chocolate theme
			//return new Color(168,168,179); //slate theme
		}
		public static Color getExtInterfaceElement(){ // slightly contrasting color for unselected main tabs and some parts of project panel
			return new Color(88, 82, 74);//chocolate theme
		//return new Color(188,188,200);//slate theme
		}
		public static Color getExtInterfaceElementContrast(){   //element that is contrasted, e.g. the highlighted one, in the main tabs/project area
			return new Color(102, 98, 86); //chocolate theme
			//return new Color(221,221,232);//slate theme
		}
			public static Color getExtInterfaceEdgeContrast(){
				return new Color(190,185,175);//chocolate theme
				//return new Color(80,80,90);//slate theme
		}
		public static Color getExtInterfaceTextContrast(){  //high contrast text in external interface area, e.g. highlighted tab
			return new Color(238,224,210);//chocolate theme
			//return new Color(28,28,33); //slate theme
		}
		public static Color getExtInterfaceTextMedium(){ //medium contrast text in external interface area, e.g. project resources text
			return new Color(210,190,154);//chocolate theme
			//new Color(48,48,56); //slate theme
		}
		public static Color getExtInterfaceTextMuted(){  //muted text in external interface area,e.g. unselected tabs
			return new Color(190,178,130);//chocolate theme

		}
		
		//INTERNAL INTERFACE AREA (tool palettes, graphics/text/etc tabs)
		public static Color getInterfaceBackground(){  //background of tool palettes and small tabs bar
			return new Color(216,204,172);  // chocolate theme  
			//return new Color(196,192,192);  // slate theme  
		}
		public static Color getInterfaceBackgroundPale(){
			return new Color(234, 228,212); // chocolate theme 
			//return new Color(216,212,212);  // slate theme 
		}
		public static Color getInterfaceElement(){ //unselected tool buttons and small tab
			return new Color(234, 228,212); // chocolate theme 
			//return new Color(216,212,212);  // slate theme 
		}
		public static Color getInterfaceElementContrast(){ //selected small tab
			return new Color(236,230,222); // chocolate theme 
			//return new Color(228,228,228);  // slate theme 

		}
		public static Color getInterfaceEdgeNegative(){  //edge to unselected small tab and tool button
			return new Color(226,222,222); // chocolate theme 
	}
		public static Color getInterfaceEdgePositive(){  //edge to selected small tab
			return new Color(114,100,90); // chocolate theme 
			//return new Color(100,100,104);  // slate theme 
		}
		public static Color getInterfaceTextContrast(){  //contrasting text; must be opposite to element contrast; e.g. for selected tab
			return new Color(44,40,40);  //chocolate theme
			//return new Color(40,40,44);  // slate theme 
		}
		public static Color getInterfaceTextMuted(){  //muted text; e.g. for unselected tabs
			return new Color(64,60,60); // chocolate theme 
			//return new Color(60,60,64);  // slate theme 
		}
		

		public static Color getProjectLight(int i){  //currently used for selected tool button
			return new Color(242, 224, 185);
		}
		public static Color getProjectDark(int i){ //currently used for selected tool button edge
			return new Color(160, 82, 45);   //just brown
		}

	}
