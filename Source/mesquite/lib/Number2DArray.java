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

import java.awt.*;

/*Last documented:  August 1999 */


/* ======================================================================== */
/**This class is used to store numbers flexibly, so that system doesn't need to know whether int, long or double.
Automatically upgrades to use long or double as needed.
This stores a whole 2Darray of similarly typed numbers.  For single numbers, use NumberObjects instead.*/
public class Number2DArray {
	public int[][] intValues;
	public long[][] longValues;
	public double[][] doubleValues;
	public static final int INT = 0;
	public static final int LONG = 1;
	public static final int DOUBLE = 2;
	public int valueClass = INT;
	private int lengthC = 0;  // the size of the array
	private int lengthT = 0;  // the size of the array
	private NameReference name;

	public Number2DArray(int lengthC, int lengthT) {
		intValues = new int[lengthC][lengthT];
		this.lengthC=lengthC;
		this.lengthT=lengthT;
		for (int i=0; i<lengthC; i++)
			for (int j=0; j<lengthT; j++)
				intValues[i][j]=0;
		this.valueClass = INT;
	}
	public Number2DArray(int lengthC, int lengthT, int valueClass) {
		this.lengthC=lengthC;
		this.lengthT=lengthT;
		if (valueClass==INT) {
			intValues = new int[lengthC][lengthT];
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					intValues[i][j]=0;
		}
		else if (valueClass == LONG) {
			longValues = new long[lengthC][lengthT];
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					longValues[i][j]=0;
		}
		else if (valueClass == DOUBLE) {
			doubleValues = new double[lengthC][lengthT];
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					doubleValues[i][j]=0;
		}
		this.valueClass = valueClass;
	}
	
	/*...........................................................*/
	/** DOCUMENT */
	public boolean legalIndices(int i, int j) {
		if (valueClass==INT)
			return intValues!=null && i>=0 && i<intValues.length && intValues.length>0 && j>=0 && j< intValues[0].length;
		else if (valueClass == LONG)
			return longValues!=null && i>=0 && i<longValues.length && longValues.length>0 && j>=0 && j< longValues[0].length;
		else //if (valueClass == DOUBLE)
			return doubleValues!=null && i>=0 && i<doubleValues.length && doubleValues.length>0 && j>=0 && j< doubleValues[0].length;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public double getDoubleValue(int i, int j) {
		if (!legalIndices(i, j))
			return MesquiteDouble.unassigned;
		if (valueClass==INT)
			return MesquiteDouble.toDouble(intValues[i][j]);
		else if (valueClass == LONG)
			return MesquiteDouble.toDouble(longValues[i][j]);
		else //if (valueClass == DOUBLE)
			return doubleValues[i][j];
	}
	/*-------------------------------------GET/SET ------------------------------------*/
	/** Places the value at array element i,j into
	the MesquiteNumber n.  Used instead of getValue to avoid creating a MesquiteNumber object. */
	public void placeValue(int i, int j, MesquiteNumber n) {
		if (!legalIndices(i, j))
			return;
		if (n!=null) {
			if (valueClass==INT)
				n.setValue(intValues[i][j]);
			else if (valueClass == LONG)
				n.setValue(longValues[i][j]);
			else if (valueClass == DOUBLE)
				n.setValue(doubleValues[i][j]);
		}
	}
	/*...........................................................*/
	/** Sets value at i,j to value passed */
	public void setValue(int i, int j, int v) {
		if (!legalIndices(i, j))
			return;
		if (valueClass==INT)
			intValues[i][j] = v;
		else if (valueClass == LONG)
			longValues[i][j] = MesquiteLong.toLong(v);
		else if (valueClass == DOUBLE)
			doubleValues[i][j]=MesquiteDouble.toDouble(v);
	}

	/** Sets value at i,j to value passed */
	public void setValue(int i, int j, long v) {
		if (!legalIndices(i, j))
			return;
		if (valueClass==INT) {
			longValues = new long[lengthC][lengthT];
			for (int ni=0; ni<lengthC; ni++)
				for (int nj=0; nj<lengthT; nj++)
					longValues[ni][nj]=MesquiteLong.toLong(intValues[ni][nj]); 
			longValues[i][j] = v;
			intValues=null;
			valueClass=LONG;
		}
		else if (valueClass == LONG)
			longValues[i][j] = v;
		else if (valueClass == DOUBLE)
			doubleValues[i][j]=MesquiteDouble.toDouble(v);
	}
	
	/** Sets value at i,j to value passed */
	public void setValue(int i, int j, double v) {
		if (!legalIndices(i, j))
			return;
		if (valueClass==INT) {
			doubleValues = new double[lengthC][lengthT];
			for (int ni=0; ni<lengthC; ni++)
				for (int nj=0; nj<lengthT; nj++)
					doubleValues[ni][nj]=MesquiteDouble.toDouble(intValues[ni][nj]);
			doubleValues[i][j] = v;
			intValues=null;
			valueClass=DOUBLE;
		}
		else if (valueClass == LONG) {
			doubleValues = new double[lengthC][lengthT];
			for (int ni=0; ni<lengthC; ni++)
				for (int nj=0; nj<lengthT; nj++)
					doubleValues[ni][nj]=MesquiteDouble.toDouble(longValues[ni][nj]);
			doubleValues[i][j] = v;
			longValues=null;
			valueClass=DOUBLE;
		}
		else if (valueClass == DOUBLE)
			doubleValues[i][j]=v;
	}

	/** Sets value at i,j to value passed */
	public void setValue(int i, int j, MesquiteNumber v) {
		if (v==null)
			return;
		if (!legalIndices(i, j))
			return;
		if (v.getValueClass()==INT)
			setValue(i,j, v.getIntValue());
		else if (v.getValueClass() == LONG)
			setValue(i,j, v.getLongValue());
		else if (v.getValueClass() == DOUBLE)
			setValue(i, j, v.getDoubleValue());
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void setToInfinite(int i, int j) {
		if (!legalIndices(i, j))
			return;
		if (valueClass==INT)
			intValues[i][j] = MesquiteInteger.infinite;
		else if (valueClass == LONG)
			longValues[i][j] = MesquiteLong.infinite;
		else if (valueClass == DOUBLE)
			doubleValues[i][j]=MesquiteDouble.infinite;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void setToZero(int i, int j) {
		if (!legalIndices(i, j))
			return;
		if (valueClass==INT)
			intValues[i][j] = 0;
		else if (valueClass == LONG)
			longValues[i][j] = 0;
		else if (valueClass == DOUBLE)
			doubleValues[i][j]=0;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public void zeroArray() {
		if (valueClass==INT) {
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					intValues[i][j]=0;
		}
		else if (valueClass == LONG) {
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					longValues[i][j]=0;
		}
		else if (valueClass == DOUBLE) {
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					doubleValues[i][j]=0;
		}
	}
	/** DOCUMENT */
	public void deassignArray() {
		if (valueClass==INT) {
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					intValues[i][j]=MesquiteInteger.unassigned;
		}
		else if (valueClass == LONG) {
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					longValues[i][j]=MesquiteLong.unassigned;
		}
		else if (valueClass == DOUBLE) {
			for (int i=0; i<lengthC; i++)
				for (int j=0; j<lengthT; j++)
					doubleValues[i][j]=MesquiteDouble.unassigned;
		}
	}
	/*-------------------------------------ARITHMETIC ------------------------------------*/
	/**Add the value in the passed MesquiteNumber to that of element i,j.  NOTE: the sum will be returned in the MesquiteNumber passed!*/
	public void addValue(int i, int j, MesquiteNumber n) {
		if (!legalIndices(i, j))
			return;
		if (n!=null) {
			if (valueClass==INT)
				n.add(intValues[i][j]);
			else if (valueClass == LONG)
				n.add(longValues[i][j]);
			else if (valueClass == DOUBLE)
				n.add(doubleValues[i][j]);
			setValue( i,j, n);
		}
	}
	/*-------------------------------------QUERY, COMPARISON ------------------------------------*/
	/** DOCUMENT */
	public boolean isZero(int i, int j) {
		if (!legalIndices(i, j))
			return false;
		if (valueClass==INT) {
			return intValues[i][j]==0;
		}
		else if (valueClass == LONG) {
			return longValues[i][j]==0;
		}
		else if (valueClass == DOUBLE) {
			return doubleValues[i][j]==0;
		}
		return false;
	}
	/** DOCUMENT */
	public boolean isUnassigned(int i, int j) {
		if (!legalIndices(i, j))
			return true;
		if (valueClass==INT) {
			return intValues[i][j]==MesquiteInteger.unassigned;
		}
		else if (valueClass == LONG) {
			return longValues[i][j]==MesquiteLong.unassigned;
		}
		else if (valueClass == DOUBLE) {
			return doubleValues[i][j]==MesquiteDouble.unassigned;
		}
		return false;
	}
	/** DOCUMENT */
	public boolean isInfinite(int i, int j) {
		if (!legalIndices(i, j))
			return false;
		if (valueClass==INT) {
			return intValues[i][j]==MesquiteInteger.infinite;
		}
		else if (valueClass == LONG) {
			return longValues[i][j]==MesquiteLong.infinite;
		}
		else if (valueClass == DOUBLE) {
			return doubleValues[i][j]==MesquiteDouble.infinite;
		}
		return false;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public boolean equals(int i, int j, MesquiteNumber n){
		if (!legalIndices(i, j))
			return false;
		if (n==null)
			return false;
		else {
			if (valueClass==INT)
				return n.equals(intValues[i][j], true);
			else if (valueClass == LONG)
				return n.equals(longValues[i][j], true);
			else if (valueClass == DOUBLE)
				return n.equals(doubleValues[i][j], true);
			return false;
		}
	}
	/*...........................................................*/
	/** this places the minimum value along second dimension at first dim = "index" into
	the MesquiteNumber n */
	public void placeMinimum2nd(int i, MesquiteNumber n) {
		if (!legalIndices(i, 0))
			return;
		if (n!=null && lengthC>0 && lengthT>0) {
			if (valueClass==INT) {
				int min = intValues[i][0];
				for (int j=0; j<lengthT; j++) {
					min = MesquiteInteger.minimum(min, intValues[i][j]);
				}
				n.setValue(min);
			}
			else if (valueClass == LONG) {
				long min = longValues[i][0];
				for (int j=0; j<lengthT; j++) {
					min = MesquiteLong.minimum(min, longValues[i][j]);
				}
				n.setValue(min);
			}
			else if (valueClass == DOUBLE) {
				double min = doubleValues[i][0];
				for (int j=0; j<lengthT; j++) {
					min = MesquiteDouble.minimum(min, doubleValues[i][j]);
				}
				n.setValue(min);
			}
		}
	}
	/*-------------------------------------SIZE ------------------------------------*/
	/** DOCUMENT */
	public int getSizeC() {
		return lengthC;
	}
	/*...........................................................*/
	/** DOCUMENT */
	public int getSizeT() {
		return lengthT;
	}
	/*...........................................................*/
	/** Changes the array size to the new dimensions*/
	public void resetSize(int newNumC, int newNumT) {
		if (newNumC == lengthC && newNumT == lengthT)
			return;
		if (valueClass==INT) {
			//System.out.println("grow nao 1");
			int[][] newIntValues = new int[newNumC][newNumT];
			for (int i=0; i<newNumC; i++)
				for (int j=0; j<newNumT; j++) {
					if (i<lengthC && j<lengthT)
						newIntValues[i][j]=intValues[i][j];
					else
						newIntValues[i][j]=0;
				}
			intValues=newIntValues;
		}
		else if (valueClass == LONG) {
			//System.out.println("grow nao 2");
			long[][] newLongValues = new long[newNumC][newNumT];
			for (int i=0; i<newNumC; i++)
				for (int j=0; j<newNumT; j++) {
					if (i<lengthC && j<lengthT)
						newLongValues[i][j]=longValues[i][j];
					else
						newLongValues[i][j]=0;
				}
			longValues=newLongValues;
		}
		else if (valueClass == DOUBLE) {
			//System.out.println("grow nao 3");
			double[][] newDoubleValues = new double[newNumC][newNumT];
			for (int i=0; i<newNumC; i++)
				for (int j=0; j<newNumT; j++) {
					if (i<lengthC && j<lengthT)
						newDoubleValues[i][j]=doubleValues[i][j];
					else
						newDoubleValues[i][j]=0;
				}
			doubleValues=newDoubleValues;
		}
		lengthC=newNumC;
		lengthT=newNumT;
	}
	/*-------------------------------------STRINGS, NAMES ------------------------------------*/
	/** Returns the i,jth element as a string */
	public String toString(int i, int j) {
		if (valueClass==INT)
			return MesquiteInteger.toString(intValues[i][j]);// + " (int)";
		else if (valueClass == LONG)
			return MesquiteLong.toString(longValues[i][j]);// + " (long)";
		else if (valueClass == DOUBLE)
			return MesquiteDouble.toString(doubleValues[i][j]);// + " (double)";
		else return "";
	}
	/*...........................................................*/
	/** Returns the entire array as a string, with tabs between elements */
	public String toString() {
		String s = "";
		if (valueClass==INT) {
			for (int i=0; i<lengthC; i++) {
				for (int j=0; j<lengthT; j++)
					s+= MesquiteInteger.toString(intValues[i][j]) + "\t";
				s += "\n";
			}
		}
		else if (valueClass == LONG) {
			for (int i=0; i<lengthC; i++) {
				for (int j=0; j<lengthT; j++)
					s+= MesquiteLong.toString(longValues[i][j]) + "\t";
				s += "\n";
			}
		}
		else if (valueClass == DOUBLE) {
			for (int i=0; i<lengthC; i++) {
				for (int j=0; j<lengthT; j++)
					s+= MesquiteDouble.toString(doubleValues[i][j]) + "\t";
				s += "\n";
			}
		}
		return s;
	}
	/*...........................................................*/
	/** Returns the upper left corner (up to numC, numT) of the matrix as a string. */
	public String partToString(int numC, int numT) {
		String s = "";
		if (numC>lengthC)
			numC = lengthC;
		if (numT>lengthT)
			numT = lengthT;
		if (valueClass==INT) {
			for (int i=0; i<numC; i++) {
				for (int j=0; j<numT; j++)
					s+= MesquiteInteger.toString(intValues[i][j]) + "\t";
				s += "\n";
			}
		}
		else if (valueClass == LONG) {
			for (int i=0; i<numC; i++) {
				for (int j=0; j<numT; j++)
					s+= MesquiteLong.toString(longValues[i][j]) + "\t";
				s += "\n";
			}
		}
		else if (valueClass == DOUBLE) {
			for (int i=0; i<numC; i++) {
				for (int j=0; j<numT; j++)
					s+= MesquiteDouble.toString(doubleValues[i][j]) + "\t";
				s += "\n";
			}
		}
		return s;
	}
	/*...........................................................*/
	/** Returns a string indicating values along a single column */
	public String toString2nd(int i) {
		if (!legalIndices(i, 0))
			return "";
		String s = "";
		if (valueClass==INT) {
				for (int j=0; j<lengthT; j++)
					s+= MesquiteInteger.toString(intValues[i][j]) + "\t";
		}
		else if (valueClass == LONG) {
				for (int j=0; j<lengthT; j++)
					s+= MesquiteLong.toString(longValues[i][j]) + "\t";
		}
		else if (valueClass == DOUBLE) {
				for (int j=0; j<lengthT; j++)
					s+= MesquiteDouble.toString(doubleValues[i][j]) + "\t";
		}
		return s;
	}
	/*...........................................................*/
	/** Sets the name reference for the array */
	public void setNameReference(NameReference nr){
		name = nr;
	}
	/*...........................................................*/
	/** Gets the name reference for the array */
	public NameReference getNameReference(){
		return name;
	}
}

