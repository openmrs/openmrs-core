var conceptTimeout;
var conceptIndex;
var conceptsFound = new Array();
var conceptTableBody;
var text = "";
var textbox = null;
var includeRetired = false;
var keyCode;
var ENTERKEY = 13;
var lastPhraseSearched = "";
	
function searchBoxChange(bodyElementId, obj, event, retired, delay) {
	conceptTableBody = bodyElementId;
	textbox = obj;
	if (!delay)  { delay = 400; }
	
	clearTimeout(conceptTimeout);
	
	text = obj.value.toString();
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
		obj.value = lastPhraseSearched;
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
				return false;
			}
		}
		obj.focus();
		//obj.select();
		obj.value = "";
		if (text != lastPhraseSearched || includeRetired != retired) {
			conceptTimeout = setTimeout("updateConcepts('" + text + "')", 0);
		}
		else {
			showHighlight();
		}
	}

	else if ((keyCode > 57 && keyCode <= 127) ||
		(keyCode == 8 )) {
			//	"if alpha key entered or 
			//  backspace key pressed"
			hideHighlight();
			conceptTimeout = setTimeout("updateConcepts('" + text + "')", delay);
	}
	
	//value applied here so as use 'includeRetired' as 'lastRetired' as well
	// (this will still be called before the updateConcepts() timeout is called.)
	includeRetired = retired;
	if (!retired){ includeRetired = false; }
	
	return false;
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
				str += " &rArr " + conceptHit.name;
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
   		keyCode == ENTERKEY &&
   		typeof concepts[0] != 'string') {
	   		conceptsFound.push(concepts[0]);
	   		selectConcept(1);
	   		return;
	}

    DWRUtil.addRows(conceptTableBody, concepts, [ getNumber, getCellContent ]);
    
    if (keyCode == ENTERKEY) {
    	// showHighlighting must happen here to assure it occurs after concepts are returned.
    	// must be called with Timeout because DWRUtil.addRows uses setTimeout
    	setTimeout("showHighlight()", 0);
    }
}

// clears variables. Equivalent to reloading the page.
function resetForm() {
	lastPhraseSearched = "";
	var conceptsFound = new Array();
	var text = "";
	var textbox = null;
	var includeRetired = false;
	var lastPhraseSearched = "";
}
