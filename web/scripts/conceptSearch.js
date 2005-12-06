var conceptTimeout;
var conceptIndex;
var conceptsFound = new Array();
var conceptTableBody;
var highlighted = false;
var text = "";
var includeRetired = false;
	
function searchBoxChange(bodyElementId, obj, event, retired, delay) {
	conceptTableBody = bodyElementId;
	includeRetired = retired;
	if (!delay)  { delay = 400; }
	if (!retired){ includeRetired = false; }
		
	text = obj.value;
	var keyCode = 0;
	if (event == null) { 
		// if onSubmit function called
		keyCode = 13;	//mimic user hitting enter key
	}
	else {
		if (event.altKey == false && event.ctrlKey == false) {
			// control and alt keys don't allow a search
			keyCode = event.keyCode;
		}
	}
	if (keyCode == 13) {
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
					if (textWords.length != 1) {
						//if only one number entered, assumed searching on number and not an error
						alert("Invalid choice: \"" + textWords[i] + "\"");
						return false;
					}
				}
			}
			if (conceptsReturned.length > 0)
				onSelect(conceptsReturned);
		}
		obj.focus();
		obj.select();
	}

	if ((keyCode > 57 && keyCode <= 127) ||
		(keyCode == 13)) {
			//	"if alpha key entered or 
			//	enter key pressed"
			clearTimeout(conceptTimeout);
			hideHighlight(obj);
			conceptTimeout = setTimeout("updateConcepts('" + text + "')", delay);
	}
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
	    DWRConceptService.findConcepts(fillTable, text , conceptClasses, includeRetired);
	}
    return false;
}

function selectConcept(index) {
	var conceptsReturned = new Array();
	conceptsReturned.push(conceptsFound[index-1]);
	onSelect(conceptsReturned);
}

function showHighlight(obj) {
	if (highlighted == false) {
		var TDs = document.getElementsByTagName('TD')
		for(i=0; i <TDs.length;i++)
		{
			if(TDs[i].className == 'conceptIndex')
				TDs[i].className = 'conceptIndexHighlight';
		}
		obj.className = "conceptHighlight";
	}
	highlighted = true;
}

function hideHighlight(obj) {
	if (highlighted) {
		var TDs = document.getElementsByTagName('TD')
		for(i=0; i <TDs.length;i++)
		{
			if(TDs[i].className == 'conceptIndexHighlight')
				TDs[i].className = 'conceptIndex';
		}
		obj.className = "";
	}
	highlighted = false;
}

var getNumber = function(concept) {
		conceptsFound.push(concept);
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
				str += " " + conceptHit.name;
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
    DWRUtil.addRows(conceptTableBody, concepts, [ getNumber, getCellContent ]);
    
    // If we get only one result and that result's conceptId is what we searched on, jump to that concept
   	if (concepts.length == 1 && concepts[0].conceptId == text) {
   		// timeout forces execution after "addRows"
   		// assumes findConcepts() appends a search on conceptId
   		setTimeout("selectConcept(1)", 0);
	}
}