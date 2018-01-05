package mesquite.trunk;

import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;
import net.bytebuddy.agent.ByteBuddyAgent;

import mesquite.lib.MesquiteTrunk;

import java.io.File;

public class JarLoader {
	private static final Class[] parameters = new Class[]{URL.class};
	static boolean noClassDefWarningGiven = false;

	public static boolean addJarFileToClassPath(String s)  {
		boolean jarAdded = false;
		try {
			File f = new File(s);
			URI uri = f.toURI();
			if (MesquiteTrunk.isJavaGreaterThanOrEqualTo(9.0)) {

				Instrumentation instrumentation = null;
				try {
					Class byteBuddyClass = MesquiteTrunk.basicClassLoader.loadClass("net.bytebuddy.agent.ByteBuddyAgent");
					Method installMethod = byteBuddyClass.getDeclaredMethod("install", null);
					instrumentation = (Instrumentation)installMethod.invoke(null,null);
					jarAdded = true;
				} catch (Exception e) {
					e.printStackTrace();
				}

				//	Instrumentation instrumentation = ByteBuddyAgent.install();
				instrumentation.appendToSystemClassLoaderSearch(new JarFile(f));
			} else {
				addURL(uri.toURL());
				jarAdded = true;
			}
		} catch (IOException e) {
			System.out.println("\nIOException\n");
		}
		catch (NoClassDefFoundError e) {
			if (!noClassDefWarningGiven)
				System.out.println("\nNoClassDefFoundError: ByteBuddy not found\n");
			noClassDefWarningGiven= true;
		}
		return jarAdded;
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
