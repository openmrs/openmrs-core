var savedText = "";
var autoJump = true;

function showSearch() {
	encounterListing.style.display = "none";
	searchBox.focus();
}

function findObjects(text) {
	savedText = text;
	DWREncounterService.findEncounters(fillTable, text, $('includeVoided').checked);
	encounterListing.style.display = "";
	return false;
}

var getPatient = function(enc) {
	if (typeof enc == 'string') {
		var td = document.createElement("td");
		td.colSpan = 6;
		td.innerHTML = enc;
		return td;
	}
	return enc.patientName;
}

var getType = function(enc) {
	if (typeof enc == 'string') return noCell();
	return enc.encounterType;
}

var getForm = function(enc) {
	if (typeof enc == 'string') return noCell();
	return enc.formName;
}

var getProvider = function(enc) {
	if (typeof enc == 'string') return noCell();
	return enc.providerName;
}

var getLocation = function(enc) {
	if (typeof enc == 'string') return noCell();
	return enc.location;
}

var getDateTime = function(enc) {
	if (typeof p == 'string') return noCell();
	return getDateString(enc.encounterDateTime);
}

var customCellFunctions = [getNumber, getPatient, getType, getForm, getProvider, getLocation, getDateTime];

function search(event, delay) {
	searchBoxChange("searchTableBody", searchBox, event, includeVoided.checked, delay);
	return false;
}

function allowAutoListWithNumber() {
	return true;
}

function allowAutoJump() {
	if (autoJump == false) {
		autoJump = true;
		return false;
	}
	return true;	
}
