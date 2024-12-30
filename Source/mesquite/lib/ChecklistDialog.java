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
import java.awt.event.*;

/*===============================================*/
public class ChecklistDialog extends MesquiteDialog implements ActionListener{
	ChecklistScrollPane pane;
	Panel boxPanel;
	MesquiteInteger selected;
	IntegerArray indicesSelected = new IntegerArray(0);
	String[] strings = null;
	Listable[] listables = null;
	TextArea explanation = null;
	Button cancel,ok;
	int numItems = 0;
	ChecklistBox[] boxes;
	
	public ChecklistDialog (MesquiteWindow parent, String title, String message, Object names, boolean doShow) {
		super(parent, title);
		setLayout(new GridBagLayout());
		if (names instanceof Listable[]) {
			listables = (Listable[])names;
			numItems = listables.length;
		}
		else if (names instanceof String[]) {
			strings = (String[])names;
			numItems = strings.length;
		}
	//	this.selected = selected;
		Font f = new Font ("Dialog", Font.PLAIN, 12);
		TextArea lab = new MQTextArea (message,3, 6, TextArea.SCROLLBARS_NONE);
		addComponent(this, lab, 0, GridBagConstraints.RELATIVE, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
		lab.setEditable(false);
		
		pane = new ChecklistScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
		boxPanel = new MQPanel();
		boxPanel.setLayout(new GridLayout(numItems, 1));
		addComponent(this, pane, 0, GridBagConstraints.RELATIVE, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
		//pane.setBackground(Color.red);
		//boxPanel.setBackground(Color.blue);
		if (anyDocumentables(listables)){
			explanation = new MQTextArea ("",3, 6, TextArea.SCROLLBARS_VERTICAL_ONLY);
			explanation.setFont(new Font ("SanSerif", Font.PLAIN, 12));

			addComponent(this, explanation, 0, GridBagConstraints.RELATIVE, 1, 1, 1, 1, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
		}
		Panel buttons = new MQPanel();
		Button sho=null;
		buttons.add("West", cancel = new Button("Cancel"));
		if (doShow)
			buttons.add("Center", sho = new Button("Show")); //NOTE about a kludge: the name of this button needs to remain "show".  See buttonHit in ListDialog
		buttons.add("East", ok = new Button("OK"));
		//cancel.setBackground(Color.white);
		if (doShow)
			sho.setBackground(Color.white);
		//ok.setBackground(Color.white);
		cancel.setFont(f);
		ok.setFont(f);
		if (doShow)
			sho.setFont(f);
		lab.setFont(f);
		
		
		pane.addChecks(boxPanel);
		pane.setVisible(true);
		boxPanel.setVisible(true);
		boxes = new ChecklistBox[numItems];
		if (listables!=null){
			numItems = listables.length;
			//add checkbox
			for (int i=0; i<listables.length; i++) {
				if (listables[i]!=null){
					String name = null;
					if (listables[i] instanceof SpecialListName && ((SpecialListName)listables[i]).getListName()!=null)
						name = ((SpecialListName)listables[i]).getListName();
					else if (listables[i].getName()!=null)
						name = listables[i].getName();
					boxes[i] = new ChecklistBox(name, this);
					boxPanel.add(boxes[i]);
					boxes[i].setVisible(true);
				}
			}
		}
		else if (strings!=null){
			for (int i=0; i<strings.length; i++)
				if (strings[i]!=null) {
					boxes[i] = new ChecklistBox(strings[i], this);
					boxPanel.add(boxes[i]);
					boxes[i].setVisible(true);
				}
		}
		
		addComponent(this, buttons, 0, GridBagConstraints.RELATIVE, 1, 1, 1, 0, GridBagConstraints.BOTH, GridBagConstraints.CENTER);
		setSize(460, 440);
		setDefaultButton("OK");
		MesquiteWindow.centerWindow(this);
	}
	private boolean anyDocumentables(Listable[] L){
		if (L==null)
			return false;
		for (int i=0; i<L.length; i++)
			if (L[i] instanceof Explainable)
				return true;
		return false;
	}
	private int getIndex(ChecklistBox c){
		for (int i=0; i<numItems; i++)
			if (boxes[i] == c)
				return i;
		return -1;
	}
	private int numSelected(){
		int count = 0;
		for (int i=0; i<numItems; i++)
			if (boxes[i].getState())
				count++;
		return count;
	}
	private int[] getSelectedIndexes(){
		int[] s = new int[numSelected()];
		int count = 0;
		for (int i=0; i<numItems; i++)
			if (boxes[i].getState()) {
				s[count++] = i;
			}
		return s;
	}
	
	private void setSelected(boolean[] selected){
		if (selected == null)
			return;
		for (int i=0; i<numItems && i<selected.length; i++)
			boxes[i].setState(selected[i]);
	}
	private void setStyle(Component c, int style){
		if (c==null)
			return;
		Font fontToSet = new Font ("Dialog", style, 12);
 		if (fontToSet!= null)
 			c.setFont(fontToSet);
	}
	private void setHighlighted(boolean[] highlighted){
		if (highlighted == null)
			return;
		for (int i=0; i<numItems && i<highlighted.length; i++) {
			if (highlighted[i])
				setStyle(boxes[i], Font.BOLD);
			else
				setStyle(boxes[i], Font.PLAIN);
		}
	}
	private void showExplanation(int selected){
		if (selected <0) {
			if (explanation !=null) {
				explanation.setText("");
				return;
			}
		}
		else if (listables !=null && selected < listables.length && listables[selected] instanceof Explainable) {
			if (explanation !=null) {
				String exp = ((Explainable)listables[selected]).getExplanation();
				if (exp == null)
					exp = "";
				explanation.setText(exp);
				return;
			}
		}
		if (explanation !=null) 
			explanation.setText("");
	}
	public void boxSelected(ChecklistBox c){
		if (listables==null)
			return;
		int whichBox = getIndex(c);
		if (whichBox>=0)
			showExplanation(whichBox);
	}
	public void actionPerformed(ActionEvent e){
		if (defaultButtonString!=null) {
			buttonHit(defaultButtonString, null);
			dispose();
		}
	}
	public static Listable[] queryListMultiple(MesquiteWindow parent, String title, String message, ListableVector vector, boolean[] selected) {
		return queryListMultiple(parent, title, message, null, true, vector, selected);
	}
	public static Listable[] queryListMultiple(MesquiteWindow parent, String title, String message, String okButton, boolean hasDefault, ListableVector vector, boolean[] selected) {
		if (vector==null) 
			return null;
		Listable[] names = vector.getListables();
		if (names==null)
			return null;
		ChecklistDialog id = new ChecklistDialog(parent, title, message, names, false);
		if (okButton!=null)
			id.ok.setLabel(okButton);
		if (hasDefault)
			id.setDefaultButton(id.ok.getLabel());
		else
			id.setDefaultButton(null);
		id.setSelected(selected);
		id.setHighlighted(selected);
		id.setVisible(true);
		IntegerArray result = id.indicesSelected;
		if (result==null || result.getSize()==0) {
			id.dispose();
			return null;
		}
		Listable[] L = new Listable[result.getSize()];
		for (int i=0; i<result.getSize(); i++)
			L[i] = (Listable)vector.elementAt(result.getValue(i));
		id.dispose();
		return L;
	}
	public void buttonHit(String buttonLabel, Button button) {
		if (buttonLabel.equalsIgnoreCase(ok.getLabel())){
			indicesSelected.setValues(getSelectedIndexes());
		}
		else {
			indicesSelected.setValues(null);
		}
	}
}
/* ======================================================================== */
class ChecklistScrollPane extends MQScrollPane{
	public ChecklistScrollPane () {
		super();
	}
	public ChecklistScrollPane (int scrollPolicy) {
		super(scrollPolicy);
	}
	public void addChecks(Component c){
		addImpl(c, null, 0);
	}
}

class ChecklistBox extends Checkbox implements ItemListener {
	ChecklistDialog dlog;
	public ChecklistBox(String name, ChecklistDialog dlog){
		super(name);
		this.dlog = dlog;
		addItemListener(this);
	}
	public void itemStateChanged(ItemEvent e){
		//if (e.getStateChange() == ItemEvent.SELECTED){
				dlog.boxSelected(this);
		//}
	}
}



