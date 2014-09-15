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

public class MesquitePackageRecord implements Listable, Explainable {
	String[] pStored;
	boolean loaded = false;
	public MesquitePackageRecord(){
		pStored = new String[3];
	}
	public void setStored(int which, String s){
		if (which>=0 && which < pStored.length){
			pStored[which] = s;
		}
	}
	public String getName(){
		String s = null;
		if (pStored[1] == null)
			s= pStored[0];
		else
			s= pStored[1];
		return s;
	}
	public boolean getLoaded(){
		return loaded;
	}
	public void setLoaded(boolean l){
		loaded = l;
	}
	public String getExplanation(){
		if (StringUtil.blank(pStored[2]))
			return "Sorry, no explanation is available for the package \"" + getName() + "\"";
		return pStored[2];
	}
	public String getPackageName(){
		if (pStored[0]==null)
			return null;
		return "mesquite." + pStored[0];
	}
	public static MesquitePackageRecord findPackage(String s){
		if (s==null)
			return null;
		ListableVector packages = MesquiteTrunk.mesquiteTrunk.packages;
		for (int i=0; i<packages.size(); i++){
			Object obj = packages.elementAt(i);
			if (obj instanceof MesquitePackageRecord) {
				MesquitePackageRecord mpr = (MesquitePackageRecord)obj;
				if (s.equals(mpr.getPackageName()))
					return mpr;
			}
		}
		return null;
	}
	public static boolean allPackagesActivated(){
		ListableVector packages = MesquiteTrunk.mesquiteTrunk.packages;
		for (int i=0; i<packages.size(); i++){
			Object obj = packages.elementAt(i);
			if (obj instanceof MesquitePackageRecord) {
				MesquitePackageRecord mpr = (MesquitePackageRecord)obj;
				if (!mpr.getLoaded())
					return false;
			}
		}
		return true;
	}
}
