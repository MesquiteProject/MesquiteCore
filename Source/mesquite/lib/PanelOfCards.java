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

import java.awt.*;



/*===============================================*/
/** a field for longs */
public abstract class PanelOfCards extends Panel  {
	ExtensibleDialog dialog;
	Panel addPanel = null;
	Panel oldAddPanel = null;
	Panel choicePanel;
	Panel cardPanel = null;
	CardPanel card = null;
	CardLayout cardLayout = new CardLayout();
	int currentCard = 0;
	int numCards = 0;
	protected String name;
	GridBagLayout cardPanelLayout;
	GridBagConstraints cardPanelConstraints;

	PanelOfCards (ExtensibleDialog dialog, String name){
		super();
		this.dialog = dialog;
		this.name = name;
//		oldAddPanel = dialog.getAddPanel();

		cardPanelLayout = new GridBagLayout();
		cardPanelConstraints = new GridBagConstraints();
		cardPanelConstraints.gridx=0;
		cardPanelConstraints.gridy = GridBagConstraints.RELATIVE;
		cardPanelConstraints.gridwidth=1;
		cardPanelConstraints.gridheight=1;
		cardPanelConstraints.ipadx=4;
		cardPanelConstraints.ipadx=1;
		cardPanelConstraints.weightx=1;
		cardPanelConstraints.weighty=1;
		cardPanelConstraints.fill=GridBagConstraints.BOTH;
		cardPanelConstraints.anchor=GridBagConstraints.CENTER;
		cardPanelLayout.setConstraints(this,cardPanelConstraints);
		this.setLayout(cardPanelLayout);

		choicePanel = new Panel();
		add(choicePanel,cardPanelConstraints);

		cardPanel = new Panel();
		add(cardPanel,cardPanelConstraints);

		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);

		installChoicePanel(choicePanel);
		dialog.nullifyAddPanel();
	}
	/*.................................................................................................................*/
	public LayoutManager getCardLayout () {
		return cardLayout;
	}
	/*.................................................................................................................*/
	public LayoutManager getCardPanelLayout () {
		return cardPanelLayout;
	}
	/*.................................................................................................................*/
	public Panel getCardPanel () {
		return cardPanel;
	}
	/*.................................................................................................................*/
	public void showCard (int item) {
		cardLayout.first(cardPanel);
		if (item>0)
			for (int i=0; i<item; i++)
				cardLayout.next(cardPanel);
		//cardLayout.show(cardPanel, Integer.toString(item));
		MesquiteWindow.invalidateAll(cardPanel);
		cardPanel.validate();
		cardPanel.doLayout();
		//cardLayout.layoutContainer(cardPanel);
		cardLayout.invalidateLayout(cardPanel);
	}
	/*.................................................................................................................*/
	public abstract void installChoicePanel (Panel choicePanel) ;
	/*.................................................................................................................*/
	public abstract void addChoice (String s) ;
	/*.................................................................................................................*
	public abstract void prepareChoicePanel (Panel choicePanel) ;
	/*.................................................................................................................*
	public abstract void cleanEmptyChoice (Panel choicePanel) ;
	/*.................................................................................................................*
	public abstract void createChoicePanel (Panel choicePanel) ;
	/*.................................................................................................................*/
	public Panel addNewCard (String title) {
		String cardTitle =Integer.toString(numCards);
		PoCPanel newPanel = new PoCPanel();  //the new panel
		cardPanel.add(newPanel, cardTitle);
		cardLayout.addLayoutComponent(newPanel, cardTitle);
		addChoice(title);

	//	if (numCards==0)
	//		showCard(0);
		numCards++;
		return newPanel;
	}


	/*.................................................................................................................*
	public void finalizeCards () {
		if (getComponentCount()<=1) { //there is only one card; don't need tabs
			cleanEmptyChoice(choicePanel);
			remove(choicePanel);
		}
		else
			createChoicePanel(choicePanel);
		dialog.setAddPanel(oldAddPanel);
	}
	/*.................................................................................................................*
	public void newCard (String title, String cardTitle) {
		prepareNewCard();
    		card =new CardPanel();
    		finalizeNewCard(title, cardTitle);
	}
	/*.................................................................................................................*
	public void prepareNewCard () {
		if (card!=null && (card.getComponentCount()==0))  //then it is an empty card, get rid of it
			remove(card);
		dialog.setAddPanel(this);
	}
	/*.................................................................................................................*
	public void finalizeNewCard (String title, String cardTitle) {
    		add(cardTitle, card);
    		card.setName(cardTitle);
     		card.setLayout(dialog.getGridBagLayout());
		dialog.setAddPanel(card);
		//dialog.addLabel(title);
	}
	 */
}


class PoCPanel extends Panel {
	public Dimension getPreferredSize(){
		return new Dimension(500, 500);
	}
}


