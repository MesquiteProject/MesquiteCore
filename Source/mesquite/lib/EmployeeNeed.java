/* Mesquite source code.  Copyright 2001 and onward, D. Maddison and W. Maddison. 


Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.lib;



public class EmployeeNeed  {
	public static int REQUIRED = 0;
	public static int THISOROTHERREQUIRED = 1;
	public static int OPTIONAL = 2; 
	
	int requiredStatus = REQUIRED;
	EmployeeNeed alternativeRequired = null; //when THISOROTHERREQUIRED, please indicate which is other that can substitute
	Class dutyClass = null;
	String explanation = null;
	String accessPoint = null;
	String alternativeEmployerLabel = null;
	int priority = 0;
	boolean suppressListing = false;
	boolean emphasize = false;
	int whichOne = 0;  //use to distinguish if more than one of same duty class
	MesquiteModuleInfo requestor;
	boolean isEntryPoint = false;
	
	public EmployeeNeed(Class dutyClass, String explanation, String accessPoint){  //Todo: should also store how this is accessed by the employer (e.g. entry points)
		this.dutyClass = dutyClass;
		this.explanation = explanation;
		this.accessPoint = accessPoint;
	}
	/*------------------------------*/
	public MesquiteModuleInfo getRequestor(){
		return requestor;
	}
	public void setRequestor(MesquiteModuleInfo r){
		requestor = r;
	}
	/*------------------------------*/
	public String getAlternativeEmployerLabel(){
		return alternativeEmployerLabel;
	}
	public void setAlternativeEmployerLabel(String r){
		alternativeEmployerLabel = r;
	}
	/*------------------------------*/
	public int getPriority(){
		return priority;
	}
	public void setPriority(int r){
		priority = r;
	}
	/*------------------------------*/
	public boolean getSuppressListing(){
		return suppressListing;
	}
	public void setSuppressListing(boolean r){
		suppressListing = r;
	}
	/*------------------------------*/
	public boolean getEmphasize(){
		return emphasize;
	}
	public void setEmphasize(boolean r){
		emphasize = r;
	}
		
	/*------------------------------*/
	public Class getDutyClass(){
		return dutyClass;
	}
	public void setDutyClass(Class d){
		 dutyClass = d;
	}
	/*------------------------------*/
	public String getExplanation(){
		return explanation;
	}
	public void setExplanation(String e){
		 explanation = e;
	}
	/*------------------------------*/
	public String getAccessPoint(){
		return accessPoint;
	}
	public void setAccessPoint(String e){
		accessPoint = e;
	}
	/*------------------------------*/
	public void setAlternative(EmployeeNeed e){
		alternativeRequired = e;
	}
	public EmployeeNeed getAlternative(){
		return alternativeRequired;
	}
	/*------------------------------*/
	public void setRequiredStatus(int status){
		requiredStatus = status;
	}
	public int getRequiredStatus(){
		return requiredStatus;
	}
	/*------------------------------*/
	public void setWhichOne(int w){
		whichOne = w;
	}
	public int getWhichOne(){
		return whichOne;
	}
	/*------------------------------*/
	String entryCommand = null;
	/*------------------------------*/
	public void setAsEntryPoint(String command){
		isEntryPoint = true;
		entryCommand = command;
	}
	public boolean isEntryPoint(){
		return isEntryPoint;
	}
	public String getEntryCommand(){
		return entryCommand;
	}
	public void setEntryCommand(String command){
		entryCommand = command;
	}


}


