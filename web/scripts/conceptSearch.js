var conceptTimeout;
var conceptIndex;
var conceptsFound;
var conceptTableBody;
	
function searchBoxChange(bodyElementId, event, obj, delay) {
	if (!delay) {
		delay = 400;
	}
	conceptTableBody = bodyElementId;
	var text = obj.value;
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
			if (conceptsFound.length < 1)
			{
				//alert("Empty choice list!");
				return false;
			}
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
					alert("Invalid choice: \"" + textWords[i] + "\"");
					return false;
				}
			}
			onSelect(conceptsReturned);
			return false;
		}
		obj.focus();
		obj.select();
	}
	
	if ((keyCode > 57 && keyCode <= 127) || keyCode == 13) {
			// if alpha entered or enter key
			clearTimeout(conceptTimeout);
			conceptTimeout = setTimeout("updateConcepts('" + text + "')", delay);
	}
	return false;
}

function updateConcepts(text) {
	if (text.length > 2) {							//must have at least 3 characters entered
		conceptsFound = new Array();				//zero-out numbered concept list
		conceptIndex = 1;							//our numbering is one-based
		clearTimeout(conceptTimeout);				//stop any timeout that may have just occurred...fixes 'duplicate data' error
	    DWRUtil.removeAllRows(conceptTableBody);	//clear out the current rows
	    if (typeof conceptClasses == 'undefined')	//conceptClasses is only optionally defined
	    	conceptClasses = new Array();
	    DWRConceptService.findConcepts(fillTable, text , conceptClasses);
	}
    return false;
}

function selectConcept(index) {
	var conceptsReturned = new Array();
	conceptsReturned.push(conceptsFound[index-1]);
	onSelect(conceptsReturned);
}

var getNumber = function(concept) {
		conceptsFound.push(concept);
		var str = "";
		str = str + "<span class='conceptIndex'>";
		str = str + conceptIndex + ". ";
		str = str + "</span>";
		return str;
	};

var getCellContent = function(concept) { 
	    if (typeof concept == 'string') {
    		return concept;
    	}	
	    else {
			var str = "";
			str += "<a href=\"#addConcept\" onClick=\"selectConcept('" + conceptIndex + "'); return false;\" ";
			str += "class='conceptHit'>";
			str += concept.name;
			str += "(" + concept.conceptId + ")";
			str += "</a>";
			conceptIndex = conceptIndex + 1;
			return str;
		}
	};

function fillTable(concepts) {
    DWRUtil.addRows(conceptTableBody, concepts, [ getNumber, getCellContent ]);
}