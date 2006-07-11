var nameListBox= null;
var idListBox  = null;
var addButton  = null;
var myConceptSearchMod = null;
var drugConcepts = new Array();

window.onload = function() {
	myConceptSearchMod = new fx.Resize("conceptSearchForm", {duration: 100});
	myConceptSearchMod.hide();
	changeClass(document.getElementById("conceptClass"));
	changeDatatype(document.getElementById("datatype"));
	changeSetStatus(document.getElementById('conceptSet'));
};

function removeItem(nameList, idList, delim)
{
	var sel   = document.getElementById(nameList);
	var input = document.getElementById(idList);
	var optList   = sel.options;
	var lastIndex = -1;
	var i = 0;
	while (i<optList.length) {
		// loop over and erase all selected items
		if (optList[i].selected) {
			optList[i] = null;
			lastIndex = i;
		}
		else {
			i++;
		}
	}
	copyIds(nameList, idList, delim);
	while (lastIndex >= optList.length)
		lastIndex = lastIndex - 1;
	if (lastIndex >= 0) {
		optList[lastIndex].selected = true;
		return optList[lastIndex];
	}
	return null;
}

function addConcept(nameList, idList, obj)
{
	nameList = document.getElementById(nameList);
	idList   = document.getElementById(idList);
	if (idList != idListBox) {
		//if user clicked on a new button
		closeConceptBox();
		nameListBox = nameList;	// used by onSelect()
		idListBox   = idList;	// used by onSelect()
	}
	
	var conceptSearchForm = document.getElementById("conceptSearchForm");
	setPosition(obj, conceptSearchForm, 520, 290);
	
	DWRUtil.removeAllRows("conceptSearchBody");
	
	myConceptSearchMod.toggle();
	if (addButton == null) {
		var searchText = document.getElementById("searchText");
		searchText.value = '';
		searchText.select();
		addButton = obj;
		resetForm();
		//searchText.focus();  //why does this cause the inner box to shift position?!?
	}
	else {
		obj.focus();
		addButton = null;
	}
}

function closeConceptBox() {
	myConceptSearchMod.hide();
	addButton = null;
	drugConcepts = new Array();
	return false;
}

function moveUp(nameList, idList)
{
	var input = document.getElementById(idList);
	var sel = document.getElementById(nameList);
	var optList = sel.options;
	for (var i=1; i<optList.length; i++) {
		// loop over and move up all selected items
		if (optList[i].selected && !optList[i-1].selected) {
			var id   = optList[i].value;
			var name = optList[i].text;
			optList[i].value = optList[i-1].value;
			optList[i].text  = optList[i-1].text;
			optList[i].selected = false;
			optList[i-1].value = id;
			optList[i-1].text  = name;
			optList[i-1].selected = true;
		}
	}
	copyIds(nameList, idList, ' ');
}

function moveDown(nameList, idList)
{
	var input = document.getElementById(idList);
	var sel = document.getElementById(nameList);
	var optList = sel.options;
	for (var i=optList.length-2; i>=0; i--) {
		if (optList[i].selected && !optList[i+1].selected) {
			var id   = optList[i].value;
			var name = optList[i].text;
			optList[i].value = optList[i+1].value;
			optList[i].text  = optList[i+1].text;
			optList[i].selected = false;
			optList[i+1].value = id;
			optList[i+1].text  = name;
			optList[i+1].selected = true;
		}
	}
	copyIds(nameList, idList, ' ');
}

function copyIds(from, to, delimiter)
{
	var sel = document.getElementById(from);
	var input = document.getElementById(to);
	var optList = sel.options;
	var remaining = new Array();
	var i=0;
	while (i < optList.length)
	{
		remaining.push(optList[i].value);
		i++;
	}
	input.value = remaining.join(delimiter);
}

function addSynonym(locale) {
	var obj = document.getElementById("addSyn" + locale);
	var synonyms = document.getElementById("syns" + locale).options;
	var syn = obj.value.toUpperCase();
	if (syn != "") {
		var addable = true;
		for (var i=0; i<synonyms.length; i++) {
			synonyms[i].selected = false;
			if (synonyms[i].value == syn) {
				addable = false;
				synonyms[i].selected = true;
			}
		}
		if (addable) {
			var opt = new Option(syn, syn);
			opt.selected = true;
			synonyms[synonyms.length] = opt;
		}
	}
	obj.value = "";
	obj.focus();
	copyIds("syns" + locale, "newSynonyms" + locale, ",");
	window.Event.keyCode = 0;  //disable enter key submitting form
}

var onSelect = function(conceptList) {
	var options = nameListBox.options;		//nameListBox var from addConcept(nameList, idList, obj)
	for (i=0; i<conceptList.length; i++) {
		if (conceptList[i].className == "Drug")
			drugConcepts.push(conceptList[i]);
		else
			addOption(conceptList[i], options);
	}
	copyIds(nameListBox.id, idListBox.id, ' ');
	
	doSelectDrugs(drugConcepts);

	return false;
};

var doSelectDrugs = function() {
	if (drugConcepts.length > 0) {
		resetForm();
		DWRConceptService.getDrugs(fillTable, drugConcepts.pop().conceptId, true);
	}
	else {
		closeConceptBox();
	}
}

function addOption(obj, options) {
	var objId = obj.conceptId;
	var objName = obj.name + ' ('+objId+')';
	
	if (obj.drugId != null) //if obj is actually a drug object
		objId = objId + "^" + obj.drugId;
		
	if (isAddable(objId, options)==true) {
		var opt = new Option(objName, objId);
		opt.selected = true;
		options[options.length] = opt;
	}
}

function isAddable(value, options) {
	for (x=0; x<options.length; x++)
		if (options[x].value == value)
			return false;

	return true;
}

function removeHiddenRows() {
	var rows = document.getElementsByTagName("TR");
	var i = 0;
	while (i < rows.length) {
		if (rows[i].style.display == "none") {
			rows[i].parentNode.removeChild(rows[i]);
		}
		else {
			i = i + 1;
		}
	}
}

function changeClass(obj) {
	var row = document.getElementById("conceptSetRow");
	//if (isSet)
	//	row.style.display = "";
	//else
	//	row.style.display = "none";
}

function changeSetStatus(obj) {
	var row = document.getElementById("conceptSetRow");
	if (obj.checked)
		row.style.display = "";
	else
		row.style.display = "none";
}

var customDatatypes = new Array();
customDatatypes.push("numeric");
customDatatypes.push("coded");

function changeDatatype(obj) {
	for (var i=0; i < customDatatypes.length; i++) {
		var row = document.getElementById(customDatatypes[i] + "DatatypeRow");
		if (obj[obj.selectedIndex].text.toLowerCase() == customDatatypes[i])
			row.style.display = "";
		else
			row.style.display = "none";
	}
}

function listKeyPress(from, to, delim, event) {
	var keyCode = event.keyCode;
	if (keyCode == 8 || keyCode == 46) {
		removeItem(from, to, delim);
		window.Event.keyCode = 0;	//attempt to prevent backspace key (#8) from going back in browser
	}
}

function synonymKeyPress(obj, event, locale) {
	if (event.keyCode==13) {
		addSynonym(locale); 
		return false;
	}
	else if (event.keyCode == 46 && obj.value == "") {
		var selectedItem = removeItem('syns' + locale, 'newSynonyms' + locale, ',');
		if (selectedItem != null)
			selectedItem.selected = false;
	}
}

function hotkeys(event) {
	var k = event.keyCode;
	if (event.cntrlKey == true) {
		if (k == 86) { // v
			document.location = document.getElementById('viewConcept').href;
		}
	}
	if (k == 37) { // left key
		document.location = document.getElementById('previousConcept').href;
	}
	else if (k == 39) { //right key
		document.location = document.getElementById('nextConcept').href;
	}
}

document.onkeypress = hotkeys;
