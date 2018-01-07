package start;

import java.io.File;
import java.lang.reflect.*;
import java.lang.Class;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;
import java.lang.ClassLoader;

/*This class is used to start Mesquite. 
 * 
 * History: In older Javas, application bundles/executables could set their initial classpath to outside the bundle, but in Java 1.7 on MacOS
 * that became prohibited. This starter was therefore built to live inside application bundle, and it would manually find mesquite.Mesquite outside the
 * application bundle, and start it using the system class loader. This could work because the system classloader was also a URLClassLoader, and therefore
 * could have Mesquite's classpath added at runtime by reflection-hacking into addURL. With Java 1.9 that changed, and
 * the system ClassLoader could no longer add classpaths after startup. Thus, this starter had to make its own URLClassLoader, and also find
 * and add all the various possible classpaths.
 * 
 * Because (as of Java 9) Mesquite needs to use a custom classloader for its modules, this starter is now the required way to start Mesquite.
 * 
 * The scenario is this:
 * start.Mesquite is instantiated and calls startMesquite
 * startMesquite finds the basic Mesquite classpath (i.e. Mesquite_Folder) in order to be able to ask mesquite.Mesquite to supply a class loader with all classpaths added
 * makeModuleClassLoader in mesquite.Mesquite adds classpaths for the basic Mesquite_Folder classpath, the directories indicated by classpaths.txt, and supplementary locations (support files/classes, additionalModules)
 * Also added are any jars found in jars/ directories.  See mesquite.Mesquite.makeModuleClassLoader for details (e.g., when ByteBuddy is used).
 * This is all complex because different running conditions generate different configurations:
 * —Under Javas before 9.0, the system class loader is a URLClassLoader and so the extra classpaths can be added at runtime. We end up with a single class loader, the system on.
 * —Under Java 9, the modules are always loaded by the URLClassLoader created by makeModuleClassLoader. 
 * 
 * startMesquite then uses the classloader made to instantiate mesquite_Mesquite and call its main method. Mesquite then goes on to use the classloader to use modules.
 * 
 * Starting regimes: 
 * Eclipse: set start.Mesquite as main class
 * 
 *	For all other execution, this class and ByteBuddy classes need to be bundled into a jar file, and that is used to start Mesquite. See Executables folder for instructions.
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
		
		System.out.println("Location of executable start class: " + loc);
		if (loc.startsWith("file:")){
				loc = loc.substring(5, loc.length());
		}
		loc = decodeFromURL(loc); 
		
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
			System.out.println("$$$ URLClassLoader.class in start.Mesquite " + URLClassLoader.class); //Debugg.println
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
	/*.................................................................................................................*/
	public static String decodeFromURL(String s){
		if (s==null) return null;
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


}




