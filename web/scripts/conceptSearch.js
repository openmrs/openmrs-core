var conceptTimeout;
var conceptIndex;
var conceptsFound;
var conceptTableBody;
var text;
var textbox;
var includeRetired;
var keyCode;
var lastPhraseSearched;

var ENTERKEY = 13;

resetForm();

// clears variables. Equivalent to reloading the page.
function resetForm() {
	lastPhraseSearched = "";
	conceptsFound = new Array();
	text = "";
	textbox = null;
	includeRetired = false;
}

function searchBoxChange(bodyElementId, obj, event, retired, delay) {
	conceptTableBody = bodyElementId;
	textbox = obj;
	if (!delay)  { delay = 400; }
	
	clearTimeout(conceptTimeout);
	
	text = textbox.value.toString();
	keyCode = 0;
	if (event == null) { 
		// if onSubmit function called
		keyCode = ENTERKEY;	//mimic user hitting enter key
	}
	else {
		if (event.altKey == false && event.ctrlKey == false) {
			// this if statement cancels the search on alt and control keys
			keyCode = event.keyCode;
		}
	}
	
	if (keyCode == 27) {
		hideHighlight();
		textbox.value = lastPhraseSearched;
		return false;
	}
	else if(text == "") {
		return false;
	}
	else if (keyCode == ENTERKEY) {
		hideHighlight();
		// if the user hit the enter key then check for sequence of numbers
		if (text.match(/^\s*\d+\s*(,\d+\s*)*$/))
		{
			var textWords = text.split(/\s*,\s*/);
			var conceptsReturned = new Array();
			for (i=0; i<textWords.length; i++)
			{
				if (textWords[i] > 0 && textWords[i] <= conceptsFound.length)
				{
					conceptsReturned.push(conceptsFound[textWords[i]-1]);
				}
				else
				{
					//if only one number entered, assumed searching on conceptId or just a number and its not an error
					if (textWords.length != 1) {
						alert("Invalid choice: '" + textWords[i] + "'");
						return false;
					}
					
				}
			}
			if (conceptsReturned.length > 0) {
				onSelect(conceptsReturned);
				textbox.value = lastPhraseSearched;
				return false;
			}
		}
		textbox.focus();
		//textbox.select();
		textbox.value = "";
		if (text != lastPhraseSearched || includeRetired != retired) {
			//this was a new search with the enter key pressed
			conceptTimeout = setTimeout("updateConcepts('" + text + "')", 0);
		}
		else if (conceptsFound.length == 1) {
			// this was a new redundant 'search' with enter key pressed and only one concept
			selectConcept(1);
		}
		else {
			// this was a new redundant 'search' with enter key pressed
			showHighlight();
		}
	}

	else if ((keyCode > 57 && keyCode <= 127) ||
		keyCode == 8 || keyCode == 32 || keyCode == 46) {
			//	"if alpha key entered or 
			//   backspace key pressed or
			//   spacebar pressed or 
			//   delete key pressed"
			hideHighlight();
			clearInformationBar();
			conceptTimeout = setTimeout("updateConcepts('" + text + "')", delay);
	}
	
	//value applied here so as use 'includeRetired' as 'lastRetired' as well
	// (this will still be called before the updateConcepts() timeout is called.)
	includeRetired = retired;
	if (!retired){ includeRetired = false; }
	
	return false;
}

function updateInformationBar() {
	
	//TODO create another dwr method to just get total # of hits
	//     so that we don't need to return all 200 hits and only show #31-#45
	
	var infoBar = document.getElementById("conceptSearchInfoBar");
	if (infoBar == null) {
		infoBar = document.createElement('div');
		infoBar.id = "conceptSearchInfoBar";
		var table = document.getElementById(conceptTableBody).parentNode;
		table.parentNode.insertBefore(infoBar, table);
	}
	infoBar.innerHTML = "Results for '" + lastPhraseSearched + "'";
}
function clearInformationBar() {
	var infoBar = document.getElementById("conceptSearchInfoBar");
	if (infoBar != null)
		infoBar.innerHTML = "&nbsp;";
}

function updateConcepts(text) {
	if (text.length > 1) {							//must have at least 2 characters entered
		conceptsFound = new Array();				//zero-out numbered concept list
		conceptIndex = 1;							//our numbering is one-based
	    if (typeof conceptClasses == 'undefined')	//conceptClasses is only optionally defined
	    	conceptClasses = new Array();
		clearTimeout(conceptTimeout);				//stop any timeout that may have just occurred...fixes 'duplicate data' error
	    DWRUtil.removeAllRows(conceptTableBody);	//clear out the current rows
	    DWRConceptService.findConcepts(fillTable, text, conceptClasses, includeRetired);
	    lastPhraseSearched = text;
	}
    return false;
}

function selectConcept(index) {
	var conceptsReturned = new Array();
	if (conceptsFound.length > 0) {
		conceptsReturned.push(conceptsFound[index-1]);
		//textbox.value = lastPhraseSearched;
		onSelect(conceptsReturned);
	}
}

function showHighlight() {
	var elements = document.getElementsByTagName('SPAN')
	for(i=0; i <elements.length;i++)
	{
		if(elements[i].className == 'conceptIndex')
			elements[i].className = 'conceptIndexHighlight';
	}
	textbox.className = "conceptHighlight";
}

function hideHighlight() {
	var elements = document.getElementsByTagName('SPAN')
	for(i=0; i <elements.length;i++)
	{
		if(elements[i].className == 'conceptIndexHighlight')
			elements[i].className = 'conceptIndex';
	}
	textbox.className = "";
}

var getNumber = function(conceptHit) {
	    if (typeof conceptHit == 'string') {
    		return "";
    	}	
		conceptsFound.push(conceptHit);
		var str = "";
		str = str + "<span class='conceptIndex'>";
		str = str + conceptIndex + ". ";
		str = str + "</span>";
		return str;
	};

var getCellContent = function(conceptHit) { 
	    if (typeof conceptHit == 'string') {
    		return conceptHit;
    	}	
	    else {
			var str = "";
			str += "<a href=\"#selectConcept\" onClick=\"selectConcept('" + conceptIndex + "'); return false;\" ";
			str += "class='conceptHit'>";
			if (conceptHit.synonym != "") {
				str += " <span class='mainHit'>" + conceptHit.synonym + "</span>";
				str += " <span class='additionalHit'>&rArr; " + conceptHit.name + "</span>";
			}
			else {
				str += " <span class='mainHit'>" + conceptHit.name + "</span>";
			}
			str += "</a>";
			if (conceptHit.retired) {
				str = "<span class='retired'>" + str + "</span>";
			}
			conceptIndex = conceptIndex + 1;
			return str;
		}
	};

function fillTable(concepts) {
    // If we get only one result and the enter key was pressed jump to that concept
   	if (concepts.length == 1 && 
   		keyCode == ENTERKEY)
   			if (typeof concepts[0] == 'string') {
   				hideHighlight();
   			else {
		   		conceptsFound.push(concepts[0]);
		   		getCellContent("Forwarding...");
		   		selectConcept(1);
	   			return;
	   		}
	}

    DWRUtil.addRows(conceptTableBody, concepts, [ getNumber, getCellContent ]);
    
	updateInformationBar();
    
    if (keyCode == ENTERKEY) {
    	// showHighlighting must be called here to assure it occurs after 
    	// concepts are returned. Must be called with Timeout because 
    	// DWRUtil.addRows uses setTimeout
    	setTimeout("showHighlight()", 0);
    }
}