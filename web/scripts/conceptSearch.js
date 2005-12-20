function findObjects(text) {
	
	//must have at least 2 characters entered or that character be a number
	if (text.length > 1 || (parseInt(text) >= 0 && parseInt(text) <= 9)) {
	    if (typeof conceptClasses == 'undefined')	//conceptClasses is only optionally defined
	    	conceptClasses = new Array();
	    DWRConceptService.findConcepts(fillTable, text, conceptClasses, includeRetired);
	}
	else {
		objectsFound[0] = "Invalid number of search characters";
		fillTable(objectsFound);
	}
    return false;
}

var getCellContent = function(conceptHit) { 
	    if (typeof conceptHit == 'string') {
    		return conceptHit;
    	}	
	    else {
			var str = "";
			str += "<a href=\"#selectObject\" onClick=\"selectObject('" + searchIndex + "'); return false;\" ";
			str += "class='searchHit'>";
			if (conceptHit.synonym != "") {
				str += " <span class='mainHit'>" + conceptHit.synonym + "</span>";
				str += " <span class='additionalHit'>&rArr; " + conceptHit.name + "</span>";
			}
			else {
				str += " <span class='mainHit'>" + conceptHit.name + "</span>";
			}
			str += "</a>";
			if (conceptHit.retired) {
				str = "<div class='retired'>" + str + "</div>";
			}
			return str;
		}
	};