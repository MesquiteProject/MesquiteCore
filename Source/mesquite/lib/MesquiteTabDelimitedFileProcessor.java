package mesquite.lib;


	
	
	import java.awt.Button;
import java.awt.Choice;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import mesquite.lib.ui.ExtensibleDialog;
import mesquite.lib.ui.SingleLineTextField;

	/* ======================================================================== */
	public class MesquiteTabDelimitedFileProcessor implements ActionListener, TextListener {
		String sampleCodeListPath = null;
		String[][] sampleCodeList; // organized as [row][column]
		int chosenNameCategory = 0;
		String[] nameCategories = new String[]{"<choose column>"};


		/*.................................................................................................................*/
		public boolean processNameCategories() {
			int count = sampleCodeList[0].length;
			for (int i = 1; i<sampleCodeList[0].length; i++){
				if (StringUtil.notEmpty(sampleCodeList[0][i]))
					count++;
			}
			nameCategories = new String[count+1];
			count = 0;
			nameCategories[count++]="<choose column>";
			for (int i = 1; i<sampleCodeList[0].length; i++){
				String s = sampleCodeList[0][i];
				if (StringUtil.notEmpty(s)) {
					nameCategories[count]=s;
					count++;
				}
			}
			return true;

		}
		/*.................................................................................................................*/
		public boolean scanTabbedDocument() {   
			if (!StringUtil.blank(sampleCodeListPath)) {
				sampleCodeList = MesquiteFile.getTabDelimitedTextFile(sampleCodeListPath);

				if (sampleCodeList != null && sampleCodeList.length>0 && sampleCodeList[0].length>1) {
					return processNameCategories();
				}
			}	
			return false;

		}
		public int getChosenNameCategory() {
			return chosenNameCategory;
		}
		public void setChosenNameCategory(int chosenNameCategory) {
			this.chosenNameCategory = chosenNameCategory;
		}

		public String[][] getSampleCodeList() {
			if (sampleCodeList!=null)
				return sampleCodeList;
			if (scanTabbedDocument())
				return sampleCodeList;
			return sampleCodeList;
		}
		public void setSampleCodeList(String[][] sampleCodeList) {
			this.sampleCodeList = sampleCodeList;
		}

		public String getSampleCodeListPath() {
			return sampleCodeListPath;
		}
		public void setSampleCodeListPath(String sampleCodeListPath) {
			this.sampleCodeListPath = sampleCodeListPath;
		}


		/*.................................................................................................................*/
		public boolean optionsSpecified(){
			return StringUtil.notEmpty(sampleCodeListPath);
		}


		/*.................................................................................................................*/
		public void processSingleXMLPreference (String tag, String content) {
			if ("sampleCodeListPath".equalsIgnoreCase(tag)){
				sampleCodeListPath = StringUtil.cleanXMLEscapeCharacters(content);
			}
			if ("chosenNameCategory".equalsIgnoreCase(tag)){
				chosenNameCategory = MesquiteInteger.fromString(content);
			}
		}
		/*.................................................................................................................*/
		public void preparePreferencesForXML (StringBuffer buffer) {
			StringUtil.appendXMLTag(buffer, 2, "sampleCodeListPath", sampleCodeListPath);  
			StringUtil.appendXMLTag(buffer, 2, "chosenNameCategory", chosenNameCategory);  
		}
		Choice categoryChoice ;
		
		SingleLineTextField sampleCodeFilePathField;

		/*.................................................................................................................*/
		public void addTabbedFileChooser(ExtensibleDialog dialog, String fileExplanationText, String columnExplanationText) {

			sampleCodeFilePathField = dialog.addTextField(fileExplanationText+":", sampleCodeListPath,26);
			sampleCodeFilePathField.addTextListener(this);
			final Button dnaCodesBrowseButton = dialog.addAListenedButton("Browse...",null, this);
			dnaCodesBrowseButton.setActionCommand("TaxonNameFileBrowse");

			String[] categories=null;

			if (nameCategories==null) {
				categories = new String[1];
				categories[0]="Sample Code                  ";
			} else
				categories = nameCategories;

			int currentCategory = chosenNameCategory;
			if (currentCategory<0)
				currentCategory=0;
			categoryChoice = dialog.addPopUpMenu(columnExplanationText+":", categories, currentCategory);


		}
		
		/*.................................................................................................................*/
		public void processTabbedFileChoiceExtensibleDialog () {
			sampleCodeListPath = sampleCodeFilePathField.getText();
			chosenNameCategory = categoryChoice.getSelectedIndex();
	}

		/*.................................................................................................................*/
		public  void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equalsIgnoreCase("TaxonNameFileBrowse")) {
				MesquiteString dnaNumberListDir = new MesquiteString();
				MesquiteString dnaNumberListFile = new MesquiteString();
				String s = MesquiteFile.openFileDialog("Choose file containing Taxon ID codes and names", dnaNumberListDir, dnaNumberListFile);
				if (!StringUtil.blank(s)) {
					sampleCodeListPath = s;
					if (sampleCodeFilePathField!=null) 
						sampleCodeFilePathField.setText(sampleCodeListPath);
				}
			}
		}
		/*.................................................................................................................*/
		public void textValueChanged(TextEvent e) {
			if (e.getSource().equals(sampleCodeFilePathField)) {
				sampleCodeListPath = sampleCodeFilePathField.getText();
				if (StringUtil.notEmpty(sampleCodeListPath)) {
					chosenNameCategory=-1;
					scanTabbedDocument();
					//	processNameCategories();
					//initialize(sampleCodeListPath);
				}	
				categoryChoice.removeAll();
				if (nameCategories!=null) {			
					for (int i=0; i<nameCategories.length; i++) {
						if (!StringUtil.blank(nameCategories[i])) {
							categoryChoice.add(nameCategories[i]);
						}
					}
				}
				categoryChoice.repaint();

			}
		}
		/*.................................................................................................................*/
		public  String[] getCondensedSecondaryColumn() {
			String[] column = new String[0];
			for (int row = 1; row<sampleCodeList.length; row++){   // start with row 1 to skip the title
				if (chosenNameCategory< sampleCodeList[row].length){
					String s = sampleCodeList[row][chosenNameCategory];
					if (!StringArray.exists(column,s)) {
						column = StringArray.addToEnd(column, s);
					}
				}
			}
			return column;
		}

		/*.................................................................................................................*/
		public  String getNameFromTabDelimitedFile(MesquiteString sampleCode, String taxonName) {
			String sampleCodeString  = sampleCode.getValue();
			if (sampleCodeString==null){
				MesquiteMessage.warnUser("Taxon \"" + taxonName + "\" has no target string to match.");
				return null;
			}
			if (sampleCodeString.contains("/"))  // if there is more than one item, then take only the first one.
				sampleCodeString = StringUtil.getFirstItem(sampleCodeString, "/");
			for (int row = 0; row<sampleCodeList.length; row++){
				if (chosenNameCategory< sampleCodeList[row].length){
					String code = sampleCodeList[row][0];
					if (sampleCodeString.equalsIgnoreCase(code)) {
						return sampleCodeList[row][chosenNameCategory];
					}
				}
			}
			return null;
		}


	}


