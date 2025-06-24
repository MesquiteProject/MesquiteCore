/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.lib;


import java.io.UnsupportedEncodingException;
import java.util.Vector;

/* ��������������������������� A string buffer that can exceed 2^^31 ������������������������������� */

public class MesquiteStringBuffer {
	private Vector buffers;
	private StringBuffer currentBuffer;
	long ALMOST_INT_OVERFLOW = 2000000000L;
	public MesquiteStringBuffer(StringBuffer s) { 
		this();
		currentBuffer.append(s);
	}
	public MesquiteStringBuffer(String s) { 
		this();
		currentBuffer.append(s);
	}
	public MesquiteStringBuffer() { 
		buffers = new Vector();
		currentBuffer = new StringBuffer(0);
		buffers.addElement(currentBuffer);
	}
/*	public boolean equals(String s){
		if (getNumStrings() != 1)
			return false;
		String sHere = getString(0);
		if (sHere == null)
			return s == null;
		else
			return sHere.equals(s);
	}
*/
	public MesquiteStringBuffer(long allocate) { 
		buffers = new Vector();
		if (allocate > ALMOST_INT_OVERFLOW)
			allocate = ALMOST_INT_OVERFLOW;
		currentBuffer = new StringBuffer(0);
		buffers.addElement(currentBuffer);
	}
	public int getNumStrings(){
		return buffers.size();
	}
	public String getString(int i){
		StringBuffer sb = (StringBuffer)buffers.elementAt(i);
		return sb.toString();
	}
	public void logln(){
		for (int i = 0; i<getNumStrings(); i++)
			MesquiteMessage.print(getString(i));
		MesquiteMessage.println("");
	}
	public byte[] getBytes(String encoding, int i){
		byte[] sBytes = null;
		try{
			sBytes = getString(i).getBytes(encoding);
		}
		catch (UnsupportedEncodingException e){
			MesquiteMessage.printStackTrace("Unsupported encoding");
		}
		return sBytes;
	}
	public StringBuffer getStringBuffer(int i){
		StringBuffer sb = (StringBuffer)buffers.elementAt(i);
		return sb;
	}
	public void setLength(long length){
		if (length == 0){
			currentBuffer = getStringBuffer(0);
			currentBuffer.setLength(0);
			for (int ibb = buffers.size()-1; ibb>0; ibb--)
				buffers.removeElementAt(ibb);
			
		}
		long totals = 0;
		int ib =0;
		while (length>=totals && ib<buffers.size()){
			StringBuffer sb = (StringBuffer)buffers.elementAt(ib);
			if (totals + sb.length()>=length){ //first time hit longer
				sb.setLength((int)(length-totals));
				currentBuffer = sb;
				for (int ibb = buffers.size()-1; ibb>ib; ibb--)
					buffers.removeElementAt(ibb);
				return;
			}
			totals += sb.length();
		}
	}
	public long length(){
		long totals = 0;
		for (int ib =0; ib<buffers.size(); ib++){
			StringBuffer sb = (StringBuffer)buffers.elementAt(ib);
			totals += sb.length();
		}
		return totals;
	}


	public void append(String s){
		if (0L + s.length() + currentBuffer.length() > ALMOST_INT_OVERFLOW){
			currentBuffer = new StringBuffer(s);
			buffers.addElement(currentBuffer);
		}
		else
			currentBuffer.append(s);
	}
	public void append(StringBuffer s){
		if (0L + s.length() + currentBuffer.length() > ALMOST_INT_OVERFLOW){
			currentBuffer = new StringBuffer(s);
			buffers.addElement(currentBuffer);
		}
		else
			currentBuffer.append(s);
	}
	public void append(MesquiteStringBuffer s){
		for (int iother = 0; iother<s.getNumStrings(); iother++){
			String iS = s.getStringBuffer(iother).toString();
			append(iS);
//			StringBuffer n = new StringBuffer(s.getStringBuffer(iother).toString());
//			buffers.addElement(n);
		}
	}

	public void append(char[] c, int len){
		if (c == null)
			return;
		if (0L + c.length + currentBuffer.length() > ALMOST_INT_OVERFLOW){
			currentBuffer = new StringBuffer(c.length);
			buffers.addElement(currentBuffer);
		}
		for (int i = 0; i<len && i<c.length; i++)
			currentBuffer.append(c[i]);
	}
	public void append(char[] c){
		if (c == null)
			return;
		if (0L + c.length + currentBuffer.length() > ALMOST_INT_OVERFLOW){
			currentBuffer = new StringBuffer(c.length);
			buffers.addElement(currentBuffer);
		}
		currentBuffer.append(c);
	}
	public void append(char c){
		if (4L + currentBuffer.length() > ALMOST_INT_OVERFLOW){
			currentBuffer = new StringBuffer(c);
			buffers.addElement(currentBuffer);
		}
		else
			currentBuffer.append(c);
	}

	public void setCharAt(long place, char c){
		long totals = 0;
		for (int ib =0; ib<buffers.size(); ib++){
			StringBuffer sb = (StringBuffer)buffers.elementAt(ib);
			if (place<totals+sb.length()){ // it's in this buffer
				sb.setCharAt((int)(place-totals), c);
				return;
			}
			totals += sb.length();
		}
		return;
	}
	public char charAt(long place){
		long totals = 0;
		for (int ib =0; ib<buffers.size(); ib++){
			StringBuffer sb = (StringBuffer)buffers.elementAt(ib);
			if (place<totals+sb.length()){ // it's in this buffer
				return sb.charAt((int)(place-totals));
			}
			totals += sb.length();
		}
		return 0;
	}
	public void insert(long place, char c){
		long totals = 0;
		for (int ib =0; ib<buffers.size(); ib++){
			StringBuffer sb = (StringBuffer)buffers.elementAt(ib);
			if (place<totals+sb.length()){ // it's in this buffer
				sb.insert((int)(place-totals), c);  //there should be lots of room as long as not a lot of inserting is done!
				return;
			}
			totals += sb.length();
		}
	}
	public void insert(long place, String s){
		long totals = 0;
		for (int ib =0; ib<buffers.size(); ib++){
			StringBuffer sb = (StringBuffer)buffers.elementAt(ib);
			if (place<totals+sb.length()){ // it's in this buffer
				sb.insert((int)(place-totals), s);  //there should be lots of room as long as not a lot of inserting is done!
				return;
			}
			totals += sb.length();
		}
	}
	public String toString() {
		String toReturn = getStringBuffer(0).toString();
		if (getNumStrings()>1)
			MesquiteMessage.printStackTrace("WARNING: MesquiteStringBuffer toString should not be used");
		return toReturn;
	}

}

