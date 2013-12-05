/* Mesquite source code.  Copyright 1997-2010 W. Maddison and D. Maddison.
Version 2.74, October 2010.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for dealing with zip files
 *
 */
public class ZipUtil {
	
    public static ZipOutputStream createZipFile(List files, OutputStream destStream) throws IOException {
        byte b[] = new byte[512];
        ZipOutputStream zout = new ZipOutputStream(destStream);
        int totalSize = 0;
        for (Iterator iter = files.iterator(); iter.hasNext();) {
			File nextFile = (File) iter.next();
	        InputStream in = new FileInputStream(nextFile);
	        ZipEntry e = new ZipEntry(nextFile.getName());
	        zout.putNextEntry(e);
	        int len=0;
	        while((len=in.read(b)) != -1) {
	        	zout.write(b,0,len);
	        	totalSize += len;
	        }
	        zout.closeEntry();
		}
        zout.finish();
        return zout;
    }	

	/**
	 * Unzips the zip file at the specified path to the specified directory
	 * optionally deletes the zip afterwards
	 * @param fullFilePath The full path to the zip file
	 * @param directoryPath The path to the directory where things should be unzipped
	 * @param deleteAfterUnzip Whether to delete the original zip after unzipping
	 */
	public static void unzipFileToDirectory(String fullFilePath, String directoryPath, boolean deleteAfterUnzip) {
		// at this point we should have the zip downloaded and on the local filesystem
		// now we want to unzip it
		try {
			ZipFile zf = new ZipFile(fullFilePath);
		    Enumeration list = zf.entries();
		    while (list.hasMoreElements()) {
		        ZipEntry ze = (ZipEntry)list.nextElement();
		        if (ze.isDirectory()) {
		            continue;
		        }
		        try {
		            dumpZipEntry(directoryPath, zf, ze);
		        } catch (IOException e) {
		            e.printStackTrace();
		            MesquiteMessage.warnUser("problem dumping zip entry: " + ze.getName());
		        }                    
		    }
		    // Clean up the zip file once the individual entries have been written out.
		    File zip = new File(fullFilePath);
		    if (deleteAfterUnzip && zip.exists()) {
		        zip.delete();
		    }
		    zf.close();		    
		} catch (ZipException e1) {
		    e1.printStackTrace();
		} catch (IOException e2) {
		    e2.printStackTrace();
		}
		
	}
	
	private static void dumpZipEntry(String directory, ZipFile zf, ZipEntry ze) throws IOException {
		InputStream istr = zf.getInputStream(ze);
		String filename = ze.getName();
		filename = cleanStringForFilename(filename);
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		String additionalPiece = directory.endsWith(MesquiteFile.fileSeparator) ? "" : MesquiteFile.fileSeparator; 
		directory =  directory + additionalPiece;
		try {
			bis = new BufferedInputStream(istr);        	
		    fos = new FileOutputStream(directory + filename);
		    int sz = (int)ze.getSize();
		    final int N = 1024;
		    byte buf[] = new byte[N];
		    int ln = 0;
		    while (sz > 0 &&  // workaround for bug
		      (ln = bis.read(buf, 0, Math.min(N, sz))) != -1) {
		        fos.write(buf, 0, ln);
		        sz -= ln;
		     }
		} catch (Exception e) {
			
		} finally {
			if (bis != null) {
				bis.close();
			}
			if (fos != null) {
				fos.flush();
				fos.close();
			}
			if (istr != null) {
				istr.close();
			}
		}
	}
	
	public static String cleanStringForFilename(String originalString) {
	    String string = removeSpaces(originalString);
	    string = removeParens(string);
	    return string;
	}
	
	public static String removeSpaces(String originalString) {
	    return removeChar(originalString, ' ');
	}
	
	public static String removeParens(String originalString) {
	    String string = removeChar(originalString, '(');
	    string = removeChar(string, ')');
	    return string;
	}
	private static String removeChar(String originalString, char badChar) {
	    String returnString = "";
	    char[] array = originalString.toCharArray();
	    for (int i = 0; i < array.length; i++) {
            char currentChar = array[i];
            if (currentChar != ' ') {
                returnString += currentChar;
            }
        }
	    return returnString;	    
	}	
}
