package mesquite.cipres;

import java.io.File;

import mesquite.lib.CommandRecord;
import mesquite.lib.MesquiteFile;
import mesquite.lib.StringUtil;
import mesquite.trunk.ClassPathHacker;

/**
 * @author terri
 * TODO: 
 * 	1. Use a dialog that can better explain which directory to choose (i.e. the one
 * 	that contains cipres.jar )
 * 	2. Have the dialog verify that the chosen directory contains a subdir with classpath.jar. 
 * 	3. Add isCipresLoaded() method.
 *	4. Test on windows. 	
 *
 */
public class CipresLoader
{
	private static boolean cipresIsLoaded = false;
	private static String cipresRoot = "";

	/**
	 * @param storedCipresRoot path to top-level directory of cipres installation. If null, "", or
	 * directory doesn't contain the required jar file, the user will be prompted to choose
	 * the directory.
	 * @return true if the cipres classpath.jar is already loaded or is loaded by this call. 
	 * false if the user cancels out of the dialog.
	 * @throws Exception if the cipres classpath jar file doesn't exist where specified or can't
	 * be loaded.
	 */
	public static boolean loadCipres(String storedCipresRoot) throws Exception
	{
		if (cipresIsLoaded)
		{
			return true;
		}
		String jarLocation = null;
		if (StringUtil.blank(storedCipresRoot) || 
				! (new File(jarLocation = cipresJarPath(storedCipresRoot))).exists() )
		{
			String newCipresRoot = MesquiteFile.chooseDirectory("Choose directory containing CIPRes");
			if (StringUtil.blank(newCipresRoot))
			{
				return false;
			}
			jarLocation = cipresJarPath(newCipresRoot);
			File cipresJar = new File(jarLocation);
			if (!cipresJar.exists())
			{
				throw new Exception (jarLocation + " doesn't exist"); 
			}
			cipresRoot = newCipresRoot;
		}else
		{
			cipresRoot = storedCipresRoot;
		}
		try
		{
			ClassPathHacker.addFile(jarLocation);
			return (cipresIsLoaded = true);
		}
		catch (Throwable t)
		{
			throw new Exception ("an error occurred while loading " + jarLocation + ": " + t);
		}
	}

	/**
	 * @return path to directory where cipres is installed.  Maybe blank (ie null or "")
	 * if user has not yet entered a valid path.
	 */
	public static String getCipresRoot()
	{
		return cipresRoot;
	}

	private static String cipresJarPath(String cipresRoot)
	{
		return 	cipresRoot + MesquiteFile.fileSeparator + 
								"lib" + MesquiteFile.fileSeparator + 
								"cipres" + MesquiteFile.fileSeparator +
								"classpath.jar";
	}
}
