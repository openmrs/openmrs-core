var conceptTimeout;
var conceptIndex;
var conceptsFound;
var allConceptsFound;
var conceptTableBody;
var text;
var textbox;
var includeRetired;
var keyCode;
var lastPhraseSearched;
var numItemsDisplayed;
var firstItemDisplayed;

var ENTERKEY = 13;

resetForm();

// clears variables. Equivalent to reloading the page.
function resetForm() {
	lastPhraseSearched = "";
	conceptsFound = new Array();
	allConceptsFound = new Array();
	text = "";
	textbox = null;
	includeRetired = false;
	firstItemDisplayed = 1;
	numItemsDisplayed = 0;
	document.onkeypress = hotkey;
	clearInformationBar();
	hideHighlight();
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
			if (!keyCode) {
				//if non-key event
				keyCode = event.which;
			}
		}
	}
	
	//reset textbox for mouse events
	if (keyCode == 1 && text == "") {
		text = lastPhraseSearched;
	}
	
	if (keyCode == 27) {
		//escape key pressed
		hideHighlight();
		textbox.value = lastPhraseSearched;
		return false;
	}
	else if (text == "" && includeRetired == retired) {
		//searched on empty string (and didn't change retired status)
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
			if (text == "")
				text = lastPhraseSearched;
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
		keyCode == 8 || keyCode == 32 || keyCode == 46 || keyCode == 1) {
			//	"if alpha key entered or 
			//   backspace key pressed or
			//   spacebar pressed or 
			//   delete key pressed or
			//   mouse event"
			hideHighlight();
			if (text.length > 1) {
				clearInformationBar();
				conceptTimeout = setTimeout("updateConcepts('" + text + "')", delay);
			}
	}
	
	//value applied here so as use 'includeRetired' as 'lastRetired' as well
	// (this will still be called before the updateConcepts() timeout is called.)
	includeRetired = retired;
	if (!retired){ includeRetired = false; }
	
	return false;
}

function updateConcepts(text) {
	clearTimeout(conceptTimeout);				//stop any timeout that may have just occurred...fixes 'duplicate data' error
	conceptsFound = new Array();				//zero-out numbered concept list
	conceptIndex = 1;							//our numbering is one-based
	firstItemDisplayed = 1;
	lastPhraseSearched = text;
	
	//must have at least 2 characters entered or that character be a number
	if (text.length > 1 || (parseInt(text) >= 0 && parseInt(text) <= 9)) {
	    if (typeof conceptClasses == 'undefined')	//conceptClasses is only optionally defined
	    	conceptClasses = new Array();
	    DWRConceptService.findConcepts(fillTable, text, conceptClasses, includeRetired);
	}
	else {
		conceptsFound[0] = "Invalid number of search characters";
		fillTable(conceptsFound);
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
	if (conceptsFound.length > 0) {
		var elements = document.getElementsByTagName('SPAN')
		for(i=0; i <elements.length;i++)
		{
			if(elements[i].className == 'conceptIndex')
				elements[i].className = 'conceptIndexHighlight';
		}
		if (textbox != null) {
			textbox.className = "conceptHighlight";
			textbox.focus();
		}
	}
}

function hideHighlight() {
	var elements = document.getElementsByTagName('SPAN')
	for(i=0; i <elements.length;i++)
	{
		if(elements[i].className == 'conceptIndexHighlight')
			elements[i].className = 'conceptIndex';
	}
	if (textbox != null) {
		textbox.className = "";
	}
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
				str = "<div class='retired'>" + str + "</div>";
			}
			conceptIndex = conceptIndex + 1;
			return str;
		}
	};

function fillTable(concepts) {
    // If we get only one result and the enter key was pressed jump to that concept
   	if (concepts.length == 1 && 
   		keyCode == ENTERKEY) {
   			if (typeof concepts[0] == 'string') {
   				// if only one string item returned, its the error message
   				hideHighlight();
			}
   			else {
		   		conceptsFound.push(concepts[0]);
		   		getCellContent("Forwarding...");
		   		selectConcept(1);
	   			return;
	   		}
	}
	
    allConceptsFound = concepts;
	
	updateInformationBar();

    DWRUtil.removeAllRows(conceptTableBody);	//clear out the current rows

    var c = concepts.slice(firstItemDisplayed - 1, firstItemDisplayed + numItemsDisplayed);
    DWRUtil.addRows(conceptTableBody, c, [ getNumber, getCellContent ]);
    
    if (keyCode == ENTERKEY) {
    	// showHighlighting must be called here to assure it occurs after 
    	// concepts are returned. Must be called with Timeout because 
    	// DWRUtil.addRows uses setTimeout
    	setTimeout("showHighlight()", 0);
    }
}

function showPrevious() {
	firstItemDisplayed = (firstItemDisplayed - numItemsDisplayed) - 1;
	if (firstItemDisplayed < 1) {
		firstItemDisplayed = 1;
		return false;
	}
	conceptIndex = firstItemDisplayed;
	fillTable(allConceptsFound);
	showHighlight();
	return false;
}

function showNext() {
	firstItemDisplayed = firstItemDisplayed + numItemsDisplayed + 1;
	if (firstItemDisplayed > allConceptsFound.length) {
		firstItemDisplayed = allConceptsFound.length;
		return false;
	}
	conceptIndex = firstItemDisplayed;
	fillTable(allConceptsFound);
	showHighlight();
	return false;
}

function updateInformationBar() {
	
	//TODO create another dwr method to just get total # of hits
	//     so that we don't need to return all 200 hits and only show #31-#45
	
	//create/find information bar
	var infoBar = document.getElementById("conceptSearchInfoBar");
	if (infoBar == null) {
		infoBar = document.createElement('div');
		infoBar.id = "conceptSearchInfoBar";
		var table = document.getElementById(conceptTableBody).parentNode;
		table.parentNode.insertBefore(infoBar, table);
	}
	
	infoBar.innerHTML = " Results for '" + lastPhraseSearched + "'. &nbsp;";
	
	// get top position of body element
	var tbody = document.getElementById(conceptTableBody); 
	var top = tbody.offsetTop;
	var parent = tbody.offsetParent;
	while (parent != null) {
		top+= parent.offsetTop;
		parent = parent.offsetParent;
	}
	
	var height = getRowHeight(); //approx. row height
	
	// get approx room below tablebody
	var remainder = getInnerHeight() - parseInt(top);
	numItemsDisplayed=Math.floor(remainder/(height + 6))-1;
	var total = allConceptsFound.length;
	var lastItemDisplayed = firstItemDisplayed + numItemsDisplayed;
	if (lastItemDisplayed > total) {
		lastItemDisplayed = total;
	}
	
	infoBar.innerHTML += " Viewing " + firstItemDisplayed + "-" + lastItemDisplayed + " of " + total + " &nbsp; ";
	
	if (firstItemDisplayed > 1) {
		//create previous link
		var prev = document.createElement("a");
		prev.href = "#prev";
		prev.className = "prevItems";
		prev.innerHTML = "Previous " + (firstItemDisplayed-numItemsDisplayed < 1 ? firstItemDisplayed - 1: numItemsDisplayed + 1) + " Results";
		prev.onclick = showPrevious;
		infoBar.appendChild(prev);
		if (lastItemDisplayed != total) {
			var s = document.createElement("span");
			s.innerHTML = " | ";
			infoBar.appendChild(s);	
		}
	}
	if (lastItemDisplayed < total) {
		//create next link
		var next = document.createElement("a");
		next.href = "#next";
		next.className = "nextItems";
		next.innerHTML = "Next " + (lastItemDisplayed+numItemsDisplayed > total ? total - lastItemDisplayed: numItemsDisplayed + 1 ) + " Results";
		next.onclick = showNext;
		infoBar.appendChild(next);
	}
}

function clearInformationBar() {
	var infoBar = document.getElementById("conceptSearchInfoBar");
	if (infoBar != null)
		infoBar.innerHTML = "&nbsp;";
}

function getStyle(obj,styleProp) {
	if (obj.currentStyle) {
		var y = obj.currentStyle[styleProp];
	}
	else if (window.getComputedStyle) {
		var y = document.defaultView.getComputedStyle(obj,null).getPropertyValue(styleProp);
	}
	return y;
}

function hotkey(event) {
	var k = event.keyCode;
	if (k == 33) { //PAGE UP
		showPrevious();
	}
	else if (k == 34) { // PAGE DOWN
		showNext();
	}
	else if (event.ctrlKey == true) { //if CONTROL-*
		if (k == 37) { //LEFT key
			showPrevious();
		}
		else if (k == 39) { //RIGHT key
			showNext();
		}
	}
}

function getInnerHeight() {
  var myWidth = 0, myHeight = 0;
  if( typeof( window.innerWidth ) == 'number' ) {
    //Non-IE
    myWidth = window.innerWidth;
    myHeight = window.innerHeight;
    //alert("myHeight1: " + myHeight);
  } else if( document.documentElement &&
      ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
    //IE 6+ in 'standards compliant mode'
    myWidth = document.documentElement.clientWidth;
    myHeight = document.documentElement.clientHeight;
	//alert("myHeight2: " + myHeight);
  } else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
    //IE 4 compatible
    myWidth = document.body.clientWidth;
    myHeight = document.body.clientHeight;
    //alert("myHeight3 : " + myHeight);
  }
  
  return parseInt(myHeight);
}

function getRowHeight() {
	var h = 0;
	h = getStyle(textbox, 'height');
	h = parseInt(h.slice(0, h.length - 2)); //remove 'px' from height
	if (parseInt(h) > 0) {
		return h;
	}
	// this silly code is brought to you by the great IE
	h = getStyle(textbox, 'lineHeight');
	if (h == 'largest')
		return 17;
	if (h == 'smallest')
		return 10;
	return 13; //normal
}