package mesquite.lib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Utility class for dealing with zip files
 *
 */
public class ZipUtil {

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
