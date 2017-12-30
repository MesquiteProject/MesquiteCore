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

public class JarLoader {

	private static final Class[] parameters = new Class[]{URL.class};

	public static void addJarFileToClassPath(String s) throws IOException {
		File f = new File(s);
		URI uri = f.toURI();
		if (MesquiteTrunk.isJavaGreaterThanOrEqualTo(9.0)) {
			Instrumentation instrumentation = ByteBuddyAgent.install();
			instrumentation.appendToSystemClassLoaderSearch(new JarFile(f));
		} else {
			addURL(uri.toURL());
		}
	}
	
	 static void addURL(URL u) throws IOException {
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
