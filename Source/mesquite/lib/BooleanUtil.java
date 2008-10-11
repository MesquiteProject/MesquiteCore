package mesquite.lib;

public class BooleanUtil {

	/*-----------------------------------------*/
	public static boolean anyValuesTrue(boolean[] booleanArray) {
		for (int i=0; i<booleanArray.length; i++) {
			if (booleanArray[i])
				return true;
		}
		return false;
	}

	/*-----------------------------------------*/
	public static boolean[]  copyBooleanArray(boolean[] booleanArray) {
		boolean[] newArray = new boolean[booleanArray.length];
		for (int i=0;i<newArray.length; i++)
			newArray[i]=booleanArray[i];
		return newArray;
	}

	
	/*------------------------------------------*/
 	/**Reverses the elements of a boolean array*/
 	public static boolean[] reverseBooleanArray (boolean[] d) {
		if (d==null)
			return null;
		boolean[] values = new boolean[d.length];
		for (int i=0; i<values.length; i++)
			values[values.length-1-i] =  d[i];
		return values;
 	}

}
