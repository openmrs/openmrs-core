var nameListBox;
var idListBox;
var addButton;

window.onload = function() {
	myConceptSearchMod = new fx.Resize("conceptSearchForm", {duration: 100});
	myConceptSearchMod.hide();
	changeClass(document.getElementById("conceptClass"));
	changeDatatype(document.getElementById("datatype"));
};

function removeItem(nameList, idList, delim)
{
	var input = document.getElementById(idList);
	var sel = document.getElementById(nameList);
	var optList = sel.options;
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
		myConceptSearchMod.hide();
		nameListBox = nameList;	// used by onSelect()
		idListBox   = idList;	// used by onSelect()
	}
	
	conceptSearchForm = document.getElementById("conceptSearchForm");
	var test = "";
	var width = nameList.offsetWidth + 20;
	conceptSearchForm.style.left = (getElementLeft(nameList) + width) + "px";
	conceptSearchForm.style.top = (getElementTop(nameList)-50) + "px";
	
	DWRUtil.removeAllRows("conceptSearchBody");
	
	myConceptSearchMod.toggle();
	var searchText = document.getElementById("searchText");
	searchText.value = '';
	searchText.select();
	addButton = obj;
	//searchText.focus();  //why does this cause the inner box to shift position?!?
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

function getElementLeft(elm) {
	var x = 0;
	while (elm != null) {
		x+= elm.offsetLeft;
		elm = elm.offsetParent;
	}
	return parseInt(x);
}

function getElementTop(elm) {
	var y = 0;
	while (elm != null) {
		y+= elm.offsetTop;
		elm = elm.offsetParent;
	}
	return parseInt(y);
}

function addSynonym() {
	var obj = document.getElementById("addSyn");
	var synonyms = document.getElementById("syns").options;
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
	copyIds("syns", "newSynonyms", ",");
	window.Event.keyCode = 0;  //disable enter key submitting form
}

var onSelect = function(conceptList) {
	var options = nameListBox.options;		//nameListBox var from addConcept()
	for (i=0; i<conceptList.length; i++) {
		var addable = true;	
		var conceptId = conceptList[i].conceptId;
		var conceptName = conceptList[i].name;
		for (x=0; x<options.length; x++) {
			if (options[x].value == conceptId) {
				addable = false;
			}
		}
		if (addable) {
			var opt = new Option(conceptName + ' ('+conceptId+')', conceptId);
			opt.selected = true;
			options[options.length] = opt;
		}
			
	}
	copyIds(nameListBox.id, idListBox.id, ' ');
	myConceptSearchMod.hide();
	addButton.focus();
	resetForm();
};

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
	var row = document.getElementById("setClassRow");
	var isSet = false;
	for (var i=0; i < setClasses.length; i++) {
		if (obj[obj.selectedIndex].value == setClasses[i]) {
			isSet = true;
		}
	}
	if (isSet) {
		row.style.display = "";
	}
	else {
		row.style.display = "none";
	}
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

function synonymKeyPress(obj, event) {
	if (event.keyCode==13) {
		addSynonym(); 
		return false;
	}
	else if (event.keyCode == 46 && obj.value == "") {
		var selectedItem = removeItem('syns', 'newSynonyms', ',');
		if (selectedItem != null)
			selectedItem.selected = false;
	}
}