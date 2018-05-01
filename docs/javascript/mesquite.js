var params = Spry.Utils.getLocationParamsAsObject();

var qsParm = new Array();

qsParm['OverviewOfMesquitePanel'] = null;
qsParm['GettingStartedPanel'] = null;
qsParm['BasicMesquiteOperationPanel'] = null;
qsParm['TaxaTreesCharactersPanel'] = null;
qsParm['AnalysesPanel'] = null;
qsParm['WorkflowsPanel'] = null;
qsParm['GettingHelpPanel'] = null;
qsParm['TechnicalDetailsPanel'] = null;
qsParm['HistoryPanel'] = null;
qsParm['PublishingResultsPanel'] = null;

qs();

function qs() {
	var query = window.location.search.substring(1);
	var parms = query.split('&');
	for (var i=0; i<parms.length; i++) {
		var pos = parms[i].indexOf('=');
		if (pos > 0) {
			var key = parms[i].substring(0,pos);
			var val = parms[i].substring(pos+1);
			qsParm[key] = val;
		}
	}
}

parameterString = function()
{
	var paramString = '';
	if (GettingStartedPanel.isOpen()) {
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'GettingStartedPanel=open';
	}
	if (TaxaTreesCharactersPanel.isOpen()) {
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'TaxaTreesCharactersPanel=open';
	}
	if (OverviewOfMesquitePanel.isOpen()) {
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'OverviewOfMesquitePanel=open';
	}
	if (BasicMesquiteOperationPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'BasicMesquiteOperationPanel=open';
	}
	if (AnalysesPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'AnalysesPanel=open';
	}
	if (WorkflowsPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'WorkflowsPanel=open';
	}
	if (GettingHelpPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'GettingHelpPanel=open';
	}
	if (TechnicalDetailsPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'TechnicalDetailsPanel=open';
	}
	if (HistoryPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'HistoryPanel=open';
	}
	if (PublishingResultsPanel.isOpen()){
		if (paramString=='') paramString+='?'; else paramString+='&';
		paramString+= 'PublishingResultsPanel=open';
	}

	return paramString;
};


pageLink = function(page)
{
	document.location.href = page + parameterString();
};

