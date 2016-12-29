package mesquite.lib;

import java.awt.GraphicsEnvironment;

public class FontUtil {

	public static String setFontOther = "setFontOther";
	public static String otherFontArgument = "chooseOtherFont";
	

	/*.................................................................................................................*/
	public static MesquiteSubmenuSpec getFontSubmenuSpec(MesquiteModule mb, Commandable ownerObject){
		return getFontSubmenuSpec(null, "Font", mb, ownerObject);
	}

	/*.................................................................................................................*/
	public static MesquiteSubmenuSpec getFontSubmenuSpec(MesquiteMenuSpec whichMenu, String submenuName, MesquiteModule mb, Commandable ownerObject){
		MesquiteSubmenuSpec msf = mb.addSubmenu(whichMenu, submenuName);
		String[] fontList = getSmallFontList();
		for (int i=0; i<fontList.length; i++) 
			mb.addItemToSubmenu(whichMenu, msf, fontList[i], mb.makeCommand("setFont",  ownerObject));
		mb.addLineToSubmenu(whichMenu, msf);
		mb.addItemToSubmenu(whichMenu, msf, "Other...", mb.makeCommand(setFontOther,  ownerObject));
		mb.addLineToSubmenu(whichMenu, msf);
		msf.setDocumentItems(false);
		return msf;
	}

	/*.................................................................................................................*/
	public static String getFontNameFromDialog(MesquiteWindow mw){
		MesquiteInteger io = new MesquiteInteger(0);
		String[] fonts = getFullFontList();
		ListDialog id = new ListDialog(mw, "Choose Font", "Choose Font", false, null, fonts, io, null,false, false);

		id.completeDialog("OK","Cancel", true,id);
		id.setVisible(true);
		id.dispose();
		int selected = io.getValue();
		if (selected>=0 && selected<fonts.length) {
			return fonts[selected];
		}

		return null;
	}

	/*.................................................................................................................*/
	public static String getFontNameFromDialogIfNeeded(String fontName, MesquiteModule mb){
		if (fontName.equalsIgnoreCase(FontUtil.otherFontArgument)) {
			fontName = FontUtil.getFontNameFromDialog(mb.containerOfModule());
			if (fontName!=null) 
				mb.logln("Font chosen: " + fontName);
		}
		return fontName;
	}

	/** This method returns the list of all available fonts.*/
	public static String[] getFullFontList(){
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
		String[] fonts = ge.getAvailableFontFamilyNames(); 
		/* this is the old code that returned only a small set of font family groups (e.g., "Serif")
		 * Toolkit tk = Toolkit.getDefaultToolkit();
		String[] fonts = tk.getFontList(); */
		return fonts;
	}
	
	/** This method returns the small default font list.  It only contains guaranteed font names.*/
	public static String[] getSmallFontList(){
		int numSmallFonts = 6;
		String[] fonts = new String[numSmallFonts];
		fonts[0] = "Arial";
		fonts[1] = "Helvetica";
		fonts[2] = "Geneva";
		fonts[3] = "Times New Roman";
		fonts[4] = "SansSerif";
		fonts[5] = "Serif";

		return fonts;
	}


}
