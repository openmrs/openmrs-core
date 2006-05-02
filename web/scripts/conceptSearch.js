function findObjects(text) {
	if (debugBox) debugBox.innerHTML += '<br> Entering findObjects for: ' + text;
	//must have at least 2 characters entered or that character be a number
	if (text.length > 1 || (parseInt(text) >= 0 && parseInt(text) <= 9)) {
	    if (typeof conceptClasses == 'undefined')	//conceptClasses is only optionally defined
	    	conceptClasses = new Array();
	    if (typeof preFillTable == 'function')
	    	DWRConceptService.findConcepts(preFillTable, text, conceptClasses, includeRetired);
	    else
	    	DWRConceptService.findConcepts(fillTable, text, conceptClasses, includeRetired);
	    if (debugBox) debugBox.innerHTML += '<br> DWRConceptService.findConcepts called';
	}
	else {
		var msg = new Array();
		msg.push("Invalid number of search characters");
		fillTable(msg);
	}
    return false;
}

var editConcept = function(event, index) {
	if (event.ctrlKey) {
		var win = window.open();
		win.location.href = "concept.form?conceptId=" + objectsFound[index-1].conceptId;
		}
	else {
		location.href = "concept.form?conceptId=" + objectsFound[index-1].conceptId;
	}
}

var editDrug = function(event, index) {
	// TODO complete this function after completing drug forms
}

var getCellContent = function(conceptHit) { 
	    if (typeof conceptHit == 'string') {
    		return conceptHit;
    	}
    	else if (conceptHit.drugId != null) {
    		var str = "";
    		str += "<a href=\"#selectDrug\" ";
			str += "onClick=\"return false; return selectObject('" + searchIndex + "');\" ";
			str += "onDblClick=\"return editDrug(event, '" + searchIndex + "')\" "
			str += "class='searchHit'>";
			str += conceptHit.fullName;
			if (showConceptIds())
				str += " (" + conceptHit.conceptId + ")";
			str += "</a>";
			return str;
    	}
	    else {
			var str = "";
			str += "<a href=\"#selectConcept\" ";
			str += "onClick=\"return false; return selectObject('" + searchIndex + "');\" ";
			str += "onDblClick=\"return editConcept(event, '" + searchIndex + "')\" "
			str += "title=\"" + conceptHit.description + "\" ";
			str += "class='searchHit'>";
			if (conceptHit.synonym != "" && conceptHit.synonym != null) {
				str += " <span class='mainHit'>" + conceptHit.synonym + "</span>";
				str += " <span class='additionalHit'>&rArr; " + conceptHit.name;
				if (showConceptIds())
					str += " (" + conceptHit.conceptId + ")";
				str += "</span>";
			}
			else {
				str += " <span class='mainHit'>" + conceptHit.name;
				if (showConceptIds())
					str += " (" + conceptHit.conceptId + ")";
				str += "</span>";
			}
			str += "</a>";
			if (conceptHit.retired) {
				str = "<div class='retired'>" + str + "</div>";
			}
			if ($('verboseListing').checked) {
				str += "<div class='description'>#" + conceptHit.conceptId + ": " + conceptHit.description + "</div>";
			}
			return str;
		}
	};
	
function customGetRowHeight(height) {
	var verbose = $('verboseListing');
	if (verbose != null && verbose.checked)
		return parseInt(height * 3);
	else
		return height;
}

function allowAutoListWithNumber() {
	return true;
}

function showConceptIds() {
	return false;
}