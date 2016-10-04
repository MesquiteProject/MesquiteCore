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

 
/*=======================*/
/** A timer for profiling.*/
public class MesquiteTimer {
	long accumulatedTime = 0;
	long currentBout = 0;
	long lastCheckedTime = 0;
	long veryStart = 0;
	int timesStarted = 0;
	public MesquiteTimer(){
		veryStart = System.currentTimeMillis();
	}
	public MesquiteTimer(boolean startNow){
		veryStart = System.currentTimeMillis();
	}
	public void start() {
		if (timesStarted==0) // set veryStart to first call to start.
			veryStart = System.currentTimeMillis();
		timesStarted++;
		currentBout = System.currentTimeMillis();
		lastCheckedTime = currentBout;
	}
	public void reset() {
		currentBout = 0;
		accumulatedTime = 0;
	}
	public void resetLastTime() {
		lastCheckedTime = System.currentTimeMillis();
	}
	public void fullReset() {
		veryStart = System.currentTimeMillis();
		reset();
		resetLastTime();
	}
	public void end() {
		lastCheckedTime = System.currentTimeMillis();
		accumulatedTime += System.currentTimeMillis() - currentBout;
	}
	public long timeSinceLast() {
		long t =System.currentTimeMillis() -lastCheckedTime;
		lastCheckedTime = System.currentTimeMillis();
		return  t;
	}
	public double timeSinceLastInSeconds() {
		long t =System.currentTimeMillis() -lastCheckedTime;
		lastCheckedTime = System.currentTimeMillis();
		return  (1.0*t)/1000.0;
	}
	public long timeSinceVeryStart() {
		long t =System.currentTimeMillis() -veryStart;
		return  t;
	}
	public double timeSinceVeryStartInSeconds() {
		long t =System.currentTimeMillis() -veryStart;
		lastCheckedTime = System.currentTimeMillis();
		return  (1.0*t)/1000.0;
	}
	public static String getDayHoursMinutesSecondsFromMilliseconds(long t) {
		double seconds =  (1.0*t)/1000.0;
		long minutes = 0;
		long hours = 0;
		long days = 0;
		if (seconds>60.0){
			minutes = (long)seconds/60;
			seconds = seconds-minutes*60.0;
		}
		if (minutes>60){
			hours = minutes/60;
			minutes = minutes-hours*60;
		}
		if (hours>24){
			days = hours/24;
			hours = hours-days*24;
		}
		String s = "";
		if (days>0){
			if (days==1)
				s+= "1 day ";
			else
				s+= "" + days + " days ";
		}
		if (hours>0) {
			if (hours==1)
				s+= "1 hour ";
			else
				s+= "" + hours + " hours ";
		}
		if (minutes>0) {
			if (minutes==1)
				s+= "1 minute ";
			else
				s+= "" + minutes + " minutes ";
		}
		if (seconds>0.0)
			s+= "" + MesquiteDouble.toStringDigitsSpecified(seconds, 1) + " seconds ";
		return s;
	}
	public static String getHoursMinutesSecondsFromMilliseconds(long t) {
		double seconds =  (1.0*t)/1000.0;
		long minutes = 0;
		long hours = 0;
		if (seconds>60.0){
			minutes = (long)seconds/60;
			seconds = seconds-minutes*60.0;
		}
		if (minutes>60){
			hours = minutes/60;
			minutes = minutes-hours*60;
		}
		String s = "";
		if (hours>0) {
			if (hours==1)
				s+= "1 hour ";
			else
				s+= "" + hours + " hours ";
		}
		if (minutes>0) {
			if (minutes==1)
				s+= "1 minute ";
			else
				s+= "" + minutes + " minutes ";
		}
		if (seconds>0.0)
			s+= "" + MesquiteDouble.toStringDigitsSpecified(seconds, 1) + " seconds ";
		return s;
	}

	public String timeSinceVeryStartInHoursMinutesSeconds() {
		long t =System.currentTimeMillis() -veryStart;
		lastCheckedTime = System.currentTimeMillis();
		return getHoursMinutesSecondsFromMilliseconds(t);
	}

	public long getAccumulatedTime() {
		return accumulatedTime;
	}
	public long getTimesStarted() {
		return timesStarted;
	}
}


