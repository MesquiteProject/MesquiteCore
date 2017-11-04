/* this class courtesy of Antony Miguel, http://forum.java.sun.com/thread.jsp?forum=32&thread=300557&message=1191210 */

package mesquite.trunk;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.ProcessHandle;
import java.util.jar.JarFile;

import mesquite.lib.MesquiteTrunk;

import java.io.File;

import net.bytebuddy.agent.ByteBuddyAgent;



public class ClassPathHacker {

	private static final Class[] parameters = new Class[]{URL.class};
	
	public static void addFile(String s) throws IOException {
		File f = new File(s);
		addFile(f);
	}
	
	public static void addFile(File f) throws IOException {
		addURL(f.toURL());
	}
	
	
	public static void addURL(URL u) throws IOException {
			
		if (MesquiteTrunk.isJavaGreaterThanOrEqualTo(9.0)) {
			
		} else
		{
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

}


class ClassPathAgent {
    public static void agentmain(String args, Instrumentation instrumentation) throws IOException {
        instrumentation.appendToSystemClassLoaderSearch(new JarFile(args));
    }
}



class ClassPathUtil {
    private static File AGENT_JAR = new File("/path/to/agent.jar");

    public static void addJarToClassPath(File jarFile) {
        ByteBuddyAgent.attach(AGENT_JAR, String.valueOf(ProcessHandle.current().pid()), jarFile.getPath());
    }
}

