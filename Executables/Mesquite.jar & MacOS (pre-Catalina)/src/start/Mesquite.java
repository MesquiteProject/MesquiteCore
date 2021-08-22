/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package start;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.lang.Class;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Vector;
import java.lang.ClassLoader;

/*This class is used to start Mesquite.  It is now the expected way to start Mesquite, whether in executables or not,
 * because (as of Java 9) Mesquite needs to use a custom classloader for its modules.
 * 
 * History: In older Javas, application bundles/executables could set their initial classpath to outside the bundle, but in Java 1.7 on MacOS
 * that became prohibited. This starter was therefore built to live inside application bundle, and it would manually find mesquite.Mesquite outside the
 * application bundle, and start it using the system class loader. This could work because the system classloader was also a URLClassLoader, and therefore
 * could have Mesquite's classpath added at runtime by reflection-hacking into addURL. With Java 1.9 that changed, and
 * the system ClassLoader was no longer a URLClassLoader and could no longer add classpaths after startup. Thus, this starter had to make its own URLClassLoader, and also find
 * and add all the various possible classpaths.
 * 
 * The scenario is this:
 * 1. start.Mesquite is instantiated and startMesquite is called
 * 2. startMesquite finds the basic Mesquite classpath (i.e. Mesquite_Folder) in order to be able to ask mesquite.Mesquite to supply a class loader with all classpaths added
 * 3. makeModuleClassLoader in mesquite.Mesquite adds classpaths for the basic Mesquite_Folder classpath, the directories indicated by classpaths.txt, and supplementary locations (support files/classes, additionalModules)
 * Also added are any jars found in jars/ directories.  See mesquite.Mesquite.makeModuleClassLoader for details (e.g., when ByteBuddy is used).
 * 4. This is all complex because different running conditions generate different configurations:
 * —Under Javas before 9.0, the system class loader is a URLClassLoader and so the extra classpaths can be added at runtime. We end up with a single class loader, the system one.
 * —Under Java 9, the modules are always loaded by the URLClassLoader created here and filled in by makeModuleClassLoader. 
 * 
 * startMesquite then uses the classloader to instantiate mesquite_Mesquite and call its main method. Mesquite then goes on to use the classloader to use modules.
 * 
 * Starting regimes: 
 * Eclipse: set start.Mesquite as main class
 * For all other execution, this class and ByteBuddy classes need to be bundled into a jar file, and that is used to start Mesquite. See Executables folder for instructions.
  */

public class Mesquite {
	URLClassLoader basicLoader;

	public static void main(String args[]) throws Exception{
		Mesquite m = new Mesquite();
		m.startMesquite(args);
	}
	
	Vector startupNotices = new Vector();
	void startMesquite(String args[]){
		ClassLoader cl = start.Mesquite.class.getClassLoader();
		String loc = cl.getResource("start/Mesquite.class").getPath();
		System.out.println("Starting Mesquite with Java: " + System.getProperty("java.version"));
		startupNotices.addElement("start.Mesquite: Location of executable start class: " + loc);
		
		if (loc.startsWith("file:")){
				loc = loc.substring(5, loc.length());
		}
	//	loc = decodeFromURL(loc); 
		try {
			loc = URLDecoder.decode(loc, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Location of executable start class: " + loc);
		
		/*Find Mesquite_Folder by moving upwards until signature folders are found
		 * Mesquite_Folder must contain "mesquite", "images", and "start"*/
		loc = findMesquite_Folder(loc);
		//If started from a jar file within an OS X application bundle, loc is initially within <bundle>/Contents/Java/<jar>/start/Mesquite.class
		
		File mesquiteDirectory = new File(loc);		
		System.out.println("Mesquite Folder: " + loc + " (exists = " + mesquiteDirectory.exists() + ")");
		startupNotices.addElement("start.Mesquite: Mesquite Folder: " + loc);

		/*First, build a basic class loader that will be used to load mesquite.Mesquite from the Mesquite_Folder
		and also passed to Mesquite to use in loading modules. This basic class loader needs to have already 
		added the URLs to the extra module directories specified in classpaths.txt. 
		It asks Mesquite to handle much of this.*/ 
		try {
			URL u =null;
			URL[] us= null;
			u =mesquiteDirectory.toURL();
			URL[] forMF = {u};
			ClassLoader current = ClassLoader.getSystemClassLoader();
			if (current instanceof URLClassLoader){
				basicLoader = (URLClassLoader)current;
				Class sysclass = URLClassLoader.class;

				try {
					Method method = sysclass.getDeclaredMethod("addURL",new Class[]{URL.class});
					method.setAccessible(true);
					method.invoke(basicLoader,new Object[]{ u });
				}
				catch (Throwable t) {
				}
			}
			if (basicLoader == null)
				basicLoader = new URLClassLoader(forMF, null);
			Class mesquiteFileClass = basicLoader.loadClass("mesquite.Mesquite");
			Class[] argTypesMCL = new Class[] {String.class, URLClassLoader.class, Vector.class};
			Method makeClassLoader = mesquiteFileClass.getDeclaredMethod("makeModuleClassLoader", argTypesMCL);
			basicLoader = (URLClassLoader)makeClassLoader.invoke(null, new Object[]{loc, basicLoader, startupNotices});
		} 
		catch (Throwable t) {
			t.printStackTrace();
		}
	
		/*Next, using the basic class loader to start Mesquite, doing it via a special main method that is passed this starter*/
		try {
			Class c = basicLoader.loadClass("mesquite.Mesquite");
			Class[] argTypes = new Class[] { String[].class, Object.class};
			Method main = c.getDeclaredMethod("mainViaStarter", argTypes);
			main.invoke(null, new Object[]{args, this});
		} 
		catch (Throwable t) {
			System.out.println("There appears to be a problem starting Mesquite.");
			t.printStackTrace();
		}
		

	}
	/*-------------------------*/
	String findMesquite_Folder(String loc){
		while (loc.length() > 0){
			File here = new File(loc);	
			if (here.exists() && here.isDirectory()){
				String[] list = here.list();
				if (itemFound(list, "mesquite") && itemFound(list, "images") && itemFound(list, "start"))	
					return loc;
			}
			loc = stripLast(loc);
		}
		return null;
	}
	/*-------------------------*/
	boolean itemFound(String[] list, String item){
		if (list == null || item == null)
			return false;
		for (int i = 0; i<list.length; i++)
			if (item.equals(list[i]))
				return true;
		return false;
	}
	/*-------------------------*/
	public ClassLoader getMesquiteClassLoader(){
			return basicLoader;
	}
	/*-------------------------*/
	public Vector getStartupNotices(){
			return startupNotices;
	}
	/*-------------------------*/
	static String stripLast(String s){
		return s.substring(0, s.lastIndexOf("/"));
	}
	/*The methods below were in place ore being developed before URLDecoder was discovered. Left just in case, as backup
	static String[][] decodeReplace = {{"%C3%a9","é"}, {"%C3%ad","í"},  {"%C3%bc","ü"}};

	public static int indexOfIgnoreCase(String a, String b) {
		if (a == null || b == null)
			return -1;
		a = a.toLowerCase();
		b = b.toLowerCase();
		return a.indexOf(b);
	}

	public static String replace(String s,String from,String to){
		if (s == null || from == null)
			return null;
		if (to!=null && from.equals(to))
			return s;
		String newString = s;
		int indexCounter = 0;
		int pos = indexOfIgnoreCase(s, from);
		while (pos>=indexCounter) {
			if (to == null) {
				newString = newString.substring(0, pos)+newString.substring(pos+from.length(), newString.length());
				indexCounter = pos;
			}
			else {
				newString = newString.substring(0, pos)+to+newString.substring(pos+from.length(), newString.length());
				indexCounter = pos +to.length();
			}
			pos = newString.indexOf(from, indexCounter);
		}
		return newString;
	}

	public static String decodeFromURL(String s){
		if (s==null) return null;
		boolean done = false;
		while (!done){
			done = true;
			for (int i = 0; i<decodeReplace.length; i++){
				if (indexOfIgnoreCase(s,  decodeReplace[i][0])>=0){
					s = replace(s, decodeReplace[i][0], decodeReplace[i][1]);
					done = false;
				}
			}
		}
		System.out.println("decoded " + s);
		StringBuffer buffer = new StringBuffer(s.length()*2);
		for (int i=0; i<s.length(); i++) {
			if (s.charAt(i) == '%' && i < s.length()+2){
			if (s.charAt(i+1) == '2' && s.charAt(i+2) == '0')
				buffer.append(" ");
			else if (s.charAt(i+1) == '3' && s.charAt(i+2) == 'E')
				buffer.append(">");
			else if (s.charAt(i+1) == '3' && s.charAt(i+2) == 'C')
				buffer.append("<");
			else if (s.charAt(i+1) == '2' && s.charAt(i+2) == '2')
				buffer.append("\"");
			else if (s.charAt(i+1) == '2' && s.charAt(i+2) == '3')
				buffer.append("#");
			else if (s.charAt(i+1) == '2' && s.charAt(i+2) == '5')
				buffer.append("%");
			else if (s.charAt(i+1) == '7' && s.charAt(i+2) == 'B')
				buffer.append("{");
			else if (s.charAt(i+1) == '7' && s.charAt(i+2) == 'D')
				buffer.append("}");
			else if (s.charAt(i+1) == '7' && s.charAt(i+2) == 'C')
				buffer.append("|");
			else if (s.charAt(i+1) == '5' && s.charAt(i+2) == 'C')
				buffer.append("\\");
			else if (s.charAt(i+1) == '5' && s.charAt(i+2) == 'E')
				buffer.append("^");
			else if (s.charAt(i+1) == '7' && s.charAt(i+2) == 'E')
				buffer.append("E~");
			else if (s.charAt(i+1) == '5' && s.charAt(i+2) == 'B')
				buffer.append("[");
			else if (s.charAt(i+1) == '5' && s.charAt(i+2) == 'D')
				buffer.append("]");
			else if (s.charAt(i+1) == '6' && s.charAt(i+2) == '0')
				buffer.append("`");
				i += 2;
				}
			else
				buffer.append(s.charAt(i));
		}
		return buffer.toString();
	}

*/
}




