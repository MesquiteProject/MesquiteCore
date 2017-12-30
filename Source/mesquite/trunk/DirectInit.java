/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.trunk;

import mesquite.lib.*;

import java.io.*;

/* called by Mesquite trunk early (after directories found, before modules loaded) for any init activities other than by modules */
public class DirectInit {
	
	public DirectInit(MesquiteTrunk mesquite){
		/* This will be used to load jar files at runtime*/
		loadJars(mesquite.getRootPath(), mesquite.jarFilesLoaded);
		loadJarsInDirectories(mesquite.getRootPath() + MesquiteFile.fileSeparator + "mesquite", mesquite.jarFilesLoaded);
	}
	public static void loadJars(String directoryPath, StringBuffer buffer){
		try {
			String jarsPath = directoryPath;
			if (!jarsPath.endsWith(MesquiteFile.fileSeparator) && !jarsPath.endsWith("/"))
				jarsPath += MesquiteFile.fileSeparator;
			jarsPath += "jars";
			File f = new File(jarsPath);
			if (f.exists() && f.isDirectory()){
				String[] jars = f.list();
				if (jars.length>0)
					buffer.append("Incorporated from " + directoryPath +" ");
				for (int i = 0; i< jars.length; i++) {
					if (jars[i] != null && !jars[i].startsWith(".")){
						String path = jarsPath + "/" + jars[i];
						buffer.append(" " + jars[i]);
					JarLoader.addJarFileToClassPath(path);
					System.out.println("Jar file added to classpath: " + path);
					}
				}
				buffer.append("\n\n");
			}
		}
		catch (Throwable t){
			System.out.println("DirectInit error " + t);
		}
	}
	public static void loadJarsInDirectories(String path, StringBuffer buffer){ //path has no slash at the end of it
		File f = new File(path);  //  
		if (!f.exists())
			return;
		else if (f.isDirectory()){  // is a directory; hence look inside at each item
			String[] fileList = f.list();
			for (int i=0; i<fileList.length; i++)
				if (fileList[i]!=null) {
					if (fileList[i].equalsIgnoreCase("jars"))
						loadJars(path, buffer);
					else 
						loadJarsInDirectories(path + MesquiteFile.fileSeparator + fileList[i], buffer);
				}
		}
}


}

