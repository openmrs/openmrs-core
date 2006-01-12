
// General OpenMRS searching display algorithm
// Author: Ben Wolfe
// Version: 1.5
//
// To use, import this file in the html and create (at least) two methods similar to these:
//
//	findObjects(textPhrase) {
//	  DWRConceptService.findConcepts(fillTable, text, includeRetired);
//	}
//	var getCellContent = function(obj) {
//	  return "<a href='#' onClick=selectObject('" + searchIndex + "')>" + obj.name + "</a>";
//	};
//  var onSelect(array) {
//    alert(array[0].id)'
//  }
//
//	OR for more complicated displays:
//
//	(same as above)
//	findObjects(textPhrase) {
//	  DWRService.findConcepts(fillTable, text, includeRetired);
// 
//	}
//  var onSelect(array) {
//    alert(array[0].id)'
//  }
//	var getCellContent = function(obj) {
//	  return "<a href='#' onClick=selectObject('" + searchIndex + "')>" + obj.name + "</a>";
//	};
//	var customCellFunctions = [getNumber, getCellContent, getCell2Content, getCell3Content];
//	Note: getNumber increments the searchIndex
//	var getCell2Content = function(obj) {
//	  return obj.age;
//	  };
//	var getCell3Content = function(obj) {
//	  return obj.group;
//	};



var searchTimeout;
var searchIndex;
var objectsFound;
var allObjectsFound;
var objectHitsTableBody;
var text;
var textbox;
var includeRetired;
var keyCode;
var lastPhraseSearched;
var numItemsDisplayed;
var firstItemDisplayed;

var debugBox;

var ENTERKEY = 13;

resetForm();

// clears variables. Equivalent to reloading the page.
//  (useful for pages with multiple search boxes)
function resetForm() {
	hideHighlight();
	lastPhraseSearched = null;
	objectsFound = new Array();
	allObjectsFound = new Array();
	text = null;
	textbox = null;
	includeRetired = false;
	searchIndex = 0;
	firstItemDisplayed = 1;
	numItemsDisplayed = 0;
	document.onkeypress = hotkey;
	clearInformationBar();
	debugBox = $("debugBox");
	if (debugBox) debugBox.innerHTML = "";
}

function searchBoxChange(bodyElementId, obj, event, retired, delay) {
	if (debugBox) debugBox.innerHTML += '<br>---- delay: ' + delay;
	objectHitsTableBody = $(bodyElementId);
	textbox = obj;
	if (!delay)  { delay = 400; }
	clearTimeout(searchTimeout);
	text = textbox.value.toString();
		
	key = 0;
	if (event == null) { 
		// if onSubmit function called
		key = ENTERKEY;	//mimic user hitting enter key
	}
	else {
		if (!event.altKey && !event.ctrlKey) {
			// this if statement cancels the search on alt and control keys
			key = event.keyCode;
			if (!key && (event.type == "click" || event.type == "change")) {
				//if non-key event like clicking checkbox or changing dropdown list
				key = 1;
			}
		}
	}
	if (debugBox) debugBox.innerHTML += '<br> key: ' + key;
	
	//reset textbox for mouse events
	if (key == 1 && text == "") {
		text = lastPhraseSearched;
	}
	
	if (key == 27) {
		//escape key pressed
		exitNumberMode(textbox);
		return false;
	}
	else if (text == "" && includeRetired == retired) {
		//searched on empty string (and didn't change retired status)
		return false;
	}
	else if (key == ENTERKEY) {
		if (debugBox) debugBox.innerHTML += '<br> Enter key pressed, search: ' + text;
		hideHighlight();
		// if the user hit the enter key then check for sequence of numbers
		if (text.match(/^\s*\d+\s*(,\d+\s*)*$/))
		{
			if (debugBox) debugBox.innerHTML += '<br> text matched set of numbers';
			var textWords = text.split(/\s*,\s*/);
			var objectsReturned = new Array();
			for (i=0; i<textWords.length; i++)
			{
				if (textWords[i] > 0 && textWords[i] <= objectsFound.length)
				{
					objectsReturned.push(objectsFound[textWords[i]-1]);
				}
				else
				{
					//if only one number entered, assumed searching on object id or just a number and its not an error
					if (textWords.length != 1) {
						alert("Invalid choice: '" + textWords[i] + "'");
						return false;
					}
				}
			}
			if (objectsReturned.length > 0) {
				onSelect(objectsReturned);
				textbox.value = lastPhraseSearched;
				return false;
			}
		}
		textbox.focus();
		//textbox.select();
		textbox.value = "";
		if (debugBox) debugBox.innerHTML += '<br> textbox.value cleared';
		if (text != lastPhraseSearched || includeRetired != retired || (typeof allowNewSearch != 'undefined' && allowNewSearch() == true)) {
			//this was a new search with the enter key pressed
			if (debugBox) debugBox.innerHTML += '<br> This was a new search';
			if (text == "")
				text = lastPhraseSearched;
			keyCode = key; 	//save keyCode for later testing in fillTable()
			preFindObjects(text);
			if (debugBox) debugBox.innerHTML += '<br> preFindObjects called for ENTERKEY';
		}
		else if (objectsFound.length == 1 && (typeof allowAutoJump != 'undefined' && allowAutoJump() == true)) {
			// this was a new redundant 'search' with enter key pressed and only one object
			selectObject(1);
		}
		else {
			// this was a new redundant 'search' with enter key pressed
			showHighlight();
			if (debugBox) debugBox.innerHTML += '<br> This was a redundant search';
		}
	}

	else if ((key > 57 && key <= 127) ||
		key == 8 || key == 32 || key == 46 || key == 1) {
			//	"if alpha key entered or 
			//   backspace key pressed or
			//   spacebar pressed or 
			//   delete key pressed or
			//   mouse event"
			hideHighlight();
			if (text.length > 1) {
				clearInformationBar();
				if (debugBox) debugBox.innerHTML += '<br> setting preFindObjects timeout for other key: ' + key;
				keyCode = key;	//save keyCode for later testing in fillTable
				searchTimeout = setTimeout("preFindObjects(text)", delay);
				if (debugBox) debugBox.innerHTML += '<br> preFindObjects timeout called for other key: ' + key;
			}
	}
	
	//value applied here so as use 'includeRetired' as 'lastRetired' as well
	// (this will still be called before the preFindObjects() timeout is called.)
	includeRetired = retired;
	if (!retired){ includeRetired = false; }
	
	return false;
}

function preFindObjects(phrase) {
	if (debugBox) debugBox.innerHTML += '<br> preFindObjects initialized with search on: ' + phrase;
	clearTimeout(searchTimeout);			//stop any timeout that may have just occurred...fixes 'duplicate data' error
	objectsFound = new Array();				//zero-out numbered object list
	searchIndex = 0;						//our numbering is one-based, but the searchIndex is incremented prior to printing
	firstItemDisplayed = 1;					//zero-out our paging index (but we have a one-based list, see line above)
	lastPhraseSearched = text;
	
	if (debugBox) debugBox.innerHTML += '<br> findObjects being called';
	
	var b = findObjects(phrase);
	
	if (debugBox) debugBox.innerHTML += '<br> findObjects called';
	
	return b;
}

function selectObject(index) {
	var objectsReturned = new Array();
	if (objectsFound.length > 0) {
		objectsReturned.push(objectsFound[index-1]);
		//textbox.value = lastPhraseSearched;
		onSelect(objectsReturned);
	}
}

function showHighlight(box) {
	if (objectsFound.length > 0) {
		var elements = document.getElementsByTagName('TD')
		for(i=0; i <elements.length;i++)
		{
			if(elements[i].className == 'searchIndex')
				elements[i].className = 'searchIndexHighlight';
		}
		if (box != null)
			obj = box;
		else
			obj = textbox;
			
		if (obj != null) {
			obj.className = "searchHighlight";
			obj.focus();
		}
	}
}

function hideHighlight(box) {
	var elements = document.getElementsByTagName('TD')
	for(i=0; i <elements.length;i++)
	{
		if(elements[i].className == 'searchIndexHighlight')
			elements[i].className = 'searchIndex';
	}
	if (box != null)
		obj = box;
	else
		obj = textbox;
		
	if (obj != null) {
		obj.className = "";
	}
}

var getNumber = function(searchHit) {
	    if (typeof searchHit == 'string') {
    		return "";
    	}
		objectsFound.push(searchHit);
		searchIndex = searchIndex + 1;
		var td = document.createElement("td");
		td.className = "searchIndex";
		td.innerHTML = searchIndex + ". ";
		return td;
	};
var getString  = function(s) { return s; };

function fillTable(objects, cells) {
    // If we get only one result and the enter key was pressed jump to that object
   	if (objects.length == 1 && 
   		keyCode == ENTERKEY) {
   			if (typeof objects[0] == 'string') {
   				// if only one string item returned, its a message
   				hideHighlight();
			}
   			else if (typeof allowAutoJump != 'undefined' && allowAutoJump() == true){
		   		objectsFound.push(objects[0]);
		   		selectObject(1);
	   			return;
	   		}
	}
	
    allObjectsFound = objects;
	
	updatePagingNumbers();

    DWRUtil.removeAllRows(objectHitsTableBody);	//clear out the current rows

    var objs = objects.slice(firstItemDisplayed - 1, firstItemDisplayed + numItemsDisplayed);
    var funcs = new Array();
    if (cells != null)
    	funcs = cells;
    else if (typeof customCellFunctions == "undefined")
    	funcs = [ getNumber, getCellContent ];
    else
    	funcs = customCellFunctions;
    DWRUtil.addRows(objectHitsTableBody, objs, funcs);
    
   	setTimeout("updateInformationBar()", 0);
    
    if (keyCode == ENTERKEY) {
    	// showHighlighting must be called here to assure it occurs after 
    	// objects are returned. Must be called with Timeout because 
    	// DWRUtil.addRows uses setTimeout
    	if (debugBox) debugBox.innerHTML += "<br>showing highlight at end of fillTable() due to enterkey";
    	setTimeout("showHighlight()", 0);
    }
    if (debugBox) debugBox.innerHTML += "<br>ending fillTable().  Keycode was: " + keyCode;
}

function showPrevious() {
	firstItemDisplayed = (firstItemDisplayed - numItemsDisplayed) - 1;
	if (firstItemDisplayed < 1) {
		firstItemDisplayed = 1;
		return false;
	}
	searchIndex = firstItemDisplayed - 1;
	fillTable(allObjectsFound);
	//if we're in 'number mode'
	if (textbox.value == "") 
		showHighlight();
	return false;
}

function showNext() {
	firstItemDisplayed = firstItemDisplayed + numItemsDisplayed + 1;
	if (firstItemDisplayed > allObjectsFound.length) {
		firstItemDisplayed = allObjectsFound.length;
		return false;
	}
	searchIndex = firstItemDisplayed - 1;
	fillTable(allObjectsFound);
	//if we're in 'number mode'
	if (textbox.value == "") 
		showHighlight();
	return false;
}

function updatePagingNumbers() {

	//create/find information bar
	var infoBar = document.getElementById("searchInfoBar");
	if (infoBar == null) {
		infoBar = document.createElement('div');
		infoBar.id = "searchInfoBar";
		infoBar.innerHTML = "&nbsp;";
		var table = objectHitsTableBody.parentNode;
		table.parentNode.insertBefore(infoBar, table);
	}

	// get top position of body element
	var tbody = objectHitsTableBody;
	var top = tbody.offsetTop;
	var parent = tbody.offsetParent;
	while (parent != null) {
		top+= parent.offsetTop;
		parent = parent.offsetParent;
	}
	
	var height = getRowHeight(); //approx. row height
	
	// get approx room below tablebody
	var remainder = getWindowHeight() - parseInt(top);
	numItemsDisplayed=Math.floor(remainder/(height + 6))-1;
}

function updateInformationBar() {
	
	//TODO optional: create another dwr method to just get total # of hits
	//     so that we don't need to return all 200 hits and only show #31-#45
	
	var infoBar = document.getElementById("searchInfoBar");
	
	var total = allObjectsFound.length;
	var lastItemDisplayed = firstItemDisplayed + numItemsDisplayed;
	if (lastItemDisplayed > total) {
		lastItemDisplayed = total;
	}
	
	infoBar.innerHTML = " Results for '" + lastPhraseSearched + "'. &nbsp;";
	
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
	var infoBar = document.getElementById("searchInfoBar");
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
	if (event.ctrlKey == true) { //if CONTROL-*
		if (k == 33) { //PAGE UP
			showPrevious();
		}
		else if (k == 34) { // PAGE DOWN
			showNext();
		}
		else if (k == 37) { //LEFT key
			showPrevious();
		}
		else if (k == 39) { //RIGHT key
			showNext();
		}
	}
}

function getWindowHeight() {
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
	// this silly code is brought to you courtesy of the ever-standards-compliant IE web browser designers
	h = getStyle(textbox, 'lineHeight');
	if (h == 'largest')
		return 17;
	if (h == 'smallest')
		return 10;
	return 13; //normal
}

function exitNumberMode(txtbox) {
	hideHighlight(txtbox);
	txtbox.value = lastPhraseSearched;
}