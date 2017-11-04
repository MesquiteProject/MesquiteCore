/* this class courtesy of Antony Miguel, http://forum.java.sun.com/thread.jsp?forum=32&thread=300557&message=1191210 */

package mesquite.trunk;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

import mesquite.lib.MesquiteTrunk;

import java.io.File;

import net.bytebuddy.agent.ByteBuddyAgent;



public class ClassPathHacker {

	private static final Class[] parameters = new Class[]{URL.class};
	
	public static Class loadModule(String path, String name) throws IOException, ClassNotFoundException {
		File f = new File(path);
		URI uri = f.toURI();
		URLClassLoader loader = new URLClassLoader(new URL[] {uri.toURL()});
		return loader.loadClass(name);
	}

	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}
	
	static void addFile(File f) throws IOException {
		if (MesquiteTrunk.isJavaGreaterThanOrEqualTo(9.0)) {
			URI uri = f.toURI();
			URLClassLoader loader = new URLClassLoader(new URL[] {uri.toURL()});
			//Instrumentation instrumentation = ByteBuddyAgent.install();
			//instrumentation.appendToSystemClassLoaderSearch(new JarFile(f));

		} else {
			URI uri = f.toURI();
			addURL(uri.toURL());
		}
	}
	
	
	public static void addURL(URL u) throws IOException {
		URLClassLoader sysloader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		try {
			Method method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ u });
		} 
		catch (Throwable t) {
			t.printStackTrace();
			throw new IOException("Error, could not add URL to system classloader");
		}


	}

}



